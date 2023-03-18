package me.gadse.paperbag.listener;

import lombok.RequiredArgsConstructor;
import me.gadse.paperbag.Paperbag;
import me.gadse.paperbag.inventory.BackpackHolder;
import me.gadse.paperbag.inventory.IGUI;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

@RequiredArgsConstructor
public class InventoryDrag implements Listener {

    private final Paperbag plugin;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof IGUI igui)) {
            return;
        }

        igui.onDrag(event.getWhoClicked(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBackpackDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof BackpackHolder)) {
            return;
        }

        if (event.getOldCursor().getItemMeta() != null
                && event.getOldCursor().getItemMeta().getPersistentDataContainer()
                .has(plugin.getBackpackSizeKey(), PersistentDataType.BYTE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBackpackEquip(InventoryDragEvent event) {
        if (event.getInventory().getType() != InventoryType.CRAFTING) {
            return;
        }

        if (event.getRawSlots().contains(5)
                && event.getOldCursor().getItemMeta() != null
                && event.getOldCursor().getItemMeta().getPersistentDataContainer()
                .has(plugin.getBackpackSizeKey(), PersistentDataType.BYTE)) {
            event.setCancelled(true);
        }
    }
}
