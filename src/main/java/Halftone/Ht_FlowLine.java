package Halftone;

import Data.FlowLine.FlowLine;
import Data.ImageData;
import Halftone.Util.FlowLine.FlowLineGenerator;
import Halftone.Util.FlowLine.FlowLineRendererBezier;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Ht_FlowLine {
    private final FlowLineGenerator generator = new FlowLineGenerator();
    private final FlowLineRendererBezier renderer = new FlowLineRendererBezier();

    public Color backgroundColor = Color.WHITE;
    public Color foregroundColor = Color.BLACK;
    
    public int minLineSize = 2; // How small the line can be before before it's cut from being rendered

    /**
     * Applies a flow line halftone pattern over the input image by generating
     * and rendering Bézier ribbons that follow the local gradient direction.
     *
     * @param input The original image to overlay with flow lines.
     * @param kernelSize The size of each kernel cell in pixels.
     * @param data Object containing rotation, bounds, and color average data of the input image.
     * @param minStepSize Minimum step size for dynamic kernel traversal.
     * @param maxStepSize Maximum step size for dynamic kernel traversal.
     * @param followMaxChange If true, lines follow the gradient direction; otherwise perpendicular to it.
     * @return A new ARGB BufferedImage containing the flow line pattern.
     */
    public BufferedImage applyFlowLinePattern(BufferedImage input, int kernelSize, ImageData data, int minStepSize, int maxStepSize, boolean followMaxChange) {
        minStepSize = Math.max(1, minStepSize);
        maxStepSize = Math.max(maxStepSize, minStepSize);

        int width = input.getWidth();
        int height = input.getHeight();

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = output.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, height);

        // Configure and run generator
        generator.minStepSize = minStepSize;
        generator.maxStepSize = maxStepSize;
        generator.followMaxChange = followMaxChange;

        int numKernels = data.avgGrid.length;
        int numSegments = data.avgGrid[0].length;
        List<FlowLine> allLines = generator.generate(data.avgGrid, numKernels, numSegments);

        // Filter out nulls and lines below the minimum size
        List<FlowLine> validLines = new ArrayList<>();
        
        for (FlowLine line : allLines) {
            if (line != null && line.size() >= minLineSize) {
                validLines.add(line);
            }
        }

        // Configure and run renderer
        renderer.backgroundColor = backgroundColor;
        renderer.foregroundColor = foregroundColor;
        renderer.render(g2d, validLines, data, kernelSize);

        g2d.dispose();

        return output;
    }
}