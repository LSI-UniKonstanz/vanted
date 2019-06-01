package org.vanted.addons.stressminaddon.util;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.managers.pluginmgr.DefaultPluginManager;
import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import static data.TestGraphs.*;
import static org.junit.Assert.*;

/**
 * Test methods of {@link ConnectedComponentsHelper}-class.
 * @author Jannik
 */
public class ConnectedComponentsHelperTest {


    /**
     * Setup tests
     */
    @Before
    public void setUp() {
        // set up mock MainFrame for testing with GraphHelper
        if (MainFrame.getInstance() == null)
            new MainFrame(new DefaultPluginManager(Preferences.userRoot()), Preferences.userRoot());
    }

    /**
     * Test method {@link ConnectedComponentsHelper#getConnectedComponents(Collection)}.
     * @author Jannik
     */
    @Test
    public void getConnectedComponents() {
        
        // Test empty set
        assertTrue("empty", ConnectedComponentsHelper.getConnectedComponents(Collections.emptySet()).isEmpty());
        
        // Test complete graph
        Set<List<Node>> components = ConnectedComponentsHelper.getConnectedComponents(GRAPH_2_NODES);
        assertEquals("all num", 2, components.size());

        // Test if the correct nodes are included
        for (List<Node> component : components) {
            assertTrue("all contains",
                    component.containsAll(GRAPH_2_NODES.subList(0, GRAPH_2_NODES.size() / 2)) ||
                            component.containsAll(GRAPH_2_NODES.subList(GRAPH_2_NODES.size() / 2, GRAPH_2_NODES.size())));
        }

        final Iterator<List<Node>> it = components.iterator();
        // test disjointness
        assertTrue("all disjoint", Collections.disjoint(it.next(), it.next()));

        // test selection
        ArrayList<Node> selection = new ArrayList<>();
        for (int i = 0; i < GRAPH_2_NODES.size(); i+=2) {
            selection.add(GRAPH_2_NODES.get(i));
        }
        components = ConnectedComponentsHelper.getConnectedComponents(selection);
        assertEquals("selected num", selection.size(), components.size());

        // test unmodifiable
        List<Node> list = components.iterator().next();
        try { list.remove(0); fail("lists are modifiable");
        } catch (UnsupportedOperationException e) {}
        try { list.add(null); fail("lists are modifiable");
        } catch (UnsupportedOperationException e) {}

        // test random access
        assertTrue("list random access", list instanceof RandomAccess);
    }

    /**
     * Test method {@link ConnectedComponentsHelper#layoutConnectedComponents(Set)}.
     * @author Jannik
     */
    @Test
    public void layoutConnectedComponents() {
        // TODO test only one component
        // TODO test graphs with bends
        // create copy of working graph so that the actual graph isn't changed
        Graph graph = (Graph) GRAPH_2.copy();
        final List<Node> nodes = graph.getNodes();
        final Set<List<Node>> components = ConnectedComponentsHelper.getConnectedComponents(nodes);
        final List<Vector2d> oldPositions = nodes.stream().map(AttributeHelper::getPositionVec2d).collect(Collectors.toList());
        ConnectedComponentsHelper.layoutConnectedComponents(components);
        final List<Vector2d> newPositions = nodes.stream().map(AttributeHelper::getPositionVec2d).collect(Collectors.toList());


        // only test whether something was changed TODO maybe deeper test
        Vector2d oldPos, newPos;
        for (int pos = 0; pos < nodes.size(); pos++) {
            oldPos = oldPositions.get(pos);
            newPos = newPositions.get(pos);

            assertTrue("Node was moved", oldPos.distance(newPos) > 0);
        }
    }

    /**
     * Test method {@link ConnectedComponentsHelper#getConnectedComponents(Collection)}.
     * @author Jannik
     */
    @Test
    public void getConnectedComponetBounds() {
        // Graph 1
        Rectangle2D.Double expectedBounds = new Rectangle2D.Double(0.5, 0.5, 2, 2);
        Rectangle2D.Double bounds         = ConnectedComponentsHelper.getConnectedComponetBounds(GRAPH_1_NODES,
                0, 0, 0, 0);
        assertEquals("Graph 1: x", expectedBounds.x, bounds.x, 0.001);
        assertEquals("Graph 1: y", expectedBounds.y, bounds.y, 0.001);
        assertEquals("Graph 1: width", expectedBounds.width, bounds.width, 0.001);
        assertEquals("Graph 1: height", expectedBounds.height, bounds.height, 0.001);

        // Graph 1 (scaled margin)
        expectedBounds = new Rectangle2D.Double(-0.5, 0.3, 2*2, 2*1.2);
        bounds         = ConnectedComponentsHelper.getConnectedComponetBounds(GRAPH_1_NODES,
                0.5, 0.1, 0, 0);
        assertEquals("Graph 1 (scaled margin): x", expectedBounds.x, bounds.x, 0.001);
        assertEquals("Graph 1 (scaled margin): y", expectedBounds.y, bounds.y, 0.001);
        assertEquals("Graph 1 (scaled margin): width", expectedBounds.width, bounds.width, 0.001);
        assertEquals("Graph 1 (scaled margin): height", expectedBounds.height, bounds.height, 0.001);

        // Graph 1 (minimal margin)
        expectedBounds = new Rectangle2D.Double(-15.5, -7.5, 2+2*16, 2+2*8);
        bounds         = ConnectedComponentsHelper.getConnectedComponetBounds(GRAPH_1_NODES,
                0, 0, 16, 8);
        assertEquals("Graph 1 (minimal margin): x", expectedBounds.x, bounds.x, 0.001);
        assertEquals("Graph 1 (minimal margin): y", expectedBounds.y, bounds.y, 0.001);
        assertEquals("Graph 1 (minimal margin): width", expectedBounds.width, bounds.width, 0.001);
        assertEquals("Graph 1 (minimal margin): height", expectedBounds.height, bounds.height, 0.001);

        // Graph 2 (test node width)
        expectedBounds = new Rectangle2D.Double(-20, -20, 43, 43);
        bounds = ConnectedComponentsHelper.getConnectedComponetBounds(
                GRAPH_2_NODES.subList(GRAPH_2_NODES.size()/2, GRAPH_2_NODES.size()),
                0, 0, 0, 0);
        assertEquals("Graph 2 (node size): x", expectedBounds.x, bounds.x, 0.001);
        assertEquals("Graph 2 (node size): y", expectedBounds.y, bounds.y, 0.001);
        assertEquals("Graph 2 (node size): width", expectedBounds.width, bounds.width, 0.001);
        assertEquals("Graph 2 (node size): height", expectedBounds.height, bounds.height, 0.001);
    }

    /**
     * Test method {@link ConnectedComponentsHelper#getMaxNodeSize(List)}.
     * @author Jannik
     */
    @Test
    public void getMaxNodeSize() {
        // test graph 1: Should be 1
        assertEquals("graph 1", 1, ConnectedComponentsHelper.getMaxNodeSize(GRAPH_1_NODES), 0.001);
        // test graph 2: Should be 42
        assertEquals("graph 2", 42, ConnectedComponentsHelper.getMaxNodeSize(GRAPH_2_NODES), 0.001);
    }

    /**
     * Test exceptions method {@link ConnectedComponentsHelper#getMaxNodeSize(List)}.
     * @author Jannik
     */
    @Test
    public void exceptions() {
        // non-null
        try {
            ConnectedComponentsHelper.getConnectedComponents(null); fail("No exception thrown");
        } catch (NullPointerException e) {
        } catch (Throwable t) {
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }
        try {
            ConnectedComponentsHelper.layoutConnectedComponents(null); fail("No exception thrown");
        } catch (NullPointerException e) {
        } catch (Throwable t) {
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }
        try {
            ConnectedComponentsHelper.getConnectedComponetBounds(null,
                    0, 0, 0, 0);
            fail("No exception thrown");
        } catch (NullPointerException e) {
        } catch (Throwable t) {
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }
        try {
            ConnectedComponentsHelper.getMaxNodeSize(null); fail("No exception thrown");
        } catch (NullPointerException e) {
        } catch (Throwable t) {
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }

        // negative fractions
        try {
            ConnectedComponentsHelper.getConnectedComponetBounds(GRAPH_2_NODES,
                    -0.5, 0, 0, 0);
            fail("No exception thrown");
        } catch (IllegalArgumentException e) {
        } catch (Throwable t) {
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }
        try {
            ConnectedComponentsHelper.getConnectedComponetBounds(GRAPH_2_NODES,
                    0, -0.5, 0, 0);
            fail("No exception thrown");
        } catch (IllegalArgumentException e) {
        } catch (Throwable t) {
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }
    }
}