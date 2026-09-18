import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Interactive, map-style city canvas for the dispatch simulation. It deliberately keeps
 * every road selectable so that the route algorithm can react to a live closure.
 */
public class GraphVisualizerPanel extends JPanel {
    private final CityGraph graph;
    private int selectedStart = 0;
    private int selectedEnd = 13;
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
        setBackground(new Color(232, 234, 237));
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
                int clickedNode = graph.findNearestNode(e.getX(), e.getY(), 24);
                if (clickedNode != -1) {
                    draggedNodeIndex = clickedNode;
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        if (e.isControlDown() || e.isShiftDown()) {
                            selectedEnd = clickedNode;
                        } else {
                            if (selectedStart != clickedNode && selectedEnd == clickedNode) {
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
                int[] edge = graph.findNearestEdge(e.getX(), e.getY(), 10);
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
                    int margin = 30;
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
                hoveredNodeIndex = graph.findNearestNode(e.getX(), e.getY(), 24);
                hoveredEdge = (hoveredNodeIndex == -1) ? graph.findNearestEdge(e.getX(), e.getY(), 10) : null;

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

        // 1. Draw a clean street-map baselayer (offline, so no API key is required).
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
        // Google Maps-inspired neutral land, water, parks and secondary streets.
        g2.setColor(new Color(242, 241, 236));
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(198, 225, 237));
        g2.fillRoundRect(w - 115, 0, 115, h, 26, 26);
        g2.setColor(new Color(215, 232, 203));
        g2.fillRoundRect(18, h - 155, 172, 120, 28, 28);
        g2.fillRoundRect(w / 2 - 70, 18, 150, 66, 24, 24);
        g2.setColor(new Color(190, 207, 179));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(18, h - 155, 172, 120, 28, 28);

        g2.setColor(new Color(221, 222, 224));
        g2.setStroke(new BasicStroke(1f));
        for (int x = 20; x < w; x += 46) g2.drawLine(x, 0, x, h);
        for (int y = 18; y < h; y += 38) g2.drawLine(0, y, w, y);

        g2.setFont(UITheme.fontBold(10f));
        g2.setColor(new Color(125, 154, 115));
        g2.drawString("METRO GREENWAY", 38, h - 54);
        g2.setColor(new Color(92, 150, 177));
        g2.drawString("EAST RIVER", w - 97, 28);
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

                // A pale asphalt bed and white carriageway make the network read as a map.
                g2.setStroke(new BasicStroke(isHovered ? 9f : 7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (isBlocked) {
                    g2.setColor(new Color(217, 83, 79));
                } else {
                    g2.setColor(new Color(188, 190, 192));
                }
                g2.drawLine(u.x, u.y, v.x, v.y);

                // Road center line
                if (isBlocked) {
                    // Dashed hazard line
                    Stroke dashed = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                            0, new float[]{5, 5}, pulsePhase * 4);
                    g2.setStroke(dashed);
                    g2.setColor(new Color(255, 241, 118));
                    g2.drawLine(u.x, u.y, v.x, v.y);
                } else {
                    g2.setStroke(new BasicStroke(isHovered ? 4.5f : 3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.setColor(isHovered ? new Color(255, 255, 255) : new Color(250, 250, 250));
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
        String text = isBlocked ? "BLOCKED" : (distance + "k");
        g2.setFont(UITheme.fontBold(9f));
        FontMetrics fm = g2.getFontMetrics();
        int strW = fm.stringWidth(text);
        int strH = fm.getHeight();

        int pillW = strW + 8;
        int pillH = strH + 2;
        int pillX = x - pillW / 2;
        int pillY = y - pillH / 2;

        // Badge background
        g2.setColor(isBlocked ? new Color(198, 40, 40) : Color.WHITE);
        g2.fillRoundRect(pillX, pillY, pillW, pillH, 8, 8);

        // Badge border
        g2.setColor(isBlocked ? new Color(198, 40, 40) :
                   (isHovered ? new Color(66, 133, 244) : new Color(190, 190, 190)));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(pillX, pillY, pillW, pillH, 8, 8);

        // Text
        g2.setColor(isBlocked ? Color.WHITE : (isHovered ? new Color(25, 90, 190) : new Color(80, 80, 80)));
        g2.drawString(text, pillX + 4, pillY + fm.getAscent() + 1);
    }

    private void drawActivePathGlow(Graphics2D g2) {
        if (activePath == null || activePath.size() < 2) return;

        List<CityGraph.CityNode> nodes = graph.getNodes();

        // Google-route inspired blue route with a subtle high-visibility glow.
        float glowAlpha = 0.4f + 0.25f * (float) Math.sin(pulsePhase * 2);

        // Outer glow layer
        g2.setStroke(new BasicStroke(13f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(66, 133, 244, (int) (glowAlpha * 90)));
        for (int i = 0; i < activePath.size() - 1; i++) {
            CityGraph.CityNode u = nodes.get(activePath.get(i));
            CityGraph.CityNode v = nodes.get(activePath.get(i + 1));
            g2.drawLine(u.x, u.y, v.x, v.y);
        }

        // Mid glow layer
        g2.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(66, 133, 244, 210));
        for (int i = 0; i < activePath.size() - 1; i++) {
            CityGraph.CityNode u = nodes.get(activePath.get(i));
            CityGraph.CityNode v = nodes.get(activePath.get(i + 1));
            g2.drawLine(u.x, u.y, v.x, v.y);
        }

        // Inner sharp neon core with traveling pulses
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(217, 232, 255));
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

            int radius = 17;

            // Halo for Start (Emerald green pulse) or Destination (Ruby red pulse)
            if (isStart) {
                float pulse = 6f + 3f * (float) Math.sin(pulsePhase * 3);
                g2.setColor(new Color(16, 185, 129, 90));
                g2.fillOval((int) (node.x - radius - pulse), (int) (node.y - radius - pulse),
                           (int) ((radius + pulse) * 2), (int) ((radius + pulse) * 2));
            } else if (isEnd) {
                float pulse = 6f + 3f * (float) Math.sin(pulsePhase * 3);
                g2.setColor(new Color(239, 68, 68, 90));
                g2.fillOval((int) (node.x - radius - pulse), (int) (node.y - radius - pulse),
                           (int) ((radius + pulse) * 2), (int) ((radius + pulse) * 2));
            } else if (isPathNode) {
                g2.setColor(new Color(6, 182, 212, 60));
                g2.fillOval(node.x - radius - 4, node.y - radius - 4, (radius + 4) * 2, (radius + 4) * 2);
            }

            // Prominent place marker. Colours communicate service type at a glance.
            Color baseColor = new Color(node.type.colorHex);
            GradientPaint gp = new GradientPaint(
                    node.x - radius, node.y - radius, isHovered ? baseColor.brighter() : baseColor,
                    node.x + radius, node.y + radius, baseColor.darker());
            g2.setPaint(gp);
            g2.fillOval(node.x - radius, node.y - radius, radius * 2, radius * 2);

            // Node Outer Ring
            if (isStart) {
                g2.setColor(new Color(52, 211, 153));
                g2.setStroke(new BasicStroke(3f));
            } else if (isEnd) {
                g2.setColor(new Color(248, 113, 113));
                g2.setStroke(new BasicStroke(3f));
            } else if (isPathNode) {
                g2.setColor(new Color(103, 232, 249));
                g2.setStroke(new BasicStroke(2.2f));
            } else {
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
            }
            g2.drawOval(node.x - radius, node.y - radius, radius * 2, radius * 2);

            // Center Badge Icon / Short Label
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
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
        g2.setFont(isStart || isEnd ? UITheme.fontBold(10f) : UITheme.font(9.5f));
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(text);
        int textH = fm.getHeight();

        int pillW = textW + 10;
        int pillH = textH + 2;
        int pillX = node.x - pillW / 2;
        int pillY = node.y + 20;

        // Label Background Pill
        g2.setColor(isStart ? new Color(232, 245, 233, 245) :
                   (isEnd ? new Color(255, 235, 238, 245) : new Color(255, 255, 255, 240)));
        g2.fillRoundRect(pillX, pillY, pillW, pillH, 6, 6);

        // Label Border
        g2.setColor(isStart ? new Color(52, 168, 83) :
                   (isEnd ? new Color(234, 67, 53) : new Color(190, 190, 190)));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(pillX, pillY, pillW, pillH, 6, 6);

        // Label Text
        g2.setColor(isStart ? new Color(19, 100, 52) :
                   (isEnd ? new Color(174, 35, 28) : new Color(45, 45, 45)));
        g2.drawString(text, pillX + 5, pillY + fm.getAscent() + 1);
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
            int length = 24;
            int width = 14;
            g2.setColor(new Color(15, 23, 42)); // Shadow
            g2.fillRoundRect(-length / 2 + 2, -width / 2 + 2, length, width, 5, 5);

            g2.setColor(v.getType().themeColor);
            g2.fillRoundRect(-length / 2, -width / 2, length, width, 5, 5);

            // Vehicle Windshield
            g2.setColor(new Color(224, 242, 254));
            g2.fillRect(length / 2 - 8, -width / 2 + 2, 4, width - 4);

            // Vehicle Roof Stripe
            g2.setColor(Color.WHITE);
            g2.fillRect(-length / 2 + 3, -width / 2 + 5, length - 10, 4);

            // Siren Emergency Flashing Lights (Red / Blue)
            if (v.getStatus() == EmergencyVehicle.Status.EN_ROUTE) {
                boolean flash = v.isSirenFlash();
                Color c1 = flash ? new Color(239, 68, 68) : new Color(37, 99, 235);
                Color c2 = flash ? new Color(37, 99, 235) : new Color(239, 68, 68);

                g2.setColor(c1);
                g2.fillOval(-2, -4, 4, 3);
                g2.setColor(c2);
                g2.fillOval(-2, 1, 4, 3);

                // Siren Light Cones
                g2.setColor(flash ? new Color(239, 68, 68, 90) : new Color(37, 99, 235, 90));
                g2.fillArc(-20, -20, 40, 40, 45, 90);
                g2.setColor(flash ? new Color(37, 99, 235, 90) : new Color(239, 68, 68, 90));
                g2.fillArc(-20, -20, 40, 40, 225, 90);
            }

            g2.setTransform(oldTransform);

            // Floating status badge above vehicle
            String statusText = v.getStatus() == EmergencyVehicle.Status.ON_SCENE
                    ? "ARRIVED ON SCENE"
                    : String.format("%s (%.0f km/h)", v.getType().name, v.getType().avgSpeedKmh);
            g2.setFont(UITheme.fontBold(9.5f));
            FontMetrics vfm = g2.getFontMetrics();
            int vtw = vfm.stringWidth(statusText);
            g2.setColor(new Color(15, 23, 42, 210));
            g2.fillRoundRect((int) vx - vtw / 2 - 4, (int) vy - 24, vtw + 8, 14, 5, 5);
            g2.setColor(v.getStatus() == EmergencyVehicle.Status.ON_SCENE ? new Color(52, 211, 153) : new Color(250, 204, 21));
            g2.drawString(statusText, (int) vx - vtw / 2, (int) vy - 13);
        }
    }

    private void drawOverlayHUD(Graphics2D g2, int w, int h) {
        // Top-left map status card
        g2.setColor(new Color(255, 255, 255, 238));
        g2.fillRoundRect(12, 12, 275, 44, 10, 10);
        g2.setColor(new Color(210, 210, 210));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(12, 12, 275, 44, 10, 10);

        // Green live indicator dot
        g2.setColor(new Color(16, 185, 129));
        g2.fillOval(24, 28, 10, 10);

        g2.setFont(UITheme.fontBold(12f));
        g2.setColor(new Color(32, 33, 36));
        g2.drawString("METRO 911 LIVE MAP", 42, 28);
        g2.setFont(UITheme.font(10f));
        g2.setColor(new Color(95, 99, 104));
        g2.drawString("18 locations • routing network online", 42, 45);

        // Bottom Left Interactive Controls Guide
        g2.setColor(new Color(255, 255, 255, 238));
        g2.fillRoundRect(12, h - 38, 560, 26, 8, 8);
        g2.setColor(new Color(210, 210, 210));
        g2.drawRoundRect(12, h - 38, 560, 26, 8, 8);

        g2.setFont(UITheme.font(11f));
        g2.setColor(new Color(70, 70, 70));
        g2.drawString("Click: start  |  Right-click: destination  |  Click road: closure  |  Drag: reposition", 20, h - 21);
    }
}
