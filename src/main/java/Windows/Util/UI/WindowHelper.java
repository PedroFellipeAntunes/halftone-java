package Windows.Util.UI;

import javax.swing.*;
import java.awt.*;

/**
 * General-purpose window utilities.
 */
public final class WindowHelper {

    private WindowHelper() {}

    /**
     * Show an error dialog with a standard title.
     *
     * @param parent Parent component
     * @param message Error message to display
     */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Center a window on the screen.
     *
     * @param window Target window
     */
    public static void centerWindow(Window window) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        window.setLocation(
                (screen.width  - window.getWidth())  / 2,
                (screen.height - window.getHeight()) / 2);
    }
}