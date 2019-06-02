package org.vanted.addons.multilevelframework;

import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.selection.Selection;

import java.util.*;

/**
 * Helper methods for the multilevel framework add-on.
 */
public enum MlfHelper {
    ; // no instances needed

    /**
     * Calculate the subgraph induced by the selection and split it up into its connected components.
     * Note that this method is <emph>heavily</emph> inspired by
     * {@link de.ipk_gatersleben.ag_nw.graffiti.GraphHelper#getConnectedComponentsAsCopy(List)}, which
     * cannot be used directly by the MLF because it doesn't calculate the induced subgraph.
     *
     * @param selection The {@link Selection}. Must not be {@code null}.
     * @return The connected components of the subgraph in induced by the selection, as {@link CoarsenedGraph}s.
     */
    public static Collection<? extends CoarsenedGraph>
    calculateConnectedComponentsOfSelection(Set<Node> selection) {
        if (selection.size() <= 0) {
            return Collections.emptyList();
        }

        ArrayList<InternalGraph> graphList = new ArrayList<InternalGraph>();
        HashMap<Node, Node> sourceGraphNode2connectedGraphNode = new LinkedHashMap<Node, Node>();
        while (!selection.isEmpty()) {
            Node startNode = selection.iterator().next();
            Set<Node> connectedNodes = getConnectedNodes(startNode, selection);
            InternalGraph connectedComponentGraph = new InternalGraph();
            for (Node n : connectedNodes) {
                MergedNode newNode = new MergedNode(connectedComponentGraph, Collections.singleton(n));
                connectedComponentGraph.doAddNode(newNode);
                sourceGraphNode2connectedGraphNode.put(n, newNode);
            }
            for (Node n : connectedNodes) {
                for (Edge e : n.getEdges()) {
                    if (connectedNodes.contains(e.getSource()) && connectedNodes.contains(e.getTarget())) {
                        connectedComponentGraph.addEdgeCopy(e, sourceGraphNode2connectedGraphNode.get(e.getSource()),
                                sourceGraphNode2connectedGraphNode.get(e.getTarget()));
                    }
                }
            }
            graphList.add(connectedComponentGraph);
            selection.removeAll(connectedNodes);
        }
        return graphList;
    }

    /**
     * Note that this method is <emph>heavily</emph> inspired by
     * {@link de.ipk_gatersleben.ag_nw.graffiti.GraphHelper#getConnectedNodes(Node)}, which we cannot use directly
     * since it doesn't consider the selection.
     *
     * @param startNode The start node from which to find all connected nodes.
     * @param selection The nodes to consider.
     * @return All the nodes reachable from {@code startNode} that are contained in {@code selection}.
     * The {@code startNode} is always contained in the result.
     * @see de.ipk_gatersleben.ag_nw.graffiti.GraphHelper#getConnectedNodes(Node)
     */
    static Set<Node> getConnectedNodes(Node startNode, Set<Node> selection) {
        List<Node> stack = new ArrayList<>();
        Set<Node> hashSet = new HashSet<>();

        stack.add(startNode);
        hashSet.add(startNode);

        while (!stack.isEmpty()) {
            Iterator<?> neighbours = stack.remove(stack.size() - 1).getNeighborsIterator();

            while (neighbours.hasNext()) {
                Node neighbour = (Node) neighbours.next();

                // check if the node is actually in the selection
                if (!hashSet.contains(neighbour) && selection.contains(neighbour)) {
                    hashSet.add(neighbour);
                    stack.add(neighbour);
                }
            }
        }

        return hashSet;
    }
}
