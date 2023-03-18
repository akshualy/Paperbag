package me.gadse.paperbag.listener;

import lombok.RequiredArgsConstructor;
import me.gadse.paperbag.Paperbag;
import me.gadse.paperbag.inventory.BackpackHolder;
import me.gadse.paperbag.util.Messages;
import me.gadse.paperbag.util.SerializerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@RequiredArgsConstructor
public class PlayerInteract implements Listener {

    private final Paperbag plugin;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND
                || (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK)
                || event.getItem() == null
                || event.getItem().getItemMeta() == null)
            return;
        Player player = event.getPlayer();

        PersistentDataContainer container = event.getItem().getItemMeta().getPersistentDataContainer();

        String owner = container.get(plugin.getOwnerKey(), PersistentDataType.STRING);
        if (owner == null)
            return;
        event.setCancelled(true);

        if (!player.getUniqueId().equals(UUID.fromString(owner)) && !(player.hasPermission("paperbag.admin"))) {
            Messages.BACKPACK_NOT_OWNER.sendMessage(player);
            return;
        }

        int rows = container.getOrDefault(plugin.getBackpackSizeKey(),
                PersistentDataType.BYTE,
                (byte) 0);

        if (rows == 0)
            return;

        BackpackClass backpack = new BackpackClass(
                rows * 9, plugin.getBackpackTitle().replaceAll("%player%", player.getName())
        );

        String content = container.getOrDefault(
                plugin.getBackpackContentKey(),
                PersistentDataType.STRING,
                ""
        );

        if (!content.isEmpty())
            backpack.getInventory().setContents(SerializerUtil.fromBase64(content));

        player.openInventory(backpack.getInventory());
    }

    private class BackpackClass implements BackpackHolder {
        private final Inventory inventory;

        private BackpackClass(int size, String title) {
            this.inventory = plugin.getServer().createInventory(this, size, title);
        }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }
    }
}
