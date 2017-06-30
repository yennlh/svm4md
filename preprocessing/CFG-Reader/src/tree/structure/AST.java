package tree.structure;

import java.util.ArrayList;
import java.util.HashMap;

public class AST {
	public ArrayList<Vertex> V;
	public ArrayList<Integer[]> E;
	public int label;
	public HashMap<Integer, Vertex> vertices = null;
	
	public void reconstruc() {
		vertices = new HashMap<>();
		
		for (Vertex v : V) {
			vertices.put(v.id, v);
		}
		
		V = null;
	}
}
