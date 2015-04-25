package technology.tabula.json;

import java.lang.reflect.Type;

import technology.tabula.Ruling;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class RulingSerializer implements JsonSerializer<Ruling> {

    @Override
    public JsonElement serialize(Ruling arg0, Type arg1,
            JsonSerializationContext arg2) {

        JsonObject object = new JsonObject();
        
        return null;
    }

}
