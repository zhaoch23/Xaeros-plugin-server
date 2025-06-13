package com.zhaoch23.xaerosminimapserver.waypoint.option;

import com.zhaoch23.xaerosminimapserver.XaerosMinimapServer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptionManager {
    private final XaerosMinimapServer plugin;
    private final Map<String, WaypointOption> options = new HashMap<>();

    public OptionManager(XaerosMinimapServer plugin) {
        this.plugin = plugin;
    }

    public WaypointOption getOption(String id) {
        return options.get(id);
    }

    public void loadOptions(FileConfiguration config) {
        for (String id : config.getKeys(false)) {
            try {
                ConfigurationSection optionData = config.getConfigurationSection(id);
                if (optionData == null) {
                    plugin.getLogger().severe("Option " + id + " not found");
                    continue;
                }
                String initials = optionData.getString("initials");
                String text = optionData.getString("text");
                String dispatchMode = optionData.getString("dispatch-mode");
                List<String> onSelect = optionData.getStringList("onSelect");
                options.put(id, new CommandOption(initials, text, dispatchMode, onSelect));
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load " + id + " due to " + e);
            }
        }
    }

    public void loadOptions() {
        options.clear();

        File file = new File(plugin.getDataFolder(), "options/");
        if (!file.exists()) {
            file.mkdirs();
            plugin.saveResource("options/test.yml", false);
        }

        File[] files = file.listFiles();
        if (files == null) {
            return;
        }

        for (File f : files) {
            if (f.isFile()) {
                if (f.getName().endsWith(".yml")) {
                    FileConfiguration config = YamlConfiguration.loadConfiguration(f);
                    loadOptions(config);
                }
            }
        }
    }
}
