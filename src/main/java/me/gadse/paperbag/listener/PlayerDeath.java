package me.gadse.paperbag.listener;

import lombok.RequiredArgsConstructor;
import me.gadse.paperbag.Paperbag;
import me.gadse.paperbag.util.Messages;
import me.gadse.paperbag.util.Pair;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

@RequiredArgsConstructor
public class PlayerDeath implements Listener {

    private final Paperbag plugin;

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        plugin.getDataManager().addDeathChest(player, event.getDrops());
        event.getDrops().clear();

        Location location = player.getLocation();
        String world = location.getWorld() == null ? "N/A" : location.getWorld().getName();
        Messages.DEATH_CHEST_SPAWN.sendMessage(player,
                new Pair("%world%", world),
                new Pair("%X%", location.getBlockX()),
                new Pair("%Y%", location.getBlockY()),
                new Pair("%Z%", location.getBlockZ())
        );

        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                        plugin.getDataManager().removeDeathChest(location, false),
                1200 * plugin.getConfig().getInt("death_chest.despawn")
        );
    }
}
