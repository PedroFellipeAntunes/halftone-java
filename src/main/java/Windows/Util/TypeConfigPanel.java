package Windows.Util;

import Data.ConfigData;
import javax.swing.*;

/**
 * Abstract base class for TYPE-specific configuration panels.
 */
public abstract class TypeConfigPanel extends JPanel {
    protected ConfigData config;
    protected int availableWidth;
    
    public TypeConfigPanel(ConfigData config) {
        this.config = config;
        this.availableWidth = 0; // Will be set by parent
    }
    
    /**
     * Set the available width for this panel.
     * Called by parent before initializeComponents.
     */
    public void setAvailableWidth(int width) {
        this.availableWidth = width;
    }
    
    /**
     * Initialize the UI components for this config panel.
     */
    public abstract void initializeComponents();
    
    /**
     * Apply current panel values to the ConfigData object.
     */
    public abstract void applyConfig();
    
    /**
     * Get the display name for this configuration panel.
     */
    public abstract String getDisplayName();
}