package me.gadse.paperbag.commands;

import lombok.RequiredArgsConstructor;
import me.gadse.paperbag.Paperbag;
import me.gadse.paperbag.util.Messages;
import me.gadse.paperbag.util.Pair;
import org.apache.commons.lang.time.StopWatch;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@RequiredArgsConstructor
public class BackpackCommand implements CommandExecutor {

    private final Paperbag plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String arg = args.length > 0 ? args[0].toLowerCase() : "version";
        switch (arg) {
            case "reload": {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                plugin.reloadConfig();
                plugin.loadData();
                stopWatch.stop();
                Messages.RELOAD.sendMessage(sender, new Pair("%time%", stopWatch.getTime()));
                break;
            }
            case "menu": {
                if (!(sender instanceof Player))
                    break;
                Player player = (Player) sender;

                player.openInventory(plugin.getGuiUpgrade().getInventory());
                break;
            }
            case "give": {
                if (args.length < 2)
                    return false;

                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    Messages.TARGET_OFFLINE.sendMessage(sender, new Pair("%target%", args[1]));
                    return false;
                }

                ItemStack backpack = plugin.getBackpackItems().get(1).clone();
                if (backpack.getItemMeta() == null) {
                    Messages.ERROR.sendMessage(sender, new Pair("%error%", "Backpack Meta null (AIR?)."));
                    return false;
                }

                ItemMeta itemMeta = backpack.getItemMeta();
                PersistentDataContainer container = itemMeta.getPersistentDataContainer();
                container.set(plugin.getOwnerKey(), PersistentDataType.STRING, target.getUniqueId().toString());
                backpack.setItemMeta(itemMeta);

                target.getInventory().addItem(backpack)
                        .forEach((slot, itemstack) -> target.getWorld().dropItem(target.getLocation(), itemstack));

                if (!target.equals(sender))
                    Messages.GIVE.sendMessage(sender, new Pair("%target%", target.getName()));
                Messages.RECEIVE.sendMessage(target);
                break;
            }
            default: {
                sender.sendMessage(plugin.getName() + " v" + plugin.getDescription().getVersion());
                break;
            }
        }

        return true;
    }
}
