package data;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import org.Vector2d;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Contains some graphs for testing.
 * @author Jannik
 */
public class TestGraphs {

    /** Contains a graph with 4 Nodes connected to create a circle. */
    public static final Graph GRAPH_1;
    /** The positions of nodes in graph 1. @see {@link TestGraphs#GRAPH_1} */
    public static final ArrayList<Vector2d> GRAPH_1_POSITIONS;
    /** The nodes of graph 1. @see {@link TestGraphs#GRAPH_1} */
    public static final ArrayList<Node> GRAPH_1_NODES;
    /** The distances between nodes of graph 1. @see {@link TestGraphs#GRAPH_1} */
    public static final NodeValueMatrix GRAPH_1_DISTANCES;

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
        for (Vector2d pos : GRAPH_1_POSITIONS) {
            GRAPH_1_NODES.add(GraphHelper.addNodeToGraph(
                    GRAPH_1, pos.x, pos.y, 1, 1, 1, Color.WHITE, Color.BLACK));
        }
        // connect the nodes
        for (int i = 0; i < GRAPH_1_NODES.size(); i++) {
            GRAPH_1.addEdge(GRAPH_1_NODES.get(i), GRAPH_1_NODES.get((i+1) % GRAPH_1_NODES.size()), false);
            GRAPH_1_DISTANCES.set(i, (i+1) % GRAPH_1_NODES.size(), 1); // update distances
        }
    }

}
