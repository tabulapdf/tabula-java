package technology.tabula.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import technology.tabula.RectangularTextContainer;

public final class RectangularTextContainerSerializer implements JsonSerializer<RectangularTextContainer<?>> {

	public static final RectangularTextContainerSerializer INSTANCE = new RectangularTextContainerSerializer();

	private RectangularTextContainerSerializer() {
		// singleton
	}

	@Override
	public JsonElement serialize(RectangularTextContainer<?> src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject result = new JsonObject();
		result.addProperty("top",    src.getTop());
		result.addProperty("left",   src.getLeft());
		result.addProperty("width",  src.getWidth());
		result.addProperty("height", src.getHeight());
		result.addProperty("text",   src.getText());
		return result;
	}

}