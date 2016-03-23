package technology.tabula.writers;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;

import technology.tabula.Cell;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.TextChunk;
import technology.tabula.json.TableSerializer;
import technology.tabula.json.TextChunkSerializer;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JSONWriter implements Writer {
 
    class TableSerializerExclusionStrategy implements ExclusionStrategy {

        @Override
        public boolean shouldSkipClass(Class<?> arg0) {
            return false;
        }

        @Override
        public boolean shouldSkipField(FieldAttributes fa) {
            return !fa.hasModifier(Modifier.PUBLIC);
        }
    }

    
    final Gson gson;
    
    public JSONWriter() {
        gson = new GsonBuilder()
           .addSerializationExclusionStrategy(new TableSerializerExclusionStrategy())
           .registerTypeAdapter(Table.class, new TableSerializer())
           .registerTypeAdapter(RectangularTextContainer.class, new TextChunkSerializer())
           .registerTypeAdapter(Cell.class, new TextChunkSerializer())
           .registerTypeAdapter(TextChunk.class, new TextChunkSerializer())
           .create();
    }
    
    @Override
    public void write(Appendable out, Table table) throws IOException {
    	
    	out.append(gson.toJson(table, Table.class));
    }
    
    public void write(Appendable out, List<Table> tables) throws IOException {
    	
    	out.append(gson.toJson(tables.toArray(), Table[].class));
    }
}
