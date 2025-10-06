package Windows;

import Data.ConfigData;
import Data.OpType;
import Data.TYPE;
import Halftone.Operations;

import javax.swing.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class DropDownWindow {
    // Interface components
    private JFrame frame;
    private JLabel dropLabel;
    
    private final JButton colorPicker1 = new JButton();
    private final JButton colorPicker2 = new JButton();

    private JSlider sliderSize;
    private JTextField valueFieldSize;

    private JSlider sliderAngle;
    private JTextField valueFieldAngle;

    private final JButton reflectButton = new JButton("⟳");

    private JComboBox<TYPE> typeComboBox;
    private JComboBox<OpType> opTypeComboBox;

    // Initial states
    private final int[] limitsPolySides = {3, 8};
    private final ConfigData config = new ConfigData(
        15, // config.scale
        45, // config.angle
        TYPE.values()[0], // config.type
        OpType.values()[0], // config.opType
        new Color[]{Color.WHITE, Color.BLACK}, // config.colors
        3 // polygonSides default
    );
    
    private boolean loading = false;
    private final Font defaultFont = UIManager.getDefaults().getFont("Label.font");

    public DropDownWindow() {
        initFrame();
        initDropLabel();
        initTypeAndOpTypeComboBoxes();
        initSlidersAndControls();
        finalizeFrame();
    }

    private void initFrame() {
        frame = new JFrame("Halftone");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
    }

    private void initDropLabel() {
        dropLabel = new JLabel("Drop IMAGE files here", SwingConstants.CENTER);
        dropLabel.setPreferredSize(new Dimension(300, 200));
        dropLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        dropLabel.setForeground(Color.WHITE);
        dropLabel.setOpaque(true);
        dropLabel.setBackground(Color.BLACK);
        dropLabel.setTransferHandler(createTransferHandler());

        frame.add(dropLabel, BorderLayout.CENTER);
    }

    private TransferHandler createTransferHandler() {
        return new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return !loading && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }

                try {
                    List<File> files = (List<File>) support.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);

                    for (File file : files) {
                        String name = file.getName().toLowerCase();
                        
                        if (!name.endsWith(".png") && !name.endsWith(".jpg") && !name.endsWith(".jpeg")) {
                            showError("Incorrect image format, use: png, jpg or jpeg");
                            
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
    
    private void setLoadingState(boolean state) {
        loading = state;

        toggleControls(!state);

        frame.repaint();
    }
    
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
    }

    private void processFiles(List<File> files) {
        final int total = files.size();
        dropLabel.setText("LOADING (1/" + total + ")");

        setLoadingState(true);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws InterruptedException, InvocationTargetException {
                // if Polygons, open dialog and get value (dialog is modal and changes object value)
                if (config.type == TYPE.Polygons) {
                    // blocking call, returns Integer or null if cancelled
                    Integer sides = GenericInputDialog.showSliderIntDialog(frame, "Polygon Sides", limitsPolySides[0], limitsPolySides[1], config.polySides);
                    
                    if (sides != null) {
                        config.polySides = sides;
                    }
                }
                
                Operations op = new Operations(config);

                for (int i = 0; i < total; i++) {
                    final File file = files.get(i);
                    final int num = i + 1;

                    try {
                        op.startProcess(file.getPath());
                    } catch (Exception ex) {
                        ex.printStackTrace();

                        SwingUtilities.invokeAndWait(() -> {
                            showError("Error processing file (" + num + "/" + total + "): " + file.getName());
                        });

                        break;
                    }
                    
                    // If the user chose to skip displaying and not save, stop processing further files
                    if (op.save == false && op.skip == true) {
                        break;
                    }

                    publish(i + 2);
                }

                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
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

    private void onProcessingComplete() {
        dropLabel.setText("Images Generated");

        Timer resetTimer = new Timer(1000, e -> {
            dropLabel.setText("Drop IMAGE files here");
            setLoadingState(false);
        });

        resetTimer.setRepeats(false);
        resetTimer.start();
    }

    // Initialize both TYPE and OpType ComboBoxes to occupy full width
    private void initTypeAndOpTypeComboBoxes() {
        // TYPE ComboBox setup
        typeComboBox = new JComboBox<>(TYPE.values());
        typeComboBox.setSelectedItem(config.type);
        typeComboBox.setBackground(Color.BLACK);
        typeComboBox.setForeground(Color.WHITE);
        typeComboBox.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        typeComboBox.addActionListener(e -> {
            if (!loading) {
                TYPE selected = (TYPE) typeComboBox.getSelectedItem();
                config.type = selected;
            }
        });
        customizeComboBoxUI(typeComboBox);

        // OpType ComboBox setup
        opTypeComboBox = new JComboBox<>(OpType.values());
        opTypeComboBox.setSelectedItem(config.opType);
        opTypeComboBox.setBackground(Color.BLACK);
        opTypeComboBox.setForeground(Color.WHITE);
        opTypeComboBox.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        opTypeComboBox.addActionListener(e -> {
            if (!loading) {
                config.opType = (OpType) opTypeComboBox.getSelectedItem();
            }
        });
        customizeComboBoxUI(opTypeComboBox);

        // Panel to hold both ComboBoxes vertically
        JPanel comboPanel = new JPanel();
        comboPanel.setBackground(Color.BLACK);
        comboPanel.setLayout(new GridLayout(1, 2, 0, 0)); // 1 row, 2 columns

        // Add TYPE first, then OpType
        comboPanel.add(typeComboBox);
        comboPanel.add(opTypeComboBox);

        // Make sure the panel stretches horizontally in BorderLayout.NORTH
        frame.add(comboPanel, BorderLayout.NORTH);
    }

    private <E extends Enum<E>> void customizeComboBoxUI(JComboBox<E> comboBox) {
        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected ComboBoxEditor createEditor() {
                ComboBoxEditor editor = super.createEditor();
                Component editorComponent = editor.getEditorComponent();
                editorComponent.setBackground(Color.BLACK);
                editorComponent.setForeground(Color.WHITE);

                return editor;
            }

            @Override
            protected ComboPopup createPopup() {
                BasicComboPopup popup = (BasicComboPopup) super.createPopup();
                popup.getList().setBackground(Color.BLACK);
                popup.getList().setForeground(Color.WHITE);
                popup.getList().setSelectionBackground(Color.WHITE);
                popup.getList().setSelectionForeground(Color.BLACK);
                popup.setBorder(BorderFactory.createLineBorder(Color.WHITE));

                JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                bar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.WHITE));

                bar.setUI(new BasicScrollBarUI() {
                    @Override
                    protected JButton createDecreaseButton(int orientation) {
                        return createArrowButton(SwingConstants.NORTH);
                    }

                    @Override
                    protected JButton createIncreaseButton(int orientation) {
                        return createArrowButton(SwingConstants.SOUTH);
                    }

                    @Override
                    protected void configureScrollBarColors() {
                        this.thumbColor = Color.WHITE;
                        this.trackColor = Color.BLACK;
                    }
                });

                return popup;
            }

            @Override
            protected JButton createArrowButton() {
                BasicArrowButton arrow = new BasicArrowButton(
                        SwingConstants.SOUTH,
                        Color.BLACK,
                        Color.WHITE,
                        Color.WHITE,
                        Color.BLACK
                );

                arrow.setBorder(BorderFactory.createEmptyBorder());
                return arrow;
            }

            protected JButton createArrowButton(int direction) {
                BasicArrowButton arrow = new BasicArrowButton(
                        direction,
                        Color.BLACK,
                        Color.WHITE,
                        Color.WHITE,
                        Color.BLACK
                );

                arrow.setBorder(BorderFactory.createEmptyBorder());
                return arrow;
            }
        });
    }

    private void initSlidersAndControls() {
        // ----- Scale Slider -----
        sliderSize = new JSlider(JSlider.HORIZONTAL, 0, 100, config.scale);
        sliderSize.setMajorTickSpacing(25);
        sliderSize.setMinorTickSpacing(10);
        sliderSize.setPaintTicks(true);
        sliderSize.setPaintLabels(true);
        sliderSize.setBackground(Color.BLACK);
        sliderSize.setForeground(Color.WHITE);

        valueFieldSize = new JTextField(String.valueOf(config.scale));
        valueFieldSize.setForeground(Color.WHITE);
        valueFieldSize.setBackground(Color.BLACK);
        valueFieldSize.setFont(defaultFont);
        valueFieldSize.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        valueFieldSize.setPreferredSize(new Dimension(50, 20));

        sliderSize.addChangeListener(e -> {
            if (!loading) {
                config.scale = sliderSize.getValue();
                valueFieldSize.setText(String.valueOf(config.scale));
            }
        });

        valueFieldSize.addActionListener(e -> {
            if (!loading) {
                String text = valueFieldSize.getText().trim();
                
                if (!text.isEmpty()) {
                    text = text.substring(0, Math.min(text.length(), 3));
                    int value = Integer.parseInt(text);
                    value = Math.max(0, Math.min(100, value));
                    
                    sliderSize.setValue(value);
                    valueFieldSize.setText(String.valueOf(value));
                } else {
                    valueFieldSize.setText(String.valueOf(sliderSize.getValue()));
                }
                
                valueFieldSize.transferFocus();
            }
        });

        JPanel sliderPanelSize = new JPanel(new BorderLayout());
        sliderPanelSize.setBackground(Color.BLACK);

        JLabel sizeLabel = new JLabel("Scale (0px - 100px)", SwingConstants.LEFT);
        sizeLabel.setForeground(Color.WHITE);
        sizeLabel.setBackground(Color.BLACK);
        sizeLabel.setOpaque(true);

        sliderPanelSize.add(sizeLabel, BorderLayout.NORTH);
        sliderPanelSize.add(sliderSize, BorderLayout.WEST);
        sliderPanelSize.add(valueFieldSize, BorderLayout.EAST);

        // ----- Angle Slider -----
        sliderAngle = new JSlider(JSlider.HORIZONTAL, 0, 360, config.angle);
        sliderAngle.setMajorTickSpacing(90);
        sliderAngle.setMinorTickSpacing(45);
        sliderAngle.setPaintTicks(true);
        sliderAngle.setPaintLabels(true);
        sliderAngle.setBackground(Color.BLACK);
        sliderAngle.setForeground(Color.WHITE);

        valueFieldAngle = new JTextField(String.valueOf(config.angle));
        valueFieldAngle.setForeground(Color.WHITE);
        valueFieldAngle.setBackground(Color.BLACK);
        valueFieldAngle.setFont(defaultFont);
        valueFieldAngle.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        valueFieldAngle.setPreferredSize(new Dimension(50, 20));

        sliderAngle.addChangeListener(e -> {
            if (!loading) {
                config.angle = sliderAngle.getValue();
                valueFieldAngle.setText(String.valueOf(config.angle));
            }
        });

        valueFieldAngle.addActionListener(e -> {
            if (!loading) {
                String text = valueFieldAngle.getText().trim();
                
                if (!text.isEmpty()) {
                    text = text.substring(0, Math.min(text.length(), 3));
                    int value = Integer.parseInt(text);
                    value = Math.max(0, Math.min(360, value));
                    
                    sliderAngle.setValue(value);
                    valueFieldAngle.setText(String.valueOf(value));
                } else {
                    valueFieldAngle.setText(String.valueOf(sliderAngle.getValue()));
                }
                
                valueFieldAngle.transferFocus();
            }
        });

        JPanel sliderPanelAngle = new JPanel(new BorderLayout());
        sliderPanelAngle.setBackground(Color.BLACK);

        JLabel angleLabel = new JLabel("Angle (0° - 360°)", SwingConstants.LEFT);
        angleLabel.setForeground(Color.WHITE);
        angleLabel.setBackground(Color.BLACK);
        angleLabel.setOpaque(true);

        sliderPanelAngle.add(angleLabel, BorderLayout.NORTH);
        sliderPanelAngle.add(sliderAngle, BorderLayout.WEST);
        sliderPanelAngle.add(valueFieldAngle, BorderLayout.EAST);

        // ----- Color Pickers -----
        setButtonsVisualsColors(colorPicker1, config.colors[0]);
        setButtonsVisualsColors(colorPicker2, config.colors[1]);

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

        JPanel colorPanel = new JPanel(new BorderLayout());
        colorPanel.setBackground(Color.BLACK);
        colorPanel.add(colorPicker1, BorderLayout.NORTH);
        colorPanel.add(colorPicker2, BorderLayout.SOUTH);

        // ----- Reflect Button -----
        setButtonVisuals(reflectButton, 50, 50);
        
        reflectButton.addActionListener(e -> {
            int newAngle = ((config.angle % 360) + 360) % 360;
            newAngle = (180 - newAngle + 360) % 360;
            
            sliderAngle.setValue(newAngle);
        });
        
        // ----- Panel for Buttons -----
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(reflectButton);

        // ----- General Panel for Controls -----
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        controlPanel.setBackground(Color.BLACK);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.add(colorPanel);
        controlPanel.add(sliderPanelSize);
        controlPanel.add(sliderPanelAngle);
        controlPanel.add(buttonPanel);

        frame.add(controlPanel, BorderLayout.SOUTH);
    }
    
    private void setButtonVisuals(JButton button, int width, int height) {
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(width, height));
    }

    private void setButtonsVisualsColors(JButton button, Color color) {
        button.setBackground(color);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(40, 40));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void finalizeFrame() {
        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int xPos = (screenSize.width - frame.getWidth()) / 2;
        int yPos = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(xPos, yPos);
        frame.setVisible(true);
    }
}