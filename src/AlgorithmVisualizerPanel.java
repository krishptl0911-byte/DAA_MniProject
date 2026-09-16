import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 * Step-by-Step Floyd-Warshall DP Matrix Transformation Inspector.
 * Demonstrates the DAA concept with Apple typography, high-contrast playback controls,
 * cell highlighting, and live recurrence formula evaluation across the 18-node metropolitan network.
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
    private UITheme.AppleButton playPauseBtn;
    private Timer playbackTimer;
    private boolean isPlaying = false;

    public AlgorithmVisualizerPanel(CityGraph graph) {
        this.graph = graph;
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BG_DARK_ROOT);
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

        JLabel header = new JLabel("Floyd–Warshall Dynamic Programming Visualizer & Step Inspector");
        header.setFont(UITheme.fontBold(17f));
        header.setForeground(new Color(56, 189, 248));

        stepInfoLabel = new JLabel("Step 0 / " + (steps.size() - 1));
        stepInfoLabel.setFont(UITheme.fontBold(13f));
        stepInfoLabel.setForeground(new Color(241, 245, 249));

        topPanel.add(header, BorderLayout.WEST);
        topPanel.add(stepInfoLabel, BorderLayout.EAST);

        // Center Matrix Table
        createMatrixTable();
        JScrollPane scrollPane = new JScrollPane(matrixTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(UITheme.BG_DARK_ROOT);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1));

        // Bottom Explanation & Control Toolbar
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);

        // Formula & Explanation Box
        JPanel explanationCard = new JPanel(new BorderLayout(6, 6));
        explanationCard.setBackground(UITheme.BG_DARK_CARD);
        explanationCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        formulaLabel = new JLabel("Recurrence: D[i][j] = min( D[i][j], D[i][k] + D[k][j] )");
        formulaLabel.setFont(UITheme.fontMono(Font.BOLD, 13f));
        formulaLabel.setForeground(new Color(250, 204, 21));

        explanationArea = new JTextArea(3, 40);
        explanationArea.setEditable(false);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setFont(UITheme.font(12f));
        explanationArea.setBackground(UITheme.BG_DARK_ROOT);
        explanationArea.setForeground(new Color(226, 232, 240));
        explanationArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(71, 85, 105), 1),
                new EmptyBorder(6, 8, 6, 8)
        ));

        explanationCard.add(formulaLabel, BorderLayout.NORTH);
        explanationCard.add(explanationArea, BorderLayout.CENTER);

        // Playback Control Bar with Apple-style high-contrast buttons
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        controls.setOpaque(false);

        UITheme.AppleButton resetBtn = UITheme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> jumpToStep(0));

        UITheme.AppleButton prevBtn = UITheme.createSecondaryButton("Prev Step");
        prevBtn.addActionListener(e -> stepPrevious());

        playPauseBtn = UITheme.createSuccessButton("Play Steps");
        playPauseBtn.addActionListener(e -> togglePlayPause());

        UITheme.AppleButton nextBtn = UITheme.createSecondaryButton("Next Step");
        nextBtn.addActionListener(e -> stepNext());

        UITheme.AppleButton jumpEndBtn = UITheme.createSecondaryButton("Jump End");
        jumpEndBtn.addActionListener(e -> jumpToStep(steps.size() - 1));

        stepSlider = new JSlider(0, Math.max(0, steps.size() - 1), 0);
        stepSlider.setOpaque(false);
        stepSlider.setPreferredSize(new Dimension(200, 24));
        stepSlider.addChangeListener(e -> {
            if (stepSlider.getValueIsAdjusting() || isPlaying) return;
            jumpToStep(stepSlider.getValue());
        });

        JLabel stepLbl = new JLabel("Step:");
        stepLbl.setFont(UITheme.fontBold(11f));
        stepLbl.setForeground(new Color(203, 213, 225));

        JLabel speedLabel = new JLabel("Speed:");
        speedLabel.setFont(UITheme.fontBold(11f));
        speedLabel.setForeground(new Color(203, 213, 225));
        speedSlider = new JSlider(10, 500, 100);
        speedSlider.setOpaque(false);
        speedSlider.setPreferredSize(new Dimension(90, 24));
        speedSlider.addChangeListener(e -> {
            if (playbackTimer != null) playbackTimer.setDelay(speedSlider.getValue());
        });

        controls.add(resetBtn);
        controls.add(prevBtn);
        controls.add(playPauseBtn);
        controls.add(nextBtn);
        controls.add(jumpEndBtn);
        controls.add(stepLbl);
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

    private void createMatrixTable() {
        String[] locations = graph.getLocations();
        String[] colNames = new String[locations.length + 1];
        colNames[0] = "From \\ To";
        System.arraycopy(locations, 0, colNames, 1, locations.length);

        tableModel = new DefaultTableModel(colNames, locations.length) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        matrixTable = new JTable(tableModel);
        matrixTable.setRowHeight(26);
        matrixTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        JTableHeader th = matrixTable.getTableHeader();
        th.setFont(UITheme.fontBold(11f));
        th.setBackground(UITheme.BG_DARK_CARD);
        th.setForeground(new Color(241, 245, 249));
        th.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(new Color(15, 23, 42));
                setForeground(new Color(241, 245, 249));
                setFont(UITheme.fontBold(10.5f));
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, UITheme.BORDER_DARK));
                return c;
            }
        });

        // Configure column widths for horizontal scrolling
        for (int i = 0; i < matrixTable.getColumnCount(); i++) {
            TableColumn col = matrixTable.getColumnModel().getColumn(i);
            if (i == 0) {
                col.setPreferredWidth(160);
                col.setMinWidth(140);
            } else {
                col.setPreferredWidth(85);
                col.setMinWidth(75);
            }
        }

        matrixTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                            boolean isSelected, boolean hasFocus,
                                                            int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(UITheme.font(11f));

                if (column == 0) {
                    setBackground(UITheme.BG_DARK_CARD);
                    setForeground(new Color(56, 189, 248));
                    setFont(UITheme.fontBold(11f));
                    setHorizontalAlignment(SwingConstants.LEFT);
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
                            setFont(UITheme.fontBold(11.5f));
                        } else {
                            setBackground(new Color(245, 158, 11)); // Amber (Evaluating)
                            setForeground(Color.BLACK);
                            setFont(UITheme.fontBold(11.5f));
                        }
                    } else if (row == i && jCol == k) {
                        // Cell D[i][k]
                        setBackground(new Color(59, 130, 246)); // Blue
                        setForeground(Color.WHITE);
                        setFont(UITheme.fontBold(11f));
                    } else if (row == k && jCol == j) {
                        // Cell D[k][j]
                        setBackground(new Color(6, 182, 212)); // Cyan
                        setForeground(Color.BLACK);
                        setFont(UITheme.fontBold(11f));
                    } else if (row == k || jCol == k) {
                        // Pivot Row/Col k
                        setBackground(new Color(30, 58, 138, 100));
                        setForeground(new Color(191, 219, 254));
                    } else {
                        setBackground(UITheme.BG_DARK_ROOT);
                        setForeground(new Color(226, 232, 240));
                    }
                } else {
                    setBackground(UITheme.BG_DARK_ROOT);
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

        stepInfoLabel.setText(String.format("Step %d / %d (Pivot k=%s)",
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
            playPauseBtn.setText("Play Steps");
            playPauseBtn.setColors(new Color(220, 252, 231), new Color(5, 150, 105), new Color(134, 239, 172));
        } else {
            if (currentStepIndex >= steps.size() - 1) {
                currentStepIndex = 0;
            }
            playbackTimer.start();
            isPlaying = true;
            playPauseBtn.setText("Pause");
            playPauseBtn.setColors(new Color(254, 226, 226), new Color(220, 38, 38), new Color(252, 165, 165));
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
