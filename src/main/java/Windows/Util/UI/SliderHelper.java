package Windows.Util.UI;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;

import static Windows.Util.UI.UIConstants.*;

/**
 * Factory and styling methods for sliders and slider panel composites.
 */
public final class SliderHelper {
    private SliderHelper() {}

    // Height in pixels for slider tick marks
    private static final int MINOR_TICK_HEIGHT = 2;
    private static final int MAJOR_TICK_HEIGHT = 6;

    // ===== STYLING =====
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

    // ===== FACTORY =====
    /**
     * Create a configured horizontal slider with tick spacing and a fully themed UI.
     *
     * Track      : BG_COLOR fill with BORDER_COLOR outline; portion left of the thumb
     *              filled in FG_COLOR to indicate progress.
     * Thumb      : solid FG_COLOR rectangle with BG_COLOR outline.
     * Minor ticks: MINOR_TICK_HEIGHT px lines drawn downward from the top of the tick
     *              area so they sit between the track and the numeric labels.
     * Major ticks: MAJOR_TICK_HEIGHT px lines drawn the same way, visibly taller.
     * Labels     : inherit FG_COLOR from the slider foreground.
     * Focus      : dotted focus rectangle suppressed (sliders are not Tab-navigable
     *              in this application).
     *
     * @param min Minimum slider value
     * @param max Maximum slider value
     * @param initial Initial slider value
     * @param bgColor Background color
     * @param fgColor Foreground color
     * @return Configured JSlider
     */
    public static JSlider createSlider(int min, int max, int initial,
                                       Color bgColor, Color fgColor) {
        int range = max - min;
        JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, initial);
        slider.setMajorTickSpacing(Math.max(1, range / 4));
        slider.setMinorTickSpacing(Math.max(1, range / 10));
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        styleSlider(slider, bgColor, fgColor);

        slider.setUI(new BasicSliderUI(slider) {
            // ----- Track -----
            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int trackY = trackRect.y + trackRect.height / 2 - 2;
                int trackH = 4;

                // Empty portion
                g2.setColor(bgColor);
                g2.fillRect(trackRect.x, trackY, trackRect.width, trackH);

                // Border around the whole track
                g2.setColor(BORDER_COLOR);
                g2.drawRect(trackRect.x, trackY, trackRect.width - 1, trackH - 1);

                // Filled portion (min → thumb centre)
                int fillWidth = thumbRect.x + thumbRect.width / 2 - trackRect.x;
                
                if (fillWidth > 0) {
                    g2.setColor(fgColor);
                    g2.fillRect(trackRect.x, trackY, fillWidth, trackH);
                }

                g2.dispose();
            }

            // ----- Thumb -----
            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Solid fill
                g2.setColor(fgColor);
                g2.fillRect(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);

                // Thin outline so the thumb edge is visible against the filled track
                g2.setColor(bgColor);
                g2.drawRect(thumbRect.x, thumbRect.y, thumbRect.width - 1, thumbRect.height - 1);

                g2.dispose();
            }

            // ----- Ticks -----
            @Override
            protected void paintMinorTickForHorizSlider(Graphics g, Rectangle tickBounds, int x) {
                g.setColor(BORDER_COLOR);
                // Draw downward from the top of the tick area (adjacent to the track).
                // The label area sits entirely below tickBounds, so ticks drawn here
                // are always between the track and the numeric labels.
                g.drawLine(x, 0, x, MINOR_TICK_HEIGHT);
            }

            @Override
            protected void paintMajorTickForHorizSlider(Graphics g, Rectangle tickBounds, int x) {
                g.setColor(BORDER_COLOR);
                g.drawLine(x, 0, x, MAJOR_TICK_HEIGHT);
            }

            // ----- Focus -----
            /** Suppress the default dotted focus rectangle. */
            @Override
            public void paintFocus(Graphics g) {
                // Intentionally empty.
            }
        });

        return slider;
    }

    /**
     * Create a composite panel containing a label, a slider, and a text field
     * that mirrors and accepts the slider value.
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
        JTextField valueField = TextFieldHelper.createTextField(String.valueOf(initial), bgColor, fgColor);

        slider.addChangeListener(e -> valueField.setText(String.valueOf(slider.getValue())));

        valueField.addActionListener(e -> {
            String text = valueField.getText().trim();
            
            try {
                int value = text.isEmpty() ? slider.getValue()
                        : Math.max(min, Math.min(max, Integer.parseInt(text)));
                slider.setValue(value);
                valueField.setText(String.valueOf(value));
            } catch (NumberFormatException ex) {
                valueField.setText(String.valueOf(slider.getValue()));
            }
            valueField.transferFocus();
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bgColor);
        panel.add(LabelHelper.createLabel(labelText, bgColor, fgColor, SwingConstants.LEFT), BorderLayout.NORTH);
        panel.add(slider,     BorderLayout.CENTER);
        panel.add(valueField, BorderLayout.EAST);

        return new Component[]{panel, slider, valueField};
    }
}