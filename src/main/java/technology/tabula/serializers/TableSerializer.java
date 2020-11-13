package technology.tabula.serializers;

import com.google.gson.*;
import technology.tabula.table.Table;
import technology.tabula.text.RectangularTextContainer;

import java.lang.reflect.Type;
import java.util.List;

public final class TableSerializer implements JsonSerializer<Table> {

	public static final TableSerializer INSTANCE = new TableSerializer();

	private TableSerializer() {}

	@Override
	public JsonElement serialize(Table src, Type typeOfSrc, JsonSerializationContext context) {

		JsonObject result = new JsonObject();

		result.addProperty("extraction_method", src.getExtractionMethod());
		result.addProperty("top",    src.getTop());
		result.addProperty("left",   src.getLeft());
		result.addProperty("width",  src.getWidth());
		result.addProperty("height", src.getHeight());
		result.addProperty("right",  src.getRight());
		result.addProperty("bottom", src.getBottom());

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
