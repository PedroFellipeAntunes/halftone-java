package FileManager;

import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PngConverter {
    /**
     * Converts the given BufferedImage into a true PNG‐encoded BufferedImage
     * by writing it to an in-memory byte array in PNG format, then reading it back.
     * This ensures standard PNG color model, compression, and metadata are applied.
     *
     * @param inputImage The source image (possibly JPEG or other format).
     * @return A new BufferedImage read from the PNG‐encoded bytes,
     * or null if an error occurs.
     */
    public BufferedImage convertToPng(BufferedImage inputImage) {
        BufferedImage pngImage = null;
        try {
            // Convert to a byte array in png format
            ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
            ImageIO.write(inputImage, "PNG", byteArrayOut);
            byte[] bytes = byteArrayOut.toByteArray();
            
            InputStream byteArrayIn = new ByteArrayInputStream(bytes);
            
            // Read from byte array
            // This round‐trip enforces PNG encoding (e.g., enforces ARGB color model)
            pngImage = ImageIO.read(byteArrayIn);
        } catch (IOException e) {
            System.out.println("Error when converting image format: " + e.getMessage());
        }
        
        return pngImage;
    }
    
    /**
     * Performs a fast "conversion" to a PNG‐compatible image by drawing the
     * source into a newly created ARGB BufferedImage. This approach avoids the
     * overhead of encoding/decoding and simply ensures the result supports
     * transparency and the usual PNG color model.
     *
     * @param input The source BufferedImage (any type).
     * @return A new BufferedImage of type TYPE_INT_ARGB containing the same
     * pixels.
     */
    public BufferedImage convertToPngFast(BufferedImage input) {
        BufferedImage copy = new BufferedImage(
                input.getWidth(),
                input.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g = copy.createGraphics();
        g.drawImage(input, 0, 0, null);
        g.dispose();
        
        return copy;
    }
}