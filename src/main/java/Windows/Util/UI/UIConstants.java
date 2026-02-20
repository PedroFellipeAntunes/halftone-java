package Windows.Util.UI;

import javax.swing.*;
import java.awt.*;

/**
 * Shared constants for the application UI color scheme, dimensions, and font.
 * All helper classes import these statically to avoid duplication.
 */
public final class UIConstants {
    private UIConstants() {}

    // ===== COLOR SCHEME =====
    public static final Color BG_COLOR = Color.BLACK;
    public static final Color FG_COLOR = Color.WHITE;
    public static final Color BORDER_COLOR = Color.WHITE;
    public static final Color PANEL_BG_COLOR = new Color(61, 56, 70);

    /**
     * Color applied to a button's background for the duration of a mouse press.
     * Using a dedicated mid-tone avoids interfering with toggle button state logic,
     * since only the background is changed and it is only restored if it still
     * holds this exact value when the mouse is released.
     */
    public static final Color PRESS_COLOR = new Color(80, 80, 80);

    // ===== STANDARD DIMENSIONS =====
    public static final Dimension BUTTON_SIZE = new Dimension(100, 40);
    public static final Dimension COLOR_BUTTON_SIZE = new Dimension(40, 40);
    public static final Dimension TEXT_FIELD_SIZE = new Dimension(50, 20);

    // ===== FONT =====
    public static final Font DEFAULT_FONT = UIManager.getDefaults().getFont("Label.font");
}