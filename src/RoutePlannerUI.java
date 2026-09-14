import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Modern Emergency Dispatch Command Center UI.
 * Integrates interactive 2D graph simulation, vehicle dispatch animation,
 * step-by-step DP matrix inspector, matrix tabs, and DAA algorithm research analytics.
 */
public class RoutePlannerUI extends JFrame implements GraphVisualizerPanel.RouteChangeListener {
    private final CityGraph cityGraph = new CityGraph();
    private FloydWarshall algorithm;

    private GraphVisualizerPanel visualizerPanel;
    private AlgorithmVisualizerPanel algoVisualizerPanel;
    private AnalyticsPanel analyticsPanel;

    private JComboBox<String> startBox;
    private JComboBox<String> endBox;
    private JComboBox<EmergencyVehicle.VehicleType> vehicleTypeBox;

    private JLabel routeSummaryLabel;
    private JLabel distanceBadgeLabel;
    private JLabel etaBadgeLabel;
    private DefaultListModel<String> turnByTurnListModel;
    private JList<String> turnByTurnList;

    private JTable distanceTable;
    private JTable nextTable;
    private JTable directRoadTable;

    public RoutePlannerUI() {
        setTitle("911 Emergency Vehicle Route Planner & DAA Visualizer | Floyd–Warshall");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 780);
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);

        recalculateAlgorithm();
        buildInterface();
        updateRouteDisplay();
    }

    private void recalculateAlgorithm() {
        this.algorithm = new FloydWarshall(cityGraph.getRoads(), cityGraph.getLocations());
        if (algoVisualizerPanel != null) {
            algoVisualizerPanel.recompute();
        }
        if (analyticsPanel != null) {
            analyticsPanel.refreshMetrics();
        }
        refreshMatrixTables();
    }

    private void buildInterface() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(15, 23, 42));
        setContentPane(root);

        // Top Command Center Header Bar
        root.add(createHeaderBar(), BorderLayout.NORTH);

        // Main Tabbed Pane
        JTabbedPane mainTabs = new JTabbedPane();
        mainTabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        mainTabs.setBackground(new Color(30, 41, 59));
        mainTabs.setForeground(new Color(241, 245, 249));

        // Tab 1: Live Interactive Map & Dispatch Center
        mainTabs.addTab("🗺️ Live Map & 911 Dispatch", createMapAndDispatchTab());

        // Tab 2: Floyd-Warshall Step-by-Step Algorithm Visualizer
        algoVisualizerPanel = new AlgorithmVisualizerPanel(cityGraph);
        mainTabs.addTab("⚡ Floyd–Warshall DP Step Visualizer", algoVisualizerPanel);

        // Tab 3: Distance & Routing Matrices
        mainTabs.addTab("📋 Distance & Next-Hop Matrices", createMatricesTab());

        // Tab 4: DAA Analytics & Complexity Comparison
        analyticsPanel = new AnalyticsPanel(cityGraph);
        mainTabs.addTab("📊 DAA Research & Complexity Benchmarks", analyticsPanel);

        root.add(mainTabs, BorderLayout.CENTER);
    }

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setBackground(new Color(15, 23, 42));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(51, 65, 85)),
                new EmptyBorder(10, 16, 10, 16)
        ));

        // Left Branding
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel logo = new JLabel("🚨");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("EMERGENCY VEHICLE DISPATCH COMMAND SYSTEM");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(new Color(248, 113, 113));

        JLabel sub = new JLabel("DAA Mini Project • All-Pairs Shortest Path Optimization with Floyd–Warshall Algorithm O(V³)");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(new Color(148, 163, 184));

        titleBlock.add(title);
        titleBlock.add(sub);
        brandPanel.add(logo);
        brandPanel.add(titleBlock);

        // Right Quick Actions
        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        quickActions.setOpaque(false);

        JButton resetMapBtn = createSmallButton("↺ Reset Road Grid", new Color(51, 65, 85));
        resetMapBtn.addActionListener(e -> {
            cityGraph.loadDefaultMetroCity();
            recalculateAlgorithm();
            visualizerPanel.repaint();
            updateRouteDisplay();
        });

        quickActions.add(resetMapBtn);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(quickActions, BorderLayout.EAST);
        return header;
    }

    private JPanel createMapAndDispatchTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(15, 23, 42));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Center: Interactive 2D Map Canvas
        visualizerPanel = new GraphVisualizerPanel(cityGraph);
        visualizerPanel.setRouteChangeListener(this);
        visualizerPanel.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1));
        panel.add(visualizerPanel, BorderLayout.CENTER);

        // Right Sidebar: Dispatch Console & Turn-by-Turn GPS Navigation
        JPanel sidebar = createDispatchSidebar();
        sidebar.setPreferredSize(new Dimension(360, 0));
        panel.add(sidebar, BorderLayout.EAST);

        return panel;
    }

    private JPanel createDispatchSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(10, 10));
        sidebar.setBackground(new Color(30, 41, 59));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                new EmptyBorder(12, 12, 12, 12)
        ));

        // Top: Selection Controls
        JPanel controls = new JPanel(new GridLayout(6, 1, 4, 6));
        controls.setOpaque(false);

        JLabel selectHeader = new JLabel("🎯 INCIDENT & DISPATCH ROUTING");
        selectHeader.setFont(new Font("SansSerif", Font.BOLD, 13));
        selectHeader.setForeground(new Color(56, 189, 248));

        String[] locNames = cityGraph.getLocations();
        startBox = new JComboBox<>(locNames);
        startBox.setSelectedIndex(0); // Central Hospital
        startBox.addActionListener(e -> {
            if (visualizerPanel != null) {
                updateRouteDisplay();
            }
        });

        endBox = new JComboBox<>(locNames);
        endBox.setSelectedIndex(5); // Accident Zone
        endBox.addActionListener(e -> {
            if (visualizerPanel != null) {
                updateRouteDisplay();
            }
        });

        vehicleTypeBox = new JComboBox<>(EmergencyVehicle.VehicleType.values());
        vehicleTypeBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof EmergencyVehicle.VehicleType) {
                    EmergencyVehicle.VehicleType vt = (EmergencyVehicle.VehicleType) value;
                    setText(vt.icon + " " + vt.name + " (" + (int) vt.avgSpeedKmh + " km/h)");
                }
                return this;
            }
        });

        controls.add(selectHeader);

        JPanel p1 = new JPanel(new BorderLayout(5, 0));
        p1.setOpaque(false);
        JLabel l1 = new JLabel("Start Base:");
        l1.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l1.setForeground(new Color(203, 213, 225));
        p1.add(l1, BorderLayout.WEST);
        p1.add(startBox, BorderLayout.CENTER);
        controls.add(p1);

        JPanel p2 = new JPanel(new BorderLayout(5, 0));
        p2.setOpaque(false);
        JLabel l2 = new JLabel("Destination:");
        l2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l2.setForeground(new Color(203, 213, 225));
        p2.add(l2, BorderLayout.WEST);
        p2.add(endBox, BorderLayout.CENTER);
        controls.add(p2);

        JPanel p3 = new JPanel(new BorderLayout(5, 0));
        p3.setOpaque(false);
        JLabel l3 = new JLabel("Vehicle Unit:");
        l3.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l3.setForeground(new Color(203, 213, 225));
        p3.add(l3, BorderLayout.WEST);
        p3.add(vehicleTypeBox, BorderLayout.CENTER);
        controls.add(p3);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        btnPanel.setOpaque(false);

        JButton dispatchBtn = new JButton("🚨 Dispatch Vehicle");
        dispatchBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        dispatchBtn.setBackground(new Color(229, 57, 53));
        dispatchBtn.setForeground(Color.WHITE);
        dispatchBtn.setFocusPainted(false);
        dispatchBtn.addActionListener(e -> dispatchVehicleSimulation());

        JButton autoNearestBtn = new JButton("⚡ Closest Unit");
        autoNearestBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        autoNearestBtn.setBackground(new Color(30, 136, 229));
        autoNearestBtn.setForeground(Color.WHITE);
        autoNearestBtn.setFocusPainted(false);
        autoNearestBtn.setToolTipText("Find nearest station to destination in O(1)");
        autoNearestBtn.addActionListener(e -> autoDispatchClosestUnit());

        btnPanel.add(dispatchBtn);
        btnPanel.add(autoNearestBtn);
        controls.add(btnPanel);

        // Center: Route Summary & Metrics Card
        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setOpaque(false);

        JPanel metricsCard = new JPanel(new GridLayout(2, 2, 6, 6));
        metricsCard.setBackground(new Color(15, 23, 42));
        metricsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                new EmptyBorder(8, 8, 8, 8)
        ));

        distanceBadgeLabel = new JLabel("Distance: 0 km");
        distanceBadgeLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        distanceBadgeLabel.setForeground(new Color(52, 211, 153));

        etaBadgeLabel = new JLabel("ETA: 0 min");
        etaBadgeLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        etaBadgeLabel.setForeground(new Color(250, 204, 21));

        metricsCard.add(distanceBadgeLabel);
        metricsCard.add(etaBadgeLabel);

        routeSummaryLabel = new JLabel("Route: —");
        routeSummaryLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        routeSummaryLabel.setForeground(new Color(226, 232, 240));

        JPanel summaryHolder = new JPanel(new BorderLayout(4, 4));
        summaryHolder.setOpaque(false);
        summaryHolder.add(metricsCard, BorderLayout.NORTH);
        summaryHolder.add(routeSummaryLabel, BorderLayout.SOUTH);

        // Bottom: GPS Turn-by-Turn Navigation Directions List
        JPanel navPanel = new JPanel(new BorderLayout(4, 4));
        navPanel.setOpaque(false);

        JLabel navHeader = new JLabel("🧭 GPS Turn-by-Turn Navigation Steps:");
        navHeader.setFont(new Font("SansSerif", Font.BOLD, 12));
        navHeader.setForeground(new Color(148, 163, 184));

        turnByTurnListModel = new DefaultListModel<>();
        turnByTurnList = new JList<>(turnByTurnListModel);
        turnByTurnList.setBackground(new Color(15, 23, 42));
        turnByTurnList.setForeground(new Color(241, 245, 249));
        turnByTurnList.setFont(new Font("SansSerif", Font.PLAIN, 12));
        turnByTurnList.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        JScrollPane navScroll = new JScrollPane(turnByTurnList);
        navScroll.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1));

        navPanel.add(navHeader, BorderLayout.NORTH);
        navPanel.add(navScroll, BorderLayout.CENTER);

        centerPanel.add(summaryHolder, BorderLayout.NORTH);
        centerPanel.add(navPanel, BorderLayout.CENTER);

        sidebar.add(controls, BorderLayout.NORTH);
        sidebar.add(centerPanel, BorderLayout.CENTER);

        return sidebar;
    }

    private JPanel createMatricesTab() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBackground(new Color(15, 23, 42));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. All-Pairs Shortest Distances Table
        distanceTable = createStyledTable();
        JPanel p1 = createTableWrapper("📍 All-Pairs Shortest Distance Matrix D[i][j] (Computed by Floyd–Warshall)", distanceTable);

        // 2. Next-Hop Matrix Table
        nextTable = createStyledTable();
        JPanel p2 = createTableWrapper("🧭 Next-Hop Reconstruction Matrix Next[i][j] (Used to reconstruct optimal paths in O(L))", nextTable);

        // 3. Direct Road Network
        directRoadTable = createStyledTable();
        JPanel p3 = createTableWrapper("🛣️ Direct City Road Network Adjacency Matrix (Base Graph Inputs)", directRoadTable);

        panel.add(p1);
        panel.add(p2);
        panel.add(p3);

        refreshMatrixTables();
        return panel;
    }

    private JPanel createTableWrapper(String title, JTable table) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBackground(new Color(30, 41, 59));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                new EmptyBorder(6, 8, 6, 8)
        ));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(new Color(56, 189, 248));

        p.add(lbl, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private JTable createStyledTable() {
        JTable table = new JTable();
        table.setRowHeight(22);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(15, 23, 42));
        table.getTableHeader().setForeground(new Color(241, 245, 249));
        table.setBackground(new Color(30, 41, 59));
        table.setForeground(new Color(226, 232, 240));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (col == 0) {
                    setBackground(new Color(15, 23, 42));
                    setForeground(new Color(56, 189, 248));
                    setFont(new Font("SansSerif", Font.BOLD, 11));
                } else {
                    setBackground(new Color(30, 41, 59));
                    setForeground(new Color(226, 232, 240));
                    setFont(new Font("SansSerif", Font.PLAIN, 11));
                }
                return c;
            }
        });
        return table;
    }

    private void refreshMatrixTables() {
        if (distanceTable == null || nextTable == null || directRoadTable == null) return;

        String[] locs = cityGraph.getLocations();
        String[] cols = new String[locs.length + 1];
        cols[0] = "From \\ To";
        System.arraycopy(locs, 0, cols, 1, locs.length);

        int[][] dist = algorithm.getDistanceMatrix();
        int[][] next = algorithm.getNextMatrix();
        int[][] roads = cityGraph.getRoads();

        Object[][] distData = new Object[locs.length][locs.length + 1];
        Object[][] nextData = new Object[locs.length][locs.length + 1];
        Object[][] roadData = new Object[locs.length][locs.length + 1];

        for (int i = 0; i < locs.length; i++) {
            distData[i][0] = locs[i];
            nextData[i][0] = locs[i];
            roadData[i][0] = locs[i];
            for (int j = 0; j < locs.length; j++) {
                distData[i][j + 1] = (dist[i][j] == CityGraph.INF) ? "∞" : dist[i][j] + " km";
                nextData[i][j + 1] = (next[i][j] == -1) ? "—" : (locs[next[i][j]] + " (" + next[i][j] + ")");
                roadData[i][j + 1] = (roads[i][j] == CityGraph.INF) ? "—" : roads[i][j] + " km";
            }
        }

        distanceTable.setModel(new DefaultTableModel(distData, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        nextTable.setModel(new DefaultTableModel(nextData, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        directRoadTable.setModel(new DefaultTableModel(roadData, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
    }

    private void updateRouteDisplay() {
        int start = startBox.getSelectedIndex();
        int end = endBox.getSelectedIndex();

        if (start < 0 || end < 0) return;

        String[] locs = cityGraph.getLocations();
        List<Integer> path = algorithm.getPath(start, end);

        visualizerPanel.setSelection(start, end, path);

        turnByTurnListModel.clear();

        if (start == end) {
            distanceBadgeLabel.setText("Distance: 0 km");
            etaBadgeLabel.setText("ETA: 0 min");
            routeSummaryLabel.setText("<html><b>Route:</b> Already at " + locs[start] + "</html>");
            turnByTurnListModel.addElement("📍 Start and destination are the same location.");
            return;
        }

        if (path.isEmpty()) {
            distanceBadgeLabel.setText("Distance: Unreachable");
            etaBadgeLabel.setText("ETA: N/A");
            routeSummaryLabel.setText("<html><font color='#ef4444'><b>No road connection available!</b> Check road blocks.</font></html>");
            turnByTurnListModel.addElement("⚠️ Destination is disconnected in current graph.");
            return;
        }

        int totalDist = algorithm.getDistance(start, end);
        EmergencyVehicle.VehicleType vType = (EmergencyVehicle.VehicleType) vehicleTypeBox.getSelectedItem();
        double speed = vType != null ? vType.avgSpeedKmh : 60.0;
        double etaMinutes = (totalDist / speed) * 60.0;

        distanceBadgeLabel.setText("Distance: " + totalDist + " km");
        etaBadgeLabel.setText(String.format("ETA: %.1f mins (@ %.0f km/h)", etaMinutes, speed));

        StringBuilder sb = new StringBuilder("<html><b>Shortest Path:</b> ");
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) sb.append(" → ");
            sb.append(locs[path.get(i)]);
        }
        sb.append("</html>");
        routeSummaryLabel.setText(sb.toString());

        // Build Turn-by-Turn GPS Steps
        int[][] roads = cityGraph.getRoads();
        for (int i = 0; i < path.size() - 1; i++) {
            int from = path.get(i);
            int to = path.get(i + 1);
            int segDist = roads[from][to];
            double segEta = (segDist / speed) * 60.0;
            turnByTurnListModel.addElement(String.format("Step %d: Proceed from %s → %s (%d km, ~%.1f min)",
                    i + 1, locs[from], locs[to], segDist, segEta));
        }
        turnByTurnListModel.addElement("🏁 Arrive at Incident Target: " + locs[end]);
    }

    private void dispatchVehicleSimulation() {
        int start = startBox.getSelectedIndex();
        int end = endBox.getSelectedIndex();
        if (start < 0 || end < 0) return;

        List<Integer> path = algorithm.getPath(start, end);
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot dispatch: Destination is unreachable due to blocked roads.",
                    "Routing Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        EmergencyVehicle.VehicleType vType = (EmergencyVehicle.VehicleType) vehicleTypeBox.getSelectedItem();
        if (vType == null) vType = EmergencyVehicle.VehicleType.AMBULANCE;

        EmergencyVehicle vehicle = new EmergencyVehicle("UNIT-911", vType);
        vehicle.dispatch(path, cityGraph.getNodes(), cityGraph.getRoads());
        visualizerPanel.addVehicle(vehicle);
    }

    private void autoDispatchClosestUnit() {
        int destination = endBox.getSelectedIndex();
        if (destination < 0) return;

        int[][] dist = algorithm.getDistanceMatrix();
        List<CityGraph.CityNode> nodes = cityGraph.getNodes();

        int bestNode = -1;
        int minDistance = CityGraph.INF;

        // Find nearest emergency response base (Hospital, Fire Station, Police)
        for (int i = 0; i < nodes.size(); i++) {
            CityGraph.LocationType type = nodes.get(i).type;
            if (type == CityGraph.LocationType.HOSPITAL ||
                type == CityGraph.LocationType.FIRE_STATION ||
                type == CityGraph.LocationType.POLICE_STATION) {
                if (dist[i][destination] < minDistance) {
                    minDistance = dist[i][destination];
                    bestNode = i;
                }
            }
        }

        if (bestNode != -1 && minDistance != CityGraph.INF) {
            startBox.setSelectedIndex(bestNode);
            // Select appropriate vehicle based on node type
            CityGraph.LocationType type = nodes.get(bestNode).type;
            if (type == CityGraph.LocationType.HOSPITAL) vehicleTypeBox.setSelectedItem(EmergencyVehicle.VehicleType.AMBULANCE);
            else if (type == CityGraph.LocationType.FIRE_STATION) vehicleTypeBox.setSelectedItem(EmergencyVehicle.VehicleType.FIRE_ENGINE);
            else if (type == CityGraph.LocationType.POLICE_STATION) vehicleTypeBox.setSelectedItem(EmergencyVehicle.VehicleType.POLICE_CRUISER);

            updateRouteDisplay();
            dispatchVehicleSimulation();
        } else {
            JOptionPane.showMessageDialog(this, "No reachable emergency base found for this location.", "Notice", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JButton createSmallButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return btn;
    }

    @Override
    public void onSelectionChanged(int start, int end) {
        if (start >= 0 && start < startBox.getItemCount()) startBox.setSelectedIndex(start);
        if (end >= 0 && end < endBox.getItemCount()) endBox.setSelectedIndex(end);
        updateRouteDisplay();
    }

    @Override
    public void onGraphStructureChanged() {
        recalculateAlgorithm();
        updateRouteDisplay();
    }
}
