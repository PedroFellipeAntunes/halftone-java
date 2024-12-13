package FileManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PngConverter {
    public BufferedImage convertToPng(BufferedImage inputImage) {
        BufferedImage pngImage = null;
        try {
            //Convert to a byte array in png format
            ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
            ImageIO.write(inputImage, "PNG", byteArrayOut);
            byte[] bytes = byteArrayOut.toByteArray();
            
            InputStream byteArrayIn = new ByteArrayInputStream(bytes);
            
            //Read from byte array
            pngImage = ImageIO.read(byteArrayIn);
        } catch (IOException e) {
            System.out.println("Error when converting image format: " + e.getMessage());
        }
        
        return pngImage;
    }
}