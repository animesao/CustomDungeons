package com.aniemsao.customdungeons.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class DefaultAssets {
    public static void createDefaults() {
        // This would initialize default loot if files don't exist
    }

    public static ItemStack getSampleItem() {
        ItemStack item = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§lLegendary Artifact");
            meta.setLore(Arrays.asList("§7A powerful item found in dungeons.", "§8Author: aniemsao"));
            item.setItemMeta(meta);
        }
        return item;
    }
}
