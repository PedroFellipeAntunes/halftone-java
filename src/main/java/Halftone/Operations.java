package Halftone;

import Halftone.Util.ImageMerger;
import Halftone.Util.ResizeImage;
import Halftone.Util.TestMethods;
import ColorSeparator.ColorChannelSeparator;
import FileManager.PngReader;
import FileManager.PngSaver;
import Windows.ImageViewer;
import Data.ImageData;
import Data.OpType;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Operations {
    private final int kernelSize;
    private final double angle;
    private final Data.TYPE type;
    private final OpType opType;
    private Color backgroundColor;
    private Color foregroundColor;

    public boolean skip = false; // Flag to skip displaying
    public boolean save = true;  // Flag to save final image automatically

    /**
     * Initializes the Halftone Operations instance with the desired parameters.
     *
     * @param kernelSize Size of each halftone kernel in pixels.
     * @param angle Rotation angle (degrees) to apply before halftoning.
     * @param type Halftone type (Dots, Lines, Squares, Triangles, Sine).
     * @param colors Two-color array: [0] = background color, [1] = foreground color.
     * @param opType Operation mode: Default, CMYK, RGB.
     */
    public Operations(int kernelSize, double angle, Data.TYPE type, Color[] colors, OpType opType) {
        this.kernelSize = kernelSize;
        this.angle = angle;
        this.type = type;
        this.backgroundColor = colors[0];
        this.foregroundColor = colors[1];
        this.opType = opType;
    }

    /**
     * Executes the full halftone processing pipeline for a single image file.
     *
     * Pipeline steps:
     * 1. Reads the image from disk using PngReader.
     * 2. Expands image borders to prevent artifacts at edges.
     * 3. Selects the correct processing branch depending on the operation type (Default, CMYK, RGB).
     * 4. Applies the halftone pattern(s) to the image or to each color channel.
     * 5. Crops the expanded borders to restore original dimensions.
     * 6. Optionally displays the result in an ImageViewer.
     * 7. Optionally saves the final image to disk with a descriptive prefix.
     *
     * @param filePath Path to the PNG file to be processed.
     * @throws IOException If reading or saving the image fails.
     */
    public void startProcess(String filePath) throws IOException {
        // 1) Read the original image from disk
        final BufferedImage original = measure("Reading image", () -> readImage(filePath));

        // 2) Expand image borders to avoid edge artifacts during halftone
        final BufferedImage expanded = measure("Expanding image borders", () ->
                new ResizeImage().expandBorder(original, kernelSize)
        );

        // 3) Apply the selected processing pipeline (Default, CMYK, RGB)
        final BufferedImage halftoned = switch (opType) {
            case CMYK -> measure("Applying CMYK", () -> processCMYK(expanded));
            case RGB -> measure("Applying RGB", () -> processRGB(expanded));
            default -> measure("Applying pattern", () -> process(expanded));
        };

        // 4) Crop the expanded borders to restore original dimensions
        final BufferedImage cropped = measure("Cropping image borders", () ->
                new ResizeImage().cropBorder(halftoned, kernelSize)
        );

        // 5) If skip flag is active, optionally save and exit without displaying
        if (skip) {
            System.out.println("- Display skip");
            if (save) {
                saveImage(cropped, filePath);
            }
            return;
        }

        // 6) Display the final halftoned image
        System.out.println("- Displaying result");
        new ImageViewer(cropped, filePath, this);
        System.out.println("FINISHED PROCESS\n");
    }

    /**
     * Saves the processed image to disk with a descriptive filename
     * based on halftone type, kernel size, angle, and operation mode.
     *
     * @param image The processed BufferedImage to save.
     * @param filePath Original file path used to derive the saved file name.
     */
    public void saveImage(BufferedImage image, String filePath) {
        PngSaver saver = new PngSaver();
        String prefix;

        // 1) Build filename prefix based on operation type
        switch (opType) {
            case CMYK -> prefix = String.format("Halftone[%s,%d,CMYK]", type, kernelSize);
            case RGB -> prefix = String.format("Halftone[%s,%d,RGB]", type, kernelSize);
            default -> prefix = String.format("Halftone[%s,%d,%.1f]", type, kernelSize, angle);
        }

        // 2) Save the image using PngSaver utility
        saver.saveToFile(prefix, filePath, image);
    }

    // --------------------------- Private/internal methods ---------------------------

    private BufferedImage process(BufferedImage expanded) {
        // Create ImageData object with kernel info and rotation
        ImageData id = new ImageData(expanded, kernelSize, angle);

        // Apply the selected halftone pattern and return processed image
        return measure("Halftone pattern: " + type, () -> applyHalftone(expanded, id));
    }

    private BufferedImage processCMYK(BufferedImage expanded) {
        // Separate expanded image into CMYK channels
        ColorChannelSeparator ccs = new ColorChannelSeparator();
        BufferedImage[] cmyk = ccs.separateCMYK(expanded, 0, false, false);

        // Define rotation angles and channel colors for halftone
        double[] angles = {15, 75, 0, 45};
        Color[] colors = {Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.BLACK};
        String[] channelNames = {"C", "M", "Y", "K"};

        BufferedImage[] halftones = new BufferedImage[angles.length];
        System.out.println("Halftone pattern: " + type + " (CMYK)");

        // Process each CMYK channel independently
        for (int i = 0; i < angles.length; i++) {
            final int index = i;
            final double angle = angles[index];
            final Color fgColor = colors[index];
            final String channel = channelNames[index];

            ImageData id = new ImageData(cmyk[index], kernelSize, angle);

            // Set white background and channel-specific foreground color
            this.backgroundColor = Color.WHITE;
            this.foregroundColor = fgColor;

            // Apply halftone to the current channel and store
            halftones[index] = measure("Applying pattern: " + channel, () -> applyHalftone(cmyk[index], id));
        }

        // Merge all CMYK halftone images using multiply blend mode
        return measure("Merging CMYK images", () -> new ImageMerger().mergeImagesMultiply(halftones));
    }

    private BufferedImage processRGB(BufferedImage expanded) {
        // Separate expanded image into RGB channels
        ColorChannelSeparator ccs = new ColorChannelSeparator();
        BufferedImage[] rgb = ccs.separateRBGA(expanded, 0, false, false);

        // Define rotation angles and background colors for halftone
        double[] angles = {0, 60, 120};
        Color[] colors = {Color.RED, Color.GREEN, Color.BLUE};
        String[] channelNames = {"R", "G", "B"};

        BufferedImage[] halftones = new BufferedImage[angles.length];
        System.out.println("Halftone pattern: " + type + " (RGB mode)");

        // Process each RGB channel independently
        for (int i = 0; i < angles.length; i++) {
            final int index = i;
            final double angle = angles[index];
            final Color bgColor = colors[index];
            final String channel = channelNames[index];

            ImageData id = new ImageData(rgb[index], kernelSize, angle);

            // Set channel color as background and black as foreground
            this.backgroundColor = bgColor;
            this.foregroundColor = Color.BLACK;

            // Apply halftone to the current channel and store
            halftones[index] = measure("Applying pattern: " + channel, () -> applyHalftone(rgb[index], id));
        }

        // Merge all RGB halftone images using screen blend mode
        return measure("Merging RGB images", () -> new ImageMerger().mergeImagesScreen(halftones));
    }

    private void testMethods(BufferedImage input, int kernelSize, double angle, String filePath) {
        ImageData id = new ImageData(input, kernelSize, angle);

        // Test kernel application
        new ImageViewer(TestMethods.applyKernelTest(input, kernelSize, id), filePath, this);

        // Test averaging colors
        new ImageViewer(TestMethods.applyAvgColorsTest(input, angle, kernelSize, id), filePath, this);
    }

    private BufferedImage applyHalftone(BufferedImage image, ImageData id) {
        // Apply the selected halftone pattern on the image
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
            default -> {
                Ht_Line sineGen = new Ht_Line();
                sineGen.backgroundColor = backgroundColor;
                sineGen.foregroundColor = foregroundColor;
                
                return sineGen.applySinePattern(image, kernelSize, id);
            }
        }
    }

    private BufferedImage readImage(String path) {
        // Reads a PNG file from disk and returns as BufferedImage
        return new PngReader().readPNG(path, false);
    }

    private <T> T measure(String label, Timeable<T> action) {
        // Measure execution time and memory usage of the given action
        Runtime rt = Runtime.getRuntime();
        System.gc();

        long startTime = System.currentTimeMillis();
        long beforeMem = rt.totalMemory() - rt.freeMemory();
        System.out.println("- " + label + " START");

        T result = action.execute();

        long afterMem = rt.totalMemory() - rt.freeMemory();
        long duration = System.currentTimeMillis() - startTime;
        double deltaMB = (afterMem - beforeMem) / 1024.0 / 1024.0;
        double totalMB = afterMem / 1024.0 / 1024.0;

        System.out.printf("- %s time: %dms | Δmem: %.2f MB | total: %.2f MB%n",
                label, duration, deltaMB, totalMB);

        return result;
    }

    @FunctionalInterface
    private interface Timeable<T> {
        T execute();
    }
}