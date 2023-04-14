package technology.tabula.writers;

import com.google.gson.*;
import technology.tabula.*;
import technology.tabula.json.RectangularTextContainerSerializer;
import technology.tabula.json.TableSerializer;
import technology.tabula.outobjects.OutTable;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;

public class SJSONWriter implements Writer {

	private Map<String, List<String>> tableMap;
	private static final ExclusionStrategy ALLCLASSES_SKIPNONPUBLIC = new ExclusionStrategy() {
		@Override public boolean shouldSkipClass(Class<?> c) { return false; }
		@Override public boolean shouldSkipField(FieldAttributes fa) { return !fa.hasModifier(Modifier.PUBLIC); }
	};

	public SJSONWriter(Map<String, List<String>> tableMap){
		this.tableMap = tableMap;
	}

	@Override
	public void write(Appendable out, Table table) throws IOException {
		write(out, Collections.singletonList(table));
	}

	@Override
	public void write(Appendable out, List<Table> tables) throws IOException {
		Gson gson = gson();
		Map<String, OutTable> outTableMap = new HashMap<>();
		OutTable outTable = null;
		for (Table table : tables) {
			if (table.getRowCount() > 0){
				String tableName = table.getTableName();
				if (outTableMap.containsKey(tableName)) {
					outTable = outTableMap.get(tableName);
				}
				else {
					outTable = new OutTable();
					outTable.setName(tableName);
					outTable.setColumn(new ArrayList<>());
					outTable.setData(new ArrayList<>());
					outTableMap.put(tableName, outTable);
				}
				int dataRow = 0;
				//查找列的位置及数据开始位置
				/*
				Map<String, Integer> colPos = null;
				if (tableMap != null) {
					List<String> cols = tableMap.get(tableName);
					for(String item: cols){
						colPos.put(item, null);
					}
				}
				*/

				/*
				if (colPos != null) {
					for (int i = 0; i < table.getRows().size(); i++) {
						List<RectangularTextContainer> row = table.getRows().get(i);
						for (int j = 0; j< row.size(); j++) {
							RectangularTextContainer<?> tc = row.get(j);
							for (String key : colPos.keySet()) {
								if (colPos.get(key) != null && Utils.isMatch(tc.getText(),key)){
									outTable.getColumn().add(tc.getText());
									colPos.put(key, Integer.valueOf(j));
									dataRow = i;
								}
							}
						}
					}
				}
				*/
				for(int i = dataRow; i< table.getRows().size(); i++){
					List<RectangularTextContainer> row = table.getRows().get(i);
					List<String> cells = new ArrayList<>(row.size());
					for (RectangularTextContainer<?> tc : row) {
						cells.add(tc.getText());
					}
					if (!Utils.isEmptyRow(cells)) outTable.getData().add(cells);
				}
			}
		}
		JsonArray array = new JsonArray();
		for (Map.Entry<String, OutTable> m : outTableMap.entrySet()) {
			array.add(gson.toJsonTree(m.getValue(), OutTable.class));
		}
		out.append(gson.toJson(array));
	}

	private static Gson gson() {
		return new GsonBuilder()
				.create();
	}

}
