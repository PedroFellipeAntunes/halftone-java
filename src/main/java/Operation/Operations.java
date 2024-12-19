package Operation;

import FileManager.PngReader;
import FileManager.PngSaver;
import Windows.ImageViewer;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class Operations {
    static int kernelSize;
    static double angle;
    static TYPE type;
    
    static int blurSize = 2; // Default smaller value
    static double frequencyFactor = 15.0;
    static int amplitude = 15 / 2;
    
    public static void processFile(String filePath, int kernelSize, double angle, TYPE type, Color[] colors) {
        Operations.kernelSize = kernelSize;
        Operations.angle = angle;
        Operations.type = type;
        
        // Get image from file
        PngReader read = new PngReader();
        
        BufferedImage image = read.readPNG(filePath, false);
        
        // Rotate image by angle
        RotateImage ri = new RotateImage();
        BufferedImage rotatedImage = ri.rotateImage(image, angle, kernelSize);
        //ImageViewer test_1 = new ImageViewer(rotatedImage, filePath);
        
        BufferedImage blurImage;
        
        switch (type) {
            case Dots:
                {
                    // Generate kernels
                    HalftoneDots hd = new HalftoneDots();
                    int[][] radii = hd.generateKernelRadii(rotatedImage, kernelSize);
                    
                    // Draw
                    rotatedImage = hd.drawHalftoneDots(colors[1], rotatedImage.getWidth(),
                            rotatedImage.getHeight(),
                            kernelSize,
                            radii);
                    //ImageViewer test_2 = new ImageViewer(rotatedImage, filePath);
                    
                    // Blur lines to new image
                    Blur br = new Blur();
                    blurImage = br.applyBidirectionalBoxBlur(rotatedImage, blurSize);
                    //ImageViewer test_3 = new ImageViewer(blurImage, filePath);
                    break;
                }
            case Lines:
                {
                    // Generate kernels
                    HalftoneLines hl = new HalftoneLines();
                    int[][][] offsets = hl.generateKernels(rotatedImage, kernelSize, false, 0);
                    
                    // Draw lines to a new image
                    rotatedImage = hl.drawPolygonsInKernels(colors[1], rotatedImage.getWidth(),
                            rotatedImage.getHeight(),
                            offsets,
                            kernelSize);
                    //ImageViewer test_2 = new ImageViewer(rotatedImage, filePath);
                    
                    // Blur lines to new image
                    Blur br = new Blur();
                    blurImage = br.applyVerticalBoxBlur(rotatedImage, blurSize);
                    //ImageViewer test_3 = new ImageViewer(blurImage, filePath);
                    break;
                }
            default:
                {
                    // Generate kernels
                    HalftoneLines hl = new HalftoneLines();
                    int[][][] offsets = hl.generateKernels(rotatedImage, kernelSize, false, 0);
                    
                    // Draw sines to a new image
                    rotatedImage = hl.drawPolygonsInKernelsSine(colors[1], rotatedImage.getWidth(),
                            rotatedImage.getHeight(),
                            offsets,
                            kernelSize,
                            frequencyFactor,
                            amplitude);
                    //ImageViewer test_2 = new ImageViewer(rotatedImage, filePath);
                    
                    // Blur sines to new image
                    Blur br = new Blur();
                    blurImage = br.applyVerticalBoxBlur(rotatedImage, blurSize);
                    //ImageViewer test_3 = new ImageViewer(blurImage, filePath);
                    break;
                }
        }
        
        // Blend both images to a new image
        Blend bd = new Blend();
        rotatedImage = bd.blendImagesWithMultiply(colors[0], rotatedImage, blurImage);
        //ImageViewer test_4 = new ImageViewer(rotatedImage, filePath);
        
        // Rotate back to original angle
        rotatedImage = ri.rotateImageSameSize(rotatedImage, -angle);
        //ImageViewer test_5 = new ImageViewer(rotatedImage, filePath);
        
        // Restore size
        image = ri.restoreOriginalSize(rotatedImage, image.getWidth(), image.getHeight());
        
        // Show image to user
        ImageViewer viewer = new ImageViewer(image, filePath);
    }
    
    //Save files
    public static void saveImage(BufferedImage image, String filePath) {
        PngSaver listToImage = new PngSaver();
        
        listToImage.saveToFile("Halftone["+type+","+kernelSize+","+angle+"]", filePath, image);
    }
}