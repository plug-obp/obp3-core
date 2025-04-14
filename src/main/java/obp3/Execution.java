package obp3;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public class Execution<I, R> {
    public String name;
    I parameters;
    Function<I, IExecutable<R>> executableMaker;

    private Thread thread;
    public final AtomicBoolean running = new AtomicBoolean(false);
    public final AtomicBoolean paused = new AtomicBoolean(false);
    private final Object pauseLock = new Object();
    private final Semaphore semaphore = new Semaphore(0);
    public R result;
    public Consumer<R> resultConsumer;
    public Function<R, String> resultToString;

    public Execution(
            String name,
            I parameters,
            Function<I, IExecutable<R>> executableMaker,
            Function<R, String> resultToString) {
        this.name = name;
        this.parameters = parameters;
        this.executableMaker = executableMaker;
        this.resultToString = resultToString;
    }

    Thread worker() {
        return new Thread(() -> {
            IExecutable<R> executable = executableMaker.apply(parameters);
            result = executable.run(() -> {
                if (paused.get()) {
                    semaphore.acquireUninterruptibly();
                }
                return !running.get();
            }); // The actual task work
            running.set(false);
            resultConsumer.accept(result);
            System.out.println(name + " finished.");
        });
    }

    public void pause() {
        if (!running.get() || paused.get()) return;
        paused.set(true);
        System.out.println(name + " paused.");
    }
    public void resume() {
        if (!running.get() || !paused.get()) return;
        synchronized (pauseLock) {
            paused.set(false);
            semaphore.release();
        }
        System.out.println(name + " resumed.");
    }

    public void start() {
        if (running.get()) {
            System.out.println(name + " already running.");
            return;
        }
        System.out.println(name + " started.");
        running.set(true);
        thread = worker();
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        if (!running.get()) return;
        synchronized (pauseLock) {
            running.set(false);
            paused.set(false);
            semaphore.release();
        }
        System.out.println(name + " stopped.");
    }

    public String resultToString() {
        return resultToString.apply(result);
    }
}
