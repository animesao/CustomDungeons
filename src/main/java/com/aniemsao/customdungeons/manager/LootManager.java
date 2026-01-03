package com.aniemsao.customdungeons.manager;

import com.aniemsao.customdungeons.CustomDungeons;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class LootManager {
    private final Map<String, YamlConfiguration> lootConfigs = new HashMap<>();
    private final File folder;

    public LootManager() {
        this.folder = new File(CustomDungeons.getInstance().getDataFolder(), "loot");
        if (!folder.exists()) folder.mkdirs();
        loadAll();
    }

    public void loadAll() {
        lootConfigs.clear();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                lootConfigs.put(file.getName().replace(".yml", ""), YamlConfiguration.loadConfiguration(file));
            }
        }
    }

    public void saveLoot(String name, ItemStack[] items) {
        File file = new File(folder, name + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        YamlConfiguration old = getLoot(name);
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                ItemStack item = items[i].clone();
                org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    if (lore != null) {
                        lore.removeIf(line -> line.contains("Chance:") || line.contains("Right-click to edit"));
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    }
                }
                config.set("items." + i + ".item", item);
                int chance = 100;
                if (old != null && old.contains("items." + i + ".chance")) {
                    chance = old.getInt("items." + i + ".chance");
                }
                config.set("items." + i + ".chance", chance);
            }
        }
        try {
            config.save(file);
            lootConfigs.put(name, config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setChance(String name, int slot, int chance) {
        YamlConfiguration config = getLoot(name);
        if (config != null && config.contains("items." + slot)) {
            config.set("items." + slot + ".chance", chance);
            try {
                config.save(new File(folder, name + ".yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void fillChest(org.bukkit.inventory.Inventory inv, String profileName) {
        YamlConfiguration config = lootConfigs.get(profileName);
        if (config == null || !config.contains("items")) {
            // Fallback or debug
            return;
        }

        java.util.Random random = new java.util.Random();
        org.bukkit.configuration.ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) return;

        for (String key : itemsSection.getKeys(false)) {
            ItemStack item = config.getItemStack("items." + key + ".item");
            int chance = config.getInt("items." + key + ".chance", 100);

            if (item != null && random.nextInt(100) < chance) {
                int slot = random.nextInt(inv.getSize());
                ItemStack chestItem = item.clone();
                org.bukkit.inventory.meta.ItemMeta meta = chestItem.getItemMeta();
                if (meta != null && meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    if (lore != null) {
                        lore.removeIf(line -> line.contains("Chance:") || line.contains("edit chance") || line.trim().isEmpty());
                        meta.setLore(lore);
                        chestItem.setItemMeta(meta);
                    }
                }
                inv.setItem(slot, chestItem);
            }
        }
    }

    public YamlConfiguration getLoot(String name) {
        return lootConfigs.get(name);
    }
}
