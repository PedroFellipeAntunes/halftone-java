package Operation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Blend {
    private final Color backgroundColor = Color.WHITE;
    
    /*
    Mix the image of generated lines with the blurred version
    */
    public BufferedImage blendImagesWithMultiply(BufferedImage image, BufferedImage blurredImage) {
        int width = Math.min(image.getWidth(), blurredImage.getWidth());
        int height = Math.min(image.getHeight(), blurredImage.getHeight());
        
        BufferedImage combinedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = combinedImage.createGraphics();
        
        g2d.setColor(backgroundColor);
        
        // Fill the background with a white rectangle
        g2d.fillRect(0, 0, width, height);
        
        // Image to store the blended values of the original image and the blurred image
        BufferedImage blended = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color originalColor = new Color(image.getRGB(x, y), true);
                Color blurredColor = new Color(blurredImage.getRGB(x, y), true);
                
                int red = (originalColor.getRed() * blurredColor.getRed()) / 255;
                int green = (originalColor.getGreen() * blurredColor.getGreen()) / 255;
                int blue = (originalColor.getBlue() * blurredColor.getBlue()) / 255;
                int alpha = (originalColor.getAlpha() * blurredColor.getAlpha()) / 255;
                
                Color blendedColor = new Color(red, green, blue, alpha);
                blended.setRGB(x, y, blendedColor.getRGB());
            }
        }
        
        g2d.drawImage(blended, 0, 0, null);
        
        g2d.dispose();
        
        return combinedImage;
    }
}