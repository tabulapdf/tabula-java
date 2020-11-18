package technology.tabula.writers;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import technology.tabula.Cell;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.TextChunk;
import technology.tabula.json.RectangularTextContainerSerializer;
import technology.tabula.json.TableSerializer;

import java.io.IOException;
import java.util.List;

import static java.lang.reflect.Modifier.PUBLIC;

public class JSONWriter implements Writer {

    private static final ExclusionStrategy ALL_CLASSES_SKIPPING_NON_PUBLIC_FIELDS = new ExclusionStrategy() {
        @Override
        public boolean shouldSkipClass(Class<?> c) {
            return false;
        }

        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return !fieldAttributes.hasModifier(PUBLIC);
        }
    };

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    @Override
    public void write(Appendable out, Table table) throws IOException {
        out.append(gson().toJson(table, Table.class));
    }

    @Override
    public void write(Appendable out, List<Table> tables) throws IOException {
        Gson gson = gson();
        JsonArray jsonElements = new JsonArray();
        for (Table table : tables)
            jsonElements.add(gson.toJsonTree(table, Table.class));
        out.append(gson.toJson(jsonElements));
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
    private static Gson gson() {
        return new GsonBuilder()
                .addSerializationExclusionStrategy(ALL_CLASSES_SKIPPING_NON_PUBLIC_FIELDS)
                .registerTypeAdapter(Table.class, TableSerializer.INSTANCE)
                .registerTypeAdapter(RectangularTextContainer.class, RectangularTextContainerSerializer.INSTANCE)
                .registerTypeAdapter(Cell.class, RectangularTextContainerSerializer.INSTANCE)
                .registerTypeAdapter(TextChunk.class, RectangularTextContainerSerializer.INSTANCE)
                .create();
    }

}
