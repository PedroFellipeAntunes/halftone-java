package Halftone.Util.FlowLine;

import Data.FlowLine.BezierPoint;
import Data.ColorAccumulator;
import Data.FlowLine.FlowLine;
import Data.ImageData;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class FlowLineRendererBezier {
    private final double lineThicknessMIN = 1.0 / 32.0;

    public Color backgroundColor = Color.WHITE;
    public Color foregroundColor = Color.BLACK;

    public int minLineSize = 2;

    // Smoothing configuration (N neighboring kernels along the line)
    public int lineSmoothingRadius = 2;

    // Number of points sampled along the Bézier curve
    public int bezierResolution = 50;

    /**
     * Renders all flow lines as filled Bézier ribbons onto the given Graphics2D context.
     *
     * @param g2d The graphics context to draw onto.
     * @param flowLines List of flow lines to render.
     * @param data Object containing rotation, bounds, and color average data of the input image.
     * @param kernelSize The size of each kernel cell in pixels.
     */
    public void render(Graphics2D g2d, List<FlowLine> flowLines, ImageData data, int kernelSize) {
        drawFlowLines(g2d, flowLines, kernelSize, data.bounds[0], data.bounds[2], data);
    }

    /**
     * Iterates over all flow lines, fills each one as a Bézier ribbon,
     * and draws end-cap dots as independent shapes to avoid winding rule conflicts.
     */
    private void drawFlowLines(Graphics2D g2d, List<FlowLine> flowLines, int kernelSize, double minXr, double minYr, ImageData data) {
        g2d.setColor(foregroundColor);

        for (FlowLine line : flowLines) {
            if (line == null || line.size() < minLineSize) continue;

            // Draw the ribbon
            Path2D ribbon = buildBezierRibbon(line, kernelSize, minXr, minYr, data);
            if (ribbon != null) {
                g2d.fill(ribbon);
            }

            // Draw end-cap dots as independent shapes to avoid winding conflicts
            drawEndCapDots(g2d, line, kernelSize, minXr, minYr, data);
        }
    }

    /**
     * Draws filled circular end-cap dots at the start and end of a flow line.
     * Each dot is drawn as an independent shape to prevent winding rule cancellation
     * that would occur if appended to the ribbon Path2D.
     */
    private void drawEndCapDots(Graphics2D g2d, FlowLine line, int kernelSize, double minXr, double minYr, ImageData data) {
        double[] halfWidths = calculateHalfWidths(line, kernelSize, data);
        List<BezierPoint> bezierPoints = generateBezierPoints(line, halfWidths, kernelSize, minXr, minYr);

        if (bezierPoints.size() < 2) return;

        fillDot(g2d, bezierPoints.get(0), data);
        fillDot(g2d, bezierPoints.get(bezierPoints.size() - 1), data);
    }

    /**
     * Fills a single circular dot at the given Bézier point position,
     * using its half-width as the radius.
     */
    private void fillDot(Graphics2D g2d, BezierPoint point, ImageData data) {
        Point2D.Double center = rotatedToImage(point.position, data);
        if (center == null) return;

        double r = point.halfWidth;
        g2d.fill(new Ellipse2D.Double(center.x - r, center.y - r, r * 2, r * 2));
    }

    /**
     * Builds a filled ribbon shape for a single flow line using a Bézier curve
     * through all kernel positions as control points.
     */
    private Path2D buildBezierRibbon(FlowLine line, int kernelSize, double minXr, double minYr, ImageData data) {
        int n = line.size();

        if (n < 2) return null;

        double[] halfWidths = calculateHalfWidths(line, kernelSize, data);
        List<BezierPoint> bezierPoints = generateBezierPoints(line, halfWidths, kernelSize, minXr, minYr);

        if (bezierPoints.size() < 2) return null;

        return buildRibbonFromBezier(bezierPoints, data);
    }

    /**
     * Calculates per-point half-widths using a moving average over neighboring
     * kernel cells along the flow line.
     */
    private double[] calculateHalfWidths(FlowLine line, int kernelSize, ImageData data) {
        int n = line.size();
        double[] halfWidths = new double[n];

        for (int i = 0; i < n; i++) {
            double sumHalfWidth = 0;
            int count = 0;

            for (int offset = -lineSmoothingRadius; offset <= lineSmoothingRadius; offset++) {
                int neighborIndex = i + offset;

                if (neighborIndex >= 0 && neighborIndex < n) {
                    Point2D.Double kPos = line.positions.get(neighborIndex);
                    int kr = (int) Math.floor(kPos.y);
                    int kc = (int) Math.floor(kPos.x);

                    if (kr >= 0 && kr < data.avgGrid.length && kc >= 0 && kc < data.avgGrid[0].length) {
                        ColorAccumulator cell = data.avgGrid[kr][kc];
                        if (cell.count > 0) {
                            sumHalfWidth += computeBaseHalfThickness(cell, kernelSize);
                            count++;
                        }
                    }
                }
            }

            halfWidths[i] = (count > 0) ? (sumHalfWidth / count) : 0;
        }

        return halfWidths;
    }

    /**
     * Generates interpolated points along a Bézier curve, using all kernel
     * positions as control points and blending their associated half-widths.
     */
    private List<BezierPoint> generateBezierPoints(FlowLine line, double[] halfWidths,
                                                    int kernelSize, double minXr, double minYr) {
        List<BezierPoint> result = new ArrayList<>();
        int n = line.size();

        // Convert kernel positions to rotated image space (control points)
        List<Point2D.Double> controlPoints = new ArrayList<>();
        for (Point2D.Double kPos : line.positions) {
            controlPoints.add(new Point2D.Double(
                minXr + kPos.x * kernelSize,
                minYr + kPos.y * kernelSize
            ));
        }

        double[] binomialCoeffs = calculateBinomialCoefficients(n - 1);

        for (int step = 0; step <= bezierResolution; step++) {
            double t = (double) step / bezierResolution;

            Point2D.Double pos = bezierInterpolate(controlPoints, binomialCoeffs, t);
            double width = bezierInterpolate1D(halfWidths, binomialCoeffs, t);

            result.add(new BezierPoint(pos, width));
        }

        return result;
    }

    /**
     * Calculates binomial coefficients C(n, k) for k = 0 to n.
     */
    private double[] calculateBinomialCoefficients(int n) {
        double[] coeffs = new double[n + 1];
        coeffs[0] = 1;

        for (int i = 1; i <= n; i++) {
            coeffs[i] = coeffs[i - 1] * (n - i + 1) / i;
        }

        return coeffs;
    }

    /**
     * Performs Bézier curve interpolation for a list of 2D control points.
     * B(t) = Σ(i=0 to n) [C(n,i) * (1-t)^(n-i) * t^i * P_i]
     */
    private Point2D.Double bezierInterpolate(List<Point2D.Double> controlPoints,
                                             double[] binomialCoeffs, double t) {
        int n = controlPoints.size() - 1;
        double x = 0.0;
        double y = 0.0;

        for (int i = 0; i <= n; i++) {
            double basis = binomialCoeffs[i] * Math.pow(1 - t, n - i) * Math.pow(t, i);

            Point2D.Double p = controlPoints.get(i);
            x += basis * p.x;
            y += basis * p.y;
        }

        return new Point2D.Double(x, y);
    }

    /**
     * Performs Bézier curve interpolation for a 1D array of scalar values (widths).
     */
    private double bezierInterpolate1D(double[] values, double[] binomialCoeffs, double t) {
        int n = values.length - 1;
        double result = 0.0;

        for (int i = 0; i <= n; i++) {
            double basis = binomialCoeffs[i] * Math.pow(1 - t, n - i) * Math.pow(t, i);
            result += basis * values[i];
        }

        return result;
    }

    /**
     * Builds the final filled ribbon polygon from a list of interpolated Bézier
     * points by offsetting each point perpendicularly by its half-width to form
     * left and right edges, then connecting them into a closed path.
     * End-cap dots are no longer appended here — they are drawn separately via
     * drawEndCapDots() to avoid winding rule cancellation artifacts.
     */
    private Path2D buildRibbonFromBezier(List<BezierPoint> bezierPoints, ImageData data) {
        int n = bezierPoints.size();

        if (n < 2) return null;

        List<Point2D.Double> leftEdge = new ArrayList<>();
        List<Point2D.Double> rightEdge = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            BezierPoint curr = bezierPoints.get(i);

            // Compute tangent as direction between neighboring points
            Point2D.Double tangent;

            if (i == 0) {
                Point2D.Double next = bezierPoints.get(1).position;
                tangent = normalize(next.x - curr.position.x, next.y - curr.position.y);
            } else if (i == n - 1) {
                Point2D.Double prev = bezierPoints.get(n - 2).position;
                tangent = normalize(curr.position.x - prev.x, curr.position.y - prev.y);
            } else {
                Point2D.Double prev = bezierPoints.get(i - 1).position;
                Point2D.Double next = bezierPoints.get(i + 1).position;
                Point2D.Double incoming = normalize(curr.position.x - prev.x, curr.position.y - prev.y);
                Point2D.Double outgoing = normalize(next.x - curr.position.x, next.y - curr.position.y);
                tangent = normalize(incoming.x + outgoing.x, incoming.y + outgoing.y);
            }

            // Normal is perpendicular to tangent
            Point2D.Double normal = new Point2D.Double(-tangent.y, tangent.x);

            // Offset left and right from center by half-width in rotated space
            Point2D.Double left = new Point2D.Double(
                curr.position.x + normal.x * curr.halfWidth,
                curr.position.y + normal.y * curr.halfWidth
            );
            Point2D.Double right = new Point2D.Double(
                curr.position.x - normal.x * curr.halfWidth,
                curr.position.y - normal.y * curr.halfWidth
            );

            Point2D.Double leftImg = rotatedToImage(left, data);
            Point2D.Double rightImg = rotatedToImage(right, data);

            if (leftImg != null && rightImg != null) {
                leftEdge.add(leftImg);
                rightEdge.add(rightImg);
            }
        }

        if (leftEdge.isEmpty()) return null;

        // Build polygon: left edge forward, right edge backward
        Path2D.Double ribbon = new Path2D.Double();
        ribbon.moveTo(leftEdge.get(0).x, leftEdge.get(0).y);

        for (int i = 1; i < leftEdge.size(); i++) {
            ribbon.lineTo(leftEdge.get(i).x, leftEdge.get(i).y);
        }

        for (int i = rightEdge.size() - 1; i >= 0; i--) {
            ribbon.lineTo(rightEdge.get(i).x, rightEdge.get(i).y);
        }

        ribbon.closePath();

        return ribbon;
    }

    /**
     * Returns a normalized direction vector for the given (x, y) components.
     * Falls back to (1, 0) if the length is near zero.
     */
    private Point2D.Double normalize(double x, double y) {
        double length = Math.sqrt(x * x + y * y);

        if (length < 1e-10) return new Point2D.Double(1, 0);

        return new Point2D.Double(x / length, y / length);
    }

    /**
     * Transforms a point from rotated image space back to original image coordinates
     * by inverting the rotation transform. Returns null if the transform is non-invertible.
     */
    private Point2D.Double rotatedToImage(Point2D.Double rotated, ImageData data) {
        if (rotated == null) return null;

        try {
            Point2D.Double original = new Point2D.Double();
            data.rotation.inverseTransform(rotated, original);

            return original;
        } catch (NoninvertibleTransformException e) {
            return null;
        }
    }

    /**
     * Computes the base half-thickness for a ribbon segment based on the cell's
     * luminance and Sobel magnitude. Dark areas always produce thick lines;
     * lighter areas rely on edge strength to maintain width.
     */
    private double computeBaseHalfThickness(ColorAccumulator cell, int kernelSize) {
        if (cell == null || cell.count == 0) {
            return 0.0;
        }

        // Gray value normalized (0.0 = black, 1.0 = white)
        double grayNorm = cell.getGrayScale() / 255.0;
        double darkness = 1.0 - grayNorm; // 1.0 = black, 0.0 = white

        // Base width scaled by darkness (max width = half the kernel)
        double baseWidth = (kernelSize / 2.0) * darkness;

        // Sobel magnitude is low in solid black areas (no variance), so we use
        // darkness as a floor: black areas stay thick regardless of edge strength
        double sobelMag = cell.magnitude; // 0..1
        double calculatedMagnitude = Math.max(sobelMag, darkness);

        double thick = baseWidth * calculatedMagnitude;

        return (thick > lineThicknessMIN) ? thick : 0.0;
    }
}