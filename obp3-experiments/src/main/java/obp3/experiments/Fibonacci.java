package obp3.experiments;

import obp3.runtime.sli.IRootedGraph;
import obp3.sli.core.RootedGraphFunctional;
import obp3.traversal.dfs.DepthFirstTraversal;
import obp3.traversal.dfs.model.FunctionalDFTCallbacksModel;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.function.Supplier;

public class Fibonacci {

    public static void main(String[] args) {
        final var n = 45;
        var timedRecursive = timeIt(() -> new Fibonacci().fibRecursive(n));
        System.out.println("Recursive: " + timedRecursive.result() + " in " + formatNanosWithSpaces(timedRecursive.elapsed()));

        var timedMemo = timeIt(() -> new Fibonacci().fibMemo(n));
        System.out.println("Recursive Memo: " + timedMemo.result() + " in " + formatNanosWithSpaces(timedMemo.elapsed()));

        var timedIterative = timeIt(() -> new Fibonacci().fibIterative(n));
        System.out.println("Iterative: " + timedIterative.result() + " in " + formatNanosWithSpaces(timedIterative.elapsed()));
        var timedTailRecursive = timeIt(() -> new Fibonacci().fibTailRecursive(n));
        System.out.println("Tail recursive: " + timedTailRecursive.result() + " in " + formatNanosWithSpaces(timedTailRecursive.elapsed()));

        var timedRRMemo = timeIt(() -> new Fibonacci().fibRRMemo(n));
        System.out.println("RR memo : " + timedRRMemo.result() + " in " + formatNanosWithSpaces(timedRRMemo.elapsed()));

        var timedRRTail = timeIt(() -> new Fibonacci().fibRRTail(n));
        System.out.println("RR tail : " + timedRRTail.result() + " in " + formatNanosWithSpaces(timedRRTail.elapsed()));

        var timedRRTailReuse = timeIt(() -> new Fibonacci().fibRRTailReuse(n));
        System.out.println("RR tail reuse : " + timedRRTailReuse.result() + " in " + formatNanosWithSpaces(timedRRTailReuse.elapsed()));

        System.out.println("rec is " + (timedRecursive.elapsed() / timedIterative.elapsed()) + "X slower than iterative");
        System.out.println("rec is " + (timedRecursive.elapsed() / timedRRTail.elapsed()) + "X slower than RR[tail]");
        System.out.println("RR[tail] is " + (timedRRTail.elapsed() / timedIterative.elapsed()) + "X slower than iterative");

        System.out.println("rec is " + (timedRecursive.elapsed() / timedRRMemo.elapsed()) + "X slower than RR[memo]");
        System.out.println("RR[memo] is " + (timedRRMemo.elapsed() / timedIterative.elapsed()) + "X slower than iterative");
    }

    long fibRecursive(int n) {
        return n <= 1 ? n : fibRecursive(n - 1) + fibRecursive(n - 2);
    }

    // memoized top-down recursion (O(n) time, O(n) space)
    long fibMemo(int n) {
        long[] cache = new long[n + 1];
        Arrays.fill(cache, -1);
        return fibMemo(n, cache);
    }

    long fibMemo(int n, long[] cache) {
        if (n <= 1) return n;
        if (cache[n] != -1) return cache[n];
        cache[n] = fibMemo(n - 1, cache) + fibMemo(n - 2, cache);
        return cache[n];
    }

    long fibIterative(int n) {
        long a = 0, b = 1;
        for (int i = 0; i < n; i++) {
            long c = a + b;
            a = b;
            b = c;
        }
        return a;
    }

    long fibTailRecursive(int n) {
        return fibTailRecursive(n, 0, 1);
    }
    long fibTailRecursive(int n, long a, long b) {
        return n == 0 ? a : fibTailRecursive(n - 1, b, a + b);
    }

    long fibRRMemo(int n) {
        IRootedGraph<Integer> graph = new RootedGraphFunctional<>(
                () -> List.of(n).iterator(),
                v -> v <= 1 ? Collections.emptyIterator() : List.of(v - 1, v - 2).iterator(),
                false, true
        );
        long[] cache = new long[n + 1];
        var dfs = new DepthFirstTraversal<>(
                DepthFirstTraversal.Algorithm.WHILE,
                graph,
                FunctionalDFTCallbacksModel.onExit((v, sf, c) -> {
                    if (v <= 1) {
                        cache[0] = 0;
                        cache[1] = 1;
                    } else {
                        cache[v] = cache[v - 1] + cache[v - 2];
                    }
                    return false;
                }));
        dfs.runAlone();
        return cache[n];
    }

    long fibRRTail(int n) {
        record Frame(int n, long a, long b) {}
        long[] result = new long[1];
        var graph = new RootedGraphFunctional<>(
                () -> List.of(new Frame(n, 0, 1)).iterator(),
                f -> {
                    if (f.n == 0) {
                        result[0] = f.a;
                        return Collections.emptyIterator();
                    }
                    return List.of(new Frame(f.n - 1, f.b, f.a + f.b)).iterator();
                },
                false, false
        );
        var dfs = new DepthFirstTraversal<>(
                DepthFirstTraversal.Algorithm.WHILE,
                graph);
        dfs.runAlone();
        return result[0];
    }

    long fibRRTailReuse(int n) {
        class Frame{ int n; long a; long b;
            Frame(int n, long a, long b) { this.n = n; this.a = a; this.b = b; }
            Frame change(int n, long a, long b) {
                this.n = n; this.a = a; this.b = b;
                return this;
            }
        }
        long[] result = new long[1];
        var graph = new RootedGraphFunctional<>(
                () -> List.of(new Frame(n, 0, 1)).iterator(),
                f -> {
                    if (f.n == 0) {
                        result[0] = f.a;
                        return Collections.emptyIterator();
                    }
                    return List.of(f.change(f.n - 1, f.b, f.a + f.b)).iterator();
                },
                false, false
        );
        var dfs = new DepthFirstTraversal<>(
                DepthFirstTraversal.Algorithm.WHILE,
                graph);
        dfs.runAlone();
        return result[0];
    }

    static <T> TimedResult<T> timeIt(Supplier<T> supplier) {
        //measure the execution time of the supplier
        long start = System.nanoTime();
        T result = supplier.get();
        long elapsed = System.nanoTime() - start;
        return new TimedResult<>(result, elapsed);
    }
    record TimedResult<T>(T result, long elapsed) { }

    static String formatNanosWithSpaces(long nanos) {
        DecimalFormat df = new DecimalFormat("#,###");
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator(' ');
        df.setDecimalFormatSymbols(symbols);
        df.setGroupingUsed(true);
        df.setGroupingSize(3);
        return df.format(nanos) + " ns";
    }
}
