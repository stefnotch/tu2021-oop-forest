package forest2;

import java.util.function.Consumer;

public class DeathUnitOfWork extends UnitOfWork {
    public final int X;
    public final int Y;

    protected DeathUnitOfWork(Consumer<Forest> step, int x, int y) {
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
        // X Distance <= 1 && Y Distance <= 1
        return Math.abs(X - other.X) <= 1 && Math.abs(Y - other.Y) <= 1;
    }

    @Override
    public boolean dependsOn(DeathUnitOfWork other) {
        // X Distance <= 0 && Y Distance <= 0
        return X == other.X && Y == other.Y;
    }

    @Override
    public boolean dependsOn(ForestUnitOfWork other) {
        return true;
    }
}
