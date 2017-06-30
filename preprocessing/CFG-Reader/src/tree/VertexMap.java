package tree;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

public class VertexMap {

	public static final boolean IS_SCAN = true;
	private static final Gson gson = new GsonBuilder().create();
	private static final String JSON_FILE = "AST_mapping.json";

	public HashMap<String, Integer> map = new HashMap<>();
	public HashMap<String, Integer> counter = new HashMap<>();

	public String add(String token) {

		if (map == null)
			map = new HashMap<>();

		if (map.containsKey(token)) {
			if (IS_SCAN) {
				counter.put(token, counter.get(token) + 1);
			}
			return token;
		}

		if (IS_SCAN) {
			map.put(token, map.size() + 1);
			counter.put(token, 1);
		}

		return token;
	}

	public Integer getMapping(String key) {
		return map.get(key);
	}

	public int getSize() {
		return map.size();
	}

	public static VertexMap load() {
		VertexMap mappingData = null;

		try {
			JsonReader reader = new JsonReader(new FileReader(JSON_FILE));
			mappingData = gson.fromJson(reader, VertexMap.class);
			reader.close();

			if (mappingData == null)
				mappingData = new VertexMap();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return mappingData;
	}

	public void writeJsonMap() {
		try {
			FileOutputStream outputStream = new FileOutputStream(JSON_FILE);
			outputStream.write(gson.toJson(this).getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
