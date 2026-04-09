package ru.fantastworld.changerVault;

import org.bukkit.plugin.java.JavaPlugin;

public final class ChangerVault extends JavaPlugin {

    private static ChangerVault instance;
    private ConfigManager configManager;
    private boolean activated;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("tables.yml", false);

        configManager = new ConfigManager(this);
        configManager.loadTables();

        activated = getConfig().getBoolean("activated", true);

        getCommand("changervault").setExecutor(new ChangerCommand(this));
        getServer().getPluginManager().registerEvents(new VaultListener(this), this);

        getLogger().info("ChangerVault загружен");
        getLogger().info("Режим: " + (activated ? "ВКЛЮЧЕН" : "ВЫКЛЮЧЕН"));
    }

    @Override
    public void onDisable() {
        getConfig().set("activated", activated);
        saveConfig();
        getLogger().info("ChangerVault выключен");
    }

    public void toggleActivated() {
        activated = !activated;
    }

    public boolean isActivated() {
        return activated;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public static ChangerVault getInstance() {
        return instance;
    }
}