package me.gadse.paperbag.util;

import me.gadse.paperbag.Paperbag;
import org.bukkit.command.CommandSender;

public enum Messages {
    PREFIX,
    ERROR,
    RELOAD,
    TARGET_OFFLINE,
    GIVE,
    RECEIVE,
    NOT_ENOUGH_MONEY,
    PAID,
    BACKPACK_NOT_OWNER,
    DEATH_CHEST_SPAWN,
    DEATH_CHEST_NOT_OWNER,
    DEATH_CHEST_COST;

    private String message = "ERROR LOADING MESSAGE FOR " + name();

    public void reloadValue(Paperbag plugin) {
        message = plugin.color(plugin.getConfig().getString("messages." + name().toLowerCase()));
    }

    public void sendMessage(CommandSender commandSender, Pair... placeholders) {
        String messageCopy = message;

        for (Pair placeholder : placeholders)
            messageCopy = messageCopy.replaceAll(placeholder.getKey(), placeholder.getValue());

        commandSender.sendMessage(PREFIX.message + messageCopy);
    }
}
