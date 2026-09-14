import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stores city locations, coordinates, categories, and direct road connections.
 * Supports dynamic edge weight updates, road blockages, and custom graph expansion.
 */
public class CityGraph {
    public static final int INF = 999999;

    public enum LocationType {
        HOSPITAL("Hospital", "🏥", 0xE53935),
        FIRE_STATION("Fire Station", "🚒", 0xF4511E),
        POLICE_STATION("Police Station", "🚓", 0x1E88E5),
        CITY_CENTER("City Center", "🏢", 0x8E24AA),
        RAILWAY_STATION("Railway Station", "🚉", 0x00897B),
        ACCIDENT_ZONE("Incident Site", "⚠️", 0xFB8C00),
        AIRPORT("Airport", "✈️", 0x3949AB),
        UNIVERSITY("University", "🎓", 0x43A047),
        RESIDENTIAL("Residential", "🏘️", 0x6D4C41);

        public final String label;
        public final String icon;
        public final int colorHex;

        LocationType(String label, String icon, int colorHex) {
            this.label = label;
            this.icon = icon;
            this.colorHex = colorHex;
        }
    }

    public static class CityNode {
        public String name;
        public LocationType type;
        public int x;
        public int y;

        public CityNode(String name, LocationType type, int x, int y) {
            this.name = name;
            this.type = type;
            this.x = x;
            this.y = y;
        }
    }

    private final List<CityNode> nodes = new ArrayList<>();
    private int[][] baseRoads;
    private int[][] currentRoads;
    private boolean[][] blockedRoads;

    public CityGraph() {
        loadDefaultMetroCity();
    }

    public void loadDefaultMetroCity() {
        nodes.clear();
        nodes.add(new CityNode("Central Hospital", LocationType.HOSPITAL, 140, 160));
        nodes.add(new CityNode("Fire Station", LocationType.FIRE_STATION, 400, 100));
        nodes.add(new CityNode("Police Station", LocationType.POLICE_STATION, 160, 420));
        nodes.add(new CityNode("City Center", LocationType.CITY_CENTER, 420, 290));
        nodes.add(new CityNode("Railway Station", LocationType.RAILWAY_STATION, 660, 140));
        nodes.add(new CityNode("Accident Zone", LocationType.ACCIDENT_ZONE, 430, 490));
        nodes.add(new CityNode("Airport", LocationType.AIRPORT, 720, 430));
        nodes.add(new CityNode("University", LocationType.UNIVERSITY, 270, 270));

        int n = nodes.size();
        baseRoads = new int[n][n];
        for (int[] row : baseRoads) Arrays.fill(row, INF);
        for (int i = 0; i < n; i++) baseRoads[i][i] = 0;

        // Symmetric undirected edges
        addInitialEdge(0, 1, 4);  // Central Hospital <-> Fire Station (4 km)
        addInitialEdge(0, 2, 6);  // Central Hospital <-> Police Station (6 km)
        addInitialEdge(0, 3, 3);  // Central Hospital <-> City Center (3 km)
        addInitialEdge(0, 7, 7);  // Central Hospital <-> University (7 km)
        addInitialEdge(1, 2, 3);  // Fire Station <-> Police Station (3 km)
        addInitialEdge(1, 4, 8);  // Fire Station <-> Railway Station (8 km)
        addInitialEdge(2, 3, 2);  // Police Station <-> City Center (2 km)
        addInitialEdge(2, 5, 7);  // Police Station <-> Accident Zone (7 km)
        addInitialEdge(3, 4, 5);  // City Center <-> Railway Station (5 km)
        addInitialEdge(3, 6, 10); // City Center <-> Airport (10 km)
        addInitialEdge(3, 7, 4);  // City Center <-> University (4 km)
        addInitialEdge(4, 5, 3);  // Railway Station <-> Accident Zone (3 km)
        addInitialEdge(5, 6, 6);  // Accident Zone <-> Airport (6 km)
        addInitialEdge(5, 7, 5);  // Accident Zone <-> University (5 km)
        addInitialEdge(6, 7, 8);  // Airport <-> University (8 km)

        blockedRoads = new boolean[n][n];
        recalculateCurrentRoads();
    }

    private void addInitialEdge(int u, int v, int dist) {
        baseRoads[u][v] = dist;
        baseRoads[v][u] = dist;
    }

    public void recalculateCurrentRoads() {
        int n = nodes.size();
        currentRoads = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    currentRoads[i][j] = 0;
                } else if (blockedRoads[i][j]) {
                    currentRoads[i][j] = INF;
                } else {
                    currentRoads[i][j] = baseRoads[i][j];
                }
            }
        }
    }

    public boolean toggleRoadBlock(int u, int v) {
        if (baseRoads[u][v] == INF) return false;
        boolean newState = !blockedRoads[u][v];
        blockedRoads[u][v] = newState;
        blockedRoads[v][u] = newState;
        recalculateCurrentRoads();
        return newState;
    }

    public boolean isRoadBlocked(int u, int v) {
        return blockedRoads[u][v];
    }

    public void setRoadDistance(int u, int v, int distance) {
        baseRoads[u][v] = distance;
        baseRoads[v][u] = distance;
        recalculateCurrentRoads();
    }

    public void setNodePosition(int index, int x, int y) {
        if (index >= 0 && index < nodes.size()) {
            nodes.get(index).x = x;
            nodes.get(index).y = y;
        }
    }

    public List<CityNode> getNodes() {
        return nodes;
    }

    public String[] getLocations() {
        String[] arr = new String[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            arr[i] = nodes.get(i).name;
        }
        return arr;
    }

    public int[][] getRoads() {
        int n = nodes.size();
        int[][] copy = new int[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(currentRoads[i], 0, copy[i], 0, n);
        }
        return copy;
    }

    public int[][] getBaseRoads() {
        int n = nodes.size();
        int[][] copy = new int[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(baseRoads[i], 0, copy[i], 0, n);
        }
        return copy;
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public int findNearestNode(int x, int y, int radius) {
        for (int i = 0; i < nodes.size(); i++) {
            CityNode node = nodes.get(i);
            int dx = node.x - x;
            int dy = node.y - y;
            if (dx * dx + dy * dy <= radius * radius) {
                return i;
            }
        }
        return -1;
    }

    public int[] findNearestEdge(int x, int y, int threshold) {
        int n = nodes.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (baseRoads[i][j] != INF) {
                    CityNode u = nodes.get(i);
                    CityNode v = nodes.get(j);
                    double dist = pointToSegmentDistance(x, y, u.x, u.y, v.x, v.y);
                    if (dist <= threshold) {
                        return new int[]{i, j};
                    }
                }
            }
        }
        return null;
    }

    private double pointToSegmentDistance(int px, int py, int x1, int y1, int x2, int y2) {
        double l2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        if (l2 == 0) return Math.hypot(px - x1, py - y1);
        double t = Math.max(0, Math.min(1, ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / l2));
        double projX = x1 + t * (x2 - x1);
        double projY = y1 + t * (y2 - y1);
        return Math.hypot(px - projX, py - projY);
    }
}
