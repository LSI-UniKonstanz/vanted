package org.vanted.addons.multilevelframework;

import org.AttributeHelper;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graph.AdjListNode;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A {@link Node} implementation that represents several nodes in a coarsening level within {@link MultilevelGraph}.
 * @see org.graffiti.graph.AdjListNode
 * @see Node
 */
public class MergedNode extends AdjListNode {
    private Set<Node> nodes;

    /**
     * Create a new {@link MergedNode}.
     * @param g
     *     The {@link Graph} that contains this node. Must not be {@code null}.
     * @param nodes
     *     The {@link Node}s represented by this node. Must not be {@code null}.
     *     Note that the collection will not be copied, but is used directly by this instance.
     * @see AdjListNode#AdjListNode(Graph)
     * @author Gordian
     */
    public MergedNode(Graph g, Set<Node> nodes) {
        super(Objects.requireNonNull(g, "MergedNode graph must not be null."));
        this.nodes = Objects.requireNonNull(nodes, "MergedNode nodes must not be null.");
        this.updateLabel();
    }

    /**
     * Create a new {@link MergedNode}.
     * @param g
     *     The {@link Graph} that contains this node. Must not be {@code null}.
     * @see AdjListNode#AdjListNode(Graph, CollectionAttribute)
     * @author Gordian
     */
    public MergedNode(Graph g, CollectionAttribute col) {
        super(Objects.requireNonNull(g), Objects.requireNonNull(col));
        this.nodes = new HashSet<>();
        this.updateLabel();
    }

    /**
     * Create a new {@link MergedNode}.
     * @param g
     *     The {@link Graph} that contains this node. Must not be {@code null}.
     * @see AdjListNode#AdjListNode(Graph)
     * @author Gordian
     */
    public MergedNode(Graph g) {
        super(g);
        this.nodes = new HashSet<>();
        this.updateLabel();
    }

    /**
     * @return
     *    the {@link Node}s represented by this {@link MergedNode}.
     *    The returned value must not be modified (see {@link MergedNode#addInnerNode(Node)}).
     * @author Gordian
     */
    public Collection<?extends Node> getInnerNodes() {
        return Collections.unmodifiableCollection(this.nodes);
    }

    /**
     * Use this method to add a {@link Node} to the collection of nodes represented by this
     * {@link MergedNode}.
     * @param node
     *     The {@link Node} to be added. Must not be {@code null}. Must not be already contained in this
     *     {@link MergedNode} (i.e. not in {@link MergedNode#getInnerNodes()}).
     * @author Gordian
     */
    public void addInnerNode(Node node) {
        if (this.nodes.contains(node)) {
            throw new IllegalArgumentException("MergedNode must not contain the same node twice.");
        }
        this.nodes.add(Objects.requireNonNull(node, "MergedNode cannot represent null nodes."));
        this.updateLabel();
    }

    /**
     * Create a label that contains information about the nodes represented by this {@link MergedNode}.
     * The label is set using {@link AttributeHelper}.
     * @author Gordian, Tobias
     */
    private void updateLabel() {
        // TODO: maybe display a nicer label if the represented nodes are unlabeled
        String label = this.nodes.stream()
                .map(n -> AttributeHelper.getLabel(n, "unknown"))
                .collect(Collectors.joining(", "));
        label += " [" + this.nodes.size() + "]";
        AttributeHelper.setLabel(this, label);
    }
}
