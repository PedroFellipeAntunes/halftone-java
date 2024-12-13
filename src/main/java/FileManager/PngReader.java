package FileManager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PngReader {
    public BufferedImage readPNG(String fileLocation, boolean grayscale) {
        try {
            File file = new File(fileLocation);
            BufferedImage image = ImageIO.read(file);
            
            //Check file format
            String formatName = fileLocation.substring(fileLocation.lastIndexOf(".") + 1);
            
            if (formatName.equalsIgnoreCase("jpg") || formatName.equalsIgnoreCase("jpeg")) {
                PngConverter converter = new PngConverter();
                image = converter.convertToPng(image);
            }
            
            if (grayscale) {
                image = convertToGrayscale(image);
            }
            
            return image;
        } catch (IOException e) {
            System.err.println("Error when reading image: " + fileLocation);
        }
        
        return null;
    }
    
    private BufferedImage convertToGrayscale(BufferedImage image) {
        Grayscale gs = new Grayscale();
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int currentPixel = image.getRGB(x, y);
                
                int[] rgba = new int[4];
                rgba[0] = (currentPixel >> 24) & 0xff; // Alpha
                rgba[1] = (currentPixel >> 16) & 0xff; // Red
                rgba[2] = (currentPixel >> 8) & 0xff;  // Green
                rgba[3] = currentPixel & 0xff;         // Blue
                
                int[] grayRGBA = gs.bt709(rgba);
                
                int newPixel = (grayRGBA[0] << 24) | (grayRGBA[1] << 16) | (grayRGBA[2] << 8) | grayRGBA[3];
                
                image.setRGB(x, y, newPixel);
            }
        }
        
        return image;
    }
}