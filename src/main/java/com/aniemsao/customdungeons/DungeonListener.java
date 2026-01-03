package com.aniemsao.customdungeons;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class DungeonListener implements Listener {
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.isNewChunk()) {
            CustomDungeons.getInstance().getDungeonManager().handleChunkLoad(
                event.getWorld(), 
                event.getChunk().getX(), 
                event.getChunk().getZ()
            );
        }
    }
}
