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
    private JSlider slider;
    private JTextField valueField;
    private JSlider sliderSize;
    private JTextField valueFieldSize;
    private JButton dotsButton;
    private JButton linesButton;
    
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
                            Operations.processFile(file.getPath(), scale, angle, type);
                            
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
        slider = new JSlider(JSlider.HORIZONTAL, 0, 100, scale);
        slider.setMajorTickSpacing(25);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBackground(Color.BLACK);
        slider.setForeground(Color.WHITE);
        
        //Value of slider
        valueField = new JTextField();
        valueField.setForeground(Color.WHITE);
        valueField.setBackground(Color.BLACK);
        valueField.setFont(defaultFont);
        valueField.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        valueField.setText(String.valueOf(slider.getValue()));
        valueField.setPreferredSize(new Dimension(50, 20));
        
        valueField.addActionListener(e -> {
            if (!loading) {
                String text = valueField.getText();
                
                if (!text.isEmpty()) {
                    text = text.substring(0, Math.min(text.length(), 3));
                    
                    int value = Integer.parseInt(text);
                    
                    value = Math.max(0, Math.min(100, value));
                    
                    slider.setValue(value);
                    valueField.setText(String.valueOf(value));
                } else {
                    valueField.setText(String.valueOf(slider.getValue()));
                }
                
                valueField.transferFocus();
            }
        });
        
        slider.addChangeListener(e -> {
            if (!loading) {
                scale = slider.getValue();
                
                valueField.setText(String.valueOf(scale));
            }
        });
        
        //Panel with slider and value
        JPanel sliderPanel = new JPanel(new BorderLayout());
        JLabel scaleLabel = new JLabel("Scale (0px - 100px)", SwingConstants.LEFT);
        scaleLabel.setBackground(Color.BLACK);
        scaleLabel.setForeground(Color.WHITE);
        scaleLabel.setOpaque(true);
        sliderPanel.add(scaleLabel, BorderLayout.NORTH);
        sliderPanel.add(slider, BorderLayout.WEST);
        sliderPanel.add(valueField, BorderLayout.EAST);
        
        //SliderSize
        sliderSize = new JSlider(JSlider.HORIZONTAL, 0, 360, angle);
        sliderSize.setMajorTickSpacing(90);
        sliderSize.setMinorTickSpacing(45);
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
                    
                    value = Math.max(0, Math.min(360, value));
                    
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
                angle = sliderSize.getValue();
                
                valueFieldSize.setText(String.valueOf(angle));
            }
        });
        
        //Panel with slider and value
        JPanel sliderPanel3 = new JPanel(new BorderLayout());
        JLabel angleLabel = new JLabel("Angle (0° - 360°)", SwingConstants.LEFT);
        angleLabel.setBackground(Color.BLACK);
        angleLabel.setForeground(Color.WHITE);
        angleLabel.setOpaque(true);
        sliderPanel3.add(angleLabel, BorderLayout.NORTH);
        sliderPanel3.add(sliderSize, BorderLayout.WEST);
        sliderPanel3.add(valueFieldSize, BorderLayout.EAST);
        
        //Bottom panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        controlPanel.setBackground(Color.BLACK);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        controlPanel.add(sliderPanel);
        controlPanel.add(sliderPanel3);
        
        dotsButton = new JButton("Dots");
        setButtonsVisuals(dotsButton);
        dotsButton.setBackground(Color.WHITE);
        dotsButton.setForeground(Color.BLACK);
        
        dotsButton.addActionListener(e -> {
            if (!loading) {
                if (!type.equals(TYPE.Dots)) {
                    dotsButton.setBackground(Color.WHITE);
                    dotsButton.setForeground(Color.BLACK);
                    
                    resetButton(linesButton);
                    
                    type = TYPE.Dots;
                }
            }
        });
        
        linesButton = new JButton("Lines");
        setButtonsVisuals(linesButton);
        
        linesButton.addActionListener(e -> {
            if (!loading) {
                if (!type.equals(TYPE.Lines)) {
                    linesButton.setBackground(Color.WHITE);
                    linesButton.setForeground(Color.BLACK);
                    
                    resetButton(dotsButton);
                    
                    type = TYPE.Lines;
                }
            }
        });
        
        //Panel with buttons on side
        JPanel verticalButtonsPanel = new JPanel(new GridLayout(0, 1));
        verticalButtonsPanel.setBackground(Color.BLACK);
        verticalButtonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        verticalButtonsPanel.add(dotsButton);
        verticalButtonsPanel.add(linesButton);
        
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
    
    private void resetButton(JButton button) {
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
    }
    
    private void ableOrDisableButton(JButton button) {
        button.setEnabled(!loading);
    }
}