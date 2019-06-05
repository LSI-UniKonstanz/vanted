package org.vanted.addons.multilevelframework;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.NodeLabelAttribute;
import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * @author Gordian
 * @see MergedNode
 */
public class MergedNodeTest {

    private Graph g;
    private HashSet<Node> innerNodes;
    private Node n1, n2, n3, n4;

    @Before
    public void setUp() throws Exception {
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
        g.addEdge(n1, n2, false);
        g.addEdge(n2, n3, false);
        g.addEdge(n3, n4, false);
        g.addEdge(n4, n2, false);
    }

    @Test
    public void constructor() {
        CollectionAttribute ca = new NodeLabelAttribute("adsf");
        MergedNode mn = new MergedNode(this.g, ca);
        assertEquals(mn.getAttributes(), ca);
        assertTrue(AttributeHelper.getLabel(mn, "xxx").contains("[0]"));
    }

    @Test
    public void getInnerNodes() {
        MergedNode mn = new MergedNode(this.g, this.innerNodes);
        assertSame(this.innerNodes.size(), mn.getWeight());
        assertTrue(innerNodes.containsAll(mn.getInnerNodes()));
        assertTrue(mn.getInnerNodes().containsAll(innerNodes));
    }

    @Test
    public void getWeight() {
        MergedNode mn = new MergedNode(this.g, Collections.singleton(this.n1));
        assertSame(1, mn.getWeight());
        assertSame(0, (new MergedNode(this.g, Collections.emptySet())).getWeight());
    }

    @Test
    public void testLabel() {
        MergedNode mn = new MergedNode(this.g, this.innerNodes);
        String label = AttributeHelper.getLabel(mn, "xxx");
        assertTrue(label.contains("[" + this.innerNodes.size() + "]"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addInnerNode() {
        MergedNode mn = new MergedNode(this.g, this.innerNodes);
        mn.addInnerNode(this.n1);
    }

    @Test
    public void updatePositionFail() {
        Graph g = new AdjListGraph();
        Node n = g.addNode();
        AttributeHelper.setPosition(n, Double.NaN, 123);
        MergedNode mn = new MergedNode(g, Collections.singleton(n));
        assertEquals(AttributeHelper.getPosition(mn), new Point2D.Double(0, 0));
        AttributeHelper.setPosition(n, Double.NEGATIVE_INFINITY, 123);
        mn = new MergedNode(g, Collections.singleton(n));
        assertEquals(AttributeHelper.getPosition(mn), new Point2D.Double(0, 0));
    }

    @Test
    public void addInnerNode2() {
        MergedNode mn = new MergedNode(this.g);
        assertSame(0, mn.getWeight());
        mn.addInnerNode(this.n1);
        assertSame(1, mn.getWeight());
        mn.addInnerNode(this.n2);
        assertSame(2, mn.getWeight());
        assertTrue(mn.getInnerNodes().containsAll(Arrays.asList(n1, n2)));
        assertTrue(Arrays.asList(n1, n2).containsAll(mn.getInnerNodes()));
    }
}