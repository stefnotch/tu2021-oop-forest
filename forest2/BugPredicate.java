package forest2;

@FunctionalInterface
interface BugPredicate {
    boolean test(Forest.Cell cell, BugPopulation existingPopulation);
}
