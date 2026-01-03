package com.aniemsao.customdungeons.gui;

import com.aniemsao.customdungeons.CustomDungeons;
import com.aniemsao.customdungeons.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class LootGUI {
    public static void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ColorUtils.format("#AA00FFDungeon Editor"));
        
        ItemStack create = new ItemStack(Material.CHEST);
        ItemMeta meta = create.getItemMeta();
        meta.setDisplayName(ColorUtils.format("#00FF00Edit Default Loot Profile"));
        create.setItemMeta(meta);
        
        inv.setItem(13, create);
        player.openInventory(inv);
    }

    public static void openLootEditor(Player player, String profileName) {
        Inventory inv = Bukkit.createInventory(null, 54, ColorUtils.format("#FFBB00Editing: " + profileName));
        
        YamlConfiguration config = CustomDungeons.getInstance().getLootManager().getLoot(profileName);
        if (config != null && config.getConfigurationSection("items") != null) {
            for (String key : config.getConfigurationSection("items").getKeys(false)) {
                int slot = Integer.parseInt(key);
                ItemStack item = config.getItemStack("items." + key + ".item");
                int chance = config.getInt("items." + key + ".chance", 100);
                
                if (item != null && slot < 54) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        List<String> lore = meta.hasLore() ? meta.getLore() : new java.util.ArrayList<>();
                        // Remove existing chance-related lore lines to prevent spam
                        lore.removeIf(line -> line.contains("Chance:") || line.contains("Right-click to edit"));
                        lore.add("");
                        lore.add("§7Chance: §f" + chance + "%");
                        lore.add("§8Right-click to edit chance");
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    }
                    inv.setItem(slot, item);
                }
            }
        }
        
        player.openInventory(inv);
    }
}
