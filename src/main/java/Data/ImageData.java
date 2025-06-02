package Data;

import Halftone.GetDataFromImage;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ImageData {
    public ColorAccumulator[][] avgGrid;
    public AffineTransform rotation;
    public double[] bounds;
    
    public ImageData(BufferedImage input, int kernelSize, double angle) {
        // Prepare rotation transform
        double theta = Math.toRadians(angle);
        double centerX = input.getWidth() / 2.0;
        double centerY = input.getHeight() / 2.0;
        rotation = AffineTransform.getRotateInstance(theta, centerX, centerY);
        
        // Calculate rotated bounds
        GetDataFromImage dataFetcher = new GetDataFromImage();
        bounds = dataFetcher.calculateRotatedBounds(input, rotation);
        
        // Compute color accumulators per kernel
        avgGrid = dataFetcher.computeColorAccumulators(input, angle, kernelSize, bounds, rotation);
    }

    public ImageData(ColorAccumulator[][] avgGrid, AffineTransform rotation, double[] bounds) {
        this.avgGrid = avgGrid;
        this.rotation = rotation;
        this.bounds = bounds;
    }
}