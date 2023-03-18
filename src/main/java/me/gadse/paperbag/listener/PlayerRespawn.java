package me.gadse.paperbag.listener;

import me.gadse.paperbag.Paperbag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PlayerRespawn implements Listener {

    private final Paperbag plugin;

    public PlayerRespawn(Paperbag plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        List<ItemStack> respawnItems = plugin.getRespawnItems().get(event.getPlayer().getUniqueId());
        if (respawnItems != null) {
            event.getPlayer().getInventory().addItem(respawnItems.toArray(new ItemStack[0]))
                    .forEach((integer, itemStack) -> event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), itemStack));
            plugin.getRespawnItems().remove(event.getPlayer().getUniqueId());
        }
    }
}
