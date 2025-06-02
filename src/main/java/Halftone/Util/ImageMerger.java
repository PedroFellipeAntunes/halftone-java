package Halftone.Util;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageMerger {
    /**
     * Merges an array of images by multiplying their color channels.
     *
     * @param images An array of BufferedImages to merge. Must contain at least
     * two images of identical dimensions.
     * @return A new BufferedImage where each pixel is the channel-wise product
     * (divided by 255) of all input images.
     * @throws IllegalArgumentException if the array is null, has fewer than two
     * images, or if image dimensions differ.
     */
    public BufferedImage mergeImagesMultiply(BufferedImage[] images) {
        validateInputArray(images);
        validateSameDimensions(images);

        int width = images[0].getWidth();
        int height = images[0].getHeight();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int red = 255;
                int green = 255;
                int blue = 255;
                int alpha = 255;

                for (BufferedImage img : images) {
                    Color color = new Color(img.getRGB(x, y), true);
                    red = (red * color.getRed()) / 255;
                    green = (green * color.getGreen()) / 255;
                    blue = (blue * color.getBlue()) / 255;
                    alpha = (alpha * color.getAlpha()) / 255;
                }

                Color multiplied = new Color(red, green, blue, alpha);
                result.setRGB(x, y, multiplied.getRGB());
            }
        }

        return result;
    }

    private void validateInputArray(BufferedImage[] images) {
        if (images == null || images.length < 2) {
            throw new IllegalArgumentException(
                "Image array must contain at least two images."
            );
        }
    }

    private void validateSameDimensions(BufferedImage[] images) {
        int width = images[0].getWidth();
        int height = images[0].getHeight();

        for (BufferedImage img : images) {
            if (img.getWidth() != width || img.getHeight() != height) {
                throw new IllegalArgumentException(
                    "All images must have the same dimensions."
                );
            }
        }
    }
}