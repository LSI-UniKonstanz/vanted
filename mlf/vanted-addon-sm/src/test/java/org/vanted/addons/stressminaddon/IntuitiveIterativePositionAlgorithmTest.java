package org.vanted.addons.stressminaddon;

import org.Vector2d;
import org.junit.Test;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;

import java.util.List;

import static org.junit.Assert.*;
import static data.TestGraphs.*;

/**
 * Test the {@link IntuitiveIterativePositionAlgorithm}-class
 * @author Rene
 */


public class IntuitiveIterativePositionAlgorithmTest {

    /** The object to test with. */
    IterativePositionAlgorithm alg = new IntuitiveIterativePositionAlgorithm();

    /**
     * Test the method nextIteration on a square-Graph
     * @throws AssertionError
     * @author Rene
     */
    @Test
    public void nextIterationSquare() {


        NodeValueMatrix weights = GRAPH_1_DISTANCES.clone().apply(x -> 1/(x*x));


        List<Vector2d> position = alg.nextIteration(GRAPH_1_NODES,GRAPH_1_DISTANCES,weights);
        Vector2d[] expected = new Vector2d[]{
                new Vector2d(0.9539762708474337,0.9539762708474337),
                new Vector2d(0.9530573762510635,2.025139164383653),
                new Vector2d(2.0199185207071757,2.041414027985703),
                new Vector2d(2.021921576807899,0.9760001791074944)
        };


        for (int pos = 0; pos < GRAPH_1_POSITIONS.size(); pos++) {
            Vector2d expectedPos = expected[pos];
            Vector2d myPos = position.get(pos);

            assertEquals("X position", expectedPos.x, myPos.x, 0.15);
            assertEquals("Y position", expectedPos.y, myPos.y, 0.15);
        }


    }

    /**
     * Test the method nextIteration on a Graph where every Node on one Position is.
     * @throws AssertionError
     * @author Rene
     */
    @Test
    public void nextIterationOnePosition(){

        NodeValueMatrix weights = GRAPH_COLLAPSED_DISTANCES.clone().apply(x-> 1/(x*x));

        List<Vector2d> position = alg.nextIteration(GRAPH_COLLAPSED_NODES_NODES, GRAPH_COLLAPSED_DISTANCES,weights);
        Vector2d[] expected = new Vector2d[]{
                new Vector2d(0,0),
                new Vector2d(0,0),
                new Vector2d(0,0),
                new Vector2d(0,0),
        };

        for (int pos = 0; pos < GRAPH_COLLAPSED_NODES_POSITIONS.size(); pos++){
            Vector2d expectedPos = expected[pos];
            Vector2d myPos = position.get(pos);

            assertEquals("X position", expectedPos.x, myPos.x,0.0);
            assertEquals("Y position", expectedPos.y, myPos.y,0.0);
        }
    }
}