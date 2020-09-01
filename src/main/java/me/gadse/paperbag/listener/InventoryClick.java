package me.gadse.paperbag.listener;

import lombok.RequiredArgsConstructor;
import me.gadse.paperbag.Paperbag;
import me.gadse.paperbag.inventory.IGUI;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

@RequiredArgsConstructor
public class InventoryClick implements Listener {

    private final Paperbag plugin;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof IGUI))
            return;

        IGUI igui = (IGUI) event.getInventory().getHolder();
        igui.onClick(event.getWhoClicked(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBackpackClick(InventoryClickEvent event) {
        if (!plugin.getDataManager().getOpenInventories().contains(event.getWhoClicked().getUniqueId()))
            return;

        ItemStack currentItem = event.getClick() == ClickType.NUMBER_KEY
                ? event.getWhoClicked().getInventory().getItem(event.getHotbarButton())
                : event.getCurrentItem();

        if (currentItem == null
                || currentItem.getItemMeta() == null
                || !currentItem.getItemMeta().getPersistentDataContainer()
                .has(plugin.getBackpackSizeKey(), PersistentDataType.BYTE))
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBackpackEquip(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.CRAFTING) return;

        ItemStack currentItem = event.getClick() == ClickType.NUMBER_KEY
                ? event.getWhoClicked().getInventory().getItem(event.getHotbarButton())
                : event.getRawSlot() == 5
                ? event.getCursor()
                : event.getCurrentItem();

        if (currentItem == null
                || currentItem.getItemMeta() == null
                || !currentItem.getItemMeta().getPersistentDataContainer()
                .has(plugin.getBackpackSizeKey(), PersistentDataType.BYTE))
            return;

        if (event.getRawSlot() == 5) {
            event.setCancelled(true);
        } else if (event.isShiftClick()) {
            ItemStack currentHelmet = event.getWhoClicked().getInventory().getHelmet();
            if (currentHelmet == null || currentHelmet.getType() == Material.AIR)
                event.setCancelled(true);
        }
    }
}
