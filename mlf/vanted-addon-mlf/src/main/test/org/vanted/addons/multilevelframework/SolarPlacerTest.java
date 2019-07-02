package org.vanted.addons.multilevelframework;

import org.AttributeHelper;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.AdjListNode;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.junit.Test;

import java.util.Arrays;

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
    public void distance() {
        Graph g = new AdjListGraph();
        Node n1 = g.addNode();
        Node n2 = g.addNode();
        AttributeHelper.setPosition(n1, 0, 1);
        AttributeHelper.setPosition(n2, 2, 3);
        assertEquals(Math.sqrt(2*2+2*2), SolarPlacer.distance(n1, n2), 0.00001);
    }
}