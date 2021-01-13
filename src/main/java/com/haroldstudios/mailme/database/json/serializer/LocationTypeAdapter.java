package com.haroldstudios.mailme.database.json.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

import com.google.gson.*;
import com.haroldstudios.mailme.MailMe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class LocationTypeAdapter extends TypeAdapter<Location> {

    @Override
    public void write(JsonWriter out, Location location) throws IOException {
        if (location == null || location.getWorld() == null) {
            out.nullValue();
            return;
        }
        out.beginArray();
        out.value(location.getWorld().getName());
        out.value(location.getX());
        out.value(location.getY());
        out.value(location.getZ());
        out.value(location.getYaw());
        out.value(location.getPitch());
        out.endArray();
    }

    @Override
    public Location read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        } else if (in.peek() == JsonToken.BEGIN_OBJECT) {
            JsonParser parser = new JsonParser();
            return deserialize(parser.parse(in));
        }
        in.beginArray();
        World world = Bukkit.getServer().getWorld(in.nextString());
        double x = in.nextDouble();
        double y = in.nextDouble();
        double z = in.nextDouble();
        float yaw = (float)in.nextDouble();
        float pitch = (float)in.nextDouble();
        in.endArray();
        return new Location(world, x, y, z, yaw, pitch);
    }

    public Location deserialize(JsonElement jsonElement) {
        JsonObject object = jsonElement.getAsJsonObject();
        try {

            return new Location(Bukkit.getWorld(object.get("world").getAsString()),
                    object.get("x").getAsDouble(),
                    object.get("y").getAsDouble(),
                    object.get("z").getAsDouble());
        } catch (Exception ex) {
            MailMe.debug(ex);
            return null;
        }


    }
}

