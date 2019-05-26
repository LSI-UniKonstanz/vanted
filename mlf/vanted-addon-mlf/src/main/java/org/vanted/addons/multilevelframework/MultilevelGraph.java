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
     *     The {@link Node}s that the {@link MergedNode} represents.
     *     Note that you add new represented {@code Node}s later using the methods of {@link MergedNode}.
     * @return the {@link MergedNode} that was created.
     */
    public MergedNode addNode(Set<Node> representedNodes) {
        MergedNode mn = this.topInternalLevel().createNode(representedNodes);
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

