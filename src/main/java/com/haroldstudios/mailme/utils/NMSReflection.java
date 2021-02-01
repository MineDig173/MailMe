/*
 *   Copyright [2020] [Harry0198]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.haroldstudios.mailme.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*

    Majority of code from https://github.com/upperlevel/book-api/blob/master/src/main/java/xyz/upperlevel/spigot/book/NmsBookHelper.java

    WIP Saving Book Clickable components through serialization
 */
public final class NMSReflection {

    /*
     * The server version string to location NMS and OBC classes
     */
    private static String versionString;

    private static final boolean doubleHands;

    private static final Class<?> craftMetaBookClass;
    private static final Field craftMetaBookField;
    // Converts JSON string to IChatBaseComponent
    private static final Method chatSerializerA;

    // Only present in versions >= 1.16.4 (otherwise null)
    private static final Method craftMetaBookInternalAddPageMethod;

    private static final Method craftPlayerGetHandle;

    private static final Method nmsItemStackSave;
    private static final Constructor<?> nbtTagCompoundConstructor;

    private static final Method craftItemStackAsNMSCopy;

    static {
        versionString = getVersion();
        final int major, minor;
        Pattern pattern = Pattern.compile("v([0-9]+)_([0-9]+)");
        Matcher m = pattern.matcher(versionString);
        if (m.find()) {
            major = Integer.parseInt(m.group(1));
            minor = Integer.parseInt(m.group(2));
        } else {
            throw new IllegalStateException(
                    "Cannot parse version \"" + versionString + "\", make sure it follows \"v<major>_<minor>...\"");
        }
        doubleHands = major <= 1 && minor >= 9;
        try {
            craftMetaBookClass = getCraftClass("inventory.CraftMetaBook");

            craftMetaBookField = craftMetaBookClass.getDeclaredField("pages");
            craftMetaBookField.setAccessible(true);

            Method cmbInternalAddMethod = null;
            try {
                //method is protected
                cmbInternalAddMethod = craftMetaBookClass.getDeclaredMethod("internalAddPage", String.class);
                cmbInternalAddMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                // Internal data change in 1.16.4
                // To detect if the server is using the new internal format we check if the internalAddPageMethod exists
                // see https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/commits/560b65c4f8a15619aaa4a1737c7040f21e725cce
            }
            craftMetaBookInternalAddPageMethod = cmbInternalAddMethod;

            Class<?> chatSerializer = getNmsClass("IChatBaseComponent$ChatSerializer", false);
            if (chatSerializer == null) {
                chatSerializer = getNmsClass("ChatSerializer");
            }

            // On versions < 1.16.4 the CraftMetaBook accepted IChatBaseComponent
            // This method converts JSON strings to its IChatBaseComponent equivalent
            chatSerializerA = chatSerializer.getDeclaredMethod("a", String.class);

            final Class<?> craftPlayerClass = getCraftClass("entity.CraftPlayer");
            craftPlayerGetHandle = craftPlayerClass.getMethod("getHandle");

            final Class<?> craftItemStackClass = getCraftClass("inventory.CraftItemStack");
            craftItemStackAsNMSCopy = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Class<?> nmsItemStackClazz = getNmsClass("ItemStack");
            Class<?> nbtTagCompoundClazz = getNmsClass("NBTTagCompound");
            nmsItemStackSave = nmsItemStackClazz.getMethod("save", nbtTagCompoundClazz);
            nbtTagCompoundConstructor = nbtTagCompoundClazz.getConstructor();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot initiate reflections for " + versionString, e);
        }
    }

    /**
     * Gets the version string for NMS and OBC class paths
     *
     * @return The version string of OBC and NMS packages
     */
    public static String getVersion() {
        if (versionString == null) {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            versionString = name.substring(name.lastIndexOf('.') + 1) + ".";
        }

        return versionString;
    }

    /**
     * Sets the pages of the book to the components json equivalent
     *
     * @param meta       the book meta to change
     * @param components the pages of the book
     */
    @SuppressWarnings("unchecked") // reflections = unchecked warnings
    public static void setPages(BookMeta meta, BaseComponent[][] components) {
        try {
            List<Object> pages = (List<Object>) craftMetaBookField.get(meta);
            if (pages != null) {
                pages.clear();
            }
            for (BaseComponent[] c : components) {
                final String json;
                if (craftMetaBookInternalAddPageMethod != null) {
                    json = c != null ? ComponentSerializer.toString(c) : "";
                    craftMetaBookInternalAddPageMethod.invoke(meta, json);
                } else {
                    BaseComponent[] nonNullC = c != null ? c : jsonToComponents("");
                    json = ComponentSerializer.toString(nonNullC);
                    pages.add(chatSerializerA.invoke(null, json));
                }
            }
        } catch (Exception e) {
            throw new UnsupportedVersionException(e);
        }
    }

    /**
     * Translates an ItemStack to his Chat-Component equivalent
     *
     * @param item the item to be converted
     * @return a Chat-Component equivalent of the parameter
     */
    public static BaseComponent[] itemToComponents(ItemStack item) {
        return jsonToComponents(itemToJson(item));
    }

    /**
     * Translates a json string to his Chat-Component equivalent
     *
     * @param json the json string to be converted
     * @return a Chat-Component equivalent of the parameter
     */
    public static BaseComponent[] jsonToComponents(String json) {
        return new BaseComponent[] { new TextComponent(json) };
    }

    /**
     * Translates an ItemStack to his json equivalent
     *
     * @param item the item to be converted
     * @return a json equivalent of the parameter
     */
    private static String itemToJson(ItemStack item) {
        try {
            // net.minecraft.server.ItemStack nmsItemStack =
            // CraftItemStack.asNMSCopy(itemStack);
            Object nmsItemStack = nmsCopy(item);

            // net.minecraft.server.NBTTagCompound compound = new NBTTagCompound();
            // compound = nmsItemStack.save(compound);
            Object emptyTag = nbtTagCompoundConstructor.newInstance();
            Object json = nmsItemStackSave.invoke(nmsItemStack, emptyTag);
            return json.toString();
        } catch (Exception e) {
            throw new UnsupportedVersionException(e);
        }
    }

    /**
     * An error thrown when this NMS-helper class doesn't support the running MC
     * version
     */
    public static class UnsupportedVersionException extends RuntimeException {
        /**
         * The current running version
         */

        public UnsupportedVersionException(Exception e) {
            super("Error while executing reflections, submit to developers the following log (version: "
                    + NMSReflection.versionString + ")", e);
        }
    }

    /**
     * Gets the EntityPlayer handled by the argument
     *
     * @param player the Player handler
     * @return the handled class
     * @throws InvocationTargetException when some problems are found with the
     *                                   reflection
     * @throws IllegalAccessException    when some problems are found with the
     *                                   reflection
     */
    public static Object toNms(Player player) throws InvocationTargetException, IllegalAccessException {
        return craftPlayerGetHandle.invoke(player);
    }

    /**
     * Creates a NMS copy of the parameter
     *
     * @param item the ItemStack to be nms-copied
     * @return a NMS-ItemStack that is the equivalent of the one passed as argument
     * @throws InvocationTargetException when some problems are found with the
     *                                   reflection
     * @throws IllegalAccessException    when some problems are found with the
     *                                   reflection
     */
    public static Object nmsCopy(ItemStack item) throws InvocationTargetException, IllegalAccessException {
        return craftItemStackAsNMSCopy.invoke(null, item);
    }

    public static Class<?> getNmsClass(String className, boolean required) {
        try {
            return Class.forName("net.minecraft.server." + versionString + "." + className);
        } catch (ClassNotFoundException e) {
            if (required) {
                throw new RuntimeException("Cannot find NMS class " + className, e);
            }
            return null;
        }
    }

    public static Class<?> getNmsClass(String className) {
        return getNmsClass(className, false);
    }

    private static Class<?> getCraftClass(String path) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + versionString + "." + path);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find CraftBukkit class at path: " + path, e);
        }
    }



}