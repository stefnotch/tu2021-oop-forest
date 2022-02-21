package forest2;

import java.util.function.Consumer;

public class ForestUnitOfWork extends UnitOfWork {
    protected ForestUnitOfWork(Consumer<Forest> step) {
        super(step);
    }

    @Override
    public boolean dependsOnAccept(UnitOfWork other) {
        return other.dependsOn(this);
    }

    @Override
    public boolean dependsOn(BugUnitOfWork other) {
        return true;
    }

    @Override
    public boolean dependsOn(DeathUnitOfWork other) {
        return true;
    }

    @Override
    public boolean dependsOn(ForestUnitOfWork other) {
        return true;
    }
}
