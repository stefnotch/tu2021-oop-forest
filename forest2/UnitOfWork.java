package forest2;

import java.util.function.Consumer;

public abstract class UnitOfWork {

    private final Consumer<Forest> step;
    protected UnitOfWork(Consumer<Forest> step) {
        this.step = step;
    }

    public abstract boolean dependsOnAccept(UnitOfWork other);
    public abstract boolean dependsOn(BugUnitOfWork other);
    public abstract boolean dependsOn(DeathUnitOfWork other);
    public abstract boolean dependsOn(ForestUnitOfWork other);

    public Consumer<Forest> getStep() {
        return step;
    }
}
