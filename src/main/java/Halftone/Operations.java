package Halftone;

import Halftone.Util.ImageMerger;
import Halftone.Util.ResizeImage;
import Halftone.Util.TestMethods;
import ColorSeparator.ColorChannelSeparator;
import Data.ConfigData;
import FileManager.PngReader;
import FileManager.PngSaver;
import Windows.ImageViewer;
import Data.ImageData;
import Data.OpType;
import Data.TYPE;

import static Util.Timing.measure;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Operations {
    private final int stipplingDensity = 85; // This is the optimal value for all kernel sizes this code uses, found it by doing some data crunching
    
    private final int kernelSize;
    private final double angle;
    private final Data.TYPE type;
    private final OpType opType;
    private final int polySides;
    private Color backgroundColor;
    private Color foregroundColor;

    public boolean skip = false; // Flag to skip displaying
    public boolean save = true; // Flag to save final image automatically

    /**
     * Initializes the Halftone Operations instance with the desired parameters.
     *
     * @param config ConfigData object which contains all the possible configuration
     * variables for the halftone process.
     */
    public Operations(ConfigData config) {
        this.kernelSize = config.scale >= 1 ? config.scale : 1;
        this.angle = config.angle;
        this.type = config.type;
        this.backgroundColor = config.colors[0];
        this.foregroundColor = config.colors[1];
        this.opType = config.opType;
        this.polySides = config.polySides;
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
        
        // Optional test
//        testMethods(expanded, kernelSize, angle, filePath);

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

        switch (opType) {
            case CMYK ->
                prefix = String.format("Halftone[%s;%d;CMYK]", formatTypeName(), kernelSize);
            case RGB ->
                prefix = String.format("Halftone[%s;%d;RGB]", formatTypeName(), kernelSize);
            default ->
                prefix = String.format("Halftone[%s;%d;%.1f]", formatTypeName(), kernelSize, angle);
        }

        saver.saveToFile(prefix, filePath, image);
    }

    // --------------------------- Private/internal methods ---------------------------
    private String formatTypeName() {
        if (type == TYPE.Polygons) {
            return String.format("%s(%d)", type, polySides);
        }
        
        return type.toString();
    }

    private BufferedImage process(BufferedImage expanded) {
        // Create ImageData object with kernel info and rotation
        ImageData id = new ImageData(expanded, kernelSize, angle);

        // Apply the selected halftone pattern and return processed image
        return measure("Halftone pattern: " + type, () -> applyHalftone(expanded, id, this.backgroundColor, this.foregroundColor));
    }

    private BufferedImage processCMYK(BufferedImage expanded) {
        // Separate expanded image into CMYK channels
        ColorChannelSeparator ccs = new ColorChannelSeparator();
        BufferedImage[] cmyk = ccs.separateCMYK(expanded, 0, false, false);

        // Define rotation angles and channel colors for halftone
        double[] angles = {15, 75, 0, 45};
        Color[] colors = {Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.BLACK};
        String[] channelNames = {"C", "M", "Y", "K"};
        
        // Set white background and channel-specific foreground color
        this.backgroundColor = Color.WHITE;

        BufferedImage[] halftones = new BufferedImage[angles.length];
        System.out.println("Halftone pattern: " + type + " (CMYK)");

        int threads = Math.max(1, Runtime.getRuntime().availableProcessors());
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Callable<BufferedImage>> tasks = new ArrayList<>();
        
        // Process each CMYK channel independently
        for (int i = 0; i < angles.length; i++) {
            final int index = i;

            tasks.add(() -> measure("Applying pattern: " + channelNames[index], () -> {
                ImageData id = new ImageData(cmyk[index], kernelSize, angles[index]);
                
                // Apply halftone to the current channel and store
                return applyHalftone(cmyk[index], id, this.backgroundColor, colors[index]);
            }));
        }

        try {
            List<Future<BufferedImage>> results = executor.invokeAll(tasks);
            
            for (int i = 0; i < results.size(); i++) {
                halftones[i] = results.get(i).get();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            
            Thread.currentThread().interrupt();
            
            throw new RuntimeException("CMYK processing interrupted", e);
        } catch (ExecutionException e) {
            executor.shutdownNow();
            
            throw new RuntimeException("Error during CMYK channel processing", e.getCause());
        } finally {
            executor.shutdown();
        }

        // Merge all CMYK halftone images using multiply blend mode
        return measure("Merging CMYK images", () -> new ImageMerger().mergeImagesMultiply(halftones));
    }

    private BufferedImage processRGB(BufferedImage expanded) {
        // Separate expanded image into RGB channels
        ColorChannelSeparator ccs = new ColorChannelSeparator();
        BufferedImage[] rgb = ccs.separateRBGA(expanded, 0, false, false);

        // Define rotation angles and channel background colors for halftone
        double[] angles = {0, 60, 120};
        Color[] colors = {Color.RED, Color.GREEN, Color.BLUE};
        String[] channelNames = {"R", "G", "B"};

        // Set black as foreground for RGB channels
        this.foregroundColor = Color.BLACK;

        BufferedImage[] halftones = new BufferedImage[angles.length];
        System.out.println("Halftone pattern: " + type + " (RGB mode)");

        int threads = Math.max(1, Runtime.getRuntime().availableProcessors());
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Callable<BufferedImage>> tasks = new ArrayList<>();

        // Process each RGB channel independently
        for (int i = 0; i < angles.length; i++) {
            final int index = i;

            tasks.add(() -> measure("Applying pattern: " + channelNames[index], () -> {
                ImageData id = new ImageData(rgb[index], kernelSize, angles[index]);

                // Apply halftone to the current channel and store
                return applyHalftone(rgb[index], id, colors[index], this.foregroundColor);
            }));
        }

        try {
            List<Future<BufferedImage>> results = executor.invokeAll(tasks);

            for (int i = 0; i < results.size(); i++) {
                halftones[i] = results.get(i).get();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            
            Thread.currentThread().interrupt();
            
            throw new RuntimeException("RGB processing interrupted", e);
        } catch (ExecutionException e) {
            executor.shutdownNow();
            
            throw new RuntimeException("Error during RGB channel processing", e.getCause());
        } finally {
            executor.shutdown();
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
    
    // Thread safe version
    private BufferedImage applyHalftone(BufferedImage image, ImageData id, Color bg, Color fg) {
        switch (type) {
            case Dots -> {
                Ht_Dot dotGen = new Ht_Dot();
                dotGen.backgroundColor = bg;
                dotGen.foregroundColor = fg;
                
                return dotGen.applyDotPattern(image, kernelSize, id);
            }
            case AlternatingTriangles -> {
                Ht_Dot dotGen = new Ht_Dot();
                dotGen.backgroundColor = bg;
                dotGen.foregroundColor = fg;
                
                return dotGen.applyAlternatingTrianglePattern(image, kernelSize, id);
            }
            case Polygons -> {
                Ht_Dot dotGen = new Ht_Dot();
                dotGen.backgroundColor = bg;
                dotGen.foregroundColor = fg;
                
                return dotGen.applyPolygonPattern(image, kernelSize, id, polySides);
            }
            case Stippling -> {
                Ht_Dot dotGen = new Ht_Dot();
                dotGen.backgroundColor = bg;
                dotGen.foregroundColor = fg;
                
                return dotGen.applyStipplingPattern(image, kernelSize, id, stipplingDensity);
            }
            case Lines -> {
                Ht_Line lineGen = new Ht_Line();
                lineGen.backgroundColor = bg;
                lineGen.foregroundColor = fg;
                
                return lineGen.applyLinePattern(image, kernelSize, id);
            }
            default -> {
                Ht_Line sineGen = new Ht_Line();
                sineGen.backgroundColor = bg;
                sineGen.foregroundColor = fg;
                
                return sineGen.applySinePattern(image, kernelSize, id);
            }
        }
    }

    private BufferedImage readImage(String path) {
        // Reads a PNG file from disk and returns as BufferedImage
        return new PngReader().readPNG(path, false);
    }
}