package me.gadse.paperbag.listener;

import lombok.RequiredArgsConstructor;
import me.gadse.paperbag.Paperbag;
import me.gadse.paperbag.inventory.IGUI;
import me.gadse.paperbag.util.SerializerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@RequiredArgsConstructor
public class InventoryClose implements Listener {

    private final Paperbag plugin;

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player))
            return;
        Player player = (Player) event.getPlayer();

        if (event.getInventory().getHolder() instanceof IGUI) {
            ItemStack itemStack = event.getInventory()
                    .getItem(plugin.getGuiUpgrade().getBackpackSlot());
            if (itemStack == null)
                return;

            player.getInventory().addItem(itemStack).forEach((slot, item) ->
                    player.getWorld().dropItem(player.getLocation(), item));
            return;
        }

        if (!plugin.getDataManager().getOpenInventories().contains(player.getUniqueId()))
            return;

        ItemStack backpack = player.getInventory().getItemInMainHand();
        if (backpack.getItemMeta() == null)
            return;
        ItemMeta itemMeta = backpack.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(plugin.getBackpackContentKey(),
                PersistentDataType.STRING,
                SerializerUtil.toBase64(event.getView().getTopInventory().getContents())
        );
        backpack.setItemMeta(itemMeta);
        plugin.getDataManager().getOpenInventories().remove(player.getUniqueId());
    }

}
