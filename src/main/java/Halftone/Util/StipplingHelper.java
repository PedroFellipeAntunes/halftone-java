package Halftone.Util;

import Data.HexPackingResult;

public class StipplingHelper {
    private int capacityHex(int rows, int cols) {
        int full = (rows + 1) / 2;
        int shortR = rows / 2;
        
        return full * cols + shortR * Math.max(0, cols - 1);
    }

    /**
     * Finds the best rows x columns combination to maximize the circle
     * diameter in a hexagonal grid.
     *
     * @param L size of the kernel (px)
     * @param N number of points to pack
     * @return HexPackingResult containing the optimal diameter, rows, and columns
     */
    public HexPackingResult diameterHexBest(double L, int N) {
        if (N <= 0) {
            return new HexPackingResult(0.0, 0, 0);
        }
        
        double best = 0.0;
        int bestRows = 1, bestCols = 1;

        for (int rows = 1; rows <= N; rows++) {
            int cols = 1;
            
            while (capacityHex(rows, cols) < N) {
                cols++;
            }
            
            double dHoriz = L / (double) cols;
            double dVert = L / (1.0 + (rows - 1) * Math.sqrt(3.0) / 2.0);
            double d = Math.min(dHoriz, dVert);
            
            if (d > best) {
                best = d;
                bestRows = rows;
                bestCols = cols;
            }
        }

        return new HexPackingResult(best, bestRows, bestCols);
    }

    private double diameterAreaApprox(double L, int N) {
        if (N <= 0) {
            return 0.0;
        }
        
        double rho = Math.PI / (2.0 * Math.sqrt(3.0));
        
        return Math.sqrt((rho * 4.0 * L * L) / (Math.PI * N));
    }

    /**
     * Returns a "good" diameter for packing N points into a kernel of
     * size kernelSize. Uses hex-best as the default and limits it by
     * the area approximation (conservative).
     *
     * @param kernelSize size of the kernel (px)
     * @param maxPoints number of points to pack
     * @return chosen circle diameter
     */
    public double diameterForMaxPoints(double kernelSize, int maxPoints) {
        if (maxPoints <= 0) {
            return 0.0;
        }

        double dHex = diameterHexBest(kernelSize, maxPoints).diameter;
        double dArea = diameterAreaApprox(kernelSize, maxPoints);

        double chosen = Math.min(dHex, dArea);
        chosen = Math.min(chosen, kernelSize);

        if (chosen < 0.25) {
            chosen = 0.25;
        }

        return chosen;
    }
}