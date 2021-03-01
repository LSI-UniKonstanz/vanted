package org.vanted.plugins.layout.multilevelframework;

import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graph.*;
import org.vanted.indexednodes.IndexedComponent;

import java.util.*;

/**
 * Represents a single level of a {@link MultilevelGraph}.
 */
class LevelGraph extends AdjListGraph implements CoarsenedGraph {
	/**
	 * Store "object attributes" associated with this level. (See {@link
	 * LevelGraph#setObject(String, Object)}). Lazily initialized as it is not always needed.
	 */
	private HashMap<String, Object> objectAttributes;
	
	/**
	 * Construct a new LevelGraph from the given component.
	 *
	 * @param component
	 * @return
	 */
	public static LevelGraph fromIndexedComponent(IndexedComponent component) {
		LevelGraph graph = new LevelGraph();
		// will need references to new mergedNodes to create edges in the new graph.
		Map<Integer, MergedNode> index2NewNode = new Hashtable<>();
		for (int node : component.nodes) {
			MergedNode mnode = new MergedNode(graph,
					Collections.singleton(component.nodes.get(node)));
			graph.doAddNode(mnode);
			index2NewNode.put(node, mnode);
		}
		for (int[] edge : component.edges) {
			graph.doAddEdge(
					index2NewNode.get(edge[0]),
					index2NewNode.get(edge[1]),
					false);
		}
		return graph;
	}
	
	/**
	 * This method just calls the superclass' method with the same signature. Note that {@code
	 * false} must be passed to the {@code directed} parameter.
	 *
	 * @author Gordian
	 * @see AdjListGraph#doAddEdge(Node, Node, boolean)
	 */
	@Override
	public Edge doAddEdge(Node source, Node target, boolean directed) {
		this.checkUndirected(directed);
		return super.doAddEdge(source, target, false);
	}
	
	/**
	 * This method just calls the superclass' method with the same signature. Note that {@code
	 * false} must be passed to the {@code directed} parameter.
	 *
	 * @author Gordian
	 * @see AdjListGraph#doAddEdge(Node, Node, boolean, CollectionAttribute)
	 */
	@Override
	protected Edge doAddEdge(Node source, Node target, boolean directed, CollectionAttribute col) {
		this.checkUndirected(directed);
		return super.doAddEdge(source, target, false, col);
	}
	
	/**
	 * This method just calls the superclass' method with the same signature.
	 *
	 * @param node
	 *           Must be an instance of {@link MergedNode} and must not be {@code null}.
	 * @author Gordian
	 * @see AdjListGraph#doAddNode(Node)
	 */
	@Override
	public void doAddNode(Node node) {
		this.checkMergedNode(node);
		super.doAddNode(node);
	}
	
	/**
	 * Create a new {@link MergedNode}.
	 *
	 * @return the new {@link MergedNode}
	 * @author Gordian
	 * @see AdjListGraph#createNode()
	 */
	@Override
	protected MergedNode createNode() {
		this.setModified(true);
		return new MergedNode(this);
	}
	
	/**
	 * Create a new {@link MergedNode} with the given {@link CollectionAttribute}.
	 *
	 * @return the new {@link MergedNode}
	 * @author Gordian
	 * @see AdjListGraph#createNode(CollectionAttribute)
	 */
	@Override
	public Node createNode(CollectionAttribute col) {
		this.setModified(true);
		return new MergedNode(this, col);
	}
	
	/**
	 * Create a new {@link MergedNode} representing the given nodes.
	 *
	 * @param nodes
	 *           {@link Node}s to be represented. This parameter must not be {@code null}.
	 * @return The newly created {@link MergedNode}.
	 * @author Gordian
	 */
	public MergedNode createNode(Set<Node> nodes) {
		this.setModified(true);
		MergedNode res = new MergedNode(this, nodes);
		return res;
	}
	
	/**
	 * This just calls the superclass' method of the same signature, but makes sure that the edge is
	 * undirected.
	 *
	 * @param directed
	 *           Must be {@code false}.
	 * @author Gordian
	 * @see AdjListGraph#createEdge(Node, Node, boolean)
	 */
	@Override
	public Edge createEdge(Node source, Node target, boolean directed) {
		this.checkUndirected(directed);
		return super.createEdge(source, target, false);
	}
	
	/**
	 * This just calls the superclass' method of the same signature, but makes sure that the edge is
	 * undirected.
	 *
	 * @param directed
	 *           Must be {@code false}.
	 * @author Gordian
	 * @see AdjListGraph#createEdge(Node, Node, boolean, CollectionAttribute)
	 */
	@Override
	protected Edge createEdge(Node source, Node target, boolean directed, CollectionAttribute col) {
		this.checkUndirected(directed);
		return super.createEdge(source, target, false, col);
	}
	
	/**
	 * {@link LevelGraph}'s are always undirected.
	 *
	 * @return {@code false}.
	 * @author Gordian
	 */
	@Override
	public boolean isDirected() {
		return false;
	}
	
	/**
	 * {@link LevelGraph}'s are always undirected.
	 *
	 * @param directed
	 *           Must be {@code false}.
	 * @author Gordian
	 */
	@Override
	public void setDirected(boolean directed) {
		this.checkUndirected(directed);
		super.setDirected(false);
	}
	
	/**
	 * {@link LevelGraph}'s are always undirected.
	 *
	 * @param directed
	 *           Must be {@code false}.
	 * @author Gordian
	 * @see AdjListGraph#setDirected(boolean, boolean)
	 */
	@Override
	public void setDirected(boolean directed, boolean adjustArrows) {
		this.checkUndirected(directed);
		super.setDirected(false, adjustArrows);
	}
	
	/**
	 * @param g
	 *           Must be a undirected {@link Graph}.
	 * @author Gordian
	 * @see AdjListGraph#addGraph(Graph)
	 */
	@Override
	public Collection<GraphElement> addGraph(Graph g) {
		this.checkUndirected(g.isDirected());
		if (g.getNodes().stream().anyMatch(n -> !(n instanceof MergedNode))) {
			throw new IllegalArgumentException("Levels of MultilevelGraph must only consist of MergedNodes.");
		}
		return super.addGraph(g);
	}
	
	/**
	 * @param node
	 *           Must be an instance of {@link MergedNode}. The {@link MergedNode}'s {@link
	 *           MergedNode#getInnerNodes()} method must not return any nodes already represented
	 *           by a {@link MergedNode} of this graph (returned by {@link
	 *           LevelGraph#getNodes()}).
	 * @return The copied node.
	 * @author Gordian
	 */
	@Override
	public Node addNodeCopy(Node node) {
		this.checkMergedNode(node);
		return super.addNodeCopy(node);
	}
	
	/**
	 * @param directed
	 *           {@code true} or {@code false}.
	 * @throws IllegalArgumentException
	 *            if the parameter is {@code true}.
	 * @author Gordian
	 */
	private void checkUndirected(boolean directed) {
		if (directed) {
			throw new IllegalArgumentException("Levels of MultilevelGraph must only contain undirected edges.");
		}
	}
	
	/**
	 * Checks whether {@link Node} is an instance of {@link MergedNode}.
	 *
	 * @param n
	 *           The {@link Node} to be checked.
	 * @author Gordian
	 */
	private void checkMergedNode(Node n) {
		if (!(n instanceof MergedNode)) {
			throw new IllegalArgumentException("Levels of MultilevelGraph must consist only of MergedNodes.");
		}
	}
	
	/**
	 * Checks whether there are multiple {@link MergedNode}s which represent the same nodes in the
	 * underlying graph.
	 *
	 * @param ref
	 *           If not {@code null}, this parameter will also be checked for overlaps with the
	 *           existing nodes.
	 * @author Gordian
	 */
	public boolean checkNoOverlappingMergedNodes(MergedNode ref) {
		final Set<Node> representedNodes = new HashSet<>();
		if (ref != null) {
			representedNodes.addAll(ref.getInnerNodes());
		}
		for (Node n : this.nodes) {
			final MergedNode mn = (MergedNode) n;
			if (mn.getInnerNodes().stream().anyMatch(representedNodes::contains)) {
				return false;
			}
			representedNodes.addAll(mn.getInnerNodes());
		}
		return true;
	}
	
	/**
	 * @author D. Garkov
	 * @see CoarsenedGraph#getMergedNodes()
	 */
	@Override
	public Collection<MergedNode> getMergedNodes() {
		Collection<MergedNode> mergedNodes = new LinkedList<>();
		for (Node node : this.nodes) {
			if (node instanceof MergedNode) {
				mergedNodes.add((MergedNode) node);
			}
		}
		
		return mergedNodes;
	}
	
	/**
	 * Add an object attribute to the graph. Works similar to the attributes in {@link
	 * org.graffiti.attributes.Attributable}.
	 *
	 * @param key
	 *           The key at which the object is stored. Must not be {@code null}.
	 * @param value
	 *           The value to store. Must not be {@code null}.
	 * @author Gordian
	 */
	public void setObject(String key, Object value) {
		if (this.objectAttributes == null) {
			this.objectAttributes = new HashMap<>();
		}
		this.objectAttributes.put(Objects.requireNonNull(key), Objects.requireNonNull(value));
	}
	
	/**
	 * @param key
	 *           The key to retrieve. If this is {@code null}, {@link Optional#empty()} will be
	 *           returned.
	 * @return The object stored at {@code key}, as an {@link Optional}. This method will never
	 *         return {@code null}.
	 * @author Gordian
	 */
	public Optional<Object> getObject(String key) {
		if (this.objectAttributes == null || key == null || !this.objectAttributes.containsKey(key)) {
			return Optional.empty();
		}
		return Optional.of(this.objectAttributes.get(key));
	}
}
