package org.nerdpower.tabula.writers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import org.nerdpower.tabula.Table;
import org.nerdpower.tabula.TextChunk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JSONWriter implements Writer {
    
    class TableSerializer implements JsonSerializer<Table> {

        @Override
        public JsonElement serialize(Table table, Type type,
                JsonSerializationContext context) {
            
            JsonObject object = new JsonObject();
            object.addProperty("extraction_method", table.getExtractionAlgorithm().toString());
            
            JsonArray jsonDataArray = new JsonArray();
            for (List<TextChunk> row: table.getRows()) {
                JsonArray jsonRowArray = new JsonArray();
                for (TextChunk textChunk: row) {
                    jsonRowArray.add(context.serialize(textChunk));
                }
                jsonDataArray.add(jsonRowArray);
            }
            object.add("data", jsonDataArray);
            
            return object;
        }
    }
    
    class TextChunkSerializer implements JsonSerializer<TextChunk> {

        @Override
        public JsonElement serialize(TextChunk textChunk, Type arg1,
                JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            
            object.addProperty("top", textChunk.getTop());
            object.addProperty("left", textChunk.getLeft());
            object.addProperty("width", textChunk.getWidth());
            object.addProperty("height", textChunk.getHeight());
            object.addProperty("text", textChunk.getText());

            return object;
        }
    }
    
    final GsonBuilder gsonBuilder = new GsonBuilder();
    final Gson gson;
    
    public JSONWriter() {
        gsonBuilder.registerTypeAdapter(Table.class, new TableSerializer());
        gsonBuilder.registerTypeAdapter(TextChunk.class, new TextChunkSerializer());
        gson = gsonBuilder.create();
    }
    
    @Override
    public void write(Appendable out, Table table) throws IOException {
        out.append(gson.toJson(table));
    }
    
}
