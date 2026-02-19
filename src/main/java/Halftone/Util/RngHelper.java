package Halftone.Util;

import Data.ConfigData;
import java.util.Random;

/**
 * Helper class for Random Number Generation using ConfigData seed.
 */
public class RngHelper {
    private static ConfigData currentConfig = null;
    private static Random rngInstance = null;
    
    private RngHelper() {
        // Prevent instantiation
    }
    
    /**
     * Initialize RNG with the given configuration.
     * Must be called before using getRng() or getNewRng().
     * 
     * @param config Configuration containing RNG seed
     */
    public static void initialize(ConfigData config) {
        currentConfig = config;
        rngInstance = new Random(config.rngSeed);
    }
    
    /**
     * Returns the shared Random instance using the configured seed.
     * This RNG is global and stateful: each call to next() advances its sequence.
     * 
     * @return Shared Random instance
     * @throws IllegalStateException if initialize() was not called
     */
    public static Random getRng() {
        if (rngInstance == null) {
            throw new IllegalStateException("RngHelper not initialized. Call initialize(config) first.");
        }
        return rngInstance;
    }
    
    /**
     * Returns a new Random instance initialized with the configured seed.
     * This ensures identical reproducible sequences regardless of prior RNG usage.
     * 
     * @return New Random instance with the configured seed
     * @throws IllegalStateException if initialize() was not called
     */
    public static Random getNewRng() {
        if (currentConfig == null) {
            throw new IllegalStateException("RngHelper not initialized. Call initialize(config) first.");
        }
        return new Random(currentConfig.rngSeed);
    }
    
    /**
     * Reset the RNG instance to its initial state using the current seed.
     * Useful for restarting reproducible sequences.
     */
    public static void reset() {
        if (currentConfig != null) {
            rngInstance = new Random(currentConfig.rngSeed);
        }
    }
}