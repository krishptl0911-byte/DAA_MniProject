import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class CaptureScreenshots {
    public static void main(String[] args) throws Exception {
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        File outDir = new File("C:/Users/intel/.gemini/antigravity/brain/01cdbe71-51c0-4798-b5a5-1510556e383a");
        if (!outDir.exists()) outDir.mkdirs();

        RoutePlannerUI ui = new RoutePlannerUI();
        ui.setVisible(true);
        Thread.sleep(500);

        JTabbedPane tabbedPane = findTabbedPane(ui);

        // 1. Capture Map & Dispatch (Tab 0)
        if (tabbedPane != null) tabbedPane.setSelectedIndex(0);
        ui.repaint();
        Thread.sleep(300);
        captureWindow(ui, new File(outDir, "tab1_map_dispatch.png"));

        // 2. Capture Step Visualizer (Tab 1)
        if (tabbedPane != null) tabbedPane.setSelectedIndex(1);
        ui.repaint();
        Thread.sleep(300);
        captureWindow(ui, new File(outDir, "tab2_dp_visualizer.png"));

        // 3. Capture Matrices (Tab 2)
        if (tabbedPane != null) tabbedPane.setSelectedIndex(2);
        ui.repaint();
        Thread.sleep(300);
        captureWindow(ui, new File(outDir, "tab3_matrices.png"));

        // 4. Capture Analytics (Tab 3)
        if (tabbedPane != null) tabbedPane.setSelectedIndex(3);
        ui.repaint();
        Thread.sleep(300);
        captureWindow(ui, new File(outDir, "tab4_analytics.png"));

        System.out.println("Screenshots captured successfully!");
        ui.dispose();
        System.exit(0);
    }

    private static JTabbedPane findTabbedPane(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JTabbedPane) return (JTabbedPane) c;
            if (c instanceof Container) {
                JTabbedPane found = findTabbedPane((Container) c);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static void captureWindow(JFrame frame, File file) {
        Container content = frame.getContentPane();
        BufferedImage img = new BufferedImage(content.getWidth(), content.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        content.printAll(g2);
        g2.dispose();
        try {
            ImageIO.write(img, "png", file);
            System.out.println("Saved: " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
