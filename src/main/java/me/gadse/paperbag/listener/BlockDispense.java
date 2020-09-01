package me.gadse.paperbag.listener;

import lombok.RequiredArgsConstructor;
import me.gadse.paperbag.Paperbag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@RequiredArgsConstructor
public class BlockDispense implements Listener {

    private final Paperbag plugin;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        ItemMeta itemMeta = event.getItem().getItemMeta();
        if (itemMeta == null)
            return;
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if (container.has(plugin.getBackpackSizeKey(), PersistentDataType.BYTE))
            event.setCancelled(true);
    }
}
