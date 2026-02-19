package Windows.Util.Panels;

import Data.ConfigData;
import Windows.Util.ConfigPanel;
import Windows.Util.UIHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Configuration panel for Stippling TYPE.
 * Provides controls to adjust stippling dot density.
 */
public class StipplingConfigPanel extends ConfigPanel {
    private JSlider densitySlider;
    private JTextField densityField;

    private static final int MIN_DENSITY = 1;
    private static final int MAX_DENSITY = 100;

    /**
     * Create a new Stippling configuration panel.
     *
     * @param config Configuration data reference
     */
    public StipplingConfigPanel(ConfigData config) {
        super(config);
    }

    /**
     * Initialize all UI components and layout.
     * Builds the title and slider used to control dot density.
     */
    @Override
    public void initializeComponents() {
        JPanel contentPanel = UIHelper.createConfigPanelContainer(this);

        // Title
        contentPanel.add(UIHelper.createConfigTitle(
            "Max Stippling Dot Density (Not recommended to change, the default value was defined by evaluating different densities):",
            availableWidth
        ));

        // Slider
        Component[] densityComponents = UIHelper.createSliderPanel(
            "Dot Density (" + MIN_DENSITY + "-" + MAX_DENSITY + ")",
            MIN_DENSITY,
            MAX_DENSITY,
            config.stipplingDensity,
            UIHelper.BG_COLOR,
            UIHelper.FG_COLOR
        );

        UIHelper.setupFullWidth((JComponent) densityComponents[0]);
        densitySlider = (JSlider) densityComponents[1];
        densityField = (JTextField) densityComponents[2];

        contentPanel.add((Component) densityComponents[0]);
    }

    /**
     * Apply the current UI state to the underlying configuration.
     */
    @Override
    public void applyConfig() {
        config.stipplingDensity = densitySlider.getValue();
    }

    /**
     * Get the display name shown in UI selectors.
     *
     * @return Display name for this configuration panel
     */
    @Override
    public String getDisplayName() {
        return "Stippling Configuration";
    }

    /**
     * Enable or disable all interactive components.
     *
     * @param enabled true to enable components, false to disable
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        densitySlider.setEnabled(enabled);
        densityField.setEnabled(enabled);
    }
}