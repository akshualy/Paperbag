package me.gadse.paperbag.inventory;

import lombok.Getter;
import lombok.NonNull;
import me.gadse.paperbag.Paperbag;
import me.gadse.paperbag.util.Messages;
import me.gadse.paperbag.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GUIUpgrade implements IGUI {

    private final Paperbag plugin;
    private final Inventory inventory;
    private final ItemStack upgradeItem, upgradeInvalidItem, upgradeMaxItem;
    private final int upgradeSlot, closeSlot;
    @Getter
    private final int backpackSlot, costSlot;

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
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, fill);
        }

        inventory.setItem(guiSection.getInt("info.slot", 0), plugin.getItemStackFromConfig("gui.info"));
        inventory.setItem(guiSection.getInt("chest.slot", 25), plugin.getItemStackFromConfig("gui.chest"));

        closeSlot = guiSection.getInt("close.slot", 8);
        inventory.setItem(closeSlot, plugin.getItemStackFromConfig("gui.close"));

        backpackSlot = guiSection.getInt("backpack.slot", 40);
        inventory.setItem(backpackSlot, new ItemStack(Material.AIR));

        costSlot = guiSection.getInt("cost.slot", 19);
        inventory.setItem(costSlot, new ItemStack(Material.AIR));

        upgradeSlot = guiSection.getInt("upgrade.slot", 4);
        upgradeItem = plugin.getItemStackFromConfig("gui.upgrade");
        upgradeInvalidItem = plugin.getItemStackFromConfig("gui.upgrade_invalid");
        upgradeMaxItem = plugin.getItemStackFromConfig("gui.upgrade_max");
        inventory.setItem(upgradeSlot, upgradeInvalidItem);

        ConfigurationSection otherFillerSection = guiSection.getConfigurationSection("other_fillers");
        if (otherFillerSection == null) {
            return;
        }

        otherFillerSection.getKeys(false).forEach(fillKey -> {
            ItemStack otherFill = plugin.getItemStackFromConfig("gui.other_fillers." + fillKey);
            otherFillerSection.getIntegerList(fillKey + ".slots").forEach(slot -> inventory.setItem(slot, otherFill));
        });
    }

    @Override
    public void onDrag(HumanEntity player, InventoryDragEvent event) {
        boolean bottomDrag = true;
        for (Integer rawSlot : event.getRawSlots()) {
            if (rawSlot < event.getView().getTopInventory().getSize()) {
                bottomDrag = false;
                break;
            }
        }

        if (bottomDrag) {
            return;
        }

        if (event.getRawSlots().size() > 1) {
            event.setCancelled(true);
            return;
        }

        int slot = event.getRawSlots().toArray(new Integer[1])[0];
        if (slot == costSlot) {
            return;
        }

        if (slot == backpackSlot) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> updateUpgradeItem(event.getWhoClicked()), 1L);
            return;
        }

        event.setCancelled(true);
    }

    @Override
    public void onClick(HumanEntity humanEntity, InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }

        if (event.getView().getBottomInventory().equals(event.getClickedInventory()) && !event.isShiftClick()) {
            return;
        }

        int rawSlot = event.getRawSlot();

        Player player = (Player) humanEntity;
        if (event.isShiftClick() || rawSlot == backpackSlot) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> updateUpgradeItem(player), 1L);
            return;
        }

        if (rawSlot == costSlot) {
            return;
        }

        if (rawSlot == closeSlot) {
            event.setCancelled(true);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> humanEntity.closeInventory(), 1L);
            return;
        }

        if (rawSlot == upgradeSlot) {
            event.setCancelled(true);
            ItemStack itemStack = event.getClickedInventory().getItem(backpackSlot);
            if (itemStack == null || itemStack.getItemMeta() == null) {
                return;
            }

            PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();

            byte rows = container.getOrDefault(plugin.getBackpackSizeKey(), PersistentDataType.BYTE, (byte) 0);
            ItemStack upgradeCost = plugin.getBackpackUpgradeCost().get((int) rows);
            if (rows == 0 || rows == 6 || upgradeCost == null) {
                return;
            }

            String ownerUID = container.get(plugin.getOwnerKey(), PersistentDataType.STRING);
            if (ownerUID == null || !UUID.fromString(ownerUID).equals(player.getUniqueId())) {
                return;
            }

            ItemStack newItem = plugin.getBackPackBySizeForPlayer(rows + 1, player);
            if (newItem == null) {
                return;
            }

            ItemStack newBackpack = newItem.clone();
            ItemMeta itemMeta = newBackpack.getItemMeta();
            if (itemMeta == null) {
                return;
            }


            ItemStack costItemStack = event.getView().getTopInventory().getItem(costSlot);
            if (costItemStack == null
                    || !costItemStack.isSimilar(upgradeCost)
                    || costItemStack.getAmount() < upgradeCost.getAmount()) {
                Messages.NOT_ENOUGH_MATERIALS.sendMessage(player);
                return;
            }

            costItemStack.setAmount(costItemStack.getAmount() - upgradeCost.getAmount());

            String costName = upgradeCost.getAmount() + "x "
                    + upgradeCost.getType().toString().toLowerCase().replaceAll("_", " ");
            Messages.PAID.sendMessage(player, new Pair("%cost%", costName));

            String content = container.getOrDefault(plugin.getBackpackContentKey(), PersistentDataType.STRING, "");
            itemMeta.getPersistentDataContainer().set(plugin.getBackpackContentKey(), PersistentDataType.STRING, content);

            String owner = container.getOrDefault(plugin.getOwnerKey(), PersistentDataType.STRING, player.getUniqueId().toString());
            itemMeta.getPersistentDataContainer().set(plugin.getOwnerKey(), PersistentDataType.STRING, owner);

            newBackpack.setItemMeta(itemMeta);

            event.getClickedInventory().setItem(backpackSlot, newBackpack);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> updateUpgradeItem(player), 1L);
            return;
        }

        event.setCancelled(true);
    }

    private void updateUpgradeItem(HumanEntity player) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
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

        String ownerUID = container.get(plugin.getOwnerKey(), PersistentDataType.STRING);
        if (ownerUID == null || !UUID.fromString(ownerUID).equals(player.getUniqueId())) {
            inventory.setItem(upgradeSlot, upgradeInvalidItem);
            return;
        }

        ItemStack upgradeCost = plugin.getBackpackUpgradeCost().get((int) rows);
        ItemStack newItem = plugin.getBackPackBySizeForPlayer(rows + 1, player);
        if (rows == 6 || upgradeCost == null || newItem == null) {
            inventory.setItem(upgradeSlot, upgradeMaxItem);
            return;
        }

        ItemStack upgradeClone = upgradeItem.clone();
        ItemMeta upgradeMeta = upgradeClone.getItemMeta();
        if (upgradeMeta == null || upgradeMeta.getLore() == null) {
            return;
        }

        String costName = upgradeCost.getAmount() + "x "
                + upgradeCost.getType().toString().toLowerCase().replaceAll("_", " ");

        List<String> lore = new ArrayList<>();
        upgradeMeta.getLore().forEach(line ->
                lore.add(
                        line.replaceAll("%cost%", costName).replaceAll("%size%", String.valueOf((rows + 1) * 9))
                )
        );
        upgradeMeta.setLore(lore);
        upgradeClone.setItemMeta(upgradeMeta);
        inventory.setItem(upgradeSlot, upgradeClone);
    }

    @Override
    @NonNull
    public Inventory getInventory() {
        Inventory clone = Bukkit.createInventory(
                this,
                inventory.getSize(),
                plugin.color(plugin.getConfig().getString("gui.title"))
        );
        clone.setContents(inventory.getContents());
        return clone;
    }
}
