package forest2;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BarkBeetle extends BugPopulation {

    private static final int MAX_GENERATIONS = 32;

    private int generation;
    private int treeHealth;

    private static final AtomicInteger counter = new AtomicInteger(0);

    public BarkBeetle(int generation) {
        this.generation = generation;
        Random random = new Random();
        this.treeHealth = random.nextInt(3) + 1;
    }

    @Override
    public String toString() {
        return "🐞";
    }

    @Override
    protected Runnable getRunnable(Forest forest, BiConsumer<Forest, BugPopulation> onEnd) {
        counter.incrementAndGet();

        return () -> {
            try {
                Random random = new Random();
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        if (!step(forest)) {
                            break; // We're done
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(random.nextInt(46) + 5);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break; // We're done
                    }

                    forest.print();
                }

                forest.coordinator.addAndExecute(new DeathUnitOfWork((f) -> {
                    // It's possible that this bug has already been eaten (and thus removed from the array)
                    if (forest.bugs.get(Y).get(X) == this) {
                        forest.bugs.get(Y).set(X, null);
                    }
                }, X, Y));

                if (counter.decrementAndGet() <= 0) {
                    forest.stop();
                }
            } finally {
                onEnd.accept(forest, this);
            }
        };
    }

    @Override
    public boolean isEdible() {
        return true;
    }

    private boolean step(Forest forest) {
        if(generation >= MAX_GENERATIONS) {
            forest.stop();
            return true;
        }

        forest.coordinator.addAndExecute(new BugUnitOfWork((f) -> {
            if(Thread.currentThread().isInterrupted()) return;

            List<Vector2> spots = forest.findFreeNeighbors(X, Y,2, ((cell, existingPopulation) -> cell == Forest.Cell.Tree && existingPopulation == null));
            if(spots.size() == 2) {
                BarkBeetle newBugA = new BarkBeetle(generation + 1);
                newBugA.X = spots.get(0).getX();
                newBugA.Y = spots.get(0).getY();
                forest.bugs.get(newBugA.Y).set(newBugA.X, newBugA);
                forest.addAndStartThread(newBugA);
                BarkBeetle newBugB = new BarkBeetle(generation + 1);
                newBugB.X = spots.get(1).getX();
                newBugB.Y = spots.get(1).getY();
                forest.bugs.get(newBugB.Y).set(newBugB.X, newBugB);
                forest.addAndStartThread(newBugB);
            }
        }, X, Y));

        treeHealth--;
        if (treeHealth <= 0) {
            forest.coordinator.addAndExecute(new DeathUnitOfWork((f) -> {
                if(Thread.currentThread().isInterrupted()) return;

                forest.cells.get(Y).set(X, Forest.Cell.NoTree);
            }, X, Y));
            return false;
        }
        return true;
    }
}
