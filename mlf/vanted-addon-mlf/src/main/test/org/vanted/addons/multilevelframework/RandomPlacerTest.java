package org.vanted.addons.multilevelframework;

import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.HashSet;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.junit.Before;
import org.junit.Test;

public class RandomPlacerTest {
	private Graph alg;
    private Node n1, n2, n3, n4;
    private RandomPlacer rp;
    private MultilevelGraph mlg;


    @Before
    public void setUp() throws Exception {
    	rp = new RandomPlacer();
    	
        this.alg = new AdjListGraph();
        this.n1 = alg.addNode();
        this.n2 = alg.addNode();
        this.n3 = alg.addNode();
        this.n4 = alg.addNode();
        //Labels
        AttributeHelper.setLabel(n1, "1");
        AttributeHelper.setLabel(n2, "2");
        AttributeHelper.setLabel(n3, "3");
        AttributeHelper.setLabel(n4, "4");
        //Positions
        AttributeHelper.setPosition(n1, 100, 100);
        AttributeHelper.setPosition(n2, 100, 100);
        AttributeHelper.setPosition(n3, 300, 300);
        AttributeHelper.setPosition(n4, 300, 300);
        //Build MLG
        mlg = new MultilevelGraph(alg);
        mlg.newCoarseningLevel();
        MergedNode mn1 = mlg.addNode(new HashSet<>(Arrays.asList(n1, n2)));
        MergedNode mn2 = mlg.addNode(new HashSet<>(Arrays.asList(n3, n4)));
        mlg.addEdge(mn1, mn2);
    }
    
    @Test
    public void testPosChange() {
    	Vector2d [] oldpos = {AttributeHelper.getPositionVec2d(n1),
    							AttributeHelper.getPositionVec2d(n2),
    							AttributeHelper.getPositionVec2d(n3),
    							AttributeHelper.getPositionVec2d(n4)};
    	rp.reduceCoarseningLevel(mlg);
    	assertFalse(oldpos[0].distance(AttributeHelper.getPositionVec2d(n1)) == 0.0);
    	assertFalse(oldpos[1].distance(AttributeHelper.getPositionVec2d(n2)) == 0.0);
    	assertFalse(oldpos[2].distance(AttributeHelper.getPositionVec2d(n3)) == 0.0);
    	assertFalse(oldpos[3].distance(AttributeHelper.getPositionVec2d(n4)) == 0.0);
    }
}
