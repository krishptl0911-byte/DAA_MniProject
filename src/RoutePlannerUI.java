import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** The graphical application used for the classroom demonstration. */
public class RoutePlannerUI extends JFrame {
    private final CityGraph cityGraph = new CityGraph();
    private final String[] locations = cityGraph.getLocations();
    private final FloydWarshall algorithm = new FloydWarshall(cityGraph.getRoads());
    private final JComboBox<String> startBox = new JComboBox<>(locations);
    private final JComboBox<String> endBox = new JComboBox<>(locations);
    private final JLabel routeLabel = new JLabel("Choose a start and destination, then find the route.");
    private final JLabel distanceLabel = new JLabel("Total distance: —");
    private final JTextArea explanationArea = new JTextArea();

    public RoutePlannerUI() {
        setTitle("Emergency Vehicle Route Planner | Floyd–Warshall");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        buildInterface();
    }

    private void buildInterface() {
        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.setBackground(new Color(245, 248, 252));
        setContentPane(root);

        JPanel heading = new JPanel(new GridLayout(2, 1));
        heading.setOpaque(false);
        JLabel title = new JLabel("Emergency Vehicle Route Planner");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(new Color(178, 34, 34));
        JLabel subtitle = new JLabel("Shortest route between any two city locations using the Floyd–Warshall algorithm");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        heading.add(title); heading.add(subtitle);
        root.add(heading, BorderLayout.NORTH);

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        selector.setBackground(Color.WHITE);
        selector.setBorder(new EmptyBorder(8, 10, 8, 10));
        selector.add(new JLabel("Emergency vehicle starts at:"));
        selector.add(startBox);
        selector.add(new JLabel("Destination:"));
        selector.add(endBox);
        JButton findButton = new JButton("Find Shortest Route");
        findButton.setBackground(new Color(178, 34, 34));
        findButton.setForeground(Color.WHITE);
        findButton.setFocusPainted(false);
        findButton.addActionListener(e -> showRoute());
        selector.add(findButton);

        JPanel result = new JPanel(new GridLayout(2, 1, 0, 5));
        result.setBackground(new Color(255, 250, 235));
        result.setBorder(new EmptyBorder(12, 14, 12, 14));
        routeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        distanceLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        result.add(routeLabel); result.add(distanceLabel);

        explanationArea.setEditable(false);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        explanationArea.setBackground(Color.WHITE);
        explanationArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        explanationArea.setText("Algorithm status: All shortest paths were calculated once when the application started.\n"
                + "Time complexity: O(V³), where V is the number of locations.\n"
                + "Select any pair of locations to retrieve the stored best route immediately.");

        JPanel upper = new JPanel(new BorderLayout(10, 10));
        upper.setOpaque(false);
        upper.add(selector, BorderLayout.NORTH);
        upper.add(result, BorderLayout.CENTER);
        upper.add(explanationArea, BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("All-Pairs Shortest Distances", new JScrollPane(createDistanceTable()));
        tabs.addTab("Direct Road Network", new JScrollPane(createRoadTable()));
        root.add(upper, BorderLayout.CENTER);
        root.add(tabs, BorderLayout.SOUTH);
    }

    private JTable createDistanceTable() { return createTable(algorithm.getDistanceMatrix(), true); }
    private JTable createRoadTable() { return createTable(cityGraph.getRoads(), false); }

    private JTable createTable(int[][] values, boolean shortestPaths) {
        String[] columns = new String[locations.length + 1];
        columns[0] = shortestPaths ? "From / To" : "Location";
        System.arraycopy(locations, 0, columns, 1, locations.length);
        Object[][] rows = new Object[locations.length][locations.length + 1];
        for (int i = 0; i < locations.length; i++) {
            rows[i][0] = locations[i];
            for (int j = 0; j < locations.length; j++)
                rows[i][j + 1] = values[i][j] == CityGraph.INF ? "∞" : values[i][j] + " km";
        }
        JTable table = new JTable(new DefaultTableModel(rows, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        table.setRowHeight(25);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return table;
    }

    private void showRoute() {
        int start = startBox.getSelectedIndex();
        int end = endBox.getSelectedIndex();
        if (start == end) {
            routeLabel.setText("Route: You are already at " + locations[start] + ".");
            distanceLabel.setText("Total distance: 0 km");
            return;
        }
        List<Integer> path = algorithm.getPath(start, end);
        if (path.isEmpty()) {
            routeLabel.setText("No route is available between these locations.");
            distanceLabel.setText("Total distance: Not reachable");
            return;
        }
        StringBuilder route = new StringBuilder("Route: ");
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) route.append("  →  ");
            route.append(locations[path.get(i)]);
        }
        routeLabel.setText(route.toString());
        distanceLabel.setText("Total shortest distance: " + algorithm.getDistance(start, end) + " km");
    }
}
