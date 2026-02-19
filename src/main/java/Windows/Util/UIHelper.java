package Windows.Util;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;

/**
 * Centralized UI styling helper with parametrized methods.
 * All visual elements can be styled consistently by passing colors and border flags.
 */
public class UIHelper {
    // Standard color scheme
    public static final Color BG_COLOR = Color.BLACK;
    public static final Color FG_COLOR = Color.WHITE;
    public static final Color BORDER_COLOR = Color.WHITE;
    public static final Color PANEL_BG_COLOR = new Color(61, 56, 70);

    // Standard dimensions
    public static final Dimension BUTTON_SIZE = new Dimension(100, 40);
    public static final Dimension COLOR_BUTTON_SIZE = new Dimension(40, 40);
    public static final Dimension TEXT_FIELD_SIZE = new Dimension(50, 20);

    // Standard font
    public static final Font DEFAULT_FONT = UIManager.getDefaults().getFont("Label.font");

    // ===== BUTTON STYLING =====

    /**
     * Apply standard styling to a button using default size.
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
     * @param button Target button
     * @param bgColor Background color
     * @param fgColor Foreground color
     * @param hasBorder Whether the button should display a border
     * @param width Preferred width
     * @param height Preferred height
     */
    public static void styleButton(JButton button, Color bgColor, Color fgColor, boolean hasBorder, int width, int height) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setBorder(hasBorder ? BorderFactory.createLineBorder(BORDER_COLOR) : BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(width, height));
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
        button.setBorder(hasBorder ? BorderFactory.createLineBorder(BORDER_COLOR) : BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setPreferredSize(COLOR_BUTTON_SIZE);
    }

    /**
     * Create a toggle button with ON/OFF states.
     *
     * @param initialState Initial boolean state
     * @param onToggle Callback executed after toggle
     * @return Object[] where index 0 is the JButton instance and index 1 is a Runnable style updater
     */
    public static Object[] createToggleButton(boolean initialState, Runnable onToggle) {
        JButton button = new JButton(initialState ? "ON" : "OFF");

        Runnable updateStyle = () -> {
            boolean isOn = button.getText().equals("ON");
            
            if (isOn) {
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
     * Create a button that inverts colors when clicked (without changing text).
     * Used for toggle-like buttons that need to keep their text.
     *
     * @param text Button text
     * @param onToggle Callback executed after inversion
     * @return Configured JButton instance
     */
    public static JButton createColorInvertButton(String text, Runnable onToggle) {
        JButton button = new JButton(text);
        styleButton(button, BG_COLOR, FG_COLOR, true);

        button.addActionListener(e -> {
            // Invert colors
            Color currentBg = button.getBackground();
            Color currentFg = button.getForeground();
            button.setBackground(currentFg);
            button.setForeground(currentBg);

            onToggle.run();
        });

        return button;
    }

    // ===== LABEL STYLING =====

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
     * Create a bold label with specified font size.
     *
     * @param text Label text
     * @param bgColor Background color
     * @param fgColor Foreground color
     * @param alignment SwingConstants alignment value
     * @param fontSize Font size in points
     * @return Configured bold JLabel
     */
    public static JLabel createBoldLabel(String text, Color bgColor, Color fgColor, int alignment, float fontSize) {
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
    public static JLabel createSectionTitle(String text, int alignment, float fontSize, int bottomPadding) {
        JLabel label = createBoldLabel(text, BG_COLOR, FG_COLOR, alignment, fontSize);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, bottomPadding, 0));
        
        return label;
    }

    /**
     * Create a wrapping text area that looks like a label.
     * This uses JTextArea for proper text wrapping.
     *
     * @param text Text content
     * @param bgColor Background color
     * @param fgColor Foreground color
     * @param font Font to apply
     * @param columns Column width for layout purposes
     * @return Configured JTextArea acting as a label
     */
    public static JTextArea createWrappingLabel(String text, Color bgColor, Color fgColor, Font font, int columns) {
        JTextArea textArea = new JTextArea(text);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setBackground(bgColor);
        textArea.setForeground(fgColor);
        textArea.setFont(font);
        textArea.setBorder(null);
        textArea.setOpaque(true);
        textArea.setColumns(columns);
        
        return textArea;
    }

    /**
     * Create a config panel title with proper wrapping and styling.
     *
     * @param text Title text
     * @param availableWidth Available width in characters or pixels heuristic
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

    // ===== TEXT FIELD STYLING =====

    /**
     * Apply standard styling to a text field.
     *
     * @param field Target JTextField
     * @param bgColor Background color
     * @param fgColor Foreground color
     */
    public static void styleTextField(JTextField field, Color bgColor, Color fgColor) {
        field.setForeground(fgColor);
        field.setBackground(bgColor);
        field.setFont(DEFAULT_FONT);
        field.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        field.setPreferredSize(TEXT_FIELD_SIZE);
    }

    /**
     * Create a text field with initial value and styling.
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
     * Create a text field with border and padding.
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
        field.setFont(DEFAULT_FONT);
        field.setPreferredSize(new Dimension(width, height));
        
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        return field;
    }

    // ===== SLIDER STYLING =====

    /**
     * Apply basic foreground/background styling to a slider.
     *
     * @param slider Target JSlider
     * @param bgColor Background color
     * @param fgColor Foreground color
     */
    public static void styleSlider(JSlider slider, Color bgColor, Color fgColor) {
        slider.setBackground(bgColor);
        slider.setForeground(fgColor);
    }

    /**
     * Create a configured horizontal slider with tick spacing.
     *
     * @param min Minimum slider value
     * @param max Maximum slider value
     * @param initial Initial slider value
     * @param bgColor Background color
     * @param fgColor Foreground color
     * @return Configured JSlider
     */
    public static JSlider createSlider(int min, int max, int initial, Color bgColor, Color fgColor) {
        int range = max - min;
        int majorTick = Math.max(1, range / 4);
        int minorTick = Math.max(1, range / 10);

        JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, initial);
        slider.setMajorTickSpacing(majorTick);
        slider.setMinorTickSpacing(minorTick);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        styleSlider(slider, bgColor, fgColor);
        
        return slider;
    }

    // ===== SLIDER PANEL (with label + slider + textfield) =====

    /**
     * Create a slider panel that contains: label, slider and a text field that mirrors the slider value.
     *
     * @param labelText Label text displayed above the slider
     * @param min Minimum slider value
     * @param max Maximum slider value
     * @param initial Initial slider value
     * @param bgColor Background color for the panel and components
     * @param fgColor Foreground color for text and ticks
     * @return Component[] where index 0 is the panel, index 1 is the JSlider, index 2 is the JTextField
     */
    public static Component[] createSliderPanel(String labelText, int min, int max, int initial,
                                                 Color bgColor, Color fgColor) {
        JSlider slider = createSlider(min, max, initial, bgColor, fgColor);
        JTextField valueField = createTextField(String.valueOf(initial), bgColor, fgColor);

        slider.addChangeListener(e -> valueField.setText(String.valueOf(slider.getValue())));

        valueField.addActionListener(e -> {
            String text = valueField.getText().trim();
            
            if (!text.isEmpty()) {
                try {
                    int value = Integer.parseInt(text);
                    value = Math.max(min, Math.min(max, value));
                    slider.setValue(value);
                    valueField.setText(String.valueOf(value));
                } catch (NumberFormatException ex) {
                    valueField.setText(String.valueOf(slider.getValue()));
                }
            } else {
                valueField.setText(String.valueOf(slider.getValue()));
            }
            valueField.transferFocus();
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bgColor);

        JLabel label = createLabel(labelText, bgColor, fgColor, SwingConstants.LEFT);
        panel.add(label, BorderLayout.NORTH);
        panel.add(slider, BorderLayout.CENTER);
        panel.add(valueField, BorderLayout.EAST);

        return new Component[]{panel, slider, valueField};
    }

    // ===== PANEL STYLING =====

    /**
     * Apply background color and optional border to a panel.
     *
     * @param panel Target JPanel
     * @param bgColor Background color
     * @param hasBorder Whether to add a line border
     */
    public static void stylePanel(JPanel panel, Color bgColor, boolean hasBorder) {
        panel.setBackground(bgColor);
        if (hasBorder) {
            panel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        }
    }

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
     * Returns the content panel that should be populated.
     *
     * @param parent Parent container panel that will host the content
     * @return Content JPanel with BoxLayout.Y_AXIS that should be filled by caller
     */
    public static JPanel createConfigPanelContainer(JPanel parent) {
        parent.setLayout(new BorderLayout());
        stylePanel(parent, BG_COLOR, false);
        parent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        stylePanel(contentPanel, BG_COLOR, false);

        parent.add(contentPanel, BorderLayout.NORTH);

        return contentPanel;
    }

    /**
     * Setup a component to take full width in a BoxLayout.
     *
     * @param component Component to adjust
     */
    public static void setupFullWidth(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
    }

    /**
     * Create a horizontal row with label on left and input on right.
     * Commonly used for: "Seed: [textfield]" or "Debug: [toggle]".
     *
     * @param labelText Label text on the left
     * @param inputComponent Component to place on the right
     * @return Configured JPanel row
     */
    public static JPanel createLabeledInputRow(String labelText, Component inputComponent) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(BG_COLOR);

        JLabel label = createLabel(labelText, BG_COLOR, FG_COLOR, SwingConstants.LEFT);

        panel.add(label, BorderLayout.WEST);
        panel.add(inputComponent, BorderLayout.CENTER);

        setupFullWidth(panel);

        return panel;
    }

    /**
     * Create a visual divider (horizontal separator line).
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
     * Create vertical spacing between elements.
     *
     * @param height Height in pixels
     * @return Component used as vertical spacer
     */
    public static Component createVerticalSpace(int height) {
        return Box.createRigidArea(new Dimension(0, height));
    }

    // ===== LIST STYLING =====

    /**
     * Apply styling to a JList.
     *
     * @param list Target JList
     * @param bgColor Background color
     * @param fgColor Foreground color
     * @param selectionBgColor Background color when selected
     * @param selectionFgColor Foreground color when selected
     * @param hasBorder Whether to draw a border around the list
     */
    public static void styleList(JList<?> list, Color bgColor, Color fgColor,
                                  Color selectionBgColor, Color selectionFgColor, boolean hasBorder) {
        list.setBackground(bgColor);
        list.setForeground(fgColor);
        list.setSelectionBackground(selectionBgColor);
        list.setSelectionForeground(selectionFgColor);
        
        if (hasBorder) {
            list.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        }
    }

    // ===== SCROLL PANE STYLING =====

    /**
     * Style a JScrollPane border presence.
     *
     * @param scrollPane Target JScrollPane
     * @param hasBorder Whether to show a line border
     */
    public static void styleScrollPane(JScrollPane scrollPane, boolean hasBorder) {
        if (hasBorder) {
            scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        } else {
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
        }
    }

    // ===== COMBO BOX STYLING =====

    /**
     * Apply base styling to a JComboBox and customize the arrow button.
     *
     * @param comboBox Target JComboBox
     * @param bgColor Background color
     * @param fgColor Foreground color
     * @param hasBorder Whether to draw a border around the combo box
     * @param <T> Type parameter for combo box model
     */
    public static <T> void styleComboBox(JComboBox<T> comboBox, Color bgColor, Color fgColor, boolean hasBorder) {
        comboBox.setBackground(bgColor);
        comboBox.setForeground(fgColor);
        
        if (hasBorder) {
            comboBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        }
        
        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                return createStyledArrowButton(SwingConstants.SOUTH, bgColor, fgColor);
            }
        });
    }

    /**
     * Fully customize ComboBox UI including popup, scrollbar, and arrows.
     * This is the complete styling used in DropDownWindow.
     *
     * @param comboBox Target JComboBox to customize
     * @param <E> Enum type used in the combo box model
     */
    public static <E extends Enum<E>> void customizeComboBoxUI(JComboBox<E> comboBox) {
        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected ComboBoxEditor createEditor() {
                ComboBoxEditor editor = super.createEditor();
                Component editorComponent = editor.getEditorComponent();
                editorComponent.setBackground(BG_COLOR);
                editorComponent.setForeground(FG_COLOR);
                
                return editor;
            }

            @Override
            protected ComboPopup createPopup() {
                BasicComboPopup popup = (BasicComboPopup) super.createPopup();
                popup.getList().setBackground(BG_COLOR);
                popup.getList().setForeground(FG_COLOR);
                popup.getList().setSelectionBackground(FG_COLOR);
                popup.getList().setSelectionForeground(BG_COLOR);
                popup.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

                JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                bar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_COLOR));

                bar.setUI(new BasicScrollBarUI() {
                    @Override
                    protected JButton createDecreaseButton(int orientation) {
                        return createStyledArrowButton(SwingConstants.NORTH, BG_COLOR, FG_COLOR);
                    }

                    @Override
                    protected JButton createIncreaseButton(int orientation) {
                        return createStyledArrowButton(SwingConstants.SOUTH, BG_COLOR, FG_COLOR);
                    }

                    @Override
                    protected void configureScrollBarColors() {
                        this.thumbColor = FG_COLOR;
                        this.trackColor = BG_COLOR;
                    }
                });

                return popup;
            }

            @Override
            protected JButton createArrowButton() {
                return createStyledArrowButton(SwingConstants.SOUTH, BG_COLOR, FG_COLOR);
            }
        });
    }

    private static JButton createStyledArrowButton(int direction, Color bgColor, Color fgColor) {
        BasicArrowButton arrow = new BasicArrowButton(direction, bgColor, fgColor, fgColor, bgColor);
        arrow.setBorder(BorderFactory.createEmptyBorder());
        
        return arrow;
    }

    // ===== UTILITY METHODS =====

    /**
     * Show an error dialog with standard title.
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
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int xPos = (screenSize.width - window.getWidth()) / 2;
        int yPos = (screenSize.height - window.getHeight()) / 2;
        window.setLocation(xPos, yPos);
    }
}