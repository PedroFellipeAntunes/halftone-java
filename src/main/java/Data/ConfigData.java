package Data;

import java.awt.Color;

public class ConfigData {
    public int scale; // Pixel size of kernel
    public int angle;
    public TYPE type;
    public OpType opType;
    public Color colors[];
    public int polySides;

    /**
     * ConfigData object which contains all the possible configuration variables 
     * for the halftone process.
     * 
     * @param scale Size of each halftone kernel in pixels.
     * @param angle Rotation angle (degrees) to apply before halftoning.
     * @param type Halftone type (Dots, Lines, Squares, Triangles, Sine).
     * @param colors Two-color array: [0] = background color, [1] = foreground color.
     * @param opType Operation mode: Default, CMYK, RGB.
     */
    public ConfigData(int scale, int angle, TYPE type, OpType opType, Color[] colors) {
        this(scale, angle, type, opType, colors, 0);
    }

    /**
     * ConfigData object which contains all the possible configuration variables 
     * for the halftone process.
     * 
     * @param scale Size of each halftone kernel in pixels.
     * @param angle Rotation angle (degrees) to apply before halftoning.
     * @param type Halftone type (Dots, Lines, Squares, Triangles, Sine).
     * @param colors Two-color array: [0] = background color, [1] = foreground color.
     * @param opType Operation mode: Default, CMYK, RGB.
     * @param polySides Value for the ammount of sides in the polygon.
     */
    public ConfigData(int scale, int angle, TYPE type, OpType opType, Color[] colors, int polySides) {
        this.scale = scale;
        this.angle = angle;
        this.type = type;
        this.opType = opType;
        this.colors = colors;
        this.polySides = polySides;
    }
}