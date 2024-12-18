package Operation;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class RotateImage {
    /*
    Rotate the image to a new image which is bigger than the original to avoid cut off
    */
    public BufferedImage rotateImage(BufferedImage image, double angle, int extendAreaSize) {
        BufferedImage extendedImage = image;
        
        // Scale image up
        if (extendAreaSize != 0) {
            extendedImage = extendImageWithBorder(image, extendAreaSize);
        }
        
        int w = extendedImage.getWidth();
        int h = extendedImage.getHeight();
        
        // Calculate new image size
        double radians = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));
        
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);
        
        // If the original size of the image is greater, keep that value
        // Used to fix the proper rotation later
        newWidth = Math.max(image.getWidth(), newWidth);
        newHeight = Math.max(image.getHeight(), newHeight);
        
        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        
        int x = (newWidth - w) / 2;
        int y = (newHeight - h) / 2;
        
        // Rotate and draw over bigger image
        Graphics2D g2d = rotated.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        g2d.rotate(radians, newWidth / 2, newHeight / 2);
        
        g2d.drawImage(extendedImage, x, y, null);
        g2d.dispose();
        
        return rotated;
    }
    
    /*
    Extend image area by copying the sides
    Only necessary since we now account for the alpha value
    Gives greater weight to borders when calculating luminance average
    */
    private BufferedImage extendImageWithBorder(BufferedImage image, int extendAreaSize) {
        int w = image.getWidth();
        int h = image.getHeight();
        
        int newWidth = w + 2 * extendAreaSize;
        int newHeight = h + 2 * extendAreaSize;
        
        BufferedImage extended = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = extended.createGraphics();
        
        g2d.drawImage(image, extendAreaSize, extendAreaSize, null);
        
        // Copy sides of the image
        for (int x = 0; x < w; x++) {
            int topPixel = image.getRGB(x, 0);
            int bottomPixel = image.getRGB(x, h - 1);
            
            for (int y = 0; y < extendAreaSize; y++) {
                extended.setRGB(x + extendAreaSize, y, topPixel);                         // Up
                extended.setRGB(x + extendAreaSize, newHeight - 1 - y, bottomPixel);      // Down
            }
        }
        
        for (int y = 0; y < h; y++) {
            int leftPixel = image.getRGB(0, y);
            int rightPixel = image.getRGB(w - 1, y);
            
            for (int x = 0; x < extendAreaSize; x++) {
                extended.setRGB(x, y + extendAreaSize, leftPixel);                        // Left
                extended.setRGB(newWidth - 1 - x, y + extendAreaSize, rightPixel);        // Right
            }
        }
        
        // Copy the corners
        int topLeftPixel = image.getRGB(0, 0);
        int topRightPixel = image.getRGB(w - 1, 0);
        int bottomLeftPixel = image.getRGB(0, h - 1);
        int bottomRightPixel = image.getRGB(w - 1, h - 1);
        
        for (int y = 0; y < extendAreaSize; y++) {
            for (int x = 0; x < extendAreaSize; x++) {
                extended.setRGB(x, y, topLeftPixel);                                      // Left Upper Corner
                extended.setRGB(newWidth - 1 - x, y, topRightPixel);                      // Right Upper Corner
                extended.setRGB(x, newHeight - 1 - y, bottomLeftPixel);                   // Left Lower Corner
                extended.setRGB(newWidth - 1 - x, newHeight - 1 - y, bottomRightPixel);   // Right Lower Corner
            }
        }
        
        g2d.dispose();
        return extended;
    }
    
    /*
    Rotate the image back to it's original angle without altering size
    */
    public BufferedImage rotateImageSameSize(BufferedImage image, double angle) {
        int w = image.getWidth();
        int h = image.getHeight();
        
        double radians = Math.toRadians(angle);
        
        BufferedImage rotated = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        
        // Rotate to original angle
        Graphics2D g2d = rotated.createGraphics();
        g2d.translate(w / 2.0, h / 2.0);
        g2d.rotate(radians);
        
        g2d.drawImage(image, -w / 2, -h / 2, null);
        g2d.dispose();
        
        return rotated;
    }
    
    /*
    Correct image to it's original size
    */
    public BufferedImage restoreOriginalSize(BufferedImage image, int originalWidth, int originalHeight) {
        BufferedImage restored = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = restored.createGraphics();
        
        int x = (originalWidth - image.getWidth()) / 2;
        int y = (originalHeight - image.getHeight()) / 2;
        
        g2d.drawImage(image, x, y, null);
        g2d.dispose();
        
        return restored;
    }
}