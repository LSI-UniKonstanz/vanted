package org.vanted.addons.stressminimization;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.junit.Test;
import org.vanted.addons.stressminimization.primitives.IndexedNodeSet;
import org.vanted.addons.stressminimization.primitives.IndexedNodeSet.NotIndexedException;

/**
 * General test cases for IndexedNodeSet
 */
public class TestIndexedNodeSet {

    // ==============
    // MARK: creation
    // ==============

    @Test
    public void testAllNodesCreation() {

        Graph g = new AdjListGraph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 10; i += 1) {
            nodes.add(g.addNode());
        }

        IndexedNodeSet nodeSet = IndexedNodeSet.setOfAllIn(nodes);

        for (int i = 0; i < nodes.size(); i += 1) {
            assert (nodeSet.contains(i));
            assert (nodeSet.contains(nodes.get(i)));
        }

    }

    @Test
    public void testEmptyCreation() {

        Graph g = new AdjListGraph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 10; i += 1) {
            nodes.add(g.addNode());
        }

        IndexedNodeSet nodeSet = IndexedNodeSet.emptySetOf(nodes);

        for (int i = 0; i < nodes.size(); i += 1) {
            assert (!nodeSet.contains(i));
            assert (!nodeSet.contains(nodes.get(i)));
        }

        assert (nodeSet.isEmpty());
        assert (nodeSet.size() == 0);

    }

    @Test
    public void testEmptySubsetCreation() {

        Graph g = new AdjListGraph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 10; i += 1) {
            nodes.add(g.addNode());
        }

        IndexedNodeSet superSet = IndexedNodeSet.emptySetOf(nodes);

        IndexedNodeSet subSet = superSet.emptySubset();

        assert (subSet.isEmpty());

    }

    @Test
    public void testSubsetCreationWithContainedElements() {

        Graph g = new AdjListGraph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 10; i += 1) {
            nodes.add(g.addNode());
        }

        IndexedNodeSet superSet = IndexedNodeSet.emptySetOf(nodes);

        superSet.add(0);
        superSet.add(2);
        superSet.add(3);
        superSet.add(7);
        superSet.add(9);

        IndexedNodeSet subSet = superSet.setOfContainedNodesWithOwnIndices();

        assert (subSet.contains(nodes.get(0)));
        assert (subSet.contains(nodes.get(2)));
        assert (subSet.contains(nodes.get(3)));
        assert (subSet.contains(nodes.get(7)));
        assert (subSet.contains(nodes.get(9)));

        assert (subSet.getIndex(nodes.get(0)) == 0);
        assert (subSet.getIndex(nodes.get(2)) == 1);
        assert (subSet.getIndex(nodes.get(3)) == 2);
        assert (subSet.getIndex(nodes.get(7)) == 3);
        assert (subSet.getIndex(nodes.get(9)) == 4);


    }

    // ==============
    // MARK: indexing
    // ==============

    @Test
    public void testAllNodesIndexedAsExpected() {

        Graph g = new AdjListGraph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 10; i += 1) {
            nodes.add(g.addNode());
        }

        IndexedNodeSet nodeSet = IndexedNodeSet.emptySetOf(nodes);
        nodeSet.add(1);
        nodeSet.add(3);
        nodeSet.add(nodes.get(6));
        nodeSet.add(7);
        nodeSet.add(nodes.get(0));

        assert (nodeSet.getIndex(nodes.get(0)) == 0);
        assert (nodeSet.getIndex(nodes.get(1)) == 1);
        assert (nodeSet.getIndex(nodes.get(3)) == 3);
        assert (nodeSet.getIndex(nodes.get(6)) == 6);
        assert (nodeSet.getIndex(nodes.get(7)) == 7);

    }

    @Test
    public void testNotContainedNodeNotIndexed() {

        Graph g = new AdjListGraph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 10; i += 1) {
            nodes.add(g.addNode());
        }

        Node specialNode = g.addNode();

        IndexedNodeSet nodeSet = IndexedNodeSet.setOfAllIn(nodes);
        try {
            nodeSet.getIndex(specialNode);
            fail();
        } catch (IndexedNodeSet.NotIndexedException ex) {
        }

    }

    @Test
    public void testIndexAccess() {

        Graph g = new AdjListGraph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 10; i += 1) {
            nodes.add(g.addNode());
        }

        IndexedNodeSet nodeSet = IndexedNodeSet.setOfAllIn(nodes);

        for (int i = 0; i < nodes.size(); i += 1) {
            assert (nodeSet.get(i).equals(nodes.get(i)));
        }

    }

    // ==============================
    // MARK: add, remove and contains
    // ==============================

    @Test
    public void testNodeAddContainsAndRemove() {

        Graph g = new AdjListGraph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 10; i += 1) {
            nodes.add(g.addNode());
        }

        Node n1 = nodes.get(2);
        Node n2 = nodes.get(9);
        Node n3 = nodes.get(7);

        IndexedNodeSet nodeSet = IndexedNodeSet.emptySetOf(nodes);

        nodeSet.add(n3);
        assert (nodeSet.contains(n3));

        nodeSet.add(n1);
        assert (nodeSet.contains(n1));

        nodeSet.remove(n1);
        assert (!nodeSet.contains(n1));

        nodeSet.add(n2);
        assert (nodeSet.contains(n2));

        nodeSet.remove(n3);
        assert (!nodeSet.contains(n3));

        nodeSet.remove(n2);
        assert (!nodeSet.contains(n2));

    }

    @Test
    public void testIndicesAddContainsAndRemove() {

        Graph g = new AdjListGraph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 10; i += 1) {
            nodes.add(g.addNode());
        }

        int i1 = 0;
        int i2 = 4;
        int i3 = 8;

        IndexedNodeSet nodeSet = IndexedNodeSet.emptySetOf(nodes);

        nodeSet.add(i1);
        nodeSet.add(i2);
        nodeSet.add(i3);

        assert (nodeSet.contains(i1));
        assert (nodeSet.contains(i2));
        assert (nodeSet.contains(i3));

        nodeSet.remove(i3);
        assert (!nodeSet.contains(i3));

        nodeSet.remove(i1);
        assert (!nodeSet.contains(i1));

        nodeSet.add(i3);
        assert (nodeSet.contains(i3));

        nodeSet.remove(i2);
        assert (!nodeSet.contains(i2));

        nodeSet.remove(i3);
        assert (!nodeSet.contains(i3));

    }

    // ====================
    // MARK: set operations
    // ====================

    @Test
    public void testUnion() {

        Graph g = new AdjListGraph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 10; i += 1) {
            nodes.add(g.addNode());
        }

        IndexedNodeSet set1 = IndexedNodeSet.emptySetOf(nodes);
        IndexedNodeSet set2 = IndexedNodeSet.emptySetOf(nodes);

        set1.add(2);
        set1.add(5);
        set1.add(8);

        set2.add(3);
        set2.add(5);
        set2.add(6);
        set2.add(7);

        set1.union(set2);

        assert (set1.contains(2));
        assert (set1.contains(3));
        assert (set1.contains(5));
        assert (set1.contains(6));
        assert (set1.contains(7));
        assert (set1.contains(8));
        assert (!set1.contains(0));
        assert (!set1.contains(1));
        assert (!set1.contains(4));
        assert (!set1.contains(9));

        // check set2 is unchanged
        assert (set2.contains(3));
        assert (set2.contains(5));
        assert (set2.contains(6));
        assert (set2.contains(7));
        assert (!set2.contains(0));
        assert (!set2.contains(1));
        assert (!set2.contains(2));
        assert (!set2.contains(4));
        assert (!set2.contains(8));
        assert (!set2.contains(9));

        set2.add(0);
        set2.add(1);
        set2.add(4);
        set2.add(9);

        set1.union(set2);

        for (int i = 0; i < 10; i += 1) {
            assert (set1.contains(i));
        }

    }

    @Test
    public void testIntersection() {

        Graph g = new AdjListGraph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 10; i += 1) {
            nodes.add(g.addNode());
        }

        IndexedNodeSet set1 = IndexedNodeSet.emptySetOf(nodes);
        IndexedNodeSet set2 = IndexedNodeSet.emptySetOf(nodes);

        set1.add(0);
        set1.add(2);
        set1.add(4);
        set1.add(6);

        set2.add(1);
        set2.add(3);
        set2.add(5);
        set2.add(7);
        set2.add(8);
        set2.add(9);

        set1.intersection(set2);

        assert (set1.isEmpty());

        set1.add(3);

        set1.intersection(set2);

        assert (set1.contains(3));

        assert (!set1.contains(0));
        assert (!set1.contains(1));
        assert (!set1.contains(2));
        assert (!set1.contains(4));
        assert (!set1.contains(5));
        assert (!set1.contains(6));
        assert (!set1.contains(7));
        assert (!set1.contains(8));
        assert (!set1.contains(9));

        set1.add(4);
        set2.add(4);

        set1.intersection(set2);

        assert (set1.contains(3));
        assert (set1.contains(4));

        assert (!set1.contains(0));
        assert (!set1.contains(1));
        assert (!set1.contains(2));
        assert (!set1.contains(5));
        assert (!set1.contains(6));
        assert (!set1.contains(7));
        assert (!set1.contains(8));
        assert (!set1.contains(9));

    }


    @Test
    public void testSetMinus() {

        Graph g = new AdjListGraph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 10; i += 1) {
            nodes.add(g.addNode());
        }

        IndexedNodeSet set1 = IndexedNodeSet.setOfAllIn(nodes);
        IndexedNodeSet set2 = IndexedNodeSet.emptySetOf(nodes);

        set1.remove(1);

        set2.add(3);
        set2.add(4);
        set2.add(5);
        set2.add(9);

        set1.setMinus(set2);

        assert (set1.contains(0));
        assert (set1.contains(2));
        assert (set1.contains(6));
        assert (set1.contains(7));
        assert (set1.contains(8));
        assert (!set1.contains(1));
        assert (!set1.contains(3));
        assert (!set1.contains(4));
        assert (!set1.contains(5));
        assert (!set1.contains(9));

        set2.add(1);
        set2.add(2);

        set1.setMinus(set2);

        assert (set1.contains(0));
        assert (set1.contains(6));
        assert (set1.contains(7));
        assert (set1.contains(8));
        assert (!set1.contains(1));
        assert (!set1.contains(2));
        assert (!set1.contains(3));
        assert (!set1.contains(4));
        assert (!set1.contains(5));
        assert (!set1.contains(9));

    }

    @Test
    public void testComplement() {

        Graph g = new AdjListGraph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 10; i += 1) {
            nodes.add(g.addNode());
        }

        IndexedNodeSet set1 = IndexedNodeSet.emptySetOf(nodes);

        set1.add(9);
        set1.add(8);
        set1.add(7);
        set1.add(6);

        set1.complement();

        assert (set1.contains(0));
        assert (set1.contains(1));
        assert (set1.contains(2));
        assert (set1.contains(3));
        assert (set1.contains(4));
        assert (set1.contains(5));

    }

    // ===============
    // MARK: iteration
    // ===============

    @Test
    public void testIterationOrder() {

        Graph g = new AdjListGraph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 10; i += 1) {
            nodes.add(g.addNode());
        }

        IndexedNodeSet nodeSet = IndexedNodeSet.setOfAllIn(nodes);

        nodeSet.remove(7);

        int lastIndex = -1;
        for (int index : nodeSet) {
            assert (index != 7);
            assert (index > lastIndex);
            lastIndex = index;
        }

    }

    // ===================
    // MARK: neighborhoods
    // ===================

    @Test
    public void testNeighborhoodExtraction() {

        Graph g = new AdjListGraph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 10; i += 1) {
            nodes.add(g.addNode());
        }

        g.addEdge(nodes.get(3), nodes.get(5), false);
        g.addEdge(nodes.get(3), nodes.get(2), false);
        g.addEdge(nodes.get(3), nodes.get(9), false);

        IndexedNodeSet nodeSet = IndexedNodeSet.setOfAllIn(nodes);

        IndexedNodeSet neighbors = nodeSet.getNeighbors(3);

        for (int index : neighbors) {
            assert (index == 5 || index == 2 || index == 9);
        }

    }

}
