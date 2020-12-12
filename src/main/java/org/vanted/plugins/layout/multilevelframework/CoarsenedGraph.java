package org.vanted.plugins.layout.multilevelframework;

import org.graffiti.graph.Graph;

import java.util.Collection;

/**
 * Adds a method to get the {@link MergedNode}s of a coarsening level to the
 * {@link Graph} interface.
 * @see Placer (the implementers of the placer interface need this information)
 */
public interface CoarsenedGraph extends Graph  {
    Collection<?extends MergedNode> getMergedNodes();
}
