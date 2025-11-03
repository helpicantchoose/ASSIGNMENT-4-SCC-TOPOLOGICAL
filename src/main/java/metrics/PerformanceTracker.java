package metrics;

import java.util.LinkedHashMap;
import java.util.Map;

public class PerformanceTracker {
    private long startTime;
    private long endTime;
    private Map<String, Long> operations;

    public void start() {
        startTime = System.nanoTime();
        operations = new LinkedHashMap<>();
    }

    public void stop() {
        endTime = System.nanoTime();
    }

    public void incrementOperation(String name) {
        operations.put(name, operations.getOrDefault(name, 0L) + 1);
    }

    public long getExecutionTimeNanos() {
        return endTime - startTime;
    }

    public Map<String, Long> getOperations() {
        return operations;
    }
}
