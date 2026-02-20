package Windows.Util.UI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static Windows.Util.UI.UIConstants.*;

/**
 * Factory and styling methods for all button types used in the application.
 */
public final class ButtonHelper {
    private ButtonHelper() {}

    // ===== STYLING =====
    /**
     * Apply standard styling to a button using the default size.
     *
     * @param button Target button
     * @param bgColor Background color
     * @param fgColor Foreground color
     * @param hasBorder Whether the button should display a border
     */
    public static void styleButton(JButton button, Color bgColor, Color fgColor, boolean hasBorder) {
        styleButton(button, bgColor, fgColor, hasBorder, BUTTON_SIZE.width, BUTTON_SIZE.height);
    }

    /**
     * Apply full styling control to a button including explicit dimensions.
     *
     * Press effect: PRESS_COLOR is applied to the background only while the mouse
     * is held down. On release the background is restored only if it still holds
     * PRESS_COLOR; if a toggle ActionListener already updated it, the post-toggle
     * color is kept. The foreground is never touched by the press effect.
     *
     * Focus indicator: when the button owns keyboard focus its border is swapped
     * to a dashed line border. On focus loss the original solid border is restored.
     * This replaces the border entirely instead of painting an overlay inside the
     * button, so the dashed line occupies exactly the same position as the solid one.
     *
     * @param button Target button
     * @param bgColor Background color
     * @param fgColor Foreground color
     * @param hasBorder Whether the button should display a solid outer border
     * @param width Preferred width
     * @param height Preferred height
     */
    public static void styleButton(JButton button, Color bgColor, Color fgColor,
                                   boolean hasBorder, int width, int height) {
        // Define both border states up front so the FocusListener can swap between them.
        // focusBorder replaces the entire border (not an inner overlay), so the dashed
        // line sits exactly where the solid line normally would.
        Border normalBorder = hasBorder
                ? BorderFactory.createLineBorder(BORDER_COLOR)
                : BorderFactory.createEmptyBorder();
        Border focusBorder = BorderFactory.createDashedBorder(BORDER_COLOR, 1.5f, 4f, 4f, false);

        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setBorder(button.isFocusOwner() ? focusBorder : normalBorder);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(width, height));

        // Suppress the default grey pressed flash; the MouseListener owns the pressed visual.
        button.setUI(new BasicButtonUI() {
            @Override
            protected void paintButtonPressed(Graphics g, AbstractButton b) {
                // Intentionally empty — MouseListener owns the pressed visual.
            }
        });

        // Press-color effect registered only once per button instance.
        if (button.getClientProperty("themedPressAdded") == null) {
            button.putClientProperty("themedPressAdded", Boolean.TRUE);
            
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (button.isEnabled()) button.setBackground(PRESS_COLOR);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    // ActionListener (e.g. toggle logic) fires before mouseReleased.
                    // Restore bgColor only when PRESS_COLOR is still set; if the
                    // toggle already updated the background we must not overwrite it.
                    if (PRESS_COLOR.equals(button.getBackground())) button.setBackground(bgColor);
                }
            });
        }

        // FocusListener swaps the real border instead of painting an overlay.
        // Registered only once per button instance.
        if (button.getClientProperty("themedFocusRepaintAdded") == null) {
            button.putClientProperty("themedFocusRepaintAdded", Boolean.TRUE);
            
            button.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) {
                    button.setBorder(focusBorder);
                }
                @Override public void focusLost(FocusEvent e) {
                    button.setBorder(normalBorder);
                }
            });
        }
    }

    /**
     * Style a color button used for color selection controls.
     *
     * @param button Target button
     * @param color Background color to apply
     * @param hasBorder Whether a border should be displayed
     */
    public static void styleColorButton(JButton button, Color color, boolean hasBorder) {
        button.setBackground(color);
        button.setBorder(hasBorder
                ? BorderFactory.createLineBorder(BORDER_COLOR)
                : BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setPreferredSize(COLOR_BUTTON_SIZE);
    }

    // ===== FACTORY =====
    /**
     * Create a toggle button with ON/OFF states.
     *
     * @param initialState Initial boolean state
     * @param onToggle Callback executed after each toggle
     * @return Object[] where index 0 is the JButton instance and index 1 is a Runnable style updater
     */
    public static Object[] createToggleButton(boolean initialState, Runnable onToggle) {
        JButton button = new JButton(initialState ? "ON" : "OFF");

        Runnable updateStyle = () -> {
            if (button.getText().equals("ON")) {
                styleButton(button, FG_COLOR, BG_COLOR, true, 80, 30);
            } else {
                styleButton(button, BG_COLOR, FG_COLOR, true, 80, 30);
            }
        };

        updateStyle.run();

        button.addActionListener(e -> {
            button.setText(button.getText().equals("ON") ? "OFF" : "ON");
            updateStyle.run();
            onToggle.run();
        });

        return new Object[]{button, updateStyle};
    }

    /**
     * Create a button that inverts colors when clicked without changing its text.
     * Used for toggle-like buttons that need to keep their label.
     *
     * @param text Button text
     * @param onToggle Callback executed after inversion
     * @return Configured JButton instance
     */
    public static JButton createColorInvertButton(String text, Runnable onToggle) {
        JButton button = new JButton(text);
        styleButton(button, BG_COLOR, FG_COLOR, true);

        button.addActionListener(e -> {
            Color bg = button.getBackground();
            Color fg = button.getForeground();
            button.setBackground(fg);
            button.setForeground(bg);
            onToggle.run();
        });

        return button;
    }
}