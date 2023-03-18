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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

@RequiredArgsConstructor
public class InventoryClick implements Listener {

    private final Paperbag plugin;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof IGUI igui)) {
            return;
        }

        igui.onClick(event.getWhoClicked(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBackpackClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BackpackHolder))
            return;

        ItemStack currentItem = event.getClick() == ClickType.NUMBER_KEY
                ? event.getWhoClicked().getInventory().getItem(event.getHotbarButton())
                : event.getCurrentItem();

        if (currentItem == null
                || currentItem.getItemMeta() == null
                || !currentItem.getItemMeta().getPersistentDataContainer()
                .has(plugin.getBackpackSizeKey(), PersistentDataType.BYTE)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBackpackEquip(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.CRAFTING) return;

        ItemStack currentItem;
        if (event.getClick() == ClickType.NUMBER_KEY) {
            currentItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
        } else if (event.getClick() == ClickType.SWAP_OFFHAND) {
            currentItem = event.getWhoClicked().getInventory().getItem(EquipmentSlot.OFF_HAND);
        } else if (event.getRawSlot() == 5) {
            currentItem = event.getCursor();
        } else {
            currentItem = event.getCurrentItem();
        }

        if (currentItem == null
                || currentItem.getItemMeta() == null
                || !currentItem.getItemMeta().getPersistentDataContainer()
                .has(plugin.getBackpackSizeKey(), PersistentDataType.BYTE)) {
            return;
        }

        if (event.getRawSlot() == 5) {
            event.setCancelled(true);
        } else if (event.isShiftClick()) {
            ItemStack currentHelmet = event.getWhoClicked().getInventory().getHelmet();
            if (currentHelmet == null || currentHelmet.getType() == Material.AIR)
                event.setCancelled(true);
        }
    }
}
