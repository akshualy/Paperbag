package me.gadse.paperbag.commands;

import lombok.RequiredArgsConstructor;
import me.gadse.paperbag.Paperbag;
import me.gadse.paperbag.util.Messages;
import me.gadse.paperbag.util.Pair;
import org.apache.commons.lang3.time.StopWatch;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class BackpackCommand implements CommandExecutor {

    private final Paperbag plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String arg = args.length > 0 ? args[0].toLowerCase() : "upgrade";
        switch (arg) {
            case "reload" -> {
                if (!sender.hasPermission("paperbag.admin")) {
                    break;
                }

                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                plugin.reloadConfig();
                plugin.loadData();
                stopWatch.stop();
                Messages.RELOAD.sendMessage(sender, new Pair("%time%", stopWatch.getTime()));
            }
            case "give" -> {
                if (!sender.hasPermission("paperbag.admin")) {
                    break;
                }

                if (args.length < 2) {
                    return false;
                }

                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    Messages.TARGET_OFFLINE.sendMessage(sender, new Pair("%target%", args[1]));
                    return false;
                }

                int amount = 1;
                if (args.length == 3) {
                    try {
                        amount = Integer.parseInt(args[2]);
                    } catch (NumberFormatException ignored) {
                        Messages.ERROR.sendMessage(sender, new Pair("%error%", args[2] + " is not a number."));
                        return false;
                    }
                }

                for (int i = 0; i < amount; i++) {
                    ItemStack backpack = plugin.getBackPackBySizeForPlayer(1, target);
                    if (backpack == null || backpack.getItemMeta() == null) {
                        Messages.ERROR.sendMessage(sender, new Pair("%error%", "Backpack Meta null (AIR?)."));
                        return false;
                    }
                    target.getInventory().addItem(backpack).forEach(
                            (slot, itemStack) -> target.getWorld().dropItem(target.getLocation(), itemStack)
                    );
                }

                if (!target.equals(sender)) {
                    Messages.GIVE.sendMessage(sender, new Pair("%target%", target.getName()));
                }
                Messages.RECEIVE.sendMessage(target);
            }
            default -> {
                if (!(sender instanceof Player player)) {
                    break;
                }

                player.openInventory(plugin.getGuiUpgrade().getInventory());
            }
        }

        return true;
    }
}
