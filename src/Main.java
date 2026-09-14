import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application entry point for the Emergency Vehicle Route Planner.
 * Configures system rendering properties and launches the command center UI.
 */
public class Main {
    public static void main(String[] args) {
        // Enable high-DPI scaling and hardware-accelerated anti-aliased text rendering
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                // Apply modern dark/flat look and feel if available, else system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            RoutePlannerUI ui = new RoutePlannerUI();
            ui.setVisible(true);
        });
    }
}
