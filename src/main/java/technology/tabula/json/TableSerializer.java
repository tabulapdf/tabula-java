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

public final class TableSerializer implements JsonSerializer<Table> {

    public static final TableSerializer INSTANCE = new TableSerializer();

    private TableSerializer() {}

    @Override
    public JsonElement serialize(Table table, Type type, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        JsonArray data = new JsonArray();

        json.addProperty("extraction_method", table.getExtractionMethod());
        json.addProperty("page_number", table.getPageNumber());
        json.addProperty("top", table.getTop());
        json.addProperty("left", table.getLeft());
        json.addProperty("width", table.getWidth());
        json.addProperty("height", table.getHeight());
        json.addProperty("right", table.getRight());
        json.addProperty("bottom", table.getBottom());
        json.add("data", data);

        for (List<RectangularTextContainer> tableRow : table.getRows()) {
            JsonArray jsonRow = new JsonArray();
            for (RectangularTextContainer textChunk : tableRow)
                jsonRow.add(context.serialize(textChunk));
            data.add(jsonRow);
        }

        return json;
    }

}
