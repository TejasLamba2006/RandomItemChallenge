package com.github.tejaslamba2006.randomitemchallenge.commands;

import com.github.tejaslamba2006.randomitemchallenge.RandomItemChallenge;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main command handler for RandomItemChallenge
 */
public class RandomItemChallengeCommand implements CommandExecutor, TabCompleter {
    
    private final RandomItemChallenge plugin;
    
    public RandomItemChallengeCommand(RandomItemChallenge plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("randomitemchallenge.admin")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "start", "enable", "on" -> handleStart(sender);
            case "stop", "disable", "off" -> handleStop(sender);
            case "toggle" -> handleToggle(sender);
            case "skip", "next" -> handleSkip(sender);
            case "interval" -> handleInterval(sender, args);
            case "status" -> handleStatus(sender);
            case "reload" -> handleReload(sender);
            case "help" -> sendHelp(sender);
            default -> sendHelp(sender);
        }
        
        return true;
    }
    
    /**
     * Start the challenge
     */
    private void handleStart(CommandSender sender) {
        if (plugin.getItemManager().isRunning()) {
            sender.sendMessage(plugin.getMessage("already-running"));
            return;
        }
        
        plugin.getItemManager().start();
    }
    
    /**
     * Stop the challenge
     */
    private void handleStop(CommandSender sender) {
        if (!plugin.getItemManager().isRunning()) {
            sender.sendMessage(plugin.getMessage("not-running"));
            return;
        }
        
        plugin.getItemManager().stop();
    }
    
    /**
     * Toggle the challenge
     */
    private void handleToggle(CommandSender sender) {
        if (plugin.getItemManager().isRunning()) {
            plugin.getItemManager().stop();
        } else {
            plugin.getItemManager().start();
        }
    }
    
    /**
     * Skip to next item
     */
    private void handleSkip(CommandSender sender) {
        if (!plugin.getItemManager().isRunning()) {
            sender.sendMessage(plugin.getMessage("not-running"));
            return;
        }
        
        plugin.getItemManager().skip();
        sender.sendMessage(plugin.getMessage("skipped"));
    }
    
    /**
     * Set or view interval
     */
    private void handleInterval(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessage("interval-current", 
                "{interval}", String.valueOf(plugin.getItemManager().getInterval())));
            return;
        }
        
        try {
            int interval = Integer.parseInt(args[1]);
            if (interval < 1) {
                sender.sendMessage(plugin.getMessage("invalid-number"));
                return;
            }
            
            plugin.getItemManager().setInterval(interval);
            sender.sendMessage(plugin.getMessage("interval-changed", "{interval}", String.valueOf(interval)));
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessage("invalid-number"));
        }
    }
    
    /**
     * Show status
     */
    private void handleStatus(CommandSender sender) {
        boolean running = plugin.getItemManager().isRunning();
        int interval = plugin.getItemManager().getInterval();
        
        sender.sendMessage(plugin.colorize("&8&m----------&r &a&lRandomItemChallenge &8&m----------"));
        sender.sendMessage(plugin.colorize("&7Status: " + (running ? "&aRUNNING" : "&cSTOPPED")));
        sender.sendMessage(plugin.colorize("&7Interval: &f" + interval + " seconds"));
        sender.sendMessage(plugin.colorize("&7Mode: &f" + 
            (plugin.getConfig().getBoolean("items.use-custom-list") ? "Custom List" : "All Items")));
        sender.sendMessage(plugin.colorize("&8&m------------------------------------------"));
    }
    
    /**
     * Reload configuration
     */
    private void handleReload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(plugin.getMessage("reload"));
    }
    
    /**
     * Send help message
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.colorize("&8&m----------&r &a&lRandomItemChallenge &8&m----------"));
        sender.sendMessage(plugin.colorize("&a/ric start &8- &7Start the challenge"));
        sender.sendMessage(plugin.colorize("&a/ric stop &8- &7Stop the challenge"));
        sender.sendMessage(plugin.colorize("&a/ric toggle &8- &7Toggle on/off"));
        sender.sendMessage(plugin.colorize("&a/ric skip &8- &7Skip to next item drop"));
        sender.sendMessage(plugin.colorize("&a/ric interval <seconds> &8- &7Set drop interval"));
        sender.sendMessage(plugin.colorize("&a/ric status &8- &7Show current status"));
        sender.sendMessage(plugin.colorize("&a/ric reload &8- &7Reload configuration"));
        sender.sendMessage(plugin.colorize("&8&m------------------------------------------"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions = Arrays.asList("start", "stop", "toggle", "skip", "interval", "status", "reload", "help");
            return filter(completions, args[0]);
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("interval")) {
            return Arrays.asList("10", "30", "60", "120");
        }
        
        return completions;
    }
    
    private List<String> filter(List<String> list, String prefix) {
        return list.stream()
            .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
            .collect(Collectors.toList());
    }
}
