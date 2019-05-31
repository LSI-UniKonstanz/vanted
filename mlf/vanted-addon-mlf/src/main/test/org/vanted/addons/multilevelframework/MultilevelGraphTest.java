package org.vanted.addons.multilevelframework;

import org.AttributeHelper;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * @see MultilevelGraph
 * @author Gordian
 */
public class MultilevelGraphTest {
    private Graph g;
    private HashSet<Node> innerNodes;
    private Node n1, n2, n3, n4;
    private Edge e1, e2, e3, e4;


    @Before
    public void setUp() throws Exception {
        this.g = new AdjListGraph();
        this.innerNodes = new HashSet<>();
        this.n1 = g.addNode();
        this.n2 = g.addNode();
        this.n3 = g.addNode();
        this.n4 = g.addNode();
        AttributeHelper.setLabel(n1, "1");
        AttributeHelper.setLabel(n2, "2");
        AttributeHelper.setLabel(n3, "3");
        AttributeHelper.setLabel(n4, "4");
        this.innerNodes.addAll(Arrays.asList(n1, n2, n3, n4));
        this. e1 = g.addEdge(n1, n2, false);
        this. e2 = g.addEdge(n2, n3, false);
        this. e3 = g.addEdge(n3, n4, false);
        this. e4 = g.addEdge(n4, n2, false);
    }

    @Test
    public void coarsening() {
        MultilevelGraph mlg = new MultilevelGraph(this.g);
        assertTrue(mlg.isComplete());
        assertEquals(mlg.getNumberOfLevels(), 1);
        assertEquals(mlg.getTopLevel(), this.g);
        assertEquals(mlg.newCoarseningLevel(), 1);
        assertNotSame(mlg.getTopLevel(), this.g);
        MergedNode mn = mlg.addNode(new HashSet<>(Arrays.asList(n1, n2)));
        assertFalse(mlg.isComplete());
        mn.addInnerNode(this.n3);
        MergedNode mn2 = mlg.addNode(Collections.singleton(this.n4));
        assertTrue(mlg.isComplete());
        Edge e = mlg.addEdge(mn, mn2);
        assertTrue(mlg.getTopLevel().containsEdge(e));
        assertTrue(mlg.getTopLevel().containsNode(mn));
        assertTrue(mlg.getTopLevel().containsNode(mn2));
        assertEquals(mlg.newCoarseningLevel(), 2);
        assertFalse(mlg.isComplete());
        MergedNode mn3 = mlg.addNode(new HashSet<>(Arrays.asList(mn, mn2)));
        assertTrue(mlg.isComplete());
        CoarsenedGraph top = mlg.popCoarseningLevel();
        assertTrue(top.getMergedNodes().contains(mn3));
    }

    @Test(expected = IllegalStateException.class)
    public void addCoarseningLevelFail() {
        MultilevelGraph mlg = new MultilevelGraph(this.g);
        mlg.newCoarseningLevel();
        mlg.addNode(new HashSet<>(Arrays.asList(n1, n2)));
        mlg.newCoarseningLevel();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addNodeFail() {
        MultilevelGraph mlg = new MultilevelGraph(this.g);
        mlg.addNode(new HashSet<>(Arrays.asList(n1, n2)));
    }

    @Test(expected = IllegalStateException.class)
    public void popCoarseningLevelFail() {
        MultilevelGraph mlg = new MultilevelGraph(this.g);
        mlg.popCoarseningLevel();
    }
}