package technology.tabula.json;

import java.lang.reflect.Type;

import technology.tabula.RectangularTextContainer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class TextChunkSerializer implements JsonSerializer<RectangularTextContainer> {

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