package FileManager;
import java.awt.Color;
public class Grayscale {
    /**
    * Expects interger array of size 4, with values between 0 and 255, 
    * with {alpha, red, green, blue}, 
    * returns grayscale based on max RGB value
    * 
    * @param RGBA
    * @return int RGBA[4]
    */
    public int[] maxValue(int RGBA[]) {
        int gray = Math.max(RGBA[1], Math.max(RGBA[2], RGBA[3]));
        
        RGBA[1] = gray;
        RGBA[2] = gray;
        RGBA[3] = gray;
        
        return RGBA;
    }
    
    /**
    * Expects packed ARGB integer value,
    * returns grayscale based on max RGB value
    * 
    * @param argb Packed ARGB integer
    * @return Packed ARGB integer with grayscale value
    */
    public int maxValue(int argb) {
        int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;
        
        int gray = Math.max(r, Math.max(g, b));
        
        return (a << 24) | (gray << 16) | (gray << 8) | gray;
    }
    
    /**
    * Expects interger array of size 4, with values between 0 and 255
    * with {alpha, red, green, blue}, 
    * returns grayscale based on min RGB value
    * 
    * @param RGBA
    * @return int RGBA[4]
    */
    public int[] minValue(int RGBA[]) {
        int gray = Math.min(RGBA[1], Math.min(RGBA[2], RGBA[3]));
        
        RGBA[1] = gray;
        RGBA[2] = gray;
        RGBA[3] = gray;
        
        return RGBA;
    }
    
    /**
    * Expects packed ARGB integer value,
    * returns grayscale based on min RGB value
    * 
    * @param argb Packed ARGB integer
    * @return Packed ARGB integer with grayscale value
    */
    public int minValue(int argb) {
        int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;
        
        int gray = Math.min(r, Math.min(g, b));
        
        return (a << 24) | (gray << 16) | (gray << 8) | gray;
    }
    
    /**
    * Expects interger array of size 4, with values between 0 and 255, 
    * with {alpha, red, green, blue}, 
    * returns grayscale based on ITU-R (BT.601) weights
    * red = 0.299, green = 0.587, blue = 0.114
    * 
    * @param RGBA
    * @return int RGBA[4]
    */
    public int[] bt601(int RGBA[]) {
        int gray = (int) (RGBA[1] * 0.299 + RGBA[2] * 0.587 + RGBA[3] * 0.114);
        
        RGBA[1] = gray;
        RGBA[2] = gray;
        RGBA[3] = gray;
        
        return RGBA;
    }
    
    /**
    * Expects packed ARGB integer value,
    * returns grayscale based on ITU-R (BT.601) weights
    * red = 0.299, green = 0.587, blue = 0.114
    * 
    * @param argb Packed ARGB integer
    * @return Packed ARGB integer with grayscale value
    */
    public int bt601(int argb) {
        int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;
        
        int gray = (int) (r * 0.299 + g * 0.587 + b * 0.114);
        
        return (a << 24) | (gray << 16) | (gray << 8) | gray;
    }
    
    /**
    * Expects interger array of size 4, with values between 0 and 255, 
    * with {alpha, red, green, blue}, 
    * returns grayscale based on ITU-R (BT.709) weights
    * red = 0.2126, green = 0.7152, blue = 0.0722
    * 
    * @param RGBA
    * @return int RGBA[4]
    */
    public int[] bt709(int RGBA[]) {
        int gray = (int) (RGBA[1] * 0.2126 + RGBA[2] * 0.7152 + RGBA[3] * 0.0722);
        
        RGBA[1] = gray;
        RGBA[2] = gray;
        RGBA[3] = gray;
        
        return RGBA;
    }
    
    /**
    * Expects packed ARGB integer value,
    * returns grayscale based on ITU-R (BT.709) weights
    * red = 0.2126, green = 0.7152, blue = 0.0722
    * 
    * @param argb Packed ARGB integer
    * @return Packed ARGB integer with grayscale value
    */
    public int bt709(int argb) {
        int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;
        
        int gray = (int) (r * 0.2126 + g * 0.7152 + b * 0.0722);
        
        return (a << 24) | (gray << 16) | (gray << 8) | gray;
    }
    
    /**
    * Expects interger array of size 4, with values between 0 and 255, 
    * with {alpha, red, green, blue}, 
    * returns grayscale based on basic weights for RGB
    * red = 0.3, green = 0.59, blue = 0.11
    * 
    * @param RGBA
    * @return int RGBA[4]
    */
    public int[] weightedAverage(int RGBA[]) {
        int gray = (int) (RGBA[1] * 0.3 + RGBA[2] * 0.59 + RGBA[3] * 0.11);
        
        RGBA[1] = gray;
        RGBA[2] = gray;
        RGBA[3] = gray;
        
        return RGBA;
    }
    
    /**
    * Expects packed ARGB integer value,
    * returns grayscale based on basic weights for RGB
    * red = 0.3, green = 0.59, blue = 0.11
    * 
    * @param argb Packed ARGB integer
    * @return Packed ARGB integer with grayscale value
    */
    public int weightedAverage(int argb) {
        int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;
        
        int gray = (int) (r * 0.3 + g * 0.59 + b * 0.11);
        
        return (a << 24) | (gray << 16) | (gray << 8) | gray;
    }
    
    /**
    * Expects interger array of size 4, with values between 0 and 255, 
    * with {alpha, red, green, blue}, 
    * returns grayscale based on basic average for RGB
    * 
    * @param RGBA
    * @return int RGBA[4]
    */
    public int[] average(int RGBA[]) {
        int average = (RGBA[1] + RGBA[2] + RGBA[3]) / 3;
        
        RGBA[1] = average;
        RGBA[2] = average;
        RGBA[3] = average;
        
        return RGBA;
    }
    
    /**
    * Expects packed ARGB integer value,
    * returns grayscale based on basic average for RGB
    * 
    * @param argb Packed ARGB integer
    * @return Packed ARGB integer with grayscale value
    */
    public int average(int argb) {
        int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;
        
        int gray = (r + g + b) / 3;
        
        return (a << 24) | (gray << 16) | (gray << 8) | gray;
    }
    
    /**
    * Expects interger array of size 4, with values between 0 and 255, 
    * with {alpha, red, green, blue}, 
    * returns grayscale by setting Saturation to 0 in HSB color space
    * 
    * @param RGBA
    * @return int RGBA[4]
    */
    public int[] hsbSaturation(int RGBA[]) {
        float[] hsb = Color.RGBtoHSB(RGBA[1], RGBA[2], RGBA[3], null);
        int grayPixel = Color.HSBtoRGB(hsb[0], 0, hsb[2]);
        
        RGBA[1] = (grayPixel >> 16) & 0xff; //Red
        RGBA[2] = (grayPixel >> 8) & 0xff; //Green
        RGBA[3] = (grayPixel) & 0xff; //Blue
        
        return RGBA;
    }
    
    /**
    * Expects packed ARGB integer value,
    * returns grayscale by setting Saturation to 0 in HSB color space
    * 
    * @param argb Packed ARGB integer
    * @return Packed ARGB integer with grayscale value
    */
    public int hsbSaturation(int argb) {
        int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;
        
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        int grayPixel = Color.HSBtoRGB(hsb[0], 0, hsb[2]);
        
        int grayR = (grayPixel >> 16) & 0xff;
        int grayG = (grayPixel >> 8) & 0xff;
        int grayB = grayPixel & 0xff;
        
        return (a << 24) | (grayR << 16) | (grayG << 8) | grayB;
    }
}