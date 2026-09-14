import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an emergency vehicle in simulation with live waypoint navigation,
 * real-time coordinates, siren strobe state, and travel metrics.
 */
public class EmergencyVehicle {
    public enum VehicleType {
        AMBULANCE("Ambulance", "🚑", new Color(229, 57, 53), 70.0, 1),
        FIRE_ENGINE("Fire Engine", "🚒", new Color(244, 81, 30), 55.0, 1),
        POLICE_CRUISER("Police Cruiser", "🚓", new Color(30, 136, 229), 85.0, 2),
        RESCUE_VAN("Rescue Squad", "🚐", new Color(251, 140, 0), 60.0, 2);

        public final String name;
        public final String icon;
        public final Color themeColor;
        public final double avgSpeedKmh; // Average emergency response speed
        public final int priority;

        VehicleType(String name, String icon, Color themeColor, double avgSpeedKmh, int priority) {
            this.name = name;
            this.icon = icon;
            this.themeColor = themeColor;
            this.avgSpeedKmh = avgSpeedKmh;
            this.priority = priority;
        }
    }

    public enum Status {
        IDLE,
        EN_ROUTE,
        ON_SCENE
    }

    private final VehicleType type;
    private final String id;
    private Status status = Status.IDLE;
    private List<Integer> pathNodeIndices = new ArrayList<>();
    private List<CityGraph.CityNode> waypoints = new ArrayList<>();

    private int currentSegmentIndex = 0;
    private double segmentProgress = 0.0; // 0.0 to 1.0
    private double currentX = 0;
    private double currentY = 0;
    private double headingAngle = 0; // In radians
    private boolean sirenFlash = false;
    private int sirenTick = 0;
    private double totalDistanceKm = 0;
    private double remainingDistanceKm = 0;

    public EmergencyVehicle(String id, VehicleType type) {
        this.id = id;
        this.type = type;
    }

    public void dispatch(List<Integer> pathNodes, List<CityGraph.CityNode> allNodes, int[][] distances) {
        if (pathNodes == null || pathNodes.size() < 2) {
            status = Status.IDLE;
            return;
        }
        this.pathNodeIndices = new ArrayList<>(pathNodes);
        this.waypoints = new ArrayList<>();
        for (int idx : pathNodes) {
            waypoints.add(allNodes.get(idx));
        }

        this.currentSegmentIndex = 0;
        this.segmentProgress = 0.0;
        this.currentX = waypoints.get(0).x;
        this.currentY = waypoints.get(0).y;
        this.status = Status.EN_ROUTE;

        // Calculate total path distance
        this.totalDistanceKm = 0;
        for (int i = 0; i < pathNodes.size() - 1; i++) {
            int u = pathNodes.get(i);
            int v = pathNodes.get(i + 1);
            if (distances != null && u < distances.length && v < distances.length) {
                totalDistanceKm += distances[u][v];
            }
        }
        this.remainingDistanceKm = totalDistanceKm;
        updateHeading();
    }

    public void update(double deltaSpeedFactor) {
        sirenTick++;
        if (sirenTick % 6 == 0) {
            sirenFlash = !sirenFlash;
        }

        if (status != Status.EN_ROUTE || waypoints.size() < 2) {
            return;
        }

        CityGraph.CityNode from = waypoints.get(currentSegmentIndex);
        CityGraph.CityNode to = waypoints.get(currentSegmentIndex + 1);

        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double segmentLengthPixels = Math.hypot(dx, dy);

        if (segmentLengthPixels < 1) {
            segmentProgress = 1.0;
        } else {
            // Speed factor controls animation velocity (pixels per frame)
            double step = (type.avgSpeedKmh * 0.06 * deltaSpeedFactor) / segmentLengthPixels;
            segmentProgress += step;
        }

        if (segmentProgress >= 1.0) {
            currentSegmentIndex++;
            if (currentSegmentIndex >= waypoints.size() - 1) {
                // Arrived at destination
                status = Status.ON_SCENE;
                currentX = waypoints.get(waypoints.size() - 1).x;
                currentY = waypoints.get(waypoints.size() - 1).y;
                remainingDistanceKm = 0;
                return;
            } else {
                segmentProgress = 0.0;
                from = waypoints.get(currentSegmentIndex);
                to = waypoints.get(currentSegmentIndex + 1);
            }
        }

        currentX = from.x + (to.x - from.x) * segmentProgress;
        currentY = from.y + (to.y - from.y) * segmentProgress;
        updateHeading();
    }

    private void updateHeading() {
        if (currentSegmentIndex < waypoints.size() - 1) {
            CityGraph.CityNode from = waypoints.get(currentSegmentIndex);
            CityGraph.CityNode to = waypoints.get(currentSegmentIndex + 1);
            headingAngle = Math.atan2(to.y - from.y, to.x - from.x);
        }
    }

    public double getEtaMinutes() {
        if (status == Status.ON_SCENE) return 0.0;
        return (totalDistanceKm / type.avgSpeedKmh) * 60.0;
    }

    public VehicleType getType() { return type; }
    public String getId() { return id; }
    public Status getStatus() { return status; }
    public double getCurrentX() { return currentX; }
    public double getCurrentY() { return currentY; }
    public double getHeadingAngle() { return headingAngle; }
    public boolean isSirenFlash() { return sirenFlash; }
    public double getTotalDistanceKm() { return totalDistanceKm; }
    public List<Integer> getPathNodeIndices() { return pathNodeIndices; }
}
