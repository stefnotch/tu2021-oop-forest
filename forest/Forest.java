package forest;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Forest {

    private final int width;
    private final int height;

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

    private final Cell[][] cells;
    private final BugPopulation[][] bugs;

    // immutable objects for locking
    private final ReentrantLock[][] locks;

    private long threadId = 0;
    private final Map<BugPopulation, Thread> threads = new HashMap<>();

    private boolean running = false;

    // cells has to be a rectangular array with the size of at least 1x1
    // bugs has the same dimensions as cells
    public Forest(Cell[][] cells, BugPopulation[][] bugs) {
        this.cells = cells;
        this.bugs = bugs;
        this.height = cells.length;
        this.width = cells[0].length;

        this.locks = new ReentrantLock[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                this.locks[row][col] = new ReentrantLock();
            }
        }
    }

    void migrate(Coordinate coordinate, int numberOfNeighbors, BugPredicate filter, Function<Coordinate, BugPopulation> spawner) {
        List<Coordinate> neighbours = new ArrayList<>();
        int x = coordinate.getX();
        int y = coordinate.getY();
        for (int offY = -1; offY <= 1; offY++) {
            for (int offX = -1; offX <= 1; offX++) {
                boolean inBounds = 0 <= y + offY && y + offY < this.height &&
                        0 <= x + offX && x + offX < this.width;
                if (!inBounds) continue;

                neighbours.add(new Coordinate(x + offX, y + offY));
            }
        }
        lock(neighbours, () -> {
            List<Coordinate> validNeighbors = neighbours.stream().filter(n -> {
                Cell curr = cells[n.getY()][n.getX()];
                return filter.test(curr, bugs[n.getY()][n.getX()]);
            }).limit(numberOfNeighbors).collect(Collectors.toList());

            for (Coordinate validNeighbor : validNeighbors) {
                BugPopulation newBug = spawner.apply(validNeighbor);
                bugs[validNeighbor.getY()][validNeighbor.getX()] = newBug;
                addAndStartThread(newBug);
            }
        });
    }

    void killTree(Coordinate coordinate) {
        lock(Collections.singletonList(coordinate), () -> cells[coordinate.getY()][coordinate.getX()] = Cell.NoTree);
    }

    void removeBug(Coordinate coordinate) {
        lock(Collections.singletonList(coordinate), () -> bugs[coordinate.getY()][coordinate.getX()] = null);
    }

    private void lock(List<Coordinate> coordinates, Runnable callback) {
        lock(coordinates, () -> {
            callback.run();
            return 0;
        });
    }

    private <T> T lock(List<Coordinate> coordinates, Supplier<T> callback) {
        for (Coordinate coordinate : coordinates) {
            locks[coordinate.getY()][coordinate.getX()].lock();
        }

        T returnValue = callback.get();

        Collections.reverse(coordinates);
        for (Coordinate coordinate : coordinates) {
            locks[coordinate.getY()][coordinate.getX()].unlock();
        }

        return returnValue;
    }

    // may only be called from one thread and has to be followed by a ??? (stop/sleep/wait?)
    public void start() {
        running = true;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (bugs[row][col] != null) {
                    addAndStartThread(bugs[row][col]);
                }
            }
        }
    }

    synchronized void stop() {
        running = false;
        synchronized (threads) {
            for (Thread value : threads.values()) {
                value.interrupt();
            }
        }
    }

    public boolean isRunning() {
        synchronized (threads) {
            return !threads.isEmpty();
        }
    }

    private void addAndStartThread(BugPopulation bug) {
        if (!running) {
            return;
        }
        if (threads.containsKey(bug)) return;

        Runnable run = () -> {
            Random random = new Random();
            while (!Thread.interrupted()) {
                try {
                    if (!bug.step(this)) {
                        break; // We're done
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(random.nextInt(46) + 5);
                } catch (InterruptedException e) {
                    break; // We're done
                }
            }

            lock(Collections.singletonList(bug.getCoordinate()), () -> {
                bugs[bug.getCoordinate().getY()][bug.getCoordinate().getX()] = null;
                synchronized (threads) {
                    threads.remove(bug);
                    bug.removed(this);
                }
            });
        };

        // Stacksize of 16k
        Thread thread = new Thread(null, run, getThreadName(), 16000);
        synchronized (threads) {
            threads.put(bug, thread);
            bug.added();
        }
        thread.start();
    }

    private String getThreadName() {
        return "BuggyThread-" + (threadId++);
    }

    @Override
    public String toString() {
        List<Coordinate> coordinates = new ArrayList<>();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                coordinates.add(new Coordinate(col, row));
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        lock(coordinates, () -> {
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    if (cells[row][col] == Cell.NoTree) {
                        stringBuilder.append(cells[row][col].toString());
                    } else if (bugs[row][col] != null) {
                        stringBuilder.append(bugs[row][col].toString());
                    } else {
                        stringBuilder.append(cells[row][col].toString());
                    }
                }
                stringBuilder.append("\n");
            }
        });

        return stringBuilder.toString();
    }

    boolean tryEat(Coordinate coordinate) {
        return lock(Collections.singletonList(coordinate), () ->
        {
            if (bugs[coordinate.getY()][coordinate.getX()] != null) {
                synchronized (threads) {
                    Thread bugThread = threads.get(bugs[coordinate.getY()][coordinate.getX()]);
                    bugThread.interrupt();
                }
                return true;
            }
            return false;
        });
    }
}
