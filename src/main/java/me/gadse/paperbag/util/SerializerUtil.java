package me.gadse.paperbag.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SerializerUtil {

    public static String toBase64(ItemStack[] itemStacks) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            BukkitObjectOutputStream itemOutputStream = new BukkitObjectOutputStream(outputStream);
            itemOutputStream.writeInt(itemStacks.length);

            for (ItemStack itemStack : itemStacks)
                itemOutputStream.writeObject(itemStack);

            itemOutputStream.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static ItemStack[] fromBase64(String data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        try {
            BukkitObjectInputStream itemInputStream = new BukkitObjectInputStream(inputStream);
            ItemStack[] itemStacks = new ItemStack[itemInputStream.readInt()];

            for (int i = 0; i < itemStacks.length; i++) {
                try {
                    itemStacks[i] = (ItemStack) itemInputStream.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            itemInputStream.close();
            return itemStacks;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new ItemStack[0];
    }
}
