import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

public class CaptureScreenshots {
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        File outDir = new File("C:/Users/intel/.gemini/antigravity/brain/9c43c21a-2809-44de-bf07-59f7f83d9bf7");
        if (!outDir.exists()) outDir.mkdirs();

        RoutePlannerUI ui = new RoutePlannerUI();
        ui.setSize(1240, 780);
        ui.doLayout();
        ui.validate();

        // Find TabbedPane
        JTabbedPane tabbedPane = findTabbedPane(ui);

        // 1. Capture Map & Dispatch (Tab 0)
        if (tabbedPane != null) tabbedPane.setSelectedIndex(0);
        renderAndSave(ui, new File(outDir, "tab1_map_dispatch.png"));

        // 2. Capture Step Visualizer (Tab 1)
        if (tabbedPane != null) tabbedPane.setSelectedIndex(1);
        renderAndSave(ui, new File(outDir, "tab2_dp_visualizer.png"));

        // 3. Capture Matrices (Tab 2)
        if (tabbedPane != null) tabbedPane.setSelectedIndex(2);
        renderAndSave(ui, new File(outDir, "tab3_matrices.png"));

        // 4. Capture Analytics (Tab 3)
        if (tabbedPane != null) tabbedPane.setSelectedIndex(3);
        renderAndSave(ui, new File(outDir, "tab4_analytics.png"));

        System.out.println("Screenshots captured successfully!");
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

    private static void renderAndSave(Component comp, File file) {
        comp.doLayout();
        comp.validate();
        BufferedImage img = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        comp.paint(g2);
        g2.dispose();
        try {
            ImageIO.write(img, "png", file);
            System.out.println("Saved: " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
