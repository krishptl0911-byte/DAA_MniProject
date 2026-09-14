import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * DAA Academic Research & Analytics Dashboard.
 * Presents comparative algorithm analysis (Floyd-Warshall vs Dijkstra vs Bellman-Ford vs A*),
 * live benchmark profiling, graph network topology metrics, and DP theory.
 */
public class AnalyticsPanel extends JPanel {
    private final CityGraph graph;

    private JLabel verticesLabel;
    private JLabel edgesLabel;
    private JLabel densityLabel;
    private JLabel diameterLabel;
    private JLabel avgPathLabel;
    private JLabel fwRuntimeLabel;
    private JLabel dijkstraRuntimeLabel;
    private JTextArea mathematicalProofArea;

    public AnalyticsPanel(CityGraph graph) {
        this.graph = graph;
        setLayout(new BorderLayout(14, 14));
        setBackground(new Color(15, 23, 42));
        setBorder(new EmptyBorder(14, 14, 14, 14));

        buildUI();
        refreshMetrics();
    }

    private void buildUI() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("📊 DAA Algorithm Analytics, Complexity & Performance Profiling");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(56, 189, 248));
        headerPanel.add(title, BorderLayout.WEST);

        JButton benchmarkBtn = new JButton("⚡ Run Live Benchmark");
        benchmarkBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        benchmarkBtn.setBackground(new Color(16, 185, 129));
        benchmarkBtn.setForeground(Color.WHITE);
        benchmarkBtn.setFocusPainted(false);
        benchmarkBtn.addActionListener(e -> refreshMetrics());
        headerPanel.add(benchmarkBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center Content: Tabbed or Split View
        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 12, 12));
        contentPanel.setOpaque(false);

        // Upper Section: 2 columns (Graph Metrics & Live Benchmark | Algorithm Comparison Table)
        JPanel upperSection = new JPanel(new GridLayout(1, 2, 12, 12));
        upperSection.setOpaque(false);

        // Metric Cards Container
        JPanel metricsContainer = new JPanel(new BorderLayout(8, 8));
        metricsContainer.setOpaque(false);

        JPanel metricCards = new JPanel(new GridLayout(3, 2, 8, 8));
        metricCards.setOpaque(false);

        verticesLabel = createMetricCard(metricCards, "Total Locations (|V|)", "8 Vertices", new Color(59, 130, 246));
        edgesLabel = createMetricCard(metricCards, "Road Segments (|E|)", "15 Edges", new Color(16, 185, 129));
        densityLabel = createMetricCard(metricCards, "Graph Density", "0.54 (Dense)", new Color(245, 158, 11));
        diameterLabel = createMetricCard(metricCards, "Network Diameter", "13 km", new Color(236, 72, 153));
        fwRuntimeLabel = createMetricCard(metricCards, "Floyd–Warshall Time", "—", new Color(139, 92, 246));
        dijkstraRuntimeLabel = createMetricCard(metricCards, "V × Dijkstra Time", "—", new Color(6, 182, 212));

        metricsContainer.add(metricCards, BorderLayout.CENTER);
        upperSection.add(metricsContainer);

        // Algorithm Comparison Table
        JPanel tableContainer = new JPanel(new BorderLayout(6, 6));
        tableContainer.setBackground(new Color(30, 41, 59));
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JLabel tableTitle = new JLabel("Theoretical Complexity & Feature Comparison");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        tableTitle.setForeground(new Color(241, 245, 249));
        tableContainer.add(tableTitle, BorderLayout.NORTH);

        JTable compTable = createComparisonTable();
        tableContainer.add(new JScrollPane(compTable), BorderLayout.CENTER);
        upperSection.add(tableContainer);

        contentPanel.add(upperSection);

        // Lower Section: DAA Dynamic Programming Formulation Card
        JPanel dpTheoryCard = new JPanel(new BorderLayout(8, 8));
        dpTheoryCard.setBackground(new Color(30, 41, 59));
        dpTheoryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel theoryTitle = new JLabel("🧠 Dynamic Programming Formulation & Design Principles (Floyd–Warshall)");
        theoryTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        theoryTitle.setForeground(new Color(250, 204, 21));

        mathematicalProofArea = new JTextArea();
        mathematicalProofArea.setEditable(false);
        mathematicalProofArea.setLineWrap(true);
        mathematicalProofArea.setWrapStyleWord(true);
        mathematicalProofArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        mathematicalProofArea.setBackground(new Color(15, 23, 42));
        mathematicalProofArea.setForeground(new Color(226, 232, 240));
        mathematicalProofArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                new EmptyBorder(8, 10, 8, 10)
        ));

        setTheoryText();

        dpTheoryCard.add(theoryTitle, BorderLayout.NORTH);
        dpTheoryCard.add(new JScrollPane(mathematicalProofArea), BorderLayout.CENTER);

        contentPanel.add(dpTheoryCard);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JLabel createMetricCard(JPanel parent, String title, String initialValue, Color accent) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(new Color(30, 41, 59));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        titleLbl.setForeground(new Color(148, 163, 184));

        JLabel valLbl = new JLabel(initialValue);
        valLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        valLbl.setForeground(accent);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        parent.add(card);
        return valLbl;
    }

    private JTable createComparisonTable() {
        String[] columns = {"Algorithm", "Paradigm", "Time Complexity", "Space", "Negative Weights", "Optimal Use-Case"};
        Object[][] data = {
            {"Floyd–Warshall", "Dynamic Prog.", "O(V³)", "O(V²)", "Yes (No neg cycles)", "All-Pairs / Precomputed Quick Dispatch"},
            {"Dijkstra (Heap)", "Greedy", "O((V+E) log V)", "O(V)", "No", "Single-Source, Non-negative weights"},
            {"Bellman–Ford", "Dynamic Prog.", "O(V · E)", "O(V)", "Yes (Detects neg cycles)", "Single-Source with negative edge costs"},
            {"A* Search", "Heuristic / Best-First", "O(b^d) ~ O(E)", "O(V)", "No", "Point-to-Point with Euclidean Heuristic"}
        };

        JTable table = new JTable(new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(15, 23, 42));
        table.getTableHeader().setForeground(new Color(241, 245, 249));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(table, value, isSel, hasFocus, r, c);
                setBackground(r == 0 ? new Color(30, 58, 138, 80) : new Color(30, 41, 59));
                setForeground(r == 0 ? new Color(147, 197, 253) : new Color(226, 232, 240));
                setFont(new Font("SansSerif", r == 0 ? Font.BOLD : Font.PLAIN, 11));
                return comp;
            }
        });

        return table;
    }

    public void refreshMetrics() {
        int n = graph.getNodeCount();
        int[][] roads = graph.getRoads();

        int edgeCount = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (roads[i][j] != CityGraph.INF) edgeCount++;
            }
        }

        double maxPossibleEdges = (n * (n - 1)) / 2.0;
        double density = maxPossibleEdges > 0 ? (edgeCount / maxPossibleEdges) : 0;

        // Run Floyd-Warshall Benchmark
        long fwStart = System.nanoTime();
        FloydWarshall fw = new FloydWarshall(roads);
        long fwEnd = System.nanoTime();
        long fwDuration = fwEnd - fwStart;

        // Run V x Dijkstra Benchmark
        long dijkstraStart = System.nanoTime();
        for (int i = 0; i < n; i++) {
            new Dijkstra(roads, i);
        }
        long dijkstraEnd = System.nanoTime();
        long dijkstraDuration = dijkstraEnd - dijkstraStart;

        int diameter = 0;
        int sumDistances = 0;
        int reachablePairs = 0;
        int[][] distMat = fw.getDistanceMatrix();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && distMat[i][j] != CityGraph.INF) {
                    diameter = Math.max(diameter, distMat[i][j]);
                    sumDistances += distMat[i][j];
                    reachablePairs++;
                }
            }
        }

        double avgDistance = reachablePairs > 0 ? ((double) sumDistances / reachablePairs) : 0;

        verticesLabel.setText(n + " Locations");
        edgesLabel.setText(edgeCount + " Active Roads");
        densityLabel.setText(String.format("%.2f (%s)", density, density > 0.4 ? "Dense Graph" : "Sparse Graph"));
        diameterLabel.setText(diameter + " km (Max Pair)");
        fwRuntimeLabel.setText(String.format("%,d ns (%.3f ms)", fwDuration, fwDuration / 1_000_000.0));
        dijkstraRuntimeLabel.setText(String.format("%,d ns (%.3f ms)", dijkstraDuration, dijkstraDuration / 1_000_000.0));
    }

    private void setTheoryText() {
        mathematicalProofArea.setText(
            "1. SUBPROBLEM DEFINITION:\n" +
            "   Let D^(k)[i][j] be the shortest distance from vertex i to vertex j using only intermediate vertices from {0, 1, ..., k}.\n\n" +
            "2. BASE CASE (k = -1 / Initial Adjacency Matrix):\n" +
            "   D^(-1)[i][j] = weight(i, j) if edge exists; 0 if i == j; ∞ otherwise.\n" +
            "   Next^(-1)[i][j] = j if edge exists; -1 otherwise.\n\n" +
            "3. RECURRENCE RELATION (Dynamic Programming Step):\n" +
            "   For k = 0 to V-1:\n" +
            "       D^(k)[i][j] = min( D^(k-1)[i][j],  D^(k-1)[i][k] + D^(k-1)[k][j] )\n" +
            "       If updated: Next[i][j] = Next[i][k]\n\n" +
            "4. REAL-WORLD DAA REASONING FOR EMERGENCY VEHICLE DISPATCH:\n" +
            "   - In an emergency dispatch 911 system, incidents occur dynamically at any location.\n" +
            "   - Precomputing the O(V^3) All-Pairs matrix once during initialization enables O(1) instantaneous lookup\n" +
            "     to locate the nearest ambulance, fire station, or police cruiser when an emergency is reported."
        );
    }
}
