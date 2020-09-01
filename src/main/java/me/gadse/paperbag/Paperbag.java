package me.gadse.paperbag;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import me.gadse.paperbag.commands.BackpackCommand;
import me.gadse.paperbag.inventory.GUIUpgrade;
import me.gadse.paperbag.listener.*;
import me.gadse.paperbag.util.DataManager;
import me.gadse.paperbag.util.Messages;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public final class Paperbag extends JavaPlugin {

    @Getter
    private DataManager dataManager;
    @Getter
    private GUIUpgrade guiUpgrade;

    @Getter
    private Economy economy;

    @Getter
    private final Map<Integer, ItemStack> backpackItems = new HashMap<>();
    @Getter
    private NamespacedKey backpackSizeKey, backpackContentKey, backpackCostKey, ownerKey;
    @Getter
    private String backpackTitle;

    private Method setProfileMethod;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs())
            return;
        saveDefaultConfig();

        backpackSizeKey = new NamespacedKey(this, "backpack-size");
        backpackContentKey = new NamespacedKey(this, "backpack-content");
        backpackCostKey = new NamespacedKey(this, "backpack-cost");
        ownerKey = new NamespacedKey(this, "owner");

        PluginManager pluginManager = getServer().getPluginManager();
        if (pluginManager.getPlugin("Vault") == null) {
            getLogger().severe("Vault not installed. Upgrading backpacks will not cost anything.");
        } else {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager()
                    .getRegistration(Economy.class);
            if (rsp == null)
                getLogger().severe("Vault installed without economy plugin. " +
                        "Upgrading backpacks will not cost anything.");
            else
                economy = rsp.getProvider();
        }

        loadData();

        pluginManager.registerEvents(new BlockBreak(this), this);
        pluginManager.registerEvents(new BlockDispense(this), this);
        pluginManager.registerEvents(new BlockPlace(this), this);
        pluginManager.registerEvents(new InventoryClick(this), this);
        pluginManager.registerEvents(new InventoryClose(this), this);
        pluginManager.registerEvents(new PlayerDeath(this), this);
        pluginManager.registerEvents(new PlayerInteract(this), this);

        PluginCommand backpackCommand = getCommand("backpack");
        if (backpackCommand != null)
            backpackCommand.setExecutor(new BackpackCommand(this));
    }

    public void loadData() {
        backpackItems.clear();

        for (Messages message : Messages.values())
            message.reloadValue(this);

        dataManager = new DataManager(this);
        guiUpgrade = new GUIUpgrade(this);

        backpackTitle = color(getConfig().getString("backpack-title"));

        ConfigurationSection backpackSection = getConfig().getConfigurationSection("backpacks");
        if (backpackSection == null)
            return;

        backpackSection.getKeys(false).forEach(backpackKey -> {
            ItemStack itemStack = getItemStackFromConfig("backpacks." + backpackKey);
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null)
                return;

            int rows = 1;
            try {
                rows = Integer.parseInt(backpackKey);
            } catch (NumberFormatException ignored) {
            }

            itemMeta.getPersistentDataContainer().set(backpackSizeKey,
                    PersistentDataType.BYTE,
                    (byte) rows
            );

            itemMeta.getPersistentDataContainer().set(backpackCostKey,
                    PersistentDataType.INTEGER,
                    backpackSection.getInt(backpackKey + ".cost", -1)
            );

            itemStack.setItemMeta(itemMeta);
            backpackItems.putIfAbsent(rows, itemStack);
        });
    }

    public ItemStack getItemStackFromConfig(String configPath) {
        ConfigurationSection itemSection = getConfig().getConfigurationSection(configPath);
        if (itemSection == null) {
            getLogger().warning(configPath + " was not a configuration section. Returning AIR.");
            return new ItemStack(Material.AIR);
        }

        String materialName = itemSection.getString("material");
        if (materialName == null || materialName.isEmpty()) {
            getLogger().warning(configPath + ".material was empty or did not exist. " +
                    "Falling back to DIRT.");
            materialName = "DIRT";
        }

        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) {
            getLogger().warning(configPath + ".material -> " + materialName + " does not exist. " +
                    "Falling back to DIRT.");
            material = Material.DIRT;
        }
        ItemStack itemStack = new ItemStack(material);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return itemStack;

        itemMeta.setDisplayName(color(itemSection.getString("displayName")));

        List<String> lore = new ArrayList<>();
        itemSection.getStringList("lore").forEach(line -> lore.add(color(line)));
        itemMeta.setLore(lore);

        if (itemMeta instanceof SkullMeta)
            setProfileToMeta((SkullMeta) itemMeta, itemSection.getString("texture"));

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public void setProfileToMeta(SkullMeta skullMeta, String texture) {
        if (texture == null || texture.isEmpty())
            return;
        try {
            // Make setProfile accessible through reflection
            if (setProfileMethod == null) {
                setProfileMethod = skullMeta.getClass()
                        .getDeclaredMethod("setProfile", GameProfile.class);
                setProfileMethod.setAccessible(true);
            }

            // Create game profile based off texture
            UUID uuid = new UUID(
                    texture.substring(texture.length() - 20).hashCode(),
                    texture.substring(texture.length() - 10).hashCode()
            );
            GameProfile gameProfile = new GameProfile(uuid, texture.substring(88, 100));
            gameProfile.getProperties().put("textures", new Property("textures", texture));

            // Set texture to meta
            setProfileMethod.invoke(skullMeta, gameProfile);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    public String color(String text) {
        if (text == null || text.isEmpty())
            return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @Override
    public void onDisable() {
        backpackItems.clear();
        dataManager.removeAllDeathChests();
    }
}
