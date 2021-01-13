package com.haroldstudios.mailme.database.json.serializer;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.bukkit.util.Vector;

import java.io.IOException;

public class VectorTypeAdapter extends TypeAdapter<Vector> {

    @Override
    public void write(JsonWriter out, Vector value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginArray();
        out.value(value.getX());
        out.value(value.getY());
        out.value(value.getZ());
        out.endArray();
    }

    @Override
    public Vector read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        in.beginArray();
        double x = in.nextDouble();
        double y = in.nextDouble();
        double z = in.nextDouble();
        in.endArray();
        return new Vector(x, y, z);
    }
}
