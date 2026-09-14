import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implements all-pairs shortest paths and remembers each shortest route. */
public class FloydWarshall {
    private final int[][] distance;
    private final int[][] next;

    public FloydWarshall(int[][] graph) {
        int n = graph.length;
        distance = new int[n][n];
        next = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                distance[i][j] = graph[i][j];
                next[i][j] = graph[i][j] == CityGraph.INF ? -1 : j;
            }
        }

        // k is the intermediate location considered in this round.
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (distance[i][k] != CityGraph.INF && distance[k][j] != CityGraph.INF
                            && distance[i][k] + distance[k][j] < distance[i][j]) {
                        distance[i][j] = distance[i][k] + distance[k][j];
                        next[i][j] = next[i][k];
                    }
                }
            }
        }
    }

    public int getDistance(int start, int end) {
        return distance[start][end];
    }

    public List<Integer> getPath(int start, int end) {
        if (next[start][end] == -1) return Collections.emptyList();
        List<Integer> path = new ArrayList<>();
        int current = start;
        path.add(current);
        while (current != end) {
            current = next[current][end];
            path.add(current);
        }
        return path;
    }

    public int[][] getDistanceMatrix() {
        int n = distance.length;
        int[][] copy = new int[n][n];
        for (int i = 0; i < n; i++) System.arraycopy(distance[i], 0, copy[i], 0, n);
        return copy;
    }
}
