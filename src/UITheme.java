import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Modern Apple-styled Theme & UI Component Engine.
 * Provides Apple San Francisco typography resolution and high-contrast tactile buttons
 * combining light backgrounds with bright/vibrant text and clear click/hover feedback.
 */
public class UITheme {

    // Detected San Francisco / Apple-like font family name
    public static final String SF_FONT_FAMILY;

    static {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Set<String> available = new HashSet<>(Arrays.asList(ge.getAvailableFontFamilyNames()));

        String[] preferred = {
            "SF Pro Display",
            "SF Pro Text",
            "SF Pro",
            "San Francisco",
            ".AppleSystemUIFont",
            "-apple-system",
            "Segoe UI Variable Display",
            "Segoe UI",
            "Helvetica Neue",
            "Arial",
            "SansSerif"
        };

        String chosen = "Segoe UI";
        for (String f : preferred) {
            if (available.contains(f)) {
                chosen = f;
                break;
            }
        }
        SF_FONT_FAMILY = chosen;
    }

    /**
     * Creates an Apple San Francisco font with specified style and point size.
     */
    public static Font font(int style, float size) {
        return new Font(SF_FONT_FAMILY, style, (int) size).deriveFont(style, size);
    }

    public static Font font(float size) {
        return font(Font.PLAIN, size);
    }

    public static Font fontBold(float size) {
        return font(Font.BOLD, size);
    }

    public static Font fontMono(int style, float size) {
        return new Font("Consolas", style, (int) size).deriveFont(style, size);
    }

    // Modern Color Palette (Slate Dark Theme & Apple Light Accent Buttons)
    public static final Color BG_DARK_ROOT = new Color(15, 23, 42);       // Slate 900
    public static final Color BG_DARK_CARD = new Color(30, 41, 59);       // Slate 800
    public static final Color BG_DARK_HEADER = new Color(24, 34, 49);     // Slate 850
    public static final Color BORDER_DARK = new Color(51, 65, 85);        // Slate 700
    public static final Color TEXT_PRIMARY = new Color(248, 250, 252);     // Slate 50
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184);   // Slate 400
    public static final Color TEXT_MUTED = new Color(100, 116, 139);       // Slate 500

    /**
     * High-contrast Apple-style Button with Light Background + Bright/Vibrant Text,
     * rounded capsule/rectangle corners, crisp borders, and distinct click/hover states.
     */
    public static class AppleButton extends JButton {
        private Color normalBg;
        private Color hoverBg;
        private Color pressedBg;
        private Color normalFg;
        private Color borderColor;
        private int cornerRadius = 9;

        public AppleButton(String text, Color normalBg, Color normalFg, Color borderColor) {
            super(text);
            this.normalBg = normalBg;
            this.normalFg = normalFg;
            this.borderColor = borderColor;
            
            // Hover: slightly brighter/richer light background
            this.hoverBg = calculateHoverBg(normalBg);
            // Pressed: high-contrast vivid highlight so click is instantly visible
            this.pressedBg = calculatePressedBg(normalBg, normalFg);

            setFont(UITheme.fontBold(12f));
            setForeground(normalFg);
            setBackground(normalBg);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(7, 14, 7, 14));
        }

        private static Color calculateHoverBg(Color bg) {
            int r = Math.min(255, bg.getRed() + 12);
            int g = Math.min(255, bg.getGreen() + 12);
            int b = Math.min(255, bg.getBlue() + 12);
            return new Color(r, g, b, 255);
        }

        private static Color calculatePressedBg(Color bg, Color fg) {
            // Distinct active click background: blend toward bright text color
            int r = (bg.getRed() * 2 + fg.getRed()) / 3;
            int g = (bg.getGreen() * 2 + fg.getGreen()) / 3;
            int b = (bg.getBlue() * 2 + fg.getBlue()) / 3;
            return new Color(r, g, b, 255);
        }

        public void setColors(Color normalBg, Color normalFg, Color borderColor) {
            this.normalBg = normalBg;
            this.normalFg = normalFg;
            this.borderColor = borderColor;
            this.hoverBg = calculateHoverBg(normalBg);
            this.pressedBg = calculatePressedBg(normalBg, normalFg);
            setForeground(normalFg);
            repaint();
        }

        public void setCornerRadius(int radius) {
            this.cornerRadius = radius;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            ButtonModel model = getModel();
            Color currentBg;
            Color currentFg;
            Color currentBorder = borderColor;

            if (model.isPressed()) {
                currentBg = pressedBg;
                currentFg = (normalFg.equals(Color.WHITE)) ? Color.WHITE : normalFg.darker();
                currentBorder = normalFg;
            } else if (model.isRollover()) {
                currentBg = hoverBg;
                currentFg = normalFg;
                currentBorder = normalFg.brighter();
            } else {
                currentBg = normalBg;
                currentFg = normalFg;
            }

            // Draw Button Outer Drop Shadow / Glow when hovered or pressed
            if (model.isRollover() || model.isPressed()) {
                g2.setColor(new Color(normalFg.getRed(), normalFg.getGreen(), normalFg.getBlue(), model.isPressed() ? 90 : 50));
                g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, cornerRadius + 2, cornerRadius + 2));
            }

            // Draw Button Body (Light Background)
            g2.setColor(currentBg);
            g2.fill(new RoundRectangle2D.Float(2, 2, w - 4, h - 4, cornerRadius, cornerRadius));

            // Draw Button Border
            g2.setColor(currentBorder);
            g2.setStroke(new BasicStroke(model.isPressed() ? 2.0f : (model.isRollover() ? 1.5f : 1.2f)));
            g2.draw(new RoundRectangle2D.Float(2.5f, 2.5f, w - 5f, h - 5f, cornerRadius, cornerRadius));

            // Draw Text (Bright / Vibrant Color)
            g2.setFont(getFont());
            g2.setColor(currentFg);
            FontMetrics fm = g2.getFontMetrics();
            int strW = fm.stringWidth(getText());
            int strH = fm.getAscent() - fm.getDescent();

            int textX = (w - strW) / 2;
            int textY = (h + strH) / 2 + (model.isPressed() ? 1 : 0); // Subtle 1px tactile click depress

            g2.drawString(getText(), textX, textY);
            g2.dispose();
        }
    }

    // Preset High-Contrast Apple Button Styles (Light Background + Bright/Vibrant Text)

    /**
     * Danger / Urgent Action: Light Coral Background (`#FEE2E2`) + Vibrant Ruby Red Text (`#DC2626`)
     */
    public static AppleButton createDangerButton(String text) {
        return new AppleButton(text, new Color(254, 226, 226), new Color(220, 38, 38), new Color(252, 165, 165));
    }

    /**
     * Info / Primary Action: Light Sky Azure Background (`#E0F2FE`) + Vibrant Apple Blue Text (`#0284C7`)
     */
    public static AppleButton createInfoButton(String text) {
        return new AppleButton(text, new Color(224, 242, 254), new Color(2, 132, 199), new Color(186, 230, 253));
    }

    /**
     * Success / Benchmark / Play Action: Light Mint Background (`#DCFCE7`) + Vibrant Emerald Green Text (`#059669`)
     */
    public static AppleButton createSuccessButton(String text) {
        return new AppleButton(text, new Color(220, 252, 231), new Color(5, 150, 105), new Color(134, 239, 172));
    }

    /**
     * Neutral / Navigation / Reset Action: Light Crisp Slate Background (`#F1F5F9`) + Bold Slate Blue Text (`#0F172A`)
     */
    public static AppleButton createSecondaryButton(String text) {
        return new AppleButton(text, new Color(241, 245, 249), new Color(15, 23, 42), new Color(203, 213, 225));
    }

    /**
     * Warning / Yellow Action: Light Amber Background (`#FEF3C7`) + Vibrant Amber Text (`#D97706`)
     */
    public static AppleButton createWarningButton(String text) {
        return new AppleButton(text, new Color(254, 243, 199), new Color(217, 119, 6), new Color(253, 230, 138));
    }
}
