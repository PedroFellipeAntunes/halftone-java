package Data;

public class HexPackingResult {
    public final double diameter;
    public final int rows;
    public final int cols;

    public HexPackingResult(double diameter, int rows, int cols) {
        this.diameter = diameter;
        this.rows = rows;
        this.cols = cols;
    }
}