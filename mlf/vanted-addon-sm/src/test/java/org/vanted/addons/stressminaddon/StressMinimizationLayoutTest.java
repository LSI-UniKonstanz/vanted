package org.vanted.addons.stressminaddon;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.NullView;
import org.Vector2d;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;
import org.junit.Before;
import org.junit.Test;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static data.TestGraphs.*;
import static org.junit.Assert.*;

/**
 * Test the {@link StressMinimizationLayout}-class
 * @author Jannik
 */
public class StressMinimizationLayoutTest {

    /* Access to private methods to test them. */
    private Method stressMin;
    private Method posDiff;

    /** The object to test with. */
    private StressMinimizationLayout layout;

    /**
     * Set up testing
     * @author Jannik
     */
    @Before
    public void setUp() throws Exception {
        // get private methods to test
        Objects.requireNonNull(stressMin = StressMinimizationLayout.class.getDeclaredMethod(
                "calculateStress", List.class, List.class, NodeValueMatrix.class, NodeValueMatrix.class));
        Objects.requireNonNull(posDiff = StressMinimizationLayout.class.getDeclaredMethod(
                "differencePositionsSmallerEpsilon", List.class, List.class, double.class));
        layout = new StressMinimizationLayout();

    }

    /**
     * Calls a private method.
     *
     * @param obj the object to call the function from.
     * @param method The method (object) to call.
     * @param args The arguments to be used
     *
     * @return what the invoked method returned.
     *
     * @throws InvocationTargetException
     *      if the underlying method throws an exception.
     *
     * @see Method#invoke(Object, Object...)
     * @author Jannik
     */
    private Object invokePrivateMethod(final StressMinimizationLayout obj, final Method method, final Object... args)
            throws InvocationTargetException {
        try {
            method.setAccessible(true);
            return method.invoke(obj, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("This shouldn't happen!", e);
        }
    }

    /**
     * Test the private method
     * StressMinimizationLayout#differencePositionsSmallerEpsilon(ArrayList, ArrayList, double)
     * @throws InvocationTargetException if the method was called incorrectly (This shouldn't happen)
     * @author Jannik
     */
    @Test
    public void differencePositionsSmallerEpsilon() throws InvocationTargetException {
        // Create test data ( all points are rotated )
        ArrayList<Vector2d> oldList = new ArrayList<>(Arrays.asList(
                        new Vector2d(1,1), new Vector2d(1,2), new Vector2d(2, 2), new Vector2d(2, 1)));
        ArrayList<Vector2d> newList = new ArrayList<>(Arrays.asList(
                new Vector2d(1,2), new Vector2d(2, 2), new Vector2d(2, 1), new Vector2d(1,1)));

        // test with data
        // all the differences should be 1
        assertTrue((boolean) invokePrivateMethod(null, this.posDiff, oldList, newList, 1.125));
        assertFalse((boolean) invokePrivateMethod(null, this.posDiff, oldList, newList, 1.0));
    }

    /**
     * Test the private method
     * StressMinimizationLayout#calculateStress(List, NodeValueMatrix, NodeValueMatrix)
     * @throws InvocationTargetException if the method was called incorrectly (This shouldn't happen)
     * @author Jannik
     */
    @Test
    public void calculateStress() throws InvocationTargetException {

        // calculate the standard weights
        NodeValueMatrix weights = GRAPH_1_DISTANCES.clone().apply(x -> Math.pow(x, -2));

        // test the method
        // expected value calculated by hand.
        double stress = (double) invokePrivateMethod(null, stressMin,
                GRAPH_1_NODES, GRAPH_1_POSITIONS, GRAPH_1_DISTANCES, weights);
        assertEquals(3 -2*Math.sqrt(2), stress, 0.001);

        // Test only one node edge case
        stress = (double) invokePrivateMethod(null, stressMin,
                Collections.singletonList(GRAPH_1_NODES.get(0)), GRAPH_1_POSITIONS.subList(0, 1),
                new NodeValueMatrix(1), new NodeValueMatrix(1));
        assertEquals(0.0, stress, 0.001);
    }

    @Test
    public void getDescription() {
        // TODO
    }

    /**
     * Test name given by algorithm.
     * @author Jannik
     */
    @Test
    public void getName() {
        assertEquals("Stress Minimization", layout.getName());
    }

    /**
     * Test checking method of algorithm.
     * @author Jannik
     */
    @Test
    public void check() {
        // check normal graph
        try {
            layout.attach(GRAPH_1, new Selection(GRAPH_1_NODES));
            layout.check();
        } catch (PreconditionException e) {
            e.printStackTrace();
            fail("No exception should have been thrown!");
        }

        // check null graph
        try {
            layout.attach(null, null);
            layout.check();
            fail("No exception thrown!");
        } catch (PreconditionException e) {}
        catch (Throwable t) {
            fail("Wrong Throwable thrown.");
        }

        // check empty graph
        try {
            layout.attach(new AdjListGraph(), new Selection());
            layout.check();
            fail("No exception thrown!");
        } catch (PreconditionException e) {}
        catch (Throwable t) {
            fail("Wrong Throwable thrown.");
        }
    }

    @Test
    public void execute() {
        // TODO
    }

    @Test
    public void getParameters() {
        // TODO
    }

    @Test
    public void setParameters() {
        // TODO
    }

    /**
     * Test category given by algorithm.
     * @author Jannik
     */
    @Test
    public void getCategory() {
        assertEquals("Layout", layout.getCategory());
    }

    /**
     * Test method {@link StressMinimizationLayout#isLayoutAlgorithm()}.
     * @author Jannik
     */
    @Test
    public void isLayoutAlgorithm() {
        assertTrue(layout.isLayoutAlgorithm());
    }

    /**
     * Test method {@link StressMinimizationLayout#activeForView(View)}.
     * @author Jannik
     */
    @Test
    public void activeForView() {
        assertFalse(layout.activeForView(null));
        assertTrue(layout.activeForView(new NullView()));
    }
}