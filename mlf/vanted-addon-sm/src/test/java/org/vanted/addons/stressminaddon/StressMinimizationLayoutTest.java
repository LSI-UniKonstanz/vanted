package org.vanted.addons.stressminaddon;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.NullView;
import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.managers.pluginmgr.DefaultPluginManager;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;
import org.vanted.addons.stressminaddon.util.NullPlacer;
import org.vanted.addons.stressminaddon.util.gui.EnableableNumberParameter;
import org.vanted.addons.stressminaddon.util.gui.Parameterizable;
import org.vanted.addons.stressminaddon.util.gui.ParameterizableSelectorParameter;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.*;
import java.util.prefs.Preferences;

import static data.TestGraphs.*;
import static org.junit.Assert.*;
import static org.vanted.addons.stressminaddon.StressMinimizationLayout.*;

/**
 * Test the {@link StressMinimizationLayout}-class
 * @author Jannik
 */
public class StressMinimizationLayoutTest {

    /* Access to private methods to test them. */
    private Method stressMinMethod;
    private Method posDiffMethod;

    /** The object to test with. */
    private StressMinimizationLayout sm;


    /*
     * Set up a {@link MainFrame} for execute to work
     * @author Jannik
     */
    @BeforeClass
    public static void setUpVANTED() {
        new MainFrame(new DefaultPluginManager(Preferences.userRoot()), Preferences.userRoot());
    }

    /*
     * Cleanly close possible invisible {@link MainFrame}
     * @author Jannik
     */
    @AfterClass
    public static void tearDownVANTED() {
        for (Window w : Window.getWindows()) {
            w.dispose();
        }
    }

    /**
     * Set up testing
     * @author Jannik
     */
    @Before
    public void setUp() throws Exception {
        // get private methods to test
        Objects.requireNonNull(stressMinMethod = StressMinimizationLayout.class.getDeclaredMethod(
                "calculateStress", List.class, NodeValueMatrix.class, NodeValueMatrix.class));
        Objects.requireNonNull(posDiffMethod = StressMinimizationLayout.class.getDeclaredMethod(
                "differencePositionsSmallerEpsilon", List.class, List.class, double.class));
        sm = new StressMinimizationLayout();
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
     * Test the private static method
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
        assertTrue((boolean) invokePrivateMethod(null, this.posDiffMethod, oldList, newList, 1.125));
        assertFalse((boolean) invokePrivateMethod(null, this.posDiffMethod, oldList, newList, 1.0));
    }

    /**
     * Test the private static method
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
        double stress = (double) invokePrivateMethod(null, stressMinMethod,
                GRAPH_1_POSITIONS, GRAPH_1_DISTANCES, weights);
        assertEquals(3 -2*Math.sqrt(2), stress, 0.001);

        // Test only one node edge case
        stress = (double) invokePrivateMethod(null, stressMinMethod, GRAPH_1_POSITIONS.subList(0, 1),
                new NodeValueMatrix(1), new NodeValueMatrix(1));
        assertEquals(0.0, stress, 0.001);
    }

    /**
     * Test the public static methods {@link StressMinimizationLayout#calculateStress(Graph, boolean)} and
     * {@link StressMinimizationLayout#calculateStress(Graph, boolean, double, double, double, double)}.
     * @author Jannik
     */
    @Test
    public void publicCalculateStress() {
        // test the method with the same value as above
        List<Double> actualStress = StressMinimizationLayout.calculateStress(GRAPH_1, true,
                0, 1, 1, -2); // this setup ignores node sizes
        assertEquals("Size of returned list 1 (cumulative)", 1, actualStress.size());
        assertEquals("Values 1 (cumulative)", 3 -2*Math.sqrt(2), actualStress.get(0), 0.001);

        // disabling the cumulation should result in the same
        actualStress = StressMinimizationLayout.calculateStress(GRAPH_1, false,
                0, 1, 1, -2);
        assertEquals("Size of returned list 1 (non-cumulative)", 1, actualStress.size());
        assertEquals("Values 1 (non-cumulative)", 3 -2*Math.sqrt(2), actualStress.get(0), 0.001);

        // test with two component graph
        actualStress = StressMinimizationLayout.calculateStress(GRAPH_2, true,
                0, 1, 1, -2);
        assertEquals("Size of returned list 2 (cumulative)", 1, actualStress.size());
         // each connected component has the same stress so the cumulated stress should be the same
        assertEquals("Values 2 (cumulative)", 3 -2*Math.sqrt(2), actualStress.get(0), 0.001);

        // test non cumulative
        actualStress = StressMinimizationLayout.calculateStress(GRAPH_2, false,
                0, 1, 1, -2);
        assertEquals("Size of returned list 2 (non-cumulative)", 2, actualStress.size());
        // each connected component has the same stress
        assertEquals("Values 2 (cumulative)", 3 -2*Math.sqrt(2), actualStress.get(0), 0.001);
        assertEquals("Values 2 (cumulative)", 3 -2*Math.sqrt(2), actualStress.get(1), 0.001);

        // test empty cumulative
        actualStress = StressMinimizationLayout.calculateStress(new AdjListGraph(), true);
        assertEquals("Size of returned list empty (cumulative)", 1, actualStress.size());
        assertEquals("Values empty (cumulative)", Double.NaN, actualStress.get(0), 0.0);
        // test empty non-cumulative
        actualStress = StressMinimizationLayout.calculateStress(new AdjListGraph(), false);
        assertEquals("Size of returned list empty (cumulative)", 0, actualStress.size());
    }

    /**
     * Test method {@link StressMinimizationLayout#getDescription()}.
     * @author Jannik
     */
    @Test
    public void getDescription() {
        assertTrue(sm.getDescription() != null && !sm.getDescription().trim().isEmpty());
    }

    /**
     * Test name given by algorithm.
     * @author Jannik
     */
    @Test
    public void getName() {
        assertEquals("Stress Minimization", sm.getName());
    }

    /**
     * Test checking method of algorithm.
     * @author Jannik
     */
    @Test
    public void check() {
        // check normal graph
        try {
            sm.attach(GRAPH_1, new Selection(GRAPH_1_NODES));
            sm.check();
        } catch (PreconditionException e) {
            e.printStackTrace();
            fail("No exception should have been thrown!");
        }

        // check null graph
        try {
            sm.attach(null, null);
            sm.check();
            fail("No exception thrown!");
        } catch (PreconditionException e) {}
        catch (Throwable t) {
            fail("Wrong Throwable thrown.");
        }

        // check empty graph
        try {
            sm.attach(new AdjListGraph(), new Selection());
            sm.check();
            fail("No exception thrown!");
        } catch (PreconditionException e) {}
        catch (Throwable t) {
            fail("Wrong Throwable thrown.");
        }
    }

    /**
     * Test the method {@link StressMinimizationLayout#execute()}
     * @author Jannik
     */
    @Test
    public void execute() throws InterruptedException {
        Graph original = GRAPH_2;
        Selection emptySelection = new Selection();
        // test in default mode
        Graph workCopy = ((Graph) original.copy());
        sm.attach(workCopy, emptySelection);
        sm.execute();
        Thread.sleep(1000); // wait for execution to finish
        somethingChanged("execute (default)", original, workCopy);

        // test with selection
        {
            // disable background tasks for here on
            sm.state.backgroundTask  = false;
            sm.state.multipleThreads  = false;

            workCopy = ((Graph) original.copy());
            List<Node> allOldNodes = original.getNodes();
            List<Node> allNewNodes = workCopy.getNodes();
            sm.attach(workCopy, new Selection(allNewNodes.subList(1, allNewNodes.size())));
            sm.execute();
            somethingChanged("execute (selection)", original, workCopy);
            // ... but not first node
            assertFalse("execute (selection): first node moved", AttributeHelper.getPositionVec2d(allOldNodes.get(0)).distance(AttributeHelper.getPositionVec2d(allNewNodes.get(0))) > 0);
        }

        // animation, undoable combinations, move into view
        for (int animate = 0; animate <= 1; animate++) {
            for (int undoable = 0; undoable <= 1; undoable++) {
                for (int move = 0; move <= 1; move++) {
                    sm.state.backgroundTask  = false;
                    sm.state.multipleThreads  = false;
                    sm.state.doAnimations = animate == 1;
                    sm.state.intermediateUndoable = undoable == 1;
                    sm.state.moveIntoView = move == 1;

                    if (sm.state.intermediateUndoable) {
                        sm.state.doAnimations = true;
                    }

                    workCopy = ((Graph) original.copy());
                    sm.attach(workCopy, emptySelection);
                    sm.execute();
                    somethingChanged("execute ("
                            + (animate == 1 ? "" : "not") + " animated, "
                            + (undoable == 1 ? "" : "not") + " undoable, "
                            + (move == 1 ? "" : "not") + " move into view)", original, workCopy);
                }
            }
        }

        // test different stop conditions
        // only stress minimization
        workCopy = ((Graph) original.copy());
        sm.state.backgroundTask  = false;
        sm.state.multipleThreads  = false;
        sm.state.positionChangeEpsilon = Double.NEGATIVE_INFINITY;
        sm.attach(workCopy, emptySelection);
        sm.execute();

        // only position change
        workCopy = ((Graph) original.copy());
        sm.state.backgroundTask  = false;
        sm.state.multipleThreads  = false;
        sm.state.stressEpsilon = Double.NEGATIVE_INFINITY;
        sm.attach(workCopy, emptySelection);
        sm.execute();


        // MLF compatibility
        workCopy = ((Graph) original.copy());
        workCopy.setBoolean("GRAPH_IS_MLF_COARSENING_LEVEL", true);
        sm.attach(workCopy, emptySelection);
        sm.execute();
        somethingChanged("execute (mlf compatibility)", original, workCopy);

        workCopy = ((Graph) original.copy());
        workCopy.setBoolean("GRAPH_IS_MLF_COARSENING_LEVEL", true);
        workCopy.setBoolean("GRAPH_IS_MLF_COARSENING_TOP_LEVEL", true);
        workCopy.setBoolean("GRAPH_IS_MLF_COARSENING_BOTTOM_LEVEL", true);
        sm.attach(workCopy, emptySelection);
        sm.execute();
        somethingChanged("execute (mlf compatibility top/bottom)", original, workCopy);

        // SM already active
        original = GRAPH_LONG_PATH;
        workCopy = ((Graph) original.copy());
        State state = sm.state;
        state.backgroundTask = false;
        sm.attach(workCopy, emptySelection);
        Thread t = new Thread(()->sm.execute());
        t.start();
        Thread.sleep(500);
        sm.execute();
        state.keepRunning.set(false);
        Thread.sleep(500);
        t.join();
        System.out.flush();

        Window w = null;
        for (Window window : Window.getWindows()) {
            if (window instanceof JDialog) {
                w = window;
                break;
            }
        }

        assertNotNull("Pop-up: already running", w); // a pop-up should have opened
        w.dispose();
        System.out.flush();

        // SM with non finite values (user notification)
        original = GRAPH_1;
        workCopy = ((Graph) original.copy());
        state = sm.state;
        state.backgroundTask = false;
        state.weightScalingFactor = Double.POSITIVE_INFINITY;
        state.debugEnabled = false; // to ensure user pop-up
        state.doAnimations = false; // every entry should be checked
        sm.attach(workCopy, emptySelection);
        sm.execute();
        System.out.flush();
        Thread.sleep(500);

        Window w2 = null;
        for (Window window : Window.getWindows()) {
            if (window instanceof JDialog && window != w) {
                w2 = window;
                break;
            }
        }

        assertNotNull("Pop-up: non-finite", w2); // a (different) pop-up should have opened
        w2.dispose();
        System.out.flush();

        // SM with non finite values (user notification)
        workCopy = ((Graph) original.copy());
        state = sm.state;
        state.backgroundTask = false;
        state.weightScalingFactor = Double.POSITIVE_INFINITY;
        state.debugEnabled = true; // only debug-write
        state.doAnimations = true; // also check animated entries
        sm.attach(workCopy, emptySelection);
        sm.execute();

        assertFalse("Non-finite values", state.debugAllFinite);
    }

    /**
     * Tests the exceptional behaviour of {@link StressMinimizationLayout}
     * @author Jannik
     */
    @Test
    public void exceptions() throws InterruptedException {
        try {
            assert false : "Assertions enabled";
            throw new AssertionError("Assertions must be enabled to run this test suite!");
        } catch (AssertionError e) {
            if (!"Assertions enabled".equals(e.getMessage())) {
                throw e;
            }
        }

        Graph original = GRAPH_LONG_PATH;
        Selection emptySelection = new Selection();
        Graph workCopy = ((Graph) original.copy());

        // violated precondition
        try {
            sm.state.intermediateUndoable = true;
            sm.state.doAnimations = false;

            sm.attach(workCopy, emptySelection);
            sm.execute();
            fail("No exception thrown");
        } catch (AssertionError e) {
            if ("No exception thrown".equals(e.getMessage()))
                throw e;
            sm.state.intermediateUndoable = false;
            sm.state.doAnimations = true;
        } catch (Throwable t) {
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }

        assertFalse("Sill working on", workCopy.getAttributes().getCollection().containsKey(WORKING_ATTRIBUTE));


        // test interruption
        workCopy = (Graph) original.copy();
        PrintStream oldErr = System.err;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stream));

        sm.state.backgroundTask = false;
        sm.attach(workCopy, emptySelection);

        Thread t = new Thread(() -> sm.execute());
        t.start();

        Thread.sleep(3000);
        t.interrupt();
        Thread.sleep(300); // wait for exception to be printed
        System.setErr(oldErr);
        assertFalse("Exception printed", stream.toString().trim().isEmpty());


        // test static register method exceptions
        InitialPlacer ip = new InitialPlacer() {
            @Override public List<Vector2d> calculateInitialPositions(List<Node> nodes, NodeValueMatrix distances) { return null; }
            @Override public String getName() { return "Dummy InitialPlacer"; }
            @Override public String getDescription() { return null; }
            @Override public Parameter[] getParameters() { return new Parameter[0]; }
            @Override public void setParameters(Parameter[] parameters) { }
        };
        IterativePositionAlgorithm ipa = new IterativePositionAlgorithm() {
            @Override public List<Vector2d> nextIteration(List<Node> nodes, List<Vector2d> positions, NodeValueMatrix distances, NodeValueMatrix weights) { return null; }
            @Override public String getName() { return "Dummy IterativePositionAlgorithm"; }
            @Override public String getDescription() { return null; }
            @Override public Parameter[] getParameters() { return new Parameter[0]; }
            @Override public void setParameters(Parameter[] parameters) { }
        };
        try {
            // name already registered
            try {StressMinimizationLayout.registerInitialPlacer(new PivotMDS()); fail("No exception thrown");} catch (IllegalArgumentException e) {}
            try {StressMinimizationLayout.registerIterativePositionAlgorithm(new IntuitiveIterativePositionAlgorithm()); fail("No exception thrown");} catch (IllegalArgumentException e) {}
            // no suitable constructor
            try {StressMinimizationLayout.registerInitialPlacer(ip); fail("No exception thrown");} catch (IllegalArgumentException e) {}
            try {StressMinimizationLayout.registerIterativePositionAlgorithm(ipa); fail("No exception thrown");} catch (IllegalArgumentException e) {}
        } catch (Throwable thr) {
            if ("No exception thrown".equals(thr.getMessage())) {
                throw thr;
            }
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }

        // test cannot copy
        initialPlacers.add(ip);
        positionAlgorithms.add(ipa);
        Parameter[] parameters = sm.getParameters();
        JComboBox initialPlacerBox = ((JComboBox) ((ParameterizableSelectorParameter) parameters[8].getValue()).getComponent(0));
        JComboBox iterAlgBox = ((JComboBox) ((ParameterizableSelectorParameter) parameters[10].getValue()).getComponent(0));

        initialPlacerBox.setSelectedItem(ip.getName());
        stream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stream));
        sm.setParameters(parameters);
        Thread.sleep(500);
        System.setErr(oldErr);
        initialPlacerBox.setSelectedIndex(0);
        assertFalse("Exception printed initial placer", stream.toString().trim().isEmpty());

        iterAlgBox.setSelectedItem(ipa.getName());
        stream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stream));
        sm.setParameters(parameters);
        Thread.sleep(500);
        System.setErr(oldErr);
        iterAlgBox.setSelectedIndex(0);
        assertFalse("Exception printed iteration position algorithm", stream.toString().trim().isEmpty());
        initialPlacers.remove(ip);
        positionAlgorithms.remove(ipa);

    }

    /**
     * Test {@link StressMinimizationLayout.WorkUnit#toString()}
     */
    @Test
    public void workUnitToString() {
        sm.state.initialPlacer = new NullPlacer();
        assertTrue("WorkUnit toString()",
                sm.new WorkUnit(
                        Collections.singletonList(GRAPH_1_NODES.get(0)), -1, sm.state)
                        .toString().length() > 0);
    }

    /**
     * Test methods implemented by {@link org.BackgroundTaskStatusProvider} and implemented
     * by {@link State}.
     * @author Jannik
     */
    @Test
    public void backgroundTaskStatusProvider() {
        State state = sm.state;

        assertEquals("Status message 1", "", state.getCurrentStatusMessage1());
        assertNull("Status message 2", state.getCurrentStatusMessage2());
        assertEquals("Status value", -1, state.getCurrentStatusValue());
        assertEquals("Status value (fine)", -1, state.getCurrentStatusValueFine(), 0.0);
        assertFalse("Waits for user", state.pluginWaitsForUser());
        // no-ops
        state.setCurrentStatusValue(42);
        assertEquals("Status value after set", -1, state.getCurrentStatusValue());
        state.pleaseContinueRun();;
        assertFalse("Waits for user after continue", state.pluginWaitsForUser());

        // stopping
        assertTrue("Standard value for ‘keep running’", state.keepRunning.get());
        sm.state.pleaseStop();
        assertFalse("‘keep running’ after stop", state.keepRunning.get());
    }

    /**
     * Test method {@link StressMinimizationLayout#getParameters()}.
     * @author Jannik
     */
    @Test
    public void getParameters() {
        Parameter[] parameters = sm.getParameters();
        assertNotNull(parameters);
        assertTrue(parameters.length > 0);
        assertSame("parameters cached", parameters, sm.getParameters());
    }

    /**
     * Test method {@link StressMinimizationLayout#setParameters(Parameter[])}.
     * @author Jannik
     */
    @Test
    public void setParameters() {
        // test without changed values
        Parameter[] parameters = sm.getParameters();
        sm.setParameters(parameters);
        StressMinimizationLayout.State state = sm.state;

        assertEquals(STRESS_EPSILON_DEFAULT, state.stressEpsilon, 0.0001);
        assertEquals(POSITION_CHANGE_EPSILON_DEFAULT, state.positionChangeEpsilon, 0.0001);
        assertEquals(MAX_ITERATIONS_DEFAULT, state.maxIterations, 0.0001);
        assertEquals(WEIGHT_SCALING_FACTOR_DEFAULT, state.weightScalingFactor, 0.0001);
        assertEquals(WEIGHT_POWER_DEFAULT, state.weightPower, 0.0001);
        equalPSP(parameters[8], state.initialPlacer);
        equalPSP(parameters[10], state.positionAlgorithm);
        assertEquals(EDGE_SCALING_FACTOR_DEFAULT, state.edgeScalingFactor, 0.0001);
        assertEquals(EDGE_LENGTH_MINIMUM_DEFAULT, state.edgeLengthMinimum, 0.0001);
        assertEquals(REMOVE_EDGE_BENDS_DEFAULT, state.removeEdgeBends);
        assertEquals(INTERMEDIATE_UNDOABLE_DEFAULT, state.intermediateUndoable);
        assertEquals(DO_ANIMATIONS_DEFAULT, state.doAnimations);
        assertEquals(MOVE_INTO_VIEW_DEFAULT, state.moveIntoView);
        assertEquals(BACKGROUND_TASK_DEFAULT, state.backgroundTask);
        assertEquals(MULTIPLE_THREADS_DEFAULT, state.multipleThreads);

        // after some boolean switches
        ((JCheckBox) ((EnableableNumberParameter) parameters[1].getValue()).getComponent(0)).setSelected(false);
        ((JCheckBox) ((EnableableNumberParameter) parameters[2].getValue()).getComponent(0)).setSelected(false);
        ((JCheckBox) ((EnableableNumberParameter) parameters[3].getValue()).getComponent(0)).setSelected(false);
        ((BooleanParameter) parameters[15]).setValue(true);
        sm.setParameters(parameters);
        state = sm.state;

        assertEquals(Double.NEGATIVE_INFINITY, state.stressEpsilon, 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, state.positionChangeEpsilon, 0.0);
        assertEquals(Long.MAX_VALUE, state.maxIterations);
        assertEquals(WEIGHT_SCALING_FACTOR_DEFAULT, state.weightScalingFactor, 0.0);
        assertEquals(WEIGHT_POWER_DEFAULT, state.weightPower, 0.0);
        equalPSP(parameters[8], state.initialPlacer);
        assertNotSame(state.initialPlacer, initialPlacers.get(0)); // check if an copy was created
        equalPSP(parameters[10], state.positionAlgorithm);
        assertNotSame(state.positionAlgorithm, positionAlgorithms.get(0)); // check if an copy was created
        assertEquals(EDGE_SCALING_FACTOR_DEFAULT, state.edgeScalingFactor, 0.0);
        assertEquals(EDGE_LENGTH_MINIMUM_DEFAULT, state.edgeLengthMinimum, 0.0);
        assertEquals(REMOVE_EDGE_BENDS_DEFAULT, state.removeEdgeBends);
        assertTrue(state.intermediateUndoable);
        assertTrue(state.doAnimations);
        assertEquals(MOVE_INTO_VIEW_DEFAULT, state.moveIntoView);
        assertEquals(BACKGROUND_TASK_DEFAULT, state.backgroundTask);
        assertEquals(MULTIPLE_THREADS_DEFAULT, state.multipleThreads);

        // test setting to minimal value
        ((JCheckBox) ((EnableableNumberParameter) parameters[1].getValue()).getComponent(0)).setSelected(true);
        ((JCheckBox) ((EnableableNumberParameter) parameters[2].getValue()).getComponent(0)).setSelected(true);
        ((JCheckBox) ((EnableableNumberParameter) parameters[3].getValue()).getComponent(0)).setSelected(true);

        ((JSpinner) ((EnableableNumberParameter) parameters[1].getValue()).getComponent(1)).setValue(0.0);
        ((JSpinner) ((EnableableNumberParameter) parameters[2].getValue()).getComponent(1)).setValue(0.0);
        ((JSpinner) ((EnableableNumberParameter) parameters[12].getValue()).getComponent(0)).setValue(0.0);
        ((JSpinner) ((EnableableNumberParameter) parameters[13].getValue()).getComponent(0)).setValue(0.0);
        sm.setParameters(parameters);
        state = sm.state;

        assertEquals(SMALLEST_NON_ZERO_VALUE, state.stressEpsilon, 0.0);
        assertEquals(SMALLEST_NON_ZERO_VALUE, state.positionChangeEpsilon, 0.0);
        assertEquals(MAX_ITERATIONS_DEFAULT, state.maxIterations);
        assertEquals(WEIGHT_SCALING_FACTOR_DEFAULT, state.weightScalingFactor, 0.0);
        assertEquals(WEIGHT_POWER_DEFAULT, state.weightPower, 0.0);
        equalPSP(parameters[8], state.initialPlacer);
        equalPSP(parameters[10], state.positionAlgorithm);
        assertEquals(0.0, state.edgeScalingFactor, 0.0);
        assertEquals(SMALLEST_NON_ZERO_VALUE, state.edgeLengthMinimum, 0.0);
        assertEquals(REMOVE_EDGE_BENDS_DEFAULT, state.removeEdgeBends);
        assertTrue(state.intermediateUndoable);
        assertTrue(state.doAnimations);
        assertEquals(MOVE_INTO_VIEW_DEFAULT, state.moveIntoView);
        assertEquals(BACKGROUND_TASK_DEFAULT, state.backgroundTask);
        assertEquals(MULTIPLE_THREADS_DEFAULT, state.multipleThreads);
    }

    /**
     * Test the equality of {@link ParameterizableSelectorParameter}s.
     * @param actual the expected value.
     * @param expected the actual value.
     * @author Jannik
     */
    private void equalPSP(final Parameter expected, final Parameterizable actual) {
        assertEquals(actual.getClass(),
                ((ParameterizableSelectorParameter) expected.getValue()).getSelectedParameterizable().getClass());
    }

    /**
     * Test the static registration algorithms
     * {@link StressMinimizationLayout#registerInitialPlacer(InitialPlacer)},
     * {@link StressMinimizationLayout#registerIterativePositionAlgorithm(IterativePositionAlgorithm)},
     * {@link StressMinimizationLayout#getInitialPlacerNames()} and
     * {@link StressMinimizationLayout#getIterativePositionAlgorithmNames()}.
     */
    @Test
    public void registers() {

        DummyInitialPlacer ip = new DummyInitialPlacer();
        DummyIterativePositionAlgorithm ipa = new DummyIterativePositionAlgorithm();

        System.out.println(Arrays.toString(DummyIterativePositionAlgorithm.class.getDeclaredConstructors()));

        Parameter[] parameters = sm.getParameters();

        assertSame("parameters cached", parameters, sm.getParameters());

        registerInitialPlacer(ip);
        registerIterativePositionAlgorithm(ipa);

        // initial placers
        assertTrue("ip in list", initialPlacers.contains(ip));
        assertTrue("ip name in list", StressMinimizationLayout.getInitialPlacerNames().contains(ip.getName()));

        // iterative position algorithm
        assertTrue("ipa in list", positionAlgorithms.contains(ipa));
        assertTrue("ipa name in list", StressMinimizationLayout.getIterativePositionAlgorithmNames().contains(ipa.getName()));

        assertNotSame("parameters updated", parameters, sm.getParameters());
        initialPlacers.remove(ip);
        positionAlgorithms.remove(ipa);
    }
    /** Dummy {@link InitialPlacer} for test {@link #registers()}. @author Jannik */
    static class DummyInitialPlacer implements InitialPlacer {
        public DummyInitialPlacer() {}
        @Override public List<Vector2d> calculateInitialPositions(List<Node> nodes, NodeValueMatrix distances) { return null; }
        @Override public String getName() { return "Dummy InitialPlacer"; }
        @Override public String getDescription() { return null; }
        @Override public Parameter[] getParameters() { return new Parameter[0]; }
        @Override public void setParameters(Parameter[] parameters) { }
    }
    /** Dummy {@link IterativePositionAlgorithm} for test {@link #registers()}. @author Jannik */
    static class DummyIterativePositionAlgorithm implements IterativePositionAlgorithm {
        private DummyIterativePositionAlgorithm() {}
        @Override public List<Vector2d> nextIteration(List<Node> nodes, List<Vector2d> positions, NodeValueMatrix distances, NodeValueMatrix weights) { return null; }
        @Override public String getName() { return "Dummy IterativePositionAlgorithm"; }
        @Override public String getDescription() { return null; }
        @Override public Parameter[] getParameters() { return new Parameter[0]; }
        @Override public void setParameters(Parameter[] parameters) { }
    }

    /**
     * Test category given by algorithm.
     * @author Jannik
     */
    @Test
    public void getCategory() {
        assertEquals("Layout", sm.getCategory());
    }

    /**
     * Test method {@link StressMinimizationLayout#isLayoutAlgorithm()}.
     * @author Jannik
     */
    @Test
    public void isLayoutAlgorithm() {
        assertTrue(sm.isLayoutAlgorithm());
    }

    /**
     * Test method {@link StressMinimizationLayout#activeForView(View)}.
     * @author Jannik
     */
    @Test
    public void activeForView() {
        assertFalse(sm.activeForView(null));
        assertTrue(sm.activeForView(new NullView()));
    }

    /**
     * Compares whether the positions of the whether the positions of the given graphs
     * are different.<br>
     * This will throw an assertion error by JUnit.
     *
     * @param testName
     *      the name/identifier of the test.
     * @param oldGraph
     *      the first graph to use for the comparison.
     * @param newGraph
     *      the second graph to use for the comparison.
     */
    private void somethingChanged(final String testName, final Graph oldGraph, final Graph newGraph) {
        ArrayList<Node> g1Nodes = new ArrayList<>(oldGraph.getNodes());
        ArrayList<Node> g2Nodes = new ArrayList<>(newGraph.getNodes());

        for (int idx = 0; idx < g1Nodes.size(); idx++) {
            Vector2d oldPos = AttributeHelper.getPositionVec2d(g1Nodes.get(idx));
            Vector2d newPos = AttributeHelper.getPositionVec2d(g2Nodes.get(idx));

            if (oldPos.distance(newPos) > 0) {
                return;
            }
        }
        fail(testName + ": Graph positions did not change!");
    }
}