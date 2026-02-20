package Windows.Util.Panels;

import Data.ConfigData;
import Windows.Util.ConfigPanel;
import Windows.Util.UI.*;

import javax.swing.*;

/**
 * Configuration panel for miscellaneous global settings.
 * Handles RNG seed input and debug image generation toggle.
 */
public class ExtraConfigPanel extends ConfigPanel {
    private JTextField seedField;
    private JButton debugToggle;
    private boolean debugState;

    /**
     * Initializes the panel with existing configuration values.
     *
     * @param config ConfigData object containing current application settings.
     */
    public ExtraConfigPanel(ConfigData config) {
        super(config);
        this.debugState = config.debugState;
    }

    /**
     * Builds and lays out all UI components within this panel.
     * Must be called before the panel is displayed.
     */
    @Override
    public void initializeComponents() {
        JPanel contentPanel = PanelHelper.createConfigPanelContainer(this);

        // === RNG SEED SECTION ===
        contentPanel.add(LabelHelper.createConfigTitle(
            "Random Number Generator Seed (for reproducible results):",
            availableWidth
        ));

        seedField = TextFieldHelper.createBorderedTextField(String.valueOf(config.rngSeed), 200, 30);
        contentPanel.add(PanelHelper.createLabeledInputRow("Seed:", seedField));

        contentPanel.add(PanelHelper.createVerticalSpace(20));
        contentPanel.add(PanelHelper.createDivider());
        contentPanel.add(PanelHelper.createVerticalSpace(20));

        // === DEBUG SECTION ===
        contentPanel.add(LabelHelper.createConfigTitle(
            "Generate Debug Images:",
            availableWidth
        ));

        // Toggle flips debugState on each click; actual write to config happens in applyConfig
        Object[] toggleComponents = ButtonHelper.createToggleButton(
            debugState,
            () -> debugState = !debugState
        );

        debugToggle = (JButton) toggleComponents[0];
        contentPanel.add(PanelHelper.createLabeledInputRow("Debug:", debugToggle));
    }

    /**
     * Reads current UI state and writes it back to the shared ConfigData object.
     * Resets the seed field to its previous value if the input is not a valid long.
     */
    @Override
    public void applyConfig() {
        // Validate and apply RNG seed; reset the field if the user typed an invalid value
        try {
            config.rngSeed = Long.parseLong(seedField.getText().trim());
        } catch (NumberFormatException e) {
            seedField.setText(String.valueOf(config.rngSeed));
        }
        config.debugState = debugState;
    }

    /**
     * Returns the display name shown in the configuration panel list.
     *
     * @return Human-readable panel name.
     */
    @Override
    public String getDisplayName() {
        return "Extra Configuration";
    }

    /**
     * Enables or disables all interactive controls within this panel.
     *
     * @param enabled True to enable controls, false to disable.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        seedField.setEnabled(enabled);
        debugToggle.setEnabled(enabled);
    }
}