package org.vanted.addons.multilevelframework;

import org.AttributeHelper;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @see RandomMerger
 * @author Tobias
 */
public class RandomMergerTest {
    RandomMerger rM;
    Graph g1,g2;
    MultilevelGraph mg1,mg2;
    Node n1, n2, n3, n4;

    @Before
    public void setUp(){
        this.rM = new RandomMerger();

        g1 = new AdjListGraph();
        g2 = new AdjListGraph();

        n1 = g1.addNode();
        n2 = g1.addNode();
        n3 = g1.addNode();
        n4 = g1.addNode();
        for (Node n : g1.getNodes()) {
            AttributeHelper.setPosition(n, 0, 0);
        }
        g1.addEdge(n1,n2,false);
        g1.addEdge(n3,n2,false);
        g1.addEdge(n4,n2,false);

        mg1 = new MultilevelGraph(g1);

        n1 = g2.addNode();
        n2 = g2.addNode();
        for (Node n : g2.getNodes()) {
            AttributeHelper.setPosition(n, 0, 0);
        }

        mg2 = new MultilevelGraph(g2);

    }

    /**
     * @author Gordian
     */
    @Test
    public void testEdgeWeightSorting() {
        RandomMerger rm = new RandomMerger();
        String path = rm.weightAttributePath;
        rm.considerEdgeWeights = true;
        rm.minNumberOfNodesPerLevel = 3;
        AdjListGraph dummy = new AdjListGraph();
        Node n1 = dummy.addNode();
        Node n2 = dummy.addNode();
        Node n3 = dummy.addNode();
        Node n4 = dummy.addNode();
        for (Node n : dummy.getNodes()) {
            AttributeHelper.setPosition(n, 0, 0);
        }
        Edge e1 = dummy.addEdge(n1, n2, false);
        Edge e2 = dummy.addEdge(n3, n4, false);
        e1.setDouble(path, 100);
        e2.setDouble(path, -1);
        MultilevelGraph mlg = new MultilevelGraph(dummy);
        rm.buildCoarseningLevels(mlg);
        for (MergedNode mn : mlg.popCoarseningLevel().getMergedNodes()) {
            assertTrue(mn.getInnerNodes().size() == 1 ||
                    mn.getInnerNodes().containsAll(Arrays.asList(n3, n4)));
        }
    }


    @Test
    public void buildCoarseningLevels1(){
        this.rM.buildCoarseningLevels(mg1);
        assertTrue(mg1.isComplete());
    }

    @Test
    public void buildCoarseningLevels2() {
        this.rM.buildCoarseningLevels(mg2);
        assertTrue(mg2.isComplete());
    }

}
