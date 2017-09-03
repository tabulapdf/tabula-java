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

	private TableSerializer() {
		// singleton
	}

	@Override
	public JsonElement serialize(Table src, Type typeOfSrc, JsonSerializationContext context) {

		JsonObject result = new JsonObject();

		result.addProperty("extraction_method", src.getExtractionMethod());
		result.addProperty("top",    src.getTop());
		result.addProperty("left",   src.getLeft());
		result.addProperty("width",  src.getWidth());
		result.addProperty("height", src.getHeight());

		JsonArray data;
		result.add("data", data = new JsonArray());
		
		for (List<RectangularTextContainer> srcRow : src.getRows()) {
			JsonArray row = new JsonArray();
			for (RectangularTextContainer textChunk : srcRow) row.add(context.serialize(textChunk));
			data.add(row);
		}

		return result;
	}

}
