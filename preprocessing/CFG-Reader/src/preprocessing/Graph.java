package preprocessing;

import java.util.HashMap;

public class Graph {
	public int numOfEdges = 0;
	public int numOfNodes = 0;
	public HashMap<String, HashMap<String, Integer>> data = null;

	public Graph(HashMap<String, HashMap<String, Integer>> g, int e) {
		numOfEdges = e;
		numOfNodes = g.size();
		data = g;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("E=");
		stringBuilder.append(numOfEdges);
		stringBuilder.append("\r\n");
		stringBuilder.append("N=");
		stringBuilder.append(numOfNodes);
		stringBuilder.append("\r\n");
		stringBuilder.append(data.toString());
		return stringBuilder.toString();
	}
}
