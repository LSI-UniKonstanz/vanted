package org.vanted.plugins.layout.multilevelframework;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graph.AdjListNode;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A {@link Node} implementation that represents several nodes in a coarsening level within {@link MultilevelGraph}.
 *
 * @see org.graffiti.graph.AdjListNode
 * @see Node
 */
public class MergedNode extends AdjListNode {
	private Set<Node> nodes;
	private int weight;
	
	/**
	 * Create a new {@link MergedNode}.
	 *
	 * @param g
	 *           The {@link Graph} that contains this node. Must not be {@code null}.
	 * @param nodes
	 *           The {@link Node}s represented by this node. Must not be {@code null}.
	 *           Note that the collection will not be copied, but is used directly by this instance.
	 * @author Gordian
	 * @see AdjListNode#AdjListNode(Graph)
	 */
	public MergedNode(Graph g, Set<Node> nodes) {
		super(Objects.requireNonNull(g, "MergedNode graph must not be null."));
		this.nodes = Objects.requireNonNull(nodes, "MergedNode nodes must not be null.");
		this.updateLabel();
		this.updatePositionAndSize();
		this.updateWeight();
	}
	
	/**
	 * Create a new {@link MergedNode}. It is preferable to use the other constructor and refrain from using
	 * {@link MergedNode#addInnerNode(Node)}.
	 *
	 * @param g
	 *           The {@link Graph} that contains this node. Must not be {@code null}.
	 * @author Gordian
	 * @see AdjListNode#AdjListNode(Graph, CollectionAttribute)
	 */
	@Deprecated
	public MergedNode(Graph g, CollectionAttribute col) {
		super(Objects.requireNonNull(g), Objects.requireNonNull(col));
		this.nodes = new HashSet<>();
		this.updateLabel();
		this.updatePositionAndSize();
		this.updateWeight();
	}
	
	/**
	 * Create a new {@link MergedNode}. It is preferable to use the other constructor and refrain from using
	 * {@link MergedNode#addInnerNode(Node)}.
	 *
	 * @param g
	 *           The {@link Graph} that contains this node. Must not be {@code null}.
	 * @author Gordian
	 * @see AdjListNode#AdjListNode(Graph)
	 */
	@Deprecated
	public MergedNode(Graph g) {
		super(g);
		this.nodes = new HashSet<>();
		this.updateLabel();
		this.updatePositionAndSize();
		this.updateWeight();
	}
	
	/**
	 * @return the {@link Node}s represented by this {@link MergedNode}.
	 *         The returned value must not be modified (see {@link MergedNode#addInnerNode(Node)}).
	 * @author Gordian
	 */
	public Collection<? extends Node> getInnerNodes() {
		return Collections.unmodifiableCollection(this.nodes);
	}
	
	/**
	 * Use this method to add a {@link Node} to the collection of nodes represented by this
	 * {@link MergedNode}.
	 * It is better to add all inner nodes using the constructor {@link MergedNode#MergedNode(Graph, Set)},
	 * because otherwise the position has to be recalculated on each change.
	 *
	 * @param node
	 *           The {@link Node} to be added. Must not be {@code null}. Must not be already contained in this
	 *           {@link MergedNode} (i.e. not in {@link MergedNode#getInnerNodes()}).
	 * @author Gordian
	 */
	@Deprecated
	public void addInnerNode(Node node) {
		if (this.nodes.contains(node)) {
			throw new IllegalArgumentException("MergedNode must not contain the same node twice.");
		}
		this.nodes.add(Objects.requireNonNull(node, "MergedNode cannot represent null nodes."));
		this.updateLabel();
		this.updatePositionAndSize();
		this.updateWeight();
	}
	
	/**
	 * @return the number of nodes that the {@link MergedNode} represents (other {@link MergedNode}s are not counted,
	 *         just the notes that they in turn represent)
	 * @author Gordian
	 */
	public int getWeight() {
		return this.weight;
	}
	
	/**
	 * Calculate the number of nodes that the {@link MergedNode} represents (other {@link MergedNode}s are not counted,
	 * just the notes that they in turn represent)
	 * 
	 * @author Gordian
	 */
	private void updateWeight() {
		if (this.getInnerNodes().isEmpty()) {
			this.weight = 0;
		}
		int sum = 0;
		for (Node n : this.nodes) {
			if (n instanceof MergedNode) {
				sum += ((MergedNode) n).getWeight();
			} else {
				sum += 1;
			}
		}
		this.weight = sum;
	}
	
	/**
	 * Set the position of this {@link MergedNode} to the center of the represented nodes
	 * (i.e. the value returned by {@link NodeTools#getCenter(Collection)})
	 * In case the returned center contains {@code NaN} values (see {@link Double#isNaN(double)}) or there are no
	 * represented nodes the center will be set to the origin (0,0).
	 *
	 * @author Gordian, Katze
	 */
	private void updatePositionAndSize() {
		if (this.nodes.isEmpty()) {
			AttributeHelper.setPosition(this, 0, 0);
			return;
		}
		Vector2d center = NodeTools.getCenter(this.getInnerNodes());
		// note that isFinite returns false for NaN as well as for INFINITY
		if (!Double.isFinite(center.x) || !Double.isFinite(center.y)) {
			center = new Vector2d(0, 0);
		}
		AttributeHelper.setPosition(this, center);
		
		double maxdiam = 0.0;
		
		for (Node i : nodes) {
			if (AttributeHelper.getHeight(i) > maxdiam) {
				maxdiam = AttributeHelper.getHeight(i);
			}
			if (AttributeHelper.getWidth(i) > maxdiam) {
				maxdiam = AttributeHelper.getWidth(i);
			}
		}
		
		double size = Math.ceil(Math.sqrt(nodes.size())) * maxdiam * 1.1;
		AttributeHelper.setHeight(this, size);
		AttributeHelper.setWidth(this, size);
	}
	
	/**
	 * Create a label that contains information about the nodes represented by this {@link MergedNode}.
	 * The label is set using {@link AttributeHelper}.
	 *
	 * @author Gordian, Tobias
	 */
	private void updateLabel() {
		String label = this.nodes.stream()
				.map(n -> {
					if (n instanceof MergedNode) {
						return "[" + ((MergedNode) n).getInnerNodes().size() + "]";
					} else {
						return AttributeHelper.getLabel(n, "unknown");
					}
				})
				.collect(Collectors.joining(", "));
		label += " | [" + this.nodes.size() + "]";
		AttributeHelper.setLabel(this, label);
	}
}
