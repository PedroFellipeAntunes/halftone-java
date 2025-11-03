package Halftone.Util;

import java.util.Random;

public class RngHelper {
    private static final long RNG_SEED = 123456789L; // Shared seed for reproducibility
    private static final Random RNG_INSTANCE = new Random(RNG_SEED);

    private RngHelper() {
        // Prevent instantiation
    }

    /**
     * Returns the shared Random instance using a fixed seed.
     * This RNG is global and stateful: each call to next() advances its sequence.
     *
     * @return Shared Random instance.
     */
    public static Random getRng() {
        return RNG_INSTANCE;
    }

    /**
     * Returns a new Random instance initialized with the same fixed seed.
     * This ensures identical reproducible sequences regardless of prior RNG usage.
     *
     * @return New Random instance with the fixed seed.
     */
    public static Random getNewRng() {
        return new Random(RNG_SEED);
    }
}