import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Dijkstra's Single-Source Shortest Path Algorithm.
 * Included for DAA comparison with Floyd-Warshall (Greedy vs Dynamic Programming).
 */
public class Dijkstra {
    private final int[] dist;
    private final int[] prev;
    private final int source;
    private final int n;
    private final long executionTimeNanos;

    private static class NodeDistance {
        int node;
        int distance;

        NodeDistance(int node, int distance) {
            this.node = node;
            this.distance = distance;
        }
    }

    public Dijkstra(int[][] graph, int source) {
        long startTime = System.nanoTime();
        this.source = source;
        this.n = graph.length;
        this.dist = new int[n];
        this.prev = new int[n];

        Arrays.fill(dist, CityGraph.INF);
        Arrays.fill(prev, -1);
        dist[source] = 0;

        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a.distance));
        pq.offer(new NodeDistance(source, 0));

        boolean[] visited = new boolean[n];

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            int u = current.node;

            if (visited[u]) continue;
            visited[u] = true;

            for (int v = 0; v < n; v++) {
                if (graph[u][v] != CityGraph.INF && u != v) {
                    int weight = graph[u][v];
                    if (dist[u] + weight < dist[v]) {
                        dist[v] = dist[u] + weight;
                        prev[v] = u;
                        pq.offer(new NodeDistance(v, dist[v]));
                    }
                }
            }
        }
        this.executionTimeNanos = System.nanoTime() - startTime;
    }

    public int getDistanceTo(int target) {
        if (target < 0 || target >= n) return CityGraph.INF;
        return dist[target];
    }

    public List<Integer> getPathTo(int target) {
        if (target < 0 || target >= n || dist[target] == CityGraph.INF) {
            return Collections.emptyList();
        }
        List<Integer> path = new ArrayList<>();
        for (int at = target; at != -1; at = prev[at]) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }

    public int[] getDistances() {
        return dist.clone();
    }

    public long getExecutionTimeNanos() {
        return executionTimeNanos;
    }
}
