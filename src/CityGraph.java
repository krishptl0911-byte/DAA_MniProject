/** Stores the city locations and direct road distances for the demo. */
public class CityGraph {
    public static final int INF = 999999;

    private final String[] locations = {
        "Central Hospital", "Fire Station", "Police Station", "City Center",
        "Railway Station", "Accident Zone", "Airport", "University"
    };

    // A weighted, undirected graph. INF means that no direct road exists.
    private final int[][] roads = {
        {0,   4,   6,   3,   INF, INF, INF, 7},
        {4,   0,   3,   INF, 8,   INF, INF, INF},
        {6,   3,   0,   2,   INF, 7,   INF, INF},
        {3,   INF, 2,   0,   5,   INF, 10,  4},
        {INF, 8,   INF, 5,   0,   3,   INF, INF},
        {INF, INF, 7,   INF, 3,   0,   6,   5},
        {INF, INF, INF, 10,  INF, 6,   0,   8},
        {7,   INF, INF, 4,   INF, 5,   8,   0}
    };

    public String[] getLocations() {
        return locations.clone();
    }

    public int[][] getRoads() {
        int[][] copy = new int[roads.length][roads.length];
        for (int i = 0; i < roads.length; i++) {
            System.arraycopy(roads[i], 0, copy[i], 0, roads.length);
        }
        return copy;
    }
}
