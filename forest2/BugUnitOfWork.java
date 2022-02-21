package forest2;

import java.util.function.Consumer;

public class BugUnitOfWork extends UnitOfWork {

    public final int X;
    public final int Y;

    protected BugUnitOfWork(Consumer<Forest> step, int x, int y) {
        super(step);
        this.X = x;
        this.Y = y;
    }

    @Override
    public boolean dependsOnAccept(UnitOfWork other) {
        return other.dependsOn(this);
    }

    @Override
    public boolean dependsOn(BugUnitOfWork other) {
        // X Distance <= 2 && Y Distance <= 2
        return Math.abs(X - other.X) <= 2 && Math.abs(Y - other.Y) <= 2;
    }

    @Override
    public boolean dependsOn(DeathUnitOfWork other) {
        // X Distance <= 1 && Y Distance <= 1
        return Math.abs(X - other.X) <= 1 && Math.abs(Y - other.Y) <= 1;
    }

    @Override
    public boolean dependsOn(ForestUnitOfWork other) {
        return true;
    }
}
