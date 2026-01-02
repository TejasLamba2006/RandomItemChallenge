package com.github.tejaslamba2006.randomitemchallenge;

import com.github.tejaslamba2006.randomitemchallenge.commands.RandomItemChallengeCommand;
import com.github.tejaslamba2006.randomitemchallenge.managers.ItemManager;
import com.github.tejaslamba2006.randomitemchallenge.utils.PluginAdvertiser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;

public class RandomItemChallenge extends JavaPlugin {

    private static RandomItemChallenge instance;
    private ItemManager itemManager;
    private MiniMessage miniMessage;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialize MiniMessage
        miniMessage = MiniMessage.miniMessage();

        // Initialize managers
        itemManager = new ItemManager(this);

        // Register commands
        RandomItemChallengeCommand mainCommand = new RandomItemChallengeCommand(this);
        getCommand("randomitemchallenge").setExecutor(mainCommand);
        getCommand("randomitemchallenge").setTabCompleter(mainCommand);

        // Show advertisement
        if (getConfig().getBoolean("show-advertisement", true)) {
            PluginAdvertiser.showEnableMessage(this);
        }

        // Auto-start if enabled
        if (getConfig().getBoolean("enabled", false)) {
            itemManager.start();
        }

        getLogger().info("RandomItemChallenge loaded! Use /ric start to begin.");
    }

    @Override
    public void onDisable() {
        // Stop the challenge
        if (itemManager != null && itemManager.isRunning()) {
            itemManager.stop(false); // Don't broadcast on shutdown
        }

        getLogger().info("RandomItemChallenge disabled!");
    }

    /**
     * Get plugin instance
     */
    public static RandomItemChallenge getInstance() {
        return instance;
    }

    /**
     * Get item manager
     */
    public ItemManager getItemManager() {
        return itemManager;
    }

    /**
     * Reload configuration
     */
    public void reload() {
        reloadConfig();
        itemManager.reload();
    }

    /**
     * Colorize text
     */
    public Component colorize(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        String legacyText = text.replace('&', '§');

        if (text.contains("<") && text.contains(">")) {
            return miniMessage.deserialize(text);
        }

        return LegacyComponentSerializer.legacySection().deserialize(legacyText);
    }

    /**
     * Get message from config with prefix
     */
    public Component getMessage(String key, String... replacements) {
        String prefix = getConfig().getString("messages.prefix", "&8[&a&lRIC&8] &7");
        String message = getConfig().getString("messages." + key, key);

        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }

        return colorize(prefix + message);
    }
}
