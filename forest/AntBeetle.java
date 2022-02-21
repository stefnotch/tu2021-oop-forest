package forest;

public class AntBeetle extends BugPopulation {

    private static final int MAX_HUNGER = 3;

    private int hunger = 0;

    private Coordinate location;

    public AntBeetle(Coordinate coordinate) {
        this.location = coordinate;
    }

    @Override
    public String toString() {
        return "🐜";
    }

    @Override
    boolean isEdible() {
        return false;
    }

    @Override
    Coordinate getCoordinate() {
        return location;
    }

    @Override
    void removed(Forest forest) {

    }

    @Override
    void added() {

    }

    @Override
    boolean step(Forest forest) {
        hunger++;
        forest.migrate(location, 1,
            (cell, existingBug) -> cell == Forest.Cell.Tree && (existingBug == null || existingBug.isEdible()),
            (coordinate) -> {
                forest.removeBug(location);
                this.location = coordinate;
                if(forest.tryEat(coordinate)) {
                    hunger = 0;
                }
                return this;
            }
        );
        if (hunger >= MAX_HUNGER) {
            return false;
        }

        forest.migrate(location, 1,
                (cell, existingBug) -> cell == Forest.Cell.Tree && (existingBug == null || existingBug.isEdible()),
                ((coordinate) -> new AntBeetle(coordinate))
        );

        return true;
    }
}
