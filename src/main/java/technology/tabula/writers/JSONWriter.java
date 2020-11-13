package technology.tabula.writers;

import com.google.gson.*;
import technology.tabula.serializers.RectangularTextContainerSerializer;
import technology.tabula.serializers.TableSerializer;
import technology.tabula.table.Table;
import technology.tabula.text.Cell;
import technology.tabula.text.RectangularTextContainer;
import technology.tabula.text.TextChunk;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;

public class JSONWriter implements Writer {

	private static final ExclusionStrategy ALL_CLASSES_SKIPPING_NON_PUBLIC_FIELDS = new ExclusionStrategy() {
		@Override
		public boolean shouldSkipClass(Class<?> c) { return false; }

		@Override
		public boolean shouldSkipField(FieldAttributes fa) { return !fa.hasModifier(Modifier.PUBLIC); }
	};

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
