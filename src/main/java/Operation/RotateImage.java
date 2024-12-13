package Operation;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class RotateImage {
    /*
    Rotate the image to a new image which is bigger than the original to avoid cut off
    */
    public BufferedImage rotateImage(BufferedImage image, double angle) {
        int w = image.getWidth();
        int h = image.getHeight();
        
        // Calculate new size for the rotated image
        double radians = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));
        
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);
        
        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        
        // Get the center of the image to rotate
        int x = (newWidth - w) / 2;
        int y = (newHeight - h) / 2;
        
        Graphics2D g2d = rotated.createGraphics();
        g2d.rotate(radians, newWidth / 2, newHeight / 2);
        
        // Draw image to new bigger image
        g2d.drawImage(image, x, y, null);
        g2d.dispose();
        
        return rotated;
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