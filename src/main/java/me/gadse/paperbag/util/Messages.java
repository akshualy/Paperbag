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
    NOT_ENOUGH_MATERIALS,
    PAID,
    BACKPACK_NOT_OWNER;

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
