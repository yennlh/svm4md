package preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import sun.security.util.PropertyExpander.ExpandException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * Project: CFG-Reader
 * Package name: 
 * File name: Main.java
 * Created date: Jan 19, 2017
 * Description:
 */

/**
 * @author Yen Nguyen
 *
 */
public class Main {
	static {
		try {
			File file = new File("err.txt");
			FileOutputStream fos = new FileOutputStream(file);
			PrintStream ps = new PrintStream(fos);
			System.setErr(ps);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	private static ArrayList<String> nullFiles = new ArrayList<>();
	private static InstructionMap mappingData = InstructionMap.load();

	public static String getNodeName(String str) {
		String insName = null;

		// TODO: Markable
		if (str.startsWith("0x") || str.startsWith("start")
				|| str.indexOf("\\n") > -1) {
			// ASM
			// label="0x00421a85\nmovl -4(%ebp), %esi"

			String[] s = str.split("\\\\n");

			// if (s.length == 1) {
			// return mappingData.addAsm(null, null, null);
			// }

			String address = s[0];
			String[] operands = s[1].split(" ");

			for (int i = 0; i < operands.length; i++) {
				String ss = operands[i];

				if (ss.endsWith(",")) {
					ss = ss.replace(",", "");
				}

				operands[i] = ss.trim();
			}

			// a0xffffffffe19837faaddb_al_eax_
			if (address.length() > 10
					&& InstructionMap.isHex(address.substring(2,
							address.length()))) {
				// System.err.println("==== Address is too long: " + address +
				// " ====");
			}

			// IGNORE JMP instructions
			// if (IS_MERGE_JUMP && insName.charAt(0) == 'j') {
			// return null;
			// }

			// http://x86.renejeschke.de/html/file_module_x86_id_279.html
			insName = mappingData.addAsm(operands[0],
					(operands.length > 1) ? operands[1] : null,
					(operands.length > 2) ? operands[2] : null,
					(operands.length > 3) ? operands[3] : null);

		} else {
			// API
			// label="GetTickCount@kernel32.dll"

			insName = str;
			
			if (insName.indexOf('@') > -1) {
				insName = insName.substring(0, insName.indexOf('@'));
			}
			
			insName = insName.toLowerCase();
			
			mappingData.addApi(insName);
		}

		return insName;
	}

	public static Graph convertToGraphObj(String filePath) throws IOException {
		int edgeWithoutVertexCounter = 0;
		HashMap<String, String> vertices = new HashMap<>();
		HashMap<String, HashMap<String, Integer>> graph = new HashMap<>();

		// DirectionManager directionManager = new DirectionManager();
		ArrayList<Pair<String, String>> processLater = new ArrayList<>();

		File fin = new File(filePath);
		FileInputStream fis = new FileInputStream(fin);

		// Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		String line = null;
		int addedEdgeCount = 0, edgeCount = 0;
		while ((line = br.readLine()) != null) {
			// System.out.println(line);

			if (line.length() < 2 || line.startsWith("digraph")
					|| line.startsWith("node") || line.startsWith("bgcolor")) {
				// Properties, Do nothing
			} else {
				if (line.contains("->")) {
					edgeCount++;

					// Edge
					String[] s = line.split("\\[");
					s = s[0].split("->");

					try {
						s[0] = s[0].trim();
						s[1] = s[1].trim();
					} catch (Exception ex) {
						// a0x004021camovl__40esp__edx ->
						// The size of "s" array is 1
						System.err
								.println("########### The size of \"s\" array is 1 ##########");
						System.err.println(filePath);
						System.err.println(line);
						ex.printStackTrace();
						System.err.println("############################");
						continue;
					}
					String name1 = s[0];
					String name2 = s[1];

					try {
						String vertexLabel1 = vertices.get(name1);
						String vertexLabel2 = vertices.get(name2);

						if (vertexLabel1 == null || vertexLabel2 == null) {
							if (vertexLabel1 == null)
								System.out.println(name1);
							if (vertexLabel2 == null)
								System.out.println(name2);
							edgeWithoutVertexCounter++;
							continue;
						}

						name1 = getNodeName(vertexLabel1);
						name2 = getNodeName(vertexLabel2);
						// System.out.println(n1 + "->" + n2);
					} catch (Exception e) {
						// a0x14690212btcl_0xffffffc7UINT8_edi -> a0x14690216
						// [color="#000000"];
						System.err.println("############################");
						System.err.println(filePath);
						System.err.println(line);
						e.printStackTrace();
						System.err.println("############################");
						continue;
					}

					// String address1 = (name1 == null) ? s[0].substring(0, 11)
					// : name1;
					// String address2 = (name2 == null) ? s[1].substring(0, 11)
					// : name2;
					// directionManager.store(name1, name2, address1, address2);

					if (name1 == null || name2 == null) {
						// Process later
						Pair<String, String> p = new Pair<String, String>(s[0],
								s[1]);
						processLater.add(p);
						System.err.println(p);
					} else if (name1.length() > 1 && name2.length() > 1) {
						if (!graph.containsKey(name1)) {
							graph.put(name1, new HashMap<String, Integer>());
						}

						HashMap<String, Integer> edges = graph.get(name1);
						if (!edges.containsKey(name2)) {
							edges.put(name2, 1);
						} else {
							edges.put(name2, edges.get(name2) + 1);
						}
						addedEdgeCount++;
					} else {
						// ????:a0x00404059call__28edx_ -> a0x00000000
						// [color="#000000"];
						System.err.println("????:" + line);
					}

				} else {
					// Vertex
					try {
						String[] s = line.split("\\[");
						String vertexName = s[0];

						String label = s[1].substring(s[1].indexOf("\"") + 1,
								s[1].indexOf("\"", 8));
						vertices.put(vertexName, label);
					} catch (Exception e) {
						System.err.println("Interrupted File: " + filePath);
						e.printStackTrace();
						continue;
					}
				}
			}
		}

		if (edgeCount == 0) {
			nullFiles.add(filePath);
			System.err.println("Null File: " + filePath);
		}

		// for (Pair<String, String> pair : processLater) {
		// try {
		// String name1 = getNodeName(pair.getL());
		// String name2 = getNodeName(pair.getR());
		//
		// String address1 = (name1 == null) ? pair.getL()
		// .substring(0, 11) : name1;
		// String address2 = (name2 == null) ? pair.getR()
		// .substring(0, 11) : name2;
		//
		// ArrayList<String> roots = new ArrayList<>();
		// ArrayList<String> leafs = new ArrayList<>();
		//
		// if (name1 == null) {
		// roots = directionManager.findRoots(address1);
		// } else {
		// roots.add(name1);
		// }
		//
		// if (name2 == null) {
		// leafs = directionManager.findLeafs(address2);
		// } else {
		// leafs.add(name2);
		// }
		//
		// for (String n1 : roots) {
		// for (String n2 : leafs) {
		// if (!graph.containsKey(n1)) {
		// graph.put(n1, new HashMap<String, Integer>());
		// }
		//
		// HashMap<String, Integer> edges = graph.get(n1);
		// if (!edges.containsKey(n2)) {
		// edges.put(n2, 1);
		// } else {
		// edges.put(n2, edges.get(n2) + 1);
		// }
		// addedEdgeCount++;
		// }
		// }
		// } catch (Exception e) {
		// System.out.println(filePath);
		// e.printStackTrace();
		// System.out.println();
		// }
		// }

		// System.out.println(graph);

		br.close();
		
		return new Graph(graph, addedEdgeCount);
	}

	public static void write(Graph graph, String fileName, boolean isVirus)
			throws IOException {

		if (graph.numOfEdges < 1 || graph.numOfNodes < 1)
			return;

		File fout = new File(fileName);
		FileOutputStream fos = new FileOutputStream(fout);

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

		int c = 0;
		for (String from : graph.data.keySet()) {
			for (Entry<String, Integer> to : graph.data.get(from).entrySet()) {
				Integer f = mappingData.getMapping(from);
				Integer t = mappingData.getMapping(to.getKey());

				if (f != null && t != null) {
					bw.write(String.valueOf(f));
					bw.write(',');
					bw.write(String.valueOf(t));
					bw.write(',');
					bw.write(String.valueOf(to.getValue()));
					bw.newLine();
					c++;
				}

			}
		}

		if (c == 0) {
			System.out.println(c);
		}

		System.out.println(fileName);

		bw.close();
	}

	public static void listLeafFolder(String directoryName,
			ArrayList<String> inputPaths, ArrayList<String> outputPaths) {
		File directory = new File(directoryName);

		// get all the files from a directory
		File[] fList = directory.listFiles();
		int fileCounter = 0, dirCounter = 0;

		for (File file : fList) {
			if (file.isFile()) {
				fileCounter++;
			} else if (file.isDirectory()) {
				listLeafFolder(file.getAbsolutePath(), inputPaths, outputPaths);
				dirCounter++;
			}
		}

		if (fileCounter > 0 && dirCounter == 0) {
			System.out.println(directoryName);

			String outPath = directoryName.replace("5Folds", "5Folds_out_extended")
					.replace("NonVirus", "non-malware")
					.replace("Virus", "malware");

			if (outPath.contains("malware")) {
				inputPaths.add(directoryName);
				outputPaths.add(outPath);
			}
		}
	}

	public static void main(String[] args) throws IOException {

		String directoryName = "F:/Extra_Development/MinorResearch/5Folds/";
		ArrayList<String> inputPaths = new ArrayList<>();
		ArrayList<String> outputPaths = new ArrayList<>();
		listLeafFolder(directoryName, inputPaths, outputPaths);

		for (int i = 0; i < inputPaths.size(); i++) {
			File inputFolder = new File(inputPaths.get(i));
			File outputFolder = new File(outputPaths.get(i));

			if (!outputFolder.exists()) {
				outputFolder.mkdirs();
			}

			for (final File fileEntry : inputFolder.listFiles()) {
				if (!fileEntry.isDirectory()) {
					// System.out.println(fileEntry.getName());

					// if (fileEntry.getPath().contains("test_model")) {
					// String f = fileEntry.getPath().replace("test_", "");
					// if ((new File(f)).exists()) {
					// System.out.println(f);
					// }
					// fileEntry.delete();
					// }

					Graph g = convertToGraphObj(fileEntry.getAbsolutePath());

					if (g == null) {
						System.out.println(fileEntry.getAbsolutePath());
					} else if (InstructionMap.IS_SCAN) {
						write(g,
								outputPaths.get(i) + "\\" + fileEntry.getName()
										+ ".am", true);
					}
				}
			}
		}

		mappingData.writeJsonMap();

		for (String f : nullFiles) {
			System.err.println(f);
		}
	}
}
