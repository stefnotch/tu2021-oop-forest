package forest;

public abstract class BugPopulation {

    abstract boolean step(Forest forest);

    abstract boolean isEdible();

    abstract Coordinate getCoordinate();

    abstract void removed(Forest forest);

    abstract void added();
}
