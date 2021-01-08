package com.haroldstudios.mailme.database.json.serializer;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.Mail;
import org.bukkit.Bukkit;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import org.bukkit.Location;

import com.google.common.io.Files;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class FileUtil {


    private final Gson gson = buildGson().create();

    public static String getName(Class<?> clazz) {
        return clazz.getSimpleName().toLowerCase();
    }

    // ------------------------------------------------------------ //
    // GET NAME - What should we call this type of object?
    // ------------------------------------------------------------ //

    public static String getName(Object o) {
        return getName(o.getClass());
    }

    public static String getName(Type type) {
        return getName(type.getClass());
    }

    private GsonBuilder buildGson() {
        return new GsonBuilder().disableHtmlEscaping()
                .enableComplexMapKeySerialization()
                .setPrettyPrinting()
                .setDateFormat("dd-MM-yyyy-hh:mm:ss.SSS")
                .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE)
                .registerTypeAdapter(Location.class, new LocationSerializer())
                .registerTypeAdapter(ItemStack.class, new ItemStackTypeAdapter())
                .registerTypeAdapter(Mail.class, new AbstractClassSerializer<Mail>())
                .registerTypeAdapterFactory(EnumTypeAdapter.ENUM_FACTORY);
    }

    public Gson getGson() { return gson; }

    // ------------------------------------------------------------ //
    // GET FILE - In which file would we like to store this object?
    // ------------------------------------------------------------ //

    public File getFile(String name) {
        return new File(MailMe.getInstance().getDataFolder(), name + ".json");
    }

    public File getFile(Class<?> clazz) {
        return getFile(getName(clazz));
    }

    public File getFile(Object obj) {
        return getFile(getName(obj));
    }

    // SAVE

    public void save(Object instance) {
        save(instance, getFile(instance));
    }

    public void save(Object instance, File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writeCatch(file, gson.toJson(instance));
    }

    // Serialization

    public String serialize(Mail mail) {
        Type token = new TypeToken<Mail>() {}.getType();
        return gson.toJson(mail, token);
    }

    public Mail deserializeMail(String json) {
        Type token = new TypeToken<Mail>() {}.getType();
        return gson.fromJson(json, token);
    }

    // LOAD BY CLASS
    public <T> T load(Class<T> clazz) {
        return load(clazz, getFile(clazz));
    }

    public <T> T load(Class<T> clazz, File file) {
        String content = readCatch(file);
        if (content == null) {
            return null;
        }

        try {
            return gson.fromJson(content, clazz);
        } catch (Exception ex) {
            MailMe.getInstance().getLogger().severe("Failed to parse " + file.toString() + ": " + ex.getMessage());
            Bukkit.getPluginManager().disablePlugin(MailMe.getInstance());
        }

        return null;
    }

    // -------------------------------------------- //
    // BYTE
    // -------------------------------------------- //
    private static final Map<String, Lock> locks = new HashMap<>();

    public static byte[] readBytes(File file) throws IOException {
        int length = (int) file.length();
        byte[] output = new byte[length];
        InputStream in = new FileInputStream(file);
        int offset = 0;
        while (offset < length) {
            offset += in.read(output, offset, (length - offset));
        }
        in.close();

        return output;
    }

    // -------------------------------------------- //
    // CATCH
    // -------------------------------------------- //

    public static String read(File file) throws IOException {
        return utf8(readBytes(file));
    }

    public static void writeCatch(final File file, final String content) {
        String name = file.getName();
        final Lock lock;

        // Create lock for each file if there isn't already one.
        if (locks.containsKey(name)) {
            lock = locks.get(name);
        } else {
            ReadWriteLock rwl = new ReentrantReadWriteLock();
            lock = rwl.writeLock();
            locks.put(name, lock);
        }
        lock.lock();
        try {
            file.createNewFile();
            Files.write(content, file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public static String readCatch(File file) {
        try {
            return read(file);
        } catch (IOException e) {
            return null;
        }
    }

    public static String utf8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
