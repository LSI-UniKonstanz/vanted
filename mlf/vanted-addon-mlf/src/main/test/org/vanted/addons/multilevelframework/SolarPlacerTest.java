package org.vanted.addons.multilevelframework;

import org.AttributeHelper;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.junit.Test;

import java.util.*;

import static org.AttributeHelper.getPositionX;
import static org.AttributeHelper.getPositionY;
import static org.junit.Assert.*;

public class SolarPlacerTest {

    /**
     * @author Gordian
     */
    @Test
    public void getParameters() {
        assertEquals(0, new SolarPlacer().getParameters().length);
    }

    /**
     * @author Gordian
     */
    @Test
    public void setParameters() {
        // shouldn't throw a NPE; the SolarPlacer doesn't (currently) have any parameters and
        // should just ignore this call
        new SolarPlacer().setParameters(null);
    }

    /**
     * @author Gordian
     */
    @Test
    public void getName() {
        String name = new SolarPlacer().getName();
        assertTrue(name.toLowerCase().contains("solar") && name.toLowerCase().contains("placer"));
    }

    /**
     * @author Gordian
     */
    @Test
    public void getDescription() {
        String description = new SolarPlacer().getDescription();
        assertTrue(description.toLowerCase().contains("solar") && description.toLowerCase().contains("placer"));
        // description needs to inform user that the placer can only be used with the solar merger
        assertTrue(description.toLowerCase().contains("solar merger"));
    }

    /**
     * @author Gordian
     */
    @Test
    public void reduceCoarseningLevel() {
        /* Graph looks like this
                  1
                  |
                  2   3
                 / \ /
                4---5
              /      \
             6        7
            /          \
           8            9--10--11  */
        Graph g = new AdjListGraph();
        Node n1 = g.addNode();
        Node n2 = g.addNode();
        Node n3 = g.addNode();
        Node n4 = g.addNode();
        Node n5 = g.addNode();
        Node n6 = g.addNode();
        Node n7 = g.addNode();
        Node n8 = g.addNode();
        Node n9 = g.addNode();
        Node n10 = g.addNode();
        Node n11 = g.addNode();
        AttributeHelper.setLabel(n1, "1");
        AttributeHelper.setLabel(n2, "2");
        AttributeHelper.setLabel(n3, "3");
        AttributeHelper.setLabel(n4, "4");
        AttributeHelper.setLabel(n5, "5");
        AttributeHelper.setLabel(n6, "6");
        AttributeHelper.setLabel(n7, "7");
        AttributeHelper.setLabel(n8, "8");
        AttributeHelper.setLabel(n9, "9");
        AttributeHelper.setLabel(n10, "10");
        AttributeHelper.setLabel(n11, "11");
        AttributeHelper.setPosition(n1, 1, 0);
        AttributeHelper.setPosition(n2, 1, 1);
        AttributeHelper.setPosition(n3, 1, 2);
        AttributeHelper.setPosition(n4, 0, 2);
        AttributeHelper.setPosition(n5, 2, 2);
        AttributeHelper.setPosition(n6, 0, 3);
        AttributeHelper.setPosition(n7, 2, 3);
        AttributeHelper.setPosition(n8, 0, 4);
        AttributeHelper.setPosition(n9, 2, 4);
        AttributeHelper.setPosition(n10, 3, 4);
        AttributeHelper.setPosition(n11, 4, 4);
        Arrays.asList(n3, n2, n4, n7).forEach(n -> g.addEdge(n5, n, false));
        g.addEdge(n1, n4, false);
        g.addEdge(n6, n4, false);
        g.addEdge(n6, n8, false);
        g.addEdge(n7, n9, false);
        g.addEdge(n1, n2, false);
        g.addEdge(n9, n10, false);
        g.addEdge(n10, n11, false);
        Set<Node> suns = new HashSet<>(Arrays.asList(n1, n3, n8, n9));
        Set<Node> planets = new HashSet<>(Arrays.asList(n2, n5, n6, n7, n10));
        Map<Node, Set<Node>> sun2planet = new HashMap<>();
        sun2planet.put(n1, Collections.singleton(n2));
        sun2planet.put(n8, Collections.singleton(n6));
        sun2planet.put(n9, new HashSet<>(Arrays.asList(n7, n10)));
        sun2planet.put(n3, Collections.singleton(n5));
        Map<Node, Set<Node>> planet2moon = new HashMap<>();
        planet2moon.put(n2, Collections.singleton(n4));
        planet2moon.put(n10, Collections.singleton(n11));

        MultilevelGraph mlg = new MultilevelGraph(g);
        mlg.newCoarseningLevel();
        MergedNode sun8 = mlg.addNode(new HashSet<>(Arrays.asList(n8, n6)));
        MergedNode sun9 = mlg.addNode(new HashSet<>(Arrays.asList(n9, n7, n10, n11)));
        MergedNode sun3 = mlg.addNode(new HashSet<>(Arrays.asList(n3, n5)));
        MergedNode sun1 = mlg.addNode(new HashSet<>(Arrays.asList(n1, n2, n4)));
        assertTrue(mlg.isComplete());

        ((LevelGraph) mlg.getTopLevel()).setObject(SolarMerger.SUNS_KEY, suns);
        ((LevelGraph) mlg.getTopLevel()).setObject(SolarMerger.SUN_TO_PLANETS_KEY, sun2planet);
        ((LevelGraph) mlg.getTopLevel()).setObject(SolarMerger.PLANETS_KEY, planets);
        ((LevelGraph) mlg.getTopLevel()).setObject(SolarMerger.PLANET_TO_MOONS_KEY, planet2moon);

        SolarPlacer sp = new SolarPlacer();
        double n10x = getPositionX(n10);
        double n10y = getPositionY(n10);
        double n11x = getPositionX(n11);
        double n11y = getPositionY(n11);
        sp.reduceCoarseningLevel(mlg);

        // assert planet between suns was placed correctly
        assertEquals(getPositionX(sun9) + 1.0/3.0 * (getPositionX(sun3) - getPositionX(sun9)),
                getPositionX(n7), 0.00001);
        assertEquals(getPositionY(sun9) + 1.0/3.0 * (getPositionY(sun3) - getPositionY(sun9)),
                getPositionY(n7), 0.00001);

        // assert moon on inter-solar-system-path was placed correctly
        assertEquals(((getPositionX(sun1) + 1.0/2.0 * (getPositionX(sun8) - getPositionX(sun1))) +
                + (getPositionX(sun1) + 1.0/2.0 * (getPositionX(sun3) - getPositionX(sun1))))/2.0,
                getPositionX(n4), 0.00001);
        assertEquals(((getPositionY(sun1) + 1.0/2.0 * (getPositionY(sun8) - getPositionY(sun1))) +
                        + (getPositionY(sun1) + 1.0/2.0 * (getPositionY(sun3) - getPositionY(sun1))))/2.0,
                getPositionY(n4), 0.00001);

        // assert position of "free" planets and moons was updated
        assertFalse(getPositionX(n10) == n10x);
        assertFalse(getPositionY(n10) == n10y);
        assertFalse(getPositionX(n11) == n11x);
        assertFalse(getPositionY(n11) == n11y);
    }

    /**
     * @author Gordian
     */
    @Test
    public void reduceCoarseningLevelTwoMoons()  {
        // 1 -- 2 -- 3 -- 4 -- 5 -- 6
        // S -- P -- M -- M -- P -- S
        Graph g = new AdjListGraph();
        Node n1 = g.addNode();
        Node n2 = g.addNode();
        Node n3 = g.addNode();
        Node n4 = g.addNode();
        Node n5 = g.addNode();
        Node n6 = g.addNode();
        AttributeHelper.setPosition(n1, 1, 1);
        AttributeHelper.setPosition(n2, 2, 2);
        AttributeHelper.setPosition(n3, 3, 3);
        AttributeHelper.setPosition(n4, 4, 4);
        AttributeHelper.setPosition(n5, 5, 5);
        AttributeHelper.setPosition(n6, 6, 6);
        AttributeHelper.setLabel(n1, "1'");
        AttributeHelper.setLabel(n2, "2'");
        AttributeHelper.setLabel(n3, "3'");
        AttributeHelper.setLabel(n4, "4'");
        AttributeHelper.setLabel(n5, "5'");
        AttributeHelper.setLabel(n6, "6'");
        g.addEdge(n1, n2, false);
        g.addEdge(n2, n3, false);
        g.addEdge(n3, n4, false);
        g.addEdge(n4, n5, false);
        g.addEdge(n5, n6, false);
        Set<Node> suns = new HashSet<>(Arrays.asList(n1, n6));
        Set<Node> planets = new HashSet<>(Arrays.asList(n2, n5));
        Map<Node, Set<Node>> sun2planet = new HashMap<>();
        sun2planet.put(n1, Collections.singleton(n2));
        sun2planet.put(n6, Collections.singleton(n5));
        Map<Node, Set<Node>> planet2moon = new HashMap<>();
        planet2moon.put(n2, Collections.singleton(n3));
        planet2moon.put(n5, Collections.singleton(n4));

        MultilevelGraph mlg = new MultilevelGraph(g);
        mlg.newCoarseningLevel();
        MergedNode sun1 = mlg.addNode(new HashSet<>(Arrays.asList(n1, n2, n3)));
        MergedNode sun6 = mlg.addNode(new HashSet<>(Arrays.asList(n4, n5, n6)));
        assertTrue(mlg.isComplete());

        ((LevelGraph) mlg.getTopLevel()).setObject(SolarMerger.SUNS_KEY, suns);
        ((LevelGraph) mlg.getTopLevel()).setObject(SolarMerger.SUN_TO_PLANETS_KEY, sun2planet);
        ((LevelGraph) mlg.getTopLevel()).setObject(SolarMerger.PLANETS_KEY, planets);
        ((LevelGraph) mlg.getTopLevel()).setObject(SolarMerger.PLANET_TO_MOONS_KEY, planet2moon);

        new SolarPlacer().reduceCoarseningLevel(mlg);

        assertEquals(getPositionX(sun1) + 1.0/5.0 * (getPositionX(sun6) - getPositionX(sun1)),
                getPositionX(n2), 0.00001);
        assertEquals(getPositionY(sun1) + 1.0/5.0 * (getPositionY(sun6) - getPositionY(sun1)),
                getPositionY(n2), 0.00001);
        assertEquals(getPositionX(sun1) + 4.0/5.0 * (getPositionX(sun6) - getPositionX(sun1)),
                getPositionX(n5), 0.00001);
        assertEquals(getPositionY(sun1) + 4.0/5.0 * (getPositionY(sun6) - getPositionY(sun1)),
                getPositionY(n5), 0.00001);
        assertEquals(getPositionX(sun1) + 2.0/5.0 * (getPositionX(sun6) - getPositionX(sun1)),
                getPositionX(n3), 0.00001);
        assertEquals(getPositionY(sun1) + 2.0/5.0 * (getPositionY(sun6) - getPositionY(sun1)),
                getPositionY(n3), 0.00001);
        assertEquals(getPositionX(sun1) + 3.0/5.0 * (getPositionX(sun6) - getPositionX(sun1)),
                getPositionX(n4), 0.00001);
        assertEquals(getPositionY(sun1) + 3.0/5.0 * (getPositionY(sun6) - getPositionY(sun1)),
                getPositionY(n4), 0.00001);
    }

    /**
     * @author Gordian
     */
    @Test
    public void reduceCoarseningLevelNoMoons() {
        // 1 -- 2 -- 3
        // S -- P -- S
        Graph g = new AdjListGraph();
        Node n1 = g.addNode();
        Node n2 = g.addNode();
        Node n3 = g.addNode();
        AttributeHelper.setPosition(n1, 1, 1);
        AttributeHelper.setPosition(n2, 2, 2);
        AttributeHelper.setPosition(n3, 3, 3);
        g.addEdge(n1, n2, false);
        g.addEdge(n2, n3, false);
        Set<Node> suns = new HashSet<>(Arrays.asList(n1, n3));
        Set<Node> planets = Collections.singleton(n2);
        Map<Node, Set<Node>> sun2planet = new HashMap<>();
        sun2planet.put(n1, Collections.singleton(n2));
        Map<Node, Set<Node>> planet2moon = Collections.emptyMap();

        MultilevelGraph mlg = new MultilevelGraph(g);
        mlg.newCoarseningLevel();
        MergedNode sun1 = mlg.addNode(new HashSet<>(Arrays.asList(n1, n2)));
        MergedNode sun3 = mlg.addNode(Collections.singleton(n3));
        assertTrue(mlg.isComplete());

        ((LevelGraph) mlg.getTopLevel()).setObject(SolarMerger.SUNS_KEY, suns);
        ((LevelGraph) mlg.getTopLevel()).setObject(SolarMerger.SUN_TO_PLANETS_KEY, sun2planet);
        ((LevelGraph) mlg.getTopLevel()).setObject(SolarMerger.PLANETS_KEY, planets);
        ((LevelGraph) mlg.getTopLevel()).setObject(SolarMerger.PLANET_TO_MOONS_KEY, planet2moon);

        new SolarPlacer().reduceCoarseningLevel(mlg);

        assertEquals(getPositionX(sun1) + 1.0/2.0 * (getPositionX(sun3) - getPositionX(sun1)),
                getPositionX(n2), 0.00001);
        assertEquals(getPositionY(sun1) + 1.0/2.0 * (getPositionY(sun3) - getPositionY(sun1)),
                getPositionY(n2), 0.00001);
    }

    /**
     * @author Gordian
     */
    @Test
    public void distance() {
        Graph g = new AdjListGraph();
        Node n1 = g.addNode();
        Node n2 = g.addNode();
        AttributeHelper.setPosition(n1, 0, 1);
        AttributeHelper.setPosition(n2, 2, 3);
        assertEquals(Math.sqrt(2*2+2*2), SolarPlacer.distance(n1, n2), 0.00001);
    }
}