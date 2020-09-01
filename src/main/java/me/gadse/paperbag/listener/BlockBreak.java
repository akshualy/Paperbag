package me.gadse.paperbag.listener;

import lombok.RequiredArgsConstructor;
import me.gadse.paperbag.Paperbag;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.persistence.PersistentDataType;

@RequiredArgsConstructor
public class BlockBreak implements Listener {

    private final Paperbag plugin;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.CHEST)
            return;

        if (((Chest) event.getBlock().getState()).getPersistentDataContainer()
                .has(plugin.getOwnerKey(), PersistentDataType.STRING))
            event.setCancelled(true);
    }

}
