package org.vanted.plugins.layout.multilevelframework;

import org.AttributeHelper;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
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

    @Test
    public void createEdgeWeightComparator() {
        String path = "weight";
        Comparator<Edge> weightComparator = MlfHelper.createEdgeWeightComparator(path);
        AdjListGraph dummy = new AdjListGraph();
        Node n1 = dummy.addNode();
        Node n2 = dummy.addNode();
        Edge e1 = dummy.addEdge(n1, n2, true);
        Edge e2 = dummy.addEdge(n2, n1, true);
        e1.setDouble(path, 1);
        e2.setDouble(path, 0);
        assertTrue(weightComparator.compare(e1, e2) > 0);
        e1.setDouble(path, -134);
        e2.setDouble(path, 35);
        assertTrue(weightComparator.compare(e1, e2) < 0);
        e1.setDouble(path, 0);
        e2.setDouble(path, 0);
        assertEquals(0, weightComparator.compare(e1, e2));
    }

    @Test
    public void getEdgeWeight() {
        String path = "weight";
        AdjListGraph dummy = new AdjListGraph();
        Node n1 = dummy.addNode();
        Node n2 = dummy.addNode();
        Edge e = dummy.addEdge(n1, n2, false);
        e.setDouble(path, 123.2);
        assertEquals(123.2, MlfHelper.getEdgeWeight(e, path, 0), 0.0);
        e.removeAttribute(path);
        e.setFloat(path, 123.2f);
        assertEquals(123.2f, MlfHelper.getEdgeWeight(e, path, 0), 0.0);
        e.removeAttribute(path);
        e.setInteger(path, 1337);
        assertEquals(1337, MlfHelper.getEdgeWeight(e, path, 0), 0.0);
        e.removeAttribute(path);
        e.setLong(path, 1337L);
        assertEquals(1337L, MlfHelper.getEdgeWeight(e, path, 0), 0.0);
        e.removeAttribute(path);
        assertEquals(42, MlfHelper.getEdgeWeight(e, path, 42), 0.0);
        e.setString(path, "nope");
        assertEquals(42, MlfHelper.getEdgeWeight(e, path, 42), 0.0);
    }

    @Test
    public void tryMakingNewInstance() {
        String bOriginal = "b{2}|[^b]{2}";
        String notBOriginal = MlfHelper.tryMakingNewInstance(bOriginal);
        assertNotSame(bOriginal, notBOriginal);
    }

    @Test
    public void tryMakingNewInstanceFail() {
        class DummyFailure {
            private long iJustStoreAUselessDummyLong;
            public DummyFailure(long leLong) {
                this.iJustStoreAUselessDummyLong = leLong;
            }
            @SuppressWarnings("unused")
			public long getRidOfNotUsedWarning() {
                return this.iJustStoreAUselessDummyLong;
            }
        }
        DummyFailure original = new DummyFailure(0xFAAAA1L);
        // calling the ctor will fail in this method
        DummyFailure newOne = MlfHelper.tryMakingNewInstance(original);
        // so it should return the original as a fallback
        assertSame(original, newOne);
    }


}