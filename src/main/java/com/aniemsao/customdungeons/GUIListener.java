package com.aniemsao.customdungeons;

import com.aniemsao.customdungeons.gui.LootGUI;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GUIListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title.contains("Dungeon Editor")) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.CHEST) {
                LootGUI.openLootEditor((Player) event.getWhoClicked(), "default_loot");
            }
        } else if (title.contains("Editing: ")) {
            String profileName = org.bukkit.ChatColor.stripColor(title.replace("Editing: ", ""));
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < 54) {
                // Allow taking items if it's not a chance edit
                if (event.getClick().isLeftClick() || event.getClick().isRightClick()) {
                    // If it's a chance edit (Shift or specific clicks if we want to distinguish)
                    // Let's make Right-Click ONLY for chance to allow normal left-click item interaction
                    if (event.getClick().isRightClick()) {
                        event.setCancelled(true);
                        int change = event.isShiftClick() ? -10 : -1;
                        updateChance(profileName, slot, change, (Player) event.getWhoClicked());
                    } else if (event.getClick().isLeftClick() && event.isShiftClick()) {
                        event.setCancelled(true);
                        updateChance(profileName, slot, 10, (Player) event.getWhoClicked());
                    }
                    // Normal left click or other clicks allowed to move items
                }
            }
        }
    }

    private void updateChance(String profileName, int slot, int change, Player p) {
        YamlConfiguration config = CustomDungeons.getInstance().getLootManager().getLoot(profileName);
        if (config != null && config.contains("items." + slot)) {
            int oldChance = config.getInt("items." + slot + ".chance", 100);
            int newChance = Math.max(0, Math.min(100, oldChance + change));
            CustomDungeons.getInstance().getLootManager().setChance(profileName, slot, newChance);
            p.sendMessage("§a[Loot] §fSlot " + slot + " §7-> §e" + newChance + "%");
            LootGUI.openLootEditor(p, profileName);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        if (title.contains("Editing: ")) {
            String profileName = title.replace("Editing: ", "");
            profileName = org.bukkit.ChatColor.stripColor(profileName);
            ItemStack[] contents = event.getInventory().getContents();
            CustomDungeons.getInstance().getLootManager().saveLoot(profileName, contents);
            event.getPlayer().sendMessage("§aLoot profile '" + profileName + "' saved!");
        }
    }
}
