package Halftone.Util;

import Data.ImageData;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class TestMethods {
    /**
     * Creates a new image where each pixel is assigned the average color of
     * its kernel.
     * For each pixel (x, y) in the input:
     * 1. Transform (x, y) into rotated coordinates (xr, yr) using the provided
     * AffineTransform.
     * 2. Determine the kernel indices (k, s) by flooring 
     * ((yr – minYr) / kernelSize) and ((xr – minXr) / kernelSize).
     * 3. If (k, s) lie within the accumulator grid, fetch the precomputed
     * average Color from avgGrid[k][s] and set that color in the output
     * image. Otherwise, write a fully transparent pixel.
     *
     * @param input The original image to test on.
     * @param angle Rotation angle (in degrees) used when computing
     * avgGrid (not used directly here).
     * @param kernelSize Size (in pixels) of each square kernel.
     * @param data Object containing rotation, bounds and color average data of
     * input image.
     * @return A new BufferedImage where each pixel is the average color of its
     * kernel.
     */
    public static BufferedImage applyAvgColorsTest(BufferedImage input, double angle, int kernelSize, ImageData data) {
        int width = input.getWidth();
        int height = input.getHeight();
        BufferedImage outputImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        double minXr = data.bounds[0];
        double minYr = data.bounds[2];
        int numKernels = data.avgGrid.length;
        int numSegments = data.avgGrid[0].length;

        Point2D src = new Point2D.Double();
        Point2D dst = new Point2D.Double();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Transform the pixel to rotated coordinates
                src.setLocation(x, y);
                data.rotation.transform(src, dst);
                double xr = dst.getX();
                double yr = dst.getY();

                // Compute kernel indices
                int k = (int) Math.floor((yr - minYr) / kernelSize);
                int s = (int) Math.floor((xr - minXr) / kernelSize);

                if (k >= 0 && k < numKernels && s >= 0 && s < numSegments) {
                    // Use the precomputed average color for this kernel
                    Color avgColor = data.avgGrid[k][s].getAverage();
                    outputImg.setRGB(x, y, avgColor.getRGB());
                } else {
                    // Outside any kernel: set fully transparent pixel
                    outputImg.setRGB(x, y, 0x00000000);
                }
            }
        }

        return outputImg;
    }
    
    /**
     * Generates an image that visualizes the kernel grid by assigning each
     * kernel region a unique hue (varying from 0.0 to 1.0 vertically) and
     * brightness (from 1.0 to 0.3 horizontally).
     * For each pixel (x, y) in the input:
     * 1. Transform (x, y) into rotated coordinates (xr, yr).
     * 2. Determine kernel indices (k, s).
     * 3. If (k, s) are valid, compute:
     *    - hue = k / (float) numKernels  (varies from 0 at top to 1 at bottom)
     *    - brightness = 1.0f – 0.7f * (s / (float) (numSegments – 1))
     * (varies from 1.0 at first column to 0.3 at last column)
     *    - Convert (hue, saturation=1.0, brightness) to RGB with Color.HSBtoRGB,
     * then set alpha = 255. Otherwise, set a fully transparent pixel.
     *
     * @param input The original image to test on.
     * @param kernelSize Size (in pixels) of each kernel.
     * @param data Object containing rotation, bounds and color average data of
     * input image.
     * @return A new BufferedImage color-coded to visualize kernel boundaries.
     */
    public static BufferedImage applyKernelTest(BufferedImage input, int kernelSize, ImageData data) {
        int width = input.getWidth();
        int height = input.getHeight();
        BufferedImage outputImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        double minXr = data.bounds[0], maxXr = data.bounds[1];
        double minYr = data.bounds[2], maxYr = data.bounds[3];

        int numKernels = (int) Math.ceil((maxYr - minYr) / kernelSize);
        int numSegments = (int) Math.ceil((maxXr - minXr) / kernelSize);

        Point2D src = new Point2D.Double();
        Point2D dst = new Point2D.Double();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Transform pixel to rotated coordinates
                src.setLocation(x, y);
                data.rotation.transform(src, dst);
                
                double xr = dst.getX();
                double yr = dst.getY();

                // Compute kernel indices
                int k = (int) Math.floor((yr - minYr) / kernelSize);
                int s = (int) Math.floor((xr - minXr) / kernelSize);

                if (k >= 0 && k < numKernels && s >= 0 && s < numSegments) {
                    // Assign hue based on vertical kernel index
                    float hue = k / (float) numKernels;
                    // Assign brightness based on horizontal segment index
                    float brightness = 1f - 0.7f * (s / (float) (numSegments - 1));
                    int rgb = Color.HSBtoRGB(hue, 1f, brightness);
                    // Add full opacity (alpha = 255)
                    int argb = (0xFF << 24) | (rgb & 0x00FFFFFF);

                    outputImg.setRGB(x, y, argb);
                } else {
                    // Outside any kernel: set fully transparent pixel
                    outputImg.setRGB(x, y, 0x00000000);
                }
            }
        }

        return outputImg;
    }
}