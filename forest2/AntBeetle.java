package forest2;


import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AntBeetle extends BugPopulation {

    private static final int MAX_HUNGER = 3;

    private int hunger = 0;

    public AntBeetle() {

    }

    @Override
    public String toString() {
        return "🐜";
    }

    @Override
    public boolean isEdible() {
        return false;
    }

    @Override
    protected Runnable getRunnable(Forest forest, BiConsumer<Forest, BugPopulation> onEnd) {
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
                }

                forest.coordinator.addAndExecute(new DeathUnitOfWork((f) -> {
                    // It's possible that this bug has already been eaten (and thus removed from the array)
                    if (forest.bugs.get(Y).get(X) == this) {
                        forest.bugs.get(Y).set(X, null);
                    }
                }, X, Y));

            } finally {
                onEnd.accept(forest, this);
            }
        };
    }


    private boolean step(Forest forest) {
        hunger++;

        forest.coordinator.addAndExecute(new BugUnitOfWork((f) -> {
            if(Thread.currentThread().isInterrupted()) return;

            List<Vector2> spots = forest.findFreeNeighbors(X, Y,1, (cell, existingPopulation) -> cell == Forest.Cell.Tree && (existingPopulation == null || existingPopulation.isEdible()));
            if(spots.size() == 1) {
                int newX = spots.get(0).getX();
                int newY = spots.get(0).getY();
                forest.bugs.get(Y).set(X, null);

                this.Y = newY;
                this.X = newX;

                if(forest.bugs.get(newY).get(newX) != null && forest.bugs.get(newY).get(newX).isEdible()) {
                    forest.bugs.get(newY).get(newX).stop();
                    hunger = 0;
                }

                forest.bugs.get(newY).set(newX, this);
            }
        }, X, Y));

        if (hunger >= MAX_HUNGER) {
            return false;
        }


        forest.coordinator.addAndExecute(new BugUnitOfWork((f) -> {
            if(Thread.currentThread().isInterrupted()) return;

            List<Vector2> spots = forest.findFreeNeighbors(X, Y,1, (cell, existingPopulation) -> cell == Forest.Cell.Tree && (existingPopulation == null || existingPopulation.isEdible()));
            if(spots.size() == 1) {
                AntBeetle newBug = new AntBeetle();
                newBug.X = spots.get(0).getX();
                newBug.Y = spots.get(0).getY();

                if(forest.bugs.get(newBug.Y).get(newBug.X) != null && forest.bugs.get(newBug.Y).get(newBug.X).isEdible()) {
                    forest.bugs.get(newBug.Y).get(newBug.X).stop();
                }
                forest.bugs.get(newBug.Y).set(newBug.X, newBug);

                forest.addAndStartThread(newBug);
            }
        }, X, Y));

        return true;
    }
}
