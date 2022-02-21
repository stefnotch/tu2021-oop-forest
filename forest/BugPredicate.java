package forest;

@FunctionalInterface
interface BugPredicate {
    boolean test(Forest.Cell cell, BugPopulation existingPopulation);
}
