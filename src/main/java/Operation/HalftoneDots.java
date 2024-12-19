package Operation;

import java.awt.*;
import java.awt.image.BufferedImage;

public class HalftoneDots {
    /*
    Generate a 2D grid of kernels
    */
    public int[][] generateKernelRadii(BufferedImage image, int kernelSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        int numKernelsX = (int) Math.ceil((double) width / kernelSize);
        int numKernelsY = (int) Math.ceil((double) height / kernelSize);
        
        int[][] radii = new int[numKernelsY][numKernelsX];
        
        for (int yKernel = 0; yKernel < numKernelsY; yKernel++) {
            for (int xKernel = 0; xKernel < numKernelsX; xKernel++) {
                int xStart = xKernel * kernelSize;
                int yStart = yKernel * kernelSize;
                
                int currentKernelWidth = Math.min(kernelSize, width - xStart);
                int currentKernelHeight = Math.min(kernelSize, height - yStart);
                
                radii[yKernel][xKernel] = calculateKernelLuminanceRadius(image, xStart, yStart, currentKernelWidth, currentKernelHeight, kernelSize);
            }
        }
        
        return radii;
    }
    
    /*
    Calculate the radius in each kernel based on luminosity
    */
    private int calculateKernelLuminanceRadius(BufferedImage image, int xStart, int yStart, int width, int height, int kernelSize) {
        double luminanceSum = 0.0;
        int pixelCount = 0;
        
        for (int x = xStart; x < xStart + width; x++) {
            for (int y = yStart; y < yStart + height; y++) {
                int rgb = image.getRGB(x, y);
                Color color = new Color(rgb, true);
                
                double opacity = color.getAlpha() / 255.0;
                double baseLuminance = 0.299 * color.getRed()
                                     + 0.587 * color.getGreen()
                                     + 0.114 * color.getBlue();
                
                double adjustedLuminance = baseLuminance + (1 - opacity) * 255;
                
                luminanceSum += adjustedLuminance;
                pixelCount++;
            }
        }
        
        double averageLuminance = luminanceSum / pixelCount;
        
        int maxRadius = (int) Math.ceil((kernelSize * Math.sqrt(2)) / 2);
        int radius = (int) (maxRadius * (1 - averageLuminance / 255));
        
        return radius;
    }
    
    /*
    Draw dots to image based on radius
    */
    public BufferedImage drawHalftoneDots(Color foregroundColor, int width, int height, int kernelSize, int[][] radii) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        
        g2d.setColor(foregroundColor);
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        for (int yKernel = 0; yKernel < radii.length; yKernel++) {
            for (int xKernel = 0; xKernel < radii[yKernel].length; xKernel++) {
                int radius = radii[yKernel][xKernel];
                
                if (radius > 0) {
                    int centerX = xKernel * kernelSize + kernelSize / 2;
                    int centerY = yKernel * kernelSize + kernelSize / 2;
                    
                    g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
                }
            }
        }
        
        g2d.dispose();
        
        return result;
    }
}