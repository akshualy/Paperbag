package me.gadse.paperbag.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public interface IGUI extends InventoryHolder {
    void onClick(HumanEntity player, InventoryClickEvent event);
}
