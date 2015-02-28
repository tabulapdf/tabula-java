package org.nerdpower.tabula.writers;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

import org.nerdpower.tabula.Cell;
import org.nerdpower.tabula.RectangularTextContainer;
import org.nerdpower.tabula.Table;
import org.nerdpower.tabula.TextChunk;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
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
            object.addProperty("top", table.getTop());
            object.addProperty("left", table.getLeft());
            object.addProperty("width", table.getWidth());
            object.addProperty("height", table.getHeight());
            
            JsonArray jsonDataArray = new JsonArray();
            for (List<RectangularTextContainer> row: table.getRows()) {
                JsonArray jsonRowArray = new JsonArray();
                for (RectangularTextContainer textChunk: row) {
                    jsonRowArray.add(context.serialize(textChunk));
                }
                jsonDataArray.add(jsonRowArray);
            }
            object.add("data", jsonDataArray);
            
            return object;
        }
    }
    
    class TextChunkSerializer implements JsonSerializer<RectangularTextContainer> {

        @Override
        public JsonElement serialize(RectangularTextContainer textChunk, Type arg1,
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

    class TableSerializerExclusionStrategy implements ExclusionStrategy {

        @Override
        public boolean shouldSkipClass(Class<?> arg0) {
            return false;
        }

        @Override
        public boolean shouldSkipField(FieldAttributes fa) {
            return !fa.hasModifier(Modifier.PUBLIC);
        }
    }

    
    //final GsonBuilder gsonBuilder;
    final Gson gson;
    
    public JSONWriter() {
        gson = new GsonBuilder()
           .addSerializationExclusionStrategy(new TableSerializerExclusionStrategy())
           .registerTypeAdapter(Table.class, new TableSerializer())
           .registerTypeAdapter(RectangularTextContainer.class, new TextChunkSerializer())
           .registerTypeAdapter(Cell.class, new TextChunkSerializer())
           .registerTypeAdapter(TextChunk.class, new TextChunkSerializer())
           .create();
    }
    
    @Override
    public void write(Appendable out, Table table) throws IOException {
        out.append(gson.toJson(table, Table.class));
    }
}
