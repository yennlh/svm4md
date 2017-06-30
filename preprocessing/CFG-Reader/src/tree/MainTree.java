/**
 * Project: CFG-Reader
 * Package name: tree
 * File name: MainTree.java
 * Created date: Mar 22, 2017
 * Description:
 */
package tree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import tree.structure.AST;
import tree.structure.Vertex;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * @author Yen Nguyen
 *
 */
public class MainTree {
	private static final String[] FILE_NAME = {
			"F:/Extra_Development/MinorResearch/CodeChef/SUMTRIAN_train_AstGraph.json",
			"F:/Extra_Development/MinorResearch/CodeChef/SUMTRIAN_test_AstGraph.json", };
	private static final Type JS_TYPE = new TypeToken<ArrayList<AST>>() {
	}.getType();
	private static final Gson gson = new GsonBuilder().create();

	public static ArrayList<AST> load(String path) {
		ArrayList<AST> astData = null;

		try {
			JsonReader reader = new JsonReader(new FileReader(path));
			astData = gson.fromJson(reader, JS_TYPE);
			reader.close();

			if (astData == null)
				astData = new ArrayList<AST>();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return astData;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		VertexMap vertexMap = VertexMap.load();
		String paths[] = {
				"F:/Extra_Development/MinorResearch/CodeChef/out/training",
				"F:/Extra_Development/MinorResearch/CodeChef/out/testing" };

		for (int i = 0; i < 2; i++) {
			ArrayList<AST> asts = load(FILE_NAME[i]);

			int count = 0;
			for (AST a : asts) {
				count++;

				String fileName = paths[i] + ((a.label == 0) ? "/a/" : "/b/")
						+ count + ".a";
				File fout = new File(fileName);

				if (!fout.getParentFile().exists()) {
					fout.getParentFile().mkdirs();
				}

				FileOutputStream fos = new FileOutputStream(fout);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						fos));

				a.reconstruc();
				HashMap<String, Integer> graph = new HashMap<>();

				for (Integer[] e : a.E) {
					if (e.length == 2) {
						String from = a.vertices.get(e[0]).token;
						String to = a.vertices.get(e[1]).token;

						int f = -1;
						int t = -1;
						try {
							f = vertexMap.getMapping(from);
							t = vertexMap.getMapping(to);
						} catch (Exception ex) {
							System.err.println(from + " " + to);
							vertexMap.add(from);
							vertexMap.add(to);
							f = vertexMap.getMapping(from);
							t = vertexMap.getMapping(to);
						}

						String key = f + "_" + t;
						Integer w = graph.get(key);

						if (w != null) {
							graph.put(key, w + 1);
						} else {
							graph.put(key, 1);
						}
					}
				}

				for (Entry<String, Integer> e : graph.entrySet()) {
					String[] ee = e.getKey().split("_");

					bw.write(ee[0]);
					bw.write(',');
					bw.write(ee[1]);
					bw.write(',');
					bw.write(String.valueOf(e.getValue()));
					bw.newLine();
				}

				bw.close();
			}
			
			vertexMap.writeJsonMap();
		}
	}

}
