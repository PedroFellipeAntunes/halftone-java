package Halftone;

import Data.ColorAccumulator;
import Data.ImageData;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Ht_Line {
    private final int lineSpacing = 0; // Currently unused
    private final double lineThicknessMIN = 1.0 / 32.0; // Small enough to allow lines to propagate but not to exist in bright areas
    
    public Color backgroundColor = Color.WHITE;
    public Color foregroundColor = Color.BLACK;

    /**
     * Apply a straight-line halftone pattern.
     *
     * @param input Source image.
     * @param kernelSize Side length (in pixels) of each square kernel.
     * @param data Precomputed ImageData (rotation, bounds, avgGrid).
     * @return BufferedImage with straight-line halftone applied.
     */
    public BufferedImage applyLinePattern(BufferedImage input, int kernelSize, ImageData data) {
        int width = input.getWidth();
        int height = input.getHeight();
        
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) output.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Invert the rotation so we can map rotated coordinates back to the original image space.
        AffineTransform invRot = invert(data.rotation);
        
        if (invRot == null) {
            g.dispose();
            
            return output;
        }

        fillBackground(g, width, height);

        double minXr = data.bounds[0];
        double minYr = data.bounds[2];
        
        int rows = data.avgGrid.length;
        int cols = data.avgGrid[0].length;

        for (int row = 0; row < rows; row++) {
            Point2D[] uppers = new Point2D[cols];
            Point2D[] lowers = new Point2D[cols];
            
            boolean[] valid = new boolean[cols];
            int countValid = 0;

            for (int col = 0; col < cols; col++) {
                ColorAccumulator acc = data.avgGrid[row][col];
                
                if (acc.count == 0) {
                    valid[col] = false;
                    
                    continue;
                }
                
                valid[col] = true;
                countValid++;

                // Compute grayscale [0..255] and alpha [0..255] for this kernel
                double gray = acc.getGrayScale();
                int alpha = acc.getAverage().getAlpha();

                // Determine half-thickness of the line segment, scaled by alpha
                double baseHalf = computeBaseHalfThickness(gray, kernelSize);
                double halfThick = baseHalf * (alpha / 255.0);

                // Find the center of the kernel in rotated space.
                Point2D centerRot = kernelCenter(row, col, kernelSize, minXr, minYr);
                
                // Build the top and bottom points in rotated coordinates.
                Point2D upperRot = new Point2D.Double(centerRot.getX(), centerRot.getY() - halfThick);
                Point2D lowerRot = new Point2D.Double(centerRot.getX(), centerRot.getY() + halfThick);

                // Map these rotated points back into original image coordinates.
                uppers[col] = mapBack(upperRot, invRot);
                lowers[col] = mapBack(lowerRot, invRot);
            }

            if (countValid >= 3) {
                Path2D poly = buildPathFromArrays(uppers, lowers, valid);
                draw(g, poly);
            }
        }

        g.dispose();
        
        return output;
    }

    /**
     * Apply a sine-wave halftone pattern.
     *
     * @param input Source image.
     * @param kernelSize Side length (in pixels) of each square kernel.
     * @param data Precomputed ImageData (rotation, bounds, avgGrid).
     * @return BufferedImage with sine-wave halftone applied.
     */
    public BufferedImage applySinePattern(BufferedImage input, int kernelSize, ImageData data) {
        int width = input.getWidth();
        int height = input.getHeight();
        
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) output.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Invert the rotation to map rotated-space points back to original.
        AffineTransform invRot = invert(data.rotation);
        
        if (invRot == null) {
            g.dispose();
            
            return output;
        }

        fillBackground(g, width, height);

        double minXr = data.bounds[0];
        double maxXr = data.bounds[1];
        double minYr = data.bounds[2];
        
        int rows = data.avgGrid.length;
        int cols = data.avgGrid[0].length;

        // Sine parameters: amplitude = half kernel, frequency = 1 cycle per (kernelSize * 6) px
        double amplitude = kernelSize / 2.0;
        double frequency = 2 * Math.PI / (kernelSize * 6.0);

        for (int row = 0; row < rows; row++) {
            if (!rowHasData(data, row)) {
                continue;
            }

            double centerY = minYr + row * kernelSize + kernelSize / 2.0;
            List<Point2D> topList = new ArrayList<>();
            List<Point2D> botList = new ArrayList<>();

            for (double x = minXr; x <= maxXr; x += 1.0) {
                InterpolatedResult ir = interpolatedGrayAndAlpha(data, row, kernelSize, minXr, x, cols);
                
                if (ir.gray < 0) {
                    // No data: close and draw if there are accumulated points
                    if (!topList.isEmpty()) {
                        Path2D poly = buildPathFromLists(topList, botList);
                        draw(g, poly);
                        topList.clear();
                        botList.clear();
                    }
                    
                    continue;
                }

                double baseHalf = computeBaseHalfThickness(ir.gray, kernelSize);
                double halfThick = baseHalf * (ir.alpha / 255.0);
                
                if (halfThick <= 0) {
                    if (!topList.isEmpty()) {
                        Path2D poly = buildPathFromLists(topList, botList);
                        
                        draw(g, poly);
                        topList.clear();
                        botList.clear();
                    }
                    
                    continue;
                }

                double ySine = centerY + amplitude * Math.sin(frequency * x);
                
                Point2D topRot = new Point2D.Double(x, ySine - halfThick);
                Point2D botRot = new Point2D.Double(x, ySine + halfThick);
                
                Point2D topOrig = mapBack(topRot, invRot);
                Point2D botOrig = mapBack(botRot, invRot);

                topList.add(new Point((int) Math.round(topOrig.getX()), (int) Math.round(topOrig.getY())));
                botList.add(new Point((int) Math.round(botOrig.getX()), (int) Math.round(botOrig.getY())));
            }

            // Finish and draw if there are points after finishing the line
            if (!topList.isEmpty()) {
                Path2D poly = buildPathFromLists(topList, botList);
                draw(g, poly);
            }
        }


        g.dispose();
        
        return output;
    }

    //---------------------- Helper Methods ----------------------

    private void fillBackground(Graphics2D g, int w, int h) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, w, h);
    }

    private AffineTransform invert(AffineTransform t) {
        try {
            return t.createInverse();
        } catch (NoninvertibleTransformException e) {
            return null;
        }
    }

    private Point2D mapBack(Point2D rotated, AffineTransform invRot) {
        Point2D original = new Point2D.Double();
        invRot.transform(rotated, original);
        
        return original;
    }
    
    /**
     * Compute half-thickness (in pixels) for a stripe based on grayscale value [0..255].
     * Black (0) yields maximum thickness = (kernelSize/2 - lineSpacing), white (255) yields zero.
     */
    private double computeBaseHalfThickness(double gray, int kernelSize) {
        double thick = ((kernelSize / 2.0) - lineSpacing) * (1.0 - (gray / 255.0));
        
        return (thick > lineThicknessMIN) ? thick : 0; // Prevent lines smaller than a pixel
    }

    private boolean rowHasData(ImageData data, int row) {
        for (ColorAccumulator acc : data.avgGrid[row]) {
            if (acc.count > 0) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Interpolate grayscale and alpha at an arbitrary X coordinate (rotated
     * space) for a given row:
     * - Compute exactCol = (x - minXr) / kernelSize to get a fractional index.
     * - Set left = floor(exactCol) and right = left + 1.
     * - If both kernels are valid (count > 0), perform linear interpolation on
     * both gray and alpha.
     * - If only one is valid, return that kernel’s gray and alpha.
     * - If neither is valid, return gray = -1 to signal “no data.”
     */
    private InterpolatedResult interpolatedGrayAndAlpha(ImageData data, int row, int kernelSize, double minXr, double x, int numCols) {
        double exact = (x - minXr) / kernelSize;
        int left = (int) Math.floor(exact);
        int right = left + 1;
        double t = exact - left; // fractional distance between left and right

        // If “left” is out of bounds, we cannot interpolate (even if “right” is in range)
        if (left < 0 || left >= numCols) {
            return new InterpolatedResult(-1, 0);
        }

        // Get gray and alpha from the left kernel, or -1 if count == 0
        ColorAccumulator accL = data.avgGrid[row][left];
        double gL = (accL.count > 0) ? accL.getGrayScale() : -1;
        double aL = (accL.count > 0) ? accL.getAverage().getAlpha() : -1;

        // Initialize right side as “no data”
        double gR = -1;
        double aR = -1;
        
        if (right >= 0 && right < numCols) {
            ColorAccumulator accR = data.avgGrid[row][right];
            
            if (accR.count > 0) {
                gR = accR.getGrayScale();
                aR = accR.getAverage().getAlpha();
            }
        }

        // If neither side has valid data, return gray = -1 to skip this position
        if (gL < 0 && gR < 0) {
            return new InterpolatedResult(-1, 0);
        }

        // If only the right side has valid data, return its values
        if (gL < 0) {
            return new InterpolatedResult(gR, (int) Math.round(aR));
        }

        // If only the left side has valid data, return its values
        if (gR < 0) {
            return new InterpolatedResult(gL, (int) Math.round(aL));
        }

        // If both have valid data, linearly interpolate gray AND alpha
        double grayInterp = gL * (1.0 - t) + gR * t;
        double alphaInterp = aL * (1.0 - t) + aR * t;
        
        return new InterpolatedResult(grayInterp, (int) Math.round(alphaInterp));
    }
    
    private Point2D kernelCenter(int row, int col, int kernelSize, double minXr, double minYr) {
        double cx = minXr + col * kernelSize + kernelSize / 2.0;
        double cy = minYr + row * kernelSize + kernelSize / 2.0;
        
        return new Point2D.Double(cx, cy);
    }
    
    /**
     * Build a closed polygon from arrays of top and bottom points, using a valid[] mask.
     * - First, add all 'top[i]' in ascending order of i for valid[i].
     * - Then add all 'bottom[i]' in descending order of i for valid[i], closing the shape.
     */
    private Path2D buildPathFromArrays(Point2D[] top, Point2D[] bot, boolean[] valid) {
        Path2D path = new Path2D.Double();
        int n = valid.length;
        boolean started = false;

        for (int i = 0; i < n; i++) {
            if (valid[i]) {
                if (!started) {
                    path.moveTo(top[i].getX(), top[i].getY());
                    started = true;
                } else {
                    path.lineTo(top[i].getX(), top[i].getY());
                }
            }
        }

        for (int i = n - 1; i >= 0; i--) {
            if (valid[i]) {
                path.lineTo(bot[i].getX(), bot[i].getY());
            }
        }

        path.closePath();
        
        return path;
    }

    /**
     * Build a closed polygon from two lists of Points:
     * - 'topList' should be in ascending X order.
     * - 'botList' will be traversed in reverse (descending X) to close the shape.
     */
    private Path2D buildPathFromLists(List<Point2D> topList, List<Point2D> botList) {
        Path2D path = new Path2D.Double();

        if (topList.isEmpty()) {
            return path;
        }

        path.moveTo(topList.get(0).getX(), topList.get(0).getY());
        for (int i = 1; i < topList.size(); i++) {
            Point2D pt = topList.get(i);
            path.lineTo(pt.getX(), pt.getY());
        }

        for (int i = botList.size() - 1; i >= 0; i--) {
            Point2D pt = botList.get(i);
            path.lineTo(pt.getX(), pt.getY());
        }

        path.closePath();
        
        return path;
    }

    private void draw(Graphics2D g, Path2D path) {
        g.setColor(foregroundColor);
        g.fill(path);
    }
    
    /**
     * Simple holder for an interpolated grayscale and alpha pair.
     */
    private static class InterpolatedResult {
        public double gray;
        public int alpha;

        public InterpolatedResult(double gray, int alpha) {
            this.gray = gray;
            this.alpha = alpha;
        }
    }
}