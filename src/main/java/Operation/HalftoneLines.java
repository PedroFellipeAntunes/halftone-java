package Operation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;

public class HalftoneLines {
    private int[] lineSpacing;
    private int horizontalBlurSize = 0;
    
    public void setHorizontalBlur (int horizontalBlurSize) {
        // Cannot be negative
        if (horizontalBlurSize >= 0) {
            this.horizontalBlurSize = horizontalBlurSize;
        }
    }
    
    /*
    Generates a 3D array of data to store kernels
    offsets[ammount of kernels][ammount of segments in kernel][2 values to offset y]
    */
    public int[][][] generateKernels(BufferedImage image, int kernelSize, boolean randomLineOffset, int lineSpacingLimit) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        
        int numKernels = (int) Math.ceil((double) imageHeight / kernelSize);
        
        lineSpacing = new int[numKernels];
        
        generateRandomSpacing(randomLineOffset, lineSpacingLimit);
        
        // [line n][segment x][2 values of y]
        int[][][] offsets = new int[numKernels][imageWidth][2];
        
        for (int index = 0, yStart = 0; yStart < imageHeight; yStart += kernelSize + lineSpacing[index]) {
            int kernelIndex = yStart / kernelSize;
            index = kernelIndex;
            
            // Considering that the last kernel can be smaller than the original size
            int currentKernelSize = Math.min(kernelSize, imageHeight - yStart);
            
            for (int x = 0; x < imageWidth; x++) {
                offsets[kernelIndex][x] = calculateColumnLuminance(image, x, yStart, currentKernelSize);
            }
        }
        
        return offsets;
    }
    
    /*
    Create a array of random spacings between kernels with same size as kernel ammount
    */
    private void generateRandomSpacing(boolean randomLineSpacing, int randomSpacingLimit) {
        Random random = new Random();
        
        for (int i = 0; i < lineSpacing.length; i++) {
            if (randomLineSpacing) {
                lineSpacing[i] = random.nextInt(randomSpacingLimit);
            } else {
                lineSpacing[i] = 0; // Just 0 it ¯\_(ツ)_/¯
            }
        }
    }
    
    /*
    Calculate the luminance value of the vertical segment of the current kernel
    Horizontal blur will increase the area which the data is sampled
    Offset is 2 values which will move the value of y of a point in a polygon
    y1 = negative offset, moves point up
    y2 = positive offset, moves point down
    */
    private int[] calculateColumnLuminance(BufferedImage image, int x, int yStart, int kernelSize) {
        double luminanceSum = 0.0;
        int columnCount = 0;
        
        for (int dx = -horizontalBlurSize; dx <= horizontalBlurSize; dx++) {
            int currentX = x + dx;
            
            if (currentX >= 0 && currentX < image.getWidth()) {
                for (int y = yStart; y < yStart + kernelSize && y < image.getHeight(); y++) {
                    int rgb = image.getRGB(currentX, y);
                    Color color = new Color(rgb, true);
                    
                    double opacity = color.getAlpha() / 255.0; // Normalized
                    
                    double baseLuminance = 0.299 * color.getRed()
                                         + 0.587 * color.getGreen()
                                         + 0.114 * color.getBlue();
                    
                    // Apply inverted alpha (more transparency == higher luminace)
                    double adjustedLuminance = baseLuminance + (1 - opacity) * 255;
                    
                    luminanceSum += adjustedLuminance;
                }
                
                columnCount++;
            }
        }
        
        double averageLuminance = luminanceSum / (columnCount * kernelSize);
        
        // Higher luminance generates smaller offset value, lower leads to greater offset
        int yOffset = (int) (kernelSize / 2 * (1 - averageLuminance / 255));
        
        return new int[]{-yOffset, yOffset};
    }
    
    /*
    Generates a new transparent image with the lines drawn on
    Lines are generated as polygons with n vertices that go from left to right of the image
    vertices_count = image_width * 2
    polygon_count = kernel_count
    */
    public BufferedImage drawPolygonsInKernels(Color foregroundColor, int imageWidth, int imageHeight, int[][][] offsets, int kernelSize) {
        BufferedImage drawnImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = drawnImage.createGraphics();
        
        g2d.setColor(foregroundColor);
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        for (int kernelIndex = 0; kernelIndex < offsets.length; kernelIndex++) {
            Polygon polygon = new Polygon();
            
            // Start position for current y
            int yStart = kernelIndex * kernelSize;
            
            // Add point to polygon going clockwise throught the array
            // Left to right
            for (int x = 0; x < offsets[kernelIndex].length; x++) {
                int y = yStart + offsets[kernelIndex][x][0];
                
                polygon.addPoint(x, y);
            }
            
            // Right to left
            for (int x = offsets[kernelIndex].length - 1; x >= 0; x--) {
                int y = yStart + offsets[kernelIndex][x][1];
                
                polygon.addPoint(x, y);
            }
            
            g2d.fill(polygon);
        }
        
        g2d.dispose();
        
        return drawnImage;
    }
    
    public BufferedImage drawPolygonsInKernelsSine(Color foregroundColor, int imageWidth, int imageHeight, int[][][] offsets, int kernelSize, double frequencyFactor, int amplitude) {
        BufferedImage drawnImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = drawnImage.createGraphics();
        
        g2d.setColor(foregroundColor);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Define sine wave values
        double frequency = frequencyFactor * 2 * Math.PI / imageWidth;
        
        for (int kernelIndex = 0; kernelIndex < offsets.length; kernelIndex++) {
            Polygon polygon = new Polygon();
            
            // Start position for current y
            int yStart = kernelIndex * kernelSize;
            
            // Add point to polygon going clockwise throught the array
            // Left to right
            for (int x = 0; x < offsets[kernelIndex].length; x++) {
                int sineY = yStart + (int) (amplitude * Math.sin(frequency * x));
                int y = sineY + offsets[kernelIndex][x][0];
                
                polygon.addPoint(x, y);
            }
            
            // Right to left
            for (int x = offsets[kernelIndex].length - 1; x >= 0; x--) {
                int sineY = yStart + (int) (amplitude * Math.sin(frequency * x));
                int y = sineY + offsets[kernelIndex][x][1];
                
                polygon.addPoint(x, y);
            }
            
            g2d.fill(polygon);
        }
        
        g2d.dispose();
        
        return drawnImage;
    }
}