package technology.tabula.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import technology.tabula.RectangularTextContainer;

public final class RectangularTextContainerSerializer implements JsonSerializer<RectangularTextContainer<?>> {

    public static final RectangularTextContainerSerializer INSTANCE = new RectangularTextContainerSerializer();

    private RectangularTextContainerSerializer() {}

    @Override
    public JsonElement serialize(RectangularTextContainer<?> textContainer, Type type, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("top", textContainer.getTop());
        json.addProperty("left", textContainer.getLeft());
        json.addProperty("width", textContainer.getWidth());
        json.addProperty("height", textContainer.getHeight());
        json.addProperty("text", textContainer.getText());
        return json;
    }

}