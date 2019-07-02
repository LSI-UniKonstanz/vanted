package org.vanted.addons.stressminaddon;

import org.graffiti.plugin.algorithm.Algorithm;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the class {@link StressMinimizationAddon}.
 * @author Jannik
 */
public class StressMinimizationAddonTest {
    /** The object to test with */
    StressMinimizationAddon sma = new StressMinimizationAddon();

    /**
     * Test the method {@link StressMinimizationAddon#initializeAddon()}
     * @author Jannik
     */
    @Test
    public void initializeAddon() {
        sma.initializeAddon();
        Algorithm[] algorithms = sma.getAlgorithms();
        assertEquals("One algorithm loaded", 1, algorithms.length);
        assertTrue(algorithms[0] instanceof StressMinimizationLayout);
    }
}