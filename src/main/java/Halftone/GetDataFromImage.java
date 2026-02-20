package Halftone;

import Data.ColorAccumulator;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class GetDataFromImage {
    /**
     * Calculates the axis-aligned bounding box of an image after applying
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
     * kernel-sized block in the rotated image space.
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
    public ColorAccumulator[][] computeColorAccumulators(BufferedImage image, double angleDegrees, int kernelSize, double[] bounds, AffineTransform rotation) {
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
    
    /**
     * Computes Sobel gradient angles for each kernel in the accumulator grid.
     * Uses a 3x3 Sobel operator applied to the grayscale values of neighboring kernels.
     * 
     * The Sobel operator computes image gradients:
     * - Gx: horizontal gradient (detects vertical edges)
     * - Gy: vertical gradient (detects horizontal edges)
     * - Angle = atan2(Gy, Gx): direction of maximum intensity change
     *
     * @param image Original image (unused, kept for future extensions).
     * @param kernelSize Size of each kernel in pixels.
     * @param bounds Rotated bounds array {minXr, maxXr, minYr, maxYr}.
     * @param rotation Rotation transform (unused, kept for future extensions).
     * @param accumulators 2D array of ColorAccumulators to populate with Sobel angles.
     */
    public void computeSobelAngles(BufferedImage image, int kernelSize, double[] bounds, AffineTransform rotation, ColorAccumulator[][] accumulators) {
        int numKernels = accumulators.length;
        int numSegments = accumulators[0].length;
        
        // Sobel kernels for gradient computation
        int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
        
        double maxMagnitude = 0.0;
        
        for (int kr = 0; kr < numKernels; kr++) {
            for (int kc = 0; kc < numSegments; kc++) {
                double gx = 0.0;
                double gy = 0.0;
                
                // Apply 3x3 Sobel operator on neighboring kernels
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nkr = kr + dy;
                        int nkc = kc + dx;
                        
                        // Use zero-padding for out-of-bounds neighbors
                        double gray = 0.0;
                        
                        if (nkr >= 0 && nkr < numKernels && nkc >= 0 && nkc < numSegments) {
                            gray = accumulators[nkr][nkc].getGrayScale();
                        }
                        
                        gx += gray * sobelX[dy + 1][dx + 1];
                        gy += gray * sobelY[dy + 1][dx + 1];
                    }
                }
                
                double magnitude = Math.sqrt(gx * gx + gy * gy);

                accumulators[kr][kc].sobelAngle = Math.atan2(gy, gx);
                accumulators[kr][kc].magnitude = magnitude;

                if (magnitude > maxMagnitude) {
                    maxMagnitude = magnitude;
                }
                
                // Compute angle: atan2(Gy, Gx) gives direction of gradient in radians
                accumulators[kr][kc].sobelAngle = Math.atan2(gy, gx);
            }
        }
        
        // Second pass: normalize magnitude to [0,1]
        if (maxMagnitude > 0.0) {
            for (int kr = 0; kr < numKernels; kr++) {
                for (int kc = 0; kc < numSegments; kc++) {
                    accumulators[kr][kc].magnitude /= maxMagnitude;
                }
            }
        }
    }
    
    /**
     * Applies a box blur to the Sobel angle and magnitude fields of each
     * ColorAccumulator in the grid.
     *
     * Angles are blurred using circular/vector averaging (via sin/cos
     * components) to correctly handle wraparound (e.g., 350° and 10° should
     * average to 0°, not 180°). Magnitudes use a standard arithmetic box blur.
     *
     * @param accumulators 2D grid of ColorAccumulators with precomputed Sobel
     * data.
     * @param blurRadius Radius of the blur kernel (e.g., 1 = 3x3, 2 = 5x5
     * window). A value of 0 is a no-op.
     */
    public void blurSobelValues(ColorAccumulator[][] accumulators, int blurRadius) {
        if (blurRadius <= 0) {
            return;
        }

        int numKernels = accumulators.length;
        int numSegments = accumulators[0].length;

        double[][] newMagnitude = new double[numKernels][numSegments];
        double[][] newSinAngle = new double[numKernels][numSegments];
        double[][] newCosAngle = new double[numKernels][numSegments];

        for (int kr = 0; kr < numKernels; kr++) {
            for (int kc = 0; kc < numSegments; kc++) {

                double sumMag = 0.0;
                double sumSin = 0.0;
                double sumCos = 0.0;
                int count = 0;

                for (int dy = -blurRadius; dy <= blurRadius; dy++) {
                    for (int dx = -blurRadius; dx <= blurRadius; dx++) {
                        int nkr = kr + dy;
                        int nkc = kc + dx;

                        // Zero-padding: skip out-of-bounds neighbours
                        if (nkr < 0 || nkr >= numKernels || nkc < 0 || nkc >= numSegments) {
                            continue;
                        }

                        double angle = accumulators[nkr][nkc].sobelAngle;
                        double mag = accumulators[nkr][nkc].magnitude;

                        sumMag += mag;
                        // Weight sin/cos by magnitude so stronger edges dominate
                        sumSin += Math.sin(angle) * mag;
                        sumCos += Math.cos(angle) * mag;
                        count++;
                    }
                }

                newMagnitude[kr][kc] = (count > 0) ? sumMag / count : 0.0;

                // atan2(0, 0) == 0 and is safe, so no special-case needed
                newSinAngle[kr][kc] = sumSin;
                newCosAngle[kr][kc] = sumCos;
            }
        }

        // Write results back
        for (int kr = 0; kr < numKernels; kr++) {
            for (int kc = 0; kc < numSegments; kc++) {
                accumulators[kr][kc].magnitude = newMagnitude[kr][kc];
                accumulators[kr][kc].sobelAngle = Math.atan2(newSinAngle[kr][kc],
                        newCosAngle[kr][kc]);
            }
        }
    }
}