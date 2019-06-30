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
 * @see SolarMerger
 * @author Tobias
 */
public class SolarMergerTest {
    SolarMerger sM;
    Graph g1,g2;
    MultilevelGraph mg1,mg2;
    Node n1, n2, n3, n4;



    @Before
    public void setUp(){
        this.sM = new SolarMerger();
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

    @Test
    public void buildCoarseningLevels1(){
        sM.minNodes = 2;
        sM.maxLevelFactor = 1;
        this.sM.buildCoarseningLevels(mg1);
    }

    @Test
    public void buildCoarseningLevels2(){
        MultilevelGraph untouchedMg1 = mg1;
        sM.minNodes = 0;
        this.sM.buildCoarseningLevels(mg1);
        assertTrue(untouchedMg1 == mg1);
    }

    @Test
    public void buildCoarseningLevels3(){
        MultilevelGraph untouchedMg1 = mg1;
        sM.maxLevelFactor = Integer.MAX_VALUE;
        this.sM.buildCoarseningLevels(mg1);
        assertTrue(untouchedMg1 == mg1);
    }

    @Test
    public void buildCoarseningLevels4(){
        MultilevelGraph mg0 = new MultilevelGraph(new AdjListGraph());
        MultilevelGraph untouchedMg1 = mg0;
        sM.minNodes = 0;
        this.sM.buildCoarseningLevels(mg0);
        assertTrue(untouchedMg1 == mg0);
    }


    /**
     * @author Tobias credits to Gordian
     */
    @Test
    public void getNameAndDescription() {
        assertTrue(sM.getName().toLowerCase().contains("solar"));
        assertTrue(sM.getName().toLowerCase().contains("merger"));
        assertTrue(sM.getDescription().toLowerCase().contains("solar"));
    }


}
