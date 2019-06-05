package org.vanted.addons.multilevelframework;

import org.AttributeHelper;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Node;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Gordian
 * @see MlfHelper
 */
public class MlfHelperTest {

    @Test
    public void calculateConnectedComponentsOfSelectionEmpty() {
        assertTrue(MlfHelper.calculateConnectedComponentsOfSelection(Collections.emptySet()).isEmpty());
    }

    @Test
    public void calculateConnectedComponentsOfSelection() {
        AdjListGraph g = new AdjListGraph();
        // circle n1, n2, n3
        Node n1 = g.addNode();
        Node n2 = g.addNode();
        Node n3 = g.addNode();
        g.addEdge(n1, n2, false);
        g.addEdge(n2, n3, false);
        g.addEdge(n1, n3, false);
        // circle n4, n5, n6, n7
        Node n4 = g.addNode();
        Node n5 = g.addNode();
        Node n6 = g.addNode();
        Node n7 = g.addNode();
        g.addEdge(n4, n5, false);
        g.addEdge(n5, n6, false);
        g.addEdge(n6, n7, false);
        g.addEdge(n7, n4, false);
        //isolated node
        Node isolated = g.addNode();

        for (Node n : Arrays.asList(n1, n2, n3, n4, n5, n6, n7, isolated)) {
            AttributeHelper.setPosition(n, 0, 0);
        }

        // select the triangle and the isolated node
        Collection<? extends CoarsenedGraph> components =
                MlfHelper.calculateConnectedComponentsOfSelection(new HashSet<>(Arrays.asList(n1, n2, n3, isolated)));

        for (CoarsenedGraph cg : components) {
            if (cg.getMergedNodes().size() == 1) { // this has to be the isolated node
                Collection<?> a = cg.getMergedNodes().iterator().next().getInnerNodes();
                assertEquals(1, a.size());
                assertTrue(a.contains(isolated));
                assertEquals(1, MlfHelper.getConnectedNodes(cg.getMergedNodes().iterator().next(),
                        new HashSet<>(cg.getMergedNodes())).size());
            } else if (cg.getMergedNodes().size() == 3) { // this has to be the triangle
                HashSet<Node> nodes = new HashSet<>();
                for (MergedNode m : cg.getMergedNodes()) {
                    nodes.addAll(m.getInnerNodes());
                }
                assertEquals(new HashSet<>(Arrays.asList(n1, n2, n3)), nodes);
                assertEquals(3, MlfHelper.getConnectedNodes(cg.getMergedNodes().iterator().next(),
                        new HashSet<>(cg.getMergedNodes())).size());
            } else {
                // the selection only contained the isolated node and the triangle, so any other size would be an error
                fail("More/other connected components than expected");
            }
        }

        // select the two circles
        Collection<? extends CoarsenedGraph> components2 =
                MlfHelper.calculateConnectedComponentsOfSelection(
                        new HashSet<>(Arrays.asList(n1, n2, n3, n4, n5, n6, n7)));

        for (CoarsenedGraph cg : components2) {
            if (cg.getMergedNodes().size() == 4) {
                HashSet<Node> nodes = new HashSet<>();
                for (MergedNode m : cg.getMergedNodes()) {
                    nodes.addAll(m.getInnerNodes());
                }
                assertEquals(new HashSet<>(Arrays.asList(n4, n5, n6, n7)), nodes);
                assertEquals(4, MlfHelper.getConnectedNodes(cg.getMergedNodes().iterator().next(),
                        new HashSet<>(cg.getMergedNodes())).size());
            } else if (cg.getMergedNodes().size() == 3) {
                HashSet<Node> nodes = new HashSet<>();
                for (MergedNode m : cg.getMergedNodes()) {
                    nodes.addAll(m.getInnerNodes());
                }
                assertEquals(new HashSet<>(Arrays.asList(n1, n2, n3)), nodes);
                assertEquals(3, MlfHelper.getConnectedNodes(cg.getMergedNodes().iterator().next(),
                        new HashSet<>(cg.getMergedNodes())).size());
            } else {
                fail("More/other connected components than expected");
            }
        }

        // select two parts of the circle that are not directly connected
        Collection<? extends CoarsenedGraph> components3 =
                MlfHelper.calculateConnectedComponentsOfSelection(
                        new HashSet<>(Arrays.asList(n4, n6)));
        assertEquals(2, components3.size());
    }

    @Test
    public void getConnectedNodes() {
        AdjListGraph g = new AdjListGraph();
        // circle n1, n2, n3
        Node n1 = g.addNode();
        Node n2 = g.addNode();
        Node n3 = g.addNode();
        g.addEdge(n1, n2, false);
        g.addEdge(n2, n3, false);
        g.addEdge(n1, n3, false);
        //isolated node
        Node isolated = g.addNode();

        Set<Node> res = MlfHelper.getConnectedNodes(n1, new HashSet<>(Arrays.asList(n1, n2, isolated)));
        assertEquals(res, new HashSet<>(Arrays.asList(n1, n2)));

        Set<Node> res2 = MlfHelper.getConnectedNodes(n1, new HashSet<>(Arrays.asList(n1, n2, n3)));
        assertEquals(res2, new HashSet<>(Arrays.asList(n1, n2, n3)));

        assertEquals(MlfHelper.getConnectedNodes(isolated, Collections.singleton(isolated)),
                Collections.singleton(isolated));

        assertEquals(MlfHelper.getConnectedNodes(isolated, Collections.emptySet()),
                Collections.singleton(isolated));
    }
}