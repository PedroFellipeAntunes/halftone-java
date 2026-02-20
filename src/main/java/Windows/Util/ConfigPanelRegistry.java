package Windows.Util;

import Data.ConfigPanelEntry;
import Data.ConfigData;
import Windows.Util.Panels.*;

import java.util.*;
import java.util.function.Function;

/**
 * Central registry for all configuration panels (global and TYPE-specific).
 * Uses LinkedHashMap to maintain insertion order.
 */
public class ConfigPanelRegistry {
    private static final Map<String, ConfigPanelEntry> panels = new LinkedHashMap<>();

    static {
        // ===== GLOBAL CONFIGS =====
        register("global:extra", "Extra", ExtraConfigPanel::new);

        // ===== TYPE-SPECIFIC CONFIGS =====
        register("type:Polygons", "Polygons", PolygonsConfigPanel::new);
        register("type:Stippling", "Stippling", StipplingConfigPanel::new);
        register("type:Lines", "Lines", LinesConfigPanel::new);
        register("type:Waves", "Waves", WaveConfigPanel::new);
        register("type:FlowLines", "Flow Lines", FlowLineConfigPanel::new);
    }

    /**
     * Register a configuration panel.
     *
     * @param id Unique identifier (e.g., "global:rng" or "type:Polygons")
     * @param displayName Human-readable name shown in UI
     * @param factory Factory function to create the panel
     */
    public static void register(String id, String displayName, Function<ConfigData, ConfigPanel> factory) {
        panels.put(id, new ConfigPanelEntry(id, displayName, factory));
    }

    /**
     * Get all registered configuration panels in insertion order.
     *
     * @return Collection of ConfigPanelEntry
     */
    public static Collection<ConfigPanelEntry> getAllPanels() {
        return panels.values();
    }

    /**
     * Get a specific panel entry by ID.
     *
     * @param id Panel identifier
     * @return ConfigPanelEntry or null if not found
     */
    public static ConfigPanelEntry getPanel(String id) {
        return panels.get(id);
    }

    /**
     * Check if a panel is registered.
     *
     * @param id Panel identifier
     * @return true if registered, false otherwise
     */
    public static boolean hasPanel(String id) {
        return panels.containsKey(id);
    }
}