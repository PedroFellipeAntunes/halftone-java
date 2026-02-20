package Windows.Util.Panels;

import Data.ConfigData;
import Windows.Util.ConfigPanel;
import Windows.Util.UI.*;

import javax.swing.*;
import java.awt.*;

import static Windows.Util.UI.UIConstants.*;

/**
 * Configuration panel for Wave (Sine) halftone type.
 * Provides controls to configure the amplitude scalar and frequency scalar
 * used in the sine-wave pattern rendering.
 *
 * Both double values are represented as integers scaled by 10 on the slider
 * (e.g. slider value 20 → 2.0, slider value 60 → 6.0), giving one decimal
 * place of precision while reusing the standard {@link SliderHelper} infrastructure.
 */
public class WaveConfigPanel extends ConfigPanel {
    private JSlider amplitudeSlider;
    private JTextField amplitudeField;
    private JSlider frequencySlider;
    private JTextField frequencyField;

    // Slider range stored as integer × 10 to represent one decimal place (0.1 – 10.0)
    private static final int AMPLITUDE_MIN = 1;   // 0.1
    private static final int AMPLITUDE_MAX = 100; // 10.0
    private static final int FREQUENCY_MIN = 1;   // 0.1
    private static final int FREQUENCY_MAX = 100; // 10.0

    private static final double SCALE = 10.0;

    /**
     * Creates a new Wave configuration panel.
     *
     * @param config Configuration data reference.
     */
    public WaveConfigPanel(ConfigData config) {
        super(config);
    }

    /**
     * Initializes all UI components and layout.
     * Builds sliders for amplitude scalar and frequency scalar.
     * Slider positions are initialised from the config's double values
     * multiplied by 10 so the integer range (1–100) maps to 0.1–10.0.
     */
    @Override
    public void initializeComponents() {
        JPanel contentPanel = PanelHelper.createConfigPanelContainer(this);

        // === AMPLITUDE SCALAR SECTION ===
        contentPanel.add(LabelHelper.createConfigTitle(
            "Amplitude scalar: controls the peak displacement of the sine wave relative to the kernel size" +
            " (amplitude = kernelSize / amplitudeScalar). Smaller values produce taller waves." +
            " Slider range 1–100 represents 0.1–10.0.",
            availableWidth
        ));

        int amplitudeInitial = (int) Math.round(config.amplitudeScalar * SCALE);

        Component[] amplitudeComponents = SliderHelper.createSliderPanel(
            "Amplitude Scalar (0.1 – 10.0)",
            AMPLITUDE_MIN, AMPLITUDE_MAX, amplitudeInitial,
            BG_COLOR, FG_COLOR
        );

        JPanel amplitudePanel = (JPanel) amplitudeComponents[0];
        PanelHelper.setupFullWidth(amplitudePanel);

        amplitudeSlider = (JSlider) amplitudeComponents[1];
        amplitudeField  = (JTextField) amplitudeComponents[2];

        contentPanel.add(amplitudePanel);

        contentPanel.add(PanelHelper.createVerticalSpace(20));
        contentPanel.add(PanelHelper.createDivider());
        contentPanel.add(PanelHelper.createVerticalSpace(20));

        // === FREQUENCY SCALAR SECTION ===
        contentPanel.add(LabelHelper.createConfigTitle(
            "Frequency scalar: controls how tightly the sine wave oscillates relative to the kernel size" +
            " (period = kernelSize × frequencyScalar). Larger values produce more widely spaced waves." +
            " Slider range 1–100 represents 0.1–10.0.",
            availableWidth
        ));

        int frequencyInitial = (int) Math.round(config.frequencyScalar * SCALE);

        Component[] frequencyComponents = SliderHelper.createSliderPanel(
            "Frequency Scalar (0.1 – 10.0)",
            FREQUENCY_MIN, FREQUENCY_MAX, frequencyInitial,
            BG_COLOR, FG_COLOR
        );

        JPanel frequencyPanel = (JPanel) frequencyComponents[0];
        PanelHelper.setupFullWidth(frequencyPanel);

        frequencySlider = (JSlider) frequencyComponents[1];
        frequencyField  = (JTextField) frequencyComponents[2];

        contentPanel.add(frequencyPanel);

        contentPanel.add(PanelHelper.createVerticalSpace(20));
        contentPanel.add(PanelHelper.createDivider());
        contentPanel.add(PanelHelper.createVerticalSpace(20));
    }

    /**
     * Applies the current UI state to the underlying configuration.
     * Converts the integer slider value back to a double by dividing by 10.
     */
    @Override
    public void applyConfig() {
        config.amplitudeScalar = amplitudeSlider.getValue() / SCALE;
        config.frequencyScalar = frequencySlider.getValue() / SCALE;
    }

    /**
     * Returns the display name shown in UI selectors.
     *
     * @return The display name for this configuration panel.
     */
    @Override
    public String getDisplayName() {
        return "Wave Configuration";
    }

    /**
     * Enables or disables all interactive components.
     *
     * @param enabled True to enable all components, false to disable them.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        amplitudeSlider.setEnabled(enabled);
        amplitudeField.setEnabled(enabled);
        frequencySlider.setEnabled(enabled);
        frequencyField.setEnabled(enabled);
    }
}