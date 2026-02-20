package Windows.Util.UI;

import javax.swing.*;
import java.awt.*;

import static Windows.Util.UI.UIConstants.*;

/**
 * Factory and styling methods for labels and label-like text display components.
 */
public final class LabelHelper {
    private LabelHelper() {}

    // ===== STYLING =====
    /**
     * Apply standard label styling.
     *
     * @param label Target JLabel
     * @param bgColor Background color
     * @param fgColor Foreground color
     */
    public static void styleLabel(JLabel label, Color bgColor, Color fgColor) {
        label.setForeground(fgColor);
        label.setBackground(bgColor);
        label.setOpaque(true);
        label.setFont(DEFAULT_FONT);
    }

    // ===== FACTORY =====
    /**
     * Create a styled label with alignment.
     *
     * @param text Label text
     * @param bgColor Background color
     * @param fgColor Foreground color
     * @param alignment SwingConstants alignment value
     * @return Configured JLabel
     */
    public static JLabel createLabel(String text, Color bgColor, Color fgColor, int alignment) {
        JLabel label = new JLabel(text, alignment);
        styleLabel(label, bgColor, fgColor);
        
        return label;
    }

    /**
     * Create a bold label with a specified font size.
     *
     * @param text Label text
     * @param bgColor Background color
     * @param fgColor Foreground color
     * @param alignment SwingConstants alignment value
     * @param fontSize Font size in points
     * @return Configured bold JLabel
     */
    public static JLabel createBoldLabel(String text, Color bgColor, Color fgColor,
                                         int alignment, float fontSize) {
        JLabel label = createLabel(text, bgColor, fgColor, alignment);
        label.setFont(label.getFont().deriveFont(Font.BOLD, fontSize));
        
        return label;
    }

    /**
     * Create a section title label (bold, with bottom padding).
     * Used for section headers in panels and windows.
     *
     * @param text Title text
     * @param alignment SwingConstants alignment value
     * @param fontSize Font size in points
     * @param bottomPadding Bottom padding in pixels
     * @return Configured section title JLabel
     */
    public static JLabel createSectionTitle(String text, int alignment,
                                            float fontSize, int bottomPadding) {
        JLabel label = createBoldLabel(text, BG_COLOR, FG_COLOR, alignment, fontSize);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, bottomPadding, 0));
        
        return label;
    }

    /**
     * Create a wrapping text area that looks like a label.
     * Uses JTextArea internally since JLabel does not support text wrapping.
     *
     * @param text Text content
     * @param bgColor Background color
     * @param fgColor Foreground color
     * @param font Font to apply
     * @param columns Column width for layout purposes
     * @return Configured JTextArea acting as a label
     */
    public static JTextArea createWrappingLabel(String text, Color bgColor, Color fgColor,
                                                Font font, int columns) {
        JTextArea area = new JTextArea(text);
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setEditable(false);
        area.setFocusable(false);
        area.setBackground(bgColor);
        area.setForeground(fgColor);
        area.setFont(font);
        area.setBorder(null);
        area.setOpaque(true);
        area.setColumns(columns);
        
        return area;
    }

    /**
     * Create a config panel title with proper wrapping and styling.
     *
     * @param text Title text
     * @param availableWidth Available width in pixels (used to compute column count heuristic)
     * @return Configured JTextArea to be used as a config panel title
     */
    public static JTextArea createConfigTitle(String text, int availableWidth) {
        int columns = availableWidth > 80 ? (availableWidth - 60) / 8 : 50;
        Font boldFont = DEFAULT_FONT.deriveFont(Font.BOLD, 14f);
        JTextArea title = createWrappingLabel(text, BG_COLOR, FG_COLOR, boldFont, columns);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        return title;
    }
}