package com.github.tejaslamba2006.randomitemchallenge.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginAdvertiser {

    private static final String SMP_CORE_PLUGIN = "smp-core";
    private static final String DISCORD_LINK = "https://discord.gg/7fQPG4Grwt";

    public static void showEnableMessage(JavaPlugin plugin) {
        if (Bukkit.getPluginManager().getPlugin(SMP_CORE_PLUGIN) != null) {
            return;
        }

        String reset = "\u001B[0m";
        String green = "\u001B[32m";
        String yellow = "\u001B[33m";
        String cyan = "\u001B[36m";
        String bold = "\u001B[1m";

        plugin.getLogger().info("");
        plugin.getLogger().info(green + "╔══════════════════════════════════════════════════════════════╗" + reset);
        plugin.getLogger().info(green + "║" + reset + bold + yellow
                + "                    ⚔ SMP CORE ⚔                            " + reset + green + "║" + reset);
        plugin.getLogger().info(green + "║" + reset + "                                                              "
                + green + "║" + reset);
        plugin.getLogger().info(green + "║" + reset + "  " + green
                + "Enhance your server with our complete SMP plugin suite!" + reset + "    " + green + "║" + reset);
        plugin.getLogger().info(green + "║" + reset + "                                                              "
                + green + "║" + reset);
        plugin.getLogger().info(green + "║" + reset + "  Features: Claims, Economy, Warps, Homes, and more!         "
                + green + "║" + reset);
        plugin.getLogger().info(green + "║" + reset + "                                                              "
                + green + "║" + reset);
        plugin.getLogger().info(green + "║" + reset + "  " + yellow + "Join our Discord: " + cyan + DISCORD_LINK + reset
                + "           " + green + "║" + reset);
        plugin.getLogger().info(green + "╚══════════════════════════════════════════════════════════════╝" + reset);
        plugin.getLogger().info("");

        Bukkit.getPluginManager().registerEvents(new PluginAdvertiserListener(plugin), plugin);
    }

    private static class PluginAdvertiserListener implements Listener {

        private final JavaPlugin plugin;

        public PluginAdvertiserListener(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();

            if (!player.isOp())
                return;
            if (Bukkit.getPluginManager().getPlugin(SMP_CORE_PLUGIN) != null)
                return;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline())
                    return;

                player.sendMessage("");
                player.sendMessage("§8§m                                                        ");
                player.sendMessage("§6§l⚔ SMP CORE §8- §7Complete SMP Plugin Suite");
                player.sendMessage("");
                player.sendMessage("§7Enhance your server with claims, economy, warps & more!");
                player.sendMessage("§eJoin our Discord: §b" + DISCORD_LINK);
                player.sendMessage("§8§m                                                        ");
                player.sendMessage("");
            }, 60L);
        }
    }
}
