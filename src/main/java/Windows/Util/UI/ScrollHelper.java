package Windows.Util.UI;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

import static Windows.Util.UI.UIConstants.*;

/**
 * Factory and styling methods for scroll panes and lists.
 */
public final class ScrollHelper {
    private ScrollHelper() {}

    // ===== STYLING =====
    /**
     * Style a JScrollPane's border.
     *
     * @param scrollPane Target JScrollPane
     * @param hasBorder Whether to show a line border
     */
    public static void styleScrollPane(JScrollPane scrollPane, boolean hasBorder) {
        scrollPane.setBorder(hasBorder
                ? BorderFactory.createLineBorder(BORDER_COLOR)
                : BorderFactory.createEmptyBorder());
    }

    /**
     * Apply styling to a JList.
     *
     * @param list Target JList
     * @param bgColor Background color
     * @param fgColor Foreground color
     * @param selectionBgColor Background color when an item is selected
     * @param selectionFgColor Foreground color when an item is selected
     * @param hasBorder Whether to draw a border around the list
     */
    public static void styleList(JList<?> list, Color bgColor, Color fgColor,
                                 Color selectionBgColor, Color selectionFgColor,
                                 boolean hasBorder) {
        list.setBackground(bgColor);
        list.setForeground(fgColor);
        list.setSelectionBackground(selectionBgColor);
        list.setSelectionForeground(selectionFgColor);
        
        if (hasBorder) list.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
    }

    // ===== FACTORY =====
    /**
     * Create a themed vertical-only scroll pane matching the application color scheme.
     * The vertical scroll bar appears only when content exceeds the available height.
     * No horizontal scroll bar is shown.
     *
     * @param view Component to wrap inside the scroll pane
     * @return Configured JScrollPane
     */
    public static JScrollPane createThemedScrollPane(JComponent view) {
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(BG_COLOR);
        scrollPane.getViewport().setBackground(BG_COLOR);

        applyThemedScrollBarUI(scrollPane.getVerticalScrollBar());

        return scrollPane;
    }

    /**
     * Apply the themed scroll bar UI (colors + arrow buttons) to the given scroll bar.
     * Package-private so ComboBoxHelper can reuse it for the combo popup's scroll bar.
     *
     * @param bar Target JScrollBar
     */
    static void applyThemedScrollBarUI(JScrollBar bar) {
        bar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_COLOR));
        bar.setBackground(BG_COLOR);
        bar.setUI(new BasicScrollBarUI() {
            @Override protected JButton createDecreaseButton(int o) { return createArrowButton(SwingConstants.NORTH); }
            @Override protected JButton createIncreaseButton(int o) { return createArrowButton(SwingConstants.SOUTH); }
            @Override protected void configureScrollBarColors() {
                this.thumbColor = FG_COLOR;
                this.trackColor = BG_COLOR;
            }
        });
    }

    /**
     * Create a themed arrow button for use in scroll bars and combo boxes.
     *
     * @param direction SwingConstants direction (NORTH or SOUTH)
     * @return Configured BasicArrowButton
     */
    static JButton createArrowButton(int direction) {
        BasicArrowButton btn = new BasicArrowButton(direction, BG_COLOR, FG_COLOR, FG_COLOR, BG_COLOR);
        btn.setBorder(BorderFactory.createEmptyBorder());
        
        return btn;
    }
}