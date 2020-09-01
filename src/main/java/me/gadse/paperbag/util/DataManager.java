package me.gadse.paperbag.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.gadse.paperbag.Paperbag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

@RequiredArgsConstructor
public class DataManager {

    private final Paperbag plugin;

    @Getter
    private final Set<UUID> openInventories = new HashSet<>();
    private final Map<Location, List<ItemStack>> deathChests = new HashMap<>();

    public void addDeathChest(Player player, List<ItemStack> itemStacks) {
        Location location = player.getLocation().clone();
        while (location.getBlock().getType() != Material.AIR)
            location.add(0, 1, 0);
        Block block = location.getBlock();
        block.setType(Material.CHEST);

        Chest chest = (Chest) block.getState();
        PersistentDataContainer container = chest.getPersistentDataContainer();
        container.set(plugin.getOwnerKey(),
                PersistentDataType.STRING,
                player.getUniqueId().toString());
        chest.update();

        deathChests.put(block.getLocation(), new ArrayList<>(itemStacks));
    }

    public void removeAllDeathChests() {
        deathChests.keySet().forEach(location -> location.getBlock().setType(Material.AIR));
        deathChests.clear();
    }

    public void removeDeathChest(Location location, boolean dropItems) {
        if (location == null || location.getWorld() == null)
            return;

        if (location.getBlock().getType() == Material.CHEST)
            location.getBlock().setType(Material.AIR);

        if (!dropItems) {
            deathChests.remove(location);
            return;
        }

        List<ItemStack> itemStacks = deathChests.get(location);
        if (itemStacks == null)
            return;

        itemStacks.forEach(itemStack ->
                location.getWorld().dropItem(location, itemStack));

        deathChests.remove(location);
    }
}
