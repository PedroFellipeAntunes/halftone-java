package Halftone;

import Data.ColorAccumulator;
import Data.ImageData;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class Ht_Dot {
    public Color backgroundColor = Color.WHITE;
    public Color foregroundColor = Color.BLACK;

    /**
     * Applies a dot halftone pattern over the input image using precomputed
     * color accumulators.
     *
     * @param input The original image to overlay with dots.
     * @param kernelSize The size of each square kernel (in pixels).
     * @param data Object containing rotation, bounds and color average data of
     * input image.
     * @return A new BufferedImage (type ARGB) containing only the dot pattern.
     */
    public BufferedImage applyDotPattern(BufferedImage input, int kernelSize, ImageData data) {
        int width = input.getWidth();
        int height = input.getHeight();

        // Create an ARGB output image and obtain its Graphics2D context
        BufferedImage outputImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = outputImg.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background with solid color
        fillBackground(g2d, width, height);

        // Unpack rotated bounds
        double minXr = data.bounds[0];
        double minYr = data.bounds[2];

        int numKernels = data.avgGrid.length;
        int numSegments = data.avgGrid[0].length;

        // Maximum possible dot radius (diagonal of one kernel)
        double maxRadius = Math.sqrt(kernelSize * kernelSize + kernelSize * kernelSize) / 2.0;

        // Draw one dot per kernel cell
        for (int kernelRow = 0; kernelRow < numKernels; kernelRow++) {
            for (int kernelCol = 0; kernelCol < numSegments; kernelCol++) {
                ColorAccumulator acc = data.avgGrid[kernelRow][kernelCol];
                
                if (acc.count == 0) {
                    continue;
                }

                // Compute inverted grayscale (0..255) and alpha (0..255)
                int alpha = acc.getAverage().getAlpha();
                double gray = 255 - acc.getGrayScale();

                // Radius based on grayscale and alpha
                double radius = (gray / 255.0) * (alpha / 255.0) * maxRadius;
                
                if (isTooSmall(radius)) continue; // Prevents dot smaller than pixel

                // Compute kernel center in rotated coordinates
                Point2D centerRot = computeKernelCenterRotated(kernelRow, kernelCol, kernelSize, minXr, minYr);

                // Transform back to original image coordinates
                Point2D centerOrig = invertTransform(centerRot, data.rotation);
                
                if (centerOrig == null) {
                    continue; // If inversion failed, skip this dot
                }

                // Draw the dot at the computed center
                drawDot(g2d, centerOrig, radius);
            }
        }

        g2d.dispose();
        
        return outputImg;
    }

    /**
     * Applies a regular polygon halftone pattern over the input image using
     * precomputed color accumulators. The polygon size is scaled according to
     * grayscale intensity and alpha, and the number of sides is configurable.
     * Each polygon is aligned consistently within its kernel cell.
     *
     * @param input The original image to overlay with polygons.
     * @param kernelSize The size of each square kernel (in pixels).
     * @param data Object containing rotation, bounds and color average data of
     * the input image.
     * @param sides Number of polygon sides (must be >= 3).
     * @return A new BufferedImage (type ARGB) containing only the polygon
     * pattern.
     */
    public BufferedImage applyPolygonPattern(BufferedImage input, int kernelSize, ImageData data, int sides) {
        if (sides < 3) {
            throw new IllegalArgumentException("Polygon must have at least 3 sides.");
        }

        int width = input.getWidth();
        int height = input.getHeight();

        BufferedImage outputImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = outputImg.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        fillBackground(g2d, width, height);

        double minXr = data.bounds[0];
        double minYr = data.bounds[2];

        int numKernels = data.avgGrid.length;
        int numSegments = data.avgGrid[0].length;

        // Maximum possible polygon radius = half diagonal of kernel (same as dot)
        double maxRadius = Math.sqrt(kernelSize * kernelSize + kernelSize * kernelSize) / 2.0;

        for (int kernelRow = 0; kernelRow < numKernels; kernelRow++) {
            for (int kernelCol = 0; kernelCol < numSegments; kernelCol++) {
                ColorAccumulator acc = data.avgGrid[kernelRow][kernelCol];
                
                if (acc.count == 0) {
                    continue;
                }

                // Compute inverted grayscale (0..255) and alpha (0..255)
                int alpha = acc.getAverage().getAlpha();
                double gray = 255 - acc.getGrayScale();

                // Scale radius exactly like applyDotPattern
                double radius = (gray / 255.0) * (alpha / 255.0) * maxRadius;
                
                if (isTooSmall(radius)) {
                    continue;
                }

                // Compute center of the kernel in rotated coordinates
                Point2D centerRot = computeKernelCenterRotated(kernelRow, kernelCol, kernelSize, minXr, minYr);

                // Square's top-left corner is center - halfSide in X and Y
                // We use the vector from center to that point to determine base angle
                double halfSideForAngle = kernelSize / 2.0; // full kernel half
                double dx = -halfSideForAngle;
                double dy = -halfSideForAngle;
                double startAngle = Math.atan2(dy, dx); // direction of first corner

                // Now generate polygon vertices with this base angle
                Point2D[] polygonCorners = new Point2D[sides];
                double angleStep = 2 * Math.PI / sides;

                for (int i = 0; i < sides; i++) {
                    double angle = startAngle + i * angleStep;
                    double x = centerRot.getX() + radius * Math.cos(angle);
                    double y = centerRot.getY() + radius * Math.sin(angle);
                    polygonCorners[i] = new Point2D.Double(x, y);
                }

                drawRotatedPolygon(g2d, polygonCorners, data.rotation);
            }
        }

        g2d.dispose();
        
        return outputImg;
    }
    
    /**
     * Applies an alternating equilateral triangle halftone pattern over the input
     * image using precomputed color accumulators. The triangles scale their size
     * according to grayscale intensity and alpha. Every other column alternates
     * orientation (pointing up or down), producing a zigzag tiling effect.
     *
     * @param input The original image to overlay with triangles.
     * @param kernelSize The size of each square kernel (in pixels).
     * @param data Object containing rotation, bounds and color average data of
     * input image.
     * @return A new BufferedImage (type ARGB) containing only the alternating triangle
     * pattern.
     */
    public BufferedImage applyAlternatingTrianglePattern(BufferedImage input, int kernelSize, ImageData data) {
        int width = input.getWidth();
        int height = input.getHeight();

        // Create an ARGB output image and obtain its Graphics2D context
        BufferedImage outputImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = outputImg.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background with solid color
        fillBackground(g2d, width, height);

        // Unpack rotated bounds
        double minXr = data.bounds[0];
        double minYr = data.bounds[2];

        int numKernels = data.avgGrid.length;
        int numSegments = data.avgGrid[0].length;
        
        // Draw one triangle per kernel cell
        for (int kernelRow = 0; kernelRow < numKernels; kernelRow++) {
            for (int kernelCol = 0; kernelCol < numSegments; kernelCol++) {
                ColorAccumulator acc = data.avgGrid[kernelRow][kernelCol];

                if (acc.count == 0) {
                    continue;
                }

                // Compute inverted grayscale (0..255) and alpha (0..255)
                int alpha = acc.getAverage().getAlpha();
                double gray = 255 - acc.getGrayScale();

                // Side length based on grayscale and alpha
                double overlapMargin = 0.5; // 50% required for triangle to properly cover the kernel area
                double side = (gray / 255.0) * (alpha / 255.0) * (kernelSize * (1.0 + overlapMargin));
                
                if (isTooSmall(side)) continue; // Prevents smaller than pixel

                // Compute circumscribed radius
                double radius = side / Math.sqrt(3);

                // Compute center of the kernel in rotated coordinates
                Point2D centerRot = computeKernelCenterRotated(kernelRow, kernelCol, kernelSize, minXr, minYr);

                // Determine orientation: up for even cols, down for odd cols
                boolean pointingUp = (kernelCol % 2 == 0);
                double baseAngle = pointingUp ? -Math.PI / 2.0 : Math.PI / 2.0;

                Point2D[] triangleCorners = new Point2D[3];
                
                // Top point of triangle
                double topAngle = baseAngle;
                double topX = centerRot.getX() + radius * Math.cos(topAngle);
                double topY = centerRot.getY() + radius * Math.sin(topAngle);
                
                for (int i = 0; i < 3; i++) {
                    double angle = baseAngle + i * (2 * Math.PI / 3.0);
                    
                    double x = centerRot.getX() + radius * Math.cos(angle);
                    double y = centerRot.getY() + radius * Math.sin(angle);
                    
                    // Shift the triangle back a bit so they line up
                    double vx = x - topX;
                    double vy = y - topY;
                    
                    x += overlapMargin / 2.0 * vx;
                    y += overlapMargin / 2.0 * vy;
                    
                    triangleCorners[i] = new Point2D.Double(x, y);
                }

                // Draw the triangle via helper (applies rotation transform)
                drawRotatedPolygon(g2d, triangleCorners, data.rotation);
            }
        }

        g2d.dispose();

        return outputImg;
    }
    
    //---------------------- Helper Methods ----------------------
    
    private boolean isTooSmall(double value) {
        return value < 0.25;
    }

    private void fillBackground(Graphics2D g2d, int width, int height) {
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, height);
    }

    private Point2D computeKernelCenterRotated(
            int row,
            int col,
            int kernelSize,
            double minXr,
            double minYr
    ) {
        double centerXr = minXr + col * kernelSize + kernelSize / 2.0;
        double centerYr = minYr + row * kernelSize + kernelSize / 2.0;
        
        return new Point2D.Double(centerXr, centerYr);
    }

    private Point2D invertTransform(Point2D rotatedPoint, AffineTransform toRot) {
        try {
            AffineTransform invRot = toRot.createInverse();
            Point2D originalPoint = new Point2D.Double();
            invRot.transform(rotatedPoint, originalPoint);
            
            return originalPoint;
        } catch (NoninvertibleTransformException e) {
            // If the transform is non‐invertible, skip drawing this shape
            return null;
        }
    }

    private void drawDot(Graphics2D g2d, Point2D center, double radius) {
        double diameter = 2.0 * radius;
        double drawX = center.getX() - radius;
        double drawY = center.getY() - radius;

        java.awt.geom.Ellipse2D.Double ellipse = new java.awt.geom.Ellipse2D.Double(drawX, drawY, diameter, diameter);

        g2d.setColor(foregroundColor);
        g2d.fill(ellipse);
    }

    private void drawRotatedPolygon(Graphics2D g2d, Point2D[] rotatedCorners, AffineTransform toRot) {
        Path2D path = new Path2D.Double();

        for (int i = 0; i < rotatedCorners.length; i++) {
            Point2D cornerOrig = invertTransform(rotatedCorners[i], toRot);

            if (cornerOrig == null) {
                return; // skip entire polygon if any corner fails to invert
            }

            if (i == 0) {
                path.moveTo(cornerOrig.getX(), cornerOrig.getY());
            } else {
                path.lineTo(cornerOrig.getX(), cornerOrig.getY());
            }
        }

        path.closePath();

        g2d.setColor(foregroundColor);
        g2d.fill(path);
    }
}