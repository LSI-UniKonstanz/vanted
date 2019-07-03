package org.vanted.addons.stressminaddon.util;

import org.Vector2d;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.junit.Test;

import java.util.List;

import static data.TestGraphs.*;
import static org.junit.Assert.*;

/**
 * Test class {@link NullPlacer}
 * @author Jannik
 */
public class NullPlacerTest {

    /** The {@link NullPlacer} to test. */
    NullPlacer np = new NullPlacer();

    /**
     * Test method {@link NullPlacer#calculateInitialPositions(List, NodeValueMatrix)}
     * @author Jannik
     */
    @Test
    public void calculateInitialPositions() {
        // check if positions are returned unchanged
        List<Vector2d> result = np.calculateInitialPositions(GRAPH_1_NODES, GRAPH_1_DISTANCES);
        for (int idx = 0; idx < GRAPH_1_POSITIONS.size(); idx++) {
            assertEquals("Positions did not change", GRAPH_1_POSITIONS.get(idx).x, result.get(idx).x, 0.0001);
            assertEquals("Positions did not change", GRAPH_1_POSITIONS.get(idx).y, result.get(idx).y, 0.0001);
        }
    }

    /**
     * Test method {@link NullPlacer#getName()}
     * @author Jannik
     */
    @Test
    public void getName() {
        // only test whether the name would be readable for user
        assertNotNull(np.getName());
        assertFalse(np.getName().trim().isEmpty());
    }

    /**
     * Test method {@link NullPlacer#getDescription()}
     * @author Jannik
     */
    @Test
    public void getDescription() {
        // only test whether the description would be readable for user
        assertNotNull(np.getDescription());
        assertFalse(np.getDescription().trim().isEmpty());
    }

    /**
     * Test method {@link NullPlacer#getParameters()}}
     * @author Jannik
     */
    @Test
    public void getParameters() {
        // should not have any parameters
        assertTrue(np.getParameters() == null || np.getParameters().length == 0);
    }

    /**
     * Test method {@link NullPlacer#setParameters(Parameter[])}
     * @author Jannik
     */
    @Test
    public void setParameters() {
        // should do nothing
        np.setParameters(new Parameter[] {new BooleanParameter(true, "TEST", "test")});
        calculateInitialPositions(); // should still be executed correctly
    }
}