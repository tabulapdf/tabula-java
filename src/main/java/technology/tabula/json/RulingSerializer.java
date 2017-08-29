package technology.tabula.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import technology.tabula.Ruling;

@Deprecated
/** @deprecated This class is unused (Aug 2017) and will be removed at some later point */
public class RulingSerializer implements JsonSerializer<Ruling> {

    @Override
    public JsonElement serialize(Ruling src, Type typeOfSrc, JsonSerializationContext context) {
        return null;
    }

}
