package org.vanted.plugins.layout.multilevelframework;

import org.AttributeHelper;
import org.graffiti.graph.*;
import org.graffiti.graphics.EdgeLabelAttribute;
import org.graffiti.graphics.NodeLabelAttribute;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * @author Gordian
 * @see LevelGraph
 */
public class LevelGraphTest {

    LevelGraph ig;
    MergedNode n1, n2;

    @Before
    public void setUp() {
        this.ig = new LevelGraph();
        this.n1 = this.ig.createNode();
        this.n2 = this.ig.createNode();
        this.ig.doAddNode(this.n1);
        this.ig.doAddNode(this.n2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void doAddEdge() {
        Edge e = this.ig.doAddEdge(this.n1, this.n2, false); // this should work
        this.ig.deleteEdge(e);
        // this should throw an exception, because InternalGraph only supports undirected edges
        this.ig.doAddEdge(this.n1, this.n2, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void doAddEdge2() {
        // this should work
        Edge e = this.ig.doAddEdge(this.n1, this.n2, false, new EdgeLabelAttribute("test"));
        this.ig.deleteEdge(e);
        // this should throw an exception, because InternalGraph only supports undirected edges
        this.ig.doAddEdge(this.n1, this.n2, true, new EdgeLabelAttribute("nope"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void doAddNode() {
        // create a node with an invalid node class
        Node nope = new AbstractNode(this.ig) {
            @Override
            public int compareTo(GraphElement o) {
                return 0;
            }

            @Override
            public Iterator<Edge> getDirectedInEdgesIterator() {
                return null;
            }

            @Override
            public Iterator<Edge> getDirectedOutEdgesIterator() {
                return null;
            }

            @Override
            public Iterator<Edge> getEdgesIterator() {
                return null;
            }

            @Override
            public Iterator<Edge> getUndirectedEdgesIterator() {
                return null;
            }

            @Override
            public void setGraph(Graph graph) {
            }

            @Override
            public int getDegree() {
                return 0;
            }
        };
        // this fails because InternalGraph only supports MergedNodes
        this.ig.doAddNode(nope);
    }

    @Test
    public void createNode() {
        MergedNode mn = this.ig.createNode(Collections.emptySet());
        assertTrue(mn.getInnerNodes().isEmpty());
    }

    @Test
    public void createNode2() {
        Node n = this.ig.createNode(new NodeLabelAttribute("ignored"));
        assertTrue(AttributeHelper.getLabel(n, "nope").contains("[0]"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEdge() {
        // this should throw an exception, because InternalGraph only supports undirected edges
        this.ig.createEdge(this.n1, this.n2, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEdge2() {
        // this should throw an exception, because InternalGraph only supports undirected edges
        this.ig.createEdge(this.n1, this.n2, true, new EdgeLabelAttribute("test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setDirected() {
        this.ig.setDirected(false); // this should work
        // this should throw an exception, because InternalGraph only supports undirected edges
        this.ig.setDirected(true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setDirected1() {
        this.ig.setDirected(false, true); // this should work
        // this should throw an exception, because InternalGraph only supports undirected edges
        this.ig.setDirected(true, true);
    }

    @Test
    public void isDirected() {
        assertFalse(this.ig.isDirected());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addGraph() {
        this.ig.addGraph(new LevelGraph()); // this should work
        AdjListGraph a = new AdjListGraph();
        a.addNode();
        a.setDirected(false);
        this.ig.addGraph(a); // this should throw an exception, because a contains a node that is not a MergedNode
    }

    @Test
    public void addNodeCopy() {
        this.ig.addNodeCopy(new MergedNode(this.ig));
        assertEquals(3, this.ig.getNodes().size());
    }

    @Test
    public void checkNoOverlappingMergedNodes() {
        Graph g = new AdjListGraph();
        MergedNode n1 = new MergedNode(g), n2 = new MergedNode(g), n3 = new MergedNode(g);
        this.n1.addInnerNode(n1);
        this.n1.addInnerNode(n2);
        this.n2.addInnerNode(n2);
        this.n2.addInnerNode(n3);
        assertFalse(this.ig.checkNoOverlappingMergedNodes(this.n2));
    }

    @Test
    public void testSetGetObject() {
        Color test = new Color(0xca, 0xff, 0xee);
        this.ig.setObject("key", test);
        assertSame(test, this.ig.getObject("key").orElse(null));

        assertFalse(this.ig.getObject("non_existent_key").isPresent());
    }

    @Test
    public void testGetMergedNodes() {
        assertEquals(new HashSet<Node>(Arrays.asList(this.n1, this.n2)), new HashSet<Node>(this.ig.getMergedNodes()));
        assertTrue(new LevelGraph().getMergedNodes().isEmpty());
    }

}