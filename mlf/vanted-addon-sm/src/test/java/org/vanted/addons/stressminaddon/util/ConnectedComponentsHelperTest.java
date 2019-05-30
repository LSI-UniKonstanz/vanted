package org.vanted.addons.stressminaddon.util;

import org.graffiti.graph.Node;
import org.junit.Test;

import java.util.*;

import static data.TestGraphs.GRAPH_2_NODES;
import static org.junit.Assert.*;

/**
 * Test methods of {@link ConnectedComponentsHelper}-class.
 * @author Jannik
 */
public class ConnectedComponentsHelperTest {

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
}