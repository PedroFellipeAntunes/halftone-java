package Data;
import FileManager.Grayscale;
import java.awt.Color;

public class ColorAccumulator {
    public int sumA = 0, sumR = 0, sumG = 0, sumB = 0, count = 0;
    
    // Sobel gradient data (only computed for FlowLine type)
    public double sobelAngle = 0.0;
    public double magnitude = 0.0;
    
    /**
     * Adds the given color's RGBA components to this accumulator.
     *
     * @param c Color to add. Its red, green, and blue values will be summed,
     * and the sample count will be incremented.
     */
    public void add(Color c) {
        sumR += c.getRed();
        sumG += c.getGreen();
        sumB += c.getBlue();
        sumA += c.getAlpha();
        
        count++;
    }
    
    /**
     * Computes and returns the average color of all added samples.
     *
     * @return A Color whose RGBA components are the integer average of all added
     * samples. If no samples have been added (count == 0), returns a fully
     * transparent black (0,0,0,0). Otherwise, returns an opaque color (alpha =
     * 255).
     */
    public Color getAverage() {
        if (count == 0) {
            return new Color(0, 0, 0, 0);
        }
        
        int avgA = sumA / count;
        int avgR = sumR / count;
        int avgG = sumG / count;
        int avgB = sumB / count;
        
        return new Color(avgR, avgG, avgB, avgA);
    }
    /**
     * Computes the grayscale (luminance) value of the accumulated color average
     * by using the provided Grayscale.bt709 method (ITU-R BT.709 weights).
     *
     * @return A double in [0,255] representing the grayscale intensity of the
     * average color. If no samples have been added (count == 0), returns 0.
     */
    public double getGrayScale() {
        if (count == 0) {
            return 0;
        }
        
        Color avg = getAverage();
        
        // Prepare RGBA array for bt709: {alpha, red, green, blue}
        int[] rgba = new int[4];
        rgba[0] = avg.getAlpha();
        rgba[1] = avg.getRed();
        rgba[2] = avg.getGreen();
        rgba[3] = avg.getBlue();
        
        // Perform BT.709 grayscale conversion
        Grayscale gs = new Grayscale();
        int[] grayRGBA = gs.bt709(rgba);
        
        return grayRGBA[1]; // Gray RED
    }
}