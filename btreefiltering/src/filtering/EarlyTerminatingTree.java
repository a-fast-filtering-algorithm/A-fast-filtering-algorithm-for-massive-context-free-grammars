package filtering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import filtering.Grammar.Production;

public class EarlyTerminatingTree {
	HashSet<Production> rules;
	HashSet<Production> terminatingRules;

	String partitionSymbol;
	EarlyTerminatingTree left;
	EarlyTerminatingTree right;
	public int size;
	public EarlyTerminatingTree(HashSet<Production> rules, HashSet<Production> terminatingRules, String s) {
		this.rules = rules;
		this.partitionSymbol = s;
		this.terminatingRules = terminatingRules;
	}
	public EarlyTerminatingTree(int depth, List<String> indexing, HashSet<Production> rules) {
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
				left = new EarlyTerminatingTree(depth+1, indexing, leftRules);
			}
			if(rightRules.size() != 0) {
				right = new EarlyTerminatingTree(depth+1, indexing, rightRules);
			}
		} else {
			return;
		}
	}
	public static void filter(EarlyTerminatingTree root, HashSet<Production> activeRules, HashSet<String> filteredTerminals, int deepestLevel) {
		continuations = new LinkedList<>();
		int initialDepth = 0;
		continuations.add(new Object[] {initialDepth, root});
		while(continuations.size() > 0) {
			Object[] continuation = continuations.remove(0);
			int depth = (int) continuation[0];
			EarlyTerminatingTree currentNode = (EarlyTerminatingTree) continuation[1];
			String sym = currentNode.partitionSymbol;
			
			if(depth > deepestLevel) { 
				// take all the rules
				activeRules.addAll(currentNode.rules);
				activeRules.addAll(currentNode.terminatingRules);
			} else {
				if(filteredTerminals.contains(sym)) {
					if(currentNode.right != null) {
						continuations.add(new Object[] {depth+1, currentNode.right});
					}
				} else {
					if(currentNode.rules.size() == 1) {
						// is leaf or past last filtered symbol
						currentNode.filterProductions(activeRules, filteredTerminals);
					} else {
						activeRules.addAll(currentNode.terminatingRules);
						if(currentNode.right != null) {
							continuations.add(new Object[] {depth+1, currentNode.right});
						}
						if(currentNode.left != null) {
							continuations.add(new Object[] {depth+1, currentNode.left});
						}
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
					break;
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
	static HashMap<Integer, EarlyTerminatingTree> treeIdxs;
	
	
	public static EarlyTerminatingTree buildTree(List<String> indexing, HashSet<Production> filterableProductions) {
		EarlyTerminatingTree.indexing = indexing;
		EarlyTerminatingTree.filterableProductions = filterableProductions;
		EarlyTerminatingTree.treeIdxs = new HashMap<>();
		continuations = new LinkedList<>();
		continuations.add(new Object[] {0, 1, filterableProductions, new HashSet<Production>()});
		int i = 0;
		while(continuations.size() > 0) {
			Object[] continuation = continuations.remove(0);
			int depth = (int) continuation[0];
			int index = (int) continuation[1];
			i++;
			HashSet<Production> rules = (HashSet<Production>) continuation[2];
			HashSet<Production> leftRulesTerminating = new HashSet<Production>();

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
					Iterator<Production> ruleIter = rules.iterator();
					while(ruleIter.hasNext()) {
						Production p = ruleIter.next();
						if(p.terminals.contains(partitionSymbol)) {
							if(p.deepestSymbol.equals(partitionSymbol)) {
								leftRulesTerminating.add(p);
								ruleIter.remove();
							} else {
								leftRules.add(p);
							}
						} else {
							rightRules.add(p);
						}
					}
					if(rightRules.size() != 0) {
						Object[] rightContinuation = new Object[]  {childDepth, rightChildIdx, rightRules};
						continuations.add(rightContinuation);
					}
					if(leftRules.size() != 0) {
						Object[] leftContinuation = new Object[] {childDepth, leftChildIdx, leftRules};
						continuations.add(leftContinuation);
					}
					
				}
			}

			
			EarlyTerminatingTree t = new EarlyTerminatingTree(rules, leftRulesTerminating, partitionSymbol);
			treeIdxs.put(index, t);
		}
		
		
		for(Integer idx : treeIdxs.keySet()) {
			EarlyTerminatingTree t = treeIdxs.get(idx);
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
