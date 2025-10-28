package Halftone.Util;

/**
 * Provides a lookup table (LUT) of precomputed radius scaling factors
 * used in stippling and halftoning processes. Each LUT entry corresponds
 * to a density value from 1 to 100, determining how much of the kernel’s
 * diagonal is used to compute the stipple radius.
 *
 * The LUT values were precomputed and taken from http://hydra.nat.uni-magdeburg.de/packing/cci/
 * to ensure smooth visual transitions between densities, avoiding costly runtime calculations.
 */
public class StipplingHelperLUTStatic {
    // Lookup table mapping density (1–100) to normalized radius fractions
    private static final double[] LUT = {
        1.000000000000,
        0.500000000000,
        0.464101615138,
        0.414213562373,
        0.370191908159,
        0.333333333333,
        0.333333333333,
        0.302593388349,
        0.276768653914,
        0.262258924190,
        0.254854701717,
        0.248163470572,
        0.236067977500,
        0.231030727971,
        0.221172539086,
        0.216664742924,
        0.208679665570,
        0.205604646760,
        0.205604646760,
        0.195224011019,
        0.190392146849,
        0.183833026582,
        0.180336009254,
        0.176939130596,
        0.173827661421,
        0.171580252187,
        0.169307931135,
        0.166252750039,
        0.162903649277,
        0.161349109065,
        0.158944541560,
        0.155533985423,
        0.154161517947,
        0.151264028247,
        0.149316776635,
        0.148219429761,
        0.147955904479,
        0.143639218073,
        0.141685521745,
        0.140373604203,
        0.137740812925,
        0.136113748716,
        0.134771891080,
        0.133368245886,
        0.132049594252,
        0.130715880038,
        0.129463747327,
        0.128348756543,
        0.126792996262,
        0.125825489530,
        0.124571676602,
        0.123690164592,
        0.122255623688,
        0.121892021857,
        0.121786324528,
        0.119281497082,
        0.118382637652,
        0.117308193128,
        0.116380564996,
        0.115657480133,
        0.115456141678,
        0.113253291983,
        0.112456192918,
        0.111582595826,
        0.110896743723,
        0.109935057298,
        0.109063482023,
        0.108345017704,
        0.107877643365,
        0.107001616606,
        0.106204499837,
        0.105553253159,
        0.104817999688,
        0.104283629835,
        0.103390915666,
        0.102779181947,
        0.102052146984,
        0.101443439719,
        0.100958464654,
        0.100319499416,
        0.099891475492,
        0.099494327805,
        0.098844919277,
        0.098526721390,
        0.098395063693,
        0.097099624005,
        0.096495211836,
        0.095855792772,
        0.095233634544,
        0.094822059587,
        0.094636278506,
        0.093592245755,
        0.093167534622,
        0.092781315284,
        0.092249177761,
        0.091884716483,
        0.091419459906,
        0.091079798229,
        0.090636019813,
        0.090235200288
    };
    
    /**
     * Computes the stippling radius for a given kernel size and density level.
     * The radius is based on a fraction of the kernel’s half-diagonal,
     * retrieved from the precomputed LUT. For density = 0, a minimal radius of
     * 0.5 is returned to avoid degenerate values.
     * 
     * The computation steps are:
     * Validate kernel size (> 1).
     * Clamp density between 1 and 100.
     * 
     * Otherwise:
     * fraction = LUT[density - 1]
     * diagonal = sqrt(2) * kernelSize
     * halfDiagonal = diagonal / 2
     * radius = fraction * halfDiagonal
     * 
     * Return max(radius, 0.5) to ensure a minimum visible radius.
     *
     * @param kernelSize The size (in pixels) of the kernel area.
     * @param density The stippling density, in the range [0, 100].
     * @return The computed radius for the given density and kernel size.
     */
    public double getRadius(int kernelSize, int density) {
        if (kernelSize <= 0) throw new IllegalArgumentException("kernelSize must be > 0");
        
        if (density < 1 || density > 100) throw new IllegalArgumentException("density must be 1..100; If you need a bigger value, a new LUT will bave to be generated");
        
        // Lookup normalized fraction from LUT (1 → full half-diagonal)
        double fraction = LUT[density - 1];
        
        // Compute half diagonal of the kernel
        double diagonal = Math.sqrt(2.0) * kernelSize;
        double halfDiagonal = diagonal / 2.0;
        
        // Final radius based on fraction of half-diagonal
        double radius = fraction * halfDiagonal;
        
        return Math.max(radius, 0.5);
    }
}