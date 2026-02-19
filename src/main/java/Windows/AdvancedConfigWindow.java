package Windows;

import Data.ConfigData;
import Windows.Util.UIHelper;
import Windows.Util.ConfigPanelRegistry;
import Data.ConfigPanelEntry;
import Windows.Util.ConfigPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;

/**
 * Advanced configuration window with a panel list on the left
 * and the corresponding configuration panel on the right.
 * Remembers the last selected panel across sessions via a static field.
 */
public class AdvancedConfigWindow extends JDialog {
    private final ConfigData config;
    private final JList<String> configList;
    private final JPanel configPanelContainer;
    private final CardLayout cardLayout;
    private final Map<String, ConfigPanel> configPanels;
    private final JButton applyButton;
    private final JButton cancelButton;

    private static final int WINDOW_WIDTH = 700;
    private static final int WINDOW_HEIGHT = 500;
    private static final int LIST_WIDTH = 180;
    private static final int PADDING = 20;

    // Persists the last selected panel name across dialog instances
    private static String lastSelectedPanel = null;

    /**
     * Creates and initializes the advanced configuration dialog.
     * Restores the previously selected panel if one was recorded.
     *
     * @param parent The parent frame that owns this dialog.
     * @param config ConfigData object to read from and write to.
     */
    public AdvancedConfigWindow(Frame parent, ConfigData config) {
        super(parent, "Advanced Configuration", true);
        this.config = config;
        this.configPanels = new HashMap<>();
        this.cardLayout = new CardLayout();
        this.configPanelContainer = new JPanel(cardLayout);

        this.applyButton  = new JButton("Apply");
        this.cancelButton = new JButton("Cancel");

        // Collect all registered panel display names for the list
        java.util.List<String> configNames = new java.util.ArrayList<>();
        
        for (ConfigPanelEntry entry : ConfigPanelRegistry.getAllPanels()) {
            configNames.add(entry.getDisplayName());
        }

        configList = new JList<>(configNames.toArray(String[]::new));

        // Restore last selection or default to the first entry
        if (lastSelectedPanel != null && configNames.contains(lastSelectedPanel)) {
            configList.setSelectedValue(lastSelectedPanel, true);
        } else {
            configList.setSelectedIndex(0);
        }

        initializeUI();
        loadConfigPanels();

        // Show whichever panel is currently selected in the list
        if (configList.getModel().getSize() > 0) {
            String selectedName = configList.getSelectedValue();
            
            if (selectedName != null) {
                showConfigPanel(selectedName);
            }
        }

        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        UIHelper.centerWindow(this);
    }
    
    /**
     * Convenience method to construct and display the dialog modally.
     *
     * @param parent The parent frame that owns this dialog.
     * @param config ConfigData object to read from and write to.
     */
    public static void showDialog(Frame parent, ConfigData config) {
        AdvancedConfigWindow window = new AdvancedConfigWindow(parent, config);
        window.setVisible(true);
    }

    /**
     * Enables or disables all interactive controls in the window,
     * including the panel list, action buttons, and every config panel.
     *
     * @param enabled True to enable, false to disable.
     */
    public void setControlsEnabled(boolean enabled) {
        configList.setEnabled(enabled);
        applyButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);

        for (ConfigPanel panel : configPanels.values()) {
            panel.setEnabled(enabled);
        }
    }

    // ===== Helper methods =====

    // Assembles the root layout: left panel list, center card container, south buttons
    private void initializeUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIHelper.BG_COLOR);

        add(createLeftPanel(), BorderLayout.WEST);

        UIHelper.stylePanel(configPanelContainer, UIHelper.BG_COLOR, true);
        add(configPanelContainer, BorderLayout.CENTER);

        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    // Builds the left-side panel containing the section title and the config list
    private JPanel createLeftPanel() {
        JPanel panel = UIHelper.createPanel(new BorderLayout(), UIHelper.BG_COLOR, true);
        panel.setPreferredSize(new Dimension(LIST_WIDTH, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIHelper.BORDER_COLOR),
            new EmptyBorder(10, 10, 10, 10)
        ));

        panel.add(
            UIHelper.createSectionTitle("Configuration", SwingConstants.CENTER, 12f, 10),
            BorderLayout.NORTH
        );

        // Apply color inversion on selection to match the application's style
        UIHelper.styleList(
            configList,
            UIHelper.BG_COLOR,
            UIHelper.FG_COLOR,
            UIHelper.FG_COLOR,
            UIHelper.BG_COLOR,
            true
        );

        // Consider extracting to a dedicated ConfigListCellRenderer class.
        configList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

                label.setBorder(new EmptyBorder(5, 10, 5, 10));

                // Invert foreground/background to highlight the selected row
                if (isSelected) {
                    UIHelper.styleLabel(label, UIHelper.FG_COLOR, UIHelper.BG_COLOR);
                } else {
                    UIHelper.styleLabel(label, UIHelper.BG_COLOR, UIHelper.FG_COLOR);
                }

                label.setText((String) value);

                return label;
            }
        });

        // Switch the visible config panel and remember the selection on each list change
        configList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedName = configList.getSelectedValue();
                
                if (selectedName != null) {
                    showConfigPanel(selectedName);
                    lastSelectedPanel = selectedName;
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(configList);
        UIHelper.styleScrollPane(scrollPane, true);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Builds the bottom panel containing the Cancel and Apply buttons
    private JPanel createButtonPanel() {
        JPanel panel = UIHelper.createPanel(
            new FlowLayout(FlowLayout.RIGHT, 10, 10),
            UIHelper.BG_COLOR,
            false
        );

        UIHelper.styleButton(cancelButton, UIHelper.BG_COLOR, UIHelper.FG_COLOR, true);
        cancelButton.addActionListener(e -> dispose());

        // Apply writes all panel configs to ConfigData, then closes the dialog
        UIHelper.styleButton(applyButton, UIHelper.BG_COLOR, UIHelper.FG_COLOR, true);
        applyButton.addActionListener(e -> {
            applyAllConfigs();
            dispose();
        });

        panel.add(cancelButton);
        panel.add(applyButton);

        return panel;
    }

    // Instantiates all registered config panels and adds them to the card layout container
    private void loadConfigPanels() {
        int configPanelWidth = WINDOW_WIDTH - LIST_WIDTH - PADDING;

        for (ConfigPanelEntry entry : ConfigPanelRegistry.getAllPanels()) {
            ConfigPanel panel = entry.createPanel(config);

            panel.setAvailableWidth(configPanelWidth);
            panel.initializeComponents();

            configPanels.put(entry.getDisplayName(), panel);
            configPanelContainer.add(panel, entry.getDisplayName());
        }
    }

    // Flips the card layout to the panel matching the given display name
    private void showConfigPanel(String displayName) {
        cardLayout.show(configPanelContainer, displayName);
    }

    // Iterates all loaded panels and writes their current state back to ConfigData
    private void applyAllConfigs() {
        for (ConfigPanel panel : configPanels.values()) {
            panel.applyConfig();
        }
    }
}