package org.vanted.addons.stressminaddon.util;

import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.graph.Node;
import org.vanted.addons.stressminaddon.StressMinimizationLayout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ShortestDistanceAlgorithm finds all the shortest paths in the given set of knots.
 * BFS is performed on all start nodes. This step will be parallelized.
 * @author theo
 */
public enum  ShortestDistanceAlgorithm {
    ; // No need for instances

    /**
     * Does the actual task of BFS from a specified start node.
     * @author theo
     */
    private static class BFSRunnable implements Runnable {

        /** The node to start from. */
        private Node start;
        /** The maximum number of nodes to process. */
        private int numberNodes;
        /** The maximal depth to reach before terminating prematurely. */
        private int maxDepth;
        /** The resulting matrix. This will be shared between all {@link BFSRunnable}s. */
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
        BFSRunnable(final Node start, final NodeValueMatrix results, final int maxDepth) {
            assert start != null && results != null;
            this.start = start;
            this.numberNodes = results.getDimension();
            this.results = results;
            this.maxDepth = maxDepth;
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
            // fast fill with +Infinity (see https://stackoverflow.com/questions/9128737/fastest-way-to-set-all-values-of-an-array/25508988#25508988)
            if (numberNodes > 0) {
                distances[0] = Double.POSITIVE_INFINITY;
            }
            for (int i = 1; i < numberNodes; i += i) {
                System.arraycopy(distances, 0, distances, i, ((numberNodes - i) < i) ? (numberNodes - i) : i);
            }

            int posStartNode = start.getInteger(StressMinimizationLayout.INDEX_ATTRIBUTE);
            distances[posStartNode] = 0;

            // Create a queue for BFS
            Queue<Node> queue = new LinkedList<>();
            queue.add(start);

            Node current;
            int posCurrent;
            // BFS
            while (queue.size() != 0) {
                current = queue.poll();
                posCurrent = current.getInteger(StressMinimizationLayout.INDEX_ATTRIBUTE);
                if (distances[posCurrent] >= this.maxDepth)
                    break;


                for (Node neighbour : current.getNeighbors()) {
                    try {
                        int posInGraph = neighbour.getInteger(StressMinimizationLayout.INDEX_ATTRIBUTE);
                        if (distances[posInGraph] == Double.POSITIVE_INFINITY) {
                            distances[posInGraph] = distances[posCurrent] + 1;
                            queue.add(neighbour);
                        }
                    } catch (AttributeNotFoundException e) {
                        // Node is not in the current connected component
                    }
                }
            }
            // copy distances to resultMatrix for node start
            results.setHalfRow(posStartNode, distances);
        }
    }

    /**
     * Executes the BFS algorithm form every node and returns the result in a {@link NodeValueMatrix}.
     * BFSRunnable is called once on all nodes. These steps are parallelized.<br>
     * The {@link StressMinimizationLayout#INDEX_ATTRIBUTE} attribute of each node must be set to it's
     * position in the nodes list.
     *
     * @param nodes the nodes to work with. The BFSs will not account for other nodes.
     * @param maxDepth the maximal depth to reach before terminating prematurely.
     * @param multipleThreads whether to use multiple threads for execution.
     *
     * @return a NodeValueMatrix containing the results of all start BFSs.
     *
     * @author theo
     */
    public static NodeValueMatrix calculateShortestPaths(final List<Node> nodes, final int maxDepth, final boolean multipleThreads) {

        int numberOfNodes = nodes.size();
        NodeValueMatrix resultMatrix = new NodeValueMatrix(numberOfNodes);
        // create a new ThreadPool
        ExecutorService executor = null;
        if (multipleThreads) {
            executor = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors(), numberOfNodes));
        }
        ArrayList<Callable<Object>> todo = new ArrayList<>(numberOfNodes);
        for (int node = 1; node < numberOfNodes; node++) { // the first node has no saved row.
            todo.add(Executors.callable(new BFSRunnable(nodes.get(node), resultMatrix, maxDepth)));
        }

        try {
            if (multipleThreads) {
                executor.invokeAll(todo);
            } else {
                for (Callable<Object> callable : todo) {
                    callable.call();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultMatrix;
    }
}
