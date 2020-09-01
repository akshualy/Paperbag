package me.gadse.paperbag.listener;

import lombok.RequiredArgsConstructor;
import me.gadse.paperbag.Paperbag;
import me.gadse.paperbag.util.Messages;
import me.gadse.paperbag.util.Pair;
import me.gadse.paperbag.util.SerializerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Set;
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

        if (!player.getUniqueId().equals(UUID.fromString(owner))) {
            Messages.BACKPACK_NOT_OWNER.sendMessage(player);
            return;
        }

        int rows = container.getOrDefault(plugin.getBackpackSizeKey(),
                PersistentDataType.BYTE,
                (byte) 0);

        if (rows == 0)
            return;

        Inventory inventory = Bukkit.createInventory(null, rows * 9,
                plugin.getBackpackTitle().replaceAll("%player%", player.getName()));

        String content = container.getOrDefault(plugin.getBackpackContentKey(),
                PersistentDataType.STRING,
                "");

        if (!content.isEmpty())
            inventory.setContents(SerializerUtil.fromBase64(content));

        plugin.getDataManager().getOpenInventories().add(player.getUniqueId());
        player.openInventory(inventory);
    }

    private final Set<UUID> confirmationSet = new HashSet<>();

    @EventHandler(priority = EventPriority.LOW)
    public void onDeathChestInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND
                || event.getClickedBlock() == null
                || event.getClickedBlock().getType() != Material.CHEST)
            return;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        Chest chest = (Chest) block.getState();
        PersistentDataContainer container = chest.getPersistentDataContainer();
        String storedUID = container.get(plugin.getOwnerKey(), PersistentDataType.STRING);
        if (storedUID == null)
            return;
        event.setCancelled(true);

        if (!storedUID.equals(player.getUniqueId().toString()))
            return;

        int deathChestCost = plugin.getConfig().getInt("death_chest.cost");
        if (!confirmationSet.contains(player.getUniqueId())) {
            confirmationSet.add(player.getUniqueId());
            Messages.DEATH_CHEST_COST.sendMessage(player,
                    new Pair("%cost%", deathChestCost)
            );
            return;
        }
        confirmationSet.remove(player.getUniqueId());

        if (plugin.getEconomy() != null) {
            if (!plugin.getEconomy().has(player, deathChestCost)) {
                Messages.NOT_ENOUGH_MONEY.sendMessage(player);
                return;
            }
            plugin.getEconomy().withdrawPlayer(player, deathChestCost);
        }

        plugin.getDataManager().removeDeathChest(block.getLocation(), true);
    }
}
