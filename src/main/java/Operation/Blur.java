package Operation;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Blur {
    /*
    Blur image only along the y axis
    */
    public BufferedImage applyVerticalBoxBlur(BufferedImage image, int verticalBlurSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        BufferedImage blurredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                blurredImage.setRGB(x, y, calculateBlurredAverage(image, x, y, verticalBlurSize));
            }
        }
        
        return blurredImage;
    }
    
    private int calculateBlurredAverage(BufferedImage image, int x, int y, int verticalBlurSize) {
        int redSum = 0, greenSum = 0, blueSum = 0, alphaSum = 0;
        int count = 0;
        
        for (int dy = -verticalBlurSize / 2; dy <= verticalBlurSize / 2; dy++) {
            int currentY = y + dy;
            
            if (currentY >= 0 && currentY < image.getHeight()) {
                Color color = new Color(image.getRGB(x, currentY), true);
                
                redSum += color.getRed();
                greenSum += color.getGreen();
                blueSum += color.getBlue();
                alphaSum += color.getAlpha();
                
                count++;
            }
        }
        
        int redAvg = redSum / count;
        int greenAvg = greenSum / count;
        int blueAvg = blueSum / count;
        int alphaAvg = alphaSum / count;
        
        return new Color(redAvg, greenAvg, blueAvg, alphaAvg).getRGB();
    }
}