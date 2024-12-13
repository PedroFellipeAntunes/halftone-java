package Operation;

import FileManager.PngReader;
import FileManager.PngSaver;
import Windows.ImageViewer;
import java.awt.image.BufferedImage;

public class Operations {
    static int kernelSize;
    static double angle;
    static TYPE type;
    
    public static void processFile(String filePath, int kernelSize, double angle, TYPE type) {
        Operations.kernelSize = kernelSize;
        Operations.angle = angle;
        Operations.type = type;
        
        // Get image from file
        PngReader read = new PngReader();
        
        BufferedImage image = read.readPNG(filePath, false);
        
        if (type.equals(TYPE.Dots)) {
            // TODO: LATER
            return;
        } else {
            // Rotate image by angle
            RotateImage ri = new RotateImage();
            BufferedImage rotatedImage = ri.rotateImage(image, angle, kernelSize);
            //ImageViewer test_1 = new ImageViewer(rotatedImage, filePath);
            
            // Generate kernels
            HalftoneLines hl = new HalftoneLines();
            int[][][] Offsets = hl.generateKernels(rotatedImage, kernelSize, false, 0);
            
            // Draw lines to a new image
            BufferedImage lineImage = hl.drawPolygonsInKernels(rotatedImage.getWidth(),
                    rotatedImage.getHeight(),
                    Offsets,
                    kernelSize);
            //ImageViewer test_2 = new ImageViewer(lineImage, filePath);
            
            // Blur lines to new image
            Blur br = new Blur();
            BufferedImage blurredImage = br.applyVerticalBoxBlur(lineImage, kernelSize / 2);
            //ImageViewer test_3 = new ImageViewer(blurredImage, filePath);
            
            // Blend both images to a new image
            Blend bd = new Blend();
            rotatedImage = bd.blendImagesWithMultiply(lineImage, blurredImage);
            //ImageViewer test_4 = new ImageViewer(rotatedImage, filePath);
            
            // Rotate back to original angle and restore size
            rotatedImage = ri.rotateImageSameSize(rotatedImage, -angle);
            image = ri.restoreOriginalSize(rotatedImage, image.getWidth(), image.getHeight());
        }
        
        // Show image to user
        ImageViewer viewer = new ImageViewer(image, filePath);
    }
    
    //Save files
    public static void saveImage(BufferedImage image, String filePath) {
        PngSaver listToImage = new PngSaver();
        
        if (type.equals(TYPE.Dots)) {
            listToImage.saveToFile("Halftone["+type+","+kernelSize+"]", filePath, image);
        } else {
            listToImage.saveToFile("Halftone["+type+","+kernelSize+","+angle+"]", filePath, image);
        }
    }
}