package FileManager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PngSaver {
    public void saveToFile(String fileName, String originalImagePath, BufferedImage image) {
        String imagePathWithoutExtension = originalImagePath.substring(0, originalImagePath.lastIndexOf('.'));
        String newFilePath = generateNewFileName(fileName, imagePathWithoutExtension);
        
        saveImageToFile(image, newFilePath);
    }
    
    private String generateNewFileName(String fileName, String imagePathWithoutExtension) {
        String newFileName = imagePathWithoutExtension + "_" + fileName;
        String newFilePath = newFileName + ".png";
        
        File newFile = new File(newFilePath);
        int counter = 1;
        
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