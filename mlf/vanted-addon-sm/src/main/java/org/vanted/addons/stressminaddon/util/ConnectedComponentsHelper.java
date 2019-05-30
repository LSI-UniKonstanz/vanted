package org.vanted.addons.stressminaddon.util;

import org.graffiti.graph.Node;

import java.util.*;

/**
 * Contains utility and helper methods for working with connected components.
 * @author Jannik
 */
public enum ConnectedComponentsHelper {
    ; // No need for instances.

    /**
     * Returns a {@link Set} containing all connected components in the given <code>workingSet</code>
     * as unmodifiable {@link RandomAccess} lists.
     *
     * This code is currently almost an exact copy of
     * {@link de.ipk_gatersleben.ag_nw.graffiti.GraphHelper#getConnectedComponents(Collection)}
     * with the addition that only nodes from the working set are regarded, not all nodes that share the same
     * connected component.
     *
     * @param workingSet
     *      nodes to work with. Only nodes in this set will be added to the returned connected components.
     *
     * @return
     *      a {@link Set} of the connected components that are contained in the <code>workingSet</code> and
     *      are saved in unmodifiable, {@link RandomAccess} lists.
     *
     * @author Christian Klukas, Jannik
     */
    public static Set<List<Node>> getConnectedComponents(final Collection<Node> workingSet) {
        Set<Set<Node>> nodeSetSet = new HashSet<>();

        // get all RefSets
        Set<Node> allNodes = new HashSet<>(workingSet);

        Set<Node> alreadyContainedNodes = new HashSet<>();

        for (Node startNode : allNodes) {
            if (alreadyContainedNodes.contains(startNode)) {
                continue;
            }

            // set of unlabeled nodes
            Set<Node> connectedComponent = new HashSet<>();
            nodeSetSet.add(connectedComponent);

            // Set of unlabeled refSets to visit
            Stack<Node> nodesToProcess = new Stack<>();
            nodesToProcess.push(startNode);

            // find and add all connected refSets
            while (!nodesToProcess.isEmpty()) {
                Node current = nodesToProcess.pop();

                // add new startNode
                connectedComponent.add(current);

                // get all adjacent nodes
                Set<Node> neighbors = current.getNeighbors();

                // add all new refSets
                for (Node neighbor : neighbors) {
                    if (!connectedComponent.contains(neighbor) && !nodesToProcess.contains(neighbor) &&
                            allNodes.contains(neighbor)) { // new neighbour found that is in working set
                        nodesToProcess.push(neighbor);
                    }
                }
            }
            // mark all nodes as seen
            alreadyContainedNodes.addAll(connectedComponent);
        }
        // prepare the final result
        HashSet<List<Node>> result = new HashSet<>(nodeSetSet.size());
        for (Set<Node> connectedComponent : nodeSetSet) {
            result.add(Collections.unmodifiableList(new ArrayList<>(connectedComponent)));
        }
        return result;
    }
}
