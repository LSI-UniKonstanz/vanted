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
 * @author Gordian
 * @see MultilevelGraph
 */
public class MultilevelGraphTest {
    private Graph g;
    private HashSet<Node> innerNodes;
    private Node n1, n2, n3, n4;
    private Edge e1, e2, e3, e4;


    @Before
    public void setUp() throws Exception {
        // create a simple test graph
        this.g = new AdjListGraph();
        this.innerNodes = new HashSet<>();
        this.n1 = g.addNode();
        this.n2 = g.addNode();
        this.n3 = g.addNode();
        this.n4 = g.addNode();
        AttributeHelper.setPosition(n1, 0, 0);
        AttributeHelper.setPosition(n2, 0, 0);
        AttributeHelper.setPosition(n3, 0, 0);
        AttributeHelper.setPosition(n4, 0, 0);
        AttributeHelper.setLabel(n1, "1");
        AttributeHelper.setLabel(n2, "2");
        AttributeHelper.setLabel(n3, "3");
        AttributeHelper.setLabel(n4, "4");
        this.innerNodes.addAll(Arrays.asList(n1, n2, n3, n4));
        this.e1 = g.addEdge(n1, n2, false);
        this.e2 = g.addEdge(n2, n3, false);
        this.e3 = g.addEdge(n3, n4, false);
        this.e4 = g.addEdge(n4, n2, false);
    }

    @Test
    public void coarsening() {
        // create some coarsening levels
        MultilevelGraph mlg = new MultilevelGraph(this.g);
        assertSame(4, mlg.getTotalNumberOfNodes());
        assertTrue(mlg.isComplete()); // level 0 is complete by definition
        assertEquals(mlg.getNumberOfLevels(), 1);
        assertEquals(mlg.getTopLevel(), this.g);
        assertEquals(mlg.newCoarseningLevel(), 1);
        assertNotSame(mlg.getTopLevel(), this.g); // check whether a new level was actually added
        MergedNode mn = mlg.addNode(new HashSet<>(Arrays.asList(n1, n2)));
        assertSame(5, mlg.getTotalNumberOfNodes());
        assertFalse(mlg.isComplete()); // n3, n4 are not yet represented by a MergedNode
        mn.addInnerNode(this.n3);
        MergedNode mn2 = mlg.addNode(Collections.singleton(this.n4));
        assertTrue(mlg.isComplete()); // now all the nodes are represented by MergedNodes
        Edge e = mlg.addEdge(mn, mn2);
        assertTrue(mlg.getTopLevel().containsEdge(e));
        assertTrue(mlg.getTopLevel().containsNode(mn));
        assertTrue(mlg.getTopLevel().containsNode(mn2));
        assertEquals(mlg.newCoarseningLevel(), 2);
        assertFalse(mlg.isComplete());
        MergedNode mn3 = mlg.addNode(new HashSet<>(Arrays.asList(mn, mn2)));
        assertSame(7, mlg.getTotalNumberOfNodes());
        assertTrue(mlg.isComplete());
        CoarsenedGraph top = mlg.popCoarseningLevel();
        assertTrue(top.getMergedNodes().contains(mn3));
    }

    @Test(expected = IllegalStateException.class)
    public void addCoarseningLevelFail() {
        MultilevelGraph mlg = new MultilevelGraph(this.g);
        mlg.newCoarseningLevel();
        mlg.addNode(new HashSet<>(Arrays.asList(n1, n2)));
        // this fails because not all nodes are represented in the current coarsening level
        mlg.newCoarseningLevel();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addNodeFail() {
        MultilevelGraph mlg = new MultilevelGraph(this.g);
        // this fails because the MultilevelGraph doesn'vanted modify level 0 (the original graph)
        mlg.addNode(new HashSet<>(Arrays.asList(n1, n2)));
    }

    @Test(expected = IllegalStateException.class)
    public void popCoarseningLevelFail() {
        MultilevelGraph mlg = new MultilevelGraph(this.g);
        // this fails because the original graph (level 0) cannot be removed
        mlg.popCoarseningLevel();
    }
}