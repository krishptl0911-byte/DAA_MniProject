import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Step-by-Step Floyd-Warshall DP Matrix Transformation Inspector.
 * Demonstrates the DAA concept with step playback, cell highlighting, and live recurrence formula evaluation.
 */
public class AlgorithmVisualizerPanel extends JPanel {
    private final CityGraph graph;
    private FloydWarshall algorithm;
    private List<FloydWarshall.StepSnapshot> steps;
    private int currentStepIndex = 0;

    private JTable matrixTable;
    private DefaultTableModel tableModel;
    private JLabel stepInfoLabel;
    private JLabel formulaLabel;
    private JTextArea explanationArea;
    private JSlider stepSlider;
    private JSlider speedSlider;
    private JButton playPauseBtn;
    private Timer playbackTimer;
    private boolean isPlaying = false;

    public AlgorithmVisualizerPanel(CityGraph graph) {
        this.graph = graph;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(15, 23, 42));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        this.algorithm = new FloydWarshall(graph.getRoads(), graph.getLocations());
        this.steps = algorithm.getSteps();
        this.currentStepIndex = 0;

        buildUI();
        updateDisplay();
    }

    public void recompute() {
        this.algorithm = new FloydWarshall(graph.getRoads(), graph.getLocations());
        this.steps = algorithm.getSteps();
        this.currentStepIndex = 0;
        if (stepSlider != null) {
            stepSlider.setMaximum(Math.max(0, steps.size() - 1));
            stepSlider.setValue(0);
        }
        updateDisplay();
    }

    private void buildUI() {
        // Top Control Header
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setOpaque(false);

        JLabel header = new JLabel("⚡ Floyd–Warshall Dynamic Programming Visualizer & Step Inspector");
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setForeground(new Color(56, 189, 248));

        stepInfoLabel = new JLabel("Step 0 / " + (steps.size() - 1));
        stepInfoLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        stepInfoLabel.setForeground(new Color(241, 245, 249));

        topPanel.add(header, BorderLayout.WEST);
        topPanel.add(stepInfoLabel, BorderLayout.EAST);

        // Center Matrix Table
        createMatrixTable();
        JScrollPane scrollPane = new JScrollPane(matrixTable);
        scrollPane.getViewport().setBackground(new Color(15, 23, 42));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1));

        // Bottom Explanation & Control Toolbar
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);

        // Formula & Explanation Box
        JPanel explanationCard = new JPanel(new BorderLayout(6, 6));
        explanationCard.setBackground(new Color(30, 41, 59));
        explanationCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        formulaLabel = new JLabel("Recurrence: D[i][j] = min( D[i][j], D[i][k] + D[k][j] )");
        formulaLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        formulaLabel.setForeground(new Color(250, 204, 21));

        explanationArea = new JTextArea(3, 40);
        explanationArea.setEditable(false);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        explanationArea.setBackground(new Color(15, 23, 42));
        explanationArea.setForeground(new Color(226, 232, 240));
        explanationArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(71, 85, 105), 1),
                new EmptyBorder(6, 8, 6, 8)
        ));

        explanationCard.add(formulaLabel, BorderLayout.NORTH);
        explanationCard.add(explanationArea, BorderLayout.CENTER);

        // Playback Control Bar
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        controls.setOpaque(false);

        JButton prevBtn = createStyledButton("⏮ Previous Step", new Color(51, 65, 85));
        prevBtn.addActionListener(e -> stepPrevious());

        playPauseBtn = createStyledButton("▶ Play Steps", new Color(16, 185, 129));
        playPauseBtn.addActionListener(e -> togglePlayPause());

        JButton nextBtn = createStyledButton("Next Step ⏭", new Color(51, 65, 85));
        nextBtn.addActionListener(e -> stepNext());

        JButton resetBtn = createStyledButton("↺ Reset to Start", new Color(71, 85, 105));
        resetBtn.addActionListener(e -> jumpToStep(0));

        JButton jumpEndBtn = createStyledButton("⏩ Jump to End", new Color(71, 85, 105));
        jumpEndBtn.addActionListener(e -> jumpToStep(steps.size() - 1));

        stepSlider = new JSlider(0, Math.max(0, steps.size() - 1), 0);
        stepSlider.setOpaque(false);
        stepSlider.setPreferredSize(new Dimension(220, 24));
        stepSlider.addChangeListener(e -> {
            if (stepSlider.getValueIsAdjusting() || isPlaying) return;
            jumpToStep(stepSlider.getValue());
        });

        JLabel speedLabel = new JLabel("Speed:");
        speedLabel.setForeground(new Color(203, 213, 225));
        speedSlider = new JSlider(50, 1000, 300);
        speedSlider.setOpaque(false);
        speedSlider.setPreferredSize(new Dimension(100, 24));
        speedSlider.addChangeListener(e -> {
            if (playbackTimer != null) playbackTimer.setDelay(speedSlider.getValue());
        });

        controls.add(resetBtn);
        controls.add(prevBtn);
        controls.add(playPauseBtn);
        controls.add(nextBtn);
        controls.add(jumpEndBtn);
        controls.add(new JLabel("Step:"));
        controls.add(stepSlider);
        controls.add(speedLabel);
        controls.add(speedSlider);

        bottomPanel.add(explanationCard, BorderLayout.NORTH);
        bottomPanel.add(controls, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        playbackTimer = new Timer(speedSlider.getValue(), e -> {
            if (currentStepIndex < steps.size() - 1) {
                currentStepIndex++;
                stepSlider.setValue(currentStepIndex);
                updateDisplay();
            } else {
                togglePlayPause();
            }
        });
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                new EmptyBorder(6, 12, 6, 12)
        ));
        return btn;
    }

    private void createMatrixTable() {
        String[] locations = graph.getLocations();
        String[] colNames = new String[locations.length + 1];
        colNames[0] = "From \\ To";
        System.arraycopy(locations, 0, colNames, 1, locations.length);

        tableModel = new DefaultTableModel(colNames, locations.length) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        matrixTable = new JTable(tableModel);
        matrixTable.setRowHeight(32);
        matrixTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        matrixTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        matrixTable.getTableHeader().setBackground(new Color(30, 41, 59));
        matrixTable.getTableHeader().setForeground(new Color(241, 245, 249));

        matrixTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("SansSerif", Font.PLAIN, 12));

                if (column == 0) {
                    setBackground(new Color(30, 41, 59));
                    setForeground(new Color(56, 189, 248));
                    setFont(new Font("SansSerif", Font.BOLD, 12));
                    return c;
                }

                int jCol = column - 1;
                FloydWarshall.StepSnapshot snap = getCurrentSnapshot();

                if (snap != null && snap.k != -1) {
                    int k = snap.k;
                    int i = snap.i;
                    int j = snap.j;

                    if (row == i && jCol == j) {
                        // Current evaluated cell D[i][j]
                        if (snap.updated) {
                            setBackground(new Color(16, 185, 129)); // Bright Emerald Green (Updated!)
                            setForeground(Color.WHITE);
                            setFont(new Font("SansSerif", Font.BOLD, 13));
                        } else {
                            setBackground(new Color(245, 158, 11)); // Amber (Evaluating)
                            setForeground(Color.BLACK);
                            setFont(new Font("SansSerif", Font.BOLD, 13));
                        }
                    } else if (row == i && jCol == k) {
                        // Cell D[i][k]
                        setBackground(new Color(59, 130, 246)); // Blue
                        setForeground(Color.WHITE);
                        setFont(new Font("SansSerif", Font.BOLD, 12));
                    } else if (row == k && jCol == j) {
                        // Cell D[k][j]
                        setBackground(new Color(6, 182, 212)); // Cyan
                        setForeground(Color.BLACK);
                        setFont(new Font("SansSerif", Font.BOLD, 12));
                    } else if (row == k || jCol == k) {
                        // Pivot Row/Col k
                        setBackground(new Color(30, 58, 138, 100));
                        setForeground(new Color(191, 219, 254));
                    } else {
                        setBackground(new Color(15, 23, 42));
                        setForeground(new Color(226, 232, 240));
                    }
                } else {
                    setBackground(new Color(15, 23, 42));
                    setForeground(new Color(226, 232, 240));
                }

                return c;
            }
        });
    }

    private FloydWarshall.StepSnapshot getCurrentSnapshot() {
        if (steps == null || steps.isEmpty() || currentStepIndex < 0 || currentStepIndex >= steps.size()) {
            return null;
        }
        return steps.get(currentStepIndex);
    }

    private void updateDisplay() {
        FloydWarshall.StepSnapshot snap = getCurrentSnapshot();
        if (snap == null || tableModel == null) return;

        String[] locations = graph.getLocations();
        int n = locations.length;
        int[][] mat = snap.distanceMatrix;

        for (int i = 0; i < n; i++) {
            tableModel.setValueAt(locations[i], i, 0);
            for (int j = 0; j < n; j++) {
                String val = (mat[i][j] == CityGraph.INF) ? "∞" : mat[i][j] + " km";
                tableModel.setValueAt(val, i, j + 1);
            }
        }

        stepInfoLabel.setText(String.format("Step %d / %d (Pivot Intermediate Node k=%s)",
                currentStepIndex, steps.size() - 1,
                (snap.k != -1 ? locations[snap.k] : "None")));

        if (snap.k != -1) {
            String ikVal = snap.distIK == CityGraph.INF ? "∞" : snap.distIK + " km";
            String kjVal = snap.distKJ == CityGraph.INF ? "∞" : snap.distKJ + " km";
            String currVal = snap.oldDist == CityGraph.INF ? "∞" : snap.oldDist + " km";
            formulaLabel.setText(String.format("D[%s][%s] = min( D[%s][%s] (%s), D[%s][%s] (%s) + D[%s][%s] (%s) )",
                    locations[snap.i], locations[snap.j],
                    locations[snap.i], locations[snap.j], currVal,
                    locations[snap.i], locations[snap.k], ikVal,
                    locations[snap.k], locations[snap.j], kjVal));
        } else {
            formulaLabel.setText("Recurrence: D[i][j] = min( D[i][j], D[i][k] + D[k][j] )");
        }

        explanationArea.setText(snap.explanation);
        matrixTable.repaint();
    }

    private void togglePlayPause() {
        if (isPlaying) {
            playbackTimer.stop();
            isPlaying = false;
            playPauseBtn.setText("▶ Play Steps");
            playPauseBtn.setBackground(new Color(16, 185, 129));
        } else {
            if (currentStepIndex >= steps.size() - 1) {
                currentStepIndex = 0;
            }
            playbackTimer.start();
            isPlaying = true;
            playPauseBtn.setText("⏸ Pause");
            playPauseBtn.setBackground(new Color(239, 68, 68));
        }
    }

    private void stepNext() {
        if (currentStepIndex < steps.size() - 1) {
            currentStepIndex++;
            stepSlider.setValue(currentStepIndex);
            updateDisplay();
        }
    }

    private void stepPrevious() {
        if (currentStepIndex > 0) {
            currentStepIndex--;
            stepSlider.setValue(currentStepIndex);
            updateDisplay();
        }
    }

    private void jumpToStep(int step) {
        currentStepIndex = Math.max(0, Math.min(steps.size() - 1, step));
        stepSlider.setValue(currentStepIndex);
        updateDisplay();
    }
}
