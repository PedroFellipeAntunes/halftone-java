package Windows.Util.Panels;

import Data.ConfigData;
import Windows.Util.ConfigPanel;
import Windows.Util.UI.*;

import javax.swing.*;
import java.awt.*;

import static Windows.Util.UI.UIConstants.*;

/**
 * Configuration panel for Lines TYPE.
 * Provides controls to configure row inversion and row draw probability.
 */
public class LinesConfigPanel extends ConfigPanel {
    private JButton invertRowToggle;
    private JSlider rowProbabilitySlider;
    private JTextField rowProbabilityField;
    private boolean invertRowState;

    private static final int MIN_PROBABILITY = 0;
    private static final int MAX_PROBABILITY = 100;

    /**
     * Creates a new Lines configuration panel.
     *
     * @param config Configuration data reference.
     */
    public LinesConfigPanel(ConfigData config) {
        super(config);
        this.invertRowState = config.invertRowSelection;
    }

    /**
     * Initializes all UI components and layout.
     * Builds a row inversion toggle and a row probability slider.
     */
    @Override
    public void initializeComponents() {
        JPanel contentPanel = PanelHelper.createConfigPanelContainer(this);

        // === ROW INVERSION SECTION ===
        contentPanel.add(LabelHelper.createConfigTitle(
            "Invert which rows are selected for drawing:",
            availableWidth
        ));

        Object[] toggleComponents = ButtonHelper.createToggleButton(
            invertRowState,
            () -> invertRowState = !invertRowState
        );

        invertRowToggle = (JButton) toggleComponents[0];
        contentPanel.add(PanelHelper.createLabeledInputRow("Invert Row Selection:", invertRowToggle));

        contentPanel.add(PanelHelper.createVerticalSpace(20));
        contentPanel.add(PanelHelper.createDivider());
        contentPanel.add(PanelHelper.createVerticalSpace(20));

        // === ROW PROBABILITY SECTION ===
        contentPanel.add(LabelHelper.createConfigTitle(
            "Percentage chance that any given row will be drawn:",
            availableWidth
        ));

        Component[] probabilityComponents = SliderHelper.createSliderPanel(
            "Row Probability (" + MIN_PROBABILITY + "-" + MAX_PROBABILITY + "%)",
            MIN_PROBABILITY, MAX_PROBABILITY, (int) (config.rowProbability * 100),
            BG_COLOR, FG_COLOR
        );

        JPanel probabilityPanel = (JPanel) probabilityComponents[0];
        PanelHelper.setupFullWidth(probabilityPanel);

        rowProbabilitySlider = (JSlider) probabilityComponents[1];
        rowProbabilityField = (JTextField) probabilityComponents[2];

        contentPanel.add(probabilityPanel);
    }

    /**
     * Applies the current UI state to the underlying configuration.
     */
    @Override
    public void applyConfig() {
        config.invertRowSelection = invertRowState;
        config.rowProbability = rowProbabilitySlider.getValue() / 100.0;
    }

    /**
     * Returns the display name shown in UI selectors.
     *
     * @return The display name for this configuration panel.
     */
    @Override
    public String getDisplayName() {
        return "Lines Configuration";
    }

    /**
     * Enables or disables all interactive components.
     *
     * @param enabled True to enable all components, false to disable them.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        invertRowToggle.setEnabled(enabled);
        rowProbabilitySlider.setEnabled(enabled);
        rowProbabilityField.setEnabled(enabled);
    }
}