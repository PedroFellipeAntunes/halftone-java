package FileManager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PngReader {
    /**
     * Reads an image from the given file path. If the file extension is JPG/JPEG,
     * performs a fast conversion to PNG format. Optionally converts the resulting
     * image to grayscale.
     *
     * @param fileLocation Path to the image file (PNG or JPG/JPEG).
     * @param grayscale If true, convert the loaded image to grayscale before returning.
     * @return BufferedImage of the loaded (and possibly converted) image, or null on error.
     */
    public BufferedImage readPNG(String fileLocation, boolean grayscale) {
        try {
            File file = new File(fileLocation);
            BufferedImage image = ImageIO.read(file);

            // Determine file extension (everything after the last dot)
            String formatName = fileLocation.substring(fileLocation.lastIndexOf('.') + 1);

            // If the input is JPEG, convert it to a true PNG‐style BufferedImage
            if (formatName.equalsIgnoreCase("jpg") || formatName.equalsIgnoreCase("jpeg")) {
                PngConverter converter = new PngConverter();
                
                image = converter.convertToPngFast(image);
            }

            if (grayscale) {
                return convertToGrayscale(image);
            }

            return image;
        } catch (IOException e) {
            System.err.println("Error when reading image: " + fileLocation);
        }

        // On error, return null
        return null;
    }

    private BufferedImage convertToGrayscale(BufferedImage image) {
        Grayscale gs = new Grayscale();

        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int currentPixel = image.getRGB(x, y);

                int[] rgba = new int[4];
                rgba[0] = (currentPixel >> 24) & 0xff; // Alpha
                rgba[1] = (currentPixel >> 16) & 0xff; // Red
                rgba[2] = (currentPixel >> 8) & 0xff;  // Green
                rgba[3] = currentPixel & 0xff;         // Blue

                int[] grayRGBA = gs.bt709(rgba);

                int newPixel = (grayRGBA[0] << 24)  // alpha
                             | (grayRGBA[1] << 16)  // red (gray)
                             | (grayRGBA[2] << 8)   // green (gray)
                             | grayRGBA[3];         // blue (gray)

                out.setRGB(x, y, newPixel);
            }
        }

        return out;
    }
}