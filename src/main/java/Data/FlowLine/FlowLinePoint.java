package Data.FlowLine;

/**
 * Simple 2D integer point representing a kernel position in the grid.
 * Used for tracking visited kernels in HashSet during flow line generation.
 */
public class FlowLinePoint {
    public final int row;
    public final int col;
    
    public FlowLinePoint(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        
        if (!(o instanceof FlowLinePoint)) return false;
        
        FlowLinePoint point = (FlowLinePoint) o;
        
        return row == point.row && col == point.col;
    }
    
    @Override
    public int hashCode() {
        return 31 * row + col;
    }
    
    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}