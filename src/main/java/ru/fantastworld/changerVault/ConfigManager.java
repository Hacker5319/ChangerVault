package ru.fantastworld.changerVault;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final ChangerVault plugin;
    private final Map<String, Map<Material, Double>> overridesByType = new HashMap<>();

    public ConfigManager(ChangerVault plugin) {
        this.plugin = plugin;
    }

    public void loadTables() {
        overridesByType.clear();
        File tablesFile = new File(plugin.getDataFolder(), "tables.yml");
        FileConfiguration tablesConfig = YamlConfiguration.loadConfiguration(tablesFile);

        ConfigurationSection section = tablesConfig.getConfigurationSection("");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            String typeStr = tablesConfig.getString(key + ".type");
            String materialStr = tablesConfig.getString(key + ".material");
            double chance = tablesConfig.getDouble(key + ".chance");

            plugin.getLogger().info("Ключ: " + key);
            plugin.getLogger().info("  type: " + typeStr);
            plugin.getLogger().info("  material: " + materialStr);
            plugin.getLogger().info("  chance: " + chance);

            if (typeStr == null || materialStr == null) {
                plugin.getLogger().warning("  ОШИБКА: type или material = null");
                continue;
            }

            try {
                Material material = Material.valueOf(materialStr.toUpperCase());
                overridesByType.computeIfAbsent(typeStr.toUpperCase(), k -> new HashMap<>())
                        .put(material, chance / 100.0);
                plugin.getLogger().info("  Загружено: " + typeStr.toUpperCase() + " -> " + material + " (" + chance + "%)");
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("  ОШИБКА: " + e.getMessage());
            }
        }

        // TODO: Добавить возможность удалить указанный предмет
    }

    public Map<Material, Double> getOverridesForType(String type) {
        return overridesByType.getOrDefault(type.toUpperCase(), new HashMap<>());
    }
}