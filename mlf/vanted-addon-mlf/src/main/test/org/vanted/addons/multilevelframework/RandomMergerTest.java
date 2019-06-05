package org.vanted.addons.multilevelframework;

import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.junit.Before;
import org.junit.Test;

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
        g1.addEdge(n1,n2,false);
        g1.addEdge(n3,n2,false);
        g1.addEdge(n4,n2,false);

        mg1 = new MultilevelGraph(g1);

        n1 = g2.addNode();
        n2 = g2.addNode();

        mg2 = new MultilevelGraph(g2);

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
