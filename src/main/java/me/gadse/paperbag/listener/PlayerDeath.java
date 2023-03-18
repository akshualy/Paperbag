package me.gadse.paperbag.listener;

import me.gadse.paperbag.Paperbag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class PlayerDeath implements Listener {

    private final Paperbag plugin;

    public PlayerDeath(Paperbag plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        List<ItemStack> respawnItems = event.getDrops().stream()
                .filter(itemStack -> itemStack.getItemMeta() != null
                                && itemStack.getItemMeta().getPersistentDataContainer().has(
                                plugin.getBackpackSizeKey(), PersistentDataType.BYTE
                        )
                ).toList();
        if (respawnItems.size() > 0)
            plugin.getRespawnItems().put(event.getEntity().getUniqueId(), respawnItems);
        event.getDrops().removeAll(respawnItems);
    }
}
