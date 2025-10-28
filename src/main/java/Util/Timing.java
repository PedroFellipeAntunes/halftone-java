package Util;

public class Timing {
    /**
     * Functional interface used by {@link #measure(String, Action)}.
     *
     * @param <T> action return type
     */
    @FunctionalInterface
    public interface Action<T> {
        T execute();
    }

    /**
     * Executes the provided action, printing a simple timing and memory report.
     * The method calls System.gc() before measuring to reduce noise from previous
     * memory allocations (same behavior as old implementation).
     *
     * @param label textual label used on the printed report
     * @param action action to execute
     * @param <T> action return type
     * @return the action result
     */
    public static <T> T measure(String label, Action<T> action) {
        Runtime rt = Runtime.getRuntime();
        System.gc();

        long startTime = System.currentTimeMillis();
        long beforeMem = rt.totalMemory() - rt.freeMemory();
        System.out.println("- " + label + " START");

        T result = action.execute();

        long afterMem = rt.totalMemory() - rt.freeMemory();
        long duration = System.currentTimeMillis() - startTime;
        double deltaMB = (afterMem - beforeMem) / 1024.0 / 1024.0;
        double totalMB = afterMem / 1024.0 / 1024.0;

        System.out.printf("- %s time: %dms | Δmem: %.2f MB | total: %.2f MB%n",
                label, duration, deltaMB, totalMB);

        return result;
    }
}