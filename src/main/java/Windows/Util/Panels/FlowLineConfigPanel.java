package Windows.Util.Panels;

import Data.ConfigData;
import Windows.Util.ConfigPanel;
import Windows.Util.UI.*;

import javax.swing.*;
import java.awt.*;

import static Windows.Util.UI.UIConstants.*;

/**
 * Configuration panel for FlowLine TYPE.
 * Provides controls to configure minimum and maximum step sizes,
 * an option to follow maximum changes in step size, the line smoothing radius,
 * and the minimum line size.
 */
public class FlowLineConfigPanel extends ConfigPanel {
    private JSlider minStepSlider;
    private JTextField minStepField;
    private JSlider maxStepSlider;
    private JTextField maxStepField;
    private JButton followMaxChangeToggle;
    private JSlider smoothSlider;
    private JTextField smoothField;
    private JSlider minLineSizeSlider;
    private JTextField minLineSizeField;

    private boolean followMaxChangeState;

    private static final int MIN_STEP_SIZE = 1;
    private static final int MAX_STEP_SIZE = 32;
    private static final int MIN_SMOOTH_RADIUS = 0;
    private static final int MAX_SMOOTH_RADIUS = 10;
    private static final int MIN_LINE_SIZE_MIN = 1;
    private static final int MIN_LINE_SIZE_MAX = 10;

    /**
     * Creates a new FlowLine configuration panel.
     *
     * @param config Configuration data reference.
     */
    public FlowLineConfigPanel(ConfigData config) {
        super(config);
        this.followMaxChangeState = config.followMaxChange;
    }

    /**
     * Initializes all UI components and layout.
     * Builds min/max step sliders with cross-validation, a smoothing radius slider,
     * a minimum line size slider, and a follow-max-change toggle placed into the
     * standard config container.
     */
    @Override
    public void initializeComponents() {
        JPanel contentPanel = PanelHelper.createConfigPanelContainer(this);

        // === MIN/MAX STEP SIZE SECTION ===
        contentPanel.add(LabelHelper.createConfigTitle(
            "Step size range based on kernel magnitude (min cannot exceed max, and max cannot be less than min):",
            availableWidth
        ));

        Component[] minComponents = SliderHelper.createSliderPanel(
            "Minimum Step Size (" + MIN_STEP_SIZE + "-" + MAX_STEP_SIZE + ")",
            MIN_STEP_SIZE, MAX_STEP_SIZE, config.minStep,
            BG_COLOR, FG_COLOR
        );

        JPanel minPanel = (JPanel) minComponents[0];
        PanelHelper.setupFullWidth(minPanel);
        minPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        minStepSlider = (JSlider) minComponents[1];
        minStepField = (JTextField) minComponents[2];

        Component[] maxComponents = SliderHelper.createSliderPanel(
            "Maximum Step Size (" + MIN_STEP_SIZE + "-" + MAX_STEP_SIZE + ")",
            MIN_STEP_SIZE, MAX_STEP_SIZE, config.maxStep,
            BG_COLOR, FG_COLOR
        );

        JPanel maxPanel = (JPanel) maxComponents[0];
        PanelHelper.setupFullWidth(maxPanel);

        maxStepSlider = (JSlider) maxComponents[1];
        maxStepField = (JTextField) maxComponents[2];

        // Cross-validation: min cannot exceed max and vice versa
        minStepSlider.addChangeListener(e -> {
            if (minStepSlider.getValue() > maxStepSlider.getValue()) {
                maxStepSlider.setValue(minStepSlider.getValue());
            }
        });

        maxStepSlider.addChangeListener(e -> {
            if (maxStepSlider.getValue() < minStepSlider.getValue()) {
                minStepSlider.setValue(maxStepSlider.getValue());
            }
        });

        contentPanel.add(minPanel);
        contentPanel.add(maxPanel);

        contentPanel.add(PanelHelper.createVerticalSpace(20));
        contentPanel.add(PanelHelper.createDivider());
        contentPanel.add(PanelHelper.createVerticalSpace(20));

        // === SMOOTHING RADIUS SECTION ===
        contentPanel.add(LabelHelper.createConfigTitle(
            "Number of neighboring kernels used to smooth line width:",
            availableWidth
        ));

        Component[] smoothComponents = SliderHelper.createSliderPanel(
            "Smooth Radius (" + MIN_SMOOTH_RADIUS + "-" + MAX_SMOOTH_RADIUS + ")",
            MIN_SMOOTH_RADIUS, MAX_SMOOTH_RADIUS, config.flowLineSmoothRadius,
            BG_COLOR, FG_COLOR
        );

        JPanel smoothPanel = (JPanel) smoothComponents[0];
        PanelHelper.setupFullWidth(smoothPanel);

        smoothSlider = (JSlider) smoothComponents[1];
        smoothField = (JTextField) smoothComponents[2];

        contentPanel.add(smoothPanel);

        contentPanel.add(PanelHelper.createVerticalSpace(20));
        contentPanel.add(PanelHelper.createDivider());
        contentPanel.add(PanelHelper.createVerticalSpace(20));

        // === MINIMUM LINE SIZE SECTION ===
        contentPanel.add(LabelHelper.createConfigTitle(
            "Minimum number of kernels a flow line must have to be rendered:",
            availableWidth
        ));

        Component[] minLineSizeComponents = SliderHelper.createSliderPanel(
            "Minimum Line Size (" + MIN_LINE_SIZE_MIN + "-" + MIN_LINE_SIZE_MAX + ")",
            MIN_LINE_SIZE_MIN, MIN_LINE_SIZE_MAX, config.minLineSize,
            BG_COLOR, FG_COLOR
        );

        JPanel minLineSizePanel = (JPanel) minLineSizeComponents[0];
        PanelHelper.setupFullWidth(minLineSizePanel);

        minLineSizeSlider = (JSlider) minLineSizeComponents[1];
        minLineSizeField = (JTextField) minLineSizeComponents[2];

        contentPanel.add(minLineSizePanel);

        contentPanel.add(PanelHelper.createVerticalSpace(20));
        contentPanel.add(PanelHelper.createDivider());
        contentPanel.add(PanelHelper.createVerticalSpace(20));

        // === FOLLOW MAX CHANGE SECTION ===
        contentPanel.add(LabelHelper.createConfigTitle(
            "Direction of flow:",
            availableWidth
        ));

        Object[] toggleComponents = ButtonHelper.createToggleButton(
            followMaxChangeState,
            () -> followMaxChangeState = !followMaxChangeState
        );

        followMaxChangeToggle = (JButton) toggleComponents[0];
        contentPanel.add(PanelHelper.createLabeledInputRow("Follow Max Change:", followMaxChangeToggle));
    }

    /**
     * Applies the current UI state to the underlying configuration.
     */
    @Override
    public void applyConfig() {
        config.minStep = minStepSlider.getValue();
        config.maxStep = maxStepSlider.getValue();
        config.flowLineSmoothRadius = smoothSlider.getValue();
        config.minLineSize = minLineSizeSlider.getValue();
        config.followMaxChange = followMaxChangeState;
    }

    /**
     * Returns the display name shown in UI selectors.
     *
     * @return The display name for this configuration panel.
     */
    @Override
    public String getDisplayName() {
        return "FlowLine Configuration";
    }

    /**
     * Enables or disables all interactive components.
     *
     * @param enabled True to enable all components, false to disable them.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        minStepSlider.setEnabled(enabled);
        minStepField.setEnabled(enabled);
        maxStepSlider.setEnabled(enabled);
        maxStepField.setEnabled(enabled);
        smoothSlider.setEnabled(enabled);
        smoothField.setEnabled(enabled);
        minLineSizeSlider.setEnabled(enabled);
        minLineSizeField.setEnabled(enabled);
        followMaxChangeToggle.setEnabled(enabled);
    }
}