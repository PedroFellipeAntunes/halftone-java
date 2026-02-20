package Data.FlowLine;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class FlowLine {
    public List<Point2D.Double> positions = new ArrayList<>();
    public double averageAngle = 0.0;
    
    public Point2D.Double getStart() {
        return positions.isEmpty() ? null : positions.get(0);
    }
    
    public Point2D.Double getEnd() {
        return positions.isEmpty() ? null : positions.get(positions.size() - 1);
    }
    
    public int size() {
        return positions.size();
    }
    
    public FlowLinePoint getStartKernel() {
        if (positions.isEmpty()) return null;
        Point2D.Double start = positions.get(0);
        return new FlowLinePoint((int) Math.floor(start.y), (int) Math.floor(start.x)); // row, col
    }
    
    public FlowLinePoint getEndKernel() {
        if (positions.isEmpty()) return null;
        Point2D.Double end = positions.get(positions.size() - 1);
        return new FlowLinePoint((int) Math.floor(end.y), (int) Math.floor(end.x)); // row, col
    }
    
    public void merge(FlowLine other) {
        if (other == null || other.positions.isEmpty()) {
            System.out.println("MERGE CALLED WITH NULL/EMPTY!");
            return;
        }
        
        int sizeBefore = this.size();
        int otherSize = other.size();
        
        this.positions.addAll(other.positions);
        
        int sizeAfter = this.size();
        
        if (sizeAfter != sizeBefore + otherSize) {
            System.out.println("MERGE FAILED! Before=" + sizeBefore + " Other=" + otherSize + " After=" + sizeAfter + " Expected=" + (sizeBefore + otherSize));
        }
        
        // Recompute average angle as weighted average
        double totalAngle = this.averageAngle * sizeBefore + other.averageAngle * otherSize;
        this.averageAngle = totalAngle / this.size();
    }
    
    public void reverse() {
        List<Point2D.Double> reversed = new ArrayList<>();
        for (int i = positions.size() - 1; i >= 0; i--) {
            reversed.add(positions.get(i));
        }
        positions = reversed;
    }
}