package Windows.Util.Panels;

import Data.ConfigData;
import Windows.Util.ConfigPanel;
import Windows.Util.UIHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Configuration panel for Polygons TYPE.
 * Provides controls to configure the number of polygon sides.
 */
public class PolygonsConfigPanel extends ConfigPanel {
    private JSlider sidesSlider;
    private JTextField sidesField;

    private static final int MIN_SIDES = 3;
    private static final int MAX_SIDES = 12;

    /**
     * Create a new Polygons configuration panel.
     *
     * @param config Configuration data reference
     */
    public PolygonsConfigPanel(ConfigData config) {
        super(config);
    }

    /**
     * Initialize all UI components and layout.
     * Builds the title and slider panel used to control polygon sides.
     */
    @Override
    public void initializeComponents() {
        JPanel contentPanel = UIHelper.createConfigPanelContainer(this);

        // Title
        JLabel titleLabel = UIHelper.createSectionTitle(
            "Number of Polygon Sides:",
            SwingConstants.LEFT,
            14f,
            15
        );
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);

        // Slider
        Component[] sidesComponents = UIHelper.createSliderPanel(
            "Sides (" + MIN_SIDES + "-" + MAX_SIDES + ")",
            MIN_SIDES,
            MAX_SIDES,
            config.polySides,
            UIHelper.BG_COLOR,
            UIHelper.FG_COLOR
        );

        UIHelper.setupFullWidth((JComponent) sidesComponents[0]);
        sidesSlider = (JSlider) sidesComponents[1];
        sidesField = (JTextField) sidesComponents[2];

        contentPanel.add((Component) sidesComponents[0]);
    }

    /**
     * Apply the current UI state to the underlying configuration.
     */
    @Override
    public void applyConfig() {
        config.polySides = sidesSlider.getValue();
    }

    /**
     * Get the display name shown in UI selectors.
     *
     * @return Display name for this configuration panel
     */
    @Override
    public String getDisplayName() {
        return "Polygons Configuration";
    }

    /**
     * Enable or disable all interactive components.
     *
     * @param enabled true to enable components, false to disable
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        sidesSlider.setEnabled(enabled);
        sidesField.setEnabled(enabled);
    }
}