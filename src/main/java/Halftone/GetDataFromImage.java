package Halftone;

import Data.ColorAccumulator;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class GetDataFromImage {
    /**
     * Calculates the axis‐aligned bounding box of an image after applying
     * the specified rotation transform.
     *
     * @param image The image to be rotated.
     * @param rotation AffineTransform that rotates the image.
     * @return A 4-element array: {minXr, maxXr, minYr, maxYr} of rotated bounds.
     */
    public double[] calculateRotatedBounds(BufferedImage image, AffineTransform rotation) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        double minXr = Double.POSITIVE_INFINITY;
        double maxXr = Double.NEGATIVE_INFINITY;
        double minYr = Double.POSITIVE_INFINITY;
        double maxYr = Double.NEGATIVE_INFINITY;
        
        Point2D src = new Point2D.Double();
        Point2D dst = new Point2D.Double();
        
        int[] xs = {0, width};
        int[] ys = {0, height};

        for (int vx : xs) {
            for (int vy : ys) {
                src.setLocation(vx, vy);
                rotation.transform(src, dst);

                double xr = dst.getX();
                double yr = dst.getY();
                
                minXr = Math.min(minXr, xr);
                maxXr = Math.max(maxXr, xr);
                minYr = Math.min(minYr, yr);
                maxYr = Math.max(maxYr, yr);
            }
        }

        return new double[]{minXr, maxXr, minYr, maxYr};
    }
    
    /**
     * Computes a 2D grid of ColorAccumulator objects, each corresponding to a
     * kernel‐sized block in the rotated image space.
     *
     * @param image Original image.
     * @param angleDegrees Rotation angle in degrees (unused here; included for
     * signature consistency).
     * @param kernelSize Size (in pixels) of each square kernel.
     * @param bounds Array of four doubles: {minXr, maxXr, minYr, maxYr}.
     * @param rotation AffineTransform mapping from original to rotated
     * coordinates.
     * @return 2D array of ColorAccumulators.
     */
    public ColorAccumulator[][] computeColorAccumulators(
            BufferedImage image,
            double angleDegrees,
            int kernelSize,
            double[] bounds,
            AffineTransform rotation
    ) {
        int width = image.getWidth();
        int height = image.getHeight();

        double minXr = bounds[0];
        double maxXr = bounds[1];
        double minYr = bounds[2];
        double maxYr = bounds[3];

        // Compute number of rows (kernels vertically) and columns (segments horizontally)
        int numKernels = (int) Math.ceil((maxYr - minYr) / kernelSize);
        int numSegments = (int) Math.ceil((maxXr - minXr) / kernelSize);

        // Initialize 2D array of accumulators for each kernel region
        ColorAccumulator[][] accumulators = new ColorAccumulator[numKernels][numSegments];
        
        for (int k = 0; k < numKernels; k++) {
            for (int s = 0; s < numSegments; s++) {
                accumulators[k][s] = new ColorAccumulator();
            }
        }
        
        // Point containers for transforming pixel positions
        Point2D src = new Point2D.Double();
        Point2D dst = new Point2D.Double();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                src.setLocation(x, y);
                rotation.transform(src, dst);

                double xr = dst.getX();
                double yr = dst.getY();

                // Determine the corresponding kernel position in the rotated space
                int k = (int) Math.floor((yr - minYr) / kernelSize);
                int s = (int) Math.floor((xr - minXr) / kernelSize);

                // If within bounds, add pixel color to corresponding accumulator
                if (k >= 0 && k < numKernels && s >= 0 && s < numSegments) {
                    int argb = image.getRGB(x, y);
                    Color c = new Color(argb, true);

                    accumulators[k][s].add(c);
                }
            }
        }

        return accumulators;
    }
}