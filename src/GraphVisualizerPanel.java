import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * High-performance 2D Interactive Canvas for City Road Network Visualization.
 * Features glowing shortest path overlays, animated emergency vehicles,
 * siren strobe effects, interactive road blockage toggles, drag-and-drop node placement,
 * and Apple San Francisco typography.
 */
public class GraphVisualizerPanel extends JPanel {
    private final CityGraph graph;
    private int selectedStart = 0;
    private int selectedEnd = 5;
    private List<Integer> activePath = new ArrayList<>();
    private final List<EmergencyVehicle> activeVehicles = new ArrayList<>();

    private int draggedNodeIndex = -1;
    private int hoveredNodeIndex = -1;
    private int[] hoveredEdge = null;
    private Point mousePos = new Point(0, 0);

    private float pulsePhase = 0f;
    private Timer animationTimer;
    private double speedMultiplier = 1.0;

    // Callback for route changes
    public interface RouteChangeListener {
        void onSelectionChanged(int start, int end);
        void onGraphStructureChanged();
    }
    private RouteChangeListener listener;

    public GraphVisualizerPanel(CityGraph graph) {
        this.graph = graph;
        setBackground(UITheme.BG_DARK_ROOT); // Slate Navy Dark Theme
        setDoubleBuffered(true);

        setupMouseInteractivity();
        setupAnimationLoop();
    }

    public void setRouteChangeListener(RouteChangeListener listener) {
        this.listener = listener;
    }

    public void setSelection(int start, int end, List<Integer> path) {
        this.selectedStart = start;
        this.selectedEnd = end;
        this.activePath = path != null ? new ArrayList<>(path) : new ArrayList<>();
        repaint();
    }

    public void setActivePath(List<Integer> path) {
        this.activePath = path != null ? new ArrayList<>(path) : new ArrayList<>();
        repaint();
    }

    public void addVehicle(EmergencyVehicle vehicle) {
        activeVehicles.clear(); // Support current active or multi-vehicle
        activeVehicles.add(vehicle);
    }

    public void setSpeedMultiplier(double speed) {
        this.speedMultiplier = speed;
    }

    public void clearVehicles() {
        activeVehicles.clear();
        repaint();
    }

    private void setupAnimationLoop() {
        animationTimer = new Timer(16, e -> { // ~60 FPS
            pulsePhase += 0.05f;
            if (pulsePhase > Math.PI * 2) pulsePhase = 0f;

            boolean needsRepaint = false;
            for (EmergencyVehicle v : activeVehicles) {
                if (v.getStatus() == EmergencyVehicle.Status.EN_ROUTE) {
                    v.update(speedMultiplier);
                    needsRepaint = true;
                }
            }
            if (needsRepaint || !activePath.isEmpty() || !activeVehicles.isEmpty()) {
                repaint();
            }
        });
        animationTimer.start();
    }

    private void setupMouseInteractivity() {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int clickedNode = graph.findNearestNode(e.getX(), e.getY(), 26);
                if (clickedNode != -1) {
                    draggedNodeIndex = clickedNode;
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        if (e.isControlDown() || e.isShiftDown()) {
                            selectedEnd = clickedNode;
                        } else {
                            if (selectedStart != clickedNode && selectedEnd == clickedNode) {
                                // Swap
                                selectedEnd = selectedStart;
                                selectedStart = clickedNode;
                            } else if (selectedStart == clickedNode) {
                                // Keep
                            } else {
                                selectedStart = clickedNode;
                            }
                        }
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        selectedEnd = clickedNode;
                    }
                    if (listener != null) listener.onSelectionChanged(selectedStart, selectedEnd);
                    repaint();
                    return;
                }

                // Check edge click (Toggle road block)
                int[] edge = graph.findNearestEdge(e.getX(), e.getY(), 12);
                if (edge != null) {
                    graph.toggleRoadBlock(edge[0], edge[1]);
                    if (listener != null) listener.onGraphStructureChanged();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedNodeIndex = -1;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedNodeIndex != -1) {
                    int margin = 35;
                    int x = Math.max(margin, Math.min(getWidth() - margin, e.getX()));
                    int y = Math.max(margin, Math.min(getHeight() - margin, e.getY()));
                    graph.setNodePosition(draggedNodeIndex, x, y);
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mousePos = e.getPoint();
                int oldNode = hoveredNodeIndex;
                hoveredNodeIndex = graph.findNearestNode(e.getX(), e.getY(), 26);
                hoveredEdge = (hoveredNodeIndex == -1) ? graph.findNearestEdge(e.getX(), e.getY(), 12) : null;

                if (hoveredNodeIndex != -1 || hoveredEdge != null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }

                if (oldNode != hoveredNodeIndex || hoveredEdge != null) {
                    repaint();
                }
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();

        // 1. Draw subtle background grid
        drawBackgroundGrid(g2, w, h);

        // 2. Draw all road edges
        drawRoadEdges(g2);

        // 3. Draw active shortest path highlight
        drawActivePathGlow(g2);

        // 4. Draw nodes (locations)
        drawCityNodes(g2);

        // 5. Draw active vehicles
        drawVehicles(g2);

        // 6. Draw HUD overlays and canvas hints
        drawOverlayHUD(g2, w, h);

        g2.dispose();
    }

    private void drawBackgroundGrid(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(30, 41, 59, 120));
        int gridSpacing = 40;
        for (int x = 0; x < w; x += gridSpacing) {
            for (int y = 0; y < h; y += gridSpacing) {
                g2.fillRect(x - 1, y - 1, 2, 2);
            }
        }
    }

    private void drawRoadEdges(Graphics2D g2) {
        List<CityGraph.CityNode> nodes = graph.getNodes();
        int n = nodes.size();
        int[][] baseRoads = graph.getBaseRoads();

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (baseRoads[i][j] == CityGraph.INF) continue;

                CityGraph.CityNode u = nodes.get(i);
                CityGraph.CityNode v = nodes.get(j);
                boolean isBlocked = graph.isRoadBlocked(i, j);
                boolean isHovered = (hoveredEdge != null &&
                        ((hoveredEdge[0] == i && hoveredEdge[1] == j) || (hoveredEdge[0] == j && hoveredEdge[1] == i)));

                // Road outer border / bed
                g2.setStroke(new BasicStroke(isHovered ? 7f : 5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (isBlocked) {
                    g2.setColor(new Color(185, 28, 28, 160)); // Crimson Red for blocked
                } else {
                    g2.setColor(new Color(51, 65, 85, 200)); // Dark Steel
                }
                g2.drawLine(u.x, u.y, v.x, v.y);

                // Road center line
                if (isBlocked) {
                    // Dashed hazard line
                    Stroke dashed = new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                            0, new float[]{6, 6}, pulsePhase * 5);
                    g2.setStroke(dashed);
                    g2.setColor(new Color(239, 68, 68));
                    g2.drawLine(u.x, u.y, v.x, v.y);
                } else {
                    g2.setStroke(new BasicStroke(isHovered ? 2.5f : 1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.setColor(isHovered ? new Color(148, 163, 184) : new Color(71, 85, 105));
                    g2.drawLine(u.x, u.y, v.x, v.y);
                }

                // Distance pill badge in midpoint
                int midX = (u.x + v.x) / 2;
                int midY = (u.y + v.y) / 2;
                drawDistanceBadge(g2, midX, midY, baseRoads[i][j], isBlocked, isHovered);
            }
        }
    }

    private void drawDistanceBadge(Graphics2D g2, int x, int y, int distance, boolean isBlocked, boolean isHovered) {
        String text = isBlocked ? "BLOCKED" : (distance + " km");
        g2.setFont(UITheme.fontBold(10f));
        FontMetrics fm = g2.getFontMetrics();
        int strW = fm.stringWidth(text);
        int strH = fm.getHeight();

        int pillW = strW + 12;
        int pillH = strH + 4;
        int pillX = x - pillW / 2;
        int pillY = y - pillH / 2;

        // Badge background
        g2.setColor(isBlocked ? new Color(127, 29, 29, 230) :
                   (isHovered ? new Color(30, 58, 138, 230) : new Color(15, 23, 42, 220)));
        g2.fillRoundRect(pillX, pillY, pillW, pillH, 10, 10);

        // Badge border
        g2.setColor(isBlocked ? new Color(239, 68, 68) :
                   (isHovered ? new Color(96, 165, 250) : new Color(71, 85, 105)));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(pillX, pillY, pillW, pillH, 10, 10);

        // Text
        g2.setColor(isBlocked ? new Color(254, 202, 202) : (isHovered ? Color.WHITE : new Color(203, 213, 225)));
        g2.drawString(text, pillX + 6, pillY + fm.getAscent() + 2);
    }

    private void drawActivePathGlow(Graphics2D g2) {
        if (activePath == null || activePath.size() < 2) return;

        List<CityGraph.CityNode> nodes = graph.getNodes();

        // Multi-layer neon glowing pulse
        float glowAlpha = 0.4f + 0.25f * (float) Math.sin(pulsePhase * 2);

        // Outer glow layer
        g2.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(6, 182, 212, (int) (glowAlpha * 90)));
        for (int i = 0; i < activePath.size() - 1; i++) {
            CityGraph.CityNode u = nodes.get(activePath.get(i));
            CityGraph.CityNode v = nodes.get(activePath.get(i + 1));
            g2.drawLine(u.x, u.y, v.x, v.y);
        }

        // Mid glow layer
        g2.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(56, 189, 248, (int) (glowAlpha * 180)));
        for (int i = 0; i < activePath.size() - 1; i++) {
            CityGraph.CityNode u = nodes.get(activePath.get(i));
            CityGraph.CityNode v = nodes.get(activePath.get(i + 1));
            g2.drawLine(u.x, u.y, v.x, v.y);
        }

        // Inner sharp neon core with traveling pulses
        g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(240, 253, 250));
        for (int i = 0; i < activePath.size() - 1; i++) {
            CityGraph.CityNode u = nodes.get(activePath.get(i));
            CityGraph.CityNode v = nodes.get(activePath.get(i + 1));
            g2.drawLine(u.x, u.y, v.x, v.y);
        }
    }

    private void drawCityNodes(Graphics2D g2) {
        List<CityGraph.CityNode> nodes = graph.getNodes();

        for (int i = 0; i < nodes.size(); i++) {
            CityGraph.CityNode node = nodes.get(i);
            boolean isStart = (i == selectedStart);
            boolean isEnd = (i == selectedEnd);
            boolean isPathNode = activePath.contains(i);
            boolean isHovered = (i == hoveredNodeIndex);

            int radius = 22;

            // Halo for Start (Emerald green pulse) or Destination (Ruby red pulse)
            if (isStart) {
                float pulse = 8f + 4f * (float) Math.sin(pulsePhase * 3);
                g2.setColor(new Color(16, 185, 129, 90));
                g2.fillOval((int) (node.x - radius - pulse), (int) (node.y - radius - pulse),
                           (int) ((radius + pulse) * 2), (int) ((radius + pulse) * 2));
            } else if (isEnd) {
                float pulse = 8f + 4f * (float) Math.sin(pulsePhase * 3);
                g2.setColor(new Color(239, 68, 68, 90));
                g2.fillOval((int) (node.x - radius - pulse), (int) (node.y - radius - pulse),
                           (int) ((radius + pulse) * 2), (int) ((radius + pulse) * 2));
            } else if (isPathNode) {
                g2.setColor(new Color(6, 182, 212, 60));
                g2.fillOval(node.x - radius - 5, node.y - radius - 5, (radius + 5) * 2, (radius + 5) * 2);
            }

            // Node Circle Body
            Color baseColor = new Color(node.type.colorHex);
            GradientPaint gp = new GradientPaint(
                    node.x - radius, node.y - radius, isHovered ? baseColor.brighter() : baseColor,
                    node.x + radius, node.y + radius, baseColor.darker());
            g2.setPaint(gp);
            g2.fillOval(node.x - radius, node.y - radius, radius * 2, radius * 2);

            // Node Outer Ring
            if (isStart) {
                g2.setColor(new Color(52, 211, 153));
                g2.setStroke(new BasicStroke(3.5f));
            } else if (isEnd) {
                g2.setColor(new Color(248, 113, 113));
                g2.setStroke(new BasicStroke(3.5f));
            } else if (isPathNode) {
                g2.setColor(new Color(103, 232, 249));
                g2.setStroke(new BasicStroke(2.5f));
            } else {
                g2.setColor(new Color(255, 255, 255, 180));
                g2.setStroke(new BasicStroke(1.8f));
            }
            g2.drawOval(node.x - radius, node.y - radius, radius * 2, radius * 2);

            // Center Badge Icon / Short Label
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
            FontMetrics iconFm = g2.getFontMetrics();
            int iconW = iconFm.stringWidth(node.type.icon);
            g2.drawString(node.type.icon, node.x - iconW / 2, node.y + iconFm.getAscent() / 2 - 2);

            // Label Card below node
            drawNodeLabel(g2, node, i, isStart, isEnd);
        }
    }

    private void drawNodeLabel(Graphics2D g2, CityGraph.CityNode node, int index, boolean isStart, boolean isEnd) {
        String tag = isStart ? " [START]" : (isEnd ? " [DEST]" : "");
        String text = node.name + tag;
        g2.setFont(isStart || isEnd ? UITheme.fontBold(11f) : UITheme.font(11f));
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(text);
        int textH = fm.getHeight();

        int pillW = textW + 14;
        int pillH = textH + 4;
        int pillX = node.x - pillW / 2;
        int pillY = node.y + 26;

        // Label Background Pill
        g2.setColor(isStart ? new Color(6, 78, 59, 230) :
                   (isEnd ? new Color(127, 29, 29, 230) : new Color(15, 23, 42, 210)));
        g2.fillRoundRect(pillX, pillY, pillW, pillH, 8, 8);

        // Label Border
        g2.setColor(isStart ? new Color(52, 211, 153) :
                   (isEnd ? new Color(248, 113, 113) : new Color(71, 85, 105)));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(pillX, pillY, pillW, pillH, 8, 8);

        // Label Text
        g2.setColor(isStart ? new Color(209, 250, 229) :
                   (isEnd ? new Color(254, 226, 226) : new Color(241, 245, 249)));
        g2.drawString(text, pillX + 7, pillY + fm.getAscent() + 2);
    }

    private void drawVehicles(Graphics2D g2) {
        for (EmergencyVehicle v : activeVehicles) {
            if (v.getStatus() == EmergencyVehicle.Status.IDLE) continue;

            double vx = v.getCurrentX();
            double vy = v.getCurrentY();
            double angle = v.getHeadingAngle();

            AffineTransform oldTransform = g2.getTransform();
            g2.translate(vx, vy);
            g2.rotate(angle);

            // Vehicle Body
            int length = 28;
            int width = 16;
            g2.setColor(new Color(15, 23, 42)); // Shadow
            g2.fillRoundRect(-length / 2 + 2, -width / 2 + 2, length, width, 6, 6);

            g2.setColor(v.getType().themeColor);
            g2.fillRoundRect(-length / 2, -width / 2, length, width, 6, 6);

            // Vehicle Windshield
            g2.setColor(new Color(224, 242, 254));
            g2.fillRect(length / 2 - 10, -width / 2 + 3, 5, width - 6);

            // Vehicle Roof Stripe
            g2.setColor(Color.WHITE);
            g2.fillRect(-length / 2 + 4, -width / 2 + 6, length - 12, 4);

            // Siren Emergency Flashing Lights (Red / Blue)
            if (v.getStatus() == EmergencyVehicle.Status.EN_ROUTE) {
                boolean flash = v.isSirenFlash();
                Color c1 = flash ? new Color(239, 68, 68) : new Color(37, 99, 235);
                Color c2 = flash ? new Color(37, 99, 235) : new Color(239, 68, 68);

                g2.setColor(c1);
                g2.fillOval(-2, -5, 5, 4);
                g2.setColor(c2);
                g2.fillOval(-2, 1, 5, 4);

                // Siren Light Cones
                g2.setColor(flash ? new Color(239, 68, 68, 90) : new Color(37, 99, 235, 90));
                g2.fillArc(-25, -25, 50, 50, 45, 90);
                g2.setColor(flash ? new Color(37, 99, 235, 90) : new Color(239, 68, 68, 90));
                g2.fillArc(-25, -25, 50, 50, 225, 90);
            }

            g2.setTransform(oldTransform);

            // Floating status badge above vehicle
            String statusText = v.getStatus() == EmergencyVehicle.Status.ON_SCENE
                    ? "ARRIVED ON SCENE"
                    : String.format("%s (%.0f km/h)", v.getType().name, v.getType().avgSpeedKmh);
            g2.setFont(UITheme.fontBold(10f));
            FontMetrics vfm = g2.getFontMetrics();
            int vtw = vfm.stringWidth(statusText);
            g2.setColor(new Color(15, 23, 42, 210));
            g2.fillRoundRect((int) vx - vtw / 2 - 5, (int) vy - 26, vtw + 10, 16, 6, 6);
            g2.setColor(v.getStatus() == EmergencyVehicle.Status.ON_SCENE ? new Color(52, 211, 153) : new Color(250, 204, 21));
            g2.drawString(statusText, (int) vx - vtw / 2, (int) vy - 14);
        }
    }

    private void drawOverlayHUD(Graphics2D g2, int w, int h) {
        // Top Left Status Badge
        g2.setColor(new Color(15, 23, 42, 220));
        g2.fillRoundRect(12, 12, 270, 44, 10, 10);
        g2.setColor(UITheme.BORDER_DARK);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(12, 12, 270, 44, 10, 10);

        // Green live indicator dot
        g2.setColor(new Color(16, 185, 129));
        g2.fillOval(24, 28, 10, 10);

        g2.setFont(UITheme.fontBold(12f));
        g2.setColor(new Color(241, 245, 249));
        g2.drawString("LIVE 911 CITY GRID SIMULATION", 42, 28);
        g2.setFont(UITheme.font(10f));
        g2.setColor(UITheme.TEXT_SECONDARY);
        g2.drawString("Floyd–Warshall O(V³) Real-Time Matrix Active", 42, 45);

        // Bottom Left Interactive Controls Guide
        g2.setColor(new Color(15, 23, 42, 220));
        g2.fillRoundRect(12, h - 38, 590, 26, 8, 8);
        g2.setColor(UITheme.BORDER_DARK);
        g2.drawRoundRect(12, h - 38, 590, 26, 8, 8);

        g2.setFont(UITheme.font(11f));
        g2.setColor(new Color(203, 213, 225));
        g2.drawString("Left-Click: Start  |  Right-Click: Dest  |  Click Road: Block/Unblock  |  Drag: Move node", 20, h - 21);
    }
}
