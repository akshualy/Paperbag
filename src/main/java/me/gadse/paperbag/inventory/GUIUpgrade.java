package me.gadse.paperbag.inventory;

import lombok.Getter;
import me.gadse.paperbag.Paperbag;
import me.gadse.paperbag.util.Messages;
import me.gadse.paperbag.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class GUIUpgrade implements IGUI {

    private final Paperbag plugin;
    private final Inventory inventory;
    private final ItemStack upgradeItem, upgradeInvalidItem, upgradeMaxItem;
    private final int upgradeSlot, closeSlot;
    @Getter
    private final int backpackSlot;

    public GUIUpgrade(Paperbag plugin) {
        this.plugin = plugin;
        ConfigurationSection guiSection = plugin.getConfig().getConfigurationSection("gui");
        if (guiSection == null)
            throw new IllegalArgumentException("The GUI section in the config does not exist.");

        this.inventory = Bukkit.createInventory(this,
                guiSection.getInt("size", 54),
                plugin.color(guiSection.getString("title"))
        );

        ItemStack fill = plugin.getItemStackFromConfig("gui.fill");
        for (int i = 0; i < inventory.getSize(); i++)
            inventory.setItem(i, fill);

        inventory.setItem(guiSection.getInt("info.slot", 0), plugin.getItemStackFromConfig("gui.info"));
        inventory.setItem(guiSection.getInt("magic.slot", 19), plugin.getItemStackFromConfig("gui.magic"));
        inventory.setItem(guiSection.getInt("chest.slot", 25), plugin.getItemStackFromConfig("gui.chest"));

        closeSlot = guiSection.getInt("close.slot", 8);
        inventory.setItem(closeSlot, plugin.getItemStackFromConfig("gui.close"));

        backpackSlot = guiSection.getInt("backpack.slot", 40);
        inventory.setItem(backpackSlot, new ItemStack(Material.AIR));

        upgradeSlot = guiSection.getInt("upgrade.slot", 4);
        upgradeItem = plugin.getItemStackFromConfig("gui.upgrade");
        upgradeInvalidItem = plugin.getItemStackFromConfig("gui.upgrade_invalid");
        upgradeMaxItem = plugin.getItemStackFromConfig("gui.upgrade_max");
        inventory.setItem(upgradeSlot, upgradeInvalidItem);

        ConfigurationSection otherFillerSection = guiSection.getConfigurationSection("other_fillers");
        if (otherFillerSection == null)
            return;

        otherFillerSection.getKeys(false).forEach(fillKey -> {
            ItemStack otherFill = plugin.getItemStackFromConfig("gui.other_fillers." + fillKey);
            otherFillerSection.getIntegerList(fillKey + ".slots").forEach(slot -> inventory.setItem(slot, otherFill));
        });
    }

    @Override
    public void onClick(HumanEntity humanEntity, InventoryClickEvent event) {
        if (event.getClickedInventory() == null)
            return;

        if (event.getView().getBottomInventory().equals(event.getClickedInventory()) && !event.isShiftClick())
            return;

        int rawSlot = event.getRawSlot();

        Player player = (Player) humanEntity;
        if (event.isShiftClick() || rawSlot == backpackSlot) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                    updateUpgradeItem(player.getOpenInventory().getTopInventory()), 1L);
            return;
        }

        if (rawSlot == closeSlot) {
            event.setCancelled(true);
            plugin.getServer().getScheduler().runTaskLater(plugin, humanEntity::closeInventory, 1L);
            return;
        }

        if (rawSlot == upgradeSlot) {
            event.setCancelled(true);
            ItemStack itemStack = event.getClickedInventory().getItem(backpackSlot);
            if (itemStack == null || itemStack.getItemMeta() == null)
                return;

            PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();

            byte rows = container.getOrDefault(plugin.getBackpackSizeKey(), PersistentDataType.BYTE, (byte) 0);
            int cost = container.getOrDefault(plugin.getBackpackCostKey(), PersistentDataType.INTEGER, -1);
            if (rows == 0 || rows == 6 || cost == -1)
                return;

            ItemStack newItem = plugin.getBackpackItems().get(rows + 1);
            if (newItem == null)
                return;

            ItemStack newBackpack = newItem.clone();
            ItemMeta itemMeta = newBackpack.getItemMeta();
            if (itemMeta == null)
                return;

            if (plugin.getEconomy() != null) {
                if (!plugin.getEconomy().has(player, cost)) {
                    Messages.NOT_ENOUGH_MONEY.sendMessage(player);
                    return;
                }

                plugin.getEconomy().withdrawPlayer(player, cost);
                Messages.PAID.sendMessage(player, new Pair("%cost%", cost));
            }

            String content = container.getOrDefault(plugin.getBackpackContentKey(), PersistentDataType.STRING, "");
            itemMeta.getPersistentDataContainer().set(plugin.getBackpackContentKey(), PersistentDataType.STRING, content);

            String owner = container.getOrDefault(plugin.getOwnerKey(), PersistentDataType.STRING, player.getUniqueId().toString());
            itemMeta.getPersistentDataContainer().set(plugin.getOwnerKey(), PersistentDataType.STRING, owner);

            newBackpack.setItemMeta(itemMeta);

            event.getClickedInventory().setItem(backpackSlot, newBackpack);
            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                    updateUpgradeItem(player.getOpenInventory().getTopInventory()), 1L);
            return;
        }

        event.setCancelled(true);
    }

    private void updateUpgradeItem(Inventory inventory) {
        ItemStack itemStack = inventory.getItem(backpackSlot);
        if (itemStack == null || itemStack.getItemMeta() == null) {
            inventory.setItem(upgradeSlot, upgradeInvalidItem);
            return;
        }

        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();

        byte rows = container.getOrDefault(plugin.getBackpackSizeKey(), PersistentDataType.BYTE, (byte) 0);
        if (rows == 0) {
            inventory.setItem(upgradeSlot, upgradeInvalidItem);
            return;
        }

        int cost = container.getOrDefault(plugin.getBackpackCostKey(), PersistentDataType.INTEGER, -1);
        ItemStack newItem = plugin.getBackpackItems().get(rows + 1);
        if (rows == 6 || cost == -1 || newItem == null) {
            inventory.setItem(upgradeSlot, upgradeMaxItem);
            return;
        }

        ItemStack upgradeClone = upgradeItem.clone();
        ItemMeta upgradeMeta = upgradeClone.getItemMeta();
        if (upgradeMeta == null || upgradeMeta.getLore() == null)
            return;

        List<String> lore = new ArrayList<>();
        upgradeMeta.getLore().forEach(line ->
                lore.add(line
                        .replaceAll("%cost%", String.valueOf(cost))
                        .replaceAll("%size%", String.valueOf((rows + 1) * 9))
                )
        );
        upgradeMeta.setLore(lore);
        upgradeClone.setItemMeta(upgradeMeta);
        inventory.setItem(upgradeSlot, upgradeClone);
    }

    @Override
    public Inventory getInventory() {
        Inventory clone = Bukkit.createInventory(this,
                inventory.getSize(),
                plugin.color(plugin.getConfig().getString("gui.title"))
        );
        clone.setContents(inventory.getContents());
        return clone;
    }
}
