package obp3.bench;

import obp3.experiments.Fibonacci;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
public class FibonacciBench {

    int n = 45;
    Fibonacci fib = new Fibonacci();

    @Benchmark
    public void recursive(Blackhole blackhole) {
        blackhole.consume(fib.fibRecursive(n));
    }

    @Benchmark
    public void memo(Blackhole blackhole) {
        blackhole.consume(fib.fibMemo(n));
    }

    @Benchmark
    public void tailRecursive(Blackhole blackhole) {
        blackhole.consume(fib.fibTailRecursive(n));
    }

    @Benchmark
    public void iterative(Blackhole blackhole) {
        blackhole.consume(fib.fibIterative(n));
    }

    @Benchmark
    public void rrMemo(Blackhole blackhole) {
        blackhole.consume(fib.fibRRMemo(n));
    }

    @Benchmark
    public void rrTail(Blackhole blackhole) {
        blackhole.consume(fib.fibRRTail(n));
    }

    @Benchmark
    public void rrTailReuse(Blackhole blackhole) {
        blackhole.consume(fib.fibRRTailReuse(n));
    }
}
