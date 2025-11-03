package Halftone.Util;

import java.util.Random;

public class RngHelper {
    private static final long RNG_SEED = 123456789L; // Shared seed
    private static final Random RNG_INSTANCE = new Random(RNG_SEED);

    private RngHelper() {
        // Prevent instantiation
    }

    public static Random getRng() {
        return RNG_INSTANCE;
    }
}