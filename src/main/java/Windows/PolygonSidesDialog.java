package Windows;

import Data.ConfigData;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public class PolygonSidesDialog {

    /**
     * Shows a modal dialog containing only a slider and a text input (no buttons, no ESC).
     * The dialog directly updates config.polySides as the user changes the controls.
     * The dialog stays open until the user closes it (clicking the window close button).
     *
     * Safe to call from any thread: if off-EDT it will schedule on EDT and block until closed.
     *
     * @param parent parent window (may be null)
     * @param config ConfigData instance that will be updated directly (may be null)
     * @param limits int[] where limits[0] = min, limits[1] = max
     */
    public static void showDialog(Frame parent, ConfigData config, int[] limits) {
        Runnable ui = () -> {
            int min = (limits != null && limits.length > 0) ? limits[0] : 3;
            int max = (limits != null && limits.length > 1) ? limits[1] : 8;
            
            if (min > max) {
                int t = min; min = max; max = t;
            }

            int init = (config != null) ? Math.max(min, Math.min(max, config.polySides)) : Math.max(min, Math.min(max, min));

            final int minVal = min;
            final int maxVal = max;
            final int initVal = init;

            final JDialog dlg = new JDialog(parent, "Polygon sides", true);
            dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dlg.setLayout(new BorderLayout());
            dlg.getContentPane().setBackground(Color.BLACK);

            final JSlider sidesSlider = new JSlider(JSlider.HORIZONTAL, minVal, maxVal, initVal);
            sidesSlider.setMajorTickSpacing(1);
            sidesSlider.setPaintTicks(true);
            sidesSlider.setPaintLabels(true);
            sidesSlider.setBackground(Color.BLACK);
            sidesSlider.setForeground(Color.WHITE);

            final JTextField sidesField = new JTextField(String.valueOf(initVal));
            sidesField.setForeground(Color.WHITE);
            sidesField.setBackground(Color.BLACK);
            sidesField.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            sidesField.setPreferredSize(new Dimension(50, 20));

            sidesSlider.addChangeListener(e -> {
                int v = sidesSlider.getValue();
                
                sidesField.setText(String.valueOf(v));
                
                if (config != null) {
                    config.polySides = v;
                }
            });

            sidesField.addActionListener(ae -> {
                String t = sidesField.getText().trim();
                if (!t.isEmpty()) {
                    try {
                        int v = Integer.parseInt(t);
                        
                        v = Math.max(minVal, Math.min(maxVal, v));
                        sidesSlider.setValue(v);
                        sidesField.setText(String.valueOf(v));
                        
                        if (config != null) {
                            config.polySides = v;
                        }
                    } catch (NumberFormatException ex) {
                        sidesField.setText(String.valueOf(sidesSlider.getValue()));
                    }
                } else {
                    sidesField.setText(String.valueOf(sidesSlider.getValue()));
                }
            });

            JLabel label = new JLabel("Sides (" + minVal + " - " + maxVal + ")", SwingConstants.LEFT);
            label.setForeground(Color.WHITE);
            label.setOpaque(true);
            label.setBackground(Color.BLACK);

            JPanel center = new JPanel(new BorderLayout());
            center.setBackground(Color.BLACK);
            center.add(label, BorderLayout.NORTH);
            center.add(sidesSlider, BorderLayout.CENTER);
            center.add(sidesField, BorderLayout.EAST);

            dlg.add(center, BorderLayout.CENTER);
            dlg.pack();
            dlg.setLocationRelativeTo(parent);

            dlg.setVisible(true);
        };

        if (SwingUtilities.isEventDispatchThread()) {
            ui.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(ui);
            } catch (InterruptedException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
    }
}