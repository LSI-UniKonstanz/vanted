package org.vanted.addons.stressminaddon;

import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * A set of metrics measures that can be used to rate the quality of a
 * given graph.
 * @author Jannik, René
 */
public class QualityMetrics {

    final static ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * Calculate the number of edge crossings in the given graph,
     * implemented in a trivial way, but with multithreading.
     *
     * @param graph the graph to be used
     * @return
     *      the number of crossing edges.
     * @author Jannik, René
     */
    public static int lineCrossings(Graph graph){
        final Edge[] edges = graph.getEdges().toArray(new Edge[0]);
        final ArrayList<Callable<Integer>> tasks = new ArrayList<>();


        for (int i = 0, edgesLength = edges.length; i < edgesLength; i++) {
            Edge currentEdge = edges[i];
            final int idx = i;
            final Point2D sourcePos = AttributeHelper.getPositionVec2d(currentEdge.getSource()).getPoint2D();
            final Point2D targetPos = AttributeHelper.getPositionVec2d(currentEdge.getTarget()).getPoint2D();
            Line2D lineCurrent = new Line2D.Double(sourcePos, targetPos);

            tasks.add(() -> {
                int innerResult = 0;
                for (int i1 = idx+1, length = edges.length; i1 < length; i1++) {
                    Edge otherEdge = edges[i1];
                    final Point2D otherSourcePos = AttributeHelper.getPositionVec2d(otherEdge.getSource()).getPoint2D();
                    final Point2D otherTargetPos = AttributeHelper.getPositionVec2d(otherEdge.getTarget()).getPoint2D();

                    // incident edges cannot cross
                    if (currentEdge.getTarget().equals(otherEdge.getTarget()) || currentEdge.getTarget().equals(otherEdge.getSource()) ||
                            currentEdge.getSource().equals(otherEdge.getSource()) || currentEdge.getSource().equals(otherEdge.getTarget())) {
                        continue;
                    }
                    Line2D lineOther = new Line2D.Double(otherSourcePos, otherTargetPos);

                    if (lineCurrent.intersectsLine(lineOther)) {
                        innerResult++;
                    }
                }
                return innerResult;
            });

        }

        try {
            return es.invokeAll(tasks).stream().mapToInt(i -> {
                try {
                    return i.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }).sum();
        } catch (InterruptedException | RuntimeException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * For documentation see {@link StressMinimizationLayout#calculateStress(Graph, boolean)}<br>
     * If a more detailed configuration is needed the method
     * {@link StressMinimizationLayout#calculateStress(Graph, boolean, double, double, double, double)}
     * can be used instead.
     *
     * @param graph the graph to be used.
     * @return the cumulated stress of the graph.
     *
     * @author Jannik
     */
    public static double stress(final Graph graph) {
        return StressMinimizationLayout.calculateStress(graph, false).get(0);
    }
}
