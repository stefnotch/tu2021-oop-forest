package forest2;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class BugPopulation {
    public int X;
    public int Y;

    private static final AtomicInteger threadId = new AtomicInteger(0);

    private volatile Thread thread;

    private String getThreadName() {
        return "BuggyThread-" + (threadId.incrementAndGet());
    }

    protected abstract Runnable getRunnable(Forest forest, BiConsumer<Forest, BugPopulation> onEnd);

    public void start(Forest forest, BiConsumer<Forest, BugPopulation> onEnd) {
        thread = new Thread(null, getRunnable(forest, onEnd), getThreadName(), 32000);
        thread.start();
    }

    public void stop() {
        thread.interrupt();
    }

    public abstract boolean isEdible();
}
