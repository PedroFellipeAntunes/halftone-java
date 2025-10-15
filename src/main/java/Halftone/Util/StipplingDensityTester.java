package Halftone.Util;

import FileManager.PngReader;
import Halftone.Ht_Dot;
import Data.ImageData;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This utility class performs a systematic experimental sweep to analyze the
 * effect of stippling density across multiple kernel sizes on a given input image.
 *
 * The process involves:
 * 1. Iterating through a defined range of kernel sizes and densities.
 * 2. Generating an average reference image (per kernel) using the same geometry
 * and rotation.
 * 3. Applying a stippling pattern to the expanded image.
 * 4. Measuring the delta (absolute mean difference) between the stippled output
 * and the average reference.
 * 5. Writing all results to a semicolon-separated log file.
 *
 * Input: - A PNG grayscale gradient test image.
 *
 * Output: - A text file "log.txt" containing: kernelSize;density;delta
 */

public class StipplingDensityTester {
    private static final String INPUT_PATH = "gradient_test.png";
    private static final double ANGLE = 0.0;

    private static final int KERNEL_MIN = 1;
    private static final int KERNEL_MAX = 100;
    private static final int KERNEL_STEP = 1;

    private static final int DENSITY_MIN = 0;
    private static final int DENSITY_MAX = 100;

    private static final int GC_DENSITY_INTERVAL = 10;
    private static final int GC_SLEEP_MS_AFTER_BLOCK = 25;
    private static final int GC_SLEEP_MS_SMALL = 5;

    // Lock object to synchronize writes to PrintWriter
    private static final Object pwLock = new Object();

    public static void main(String[] args) {
        try {
            PngReader reader = new PngReader();
            BufferedImage src = reader.readPNG(INPUT_PATH, true);
            
            if (src == null) {
                throw new IOException("Failed to load input image: " + INPUT_PATH);
            }

            ResizeImage resizer = new ResizeImage();

            File inputFile = new File(INPUT_PATH);
            File logFile = new File(inputFile.getParentFile(), "log.txt");

            runTests(src, resizer, logFile);

            System.out.println("Finished. Log saved at: " + logFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Fatal error running StipplingTester:");
            e.printStackTrace();
        }
    }

    private static void runTests(BufferedImage src, ResizeImage resizer, File logFile) throws IOException {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        try (PrintWriter pw = new PrintWriter(logFile)) {
            pw.println("kernelSize;density;delta");

            for (int kernelSize = KERNEL_MIN; kernelSize <= KERNEL_MAX; kernelSize += KERNEL_STEP) {
                final int ks = kernelSize;
                
                executor.submit(() -> {
                    try {
                        System.out.println("Starting kernelSize = " + ks);

                        BufferedImage expanded = resizer.expandBorder(src, ks);
                        
                        try {
                            processKernelSize(expanded, ks, pw);
                        } finally {
                            if (expanded != null) {
                                expanded.flush();
                                expanded = null;
                            }
                            
                            System.gc();
                            Thread.sleep(GC_SLEEP_MS_AFTER_BLOCK);
                        }

                        System.out.println("✔ KernelSize " + ks + " finished.");
                    } catch (Exception e) {
                        System.err.println("Error in kernelSize " + ks);
                        e.printStackTrace();
                    }
                });
            }

            // Shutdown executor and wait for all tasks to finish
            executor.shutdown();
            
            try {
                if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                    System.err.println("Timeout: Not all kernel tasks finished!");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            pw.flush();
            System.gc();
            
            try { Thread.sleep(GC_SLEEP_MS_AFTER_BLOCK); } catch (InterruptedException ignored) {}
        }
    }

    private static void processKernelSize(BufferedImage expanded, int kernelSize, PrintWriter pw) {
        for (int density = DENSITY_MIN; density <= DENSITY_MAX; density++) {
            ImageData id;
            Ht_Dot dotGen = new Ht_Dot();
            BufferedImage avgRef = null;
            BufferedImage stippled = null;

            try {
                id = new ImageData(expanded, kernelSize, ANGLE);
                
                avgRef = TestMethods.applyAvgColorsTest(expanded, ANGLE, kernelSize, id);
                stippled = dotGen.applyStipplingPattern(expanded, kernelSize, id, density);

                double delta = computeDifference(stippled, avgRef, kernelSize);

                synchronized (pwLock) {
                    pw.printf("%d;%d;%.6f%n", kernelSize, density, delta);
                }

//                System.out.printf("kernel=%03d / density=%03d / delta=%.6f%n", kernelSize, density, delta);
            } catch (Exception ex) {
                System.err.printf("Error processing kernel=%d density=%d : %s%n", kernelSize, density, ex.getMessage());
                ex.printStackTrace();
            } finally {
                if (stippled != null) { stippled.flush(); stippled = null; }
                
                if (avgRef != null) { avgRef.flush(); avgRef = null; }
                
                id = null;
                dotGen = null;

                if (density % GC_DENSITY_INTERVAL == 0) {
                    System.gc();
                    
                    try { Thread.sleep(GC_SLEEP_MS_SMALL); } catch (InterruptedException ignored) {}
                }
            }
        }
    }

    private static double computeDifference(BufferedImage a, BufferedImage b, int kernelSize) {
        Raster ra = a.getRaster();
        Raster rb = b.getRaster();
        
        int w = Math.min(a.getWidth(), b.getWidth());
        int h = Math.min(a.getHeight(), b.getHeight());

        long kernelCount = 0;
        double sumAbsDiff = 0.0;

        for (int ky = 0; ky <= h - kernelSize; ky += kernelSize) {
            for (int kx = 0; kx <= w - kernelSize; kx += kernelSize) {
                double avgA = kernelAverageIntensity(ra, kx, ky, kernelSize);
                double avgB = kernelAverageIntensity(rb, kx, ky, kernelSize);
                
                sumAbsDiff += Math.abs(avgA - avgB);
                
                kernelCount++;
            }
        }

        if (kernelCount == 0) return 0.0;
        
        return (sumAbsDiff / kernelCount) / 255.0;
    }

    private static double kernelAverageIntensity(Raster r, int kx, int ky, int ks) {
        int bands = r.getNumBands();
        int effectiveBands = (bands == 4) ? 3 : bands;

        double sum = 0.0;
        int count = 0;

        for (int y = ky; y < ky + ks; y++) {
            for (int x = kx; x < kx + ks; x++) {
                if (x >= r.getWidth() || y >= r.getHeight()) continue;
                
                double pixelSum = 0.0;
                
                for (int b = 0; b < effectiveBands; b++) {
                    pixelSum += r.getSample(x, y, b);
                }
                
                sum += pixelSum / effectiveBands;
                count++;
            }
        }

        return (count == 0) ? 0.0 : sum / count;
    }
}