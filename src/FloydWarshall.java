import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implements the Floyd–Warshall all-pairs shortest path algorithm with full execution trace.
 * Records step-by-step state snapshots for interactive visual playback and DAA demonstration.
 */
public class FloydWarshall {
    public static class StepSnapshot {
        public final int k;
        public final int i;
        public final int j;
        public final int oldDist;
        public final int distIK;
        public final int distKJ;
        public final int newDist;
        public final boolean updated;
        public final int[][] distanceMatrix;
        public final int[][] nextMatrix;
        public final String explanation;

        public StepSnapshot(int k, int i, int j, int oldDist, int distIK, int distKJ, int newDist,
                            boolean updated, int[][] distMat, int[][] nextMat, String explanation) {
            this.k = k;
            this.i = i;
            this.j = j;
            this.oldDist = oldDist;
            this.distIK = distIK;
            this.distKJ = distKJ;
            this.newDist = newDist;
            this.updated = updated;
            this.distanceMatrix = distMat;
            this.nextMatrix = nextMat;
            this.explanation = explanation;
        }
    }

    private final int[][] distance;
    private final int[][] next;
    private final List<StepSnapshot> steps = new ArrayList<>();
    private final int nodeCount;
    private long executionTimeNanos = 0;

    public FloydWarshall(int[][] graph) {
        this(graph, null);
    }

    public FloydWarshall(int[][] graph, String[] nodeNames) {
        long startTime = System.nanoTime();
        this.nodeCount = graph.length;
        distance = new int[nodeCount][nodeCount];
        next = new int[nodeCount][nodeCount];

        for (int i = 0; i < nodeCount; i++) {
            for (int j = 0; j < nodeCount; j++) {
                distance[i][j] = graph[i][j];
                next[i][j] = (graph[i][j] == CityGraph.INF || i == j) ? -1 : j;
            }
        }

        // Record initial state snapshot
        steps.add(new StepSnapshot(
            -1, -1, -1, 0, 0, 0, 0, false,
            deepCopy(distance), deepCopy(next),
            "Initial Adjacency Matrix loaded from road network."
        ));

        // k is the intermediate location considered in this round
        for (int k = 0; k < nodeCount; k++) {
            String kName = nodeNames != null && k < nodeNames.length ? nodeNames[k] : "Node " + k;
            for (int i = 0; i < nodeCount; i++) {
                String iName = nodeNames != null && i < nodeNames.length ? nodeNames[i] : "Node " + i;
                for (int j = 0; j < nodeCount; j++) {
                    String jName = nodeNames != null && j < nodeNames.length ? nodeNames[j] : "Node " + j;
                    int oldDist = distance[i][j];
                    int dIK = distance[i][k];
                    int dKJ = distance[k][j];
                    boolean isUpdate = false;
                    int calculatedDist = oldDist;

                    String explanation;
                    if (dIK != CityGraph.INF && dKJ != CityGraph.INF && dIK + dKJ < oldDist) {
                        calculatedDist = dIK + dKJ;
                        distance[i][j] = calculatedDist;
                        next[i][j] = next[i][k];
                        isUpdate = true;
                        explanation = String.format("Pivot k=%s: Shorter path found for (%s → %s) via %s. " +
                                "D[%d][%d] updated from %s to %d km (%d + %d).",
                                kName, iName, jName, kName, i, j,
                                (oldDist == CityGraph.INF ? "∞" : oldDist + " km"), calculatedDist, dIK, dKJ);
                    } else {
                        explanation = String.format("Pivot k=%s: Checking (%s → %s) via %s. " +
                                "Current D[%d][%d]=%s ≤ %s + %s. No update.",
                                kName, iName, jName, kName, i, j,
                                (oldDist == CityGraph.INF ? "∞" : oldDist + " km"),
                                (dIK == CityGraph.INF ? "∞" : String.valueOf(dIK)),
                                (dKJ == CityGraph.INF ? "∞" : String.valueOf(dKJ)));
                    }

                    steps.add(new StepSnapshot(
                        k, i, j, oldDist, dIK, dKJ, calculatedDist, isUpdate,
                        deepCopy(distance), deepCopy(next), explanation
                    ));
                }
            }
        }
        executionTimeNanos = System.nanoTime() - startTime;
    }

    public int getDistance(int start, int end) {
        if (start < 0 || start >= nodeCount || end < 0 || end >= nodeCount) return CityGraph.INF;
        return distance[start][end];
    }

    public List<Integer> getPath(int start, int end) {
        if (start < 0 || start >= nodeCount || end < 0 || end >= nodeCount) return Collections.emptyList();
        if (start == end) {
            List<Integer> single = new ArrayList<>();
            single.add(start);
            return single;
        }
        if (next[start][end] == -1) return Collections.emptyList();

        List<Integer> path = new ArrayList<>();
        int current = start;
        path.add(current);
        while (current != end) {
            current = next[current][end];
            if (current == -1 || path.contains(current)) {
                // Prevent infinite loop if negative cycle exists (safety guard)
                break;
            }
            path.add(current);
        }
        return path;
    }

    public int[][] getDistanceMatrix() {
        return deepCopy(distance);
    }

    public int[][] getNextMatrix() {
        return deepCopy(next);
    }

    public List<StepSnapshot> getSteps() {
        return steps;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public long getExecutionTimeNanos() {
        return executionTimeNanos;
    }

    private static int[][] deepCopy(int[][] matrix) {
        int n = matrix.length;
        int[][] copy = new int[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(matrix[i], 0, copy[i], 0, n);
        }
        return copy;
    }
}
