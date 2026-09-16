import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stores city locations, coordinates, categories, and direct road connections.
 * Supports a realistic 18-node metropolitan blueprint with multi-hop express bypasses,
 * bridge bottlenecks, multiple emergency response hubs, and dynamic blockages.
 */
public class CityGraph {
    public static final int INF = 999999;

    public enum LocationType {
        HOSPITAL("Hospital", "🏥", 0xE53935),
        FIRE_STATION("Fire Station", "🚒", 0xF4511E),
        POLICE_STATION("Police HQ", "🚓", 0x1E88E5),
        CITY_CENTER("City Center", "🏢", 0x8E24AA),
        RAILWAY_STATION("Transit Hub", "🚉", 0x00897B),
        ACCIDENT_ZONE("Incident Site", "⚠️", 0xFB8C00),
        AIRPORT("Airport", "✈️", 0x3949AB),
        UNIVERSITY("University", "🎓", 0x43A047),
        RESIDENTIAL("District", "🏘️", 0x6D4C41);

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
        // 18 Metropolitan Locations with clean spatial layout across 5 distinct city sectors
        nodes.add(new CityNode("Central Trauma Hospital", LocationType.HOSPITAL, 110, 110));      // 0
        nodes.add(new CityNode("Mountain Valley Tunnel", LocationType.ACCIDENT_ZONE, 240, 160));     // 1
        nodes.add(new CityNode("Central Fire Station 1", LocationType.FIRE_STATION, 380, 110));     // 2
        nodes.add(new CityNode("North Heights District", LocationType.RESIDENTIAL, 380, 42));       // 3
        nodes.add(new CityNode("Suburban Fire Station 2", LocationType.FIRE_STATION, 570, 65));     // 4
        nodes.add(new CityNode("Harbor Cargo Terminal", LocationType.RAILWAY_STATION, 740, 130));    // 5
        nodes.add(new CityNode("Downtown Financial Plaza", LocationType.CITY_CENTER, 360, 240));    // 6
        nodes.add(new CityNode("River Suspension Bridge", LocationType.RESIDENTIAL, 510, 250));     // 7
        nodes.add(new CityNode("Grand Central Transit", LocationType.RAILWAY_STATION, 560, 170));   // 8
        nodes.add(new CityNode("West Bay Commercial Hub", LocationType.RESIDENTIAL, 75, 280));      // 9
        nodes.add(new CityNode("Metro Police HQ", LocationType.POLICE_STATION, 190, 310));          // 10
        nodes.add(new CityNode("Highway 101 Incident Site", LocationType.ACCIDENT_ZONE, 330, 420)); // 11
        nodes.add(new CityNode("Industrial Tech Park", LocationType.CITY_CENTER, 480, 370));        // 12
        nodes.add(new CityNode("International Airport", LocationType.AIRPORT, 740, 310));           // 13
        nodes.add(new CityNode("Metropolitan University", LocationType.UNIVERSITY, 130, 470));      // 14
        nodes.add(new CityNode("South Waterfront Hospital", LocationType.HOSPITAL, 460, 490));      // 15
        nodes.add(new CityNode("East Riverfront Suburb", LocationType.RESIDENTIAL, 610, 460));      // 16
        nodes.add(new CityNode("Express Bypass Junction", LocationType.CITY_CENTER, 730, 430));     // 17

        int n = nodes.size();
        baseRoads = new int[n][n];
        for (int[] row : baseRoads) Arrays.fill(row, INF);
        for (int i = 0; i < n; i++) baseRoads[i][i] = 0;

        // 43 Symmetric Interconnected Road Segments with Realistic Weights & Bottlenecks
        // North & West Sector
        addInitialEdge(0, 1, 4);   // Central Hospital <-> Mountain Tunnel (4 km)
        addInitialEdge(0, 3, 9);   // Central Hospital <-> North Heights (9 km)
        addInitialEdge(0, 9, 7);   // Central Hospital <-> West Bay (7 km)
        addInitialEdge(1, 2, 3);   // Mountain Tunnel <-> Fire Station 1 (3 km)
        addInitialEdge(1, 6, 5);   // Mountain Tunnel <-> Downtown Plaza (5 km)
        addInitialEdge(1, 10, 4);  // Mountain Tunnel <-> Police HQ (4 km)
        addInitialEdge(2, 3, 3);   // Fire Station 1 <-> North Heights (3 km)
        addInitialEdge(2, 4, 6);   // Fire Station 1 <-> Fire Station 2 (6 km)
        addInitialEdge(2, 6, 4);   // Fire Station 1 <-> Downtown Plaza (4 km)
        addInitialEdge(3, 4, 5);   // North Heights <-> Fire Station 2 (5 km)

        // Northeast & Harbor Sector
        addInitialEdge(4, 5, 5);   // Fire Station 2 <-> Harbor Terminal (5 km)
        addInitialEdge(4, 8, 4);   // Fire Station 2 <-> Grand Central (4 km)
        addInitialEdge(5, 8, 4);   // Harbor Terminal <-> Grand Central (4 km)
        addInitialEdge(5, 13, 8);  // Harbor Terminal <-> Airport (8 km)
        addInitialEdge(6, 7, 4);   // Downtown Plaza <-> River Bridge (4 km)
        addInitialEdge(6, 10, 3);  // Downtown Plaza <-> Police HQ (3 km)
        addInitialEdge(6, 11, 6);  // Downtown Plaza <-> Highway 101 (6 km)
        addInitialEdge(6, 12, 5);  // Downtown Plaza <-> Industrial Tech (5 km)
        addInitialEdge(7, 8, 3);   // River Bridge <-> Grand Central (3 km)
        addInitialEdge(7, 12, 4);  // River Bridge <-> Industrial Tech (4 km)
        addInitialEdge(7, 13, 7);  // River Bridge <-> Airport (7 km)
        addInitialEdge(7, 16, 8);  // River Bridge <-> East Riverfront (8 km)
        addInitialEdge(8, 13, 6);  // Grand Central <-> Airport (6 km)

        // Southwest & University Sector
        addInitialEdge(9, 10, 3);  // West Bay <-> Police HQ (3 km)
        addInitialEdge(9, 14, 6);  // West Bay <-> University (6 km)
        addInitialEdge(10, 11, 4); // Police HQ <-> Highway 101 (4 km)
        addInitialEdge(10, 14, 5); // Police HQ <-> University (5 km)
        addInitialEdge(11, 12, 4); // Highway 101 <-> Industrial Tech (4 km)
        addInitialEdge(11, 14, 4); // Highway 101 <-> University (4 km)
        addInitialEdge(11, 15, 3); // Highway 101 <-> South Hospital (3 km)
        addInitialEdge(14, 15, 7); // University <-> South Hospital (7 km)

        // Southeast & Express Bypass Sector
        addInitialEdge(12, 15, 4); // Industrial Tech <-> South Hospital (4 km)
        addInitialEdge(12, 16, 5); // Industrial Tech <-> East Riverfront (5 km)
        addInitialEdge(12, 17, 7); // Industrial Tech <-> Express Bypass (7 km)
        addInitialEdge(13, 16, 6); // Airport <-> East Riverfront (6 km)
        addInitialEdge(13, 17, 4); // Airport <-> Express Bypass (4 km)
        addInitialEdge(15, 16, 4); // South Hospital <-> East Riverfront (4 km)
        addInitialEdge(16, 17, 3); // East Riverfront <-> Express Bypass (3 km)

        // Strategic long-distance direct arteries (Congested direct routes vs smart bypasses)
        addInitialEdge(0, 6, 9);   // Central Hospital <-> Downtown (Direct Congested: 9 km vs Tunnel 4+5=9 km)
        addInitialEdge(8, 12, 7);  // Grand Central <-> Industrial Tech (7 km)
        addInitialEdge(10, 15, 8); // Police HQ <-> South Hospital (8 km)

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
