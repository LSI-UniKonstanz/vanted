package org.vanted.addons.multilevelframework;

import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graph.*;

import java.util.*;

/**
 * Represents a single level of a {@link MultilevelGraph}.
 */
class LevelGraph extends AdjListGraph implements CoarsenedGraph {
    /**
     * Store "object attributes" associated with this level. (See {@link LevelGraph#setObject(String, Object)}).
     * Lazily initialized as it is not always needed.
     */
    private HashMap<String, Object> objectAttributes;

    /**
     * This method just calls the superclass' method with the same signature.
     * Note that {@code false} must be passed to the {@code directed} parameter.
     *
     * @author Gordian
     * @see AdjListGraph#doAddEdge(Node, Node, boolean)
     */
    @Override
    public Edge doAddEdge(Node source, Node target, boolean directed) {
        this.checkUndirected(directed); // TODO (review bm) needed? performance impact? check only once?
        return super.doAddEdge(source, target, false);
    }

    /**
     * This method just calls the superclass' method with the same signature.
     * Note that {@code false} must be passed to the {@code directed} parameter.
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
     * @param node Must be an instance of {@link MergedNode} and must not be {@code null}.
     * @author Gordian
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
     * @param nodes {@link Node}s to be represented. This parameter must not be {@code null}.
     * @return The newly created {@link MergedNode}.
     * @author Gordian
     */
    public MergedNode createNode(Set<Node> nodes) {
        this.setModified(true);
        MergedNode res = new MergedNode(this, nodes);
        return res;
    }

    /**
     * This just calls the superclass' method of the same signature, but makes sure that the edge is undirected.
     *
     * @param directed Must be {@code false}.
     * @author Gordian
     * @see AdjListGraph#createEdge(Node, Node, boolean)
     */
    @Override
    public Edge createEdge(Node source, Node target, boolean directed) {
        this.checkUndirected(directed);
        return super.createEdge(source, target, false);
    }

    /**
     * This just calls the superclass' method of the same signature, but makes sure that the edge is undirected.
     *
     * @param directed Must be {@code false}.
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
     * @param directed Must be {@code false}.
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
     * @param directed Must be {@code false}.
     * @author Gordian
     * @see AdjListGraph#setDirected(boolean, boolean)
     */
    @Override
    public void setDirected(boolean directed, boolean adjustArrows) {
        this.checkUndirected(directed);
        super.setDirected(false, adjustArrows);
    }

    /**
     * @param g Must be a undirected {@link Graph}.
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
     * @param node Must be an instance of {@link MergedNode}. The {@link MergedNode}'s
     *             {@link MergedNode#getInnerNodes()} method must not return any nodes already
     *             represented by a {@link MergedNode} of this graph (returned by {@link LevelGraph#getNodes()}).
     * @return The copied node.
     * @author Gordian
     */
    @Override
    public Node addNodeCopy(Node node) {
        this.checkMergedNode(node);
        return super.addNodeCopy(node);
    }

    /**
     * @param directed {@code true} or {@code false}.
     * @throws IllegalArgumentException if the parameter is {@code true}.
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
     * @param n The {@link Node} to be checked.
     * @author Gordian
     */
    private void checkMergedNode(Node n) {
        if (!(n instanceof MergedNode)) {
            throw new IllegalArgumentException("Levels of MultilevelGraph must consist only of MergedNodes.");
        }
    }

    /**
     * Checks whether there are multiple {@link MergedNode}s which represent the same nodes in the underlying graph.
     *
     * @param ref If not {@code null}, this parameter will also be checked for overlaps with the existing nodes.
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
     * @author Gordian
     * @see CoarsenedGraph#getMergedNodes()
     */
    @Override
    // only containing MergedNodes is an invariant that this wrapper class tries to
    // sustain, so this _should_ be safe
    @SuppressWarnings("unchecked")
    public Collection<? extends MergedNode> getMergedNodes() {
        return (Collection) this.nodes; // see comment above, this cast _should_ be safe
    }

    /**
     * Add an object attribute to the graph. Works similar to the attributes in
     * {@link org.graffiti.attributes.Attributable}.
     * @param key
     *      The key at which the object is stored. Must not be {@code null}.
     * @param value
     *      The value to store. Must not be {@code null}.
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
     *      The key to retrieve. If this is {@code null}, {@link Optional#empty()} will be returned.
     * @return
     *      The object stored at {@code key}, as an {@link Optional}. This method will never return {@code null}.
     * @author Gordian
     */
    public Optional<Object> getObject(String key) {
        if (this.objectAttributes == null || key == null || !this.objectAttributes.containsKey(key)) {
            return Optional.empty();
        }
        return Optional.of(this.objectAttributes.get(key));
    }
}
