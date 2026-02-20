package Data;

import java.awt.Color;

/**
 * Configuration data with default values.
 * All fields have sensible defaults that can be modified by UI components.
 */
public class ConfigData {
    // ===== PRIMARY CONFIGS (always used) =====
    public int scale = 15;
    public int angle = 45;
    public TYPE type = TYPE.Dots;
    public OpType opType = OpType.Grayscale;
    public Color[] colors = new Color[]{Color.WHITE, Color.BLACK};
    
    // ===== GLOBAL CONFIGS =====
    public long rngSeed = 123456789L;
    public boolean debugState = false;
    
    // ===== TYPE-SPECIFIC CONFIGS =====
    // Polygons
    public int polySides = 4;
    
    // Stippling (optimal value found through data analysis)
    public int stipplingDensity = 85;
    
    // Lines
    public boolean invertRowSelection = false;
    public double rowProbability = 1.0; // % Chance of drawing a row
    
    // Waves
    public double amplitudeScalar = 2.0;
    public double frequencyScalar = 6.0;
    
    // FlowLines
    public int minStep = 1;
    public int maxStep = 5;
    public boolean followMaxChange = false;
    public int flowLineSmoothRadius = 2;
    public int minLineSize = 2;
    public int blurRadius = 1;
}