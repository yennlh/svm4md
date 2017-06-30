package statistic;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import preprocessing.InstructionMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class Statistic {
	private static final Type JS_TYPE = new TypeToken<InstructionMap>() {
	}.getType();
	private static final Gson gson = new GsonBuilder().create();

	public static InstructionMap load(String pFileName) {
		File jsonFile = new File(pFileName);
		InstructionMap mappingData = null;

		try {
			JsonReader reader = new JsonReader(new FileReader(jsonFile));
			mappingData = gson.fromJson(reader, JS_TYPE);
			reader.close();

			if (mappingData == null)
				mappingData = new InstructionMap();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return mappingData;
	}

	public static Set<String> intersection(final Set<String> first,
			final Set<String> second) {
		final Set<String> copy = new HashSet<>(first);
		copy.retainAll(second);
		return copy;
	}

	public static Set<String> union(final Set<String> first,
			final Set<String> second) {
		final Set<String> copy = new HashSet<>(first);
		copy.addAll(second);
		return copy;
	}

	public static void main(String[] args) throws Exception {
		InstructionMap malware = load("statistic_args_malware.json");
		InstructionMap non_malware = load("statistic_args_non-malware.json");

		final Set<String> first = new HashSet<>(malware.map.keySet());
		final Set<String> second = new HashSet<>(non_malware.map.keySet());
		Set<String> copy = null;
		
//		// INTERSECTION
//		Set<String> intersection = intersection(first, second);
//		for (String s : intersection) {
//			System.out.println(s);
//		}
//		System.out.println("Size: " + intersection.size());
//		System.out.println("====================");
		
//		// A = A - B
//		copy = new HashSet<>(first);
//		copy.removeAll(second);
//		for (String s : copy) {
//			System.out.println(s);
//		}
//		System.out.println("Size: " + copy.size());
//		System.out.println("====================");
		
		// B = B - A
		copy = new HashSet<>(second);
		copy.removeAll(first);
		for (String s : copy) {
			System.out.println(s);
		}
		System.out.println("Size: " + copy.size());
		
		// System.out.println(union(first, second));

	}

}
