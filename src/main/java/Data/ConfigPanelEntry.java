package Data;

import Windows.Util.ConfigPanel;
import java.util.function.Function;

/**
 * Represents a configuration panel entry with metadata.
 * Holds the panel identifier, display name and factory function.
 */
public class ConfigPanelEntry {
    private final String id;
    private final String displayName;
    private final Function<ConfigData, ConfigPanel> factory;

    /**
     * Create a new configuration panel entry.
     *
     * @param id Unique panel identifier
     * @param displayName Human-readable name shown in UI
     * @param factory Factory function to create the panel
     */
    public ConfigPanelEntry(String id, String displayName, Function<ConfigData, ConfigPanel> factory) {
        this.id = id;
        this.displayName = displayName;
        this.factory = factory;
    }

    /**
     * Get the panel identifier.
     *
     * @return Panel id
     */
    public String getId() {
        return id;
    }

    /**
     * Get the display name used in UI.
     *
     * @return Display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Create the configuration panel using the provided ConfigData.
     *
     * @param config Configuration data passed to the panel
     * @return New ConfigPanel instance
     */
    public ConfigPanel createPanel(ConfigData config) {
        return factory.apply(config);
    }
}