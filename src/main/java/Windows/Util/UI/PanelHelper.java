package Windows.Util.UI;

import javax.swing.*;
import java.awt.*;

import static Windows.Util.UI.UIConstants.*;

/**
 * Factory and styling methods for panels, container layouts, dividers, and spacing.
 */
public final class PanelHelper {
    private PanelHelper() {}

    // ===== STYLING =====
    /**
     * Apply background color and an optional border to a panel.
     *
     * @param panel Target JPanel
     * @param bgColor Background color
     * @param hasBorder Whether to add a line border
     */
    public static void stylePanel(JPanel panel, Color bgColor, boolean hasBorder) {
        panel.setBackground(bgColor);
        
        if (hasBorder) panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
    }

    /**
     * Configure a component to take full width inside a BoxLayout container.
     *
     * @param component Component to adjust
     */
    public static void setupFullWidth(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
    }

    // ===== FACTORY =====
    /**
     * Create a styled panel with the specified layout.
     *
     * @param layout Layout manager to apply
     * @param bgColor Background color
     * @param hasBorder Whether to add a line border
     * @return Configured JPanel
     */
    public static JPanel createPanel(LayoutManager layout, Color bgColor, boolean hasBorder) {
        JPanel panel = new JPanel(layout);
        stylePanel(panel, bgColor, hasBorder);
        
        return panel;
    }

    /**
     * Create a config panel container with standard layout.
     * The returned content panel should be populated by the caller.
     *
     * @param parent Parent container panel that will host the content
     * @return Content JPanel with BoxLayout.Y_AXIS added to parent's NORTH
     */
    public static JPanel createConfigPanelContainer(JPanel parent) {
        parent.setLayout(new BorderLayout());
        stylePanel(parent, BG_COLOR, false);
        parent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        stylePanel(content, BG_COLOR, false);

        parent.add(content, BorderLayout.NORTH);
        
        return content;
    }

    /**
     * Create a horizontal row with a label on the left and an input component on the right.
     * Commonly used for pairs such as "Seed: [textfield]" or "Debug: [toggle]".
     *
     * @param labelText Label text on the left
     * @param inputComponent Component to place on the right
     * @return Configured JPanel row
     */
    public static JPanel createLabeledInputRow(String labelText, Component inputComponent) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(BG_COLOR);
        panel.add(LabelHelper.createLabel(labelText, BG_COLOR, FG_COLOR, SwingConstants.LEFT), BorderLayout.WEST);
        panel.add(inputComponent, BorderLayout.CENTER);
        setupFullWidth(panel);
        
        return panel;
    }

    /**
     * Create a visual divider (1 px horizontal separator line).
     *
     * @return JPanel acting as a horizontal divider
     */
    public static JPanel createDivider() {
        JPanel divider = new JPanel();
        divider.setBackground(BG_COLOR);
        divider.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setPreferredSize(new Dimension(Integer.MAX_VALUE, 1));
        
        return divider;
    }

    /**
     * Create vertical spacing between elements in a BoxLayout container.
     *
     * @param height Height in pixels
     * @return Component used as a vertical spacer
     */
    public static Component createVerticalSpace(int height) {
        return Box.createRigidArea(new Dimension(0, height));
    }
}