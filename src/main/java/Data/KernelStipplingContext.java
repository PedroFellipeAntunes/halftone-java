package Data;

import java.awt.geom.AffineTransform;

public class KernelStipplingContext {
    public final ColorAccumulator acc;
    public final int kernelRow;
    public final int kernelCol;
    public final int pointsInKernel;
    public final double leftXr;
    public final double topYr;
    
    public final int kernelSize;
    public final double radius;
    public final double diameter;
    public final AffineTransform rotation;

    // RNG constants
    public final java.util.function.LongUnaryOperator splitmix;
    public final long MASK53;
    public final double INV_2POW53;

    public KernelStipplingContext(ColorAccumulator acc, int kernelRow, int kernelCol, int pointsInKernel, double leftXr, double topYr, int kernelSize, double radius, AffineTransform rotation) {
        this.acc = acc;
        this.kernelRow = kernelRow;
        this.kernelCol = kernelCol;
        this.pointsInKernel = pointsInKernel;
        this.leftXr = leftXr;
        this.topYr = topYr;
        this.kernelSize = kernelSize;
        this.radius = radius;
        this.diameter = radius * 2;
        this.rotation = rotation;

        // Initialize RNG
        this.splitmix = (s) -> {
            long z = (s + 0x9E3779B97F4A7C15L);
            
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            z = z ^ (z >>> 31);
            
            return z;
        };
        
        this.MASK53 = (1L << 53) - 1L;
        this.INV_2POW53 = 1.0 / (double) (1L << 53);
    }
}