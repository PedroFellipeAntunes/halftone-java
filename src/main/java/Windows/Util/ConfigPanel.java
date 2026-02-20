package Windows.Util;

import Data.ConfigData;

import javax.swing.*;
import java.awt.*;

/**
 * Abstract base class for configuration panels.
 * Implements Scrollable so that when wrapped in a JScrollPane the panel
 * always tracks the viewport width, allowing BoxLayout children to size
 * themselves correctly and enabling vertical-only scrolling.
 */
public abstract class ConfigPanel extends JPanel implements Scrollable {
    protected ConfigData config;
    protected int availableWidth;

    /**
     * Creates a new ConfigPanel with the given configuration data.
     *
     * @param config Configuration data reference.
     */
    public ConfigPanel(ConfigData config) {
        this.config = config;
        this.availableWidth = 0; // Will be set by parent before initializeComponents
    }

    /**
     * Sets the available width for this panel.
     * Must be called by the parent before initializeComponents.
     *
     * @param width The available width in pixels.
     */
    public void setAvailableWidth(int width) {
        this.availableWidth = width;
    }

    /**
     * Initializes all UI components for this configuration panel.
     */
    public abstract void initializeComponents();

    /**
     * Applies the current panel values to the ConfigData object.
     */
    public abstract void applyConfig();

    /**
     * Returns the display name for this configuration panel.
     *
     * @return The display name shown in UI selectors.
     */
    public abstract String getDisplayName();

    // ===== Scrollable implementation =====

    /**
     * Returns the preferred size of the panel as the viewport hint.
     */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    /**
     * Scrolls one unit (one "line") at a time.
     */
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    /**
     * Scrolls one full visible block at a time.
     */
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return visibleRect.height;
    }

    /**
     * Always track the viewport width so horizontal layout is computed correctly.
     * This prevents BoxLayout children from collapsing when inside a JScrollPane.
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    /**
     * Do not track viewport height — allows vertical scrolling when content overflows.
     */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}