package org.vanted.addons.stressminaddon.util;

import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.TimeUnit;

/**
 * @author theo
 *
 * ShortestDistanceAlgorithm finds all the shortest paths in the given set of knots.
 * BFS is performed on all start nodes. This step will be parallelized.
 */
public class ShortestDistanceAlgorithm {

    /**
     * Does the actual task of BFS from a specified start node.
     * @author theo
     */
    private class BFSRunnable implements Runnable {

        private Node n;
        private ArrayList<Node> nodes;
        private int numberNodes;
        private NodeValueMatrix results;

        /**
         * Constructs a new {@link BFSRunnable} for execution.
         * 
         * @param start   the node BFS starts from
         * @param results matrix the final result is written to
         * @param maxDepth the maximal depth to reach before terminating prematurely
         *
         * @author theo
         */
        public BFSRunnable(Node n, final ArrayList<Node>  nodes, NodeValueMatrix results) {
            this.nodes = nodes;
            this.n = n;
            this.numberNodes = nodes.size();
            this.results = results;
        }

        /**
         * Starts the BFS search from the specified start node and writes the result until the position of the start
         * node to the result matrix.
         *
         * @author theo
         */
        @Override
        public void run() {
            // stores distances to all nodes from startNode
            double[] distances = new double[numberNodes];
            // all distances are -1 in the beginning
            for (int i = 0; i < numberNodes; i++) {
                distances[i] = -1;
            }

            int posStartNode = nodes.indexOf(n);
            distances[posStartNode] = 0;

            // Create a queue for BFS
            LinkedList<Node> queue = new LinkedList<Node>();
            queue.add(n);

            Collection<Edge> edgesOfCurrentNode;

            // BFS
            while (queue.size() != 0) {
                n = queue.poll();

                for (Node neighbour : n.getNeighbors()) {
                    int posInGraph = nodes.indexOf(neighbour);
                    if (distances[posInGraph] == -1) {
                        distances[posInGraph] = distances[nodes.indexOf(n)] +1;
                        queue.add(neighbour);
                    }
                }
            }
            // copy distances to resultMatrix for node n
            for (int i = 0; i < posStartNode; i++) {
                results.set(posStartNode, i, distances[i]);
            }
        }
    }

    /**
     * Executes the BFS algorithm form every node and returns the result in a {@link NodeValueMatrix}.
     * BFSRunnable is called once on all nodes. These steps are parallelized.
     *
     * @param nodes the nodes to work with. The BFSs will not account for other nodes.
     * @param maxDepth the maximal depth to reach before terminating prematurely.
     *
     * @return a NodeValueMatrix containing the results of all start BFSs.
     *
     * @author theo
     */
    public NodeValueMatrix getShortestPaths(final ArrayList<Node> nodes) {

        int numberOfNodes = nodes.size();
        NodeValueMatrix resultMatrix = new NodeValueMatrix(numberOfNodes);
        // create a new ThreadPool
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            for (Node n : nodes) {
                executor.execute(new BFSRunnable(n, nodes, resultMatrix));
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        executor.shutdown();

        return resultMatrix;

    }
}
