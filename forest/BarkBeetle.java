package forest;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class BarkBeetle extends BugPopulation {

    private static final int MAX_GENERATIONS = 32;

    private int generation;
    private int treeHealth;

    private final Coordinate location;

    private static final AtomicInteger counter = new AtomicInteger(0);

    public BarkBeetle(int generation, Coordinate coordinate) {
        this.generation = generation;
        this.location = coordinate;
        Random random = new Random();
        this.treeHealth = random.nextInt(3) + 1;
    }

    @Override
    public String toString() {
        return "🐞";
    }

    @Override
    boolean isEdible() {
        return true;
    }

    @Override
    Coordinate getCoordinate() {
        return location;
    }

    @Override
    void removed(Forest forest) {
        int x = counter.decrementAndGet();
        if (x <= 0) {
            forest.stop();
        }
    }

    @Override
    void added() {
        counter.incrementAndGet();
    }

    @Override
    boolean step(Forest forest) {
        System.out.println(forest.toString());

        if(generation >= MAX_GENERATIONS) {
            forest.stop();
            return true;
        }

        forest.migrate(location, 2,
                (cell, existingBug) -> cell == Forest.Cell.Tree && existingBug == null,
                (coordinate) -> new BarkBeetle(generation + 1, coordinate)
        );
        treeHealth--;
        if (treeHealth <= 0) {
            forest.killTree(location);
            return false;
        }
        return true;
    }
}
