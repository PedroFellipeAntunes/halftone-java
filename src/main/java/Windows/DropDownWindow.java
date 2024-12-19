package Windows;

import Operation.Operations;
import Operation.TYPE;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class DropDownWindow {
    private JFrame frame;
    private JLabel dropLabel;
    private JSlider sliderSize;
    private JTextField valueFieldSize;
    private JSlider sliderAngle;
    private JTextField valueFieldAngle;
    
    private JButton colorPicker1 = new JButton();
    private JButton colorPicker2 = new JButton();
    private JButton reflectButton = new JButton("⟳");
    
    private JButton dotsButton = new JButton("Dot");
    private JButton linesButton  = new JButton("Line");
    private JButton sineButton = new JButton("Sine");
    
    private JButton[] buttonsType = {dotsButton, linesButton, sineButton};
    
    private Color[] colors = {Color.WHITE, Color.BLACK};
    private int scale = 15;
    private int angle = 45;
    private TYPE type = TYPE.Dots;
    
    private boolean loading = false;
    private Font defaultFont = UIManager.getDefaults().getFont("Label.font");
    
    public DropDownWindow() {
        frame = new JFrame("Halftone");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setLayout(new BorderLayout());
        
        dropLabel = new JLabel("Drop IMAGE files here", SwingConstants.CENTER);
        dropLabel.setPreferredSize(new Dimension(300, 200));
        dropLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        dropLabel.setForeground(Color.WHITE);
        dropLabel.setOpaque(true);
        dropLabel.setBackground(Color.BLACK);
        dropLabel.setTransferHandler(new TransferHandler() {
            public boolean canImport(TransferHandler.TransferSupport support) {
                if (!loading && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    return true;
                }
                
                return false;
            }
            
            public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                
                try {
                    Transferable transferable = support.getTransferable();
                    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    
                    for (File file : files) {
                        if (!file.getName().toLowerCase().endsWith(".png")
                                && !file.getName().toLowerCase().endsWith(".jpg")
                                && !file.getName().toLowerCase().endsWith(".jpeg")) {
                            JOptionPane.showMessageDialog(frame, "Incorrect image format, use: png, jpg or jpeg", "Error", JOptionPane.ERROR_MESSAGE);
                            
                            return false;
                        }
                    }
                    
                    dropLabel.setText("LOADING (1/" + files.size() + ")");
                    loading = true;
                    
                    frame.repaint();
                    
                    new Thread(() -> {
                        int filesProcessed = 1;
                        
                        for (File file : files) {
                            Operations.processFile(file.getPath(), scale, angle, type, colors);
                            
                            filesProcessed++;
                            
                            final int finalFilesProcessed = filesProcessed;
                            
                            SwingUtilities.invokeLater(() -> {
                                dropLabel.setText("LOADING (" + finalFilesProcessed + "/" + files.size() + ")");
                            });
                        }
                        
                        SwingUtilities.invokeLater(() -> {
                            dropLabel.setText("Images Generated");
                            
                            Timer resetTimer = new Timer(1000, e2 -> {
                                dropLabel.setText("Drop IMAGE files here");
                                loading = false;
                            });
                            
                            resetTimer.setRepeats(false);
                            resetTimer.start();
                        });
                    }).start();
                    
                    return true;
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });
        
        //Slider
        sliderSize = new JSlider(JSlider.HORIZONTAL, 0, 100, scale);
        sliderSize.setMajorTickSpacing(25);
        sliderSize.setMinorTickSpacing(10);
        sliderSize.setPaintTicks(true);
        sliderSize.setPaintLabels(true);
        sliderSize.setBackground(Color.BLACK);
        sliderSize.setForeground(Color.WHITE);
        
        //Value of sliderSize
        valueFieldSize = new JTextField();
        valueFieldSize.setForeground(Color.WHITE);
        valueFieldSize.setBackground(Color.BLACK);
        valueFieldSize.setFont(defaultFont);
        valueFieldSize.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        valueFieldSize.setText(String.valueOf(sliderSize.getValue()));
        valueFieldSize.setPreferredSize(new Dimension(50, 20));
        
        valueFieldSize.addActionListener(e -> {
            if (!loading) {
                String text = valueFieldSize.getText();
                
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
        
        sliderSize.addChangeListener(e -> {
            if (!loading) {
                scale = sliderSize.getValue();
                
                valueFieldSize.setText(String.valueOf(scale));
            }
        });
        
        //Panel with sliderScale and value
        JPanel sliderPanelSize = new JPanel(new BorderLayout());
        JLabel scaleLabel = new JLabel("Scale (0px - 100px)", SwingConstants.LEFT);
        scaleLabel.setBackground(Color.BLACK);
        scaleLabel.setForeground(Color.WHITE);
        scaleLabel.setOpaque(true);
        sliderPanelSize.add(scaleLabel, BorderLayout.NORTH);
        sliderPanelSize.add(sliderSize, BorderLayout.WEST);
        sliderPanelSize.add(valueFieldSize, BorderLayout.EAST);
        
        //SliderSize
        sliderAngle = new JSlider(JSlider.HORIZONTAL, 0, 360, angle);
        sliderAngle.setMajorTickSpacing(90);
        sliderAngle.setMinorTickSpacing(45);
        sliderAngle.setPaintTicks(true);
        sliderAngle.setPaintLabels(true);
        sliderAngle.setBackground(Color.BLACK);
        sliderAngle.setForeground(Color.WHITE);
        
        //Value of sliderAngle
        valueFieldAngle = new JTextField();
        valueFieldAngle.setForeground(Color.WHITE);
        valueFieldAngle.setBackground(Color.BLACK);
        valueFieldAngle.setFont(defaultFont);
        valueFieldAngle.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        valueFieldAngle.setText(String.valueOf(sliderAngle.getValue()));
        valueFieldAngle.setPreferredSize(new Dimension(50, 20));
        
        valueFieldAngle.addActionListener(e -> {
            if (!loading) {
                String text = valueFieldAngle.getText();
                
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
        
        sliderAngle.addChangeListener(e -> {
            if (!loading) {
                angle = sliderAngle.getValue();
                
                valueFieldAngle.setText(String.valueOf(angle));
            }
        });
        
        //Panel with sliderSize and value
        JPanel sliderPanelAngle = new JPanel(new BorderLayout());
        JLabel angleLabel = new JLabel("Angle (0° - 360°)", SwingConstants.LEFT);
        angleLabel.setBackground(Color.BLACK);
        angleLabel.setForeground(Color.WHITE);
        angleLabel.setOpaque(true);
        sliderPanelAngle.add(angleLabel, BorderLayout.NORTH);
        sliderPanelAngle.add(sliderAngle, BorderLayout.WEST);
        sliderPanelAngle.add(valueFieldAngle, BorderLayout.EAST);
        
        // Configure color pickers
        setButtonsVisualsColors(colorPicker1, colors[0]);
        setButtonsVisualsColors(colorPicker2, colors[1]);
        
        colorPicker1.addActionListener(e -> {
            Color color = JColorChooser.showDialog(frame, "Choose Color Background", colors[0]);
            
            if (color != null) {
                colorPicker1.setBackground(color);
                colors[0] = color;
            }
        });
        
        colorPicker2.addActionListener(e -> {
            Color color = JColorChooser.showDialog(frame, "Choose Color Foreground", colors[1]);
            
            if (color != null) {
                colorPicker2.setBackground(color);
                colors[1] = color;
            }
        });
        
        // Panel with colors
        JPanel sliderWithColorPanel = new JPanel(new BorderLayout());
        sliderWithColorPanel.add(colorPicker1, BorderLayout.NORTH);
        sliderWithColorPanel.add(colorPicker2, BorderLayout.SOUTH);
        
        // Reflect button
        setButtonVisuals(reflectButton);
        
        reflectButton.addActionListener(e -> {
            // Reflect the angle horizontaly
            int newAngle = ((angle % 360) + 360) % 360;
            newAngle = (180 - newAngle + 360) % 360;
            sliderAngle.setValue(newAngle);
        });
        
        //Bottom panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        controlPanel.setBackground(Color.BLACK);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        controlPanel.add(sliderWithColorPanel);
        controlPanel.add(sliderPanelSize);
        controlPanel.add(sliderPanelAngle);
        controlPanel.add(reflectButton);
        
        // Dots
        setButtonsVisuals(dotsButton);
        dotsButton.setBackground(Color.WHITE);
        dotsButton.setForeground(Color.BLACK);
        
        dotsButton.addActionListener(e -> {
            if (!loading) {
                if (!type.equals(TYPE.Dots)) {
                    dotsButton.setBackground(Color.WHITE);
                    dotsButton.setForeground(Color.BLACK);
                    
                    resetButtons(dotsButton);
                    
                    type = TYPE.Dots;
                }
            }
        });
        
        // Lines
        setButtonsVisuals(linesButton);
        
        linesButton.addActionListener(e -> {
            if (!loading) {
                if (!type.equals(TYPE.Lines)) {
                    linesButton.setBackground(Color.WHITE);
                    linesButton.setForeground(Color.BLACK);
                    
                    resetButtons(linesButton);
                    
                    type = TYPE.Lines;
                }
            }
        });
        
        // Sine
        setButtonsVisuals(sineButton);
        
        sineButton.addActionListener(e -> {
            if (!loading) {
                if (!type.equals(TYPE.Sine)) {
                    sineButton.setBackground(Color.WHITE);
                    sineButton.setForeground(Color.BLACK);
                    
                    resetButtons(sineButton);
                    
                    type = TYPE.Sine;
                }
            }
        });
        
        //Panel with buttons on side
        JPanel verticalButtonsPanel = new JPanel(new GridLayout(0, 1));
        verticalButtonsPanel.setBackground(Color.BLACK);
        verticalButtonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        verticalButtonsPanel.add(dotsButton);
        verticalButtonsPanel.add(linesButton);
        verticalButtonsPanel.add(sineButton);
        
        frame.add(verticalButtonsPanel, BorderLayout.EAST);
        
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.add(dropLabel, BorderLayout.CENTER);
        
        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int xPos = (screenSize.width - frame.getWidth()) / 2;
        int yPos = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(xPos, yPos);
        
        frame.setVisible(true);
    }
    
    private void setButtonsVisuals(JButton button) {
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(100, 40));
    }
    
    private void setButtonVisuals(JButton button) {
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(40, 40));
    }
    
    private void setButtonsVisualsColors(JButton button, Color color) {
        button.setBackground(color);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(40, 40));
    }
    
    private void resetButtons(JButton excludedButton) {
        for (JButton button : buttonsType) {
            if (button != excludedButton) {
                button.setBackground(Color.BLACK);
                button.setForeground(Color.WHITE);
            }
        }
    }
    
    private void ableOrDisableButton(JButton button) {
        button.setEnabled(!loading);
    }
}