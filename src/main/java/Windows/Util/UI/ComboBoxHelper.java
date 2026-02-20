package Windows.Util.UI;

import javax.swing.*;
import javax.swing.plaf.basic.*;
import java.awt.*;

import static Windows.Util.UI.UIConstants.*;

/**
 * Factory and styling methods for combo boxes.
 */
public final class ComboBoxHelper {
    private ComboBoxHelper() {}

    /**
     * Apply base styling to a JComboBox and customize the arrow button.
     *
     * @param comboBox Target JComboBox
     * @param bgColor Background color
     * @param fgColor Foreground color
     * @param hasBorder Whether to draw a border around the combo box
     * @param <T> Type parameter for the combo box model
     */
    public static <T> void styleComboBox(JComboBox<T> comboBox, Color bgColor,
                                         Color fgColor, boolean hasBorder) {
        comboBox.setBackground(bgColor);
        comboBox.setForeground(fgColor);
        
        if (hasBorder) comboBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                return createStyledArrowButton(SwingConstants.SOUTH, bgColor, fgColor);
            }
        });
    }

    /**
     * Fully customize a ComboBox UI including the popup list, scrollbar, and arrow buttons.
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
                editor.getEditorComponent().setBackground(BG_COLOR);
                editor.getEditorComponent().setForeground(FG_COLOR);
                
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

                ScrollHelper.applyThemedScrollBarUI(
                        ((JScrollPane) popup.getComponent(0)).getVerticalScrollBar());

                return popup;
            }

            @Override
            protected JButton createArrowButton() {
                return createStyledArrowButton(SwingConstants.SOUTH, BG_COLOR, FG_COLOR);
            }
        });
    }

    /**
     * Create a themed arrow button with explicit colors.
     * Named createStyledArrowButton to avoid collision with BasicComboBoxUI.createArrowButton()
     * inside anonymous subclasses.
     *
     * @param direction SwingConstants direction (NORTH, SOUTH, etc.)
     * @param bgColor Background color
     * @param fgColor Foreground / arrow color
     * @return Configured BasicArrowButton
     */
    private static JButton createStyledArrowButton(int direction, Color bgColor, Color fgColor) {
        BasicArrowButton btn = new BasicArrowButton(direction, bgColor, fgColor, fgColor, bgColor);
        btn.setBorder(BorderFactory.createEmptyBorder());
        
        return btn;
    }
}