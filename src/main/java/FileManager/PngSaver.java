package FileManager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PngSaver {
    /**
     * Saves the given BufferedImage to disk, generating a new file name
     * based on the original image path and the provided prefix.
     *
     * @param fileName Prefix to append to the new file (e.g., "OperationName[params]").
     * @param originalImagePath The full path of the source image (including extension).
     * @param image The BufferedImage to save.
     */
    public void saveToFile(String fileName, String originalImagePath, BufferedImage image) {
        // Remove the extension from the original path (everything after the last dot)
        String imagePathWithoutExtension = originalImagePath.substring(0, originalImagePath.lastIndexOf('.'));
        
        // Generate a new file path that does not collide with existing files
        String newFilePath = generateNewFileName(fileName, imagePathWithoutExtension);
        
        // Write the image to the new file path
        saveImageToFile(image, newFilePath);
    }
    
    private String generateNewFileName(String fileName, String imagePathWithoutExtension) {
        String newFileName = imagePathWithoutExtension + "_" + fileName;
        String newFilePath = newFileName + ".png";
        
        File newFile = new File(newFilePath);
        int counter = 1;
        
        // If the file already exists, keep incrementing the counter
        // and appending it to the filename until a free name is found
        while (newFile.exists()) {
            newFilePath = imagePathWithoutExtension + "_" + fileName + "_" + counter + ".png";
            newFile = new File(newFilePath);
            
            counter++;
        }
        
        return newFilePath;
    }
    
    private void saveImageToFile(BufferedImage image, String filePath) {
        try {
            File output = new File(filePath);
            ImageIO.write(image, "png", output);
            
            System.out.println("Image saved to: " + output.toString());
        } catch (IOException e) {
            System.err.println("Error when saving image: " + e.getMessage());
        }
    }
}