package org.vanted.addons.multilevelframework;

import org.graffiti.attributes.*;
import org.graffiti.graph.*;

import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * This class stores the coarsening levels of a graph, as well as the original graph.
 */
public class MultilevelGraph {
    /**
     * Contains the coarsening levels. The highest index corresponds to the "coarsest" level.
     */
    private final ArrayList<InternalGraph> levels;
    private final Graph original_graph;

    /**
     * Create a {@link MultilevelGraph}. Initially only the original graph will be contained.
     * New coarsening levels can be added, e.g. by calling {@link this#newCoarseningLevel()}.
     * @param level_0
     *     The original graph. Must not be {@code null}.
     */
    public MultilevelGraph(Graph level_0) {
        this.levels = new ArrayList<>();
        this.original_graph = Objects.requireNonNull(level_0,
                "MultilevelGraph's level 0 must not be null.");
    }

    /**
     * Calculate the number of levels.
     * @return the number of levels (including the original graph)
     */
    public int getNumberOfLevels() {
        return this.levels.size() + 1;
    }

    /**
     * Get the topmost (i.e. the "coarsest") coarsening level.
     * @return the topmost coarsening level or the original graph if there are no coarsened versions.
     */
    public Graph getTopLevel() {
        if (this.levels.isEmpty()) {
            return this.original_graph;
        } else {
            return this.levels.get(this.levels.size() - 1);
        }
    }

    /**
     * Create a new coarsening level, based on the current top one (i.e. the one returned by
     * {@link MultilevelGraph#getTopLevel()}). Note that this method will throw
     * an {@link IllegalStateException} if the current coarsening level is not complete
     * (see {@link MultilevelGraph#isComplete()}).
     * @return
     *     the level number of the new coarsening level (starting at {@code 1}, the original
     *     graph is level {@code 0})
     */
    public int newCoarseningLevel() {
        if (!this.levels.isEmpty() && !this.isComplete()) {
            throw new IllegalStateException( "Multilevel cannot add a new coarsening level if there are ");
        }
        this.levels.add(new InternalGraph());

        // set a nicer name for the coarsened graph
        String original_name = this.original_graph.getName();
        original_name = original_name == null ? "unknown" : original_name;
        this.topInternalLevel().setName("Level " + this.levels.size() + " of " + original_name);

        return this.levels.size();
    }

    /**
     * Add a new {@link MergedNode} to the topmost coarsening level (i.e. the one returned by
     * {@link MultilevelGraph#getTopLevel()})
     * @param representedNodes
     *     The {@link Node}s that the {@link MergedNode} represents. This parameter can be {@code null}.
     *     Note that you can change the represented {@code Node}s later using the methods of {@link MergedNode}.
     * @return the {@link MergedNode} that was created.
     */
    public MergedNode addNode(Collection<Node> representedNodes) {
        MergedNode mn;
        if (representedNodes == null) {
            mn = this.topInternalLevel().createNode(Collections.emptyList());
        } else {
            mn = this.topInternalLevel().createNode(representedNodes);
        }
        this.topInternalLevel().doAddNode(mn);
        return mn;
    }

    /**
     * Checks whether the top level is complete, i.e. whether all nodes in the underlying level are represented
     * by a {@link MergedNode} in the top level. Note that this is a rather expensive computation.
     * Also note that this obviously requires that at least one coarsening level exist.
     * @return {@code true} if the level is complete or {@code false} otherwise
     */
    public boolean isComplete() {
        if (this.levels.isEmpty()) {
            return true;
        }
        // TODO: safe to hash nodes?
        // TODO: does this meet performance goals?
        final Graph prev = this.levels.size() == 1 ? this.original_graph : this.levels.get(this.levels.size() - 2);
        final Set<Node> previousNodes = new HashSet<>(prev.getNodes());
        final Set<Node> representedNodes = new HashSet<>();
        for (Node n : this.topInternalLevel().getNodes()) {
            final MergedNode mn = (MergedNode) n;
            representedNodes.addAll(mn.getInnerNodes());
        }
        return previousNodes.equals(representedNodes);
    }

    /**
     * Adds an undirected {@link AdjListEdge} to the topmost level (i.e. the one returned by
     * {@link MultilevelGraph#getTopLevel()}) connecting two {@link MergedNode}s.
     * @param source
     *     The edge's source. Must not be {@code null}.
     * @param target
     *     The edge's target. Must not be {@code null}.
     * @return
     *     The newly created edge.
     */
    public Edge addEdge(Node source, Node target) {
        return this.topInternalLevel().addEdge(Objects.requireNonNull(source,
                "MultilevelGraph edge cannot connect null node."),
                Objects.requireNonNull(target, "MultilevelGraph edge cannot connect null node."),
                false);
    }

    /**
     * Throw an exception if there are no coarsening levels. Otherwise
     * @return the topmost (i.e. "coarsest") coarsening level.
     */
    private InternalGraph topInternalLevel() {
        if (this.levels.isEmpty()) {
            throw new UnsupportedOperationException("MultilevelGraph cannot modify the original graph.");
        }
        return this.levels.get(this.levels.size() - 1);
    }
}

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
        this.assertNoOverlappingMergedNodes((MergedNode) node);
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
    public MergedNode createNode(Collection<Node> nodes) {
        this.setModified(true);
        MergedNode res = new MergedNode(this, nodes);
        this.assertNoOverlappingMergedNodes(res);
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
        this.assertNoOverlappingMergedNodes((MergedNode) node);
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
        MergedNode mn = (MergedNode) n;
        if (mn.getInnerNodes().isEmpty()) {
            throw new IllegalArgumentException("MultilevelGraph must not contain empty MergedNodes.");
        }
    }

    /**
     * Checks whether there are multiple {@link MergedNode}s which represent the same nodes in the underlying graph.
     * Since this is an expensive calculation, it uses {@code assert} statements so that it is only run when
     * the JVM has assertions enabled.
     * @param ref
     *    If not {@code null}, this parameter will also be checked for overlaps with the existing nodes.
     */
    private void assertNoOverlappingMergedNodes(MergedNode ref) {
        final BooleanSupplier bs = () -> {
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
        };
        assert bs.getAsBoolean() : "MultilevelGraph cannot contain multiple MergedNodes representing "
                        + "the same nodes in the underlying graph.";
    }
}
