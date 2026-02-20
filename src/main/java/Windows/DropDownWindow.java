package Windows;

import Data.ConfigData;
import Data.OpType;
import Data.TYPE;
import Halftone.Operations;
import Windows.Util.UI.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static Windows.Util.UI.UIConstants.*;

/**
 * Main application window.
 * Accepts image files via drag-and-drop, exposes halftone configuration controls,
 * and dispatches processing to {@link Operations} on a background thread.
 */
public class DropDownWindow {
    private JFrame frame;
    private JLabel dropLabel;

    private final JButton colorPicker1 = new JButton();
    private final JButton colorPicker2 = new JButton();

    private JSlider sliderSize;
    private JTextField valueFieldSize;

    private JSlider sliderAngle;
    private JTextField valueFieldAngle;

    private final JButton reflectButton = new JButton("⟳");
    private final JButton advancedConfigButton = new JButton("⚙");

    private JComboBox<TYPE> typeComboBox;
    private JComboBox<OpType> opTypeComboBox;

    private final ConfigData config = new ConfigData();

    // Prevents control interaction while processing is in progress
    private boolean loading = false;

    private static final int FRAME_WIDTH = 700;
    private static final int DROP_AREA_HEIGHT = 200;

    /**
     * Creates and displays the main application window.
     * Initializes all UI components and makes the frame visible.
     */
    public DropDownWindow() {
        initFrame();
        initDropLabel();
        initTypeAndOpTypeComboBoxes();
        initSlidersAndControls();
        finalizeFrame();
    }

    // ===== Private helpers =====

    // Creates the root JFrame with a fixed size and BorderLayout
    private void initFrame() {
        frame = new JFrame("Halftone");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
    }

    // Creates the central drop area label and attaches the file drag-and-drop handler
    private void initDropLabel() {
        dropLabel = LabelHelper.createLabel(
            "Drop IMAGE files here",
            BG_COLOR, FG_COLOR,
            SwingConstants.CENTER
        );
        dropLabel.setPreferredSize(new Dimension(FRAME_WIDTH, DROP_AREA_HEIGHT));
        dropLabel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        dropLabel.setTransferHandler(createTransferHandler());
        frame.add(dropLabel, BorderLayout.CENTER);
    }

    // Creates the type and operation-type combo boxes and adds them to the top of the frame
    private void initTypeAndOpTypeComboBoxes() {
        typeComboBox = new JComboBox<>(TYPE.values());
        typeComboBox.setSelectedItem(config.type);
        ComboBoxHelper.styleComboBox(typeComboBox, BG_COLOR, FG_COLOR, true);
        ComboBoxHelper.customizeComboBoxUI(typeComboBox);
        typeComboBox.addActionListener(e -> {
            if (!loading) config.type = (TYPE) typeComboBox.getSelectedItem();
        });

        opTypeComboBox = new JComboBox<>(OpType.values());
        opTypeComboBox.setSelectedItem(config.opType);
        ComboBoxHelper.styleComboBox(opTypeComboBox, BG_COLOR, FG_COLOR, true);
        ComboBoxHelper.customizeComboBoxUI(opTypeComboBox);
        opTypeComboBox.addActionListener(e -> {
            if (!loading) config.opType = (OpType) opTypeComboBox.getSelectedItem();
        });

        // Place both combo boxes side-by-side at the top
        JPanel comboPanel = PanelHelper.createPanel(new GridLayout(1, 2, 0, 0), BG_COLOR, false);
        comboPanel.add(typeComboBox);
        comboPanel.add(opTypeComboBox);

        frame.add(comboPanel, BorderLayout.NORTH);
    }

    // Builds the bottom control bar: scale slider, angle slider, color pickers, and action buttons
    private void initSlidersAndControls() {
        // Scale slider
        Component[] sizeComponents = SliderHelper.createSliderPanel(
            "Scale (0px - 100px)",
            0, 100, config.scale,
            BG_COLOR, FG_COLOR
        );

        sliderSize = (JSlider) sizeComponents[1];
        valueFieldSize = (JTextField) sizeComponents[2];

        sliderSize.addChangeListener(e -> {
            if (!loading) config.scale = sliderSize.getValue();
        });

        // Angle slider
        Component[] angleComponents = SliderHelper.createSliderPanel(
            "Angle (0° - 360°)",
            0, 360, config.angle,
            BG_COLOR, FG_COLOR
        );

        sliderAngle = (JSlider) angleComponents[1];
        valueFieldAngle = (JTextField) angleComponents[2];

        sliderAngle.addChangeListener(e -> {
            if (!loading) config.angle = sliderAngle.getValue();
        });

        // Color pickers for background and foreground
        ButtonHelper.styleColorButton(colorPicker1, config.colors[0], true);
        ButtonHelper.styleColorButton(colorPicker2, config.colors[1], true);

        colorPicker1.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(frame, "Choose Color Background", config.colors[0]);
            if (chosen != null) {
                config.colors[0] = chosen;
                colorPicker1.setBackground(chosen);
            }
        });

        colorPicker2.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(frame, "Choose Color Foreground", config.colors[1]);
            if (chosen != null) {
                config.colors[1] = chosen;
                colorPicker2.setBackground(chosen);
            }
        });

        JPanel colorPanel = PanelHelper.createPanel(new BorderLayout(), BG_COLOR, false);
        colorPanel.add(colorPicker1, BorderLayout.NORTH);
        colorPanel.add(colorPicker2, BorderLayout.SOUTH);

        // Reflect button: mirrors the current angle across 180°
        ButtonHelper.styleButton(reflectButton, BG_COLOR, FG_COLOR, true, 50, 50);
        reflectButton.addActionListener(e -> {
            int newAngle = ((config.angle % 360) + 360) % 360;
            newAngle = (180 - newAngle + 360) % 360;
            sliderAngle.setValue(newAngle);
        });

        // Advanced config opens a separate dialog for less common settings
        ButtonHelper.styleButton(advancedConfigButton, BG_COLOR, FG_COLOR, true, 50, 50);
        advancedConfigButton.addActionListener(e -> {
            if (!loading) AdvancedConfigWindow.showDialog(frame, config);
        });

        JPanel buttonPanel = PanelHelper.createPanel(new FlowLayout(FlowLayout.CENTER, 5, 0), BG_COLOR, false);
        buttonPanel.add(reflectButton);
        buttonPanel.add(advancedConfigButton);

        // Assemble all controls into the bottom control bar
        JPanel controlPanel = PanelHelper.createPanel(new FlowLayout(FlowLayout.CENTER, 10, 0), BG_COLOR, false);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.add(colorPanel);
        controlPanel.add((JPanel) sizeComponents[0]);
        controlPanel.add((JPanel) angleComponents[0]);
        controlPanel.add(buttonPanel);

        frame.add(controlPanel, BorderLayout.SOUTH);
    }

    // Packs the frame, centers it on screen, and makes it visible
    private void finalizeFrame() {
        frame.pack();
        WindowHelper.centerWindow(frame);
        frame.setVisible(true);
    }

    // Creates the drag-and-drop handler that validates file types and triggers processing
    private TransferHandler createTransferHandler() {
        return new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                // Block drops while a batch is already running
                return !loading && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;

                try {
                    List<File> files = (List<File>) support.getTransferable()
                        .getTransferData(DataFlavor.javaFileListFlavor);

                    // Validate all file formats before starting any processing
                    for (File file : files) {
                        String name = file.getName().toLowerCase();

                        if (!name.endsWith(".png") && !name.endsWith(".jpg") && !name.endsWith(".jpeg")) {
                            WindowHelper.showError(frame, "Incorrect image format, use: png, jpg or jpeg");
                            return false;
                        }
                    }

                    processFiles(files);
                    return true;
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };
    }

    // Toggles the loading flag and enables/disables all controls accordingly
    private void setLoadingState(boolean state) {
        loading = state;
        toggleControls(!state);
        frame.repaint();
    }

    // Enables or disables all interactive controls as a group
    private void toggleControls(boolean enabled) {
        typeComboBox.setEnabled(enabled);
        opTypeComboBox.setEnabled(enabled);

        sliderSize.setEnabled(enabled);
        valueFieldSize.setEnabled(enabled);
        sliderAngle.setEnabled(enabled);
        valueFieldAngle.setEnabled(enabled);

        colorPicker1.setEnabled(enabled);
        colorPicker2.setEnabled(enabled);

        reflectButton.setEnabled(enabled);
        advancedConfigButton.setEnabled(enabled);
    }

    // Processes a list of image files sequentially on a background thread,
    // updating the drop label with progress and handling per-file errors
    private void processFiles(List<File> files) {
        final int total = files.size();
        dropLabel.setText("LOADING (1/" + total + ")");
        setLoadingState(true);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws InterruptedException, InvocationTargetException {
                Operations op = new Operations(config);

                for (int i = 0; i < total; i++) {
                    final File file = files.get(i);
                    final int num = i + 1;

                    try {
                        op.startProcess(file.getPath());
                    } catch (Exception ex) {
                        ex.printStackTrace();

                        // Show error on the EDT and abort the batch
                        SwingUtilities.invokeAndWait(() ->
                            WindowHelper.showError(frame, "Error processing file (" + num + "/" + total + "): " + file.getName())
                        );

                        break;
                    }

                    // Stop the batch early if the user chose to skip remaining images
                    if (!op.save && op.skip) break;

                    publish(i + 2);
                }

                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                // Show the most recent progress count (last chunk is sufficient)
                int done = chunks.get(chunks.size() - 1);
                dropLabel.setText("LOADING (" + done + "/" + total + ")");
            }

            @Override
            protected void done() {
                onProcessingComplete();
            }
        };

        worker.execute();
    }

    // Briefly shows a completion message then resets the drop label and re-enables controls
    private void onProcessingComplete() {
        dropLabel.setText("Images Generated");

        Timer resetTimer = new Timer(1000, e -> {
            dropLabel.setText("Drop IMAGE files here");
            setLoadingState(false);
        });

        resetTimer.setRepeats(false);
        resetTimer.start();
    }
}