package com.aniemsao.customdungeons.manager;

import org.bukkit.Material;
import java.util.HashMap;
import java.util.Map;

public class StructureConfig {
    private final String name;
    private final Map<org.bukkit.util.Vector, Material> blocks = new HashMap<>();

    public StructureConfig(String name) {
        this.name = name;
    }

    public void addBlock(int x, int y, int z, Material material) {
        blocks.put(new org.bukkit.util.Vector(x, y, z), material);
    }

    public Material getBlock(int x, int y, int z) {
        return blocks.get(new org.bukkit.util.Vector(x, y, z));
    }

    public String getName() {
        return name;
    }

    public Map<org.bukkit.util.Vector, Material> getBlocks() {
        return blocks;
    }
}
