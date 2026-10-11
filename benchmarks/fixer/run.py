#!/usr/bin/env python3
"""Compile identical standalone JMH fixtures against two committed solver revisions."""
import argparse
from datetime import datetime, timezone
import hashlib
import json
import os
from pathlib import Path
import platform
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
SOURCES = ["obp3-algos/src/main/java/obp3/fixer/Fixer.java",
           "obp3-algos/src/main/java/obp3/fixer/Lattice.java"]
FLAGS = ["-Xms256m", "-Xmx256m", "-XX:+UseG1GC"]


def capture(command):
    return subprocess.check_output(command, cwd=ROOT, text=True, stderr=subprocess.STDOUT).strip()


def jar(cache, group, name, version):
    candidates = sorted((cache / group / name / version).glob(f"*/{name}-{version}.jar"))
    if len(candidates) != 1:
        raise SystemExit(f"Expected one cached {name}:{version} jar in {cache}, found {candidates}")
    return candidates[0]


def run_to_file(command, destination):
    print("Running", " ".join(map(str, command)), flush=True)
    with destination.open("w") as output:
        subprocess.run(command, cwd=ROOT, text=True, stdout=output, stderr=subprocess.STDOUT, check=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--java-home", type=Path, default=os.environ.get("JAVA_HOME"))
    parser.add_argument("--gradle-cache", type=Path,
                        default=Path.home() / ".gradle/caches/modules-2/files-2.1")
    parser.add_argument("--dependency-dir", type=Path,
                        help="Directory containing the four documented JMH/runtime jars")
    parser.add_argument("--minimal-ref", required=True)
    parser.add_argument("--refactor-ref", default="7f7dab9a3c184803760764d1e7247346ad8b6af8")
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--smoke", action="store_true", help="One short un-warmed fork; NOT a performance result")
    parser.add_argument("--case", action="append", dest="cases",
                        help="Restrict to method:mapMode:workload, e.g. coldSolve:identity:chain")
    parser.add_argument("--reverse-order", action="store_true", help="Reverse the normal paired variant order")
    args = parser.parse_args()
    if not args.java_home:
        parser.error("--java-home or JAVA_HOME is required")
    java = str(args.java_home / "bin/java")
    javac = str(args.java_home / "bin/javac")
    jar_tool = str(args.java_home / "bin/jar")
    coordinates = [
        ("org.openjdk.jmh", "jmh-core", "1.37"),
        ("org.openjdk.jmh", "jmh-generator-annprocess", "1.37"),
        ("net.sf.jopt-simple", "jopt-simple", "5.0.4"),
        ("org.apache.commons", "commons-math3", "3.6.1"),
    ]
    dependencies = [args.dependency_dir / f"{name}-{version}.jar" if args.dependency_dir
                    else jar(args.gradle_cache, group, name, version)
                    for group, name, version in coordinates]
    output = args.output.resolve()
    output.mkdir(parents=True, exist_ok=False)
    metadata = {
        "started_at_utc": datetime.now(timezone.utc).isoformat(),
        "java": capture([java, "-version"]), "platform": platform.platform(),
        "machine": platform.machine(), "cpu_count": os.cpu_count(),
        "jvm_flags": FLAGS, "jmh": "1.37", "smoke_only": args.smoke,
        "sources": {}, "commands": {},
        "dependency_sha256": {path.name: hashlib.sha256(path.read_bytes()).hexdigest()
                              for path in dependencies},
        "harness_sha256": {str(path.relative_to(HERE)): hashlib.sha256(path.read_bytes()).hexdigest()
                           for path in sorted(HERE.rglob("*.java"))},
    }
    if platform.system() == "Darwin":
        metadata["cpu"] = capture(["sysctl", "-n", "machdep.cpu.brand_string"])
        metadata["ram_bytes"] = capture(["sysctl", "-n", "hw.memsize"])
    cp = os.pathsep.join(map(str, dependencies))
    jmh_options = (["-f", "1", "-wi", "0", "-i", "1", "-r", "100ms"] if args.smoke else
                   ["-f", "3", "-wi", "3", "-w", "500ms", "-i", "5", "-r", "500ms"])
    metadata["run_order"] = ("Reverse normal paired order" if args.reverse_order else
                             "Alternate which variant runs first for each paired workload/key-policy case")
    with tempfile.TemporaryDirectory(prefix="fixer-comparison-") as temporary:
        benchmark_commands = {}
        for label, revision in [("minimal", args.minimal_ref), ("refactor", args.refactor_ref)]:
            revision = capture(["git", "rev-parse", revision])
            build = Path(temporary) / label
            build.mkdir()
            classes = build / "classes"
            classes.mkdir()
            local_sources = []
            hashes = {}
            for source in SOURCES:
                content = subprocess.check_output(["git", "show", f"{revision}:{source}"], cwd=ROOT)
                path = build / Path(source).name
                path.write_bytes(content)
                local_sources.append(str(path))
                hashes[source] = hashlib.sha256(content).hexdigest()
            metadata["sources"][label] = {"revision": revision, "sha256": hashes}
            command = [javac, "-cp", cp, "-processorpath", cp, "-d", str(classes),
                       *local_sources, *map(str, sorted((HERE / "src").rglob("*.java")))]
            run_to_file(command, output / f"{label}-compile.txt")
            runtime_cp = str(classes) + os.pathsep + cp
            manifest = build / "MANIFEST.MF"
            manifest.write_text("Premain-Class: obp3.bench.fixer.FootprintAgent\n\n")
            agent = build / "footprint-agent.jar"
            subprocess.run([jar_tool, "cfm", str(agent), str(manifest), "-C", str(classes),
                            "obp3/bench/fixer/FootprintAgent.class"], check=True)
            verify = [java, *FLAGS, "-cp", runtime_cp, "obp3.bench.fixer.FixerBenchmark"]
            run_to_file(verify, output / f"{label}-verify.txt")
            footprint = [java, *FLAGS, f"-javaagent:{agent}",
                         "--add-opens=java.base/java.util=ALL-UNNAMED",
                         "--add-opens=java.base/java.lang=ALL-UNNAMED",
                         "--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED",
                         "-cp", runtime_cp, "obp3.bench.fixer.Footprint"]
            for repeat in range(1, 4):
                run_to_file(footprint, output / f"{label}-footprint-{repeat}.csv")
            benchmark_commands[label] = [java, *FLAGS, "-cp", runtime_cp, "org.openjdk.jmh.Main"]
            metadata["commands"][label] = {"compile": command, "verify": verify,
                                            "footprint": footprint, "jmh": []}
        combined = {"minimal": [], "refactor": []}
        cases = [("cachedQuery", mode, None) for mode in ["identity", "structural"]]
        cases += [("coldSolve", mode, workload)
                  for workload in ["chain", "booleanCycle", "powersetRing", "independent", "topFanout"]
                  for mode in ["identity", "structural"]]
        case_names = [f"{method}:{mode}:{workload or 'cache'}" for method, mode, workload in cases]
        if args.cases and set(args.cases) - set(case_names):
            raise ValueError(f"Unknown cases: {set(args.cases) - set(case_names)}")
        metadata["case_count"] = len(set(args.cases)) if args.cases else len(cases)
        for case_id, (method, mode, workload) in enumerate(cases):
            if args.cases and case_names[case_id] not in args.cases:
                continue
            order = ["minimal", "refactor"] if case_id % 2 == 0 else ["refactor", "minimal"]
            if args.reverse_order:
                order.reverse()
            for label in order:
                result_file = output / f"{label}-jmh-{case_id:02d}.json"
                benchmark = [*benchmark_commands[label], f"obp3.bench.fixer.FixerBenchmark.{method}",
                             *jmh_options, "-p", f"mapMode={mode}", "-t", "1", "-prof", "gc",
                             "-rf", "json", "-rff", str(result_file), "-jvmArgs", " ".join(FLAGS)]
                if workload is not None:
                    benchmark += ["-p", f"workload={workload}"]
                metadata["commands"][label]["jmh"].append(benchmark)
                (output / "metadata.json").write_text(json.dumps(metadata, indent=2) + "\n")
                run_to_file(benchmark, output / f"{label}-jmh-{case_id:02d}.txt")
                result_text = result_file.read_text().rstrip() + "\n"
                result_file.write_text(result_text)
                results = json.loads(result_text)
                if len(results) != 1:
                    raise RuntimeError(f"Expected exactly one successful JMH result in {result_file}")
                combined[label] += results
                (output / f"{label}-jmh.json").write_text(json.dumps(combined[label], indent=2) + "\n")
    metadata["finished_at_utc"] = datetime.now(timezone.utc).isoformat()
    (output / "metadata.json").write_text(json.dumps(metadata, indent=2) + "\n")
    print(f"Results saved to {output}", flush=True)


if __name__ == "__main__":
    main()
