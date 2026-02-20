package Windows.Util.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import static Windows.Util.UI.UIConstants.*;

/**
 * Factory and styling methods for text input fields.
 */
public final class TextFieldHelper {
    private TextFieldHelper() {}

    // ===== STYLING =====
    /**
     * Apply standard styling to a text field.
     * When the field gains focus the background and foreground are inverted;
     * they are restored to the supplied colors when focus is lost.
     *
     * @param field Target JTextField
     * @param bgColor Background color (used when not focused)
     * @param fgColor Foreground color (used when not focused)
     */
    public static void styleTextField(JTextField field, Color bgColor, Color fgColor) {
        field.setForeground(fgColor);
        field.setBackground(bgColor);
        field.setCaretColor(fgColor);
        field.setFont(DEFAULT_FONT);
        field.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        field.setPreferredSize(TEXT_FIELD_SIZE);
        addFocusInversion(field, bgColor, fgColor);
    }

    // ===== FACTORY =====
    /**
     * Create a text field with an initial value and standard styling.
     *
     * @param initialValue Initial text value
     * @param bgColor Background color
     * @param fgColor Foreground color
     * @return Configured JTextField
     */
    public static JTextField createTextField(String initialValue, Color bgColor, Color fgColor) {
        JTextField field = new JTextField(initialValue);
        styleTextField(field, bgColor, fgColor);
        
        return field;
    }

    /**
     * Create a text field with a visible border and inner padding.
     * Background and foreground are inverted while the field has focus.
     *
     * @param initialValue Initial text value
     * @param width Preferred width in pixels
     * @param height Preferred height in pixels
     * @return Configured JTextField with border and padding
     */
    public static JTextField createBorderedTextField(String initialValue, int width, int height) {
        JTextField field = new JTextField(initialValue);
        field.setBackground(BG_COLOR);
        field.setForeground(FG_COLOR);
        field.setCaretColor(FG_COLOR);
        field.setFont(DEFAULT_FONT);
        field.setPreferredSize(new Dimension(width, height));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        addFocusInversion(field, BG_COLOR, FG_COLOR);
        
        return field;
    }

    /**
     * Attach a FocusListener that inverts background/foreground while the field
     * is focused and restores the original colors on focus loss.
     * The caret color is kept readable in both states.
     *
     * @param field Target text field
     * @param bgColor Normal (unfocused) background color
     * @param fgColor Normal (unfocused) foreground color
     */
    private static void addFocusInversion(JTextField field, Color bgColor, Color fgColor) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBackground(fgColor);
                field.setForeground(bgColor);
                field.setCaretColor(bgColor);
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBackground(bgColor);
                field.setForeground(fgColor);
                field.setCaretColor(fgColor);
            }
        });
    }
}