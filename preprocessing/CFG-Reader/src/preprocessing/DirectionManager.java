package preprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DirectionManager {

	// jmp instruction ->
	HashMap<String, HashSet<String>> childDirection = new HashMap<>();
	// -> jmp instruction
	HashMap<String, HashSet<String>> parentDirection = new HashMap<>();

	public void store(String n1, String n2, String address1, String address2) {
		// JUMP Instruction
		if (n1 == null) {
			if (!childDirection.containsKey(address1)) {
				childDirection.put(address1, new HashSet<String>());
			}

			if (!childDirection.get(address1).contains(address2)) {
				childDirection.get(address1).add(address2);
			}
		}

		if (n2 == null) {
			if (!parentDirection.containsKey(address2)) {
				parentDirection.put(address2, new HashSet<String>());
			}

			if (!parentDirection.get(address2).contains(address1)) {
				parentDirection.get(address2).add(address1);
			}
		}
	}

	private void findRoot(ArrayList<String> result, String address) {
		HashSet<String> roots = parentDirection.get(address);
		if (roots != null) {
			for (String r : roots) {
				if (r.startsWith("a0x")) {
					findRoot(result, r);
				} else {
					result.add(r);
				}
			}
		}
	}

	public ArrayList<String> findRoots(String address) {
		ArrayList<String> result = new ArrayList<>();
		findRoot(result, address);
		return result;
	}

	private void findLeaf(ArrayList<String> result, String address) {
		HashSet<String> roots = childDirection.get(address);
		if (roots != null) {
			for (String r : roots) {
				if (r.startsWith("a0x")) {
					findRoot(result, r);
				} else {
					result.add(r);
				}
			}
		}
	}

	public ArrayList<String> findLeafs(String address) {
		ArrayList<String> result = new ArrayList<>();
		findLeaf(result, address);
		return result;
	}
}
