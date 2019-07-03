package data;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import org.Vector2d;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.vanted.addons.stressminaddon.StressMinimizationLayout;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Contains some graphs for testing.
 * @author Jannik
 */
public class TestGraphs {

    // Graph 1
    /** Contains a graph with 4 Nodes connected to create a circle. */
    public static final Graph GRAPH_1;
    /** The positions of nodes in graph 1. @see {@link TestGraphs#GRAPH_1} */
    public static final ArrayList<Vector2d> GRAPH_1_POSITIONS;
    /** The nodes of graph 1. @see {@link TestGraphs#GRAPH_1} */
    public static final ArrayList<Node> GRAPH_1_NODES;
    /** The distances between nodes of graph 1. @see {@link TestGraphs#GRAPH_1} */
    public static final NodeValueMatrix GRAPH_1_DISTANCES;

    // Graph 2
    /** Contains a graph with 8 Nodes connected to create two circles. */
    public static final Graph GRAPH_2;
    /** The positions of nodes in graph 2. @see {@link TestGraphs#GRAPH_2} */
    public static final ArrayList<Vector2d> GRAPH_2_POSITIONS;
    /** The nodes of graph 2. @see {@link TestGraphs#GRAPH_2} */
    public static final ArrayList<Node> GRAPH_2_NODES;
    /** The distances between nodes of graph 2. @see {@link TestGraphs#GRAPH_2} */
    public static final NodeValueMatrix GRAPH_2_DISTANCES;


    // Long path graph
    /**
     * Contains a graph that is a not layouted 1000 element path with random positions.
     * Can be used if a operation on a graph should take longer. */
    public static final Graph GRAPH_LONG_PATH;
    /** The positions of nodes in the long path graph. @see {@link TestGraphs#GRAPH_LONG_PATH} */
    public static final ArrayList<Vector2d> GRAPH_LONG_PATH_POSITIONS;
    /** The nodes of the long path graph. @see {@link TestGraphs#GRAPH_LONG_PATH} */
    public static final ArrayList<Node> GRAPH_LONG_PATH_NODES;
    /** The distances between nodes of the long path graph. @see {@link TestGraphs#GRAPH_LONG_PATH} */
    public static final NodeValueMatrix GRAPH_LONG_PATH_DISTANCES;
    /*
      Initialize graphs
      @author Jannik
     */
    static {
        // init graph 1
        ///////////////////////////////////////////
        GRAPH_1_POSITIONS = new ArrayList<>(Arrays.asList(
                new Vector2d(1,1), new Vector2d(1,2), new Vector2d(2, 2), new Vector2d(2, 1)));
        GRAPH_1 = new AdjListGraph();
        GRAPH_1_NODES = new ArrayList<>();
        GRAPH_1_DISTANCES = new NodeValueMatrix(GRAPH_1_POSITIONS.size());

        GRAPH_1_DISTANCES.apply(x -> 2); // maximum distance

        // create nodes and place them
        for (int i = 0; i < GRAPH_1_POSITIONS.size(); i++) {
            Vector2d pos = GRAPH_1_POSITIONS.get(i);
            Node n = GraphHelper.addNodeToGraph(
                    GRAPH_1, pos.x, pos.y, 1, 1, 1, Color.WHITE, Color.BLACK);
            n.setInteger(StressMinimizationLayout.INDEX_ATTRIBUTE, i);
            GRAPH_1_NODES.add(n);
        }
        // connect the nodes
        for (int i = 0; i < GRAPH_1_NODES.size(); i++) {
            GRAPH_1.addEdge(GRAPH_1_NODES.get(i), GRAPH_1_NODES.get((i+1) % GRAPH_1_NODES.size()), false);
            GRAPH_1_DISTANCES.set(i, (i+1) % GRAPH_1_NODES.size(), 1); // update distances
        }

        // init graph 2
        ///////////////////////////////////////////
        GRAPH_2_POSITIONS = new ArrayList<>(Arrays.asList(
                new Vector2d(3,3), new Vector2d(3,4), new Vector2d(4, 4), new Vector2d(4, 3)));
        GRAPH_2_POSITIONS.addAll(GRAPH_1_POSITIONS);
        GRAPH_2 = new AdjListGraph();
        GRAPH_2_NODES = new ArrayList<>();
        GRAPH_2_DISTANCES = new NodeValueMatrix(GRAPH_2_POSITIONS.size());

        GRAPH_2_DISTANCES.apply(x -> Double.POSITIVE_INFINITY); // some nodes have no connection at all
        // create nodes and place them
        for (int i = 0; i < GRAPH_2_POSITIONS.size(); i++) {
            Vector2d pos = GRAPH_2_POSITIONS.get(i);
            Node n = GraphHelper.addNodeToGraph(
                    GRAPH_2, pos.x, pos.y, 1, 42, 42, Color.WHITE, Color.BLACK);
            n.setInteger(StressMinimizationLayout.INDEX_ATTRIBUTE, i);
            GRAPH_2_NODES.add(n);
        }
        // connect the nodes
        final int half = GRAPH_2_NODES.size()/2;
        for (int i = 0; i < half; i++) {
            GRAPH_2.addEdge(GRAPH_2_NODES.get(i), GRAPH_2_NODES.get((i+1) % half), false);
            GRAPH_2_DISTANCES.set(i, (i+1) % half, 1); // update distances
            GRAPH_2.addEdge(GRAPH_2_NODES.get(i + half), GRAPH_2_NODES.get((i+1) % half + half), false);
            GRAPH_2_DISTANCES.set(i + half, (i+1) % half + half, 1); // update distances
        }
        // set distances of two
        for (int i = 0; i < half/2; i++) {
            GRAPH_2_DISTANCES.set(i, i + half/2, 2);
            GRAPH_2_DISTANCES.set(i + half, i + half + half/2, 2);
        }

        // init long path graph
        ///////////////////////////////////////////
        GRAPH_LONG_PATH_POSITIONS = new ArrayList<>(1000);
        Random random = new Random(42); // keep the positions predictable
        for (int num = 0; num < 1000; num++) {
            GRAPH_LONG_PATH_POSITIONS.add(new Vector2d(random.nextDouble()*1000, random.nextDouble()));
        }
        GRAPH_LONG_PATH = new AdjListGraph();
        GRAPH_LONG_PATH_NODES = new ArrayList<>();
        GRAPH_LONG_PATH_DISTANCES = new NodeValueMatrix(GRAPH_LONG_PATH_POSITIONS.size());

        // create nodes and place them
        for (int i = 0; i < GRAPH_LONG_PATH_POSITIONS.size(); i++) {
            Vector2d pos = GRAPH_LONG_PATH_POSITIONS.get(i);
            Node n = GraphHelper.addNodeToGraph(
                    GRAPH_LONG_PATH, pos.x, pos.y, 1, 42, 42, Color.WHITE, Color.BLACK);
            n.setInteger(StressMinimizationLayout.INDEX_ATTRIBUTE, i);
            GRAPH_LONG_PATH_NODES.add(n);
        }
        // connect the nodes
        for (int i = 1; i < GRAPH_LONG_PATH_NODES.size(); i++) {
            GRAPH_LONG_PATH.addEdge(GRAPH_LONG_PATH_NODES.get(i-1), GRAPH_LONG_PATH_NODES.get(i), false);
            for (int j = 0; j < i; j++) { // update distances
                GRAPH_LONG_PATH_DISTANCES.set(i, j, i-j);
            }
        }
    }

    /** Contains a graph with 4 Nodes in a circle, all the Nodes are on the same position (0,0). */
    public static final Graph GRAPH_COLLAPSED_NODES;
    /** The positions of nodes in graph onePosition.  */
    public static final ArrayList<Vector2d> GRAPH_COLLAPSED_NODES_POSITIONS;
    /** The nodes of graph onePosition. */
    public static final ArrayList<Node> GRAPH_COLLAPSED_NODES_NODES;
    /** The distances between nodes of graph onePosition. */
    public static final NodeValueMatrix GRAPH_COLLAPSED_DISTANCES;

    /*
      Initialize graphs
      @author Rene
     */
    static{
        GRAPH_COLLAPSED_NODES_POSITIONS = new ArrayList<>(Arrays.asList(
                new Vector2d(0.0,0.0), new Vector2d(0.0,0.0), new Vector2d(0.0,0.0), new Vector2d(0.0,0.0)));
        GRAPH_COLLAPSED_NODES = new AdjListGraph();
        GRAPH_COLLAPSED_NODES_NODES = new ArrayList<>();
        GRAPH_COLLAPSED_DISTANCES = new NodeValueMatrix(GRAPH_COLLAPSED_NODES_POSITIONS.size());

        GRAPH_COLLAPSED_DISTANCES.apply(x->2);

        for (int i = 0; i < GRAPH_COLLAPSED_NODES_POSITIONS.size(); i++) {
            Vector2d pos = GRAPH_COLLAPSED_NODES_POSITIONS.get(i);
            Node n = GraphHelper.addNodeToGraph(
                            GRAPH_COLLAPSED_NODES, pos.x, pos.y, 1, 1, 1, Color.WHITE, Color.BLACK);
            n.setInteger(StressMinimizationLayout.INDEX_ATTRIBUTE, i);
            GRAPH_COLLAPSED_NODES_NODES.add(n);
        }

        for(int i = 0; i< GRAPH_COLLAPSED_NODES_NODES.size(); i++){
            GRAPH_COLLAPSED_NODES.addEdge(GRAPH_COLLAPSED_NODES_NODES.get(i), GRAPH_COLLAPSED_NODES_NODES.get((i+1) % GRAPH_COLLAPSED_NODES_NODES.size()), false);
            GRAPH_COLLAPSED_DISTANCES.set(i,(i+1) % GRAPH_COLLAPSED_NODES_NODES.size(), 1);
        }
    }
}
