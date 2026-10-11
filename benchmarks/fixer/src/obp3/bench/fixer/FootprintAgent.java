package obp3.bench.fixer;

import java.lang.instrument.Instrumentation;

public final class FootprintAgent {
    static Instrumentation instrumentation;
    public static void premain(String options, Instrumentation value) { instrumentation = value; }
}
