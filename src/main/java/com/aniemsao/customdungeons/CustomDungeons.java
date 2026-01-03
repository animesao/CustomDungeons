package com.aniemsao.customdungeons;

import com.aniemsao.customdungeons.manager.DungeonManager;
import com.aniemsao.customdungeons.manager.DatabaseManager;
import com.aniemsao.customdungeons.manager.LootManager;
import com.aniemsao.customdungeons.utils.ColorUtils;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomDungeons extends JavaPlugin {
    private static CustomDungeons instance;
    private DungeonManager dungeonManager;
    private DatabaseManager databaseManager;
    private LootManager lootManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        databaseManager = new DatabaseManager();
        databaseManager.connect();
        
        dungeonManager = new DungeonManager();
        lootManager = new LootManager();
        
        DungeonCommand dungeonCommand = new DungeonCommand();
        getCommand("cdungeon").setExecutor(dungeonCommand);
        getCommand("cdungeon").setTabCompleter(dungeonCommand);
        
        getServer().getPluginManager().registerEvents(new DungeonListener(), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        
        getLogger().info(ColorUtils.format("#00FF00CustomDungeons v3.0 by aniemsao enabled!"));
    }

    public LootManager getLootManager() {
        return lootManager;
    }

    @Override
    public void onDisable() {
        databaseManager.disconnect();
    }

    public static CustomDungeons getInstance() {
        return instance;
    }

    public DungeonManager getDungeonManager() {
        return dungeonManager;
    }
}
