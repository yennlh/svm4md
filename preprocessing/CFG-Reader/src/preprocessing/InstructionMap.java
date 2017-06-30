package preprocessing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class InstructionMap {

	public static final boolean IS_SCAN = true;

	public static final boolean IS_CONSIDER_ARGS = true;

	private static final String JSON_FILE_NAME = "node_map_extended_413949.json";
	private static final File JSON_FILE = new File(JSON_FILE_NAME);
	private static final Type JS_TYPE = new TypeToken<InstructionMap>() {
	}.getType();
	private static final Gson gson = new GsonBuilder().create();

	public int numOfAsm = 0;
	public int numOfAPI = 0;

	public HashMap<String, Integer> map = new HashMap<>();
	public HashMap<String, Integer> counter = new HashMap<>();

	// http://x86.renejeschke.de/html/file_module_x86_id_279.html
	public String addAsm(String asm, String arg1, String arg2, String arg3) {
		if (asm == null) {
			System.err.println("Null assembly instruction");
			asm = "NULL";
		}

		if (IS_CONSIDER_ARGS && arg1 != null) {
			asm += "_" + TypeManager.getTypeName(arg1);

			if (arg2 != null) {
				asm += "_" + TypeManager.getTypeName(arg2);
			}

			if (arg3 != null) {
				asm += "_" + TypeManager.getTypeName(arg3);
			}
		}

		// // IGNORE JMP instructions
		// if (asm.charAt(0) == 'j') {
		// return asm;
		// }

		if (map == null)
			map = new HashMap<>();

		if (map.containsKey(asm)) {
			if (IS_SCAN) {
				counter.put(asm, counter.get(asm) + 1);
			}
			return asm;
		}

		if (IS_SCAN) {
			map.put(asm, map.size() + 1);
			counter.put(asm, 1);

			numOfAsm++;
		}

		return asm;
	}

	public void addApi(String api) {
		if (map == null)
			map = new HashMap<>();

		// if (api.startsWith("a?")) {
		// System.out.println(api);
		// }

		if (map.containsKey(api)) {
			if (IS_SCAN) {
				// counter.put(api, -1);
				counter.put(api, counter.get(api) + 1);
			}
			return;
		}

		if (IS_SCAN) {
			map.put(api, map.size() + 1);
			counter.put(api, 1);
			numOfAPI++;
		}
	}

	public Integer getMapping(String key) {
		return map.get(key);
	}

	public int getSize() {
		return map.size();
	}

	public static InstructionMap load() {
		InstructionMap mappingData = null;

		try {
			JsonReader reader = new JsonReader(new FileReader(JSON_FILE));
			mappingData = gson.fromJson(reader, JS_TYPE);
			reader.close();

			if (mappingData == null)
				mappingData = new InstructionMap();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return mappingData;
	}

	public void writeJsonMap() {
		if (!IS_SCAN)
			return;
		
		try {
			FileOutputStream outputStream = new FileOutputStream(JSON_FILE);
			outputStream.write(gson.toJson(this).getBytes());
			outputStream.close();

			System.out.println("Matrix Size: " + map.size());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isHex(String str) {
		return str.matches("-?[0-9a-fA-F]+"); // match a number with optional
												// '-' and decimal.
	}

	public static boolean isDecimal(String str) {
		return str.matches("-?[0-9]+"); // match a number with optional
										// '-' and decimal.
	}

	public static void main(String[] args) throws IOException {
		InstructionMap mappingData = load();

		for (Iterator<Entry<String, Integer>> it = mappingData.counter
				.entrySet().iterator(); it.hasNext();) {
			Entry<String, Integer> c = it.next();
			
			if (c.getValue() <= 5) {
				mappingData.map.remove(c.getKey());
				it.remove();
			}
			
//			if (isDecimal(c.getKey()) && c.getValue() <= 5) {
//				mappingData.map.remove(c.getKey());
//				it.remove();
//			}
		}

		// Reconstruct mapping table
		int i = 1;
		for (Entry<String, Integer> e : mappingData.map.entrySet()) {
			mappingData.map.put(e.getKey(), i);
			i++;
		}

		mappingData.writeJsonMap();
	}
}
