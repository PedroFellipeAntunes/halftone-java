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
     * Applies an axis-aligned square halftone pattern over the input image
     * using precomputed color accumulators. Each square’s side length is at most the kernelSize.
     *
     * @param input The original image to overlay with squares.
     * @param kernelSize The size of each square kernel (in pixels).
     * @param data Object containing rotation, bounds and color average data of
     * input image.
     * @return A new BufferedImage (type ARGB) containing only the square pattern.
     */
    public BufferedImage applySquarePattern(BufferedImage input, int kernelSize, ImageData data) {
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

        // Draw one square per kernel cell
        for (int kernelRow = 0; kernelRow < numKernels; kernelRow++) {
            for (int kernelCol = 0; kernelCol < numSegments; kernelCol++) {
                ColorAccumulator acc = data.avgGrid[kernelRow][kernelCol];
                
                if (acc.count == 0) {
                    continue;
                }

                // Compute inverted grayscale (0..255) and alpha (0..255)
                int alpha = acc.getAverage().getAlpha();
                double gray = 255 - acc.getGrayScale();

                // Side half-length based on grayscale and alpha,
                // capped so full side = kernelSize when gray=0 and alpha=255
                double halfSide = (gray / 255.0) * (alpha / 255.0) * (kernelSize / 2.0);

                // Compute center of the kernel in rotated coordinates
                Point2D centerRot = computeKernelCenterRotated(kernelRow, kernelCol, kernelSize, minXr, minYr);

                // Define four corners of the axis-aligned square in rotated space:
                // top-left, top-right, bottom-right, bottom-left
                Point2D[] squareCorners = {
                    new Point2D.Double(centerRot.getX() - halfSide, centerRot.getY() - halfSide),
                    new Point2D.Double(centerRot.getX() + halfSide, centerRot.getY() - halfSide),
                    new Point2D.Double(centerRot.getX() + halfSide, centerRot.getY() + halfSide),
                    new Point2D.Double(centerRot.getX() - halfSide, centerRot.getY() + halfSide)
                };

                // Draw the square via helper (applies rotation transform)
                drawRotatedPolygon(g2d, squareCorners, data.rotation);
            }
        }

        g2d.dispose();
        
        return outputImg;
    }
    
    /**
     * Applies an axis-aligned equilateral triangle halftone pattern over the
     * input image using precomputed color accumulators. Each triangle’s size is
     * scaled by the grayscale.
     *
     * @param input The original image to overlay with triangles.
     * @param kernelSize The size of each square kernel (in pixels).
     * @param data Object containing rotation, bounds and color average data of
     * input image.
     * @return A new BufferedImage (type ARGB) containing only the triangle
     * pattern.
     */
    public BufferedImage applyTrianglePattern(BufferedImage input, int kernelSize, ImageData data) {
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
        double diameter = 2 * radius;
        double drawX = center.getX() - radius;
        double drawY = center.getY() - radius;

        g2d.setColor(foregroundColor);
        
        g2d.fillOval(
            (int) Math.round(drawX),
            (int) Math.round(drawY),
            (int) Math.round(diameter),
            (int) Math.round(diameter)
        );
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