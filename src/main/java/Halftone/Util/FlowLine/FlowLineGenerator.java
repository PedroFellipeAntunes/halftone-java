package Halftone.Util.FlowLine;

import Data.ColorAccumulator;
import Data.FlowLine.FlowLine;
import Data.FlowLine.FlowLinePoint;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlowLineGenerator {
    private final int numIterations = 10;
    public boolean followMaxChange = true;

    public double minStepSize = 1.0;
    public double maxStepSize = 5.0;

    /**
     * Generates a list of flow lines by iteratively merging kernel-level line
     * segments based on local gradient direction and magnitude.
     *
     * @param avgGrid The grid of color accumulators, one per kernel cell.
     * @param numKernels Number of kernel rows in the grid.
     * @param numSegments Number of kernel columns in the grid.
     * @return A list of merged flow lines covering the image.
     */
    public List<FlowLine> generate(ColorAccumulator[][] avgGrid, int numKernels, int numSegments) {
        // Each valid kernel starts as its own single-point flow line
        FlowLine[][] gridLines = new FlowLine[numKernels][numSegments];
        Set<FlowLine> activeLines = new HashSet<>();

        for (int kr = 0; kr < numKernels; kr++) {
            for (int kc = 0; kc < numSegments; kc++) {
                ColorAccumulator cell = avgGrid[kr][kc];

                if (cell.count > 0) {
                    FlowLine line = new FlowLine();
                    line.positions.add(new Point2D.Double(kc + 0.5, kr + 0.5));
                    line.averageAngle = getTargetAngle(cell);

                    gridLines[kr][kc] = line;
                    activeLines.add(line);
                }
            }
        }

        // Iteratively merge lines from their endpoints across the full grid
        for (int iter = 0; iter < numIterations; iter++) {
            int mergesThisIter = 0;

            for (int kr = 0; kr < numKernels; kr++) {
                for (int kc = 0; kc < numSegments; kc++) {
                    FlowLine currentLine = gridLines[kr][kc];

                    if (currentLine == null) continue;

                    FlowLinePoint currentKernel = new FlowLinePoint(kr, kc);
                    boolean isStart = currentKernel.equals(currentLine.getStartKernel());
                    boolean isEnd = currentKernel.equals(currentLine.getEndKernel());

                    if (isStart || isEnd) {
                        mergesThisIter += tryMergeFromGridPoint(currentLine, currentKernel, isEnd, avgGrid, gridLines, activeLines, numKernels, numSegments);
                    }
                }
            }

            if (mergesThisIter == 0) break;
        }

        return new ArrayList<>(activeLines);
    }

    /**
     * Attempts to merge the given line with a neighboring line reachable from
     * the specified endpoint kernel, if their directions are compatible.
     */
    private int tryMergeFromGridPoint(FlowLine line, FlowLinePoint point, boolean isForward, ColorAccumulator[][] avgGrid,
                                      FlowLine[][] gridLines, Set<FlowLine> activeLines,
                                      int numKernels, int numSegments) {
        double angle = getTargetAngle(avgGrid[point.row][point.col]);
        
        if (!isForward) angle += Math.PI;

        FlowLinePoint next = computeNextKernel(point, angle, numKernels, numSegments, avgGrid);
        
        if (next == null) return 0;

        FlowLine other = gridLines[next.row][next.col];
        
        if (other == null || other == line) return 0;

        // Skip merge if the angle difference between lines is too large
        double nextAngle = getTargetAngle(avgGrid[next.row][next.col]);
        double diff = Math.abs(angle - nextAngle);
        diff = Math.min(diff, 2 * Math.PI - diff);
        
        if (diff > Math.PI / 3) return 0;

        FlowLinePoint otherStart = other.getStartKernel();
        FlowLinePoint otherEnd = other.getEndKernel();

        if (isForward) {
            if (next.equals(otherStart)) {
                line.merge(other);
                updateGridReferences(other, line, gridLines);
                activeLines.remove(other);
                
                return 1;
            } else if (next.equals(otherEnd)) {
                other.reverse();
                line.merge(other);
                updateGridReferences(other, line, gridLines);
                activeLines.remove(other);
                
                return 1;
            }
        } else {
            if (next.equals(otherEnd)) {
                other.merge(line);
                updateGridReferences(line, other, gridLines);
                activeLines.remove(line);
                
                return 1;
            } else if (next.equals(otherStart)) {
                other.reverse();
                other.merge(line);
                updateGridReferences(line, other, gridLines);
                activeLines.remove(line);
                
                return 1;
            }
        }

        return 0;
    }

    /**
     * Reassigns all kernel cells that belonged to oldLine to point to newLine.
     */
    private void updateGridReferences(FlowLine oldLine, FlowLine newLine, FlowLine[][] gridLines) {
        for (Point2D.Double pos : oldLine.positions) {
            int r = (int) Math.floor(pos.y);
            int c = (int) Math.floor(pos.x);
            
            if (r >= 0 && r < gridLines.length && c >= 0 && c < gridLines[0].length) {
                gridLines[r][c] = newLine;
            }
        }
    }

    /**
     * Computes the next kernel position by stepping from the given point along
     * the provided angle, using either dynamic or fixed step size.
     */
    private FlowLinePoint computeNextKernel(FlowLinePoint from, double angle, int numKernels, int numSegments, ColorAccumulator[][] avgGrid) {
        ColorAccumulator cell = avgGrid[from.row][from.col];

        double grayNorm = cell.getGrayScale() / 255.0;
        double darkness = 1.0 - grayNorm;
        double mag = Math.max(cell.magnitude, darkness);

        // Darker areas get larger steps (fewer, bolder lines)
        // Lighter areas get smaller steps (more lines, finer detail)
        double currentStep = minStepSize + (mag * (maxStepSize - minStepSize));
        currentStep = Math.max(minStepSize, Math.min(maxStepSize, currentStep));

        double deltaX = Math.cos(angle) * currentStep;
        double deltaY = Math.sin(angle) * currentStep;

        int nextCol = (int) Math.round(from.col + deltaX);
        int nextRow = (int) Math.round(from.row + deltaY);

        if (nextRow < 0 || nextRow >= numKernels || nextCol < 0 || nextCol >= numSegments) {
            return null;
        }

        // If the step didn't move us, force a single-cell step
        if (nextRow == from.row && nextCol == from.col) {
            nextCol = from.col + (int) Math.round(Math.cos(angle));
            nextRow = from.row + (int) Math.round(Math.sin(angle));

            if (nextRow < 0 || nextRow >= numKernels || nextCol < 0 || nextCol >= numSegments) {
                return null;
            }
            
            if (nextRow == from.row && nextCol == from.col) {
                return null;
            }
        }

        return new FlowLinePoint(nextRow, nextCol);
    }

    /**
     * Returns the target flow direction angle for a kernel cell, either following
     * the maximum gradient or perpendicular to it.
     */
    private double getTargetAngle(ColorAccumulator cell) {
        double angle = cell.sobelAngle;
        
        if (!followMaxChange) {
            angle += Math.PI / 2.0;
        }
        
        return angle;
    }
}