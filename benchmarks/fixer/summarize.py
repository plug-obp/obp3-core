#!/usr/bin/env python3
"""Validate and summarize the raw warmed JMH comparison; writes summary.csv."""
import argparse
import csv
import json
from pathlib import Path
import statistics


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    args = parser.parse_args()
    directory = args.directory
    metadata = json.loads((directory / "metadata.json").read_text())
    if metadata["smoke_only"]:
        raise SystemExit("Smoke run is not a performance measurement")
    results = {}
    for label in ["minimal", "refactor"]:
        results[label] = {}
        rows = json.loads((directory / f"{label}-jmh.json").read_text())
        expected = metadata.get("case_count", 12)
        if len(rows) != expected:
            raise SystemExit(f"Incomplete {label}: expected {expected} cases, got {len(rows)}")
        for row in rows:
            raw = row["primaryMetric"]["rawData"]
            if len(raw) != 3 or any(len(fork) != 5 for fork in raw):
                raise SystemExit(f"Missing fork/iteration results: {row['params']}")
            key = (row["benchmark"].split(".")[-1], row["params"]["mapMode"],
                   row["params"].get("workload", "cache"))
            results[label][key] = row
        for repeat in [2, 3]:
            if ((directory / f"{label}-footprint-{repeat}.csv").read_bytes()
                    != (directory / f"{label}-footprint-1.csv").read_bytes()):
                raise SystemExit(f"Footprints differ across repeats for {label}")
    columns = ["method", "map_mode", "workload", "variant", "time_unit", "mean",
               "error_99_9pct", "fork_mean_min", "fork_mean_max", "allocation_bytes_per_op"]
    with (directory / "summary.csv").open("w") as output:
        writer = csv.DictWriter(output, fieldnames=columns, lineterminator="\n")
        writer.writeheader()
        for key in results["minimal"]:
            method, mode, workload = key
            scale = 1000 if method == "cachedQuery" else 1
            for label in ["minimal", "refactor"]:
                row = results[label][key]
                metric = row["primaryMetric"]
                forks = [statistics.mean(fork) * scale for fork in metric["rawData"]]
                data = {"method": method, "map_mode": mode, "workload": workload,
                        "variant": label, "time_unit": "ns/op" if scale == 1000 else "us/op",
                        "mean": metric["score"] * scale,
                        "error_99_9pct": metric["scoreError"] * scale,
                        "fork_mean_min": min(forks), "fork_mean_max": max(forks),
                        "allocation_bytes_per_op": row["secondaryMetrics"]["gc.alloc.rate.norm"]["score"]}
                writer.writerow(data)
                print(f"{label:8} {mode:10} {workload:13} {data['mean']:.4f} ± {data['error_99_9pct']:.4f} "
                      f"{data['time_unit']:5} {data['allocation_bytes_per_op']:.1f} B/op")


if __name__ == "__main__":
    main()
