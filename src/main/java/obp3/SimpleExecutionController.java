package obp3;

import java.util.concurrent.locks.LockSupport;

public class SimpleExecutionController<R> {
    IExecutable<R> executable;
    boolean running = true;
    private final Thread worker;
    R result;

    public SimpleExecutionController(IExecutable<R> executable) {
        this.executable = executable;
        worker = new Thread(() -> {
            LockSupport.park();
            R result = executable.run(() -> !running);
        });
    }

    public void pause() {
        LockSupport.park(worker);
    }
    public void resume() {
        LockSupport.unpark(worker);
    }

    public void start() {
        worker.start();
        resume();
    }
    public void stop() {
        running = false;
        LockSupport.unpark(worker);
    }
}
