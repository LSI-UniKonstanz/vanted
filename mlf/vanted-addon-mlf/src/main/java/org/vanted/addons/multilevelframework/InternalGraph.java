package org.vanted.addons.multilevelframework;

import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graph.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A subclass of {@link AdjListGraph} used by the {@link MultilevelGraph} implementation.
 * It cannot use {@link AdjListGraph} since some of the functionality it needs is not provided by
 * {@link AdjListGraph}'s public interface.
 */
class InternalGraph extends AdjListGraph {
    /**
     * This method just calls the superclass' method with the same signature.
     * Note that {@code false} must be passed to the {@code directed} parameter.
     * @see AdjListGraph#doAddEdge(Node, Node, boolean)
     */
    @Override
    public Edge doAddEdge(Node source, Node target, boolean directed) {
        this.checkUndirected(directed);
        return super.doAddEdge(source, target, false);
    }

    /**
     * This method just calls the superclass' method with the same signature.
     * Note that {@code false} must be passed to the {@code directed} parameter.
     * @see AdjListGraph#doAddEdge(Node, Node, boolean, CollectionAttribute)
     */
    @Override
    protected Edge doAddEdge(Node source, Node target, boolean directed, CollectionAttribute col) {
        this.checkUndirected(directed);
        return super.doAddEdge(source, target, false, col);
    }

    /**
     * This method just calls the superclass' method with the same signature.
     * @param node
     *     Must be an instance of {@link MergedNode} and must not be {@code null}.
     * @see AdjListGraph#doAddNode(Node)
     */
    @Override
    public void doAddNode(Node node) {
        this.checkMergedNode(node);
        assert this.checkNoOverlappingMergedNodes((MergedNode) node) :
                "MultilevelGraph cannot contain multiple MergedNodes representing "
                        + "the same nodes in the underlying graph.";
        super.doAddNode(node);
    }

    /**
     * Create a new {@link MergedNode}.
     * @return the new {@link MergedNode}
     * @see AdjListGraph#createNode()
     */
    @Override
    protected MergedNode createNode() {
        this.setModified(true);
        return new MergedNode(this);
    }

    /**
     * Create a new {@link MergedNode} with the given {@link CollectionAttribute}.
     * @return the new {@link MergedNode}
     * @see AdjListGraph#createNode(CollectionAttribute)
     */
    @Override
    public Node createNode(CollectionAttribute col) {
        this.setModified(true);
        return new MergedNode(this, col);
    }

    /**
     * Create a new {@link MergedNode} representing the given nodes.
     * @param nodes
     *     {@link Node}s to be represented. This parameter must not be {@code null}.
     * @return
     *     The newly created {@link MergedNode}.
     */
    public MergedNode createNode(Set<Node> nodes) {
        this.setModified(true);
        MergedNode res = new MergedNode(this, nodes);
        assert this.checkNoOverlappingMergedNodes(res) :
                "MultilevelGraph cannot contain multiple MergedNodes representing "
                        + "the same nodes in the underlying graph.";
        return res;
    }

    /**
     * This just calls the superclass' method of the same signature, but makes sure that the edge is undirected.
     * @param directed
     *     Must be {@code false}.
     * @see AdjListGraph#createEdge(Node, Node, boolean)
     */
    @Override
    public Edge createEdge(Node source, Node target, boolean directed) {
        this.checkUndirected(directed);
        return super.createEdge(source, target, false);
    }

    /**
     * This just calls the superclass' method of the same signature, but makes sure that the edge is undirected.
     * @param directed
     *     Must be {@code false}.
     * @see AdjListGraph#createEdge(Node, Node, boolean, CollectionAttribute)
     */
    @Override
    protected Edge createEdge(Node source, Node target, boolean directed, CollectionAttribute col) {
        this.checkUndirected(directed);
        return super.createEdge(source, target, false, col);
    }

    /**
     * {@link InternalGraph}'s are always undirected.
     * @return {@code false}.
     */
    @Override
    public boolean isDirected() {
        return false;
    }

    /**
     * {@link InternalGraph}'s are always undirected.
     * @param directed Must be {@code false}.
     */
    @Override
    public void setDirected(boolean directed) {
        this.checkUndirected(directed);
        super.setDirected(false);
    }

    /**
     * {@link InternalGraph}'s are always undirected.
     * @param directed Must be {@code false}.
     * @see AdjListGraph#setDirected(boolean, boolean)
     */
    @Override
    public void setDirected(boolean directed, boolean adjustArrows) {
        this.checkUndirected(directed);
        super.setDirected(false, adjustArrows);
    }

    /**
     * @param g Must be a undirected {@link Graph}.
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
     *     Must be an instance of {@link MergedNode}. The {@link MergedNode}'s
     *     {@link MergedNode#getInnerNodes()} method must not return any nodes already
     *     represented by a {@link MergedNode} of this graph (returned by {@link InternalGraph#getNodes()}).
     * @return
     *     The copied node.
     */
    @Override
    public Node addNodeCopy(Node node) {
        this.checkMergedNode(node);
        assert this.checkNoOverlappingMergedNodes((MergedNode) node) :
                "MultilevelGraph cannot contain multiple MergedNodes representing "
                + "the same nodes in the underlying graph.";
        return super.addNodeCopy(node);
    }

    /**
     * @throws IllegalArgumentException if the parameter is {@code true}.
     * @param directed {@code true} or {@code false}.
     */
    private void checkUndirected(boolean directed) {
        if (directed) {
            throw new IllegalArgumentException("Levels of MultilevelGraph must only contain directed nodes.");
        }
    }

    /**
     * Checks whether {@link Node} is an instance of {@link MergedNode}.
     * @param n
     *     The {@link Node} to be checked.
     */
    private void checkMergedNode(Node n) {
        if (!(n instanceof MergedNode)) {
            throw new IllegalArgumentException("Levels of MultilevelGraph must consist only of MergedNodes.");
        }
    }

    /**
     * Checks whether there are multiple {@link MergedNode}s which represent the same nodes in the underlying graph.
     * @param ref
     *    If not {@code null}, this parameter will also be checked for overlaps with the existing nodes.
     */
    public boolean checkNoOverlappingMergedNodes(MergedNode ref) {
            final Set<Node> representedNodes = new HashSet<>();
            if (ref != null) {
                representedNodes.addAll(ref.getInnerNodes());
            }
            for (Node n : this.nodes) {
                final MergedNode mn = (MergedNode) n;
                if (mn.getInnerNodes().stream().anyMatch(representedNodes::contains)) {
                    return  false;
                }
                representedNodes.addAll(mn.getInnerNodes());
            }
            return true;
    }
}
