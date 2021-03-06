package filtering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import filtering.Grammar.Production;

public class Tree {
	HashSet<Production> rules;
	String partitionSymbol;
	Tree left;
	Tree right;
	public int size;
	public Tree(HashSet<Production> rules, String s) {
		this.rules = rules;
		this.partitionSymbol = s;
	}
	public Tree(int depth, List<String> indexing, HashSet<Production> rules) {
		this.rules = rules;
		if(depth == indexing.size()) {
			return;
		}
		partitionSymbol = indexing.get(depth);
		if(rules.size() != 0) {
			HashSet<Production> leftRules = new HashSet<Production>();
			HashSet<Production> rightRules = new HashSet<Production>();
			
			for(Production p : rules) {
				if(p.terminals.contains(partitionSymbol)) {
					leftRules.add(p);
				} else {
					rightRules.add(p);
				}
			}
			if(leftRules.size() != 0) {
				left = new Tree(depth+1, indexing, leftRules);
			}
			if(rightRules.size() != 0) {
				right = new Tree(depth+1, indexing, rightRules);
			}
		} else {
			return;
		}
	}
	
	public static void filter(Tree root, HashSet<Production> activeRules, HashSet<String> filteredTerminals, int deepestLevel) {
		continuations = new LinkedList<>();
		int initialDepth = 0;
		continuations.add(new Object[] {initialDepth, root});
		while(continuations.size() > 0) {
			Object[] continuation = continuations.remove(0);
			int depth = (int) continuation[0];
			Tree t = (Tree) continuation[1];
			String sym = t.partitionSymbol;
			if(sym == null || depth > deepestLevel || t.rules.size() == 1) {
				// is leaf or past last filtered symbol
				t.filterProductions(activeRules, filteredTerminals);
			} else {
				if(filteredTerminals.contains(sym)) {
					if(t.right != null) {
						continuations.add(new Object[] {depth+1, t.right});
						
					}
				} else {
					if(t.right != null) {
						continuations.add(new Object[] {depth+1, t.right});
					}
					if(t.left != null) {
						continuations.add(new Object[] {depth+1, t.left});
					}
				}
			}
		}
	}

	private void filterProductions(HashSet<Production> activeRules, HashSet<String> filteredTerminals) {
		for(Production p : rules) {
			boolean ruledOut = false;
			for(String terminal : p.terminals) {
				if(filteredTerminals.contains(terminal)) {
					ruledOut = true;
				}
			}
			if(!ruledOut) {
				activeRules.add(p);
			}
		}
	}

	static List<Object[]> continuations;
	static List<String> indexing;
	static HashSet<Production> filterableProductions;
	static HashMap<Integer, Tree> treeIdxs;
	
	static int nodes = 0;
	public static Tree buildTree(List<String> indexing, HashSet<Production> filterableProductions) {
		Tree.indexing = indexing;
		Tree.filterableProductions = filterableProductions;
		Tree.treeIdxs = new HashMap<>();
		continuations = new LinkedList<>();
		continuations.add(new Object[] {0, 1, filterableProductions});
		int i = 0;
		while(continuations.size() > 0) {
			Object[] continuation = continuations.remove(0);
			int depth = (int) continuation[0];
			int index = (int) continuation[1];
			i++;
			HashSet<Production> rules = (HashSet<Production>) continuation[2];
			
			String partitionSymbol = null;
			if(depth < indexing.size()) {
				partitionSymbol = indexing.get(depth);
				
				// Set child indices
				int leftChildIdx = index * 2;
				int rightChildIdx = leftChildIdx + 1;
				
				// Set child depth
				int childDepth = depth + 1;
				
				
				if(rules.size() > 1) {
					HashSet<Production> leftRules = new HashSet<Production>();
					HashSet<Production> rightRules = new HashSet<Production>();
					
					for(Production p : rules) {
						if(p.terminals.contains(partitionSymbol)) {
							leftRules.add(p);
						} else {
							rightRules.add(p);
						}
					}
					if(leftRules.size() != 0) {
						Object[] leftContinuation = new Object[] {childDepth, leftChildIdx, leftRules};
						continuations.add(leftContinuation);
					}
					if(rightRules.size() != 0) {
						Object[] rightContinuation = new Object[]  {childDepth, rightChildIdx, rightRules};
						continuations.add(rightContinuation);
					}
				}
			}

			
			Tree t = new Tree(rules, partitionSymbol);
			treeIdxs.put(index, t);
		}
		
		
		for(Integer idx : treeIdxs.keySet()) {
			Tree t = treeIdxs.get(idx);
			if(treeIdxs.containsKey(2*idx)) {
				t.left = treeIdxs.get(2*idx);
			} 
			if(treeIdxs.containsKey(2*idx + 1)) {
				t.right = treeIdxs.get(2*idx + 1);
			}
		}
		System.out.println(treeIdxs.size() + " nodes");
		treeIdxs.get(1).size = treeIdxs.size();
		return treeIdxs.get(1);
	}
}
