package org.vanted.plugins.layout.multilevelframework;

import org.graffiti.attributes.*;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.selection.Selection;

import java.util.*;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Helper methods for the multilevel framework add-on.
 */
public enum MlfHelper {
	; // no instances needed
	
	/**
	 * Calculate the subgraph induced by the selection and split it up into its connected components.
	 * Note that this method is <emph>heavily</emph> inspired by
	 * {@link de.ipk_gatersleben.ag_nw.graffiti.GraphHelper#getConnectedComponentsAsCopy(List)}, which
	 * cannot be used directly by the MLF because it doesn't calculate the induced subgraph.
	 *
	 * @param selection
	 *           The {@link Selection}. Must not be {@code null}.
	 * @return The connected components of the subgraph in induced by the selection, as {@link CoarsenedGraph}s.
	 */
	public static List<? extends CoarsenedGraph> calculateConnectedComponentsOfSelection(Set<Node> selection) {
		long startTime = System.nanoTime();
		if (selection.size() <= 0) {
			return Collections.emptyList();
		}
		
		ArrayList<LevelGraph> graphList = new ArrayList<>();
		HashMap<Node, Node> sourceGraphNode2connectedGraphNode = new HashMap<>();
		while (!selection.isEmpty()) {
			Node startNode = selection.iterator().next();
			Set<Node> connectedNodes = getConnectedNodes(startNode, selection);
			LevelGraph connectedComponentGraph = new LevelGraph();
			for (Node n : connectedNodes) {
				MergedNode newNode = new MergedNode(connectedComponentGraph, Collections.singleton(n));
				connectedComponentGraph.doAddNode(newNode);
				sourceGraphNode2connectedGraphNode.put(n, newNode);
			}
			for (Node n : connectedNodes) {
				for (Edge e : n.getEdges()) {
					if (connectedNodes.contains(e.getSource()) && connectedNodes.contains(e.getTarget())) {
						connectedComponentGraph.doAddEdge(sourceGraphNode2connectedGraphNode.get(e.getSource()),
								sourceGraphNode2connectedGraphNode.get(e.getTarget()), false);
					}
				}
			}
			graphList.add(connectedComponentGraph);
			selection.removeAll(connectedNodes);
		}
		long endTime = System.nanoTime();
		
		System.out.println("Built connected components in: " + NANOSECONDS.toMillis(endTime - startTime) + " ms.");
		return graphList;
	}
	
	/**
	 * Note that this method is <emph>heavily</emph> inspired by
	 * {@link de.ipk_gatersleben.ag_nw.graffiti.GraphHelper#getConnectedNodes(Node)}, which we cannot use directly
	 * since it doesn't consider the selection.
	 *
	 * @param startNode
	 *           The start node from which to find all connected nodes.
	 * @param selection
	 *           The nodes to consider.
	 * @return All the nodes reachable from {@code startNode} that are contained in {@code selection}.
	 *         The {@code startNode} is always contained in the result.
	 * @see de.ipk_gatersleben.ag_nw.graffiti.GraphHelper#getConnectedNodes(Node)
	 */
	static Set<Node> getConnectedNodes(Node startNode, Set<Node> selection) {
		List<Node> stack = new ArrayList<>();
		Set<Node> hashSet = new HashSet<>();
		
		stack.add(startNode);
		hashSet.add(startNode);
		
		while (!stack.isEmpty()) {
			Iterator<?> neighbours = stack.remove(stack.size() - 1).getNeighborsIterator();
			
			while (neighbours.hasNext()) {
				Node neighbour = (Node) neighbours.next();
				
				// check if the node is actually in the selection
				if (!hashSet.contains(neighbour) && selection.contains(neighbour)) {
					hashSet.add(neighbour);
					stack.add(neighbour);
				}
			}
		}
		
		return hashSet;
	}
	
	/**
	 * Note that edges with no weight will be considered to have a default weight of 0.
	 * 
	 * @param weightPath
	 *           The path of the weight attribute (see {@link MlfHelper#getEdgeWeight(Edge, String, double)}).
	 * @return a {@link Comparator} that compares {@link Edge}s according to their weight.
	 * @author Gordian
	 */
	static Comparator<Edge> createEdgeWeightComparator(String weightPath) {
		return Comparator.comparingDouble(e -> getEdgeWeight(e, weightPath, 0));
	}
	
	/**
	 * Get the edge weight.
	 * 
	 * @param e
	 *           The {@link Edge} to get the weight from. Must not be {@code null}.
	 * @param weightPath
	 *           The path of the weight attribute. Not that the attribute must either be an {@link IntegerAttribute},
	 *           {@link LongAttribute}, {@link FloatAttribute} or {@link DoubleAttribute}.
	 * @param defaultValue
	 *           The default value to return if the attribute is not present or doesn't meet the above conditions.
	 * @return
	 *         The weight (as a double) or the {@code defaultValue} if the weight could not be obtained.
	 * @author Gordian
	 */
	static double getEdgeWeight(Edge e, String weightPath, double defaultValue) {
		final Map<String, Attribute> attributes = e.getAttributes().getCollection();
		if (!attributes.containsKey(weightPath)) {
			return defaultValue;
		}
		final Attribute a = attributes.get(weightPath);
		if (a instanceof DoubleAttribute) {
			return ((DoubleAttribute) a).getDouble();
		}
		if (a instanceof IntegerAttribute) {
			return ((IntegerAttribute) a).getInteger();
		}
		if (a instanceof FloatAttribute) {
			return ((FloatAttribute) a).getFloat();
		}
		if (a instanceof LongAttribute) {
			return ((LongAttribute) a).getLong();
		}
		return defaultValue;
	}
	
	/**
	 * Check whether a number is finite and in range. Display an error message if it is not.
	 * 
	 * @param number
	 *           The number to check.
	 * @param minValue
	 *           The (exclusive) minimum value.
	 * @param maxValue
	 *           The (exclusive) maximum value.
	 * @param name
	 *           The name of the corresponding parameter.
	 * @author Gordian
	 */
	public static void validateNumber(double number, double minValue, double maxValue, String name) {
		if (!(number > minValue && number < maxValue && Double.isFinite(number))) {
			MainFrame.getInstance().showMessageDialog("The value for \"" + name + "\" is out of range. " +
					"You passed in \"" + number + "\", but the value must be in the range (" + minValue + ", " +
					maxValue + ").");
			throw new IllegalArgumentException("Invalid number passed to random merger.");
		}
	}
	
	/**
	 * Create new instances of the layouters to avoid interfering with the ones that VANTED holds.
	 * This is useful, for example if the MLF runs on multiple graphs at the same time but the
	 * algorithms/mergers/placers it uses aren't thread safe.
	 * If a new instance cannot be created, there's nothing that can be done except reusing the old one.
	 * 
	 * @param t
	 *           The object to duplicate.
	 * @param <T>
	 *           The class of the object to duplicate.
	 * @return
	 *         The duplicated object, or the old one if {@link Class#newInstance()} throws an exception.
	 * @author Gordian
	 */
	@SuppressWarnings("unchecked")
	public static <T> T tryMakingNewInstance(T t) {
		final Class<? extends Object> clazz = t.getClass();
		try {
			return (T) clazz.newInstance();
		} catch (IllegalAccessException | InstantiationException e) {
			System.err.println("Failed to create new instance of " + t.getClass().getName());
			return t;
		}
	}
}
