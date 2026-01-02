package com.github.tejaslamba2006.randomitemchallenge.managers;

import com.github.tejaslamba2006.randomitemchallenge.RandomItemChallenge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the random item distribution
 */
public class ItemManager {
    
    private final RandomItemChallenge plugin;
    private final Random random;
    private final List<Material> itemPool;
    private final Set<Material> blacklist;
    
    private BukkitTask dropTask;
    private BukkitTask countdownTask;
    private boolean running;
    private int secondsUntilNext;
    
    public ItemManager(RandomItemChallenge plugin) {
        this.plugin = plugin;
        this.random = new Random();
        this.itemPool = new ArrayList<>();
        this.blacklist = new HashSet<>();
        this.running = false;
        
        loadItemPool();
    }
    
    /**
     * Load the item pool from configuration
     */
    private void loadItemPool() {
        itemPool.clear();
        blacklist.clear();
        
        // Load blacklist
        List<String> blacklistNames = plugin.getConfig().getStringList("items.blacklist");
        for (String name : blacklistNames) {
            try {
                blacklist.add(Material.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in blacklist: " + name);
            }
        }
        
        // Check if using custom list
        if (plugin.getConfig().getBoolean("items.use-custom-list", true)) {
            // Load custom list
            List<String> customList = plugin.getConfig().getStringList("items.custom-list");
            for (String name : customList) {
                try {
                    Material mat = Material.valueOf(name.toUpperCase());
                    if (mat.isItem() && !blacklist.contains(mat)) {
                        itemPool.add(mat);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in custom-list: " + name);
                }
            }
            plugin.getLogger().info("Loaded " + itemPool.size() + " items from custom list.");
        } else {
            // Use all Minecraft items except blacklist
            for (Material mat : Material.values()) {
                if (mat.isItem() && !mat.isAir() && !blacklist.contains(mat)) {
                    // Skip some obviously problematic items
                    String name = mat.name();
                    if (name.contains("SPAWN_EGG") && !plugin.getConfig().getBoolean("items.allow-spawn-eggs", false)) {
                        continue;
                    }
                    if (name.contains("_WALL_") || name.endsWith("_WALL")) {
                        continue; // Wall variants aren't proper items
                    }
                    itemPool.add(mat);
                }
            }
            plugin.getLogger().info("Loaded " + itemPool.size() + " items (all items minus blacklist).");
        }
    }
    
    /**
     * Start the random item challenge
     */
    public void start() {
        if (running) return;
        
        running = true;
        int interval = plugin.getConfig().getInt("interval", 30);
        secondsUntilNext = interval;
        
        // Show title
        if (plugin.getConfig().getBoolean("game.show-title-on-start", true)) {
            showStartTitle(interval);
        }
        
        // Broadcast
        if (plugin.getConfig().getBoolean("game.broadcast-toggle", true)) {
            Bukkit.broadcast(plugin.getMessage("started"));
        }
        
        // Start countdown task
        if (plugin.getConfig().getBoolean("game.show-countdown", true)) {
            countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                secondsUntilNext--;
                if (secondsUntilNext <= 0) {
                    secondsUntilNext = plugin.getConfig().getInt("interval", 30);
                }
                
                // Show countdown in action bar
                for (Player player : getEligiblePlayers()) {
                    player.sendActionBar(plugin.colorize("&7Next item in: &a" + secondsUntilNext + "s"));
                }
            }, 20L, 20L);
        }
        
        // Start drop task
        long intervalTicks = interval * 20L;
        dropTask = Bukkit.getScheduler().runTaskTimer(plugin, this::dropItems, intervalTicks, intervalTicks);
        
        plugin.getLogger().info("Random Item Challenge started! Interval: " + interval + " seconds");
    }
    
    /**
     * Stop the random item challenge
     */
    public void stop() {
        stop(true);
    }
    
    /**
     * Stop with optional broadcast
     */
    public void stop(boolean broadcast) {
        if (!running) return;
        
        running = false;
        
        if (dropTask != null) {
            dropTask.cancel();
            dropTask = null;
        }
        
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        
        // Clear action bars
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendActionBar(Component.empty());
        }
        
        if (broadcast && plugin.getConfig().getBoolean("game.broadcast-toggle", true)) {
            Bukkit.broadcast(plugin.getMessage("stopped"));
        }
        
        plugin.getLogger().info("Random Item Challenge stopped.");
    }
    
    /**
     * Skip to next item drop immediately
     */
    public void skip() {
        if (!running) return;
        
        dropItems();
        secondsUntilNext = plugin.getConfig().getInt("interval", 30);
    }
    
    /**
     * Check if challenge is running
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Drop random items to all eligible players
     */
    private void dropItems() {
        List<Player> players = getEligiblePlayers();
        
        if (players.isEmpty()) return;
        
        int itemsPerDrop = plugin.getConfig().getInt("items-per-drop", 1);
        
        for (Player player : players) {
            for (int i = 0; i < itemsPerDrop; i++) {
                ItemStack item = generateRandomItem();
                giveItem(player, item);
            }
        }
    }
    
    /**
     * Get list of eligible players
     */
    private List<Player> getEligiblePlayers() {
        return Bukkit.getOnlinePlayers().stream()
            .filter(p -> !p.hasPermission("randomitemchallenge.bypass"))
            .filter(p -> p.hasPermission("randomitemchallenge.receive"))
            .filter(this::isPlayerEligible)
            .collect(Collectors.toList());
    }
    
    /**
     * Check if a player is eligible
     */
    private boolean isPlayerEligible(Player player) {
        // Check game mode
        if (plugin.getConfig().getBoolean("players.survival-only", true)) {
            if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
                return false;
            }
        }
        
        // Check world whitelist
        if (plugin.getConfig().getBoolean("players.world-whitelist-enabled", false)) {
            List<String> whitelist = plugin.getConfig().getStringList("players.world-whitelist");
            if (!whitelist.contains(player.getWorld().getName())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Generate a random item
     */
    private ItemStack generateRandomItem() {
        if (itemPool.isEmpty()) {
            return new ItemStack(Material.DIRT); // Fallback
        }
        
        // Pick random material
        Material material = itemPool.get(random.nextInt(itemPool.size()));
        
        // Determine quantity
        int quantity = 1;
        if (material.getMaxStackSize() > 1) {
            if (plugin.getConfig().getBoolean("quantity.random-quantity", true)) {
                int min = plugin.getConfig().getInt("quantity.min", 1);
                int max = Math.min(plugin.getConfig().getInt("quantity.max", 16), material.getMaxStackSize());
                quantity = min + random.nextInt(Math.max(1, max - min + 1));
            } else {
                quantity = Math.min(plugin.getConfig().getInt("quantity.fixed", 1), material.getMaxStackSize());
            }
        }
        
        ItemStack item = new ItemStack(material, quantity);
        
        // Maybe add enchantments
        double enchantChance = plugin.getConfig().getDouble("enchantments.chance", 0.1);
        if (random.nextDouble() < enchantChance && canBeEnchanted(material)) {
            addRandomEnchantments(item);
        }
        
        return item;
    }
    
    /**
     * Check if material can be enchanted
     */
    private boolean canBeEnchanted(Material material) {
        // Check if any enchantment is applicable
        for (Enchantment ench : Enchantment.values()) {
            ItemStack test = new ItemStack(material);
            if (ench.canEnchantItem(test)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Add random enchantments to an item
     */
    private void addRandomEnchantments(ItemStack item) {
        List<Enchantment> applicable = new ArrayList<>();
        boolean allowTreasure = plugin.getConfig().getBoolean("enchantments.allow-treasure", false);
        
        for (Enchantment ench : Enchantment.values()) {
            if (ench.canEnchantItem(item)) {
                if (!ench.isTreasure() || allowTreasure) {
                    applicable.add(ench);
                }
            }
        }
        
        if (applicable.isEmpty()) return;
        
        int maxEnchants = plugin.getConfig().getInt("enchantments.max-enchants", 2);
        int numEnchants = 1 + random.nextInt(maxEnchants);
        int maxLevel = plugin.getConfig().getInt("enchantments.max-level", 3);
        
        Collections.shuffle(applicable, random);
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        for (int i = 0; i < Math.min(numEnchants, applicable.size()); i++) {
            Enchantment ench = applicable.get(i);
            int level = 1 + random.nextInt(Math.min(maxLevel, ench.getMaxLevel()));
            meta.addEnchant(ench, level, true);
        }
        
        item.setItemMeta(meta);
    }
    
    /**
     * Give item to player
     */
    private void giveItem(Player player, ItemStack item) {
        String deliveryMethod = plugin.getConfig().getString("delivery.method", "INVENTORY");
        
        boolean given = false;
        
        switch (deliveryMethod.toUpperCase()) {
            case "HOTBAR" -> {
                // Try hotbar first
                for (int i = 0; i <= 8; i++) {
                    if (player.getInventory().getItem(i) == null) {
                        player.getInventory().setItem(i, item);
                        given = true;
                        break;
                    }
                }
                if (!given) {
                    // Fall through to inventory
                    HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                    if (!leftover.isEmpty()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                    }
                    given = true;
                }
            }
            case "DROP" -> {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                given = true;
            }
            default -> { // INVENTORY
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                if (!leftover.isEmpty()) {
                    for (ItemStack drop : leftover.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    }
                }
                given = true;
            }
        }
        
        if (given) {
            playEffects(player);
            
            // Show message
            if (plugin.getConfig().getBoolean("delivery.show-message", true)) {
                String itemName = formatItemName(item.getType());
                Component message = plugin.colorize("&aYou received: &f" + itemName + " x" + item.getAmount());
                
                if (plugin.getConfig().getBoolean("delivery.use-action-bar", true)) {
                    player.sendActionBar(message);
                } else {
                    player.sendMessage(plugin.getMessage("received-item", 
                        "{item}", itemName, 
                        "{amount}", String.valueOf(item.getAmount())));
                }
            }
        }
    }
    
    /**
     * Format item name nicely
     */
    private String formatItemName(Material material) {
        String name = material.name().replace('_', ' ').toLowerCase();
        // Capitalize first letter of each word
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) result.append(" ");
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }
    
    /**
     * Play delivery effects
     */
    private void playEffects(Player player) {
        // Sound
        if (plugin.getConfig().getBoolean("delivery.sound.enabled", true)) {
            String soundName = plugin.getConfig().getString("delivery.sound.type", "ENTITY_ITEM_PICKUP");
            float volume = (float) plugin.getConfig().getDouble("delivery.sound.volume", 1.0);
            float pitch = (float) plugin.getConfig().getDouble("delivery.sound.pitch", 1.2);
            
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (IllegalArgumentException e) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, volume, pitch);
            }
        }
        
        // Particles
        if (plugin.getConfig().getBoolean("delivery.particles.enabled", true)) {
            String particleType = plugin.getConfig().getString("delivery.particles.type", "HAPPY_VILLAGER");
            int count = plugin.getConfig().getInt("delivery.particles.count", 10);
            
            try {
                Particle particle = Particle.valueOf(particleType.toUpperCase());
                player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0), count, 0.3, 0.3, 0.3, 0);
            } catch (IllegalArgumentException e) {
                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), count, 0.3, 0.3, 0.3, 0);
            }
        }
    }
    
    /**
     * Show start title to all players
     */
    private void showStartTitle(int interval) {
        String mainTitle = plugin.getConfig().getString("game.title.main", "&a&lRANDOM ITEMS!");
        String subTitle = plugin.getConfig().getString("game.title.sub", "&7A new item every {interval} seconds!")
            .replace("{interval}", String.valueOf(interval));
        int fadeIn = plugin.getConfig().getInt("game.title.fade-in", 10);
        int stay = plugin.getConfig().getInt("game.title.stay", 40);
        int fadeOut = plugin.getConfig().getInt("game.title.fade-out", 10);
        
        Title title = Title.title(
            plugin.colorize(mainTitle),
            plugin.colorize(subTitle),
            Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L)
            )
        );
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(title);
        }
    }
    
    /**
     * Set interval
     */
    public void setInterval(int seconds) {
        plugin.getConfig().set("interval", seconds);
        plugin.saveConfig();
        
        // Restart if running
        if (running) {
            stop(false);
            start();
        }
    }
    
    /**
     * Get current interval
     */
    public int getInterval() {
        return plugin.getConfig().getInt("interval", 30);
    }
    
    /**
     * Reload manager
     */
    public void reload() {
        boolean wasRunning = running;
        
        if (running) {
            stop(false);
        }
        
        loadItemPool();
        
        if (wasRunning) {
            start();
        }
    }
}
