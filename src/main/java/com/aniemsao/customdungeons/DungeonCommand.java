package com.aniemsao.customdungeons;

import com.aniemsao.customdungeons.manager.StructureConfig;
import com.aniemsao.customdungeons.gui.LootGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DungeonCommand implements CommandExecutor, TabCompleter {
    @Override
    public List<String> onTabComplete(org.bukkit.command.CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("gui", "spawn", "random").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("gui")) {
            return CustomDungeons.getInstance().getConfig().getStringList("loot-profiles");
        }
        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        
        CustomDungeons plugin = CustomDungeons.getInstance();
        plugin.getCommand("cdungeon").setTabCompleter(this);
        
        if (args.length > 0 && args[0].equalsIgnoreCase("gui")) {
            String profile = args.length > 1 ? args[1] : "default_loot";
            LootGUI.openLootEditor(player, profile);
            return true;
        }
        
        if (args.length > 0 && args[0].equalsIgnoreCase("spawn")) {
            CustomDungeons.getInstance().getDungeonManager().generateDungeon(player.getLocation(), "default_dungeon");
            player.sendMessage("§aDungeon spawned!");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("random")) {
            int range = 5000;
            StringBuilder sb = new StringBuilder("§d§l[CustomDungeons] §fGenerated dungeons coordinates:\n");
            for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
                if (!CustomDungeons.getInstance().getConfig().getStringList("worlds").contains(world.getName())) continue;
                sb.append("§eWorld: ").append(world.getName()).append("\n");
                for (int i = 0; i < 10; i++) {
                    double x = (Math.random() * range * 2) - range;
                    double z = (Math.random() * range * 2) - range;
                    int y = world.getHighestBlockYAt((int)x, (int)z) + 1;
                    if (y < 5) y = 60;
                    org.bukkit.Location loc = new org.bukkit.Location(world, x, y, z);
                    
                    // Advanced generation based on dimension and depth
                    String worldName = world.getName().toLowerCase();
                    StructureConfig s = new StructureConfig("Dungeon_" + i);
                    int radius = (int) (Math.random() * 8) + 8; // Larger: 8 to 16 radius
                    int height = (int) (Math.random() * 6) + 7; // Taller: 7 to 13 height
                    org.bukkit.Material wallMat;
                    org.bukkit.Material decorMat;
                    org.bukkit.Material floorMat;
                    org.bukkit.Material roofMat;

                    boolean isUnderground = Math.random() > 0.4;
                    if (isUnderground && !worldName.contains("the_end") && !worldName.contains("nether")) {
                        y = 10 + (int)(Math.random() * 30);
                        wallMat = org.bukkit.Material.COBBLESTONE;
                        decorMat = org.bukkit.Material.MOSSY_COBBLESTONE;
                        floorMat = org.bukkit.Material.STONE;
                        roofMat = org.bukkit.Material.COBBLESTONE_SLAB;
                    } else if (worldName.contains("nether")) {
                        y = (int)(Math.random() * 80) + 30;
                        wallMat = org.bukkit.Material.NETHER_BRICKS;
                        decorMat = org.bukkit.Material.GLOWSTONE;
                        floorMat = org.bukkit.Material.SOUL_SAND;
                        roofMat = org.bukkit.Material.NETHER_BRICK_SLAB;
                    } else if (worldName.contains("the_end")) {
                        wallMat = org.bukkit.Material.END_STONE_BRICKS;
                        decorMat = org.bukkit.Material.PURPUR_PILLAR;
                        floorMat = org.bukkit.Material.END_STONE;
                        roofMat = org.bukkit.Material.PURPUR_SLAB;
                    } else {
                        if (y > 120) {
                            wallMat = org.bukkit.Material.QUARTZ_BRICKS;
                            decorMat = org.bukkit.Material.GOLD_BLOCK;
                            floorMat = org.bukkit.Material.SMOOTH_QUARTZ;
                            roofMat = org.bukkit.Material.QUARTZ_SLAB;
                        } else {
                            wallMat = org.bukkit.Material.STONE_BRICKS;
                            decorMat = org.bukkit.Material.CHISELED_STONE_BRICKS;
                            floorMat = org.bukkit.Material.MOSSY_STONE_BRICKS;
                            roofMat = org.bukkit.Material.STONE_BRICK_SLAB;
                        }
                    }

                    // Build Structure with Decor and Interior
                    int roofType = new java.util.Random().nextInt(3); // 0: Gabled, 1: Domed, 2: Flat with Battlements

                    for (int dx = -radius; dx <= radius; dx++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                            // Foundation
                            s.addBlock(dx, 0, dz, floorMat);
                            
                            // Walls
                            if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                                for (int dy = 1; dy <= height; dy++) {
                                    // Window patterns with decorative shutters (trapdoors)
                                    if (dy >= 3 && dy <= 5 && (dx % 4 == 0 || dz % 4 == 0)) {
                                        s.addBlock(dx, dy, dz, org.bukkit.Material.GLASS_PANE);
                                    } else {
                                        s.addBlock(dx, dy, dz, wallMat);
                                    }
                                }
                            }
                            
                            // Pillars at corners and mid-points (more detail)
                            if ((Math.abs(dx) == radius && Math.abs(dz) == radius) || 
                                (Math.abs(dx) == radius && dz == 0) || (dx == 0 && Math.abs(dz) == radius)) {
                                for (int dy = 0; dy <= height + 2; dy++) {
                                    s.addBlock(dx, dy, dz, decorMat);
                                    // Add decorative caps to pillars
                                    if (dy == height + 2) s.addBlock(dx, dy + 1, dz, org.bukkit.Material.valueOf(wallMat.name().replace("BRICKS", "SLAB")));
                                }
                            }
                        }
                    }

                    // Randomized Roof Styles
                    if (roofType == 0) { // Gabled (Slanted)
                        for (int dy = 0; dy <= radius; dy++) {
                            for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                                if (height + 1 + dy <= height + 1 + radius) {
                                    s.addBlock(dy - radius, height + 1 + dy, dz, roofMat);
                                    s.addBlock(radius - dy, height + 1 + dy, dz, roofMat);
                                }
                            }
                        }
                    } else if (roofType == 1) { // Domed
                        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
                            for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                                double dist = Math.sqrt(dx * dx + dz * dz);
                                if (dist <= radius + 1) {
                                    int dy = (int) (Math.sqrt(Math.max(0, (radius + 1) * (radius + 1) - dist * dist)) * 0.6);
                                    s.addBlock(dx, height + 1 + dy, dz, roofMat);
                                }
                            }
                        }
                    } else { // Flat with Battlements
                        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
                            for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                                s.addBlock(dx, height + 1, dz, floorMat);
                                // Battlements (Merlons)
                                if ((Math.abs(dx) == radius + 1 || Math.abs(dz) == radius + 1) && (dx + dz) % 2 == 0) {
                                    s.addBlock(dx, height + 2, dz, wallMat);
                                }
                            }
                        }
                    }
                    
                    // Extra Interior Decoration (Furniture)
                    // Tables and chairs
                    s.addBlock(-2, 1, -2, org.bukkit.Material.OAK_FENCE);
                    s.addBlock(-2, 2, -2, org.bukkit.Material.OAK_PRESSURE_PLATE);
                    s.addBlock(-2, 1, -1, org.bukkit.Material.OAK_STAIRS);
                    
                    // Paintings/Wall Decor (Represented by banners/item frames)
                    s.addBlock(0, 3, radius - 1, org.bukkit.Material.WHITE_BANNER);
                    s.addBlock(0, 3, -radius + 1, org.bukkit.Material.WHITE_BANNER);
                    
                    // Interior plants
                    s.addBlock(radius - 2, 1, 2, org.bukkit.Material.FLOWER_POT);
                    s.addBlock(radius - 2, 2, 2, org.bukkit.Material.AZURE_BLUET);
                    
                    // Armor Stands (represented by iron blocks for now, or just decoration)
                    s.addBlock(-radius + 2, 1, radius - 2, org.bukkit.Material.LANTERN);
                    s.addBlock(radius - 2, 1, -radius + 2, org.bukkit.Material.SMOOTH_STONE_SLAB);
                    
                    // Interior details
                    // Central lighting chandelier
                    for (int dy = height - 1; dy <= height; dy++) {
                        s.addBlock(0, dy, 0, org.bukkit.Material.IRON_BARS);
                    }
                    s.addBlock(0, height - 2, 0, org.bukkit.Material.GLOWSTONE);
                    
                    // Interior Carpeting
                    for (int dx = -2; dx <= 2; dx++) {
                        for (int dz = -2; dz <= 2; dz++) {
                            s.addBlock(dx, 1, dz, org.bukkit.Material.RED_CARPET);
                        }
                    }

                    // Bookshelves and decoration
                    s.addBlock(-radius + 2, 1, -radius + 2, org.bukkit.Material.BOOKSHELF);
                    s.addBlock(-radius + 2, 2, -radius + 2, org.bukkit.Material.BOOKSHELF);
                    s.addBlock(radius - 2, 1, -radius + 2, org.bukkit.Material.CRAFTING_TABLE);
                    s.addBlock(radius - 2, 1, radius - 2, org.bukkit.Material.ANVIL);
                    
                    // Entrance
                    s.addBlock(radius, 1, 0, org.bukkit.Material.AIR);
                    s.addBlock(radius, 2, 0, org.bukkit.Material.AIR);
                    s.addBlock(radius + 1, 1, 0, org.bukkit.Material.STONE_BRICK_STAIRS);
                    
                    // Multi-chest loot
                    int chestCount = (int) (Math.random() * 10) + 5; // More chests: 5 to 15
                    for (int c = 0; c < chestCount; c++) {
                        int cx = (int)(Math.random() * (radius*2-4)) - (radius-2);
                        int cz = (int)(Math.random() * (radius*2-4)) - (radius-2);
                        org.bukkit.Material existing = s.getBlock(cx, 1, cz);
                        if (existing == null || existing == org.bukkit.Material.AIR) {
                            // Randomly select a loot profile from config
                            java.util.List<String> profiles = CustomDungeons.getInstance().getConfig().getStringList("loot-profiles");
                            String profile = (profiles == null || profiles.isEmpty()) ? "default_loot" : profiles.get(new java.util.Random().nextInt(profiles.size()));
                            
                            s.addBlock(cx, 1, cz, org.bukkit.Material.CHEST);
                            
                            // Schedule chest filling with specific profile
                            new org.bukkit.scheduler.BukkitRunnable() {
                                @Override
                                public void run() {
                                    org.bukkit.block.Block b = loc.clone().add(cx, 1, cz).getBlock();
                                    if (b.getState() instanceof org.bukkit.block.Chest) {
                                        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) b.getState();
                                        CustomDungeons.getInstance().getLootManager().fillChest(chest.getInventory(), profile);
                                    }
                                }
                            }.runTaskLater(CustomDungeons.getInstance(), 2L);
                        }
                    }
                    
                    loc.setY(y);
                    CustomDungeons.getInstance().getDungeonManager().generateUniqueDungeon(loc, s);
                    
                    sb.append(" §7- §fX: ").append((int)x).append(" Y: ").append(y).append(" Z: ").append((int)z).append("\n");
                }
            }
            org.bukkit.Bukkit.broadcastMessage(sb.toString());
            return true;
        }
        
        return false;
    }
}
