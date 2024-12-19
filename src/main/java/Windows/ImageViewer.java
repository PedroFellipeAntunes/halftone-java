package Windows;

import Operation.Operations;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageViewer extends JDialog {
    private ImagePanel panel;
    private JPanel buttonPanel;
    private JButton saveButton;
    private JButton goBackButton;
    
    private boolean goBack = false;
    
    private final int MIN_WIDTH = 500;
    private final int MIN_HEIGHT = 500;
    
    public boolean wentBack() {
        return goBack;
    }
    
    public ImageViewer(BufferedImage image, String filePath) {
        super((Frame) null, "Image Viewer", true);
        
        panel = new ImagePanel(image);
        panel.setBackground(new Color(61, 56, 70));
        
        buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        
        saveButton = new JButton("Save");
        goBackButton = new JButton("Go Back");
        
        setButtonsVisuals(saveButton);
        setButtonsVisuals(goBackButton);
        
        saveButton.addActionListener(e -> {
            Operations.saveImage(image, filePath);
            goBack = true;
            dispose();
        });
        
        goBackButton.addActionListener(e -> {
            goBack = true;
            dispose();
        });
        
        buttonPanel.add(saveButton);
        buttonPanel.add(goBackButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        add(panel, BorderLayout.CENTER);
        
        adjustWindowSize(image);
        
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void setButtonsVisuals(JButton button) {
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(100, 40));
    }
    
    private void adjustWindowSize(BufferedImage image) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxWidth = (int) (screenSize.width * 0.85);
        int maxHeight = (int) (screenSize.height * 0.85);
        
        double scaleX = (double) maxWidth / image.getWidth();
        double scaleY = (double) maxHeight / image.getHeight();
        
        double scale = Math.min(scaleX, scaleY);
        
        int scaledWidth = (int) (image.getWidth() * scale);
        int scaledHeight = (int) (image.getHeight() * scale);
        
        int finalWidth = Math.max(MIN_WIDTH, scaledWidth);
        int finalHeight = Math.max(MIN_HEIGHT, scaledHeight);
        
        setSize(finalWidth, finalHeight);
    }
    
    class ImagePanel extends JPanel {
        private BufferedImage image;
        
        public ImagePanel(BufferedImage image) {
            this.image = image;
        }
        
        public void updateImage(BufferedImage newImage) {
            this.image = newImage;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            double scaleX = (double) getWidth() / image.getWidth();
            double scaleY = (double) getHeight() / image.getHeight();
            
            double scale = Math.min(scaleX, scaleY);
            
            int newWidth = (int) (image.getWidth() * scale);
            int newHeight = (int) (image.getHeight() * scale);
            
            int x = (getWidth() - newWidth) / 2;
            int y = (getHeight() - newHeight) / 2;
            
            g.drawImage(image, x, y, newWidth, newHeight, this);
        }
    }
}