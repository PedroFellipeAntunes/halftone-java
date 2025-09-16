package Windows;

import Halftone.Operations;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageViewer extends JDialog {
    private final ImagePanel panel;
    private final JPanel buttonPanel;
    private final JButton saveButton;
    private final JButton goBackButton;
    private JButton skipRemainingButton;
        
    private final int MIN_WIDTH = 500;
    private final int MIN_HEIGHT = 500;
    
    public ImageViewer(BufferedImage image, String filePath, Operations operations) {
        super((Frame) null, "Image Viewer", true);
        
        panel = new ImagePanel(image);
        panel.setBackground(new Color(61, 56, 70));
        
        buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        
        saveButton = new JButton("Save");
        goBackButton = new JButton("Don't Save");
        skipRemainingButton = new JButton("Skip All");
        
        setButtonsVisuals(saveButton);
        setButtonsVisuals(goBackButton);
        setButtonsVisuals(skipRemainingButton);
        
        saveButton.addActionListener(e -> {
            operations.saveImage(image, filePath);
            operations.save = true;
            dispose();
        });
        
        goBackButton.addActionListener(e -> {
            operations.save = false;
            dispose();
        });
        
        skipRemainingButton.addActionListener(e -> {
            operations.skip = !operations.skip;
            
            Color temp = skipRemainingButton.getBackground();
            skipRemainingButton.setBackground(skipRemainingButton.getForeground());
            skipRemainingButton.setForeground(temp);
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(goBackButton);
        buttonPanel.add(skipRemainingButton);
        
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