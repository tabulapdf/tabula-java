package technology.tabula.json;

import java.lang.reflect.Type;
import java.util.List;

import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class TableSerializer implements JsonSerializer<Table> {

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