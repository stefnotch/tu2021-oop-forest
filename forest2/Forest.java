package forest2;

import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.Collectors;

public class Forest {

    private final int width;
    private final int height;

    public List<Vector2> findFreeNeighbors(int x, int y, int count, BugPredicate filter) {
        List<Vector2> neighbours = new ArrayList<>();

        int startX = Math.max(0, x - 1);
        int endX = Math.min(this.width - 1, x + 1);
        int startY = Math.max(0, y - 1);
        int endY = Math.min(this.height - 1, y + 1);

        for (int row = startY; row <= endY; row++) {
            for (int col = startX; col <= endX; col++) {
                if(row == y && col == x) continue;
                neighbours.add(new Vector2(col, row));
            }
        }
        List<Vector2> validNeighbours = new ArrayList<>();
        for (Vector2 n : neighbours) {
            Forest.Cell curr = cells.get(n.getY()).get(n.getX());
            if(filter.test(curr, bugs.get(n.getY()).get(n.getX()))) {
                validNeighbours.add(n);
                if(validNeighbours.size() >= count) {
                    return validNeighbours;
                }
            }
        }
        return validNeighbours;
    }

    public enum Cell {
        Tree("🌳"),
        NoTree("❌");

        private final String text;

        Cell(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public final AtomicReferenceArray<AtomicReferenceArray<Cell>> cells;
    public final AtomicReferenceArray<AtomicReferenceArray<BugPopulation>> bugs;

    public final UnitOfWorkCoordinator coordinator;


    // cells has to be a rectangular array with the size of at least 1x1
    // bugs has the same dimensions as cells
    public Forest(Cell[][] cells, BugPopulation[][] bugs) {
        this.height = cells.length;
        this.width = cells[0].length;
        this.cells = new AtomicReferenceArray<>(height);
        this.bugs = new AtomicReferenceArray<>(height);
        boolean hasBeetles = false;
        for (int row = 0; row < height; row++) {
            this.cells.set(row, new AtomicReferenceArray<>(width));
            this.bugs.set(row, new AtomicReferenceArray<>(width));
            for (int col = 0; col < width; col++) {
                this.cells.get(row).set(col, cells[row][col]);
                this.bugs.get(row).set(col, bugs[row][col]);
                if (bugs[row][col] instanceof BarkBeetle) {
                    hasBeetles = true;
                }
            }
        }
        if (!hasBeetles) {
            throw new IllegalArgumentException("beetles pls");
        }
        this.coordinator = new UnitOfWorkCoordinator(this);
    }

    private final Set<BugPopulation> runningBugs = new HashSet<>();

    private volatile boolean running = false;

    // may only be called from one thread and has to be followed by a ??? (stop/sleep/wait?)
    public void start() {
        coordinator.addAndExecute(new ForestUnitOfWork(Forest::startLambda));
    }

    private static void startLambda(Forest forest) {
        forest.running = true;
        for (int row = 0; row < forest.height; row++) {
            for (int col = 0; col < forest.width; col++) {
                BugPopulation bug = forest.bugs.get(row).get(col);
                if (bug != null) {
                    forest.addAndStartThread(bug);
                }
            }
        }
    }

    public void stop() {
        coordinator.addAndExecute(new ForestUnitOfWork(Forest::stopLambda));
    }

    private static void stopLambda(Forest forest) {
        synchronized (forest.runningBugs) {
            forest.running = false;
            for (BugPopulation value : forest.runningBugs) {
                value.stop();
            }
        }
    }

    public boolean isRunning() {
        synchronized (runningBugs) {
            return !runningBugs.isEmpty();
        }
    }

    public void addAndStartThread(BugPopulation bug) {
        synchronized (runningBugs) {
            if (!running) {
                throw new RuntimeException("Please start the simulation first");
            }
            if(!runningBugs.add(bug)) {
                throw new RuntimeException("Bug has already been added");
            }
        }

        bug.start(this, Forest::stopBugLambda);
    }

    private static void stopBugLambda(Forest forest, BugPopulation bug) {
        synchronized (forest.runningBugs) {
            if(!forest.runningBugs.remove(bug)) {
                throw new RuntimeException("Bug has already been removed");
            }
        }
    }

    // Could return a CompletableFuture
    public void print() {
        coordinator.addAndExecute(new ForestUnitOfWork(Forest::printForestLambda));
    }

    private static void printForestLambda(Forest forest) {

        StringBuilder stringBuilder = new StringBuilder();
        for (int row = 0; row < forest.height; row++) {
            for (int col = 0; col < forest.width; col++) {
                Cell cell = forest.cells.get(row).get(col);
                BugPopulation bug = forest.bugs.get(row).get(col);
                if (cell == Cell.NoTree) {
                    stringBuilder.append(cell.toString());
                } else if (bug != null) {
                    stringBuilder.append(bug.toString());
                } else {
                    stringBuilder.append(cell.toString());
                }
            }
            stringBuilder.append("\n");
        }
        //System.out.println(stringBuilder.toString());
    }
}
