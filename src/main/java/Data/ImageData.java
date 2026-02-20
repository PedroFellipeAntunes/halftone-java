package Data;

import Halftone.GetDataFromImage;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ImageData {
    public ColorAccumulator[][] avgGrid;
    public AffineTransform rotation;
    public double[] bounds;
    
    /**
     * Constructs an ImageData object that precomputes geometric and color
     * information from the given input image for halftone or stippling
     * operations.
     *
     * This method performs the following steps:
     * 1. Validates input parameters (image, kernel size, and rotation angle).
     * 2. Builds a rotation transform centered on the image using the given angle.
     * 3. Calculates the bounding box of the rotated image.
     * 4. Divides the rotated image into a grid of square kernels with the given size.
     * 5. Computes and stores the color accumulation and average color for each kernel region.
     *
     * The resulting structure allows later algorithms to map pixels to their
     * corresponding kernel and quickly access precomputed color statistics,
     * improving efficiency for visualization and halftoning.
     *
     * @param input The source image to analyze.
     * @param kernelSize Size (in pixels) of each square kernel.
     * @param angle Rotation angle in degrees (0–360) used to orient the kernel
     * grid.
     */
    public ImageData(BufferedImage input, int kernelSize, double angle) {
        this(input, kernelSize, angle, false, 0);
    }
    
    /**
     * Constructs an ImageData object with optional Sobel gradient computation.
     *
     * @param input The source image to analyze.
     * @param kernelSize Size (in pixels) of each square kernel.
     * @param angle Rotation angle in degrees (0–360) used to orient the kernel grid.
     * @param computeSobel If true, computes Sobel gradient angles for each kernel (used by FlowLine).
     * @param sobelBlurRadius Radius of the box blur applied to Sobel angle and magnitude
     * values after computation (0 = no blur).
     */
    public ImageData(BufferedImage input, int kernelSize, double angle, boolean computeSobel, int sobelBlurRadius) {
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
        
        // Optionally compute Sobel gradient angles
        if (computeSobel) {
            dataFetcher.computeSobelAngles(input, kernelSize, bounds, rotation, avgGrid);
            dataFetcher.blurSobelValues(avgGrid, sobelBlurRadius);
        }
    }
}