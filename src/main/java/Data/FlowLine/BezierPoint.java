package Data.FlowLine;

import java.awt.geom.Point2D;

/**
 * Holds an interpolated point along a Bézier curve together with its
 * associated ribbon half-width.
 */
public class BezierPoint {
    public Point2D.Double position;
    public double halfWidth;

    /**
     * Creates a new BezierPoint with the given position and half-width.
     *
     * @param position The interpolated 2D position on the Bézier curve.
     * @param halfWidth The ribbon half-width at this point.
     */
    public BezierPoint(Point2D.Double position, double halfWidth) {
        this.position = position;
        this.halfWidth = halfWidth;
    }
}