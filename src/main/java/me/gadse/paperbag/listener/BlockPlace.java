package me.gadse.paperbag.listener;

import lombok.RequiredArgsConstructor;
import me.gadse.paperbag.Paperbag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataType;

@RequiredArgsConstructor
public class BlockPlace implements Listener {

    private final Paperbag plugin;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getItemInHand().getItemMeta() == null
                || !event.getItemInHand().getItemMeta().getPersistentDataContainer()
                .has(plugin.getBackpackSizeKey(), PersistentDataType.BYTE)) {
            return;
        }
        event.setCancelled(true);
    }
}
