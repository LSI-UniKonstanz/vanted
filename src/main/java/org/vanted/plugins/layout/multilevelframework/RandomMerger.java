package org.vanted.plugins.layout.multilevelframework;

import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.*;
import org.vanted.plugins.layout.multilevelframework.Describable;

import java.util.*;

import static org.vanted.plugins.layout.multilevelframework.MlfHelper.validateNumber;

/**
 * {@link Merger} that randomly merges edges. Note that the default setting is not to merge completely randomly.
 * Starting from the second level, the {@link RandomMerger} will prefer merging {@link MergedNode}s with low weight.
 * This can be changed through the setting {@link RandomMerger#useWeights}.
 */
public class RandomMerger implements Merger {
	
	final static String COARSENING_FACTOR_NAME = "Coarsening Factor";
	final static String MIN_LEVEL_NODE_NUM_NAME = "Minimum number of nodes per level";
	final static String MAX_NAM_ITERATIONS_NAME = "Maximum number of iterations";
	final static String USE_WEIGHTS_NAME = "Use merged-node weights";
	final static String CONSIDER_EDGE_WEIGHTS_NAME = "Consider edge weights";
	final static String WEIGHT_ATTR_PATH_NAME = "Weight attribute path";
	
	// Variables containing the parameter values
	
	// the ratio in size of the baseLevel and the resulting coarsening level
	double coarseningFactor = 0.5;
	
	// the minimal number of nodes for a coarsening level
	int minNumberOfNodesPerLevel = 20;
	
	// the maximum amount of levels for one multilevelGraph
	int maxNumberOfIterations = 100;
	
	// prefer merging MergedNodes that don't already represent lots of nodes
	boolean useWeights = true;
	
	// the path at which edge weights are stored see MlfHelper.getEdgeWeight
	String weightAttributePath = "weight";
	
	// if this is true, the merger will prefer merging edges with low weight
	boolean considerEdgeWeights = false;
	
	Comparator<Edge> edgeWeightComparator = MlfHelper.createEdgeWeightComparator(weightAttributePath);
	
	/**
	 * Array of parameters that will be displayed in the GUI.
	 */
	private Parameter[] parameters = {
			// VANTED parameters only has 3 relevant digits
			new DoubleParameter(coarseningFactor, 0.001, 0.999, COARSENING_FACTOR_NAME,
					"The random merger will aim to reduce the number of nodes in each level by this factor."
							+ " It must be between 0 and 1."),
			new IntegerParameter(minNumberOfNodesPerLevel, 0, Integer.MAX_VALUE, MIN_LEVEL_NODE_NUM_NAME,
					"The minimum number of nodes per level. If there are less nodes than this number on "
							+ "a level, the random merger will terminate."),
			new IntegerParameter(maxNumberOfIterations, 0, Integer.MAX_VALUE, MAX_NAM_ITERATIONS_NAME,
					"The random merger will stop after this number of iterations, regardless of whether the"
							+ " other termination criteria are met."),
			new BooleanParameter(useWeights, USE_WEIGHTS_NAME,
					"If this parameter is set, the random merger will prefer merging the nodes that " +
							"represent the least amount of nodes of the original graph."),
			new BooleanParameter(considerEdgeWeights, CONSIDER_EDGE_WEIGHTS_NAME,
					"If this parameter is set, the random merger will prefer merging edges with low " +
							"weight. Note that this only applies to the original graph. Also note that you need to " +
							"set the name of the attribute yourself since there is no standard name for it."),
			new StringParameter(weightAttributePath, WEIGHT_ATTR_PATH_NAME,
					"This is the attribute path that will be used to obtain the edge weight."),
	};
	
	/**
	 * @see Merger#getParameters()
	 * @author Gordian
	 */
	@Override
	public Parameter[] getParameters() {
		return this.parameters;
	}
	
	/**
	 * @param parameters
	 *           The updated {@link Parameter}.
	 * @see Merger#setParameters(Parameter[])
	 * @author Gordian
	 */
	@Override
	public void setParameters(Parameter[] parameters) {
		this.parameters = parameters;
		for (Parameter parameter : parameters) {
			switch (parameter.getName()) {
				case COARSENING_FACTOR_NAME:
					final double coarseningFactor = ((DoubleParameter) parameter).getDouble();
					validateNumber(coarseningFactor, 0, 1, COARSENING_FACTOR_NAME);
					this.coarseningFactor = coarseningFactor;
					break;
				case MIN_LEVEL_NODE_NUM_NAME:
					final int minNumberOfNodesPerLevel = ((IntegerParameter) parameter).getInteger();
					validateNumber(minNumberOfNodesPerLevel, 0, Integer.MAX_VALUE, MIN_LEVEL_NODE_NUM_NAME);
					this.minNumberOfNodesPerLevel = minNumberOfNodesPerLevel;
					break;
				case MAX_NAM_ITERATIONS_NAME:
					final int maxNumberOfIterations = ((IntegerParameter) parameter).getInteger();
					validateNumber(maxNumberOfIterations, 1, Integer.MAX_VALUE, MAX_NAM_ITERATIONS_NAME);
					this.maxNumberOfIterations = maxNumberOfIterations;
					break;
				case USE_WEIGHTS_NAME:
					this.useWeights = ((BooleanParameter) parameter).getBoolean();
					break;
				case CONSIDER_EDGE_WEIGHTS_NAME:
					this.considerEdgeWeights = ((BooleanParameter) parameter).getBoolean();
					break;
				case WEIGHT_ATTR_PATH_NAME:
					this.weightAttributePath = ((StringParameter) parameter).getString();
					break;
				default:
					throw new IllegalStateException("Invalid parameter name passed to random merger.");
			}
		}
		
		// update comparator to use the new path
		this.edgeWeightComparator = MlfHelper.createEdgeWeightComparator(weightAttributePath);
	}
	
	/**
	 * Builds the coarsening levels for the graph depending on parameters. The parameters
	 * {@link RandomMerger#coarseningFactor}, {@link RandomMerger#minNumberOfNodesPerLevel},
	 * {@link RandomMerger#maxNumberOfIterations}, {@link RandomMerger#useWeights},
	 * {@link RandomMerger#considerEdgeWeights} affect the coarsening process.
	 * Consult the comments above their declaration for more information.
	 *
	 * @param multilevelGraph
	 *           The multilevelGraph which the coarsening is performed on.
	 *           The {@link MultilevelGraph} that contains the original graph.
	 * @author Tobias
	 */
	public void buildCoarseningLevels(MultilevelGraph multilevelGraph) {
		
		// checks whether the coarseningFactor is in Range and the graph component does contain multiple edges.
		// no edges leave inhibit any coarsening while merged on the last edge
		if ((multilevelGraph.getTopLevel().getNodes().size() > minNumberOfNodesPerLevel)
				&& (0 < coarseningFactor) && (coarseningFactor < 1)) {
			// calls upon build level to create multiple levels
			for (int i = 0; i < this.maxNumberOfIterations; i++) {
				buildLevel(this.coarseningFactor, multilevelGraph, i != 0 && this.useWeights);
				if (multilevelGraph.getTopLevel().getNumberOfNodes() <= this.minNumberOfNodesPerLevel) {
					break;
				}
			}
		}
		
	}
	
	/**
	 * @see Describable#getName()
	 * @author Gordian
	 */
	@Override
	public String getName() {
		return "Random Merger";
	}
	
	/**
	 * @see Describable#getDescription()
	 * @author Gordian
	 */
	@Override
	public String getDescription() {
		return "Merges edges randomly. Also has the ability to prefer edges which aren't incident to merged nodes that"
				+ "already contain lots of nodes in order to avoid merging too many nodes into one merged node.";
	}
	
	/**
	 * builds one level of a {@link MultilevelGraph} by coarsening the current top level.
	 * the resulting coarsened Level is added as the new top level
	 * 
	 * @param coarseningPerLevel
	 *           the ratio between in sizes of the baseLevel and the resulting coarsened Level
	 * @param mlg
	 *           the {@link MultilevelGraph} receiving an additional coarsening level.
	 * @param sortByWeights
	 *           if {@code true}, sort the edge list by the sum of the nodes' weights and prefer the
	 *           nodes with low weight (note that this requires {@link MergedNode}s)
	 * @author Tobias
	 */
	private void buildLevel(double coarseningPerLevel, MultilevelGraph mlg, boolean sortByWeights) {
		Graph baseLevel = mlg.getTopLevel();
		
		// initializing the new level for the resulting coarsened Graph
		mlg.newCoarseningLevel();
		
		// HashMap mapping originNodes to the Sets representing them on the next higher level.
		HashMap<Node, Set<Node>> node2nodeSet = new HashMap<>();
		
		List<Edge> edges = new ArrayList<>(baseLevel.getEdges());
		
		// maximum amount of Merges per Level limited by the relative number of Nodes and Edges
		int maxMergedEdges = (int) Math.min((edges.size() * coarseningPerLevel),
				(baseLevel.getNumberOfNodes() * coarseningPerLevel));
		
		if (sortByWeights) {
			edges.sort(Comparator.comparing(e -> ((MergedNode) e.getSource()).getWeight() + ((MergedNode) e.getTarget()).getWeight()));
		} else {
			// shuffling the list to obtain random Edges
			Collections.shuffle(edges);
		}
		
		// sort by edge weights if the user enabled this option
		if (this.considerEdgeWeights) {
			edges.sort(this.edgeWeightComparator);
		}
		
		// merging nodes and adding their origins and nodes to node2nodeSet
		for (int i = 0; i < maxMergedEdges; i++) {
			Node source = edges.get(i).getSource();
			Node target = edges.get(i).getTarget();
			
			// if both source and target are already represented, nothing should be done
			if (!node2nodeSet.containsKey(source) || !node2nodeSet.containsKey(target)) {
				if (node2nodeSet.containsKey(source)) {
					// the source node being already in the HashMap
					node2nodeSet.get(source).add(target); // (bm) add target node to set of src node
					node2nodeSet.put(target, node2nodeSet.get(source)); // (bm) set Set of target node to be same contents as set of src node
					// (bm) i.e. both src and tgt node now map to the "same" set
				} else if (node2nodeSet.containsKey(target)) {
					// the target node being already in the HashMap
					node2nodeSet.get(target).add(source);
					node2nodeSet.put(source, node2nodeSet.get(target));
				} else {
					// both the nodes not being in represented in the HashMap
					Set<Node> represented = new HashSet<>();
					represented.add(target);
					represented.add(source);
					node2nodeSet.put(source, represented);
					node2nodeSet.put(target, represented);
				}
			}
		}
		
		// Adding the nodes, which are not altered in this coarsening step together with its origin
		// to node2nodeSet.
		for (int i = maxMergedEdges; i < edges.size(); i++) {
			
			Node source = edges.get(i).getSource();
			Node target = edges.get(i).getTarget();
			
			if (!node2nodeSet.containsKey(source)) {
				// the source node being already in the HashMap
				node2nodeSet.put(source, Collections.singleton(source));
			}
			if (!node2nodeSet.containsKey(target)) {
				// the target node being already in the HashMap
				node2nodeSet.put(target, Collections.singleton(target));
			}
		}
		
		// adding unique mergedNodes to already added and preventing them from being introduces multiple times
		// The origin nodes are mapped to their resulting unique MergedNodes in node2mergedNode
		IdentityHashMap<Set<Node>, Integer> alreadyAdded = new IdentityHashMap<>();
		Map<Node, MergedNode> node2mergedNode = new HashMap<>();
		for (Set<Node> ns : node2nodeSet.values()) {
			//making sure each set appears at most once
			if (!alreadyAdded.containsKey(ns)) {
				alreadyAdded.put(ns, 0);
				// adding all represented Nodes with their resulting MergedNode to node2MergedNode
				MergedNode mergedNode = mlg.addNode(ns);
				for (Node n : ns) {
					node2mergedNode.put(n, mergedNode);
				}
			}
		}
		
		// adding the edges to the new top level
		for (Edge i : edges) {
			MergedNode source = node2mergedNode.get(i.getSource());
			MergedNode target = node2mergedNode.get(i.getTarget());
			// make sure we're not adding a loop or redundant edges
			if (source != target && !source.getNeighbors().contains(target)
					&& !target.getNeighbors().contains(source)) {
				mlg.addEdge(source, target);
			}
		}
	}
}
