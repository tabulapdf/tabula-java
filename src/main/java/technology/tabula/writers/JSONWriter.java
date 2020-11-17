package technology.tabula.writers;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import technology.tabula.texts.Cell;
import technology.tabula.texts.RectangularTextContainer;
import technology.tabula.tables.Table;
import technology.tabula.texts.TextChunk;
import technology.tabula.json.RectangularTextContainerSerializer;
import technology.tabula.json.TableSerializer;

public class JSONWriter implements Writer {

	private static final ExclusionStrategy ALL_CLASSES_SKIPPING_NON_PUBLIC_FIELDS = new ExclusionStrategy() {
		@Override
		public boolean shouldSkipClass(Class<?> c) { return false; }

		@Override
		public boolean shouldSkipField(FieldAttributes fa) { return !fa.hasModifier(Modifier.PUBLIC); }
	};

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - //
	@Override
	public void write(Appendable out, Table table) throws IOException {
		out.append(gson().toJson(table, Table.class));
	}

	@Override public void write(Appendable out, List<Table> tables) throws IOException {
		Gson gson = gson();
		JsonArray jsons = new JsonArray();
		for (Table table : tables)
			jsons.add(gson.toJsonTree(table, Table.class));
		out.append(gson.toJson(jsons));
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
