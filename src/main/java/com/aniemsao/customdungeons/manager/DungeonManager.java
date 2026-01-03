package com.aniemsao.customdungeons.manager;

import com.aniemsao.customdungeons.CustomDungeons;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

public class DungeonManager {
    private Random random = new Random();

    public void generateDungeon(org.bukkit.Location loc, String schematicName) {
        File file = new File(CustomDungeons.getInstance().getDataFolder() + "/schematics/" + schematicName + ".schem");
        if (!file.exists()) return;

        com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat format = com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats.findByFile(file);
        try (com.sk89q.worldedit.extent.clipboard.io.ClipboardReader reader = format.getReader(new java.io.FileInputStream(file))) {
            com.sk89q.worldedit.extent.clipboard.Clipboard clipboard = reader.read();
            try (com.sk89q.worldedit.EditSession editSession = com.sk89q.worldedit.WorldEdit.getInstance().getEditSessionFactory().getEditSession(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(loc.getWorld()), -1)) {
                com.sk89q.worldedit.function.operation.Operation operation = new com.sk89q.worldedit.session.ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(com.sk89q.worldedit.math.BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()))
                        .ignoreAirBlocks(false)
                        .build();
                com.sk89q.worldedit.function.operation.Operations.complete(operation);
                
                // After pasting, look for chests in the pasted area
                com.sk89q.worldedit.math.BlockVector3 min = clipboard.getMinimumPoint();
                com.sk89q.worldedit.math.BlockVector3 max = clipboard.getMaximumPoint();
                int width = max.getX() - min.getX();
                int height = max.getY() - min.getY();
                int length = max.getZ() - min.getZ();
                
                new org.bukkit.scheduler.BukkitRunnable() {
                    @Override
                    public void run() {
                        for (int x = 0; x <= width; x++) {
                            for (int y = 0; y <= height; y++) {
                                for (int z = 0; z <= length; z++) {
                                    org.bukkit.block.Block b = loc.clone().add(x, y, z).getBlock();
                                    if (b.getType() == org.bukkit.Material.CHEST) {
                                        if (b.getState() instanceof org.bukkit.block.Chest) {
                                            org.bukkit.block.Chest chest = (org.bukkit.block.Chest) b.getState();
                                            CustomDungeons.getInstance().getLootManager().fillChest(chest.getInventory(), "default_loot");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }.runTaskLater(CustomDungeons.getInstance(), 2L);
            }
        } catch (java.io.IOException | com.sk89q.worldedit.WorldEditException e) {
            e.printStackTrace();
        }
    }

    public void generateUniqueDungeon(org.bukkit.Location loc, StructureConfig config) {
        // Clear area first to prevent "merged" blocks (image fix)
        int minX = 0, maxX = 0, minY = 0, maxY = 0, minZ = 0, maxZ = 0;
        for (org.bukkit.util.Vector v : config.getBlocks().keySet()) {
            minX = Math.min(minX, v.getBlockX());
            maxX = Math.max(maxX, v.getBlockX());
            minY = Math.min(minY, v.getBlockY());
            maxY = Math.max(maxY, v.getBlockY());
            minZ = Math.min(minZ, v.getBlockZ());
            maxZ = Math.max(maxZ, v.getBlockZ());
        }
        for (int x = minX - 1; x <= maxX + 1; x++) {
            for (int y = minY; y <= maxY + 1; y++) {
                for (int z = minZ - 1; z <= maxZ + 1; z++) {
                    loc.clone().add(x, y, z).getBlock().setType(org.bukkit.Material.AIR);
                }
            }
        }

        for (java.util.Map.Entry<org.bukkit.util.Vector, org.bukkit.Material> entry : config.getBlocks().entrySet()) {
            org.bukkit.util.Vector vec = entry.getKey();
            org.bukkit.block.Block block = loc.clone().add(vec).getBlock();
            block.setType(entry.getValue());
            
            if (entry.getValue() == org.bukkit.Material.CHEST) {
                // Randomize loot profile
                String[] profiles = {"default_loot", "rare_loot", "epic_loot", "dungeon_treasure"};
                String profile = profiles[new java.util.Random().nextInt(profiles.length)];
                
                new org.bukkit.scheduler.BukkitRunnable() {
                    @Override
                    public void run() {
                        if (block.getState() instanceof org.bukkit.block.Chest) {
                            org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
                            CustomDungeons.getInstance().getLootManager().fillChest(chest.getInventory(), profile);
                        }
                    }
                }.runTaskLater(CustomDungeons.getInstance(), 1L);
            }
        }
        
        // Spawn custom mobs
        spawnDungeonMobs(loc, maxX);
    }

    private void spawnDungeonMobs(org.bukkit.Location loc, int radius) {
        org.bukkit.configuration.ConfigurationSection mobSection = CustomDungeons.getInstance().getConfig().getConfigurationSection("mobs");
        if (mobSection == null) return;
        
        java.util.List<String> mobKeys = new java.util.ArrayList<>(mobSection.getKeys(false));
        if (mobKeys.isEmpty()) return;
        
        java.util.Random rand = new java.util.Random();
        int mobCount = rand.nextInt(5) + 3;
        
        for (int i = 0; i < mobCount; i++) {
            double rx = rand.nextInt(radius * 2) - radius;
            double rz = rand.nextInt(radius * 2) - radius;
            org.bukkit.Location spawnLoc = loc.clone().add(rx, 1.5, rz);
            
            String mobKey = mobKeys.get(rand.nextInt(mobKeys.size()));
            org.bukkit.entity.EntityType type = org.bukkit.entity.EntityType.valueOf(mobSection.getString(mobKey + ".type", "ZOMBIE"));
            
            org.bukkit.entity.Entity entity = loc.getWorld().spawnEntity(spawnLoc, type);
            if (entity instanceof org.bukkit.entity.LivingEntity) {
                org.bukkit.entity.LivingEntity le = (org.bukkit.entity.LivingEntity) entity;
                le.setCustomName(com.aniemsao.customdungeons.utils.ColorUtils.format(mobSection.getString(mobKey + ".name", "Dungeon Mob")));
                le.setCustomNameVisible(true);
                le.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(mobSection.getDouble(mobKey + ".hp", 20.0));
                le.setHealth(mobSection.getDouble(mobKey + ".hp", 20.0));
                
                if (le.getEquipment() != null) {
                    if (mobSection.contains(mobKey + ".armor.helmet")) le.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.valueOf(mobSection.getString(mobKey + ".armor.helmet"))));
                    if (mobSection.contains(mobKey + ".armor.chestplate")) le.getEquipment().setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.valueOf(mobSection.getString(mobKey + ".armor.chestplate"))));
                    if (mobSection.contains(mobKey + ".armor.leggings")) le.getEquipment().setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.valueOf(mobSection.getString(mobKey + ".armor.leggings"))));
                    if (mobSection.contains(mobKey + ".armor.boots")) le.getEquipment().setBoots(new org.bukkit.inventory.ItemStack(org.bukkit.Material.valueOf(mobSection.getString(mobKey + ".armor.boots"))));
                    if (mobSection.contains(mobKey + ".weapon")) le.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.valueOf(mobSection.getString(mobKey + ".weapon"))));
                }
            }
        }
    }

    public void handleChunkLoad(World world, int chunkX, int chunkZ) {
        FileConfiguration config = CustomDungeons.getInstance().getConfig();
        if (!config.getStringList("worlds").contains(world.getName())) return;

        double chance = config.getDouble("generation-chance", 0.01);
        if (random.nextDouble() < chance) {
            org.bukkit.Location loc = new org.bukkit.Location(world, chunkX << 4, 60, chunkZ << 4);
            // logic for finding surface or specific Y
            generateDungeon(loc, "default_dungeon");
        }
    }
}
