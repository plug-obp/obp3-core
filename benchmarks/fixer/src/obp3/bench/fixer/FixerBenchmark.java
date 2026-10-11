package obp3.bench.fixer;

import obp3.fixer.Fixer;
import obp3.fixer.Lattice;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/** Same fixture and assertions for the two source versions; no production dependencies beyond Fixer/Lattice. */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class FixerBenchmark {
    public record Key(int index) {}

    public static final class Fixture {
        final Key[] keys;
        final Key[] requests;
        final Supplier<Map> maps;
        final Lattice<Long> lattice;
        final BiFunction<Key, Function<Key, Long>, Long> equation;
        final String workload;
        final long expected;

        Fixture(String workload, String mapMode) {
            this.workload = workload;
            int size = switch (workload) {
                case "chain", "booleanCycle" -> 2000;
                case "powersetRing" -> 64;
                case "independent" -> 256;
                case "topFanout" -> 512;
                default -> throw new IllegalArgumentException(workload);
            };
            keys = new Key[size];
            requests = new Key[size];
            for (int i = 0; i < size; i++) {
                keys[i] = new Key(i);
                requests[i] = mapMode.equals("identity") ? keys[i] : new Key(i);
            }
            maps = mapMode.equals("identity") ? IdentityHashMap::new : HashMap::new;
            // Cycles have no top optimization; topFanout isolates the eager/deferred edge tradeoff.
            lattice = workload.equals("topFanout")
                    ? new Lattice<>(0L, 1L, Objects::equals) : new Lattice<>(0L, Objects::equals);
            expected = workload.equals("powersetRing") ? -1L : 1L;
            equation = (key, request) -> switch (workload) {
                case "chain" -> key.index == size - 1 ? 1L : request.apply(requests[key.index + 1]);
                case "booleanCycle" -> request.apply(requests[(key.index + 1) % size]) | (key.index == size - 1 ? 1L : 0L);
                case "powersetRing" -> request.apply(requests[(key.index + 1) % size]) | (1L << key.index);
                case "independent" -> 1L;
                case "topFanout" -> {
                    if (key.index == 0) {
                        for (int i = 1; i < size; i++) request.apply(requests[i]);
                    }
                    yield 1L;
                }
                default -> throw new AssertionError(workload);
            };
        }

        Fixer<Key, Long> newSolver() { return new Fixer<>(equation, lattice, maps); }

        long solve(Fixer<Key, Long> solver) {
            if (workload.equals("independent")) {
                long sum = 0;
                for (Key key : requests) sum += solver.apply(key);
                return sum;
            }
            return solver.apply(requests[0]);
        }

        void verify(Fixer<Key, Long> solver) {
            long actual = solve(solver);
            long wanted = workload.equals("independent") ? keys.length : expected;
            if (actual != wanted) throw new AssertionError(workload + ": " + actual + " != " + wanted);
            for (Key key : requests) {
                if (solver.apply(key) != expected) throw new AssertionError("Incorrect cached solution: " + key);
            }
        }
    }

    @State(Scope.Thread)
    public static class ColdState {
        @Param({"identity", "structural"}) public String mapMode;
        @Param({"chain", "booleanCycle", "powersetRing", "independent", "topFanout"}) public String workload;
        Fixture fixture;
        @Setup(Level.Trial) public void setup() {
            fixture = new Fixture(workload, mapMode);
            fixture.verify(fixture.newSolver());
        }
    }

    @State(Scope.Thread)
    public static class CachedState {
        @Param({"identity", "structural"}) public String mapMode;
        Fixture fixture;
        Fixer<Key, Long> solver;
        @Setup(Level.Trial) public void setup() {
            fixture = new Fixture("independent", mapMode);
            solver = fixture.newSolver();
            fixture.verify(solver);
        }
    }

    /** One fresh solver plus the whole demanded solution (256 public calls for independent). */
    @Benchmark public long coldSolve(ColdState state) {
        return state.fixture.solve(state.fixture.newSolver());
    }

    /** 256 cached public calls, normalized by JMH to one lookup. Keys are already allocated. */
    @Benchmark @OperationsPerInvocation(256)
    public long cachedQuery(CachedState state) {
        long sum = 0;
        for (Key key : state.fixture.requests) sum += state.solver.apply(key);
        return sum;
    }

    public static void main(String[] args) {
        for (String mapMode : new String[]{"identity", "structural"}) {
            for (String workload : new String[]{"chain", "booleanCycle", "powersetRing", "independent", "topFanout"}) {
                Fixture fixture = new Fixture(workload, mapMode);
                fixture.verify(fixture.newSolver());
                System.out.println("verified," + mapMode + "," + workload);
            }
        }
    }
}
