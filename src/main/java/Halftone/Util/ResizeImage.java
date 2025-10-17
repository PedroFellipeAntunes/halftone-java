package Halftone.Util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ResizeImage {
    /**
     * Expands the border of the given image by replicating its edge pixels.
     * The new image will be (width + (2 * kernelSize)) × (height + 
     * (2 * kernelSize)).
     * The central region is the original image. The top and bottom borders
     * are filled by repeating the first and last rows, respectively. The left
     * and right borders are filled by repeating the first and last columns.
     * Corners are filled with the corresponding corner pixels.
     *
     * @param input The original BufferedImage to expand.
     * @param kernelSize The thickness of the border to add (in pixels).
     * @return A new BufferedImage with borders expanded by kernelSize.
     */
    public BufferedImage expandBorder(BufferedImage input, int kernelSize) {
        if (kernelSize < 1) {
            throw new Error("kernelSize must be greater than or equal to 1");
        }
        
        int width = input.getWidth();
        int height = input.getHeight();
        int newWidth = width + 2 * kernelSize;
        int newHeight = height + 2 * kernelSize;

        BufferedImage output = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = output.createGraphics();

        // Draw the original image at offset (kernelSize, kernelSize)
        g2d.drawImage(input, kernelSize, kernelSize, null);

        // Fill top and bottom borders by repeating the first/last row pixels
        for (int x = 0; x < width; x++) {
            int topPixel = input.getRGB(x, 0);
            int bottomPixel = input.getRGB(x, height - 1);
            for (int y = 0; y < kernelSize; y++) {
                // Top border
                output.setRGB(x + kernelSize, y, topPixel);
                // Bottom border
                output.setRGB(x + kernelSize, newHeight - y - 1, bottomPixel);
            }
        }

        // Fill left and right borders by repeating the first/last column pixels
        for (int y = 0; y < height; y++) {
            int leftPixel = input.getRGB(0, y);
            int rightPixel = input.getRGB(width - 1, y);
            for (int x = 0; x < kernelSize; x++) {
                // Left border
                output.setRGB(x, y + kernelSize, leftPixel);
                // Right border
                output.setRGB(newWidth - x - 1, y + kernelSize, rightPixel);
            }
        }

        // Fill corner regions by repeating the corresponding corner pixel
        int topLeftPixel = input.getRGB(0, 0);
        int topRightPixel = input.getRGB(width - 1, 0);
        int bottomLeftPixel = input.getRGB(0, height - 1);
        int bottomRightPixel = input.getRGB(width - 1, height - 1);

        for (int x = 0; x < kernelSize; x++) {
            for (int y = 0; y < kernelSize; y++) {
                // Top-left corner
                output.setRGB(x, y, topLeftPixel);
                // Top-right corner
                output.setRGB(newWidth - x - 1, y, topRightPixel);
                // Bottom-left corner
                output.setRGB(x, newHeight - y - 1, bottomLeftPixel);
                // Bottom-right corner
                output.setRGB(newWidth - x - 1, newHeight - y - 1, bottomRightPixel);
            }
        }

        g2d.dispose();
        
        return output;
    }

    /**
     * Crops the border of the given image by removing kernelSize pixels from
     * each side.
     * The input image must be at least (2*kernelSize) larger in both
     * dimensions; otherwise, an IllegalArgumentException is thrown.
     *
     * @param input The BufferedImage with an existing border.
     * @param kernelSize The thickness of the border to remove (in pixels).
     * @return A new BufferedImage with borders removed, restoring original
     * dimensions.
     * @throws IllegalArgumentException if kernelSize is too large for the
     * input dimensions.
     */
    public BufferedImage cropBorder(BufferedImage input, int kernelSize) {
        if (kernelSize < 1) {
            throw new Error("kernelSize must be greater than or equal to 1");
        }
        
        int width = input.getWidth();
        int height = input.getHeight();
        int newWidth = width - 2 * kernelSize;
        int newHeight = height - 2 * kernelSize;

        if (newWidth <= 0 || newHeight <= 0) {
            throw new IllegalArgumentException(
                "Kernel size is too large for the input image dimensions."
            );
        }

        BufferedImage output = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                int rgb = input.getRGB(x + kernelSize, y + kernelSize);
                output.setRGB(x, y, rgb);
            }
        }

        return output;
    }
}