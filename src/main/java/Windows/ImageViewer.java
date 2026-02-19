package Windows;

import Halftone.Operations;
import Windows.Util.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Modal dialog that displays a processed halftone image and provides
 * save, discard, and skip-remaining actions.
 */
public class ImageViewer extends JDialog {
    private final ImagePanel panel;
    private final JPanel buttonPanel;
    private final JButton saveButton;
    private final JButton goBackButton;
    private final JButton skipRemainingButton;

    private static final int MIN_WIDTH = 500;
    private static final int MIN_HEIGHT = 500;
    private static final int BUTTON_HEIGHT = 40;

    /**
     * Creates and immediately displays the image viewer dialog.
     * Scales the window to fit the image within 85% of the screen.
     *
     * @param image The processed image to display.
     * @param filePath Original file path, used by the save action.
     * @param operations The active Operations instance, used to trigger save/skip.
     */
    public ImageViewer(BufferedImage image, String filePath, Operations operations) {
        super((Frame) null, "Image Viewer", true);

        panel = new ImagePanel(image);
        panel.setBackground(UIHelper.PANEL_BG_COLOR);

        buttonPanel = UIHelper.createPanel(new FlowLayout(), UIHelper.BG_COLOR, false);

        saveButton = new JButton("Save");
        goBackButton = new JButton("Don't Save");

        // Toggle button that inverts its own colors to signal the active skip state
        skipRemainingButton = UIHelper.createColorInvertButton("Skip All", () ->
            operations.skip = !operations.skip
        );

        // Save the image to disk and mark save=true so the batch continues
        saveButton.addActionListener(e -> {
            operations.saveImage(image, filePath);
            operations.save = true;
            dispose();
        });

        // Discard this result without saving; batch will still continue
        goBackButton.addActionListener(e -> {
            operations.save = false;
            dispose();
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(goBackButton);
        buttonPanel.add(skipRemainingButton);

        add(buttonPanel, BorderLayout.SOUTH);
        add(panel, BorderLayout.CENTER);

        adjustWindowSize(image);
        styleButtons();

        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ===== Helper methods =====

    // Computes a window size that fits the image within 85% of the screen,
    // while enforcing minimum dimensions so small images don't create tiny windows
    private void adjustWindowSize(BufferedImage image) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxWidth = (int) (screenSize.width * 0.85);
        int maxHeight = (int) (screenSize.height * 0.85);

        // Use the more restrictive axis to preserve aspect ratio
        double scale = Math.min(
            (double) maxWidth / image.getWidth(),
            (double) maxHeight / image.getHeight()
        );

        int finalWidth = Math.max(MIN_WIDTH, (int) (image.getWidth() * scale));
        int finalHeight = Math.max(MIN_HEIGHT, (int) (image.getHeight() * scale));

        setSize(finalWidth, finalHeight);
    }

    // Sizes the buttons proportionally to the dialog width, capped at 100px each
    private void styleButtons() {
        int buttonWidth = Math.min(100, getWidth() / 3);

        UIHelper.styleButton(saveButton, UIHelper.BG_COLOR, UIHelper.FG_COLOR, true, buttonWidth, BUTTON_HEIGHT);
        UIHelper.styleButton(goBackButton, UIHelper.BG_COLOR, UIHelper.FG_COLOR, true, buttonWidth, BUTTON_HEIGHT);
        UIHelper.styleButton(skipRemainingButton, UIHelper.BG_COLOR, UIHelper.FG_COLOR, true, buttonWidth, BUTTON_HEIGHT);
    }
    
    /**
     * Panel that renders a {@link BufferedImage} scaled to fill its bounds
     * while preserving the original aspect ratio and centering the result.
     *
     * NOTE: Consider extracting to its own file (ImagePanel.java).
     */
    class ImagePanel extends JPanel {
        private BufferedImage image;

        /**
         * @param image The initial image to render.
         */
        public ImagePanel(BufferedImage image) {
            this.image = image;
        }

        /**
         * Replaces the displayed image and triggers a repaint.
         *
         * @param newImage The replacement image.
         */
        public void updateImage(BufferedImage newImage) {
            this.image = newImage;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Scale image to fit the panel while preserving aspect ratio
            double scale = Math.min(
                (double) getWidth() / image.getWidth(),
                (double) getHeight() / image.getHeight()
            );

            int newWidth = (int) (image.getWidth() * scale);
            int newHeight = (int) (image.getHeight() * scale);

            // Center the scaled image within the panel
            int x = (getWidth() - newWidth) / 2;
            int y = (getHeight() - newHeight) / 2;

            g.drawImage(image, x, y, newWidth, newHeight, this);
        }
    }
}