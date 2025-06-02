package Halftone;

import Halftone.Util.ImageMerger;
import Halftone.Util.ResizeImage;
import Halftone.Util.TestMethods;
import ColorSeparator.ColorChannelSeparator;
import FileManager.PngReader;
import FileManager.PngSaver;
import Windows.ImageViewer;
import Data.ImageData;
import Data.TYPE;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Operations {
    // Configuration parameters
    private final int kernelSize;
    private final double angle;
    private final TYPE type;
    private final Color backgroundColor;
    private Color foregroundColor;
    
    private final boolean CMYK;

    /**
     * Initializes the Halftone Operations instance with the desired parameters.
     *
     * @param kernelSize Size of each kernel in pixels.
     * @param angle Rotation angle (in degrees) to apply before halftoning.
     * @param type Halftone type (Dots, Lines, or Sine).
     * @param colors Array of two colors: [0] = background, [1] = foreground.
     * @param CMYK Boolean if true will generate a colored halftone image, by
     * applying the effect to each CMYK channel using the corresponding angles 
     * [15, 75, 0, 45] and merging into a final image.
     */
    public Operations(int kernelSize, double angle, TYPE type, Color[] colors, boolean CMYK) {
        this.kernelSize = kernelSize;
        this.angle = angle;
        this.type = type;
        this.backgroundColor = colors[0];
        this.foregroundColor = colors[1];
        this.CMYK = CMYK;
    }

    /**
     * Executes the full halftone processing pipeline on the specified file.
     *
     * Steps:
     * 1. Read the image from disk.
     * 2. Expand borders to avoid edge artifacts.
     * 3. Compute rotation transform and rotated bounds.
     * 4. Accumulate color averages per kernel.
     * 5. Apply the chosen halftone pattern.
     * 6. Crop the expanded borders.
     * 7. Display the final image.
     *
     * @param filePath Path to the PNG file to process.
     */
    public void startProcess(String filePath) {
        // Read from file
        BufferedImage original = measureTime("Reading image", () -> readImage(filePath));

        // Expand borders
        BufferedImage expanded = measureTime("Expanding image borders", () 
                -> new ResizeImage().expandBorder(original, kernelSize)
        );
        
        // Test methods for kernel rotation
//        testMethods(expanded, kernelSize, angle, filePath);
        
        // Apply pattern
        BufferedImage halftoned;
        
        if (CMYK) {
            halftoned = measureTime("Applying CMYK", () ->
                processCMYK(expanded)
            );
        } else {
            halftoned = measureTime("Applying pattern", () ->
                process(expanded)
            );
        }
        
        // Crop expanded borders
        BufferedImage cropped = measureTime("Cropping image borders", ()
                -> new ResizeImage().cropBorder(halftoned, kernelSize)
        );
        
        // View result
        new ImageViewer(cropped, filePath, this);
    }
    
    private BufferedImage process(BufferedImage expanded) {
        // Data for rotation, bounds and average color grid
        ImageData id = new ImageData(expanded, kernelSize, angle);

        // Apply halftone pattern
        return measureTime("Halftone pattern: " + type, ()
                -> applyHalftone(expanded, id)
        );
    }
    
    private BufferedImage processCMYK(BufferedImage expanded) {
        // Separate image into CMYK colors
        ColorChannelSeparator ccs = new ColorChannelSeparator();
        BufferedImage[] cmyk = ccs.separateCMYK(expanded, 0, false, false);
        
        double[] angles = {15, 75, 0, 45};
        Color[] colors = {Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.BLACK};
        
        BufferedImage[] halftones = new BufferedImage[angles.length];
        
        // Apply halftone to each image
        System.out.println("Halftone pattern: " + type);
        for (int i = 0; i < angles.length; i++) {
            ImageData id = new ImageData(cmyk[i], kernelSize, angles[i]);
            
            foregroundColor = colors[i];
            
            halftones[i] = measureTime("Image: " + i, ()
                    -> applyHalftone(expanded, id)
            );
        }
        
        // Merge images with multiply blend
        return measureTime("Merging CMYK images", () ->
            new ImageMerger().mergeImagesMultiply(halftones)
        );
    }
    
    private void testMethods(BufferedImage input, int kernelSize, double angle, String filePath) {
        ImageData id = new ImageData(input, kernelSize, angle);
        
        new ImageViewer(TestMethods.applyKernelTest(input, kernelSize, id), filePath, this);
        new ImageViewer(TestMethods.applyAvgColorsTest(input, angle, kernelSize, id), filePath, this);
    }
    
    private BufferedImage applyHalftone(BufferedImage image, ImageData id) {
        switch (type) {
            case Dots -> {
                Ht_Dot dotGen = new Ht_Dot();
                dotGen.backgroundColor = backgroundColor;
                dotGen.foregroundColor = foregroundColor;
                
                return dotGen.applyDotPattern(image, kernelSize, id);
            }
            case Squares -> {
                Ht_Dot dotGen = new Ht_Dot();
                dotGen.backgroundColor = backgroundColor;
                dotGen.foregroundColor = foregroundColor;
                
                return dotGen.applySquarePattern(image, kernelSize, id);
            }
            case Triangles -> {
                Ht_Dot dotGen = new Ht_Dot();
                dotGen.backgroundColor = backgroundColor;
                dotGen.foregroundColor = foregroundColor;
                
                return dotGen.applyTrianglePattern(image, kernelSize, id);
            }
            case Lines -> {
                Ht_Line lineGen = new Ht_Line();
                lineGen.backgroundColor = backgroundColor;
                lineGen.foregroundColor = foregroundColor;
                
                return lineGen.applyLinePattern(image, kernelSize, id);
            }
            default -> { // Sine
                Ht_Line sineGen = new Ht_Line();
                sineGen.backgroundColor = backgroundColor;
                sineGen.foregroundColor = foregroundColor;
                
                return sineGen.applySinePattern(
                    image,
                    kernelSize,
                    id
                );
            }
        }
    }
    
    private BufferedImage readImage(String path) {
        return new PngReader().readPNG(path, false);
    }
    
    /**
     * Saves the processed image to disk with an informative filename.
     *
     * @param image The BufferedImage to save.
     * @param filePath The original file path used as a base for naming.
     */
    public void saveImage(BufferedImage image, String filePath) {
        PngSaver saver = new PngSaver();
        
        String prefix;
        
        if (CMYK) {
            prefix = String.format("Halftone[%s,%d,CMYK]", type, kernelSize);
        } else {
            prefix = String.format("Halftone[%s,%d,%.1f]", type, kernelSize, angle);
        }
        
        saver.saveToFile(prefix, filePath, image);
    }

    // ——— Timing helper methods ——————————————————————————————

    private <T> T measureTime(String label, Timeable<T> action) {
        long start = System.currentTimeMillis();
        System.out.println(label);
        T result = action.execute();
        long duration = System.currentTimeMillis() - start;
        System.out.println("TIME: " + duration + "ms");
        
        return result;
    }

    private void measureTime(String label, Runnable action) {
        long start = System.currentTimeMillis();
        System.out.println(label);
        action.run();
        long duration = System.currentTimeMillis() - start;
        System.out.println("TIME: " + duration + "ms");
    }

    @FunctionalInterface
    private interface Timeable<T> {
        T execute();
    }
}