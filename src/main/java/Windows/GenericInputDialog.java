package Windows;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GenericInputDialog {
    public enum FieldType {
        SLIDER_INT, // slider + numeric text box (int)
        INTEGER_TEXT, // plain integer text field
        TEXT, // plain text field
        CHECKBOX, // boolean checkbox
        OPTIONS_COMBO // JComboBox with provided options (String[])
    }

    /**
     * Descriptor for a single input field.
     * - key: unique id used in result map
     * - label: shown to the user
     * - type: one of FieldType
     * - initValue: initial value (Integer, String, Boolean)
     * - min/max: used for slider only (ints)
     * - options: used for OPTIONS_COMBO (String[])
     */
    public static class FieldDescriptor {
        public final String key;
        public final String label;
        public final FieldType type;
        public final Object initValue; // initial value (Integer, String, Boolean)
        public final int min, max; // used for slider
        public final String[] options; // used for combo

        public FieldDescriptor(String key, String label, FieldType type, Object initValue) {
            this(key, label, type, initValue, 0, 0, null);
        }

        public FieldDescriptor(String key, String label, FieldType type, Object initValue, int min, int max) {
            this(key, label, type, initValue, min, max, null);
        }

        public FieldDescriptor(String key, String label, FieldType type, Object initValue, String[] options) {
            this(key, label, type, initValue, 0, 0, options);
        }

        private FieldDescriptor(String key, String label, FieldType type, Object initValue, int min, int max, String[] options) {
            this.key = Objects.requireNonNull(key);
            this.label = Objects.requireNonNull(label);
            this.type = Objects.requireNonNull(type);
            this.initValue = initValue;
            this.min = min;
            this.max = max;
            this.options = options;
        }
    }

    /**
     * Show a modal dialog constructed from the provided descriptors.
     *
     * @param parent parent frame (may be null)
     * @param windowTitle dialog title
     * @param descriptors list of field descriptors (order preserved)
     * @return LinkedHashMap with values keyed by FieldDescriptor.key, or null if user cancelled/closed dialog
     */
    public static LinkedHashMap<String, Object> showDialog(Frame parent, String windowTitle, List<FieldDescriptor> descriptors) {
        AtomicReference<LinkedHashMap<String, Object>> outRef = new AtomicReference<>(null);

        Runnable ui = new Runnable() {
            @Override
            public void run() {
                // Special-case: a single SLIDER_INT should mimic the original PolygonSidesDialog layout & behaviour
                if (descriptors != null && descriptors.size() == 1 && descriptors.get(0).type == FieldType.SLIDER_INT) {
                    FieldDescriptor fd = descriptors.get(0);

                    int min = fd.min;
                    int max = fd.max;
                    int init = (fd.initValue instanceof Number) ? ((Number) fd.initValue).intValue() : min;
                    init = Math.max(min, Math.min(max, init));

                    final JDialog dlg = new JDialog(parent, windowTitle, true);
                    dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    dlg.getContentPane().setBackground(Color.BLACK);
                    dlg.setLayout(new BorderLayout());

                    final JSlider sidesSlider = new JSlider(JSlider.HORIZONTAL, min, max, init);
                    sidesSlider.setMajorTickSpacing(1);
                    sidesSlider.setPaintTicks(true);
                    sidesSlider.setPaintLabels(true);
                    sidesSlider.setBackground(Color.BLACK);
                    sidesSlider.setForeground(Color.WHITE);

                    final JTextField sidesField = new JTextField(String.valueOf(init));
                    sidesField.setForeground(Color.WHITE);
                    sidesField.setBackground(Color.BLACK);
                    sidesField.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                    sidesField.setPreferredSize(new Dimension(50, 20));

                    sidesSlider.addChangeListener(e -> sidesField.setText(String.valueOf(sidesSlider.getValue())));

                    sidesField.addActionListener(ae -> {
                        String t = sidesField.getText().trim();
                        if (!t.isEmpty()) {
                            try {
                                int v = Integer.parseInt(t);

                                v = Math.max(min, Math.min(max, v));
                                sidesSlider.setValue(v);
                                sidesField.setText(String.valueOf(v));
                            } catch (NumberFormatException ex) {
                                sidesField.setText(String.valueOf(sidesSlider.getValue()));
                            }
                        } else {
                            sidesField.setText(String.valueOf(sidesSlider.getValue()));
                        }
                    });

                    JLabel label = new JLabel(fd.label, SwingConstants.LEFT);
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

                    // Show modally; when closed, return current slider value
                    dlg.setVisible(true);

                    LinkedHashMap<String, Object> result = new LinkedHashMap<>();
                    result.put(fd.key, sidesSlider.getValue());
                    outRef.set(result);
                    return;
                }

                // Generic multi-field form (with OK / Cancel)
                JDialog dlg = new JDialog(parent, windowTitle, true);
                dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dlg.getContentPane().setBackground(Color.BLACK);
                dlg.setLayout(new BorderLayout());

                JPanel form = new JPanel();
                form.setLayout(new GridBagLayout());
                form.setBackground(Color.BLACK);
                form.setBorder(new EmptyBorder(8, 8, 8, 8));

                GridBagConstraints gbcLabel = new GridBagConstraints();
                gbcLabel.gridx = 0;
                gbcLabel.anchor = GridBagConstraints.WEST;
                gbcLabel.insets = new Insets(4, 4, 4, 8);

                GridBagConstraints gbcField = new GridBagConstraints();
                gbcField.gridx = 1;
                gbcField.weightx = 1.0;
                gbcField.fill = GridBagConstraints.HORIZONTAL;
                gbcField.insets = new Insets(4, 0, 4, 4);

                // Keep references to components so we can read their values later
                Map<String, JComponent> componentMap = new LinkedHashMap<>();

                int row = 0;

                for (FieldDescriptor fd : descriptors) {
                    gbcLabel.gridy = row;
                    gbcField.gridy = row;

                    JLabel lbl = new JLabel(fd.label);
                    lbl.setForeground(Color.WHITE);
                    lbl.setOpaque(true);
                    lbl.setBackground(Color.BLACK);
                    form.add(lbl, gbcLabel);

                    JComponent fieldComp = null;

                    switch (fd.type) {
                        case SLIDER_INT -> {
                            int min = fd.min;
                            int max = fd.max;

                            int init = (fd.initValue instanceof Number) ? ((Number) fd.initValue).intValue() : min;
                            init = Math.max(min, Math.min(max, init));

                            JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, init);
                            slider.setMajorTickSpacing(Math.max(1, (max - min) / 4));
                            slider.setPaintTicks(true);
                            slider.setPaintLabels(true);
                            slider.setBackground(Color.BLACK);
                            slider.setForeground(Color.WHITE);

                            JTextField txt = new JTextField(String.valueOf(init));
                            txt.setPreferredSize(new Dimension(60, 24));
                            txt.setBackground(Color.BLACK);
                            txt.setForeground(Color.WHITE);

                            slider.addChangeListener(e -> txt.setText(String.valueOf(slider.getValue())));

                            txt.addActionListener(e -> {
                                try {
                                    int v = Integer.parseInt(txt.getText().trim());

                                    v = Math.max(min, Math.min(max, v));

                                    slider.setValue(v);
                                } catch (NumberFormatException ignored) {
                                    txt.setText(String.valueOf(slider.getValue()));
                                }
                            });

                            JPanel holder = new JPanel(new BorderLayout(6, 0));
                            holder.setBackground(Color.BLACK);
                            holder.add(slider, BorderLayout.CENTER);
                            holder.add(txt, BorderLayout.EAST);

                            fieldComp = holder;

                            componentMap.put(fd.key + "::slider", slider);
                            componentMap.put(fd.key + "::text", txt);
                        }
                        case INTEGER_TEXT -> {
                            JTextField txt = new JTextField((fd.initValue != null) ? String.valueOf(fd.initValue) : "0");
                            txt.setBackground(Color.BLACK);
                            txt.setForeground(Color.WHITE);

                            fieldComp = txt;
                            componentMap.put(fd.key, txt);
                        }
                        case TEXT -> {
                            JTextField txt = new JTextField((fd.initValue != null) ? String.valueOf(fd.initValue) : "");
                            txt.setBackground(Color.BLACK);
                            txt.setForeground(Color.WHITE);

                            fieldComp = txt;
                            componentMap.put(fd.key, txt);
                        }
                        case CHECKBOX -> {
                            boolean init = (fd.initValue instanceof Boolean) && ((Boolean) fd.initValue);

                            JCheckBox cb = new JCheckBox();
                            cb.setSelected(init);
                            cb.setBackground(Color.BLACK);
                            cb.setForeground(Color.WHITE);

                            fieldComp = cb;
                            componentMap.put(fd.key, cb);
                        }
                        case OPTIONS_COMBO -> {
                            String[] opts = (fd.options != null) ? fd.options : new String[0];
                            JComboBox<String> combo = new JComboBox<>(opts);

                            if (fd.initValue != null) combo.setSelectedItem(String.valueOf(fd.initValue));

                            combo.setBackground(Color.BLACK);
                            combo.setForeground(Color.WHITE);

                            fieldComp = combo;
                            componentMap.put(fd.key, combo);
                        }
                        default -> {
                            JTextField txt = new JTextField((fd.initValue != null) ? String.valueOf(fd.initValue) : "");
                            txt.setBackground(Color.BLACK);
                            txt.setForeground(Color.WHITE);

                            fieldComp = txt;
                            componentMap.put(fd.key, txt);
                        }
                    }

                    form.add(fieldComp, gbcField);
                    row++;
                }

                // Buttons
                JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttons.setBackground(Color.BLACK);

                JButton ok = new JButton("OK");
                JButton cancel = new JButton("Cancel");

                ok.addActionListener(e -> {
                    LinkedHashMap<String, Object> result = new LinkedHashMap<>();

                    try {
                        for (FieldDescriptor fd : descriptors) {
                            switch (fd.type) {
                                case SLIDER_INT -> {
                                    JSlider slider = (JSlider) componentMap.get(fd.key + "::slider");
                                    result.put(fd.key, slider.getValue());
                                }
                                case INTEGER_TEXT -> {
                                    JTextField t = (JTextField) componentMap.get(fd.key);

                                    try {
                                        result.put(fd.key, Integer.valueOf(t.getText().trim()));
                                    } catch (NumberFormatException ex) {
                                        result.put(fd.key, 0);
                                    }
                                }
                                case TEXT -> {
                                    JTextField t = (JTextField) componentMap.get(fd.key);
                                    result.put(fd.key, t.getText());
                                }
                                case CHECKBOX -> {
                                    JCheckBox cb = (JCheckBox) componentMap.get(fd.key);
                                    result.put(fd.key, cb.isSelected());
                                }
                                case OPTIONS_COMBO -> {
                                    JComboBox<?> combo = (JComboBox<?>) componentMap.get(fd.key);
                                    result.put(fd.key, combo.getSelectedItem());
                                }
                                default -> {
                                    JComponent c = componentMap.get(fd.key);

                                    if (c instanceof JTextField jTextField) result.put(fd.key, jTextField.getText());

                                    else result.put(fd.key, null);
                                }
                            }
                        }
                    } finally {
                        outRef.set(result);
                        dlg.dispose();
                    }
                });

                cancel.addActionListener(e -> {
                    outRef.set(null);
                    dlg.dispose();
                });

                buttons.add(cancel);
                buttons.add(ok);

                dlg.add(form, BorderLayout.CENTER);
                dlg.add(buttons, BorderLayout.SOUTH);
                dlg.pack();
                dlg.setLocationRelativeTo(parent);
                dlg.setVisible(true);
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            ui.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(ui);
            } catch (InterruptedException | InvocationTargetException ex) {
                ex.printStackTrace();

                return null;
            }
        }

        return outRef.get();
    }

    // Convenience helper to create a simple single-slider dialog (polygon sides example)
    public static Integer showSliderIntDialog(Frame parent, String title, int min, int max, int init) {
        FieldDescriptor fd = new FieldDescriptor("value", title, FieldType.SLIDER_INT, init, min, max);
        LinkedHashMap<String, Object> res = showDialog(parent, title, Arrays.asList(fd));

        if (res == null) return null;

        Object v = res.get("value");

        return (v instanceof Integer) ? (Integer) v : null;
    }
}