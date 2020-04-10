package org.vanted.addons.multilevelframework;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.graph_generation.ErdosRenyiGraphGenerator;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.graph_generation.WattsStrogatzGraphGenerator;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.circle.CircleLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.PatternSpringembedder;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.random.RandomLayouterAlgorithm;
import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.*;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vanted.addons.multilevelframework.pse_hack.BlockingForceDirected;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Warning: These tests need a VANTED instance and are slow as hell (or rather: "slow as VANTED").
 */
public class TestsWithVANTED {
    static Thread vanted = null;
    /**
     * @author Gordian
     */
    @BeforeClass
    public static void startVANTED() throws InterruptedException {
        vanted = new Thread(() -> StartVantedWithAddon.main(new String[0]));
        vanted.start();
        Thread.sleep(12000); // increase if your computer takes longer to start VANTED
    }

    /**
     * @author Gordian
     */
    @AfterClass
    public static void stopVANTED() {
        vanted.stop();
        vanted = null;
    }



//----------------------------------------MultilevelFrameworkAddon------------------------------------------------------
    /**
     * @see MultilevelFrameworkAddon
     */


    private String old_name;

    /**
     * @author Gordian
     */
    @Test
    public void initializeAddon() {
        MultilevelFrameworkAddon a = new MultilevelFrameworkAddon();
        a.initializeAddon();
        assertTrue(a.getAlgorithms()[0] instanceof MultilevelFrameworkLayouter);
    }

    /**
     * @author Gordian
     */
    @Test
    public void getIcon() {
        MultilevelFrameworkAddon a = new MultilevelFrameworkAddon();
        ImageIcon old = a.getIcon();
        assertNotNull(old);
        old_name = MultilevelFrameworkAddon.ICON_NAME;
        MultilevelFrameworkAddon.ICON_NAME = "invalid";
        assertNotSame(a.getIcon(), old);
    }

    /**
     * @author Gordian
     */
    @After
    public void restore() {
        MultilevelFrameworkAddon.ICON_NAME = old_name;
    }



//------------------------------------------LayoutAlgorithmWrapper------------------------------------------------------
    /**
     * @see LayoutAlgorithmWrapper
     */


    /**
     * @author Gordian
     */
    @Test
    public void getGUI() throws InvocationTargetException, InterruptedException {
        assertNotNull(LayoutAlgorithmWrapper.getLayoutAlgorithms().get("Circle").getGUI());
        SwingUtilities.invokeAndWait(() -> assertNotNull(LayoutAlgorithmWrapper.getLayoutAlgorithms()
                .get(BlockingForceDirected.springName).getGUI()));
        assertNull(new LayoutAlgorithmWrapper("Dummy", new Algorithm() { // "empty" algorithm
            @Override public String getName() { return ""; }
            @Override public void setParameters(Parameter[] params) { }
            @Override public Parameter[] getParameters() { return null; }
            @Override public void attach(Graph g, Selection selection) { }
            @Override public void check() throws PreconditionException { } @Override public void execute() { }
            @Override public void reset() { }
            @Override public String getCategory() { return "Layout"; }
            @Override public Set<Category> getSetCategory() { return null; }
            @Override public String getMenuCategory() { return null; }
            @Override public boolean isLayoutAlgorithm() { return true; }
            @Override public boolean showMenuIcon() { return false; }
            @Override public KeyStroke getAcceleratorKeyStroke() { return null; }
            @Override public String getDescription() { return ""; }
            @Override public void setActionEvent(ActionEvent a) { }
            @Override public ActionEvent getActionEvent() { return null; }
            @Override public boolean mayWorkOnMultipleGraphs() { return false; }
        }, false).getGUI());
        assertNull(new LayoutAlgorithmWrapper("Dummy", new ThreadSafeAlgorithm() { // "empty" thread safe algorithm
            @Override public boolean setControlInterface(ThreadSafeOptions options, JComponent jc) { return false; }
            @Override public void executeThreadSafe(ThreadSafeOptions options) { }
            @Override public void resetDataCache(ThreadSafeOptions options) { }
            @Override public String getName() { return ""; }
            @Override public void setParameters(Parameter[] params) { }
            @Override public Parameter[] getParameters() { return null; }
            @Override public void attach(Graph g, Selection selection) { }
            @Override public void check() throws PreconditionException { }
            @Override public void execute() { }
            @Override public void reset() { }
            @Override public String getCategory() { return "Layout"; }
            @Override public Set<Category> getSetCategory() { return null; }
            @Override public String getMenuCategory() { return null; }
            @Override public boolean isLayoutAlgorithm() { return true; }
            @Override public String getDescription() { return null; }
            @Override public void setActionEvent(ActionEvent a) { }
            @Override public ActionEvent getActionEvent() { return null; }
        }, true).getGUI());
    }

    /**
     * @author Gordian
     */
    @Test
    public void execute() throws InvocationTargetException, InterruptedException {
        AdjListGraph g = new AdjListGraph();
        Node n1 = g.addNode();
        Node n2 = g.addNode();
        Node n3 = g.addNode();
        g.addEdge(n1, n2, false);
        g.addEdge(n2, n3, false);
        g.addEdge(n1, n3, false);
        for (Node n : g.getNodes()) { AttributeHelper.setPosition(n, 0, 0); }
        LayoutAlgorithmWrapper law = LayoutAlgorithmWrapper.getLayoutAlgorithms().get("Circle");
        law.execute(g, new Selection());
        assertTrue(g.getNodes().stream()
                .anyMatch(n -> AttributeHelper.getPositionX(n) != 0 || AttributeHelper.getPositionY(n) != 0));
        LayoutAlgorithmWrapper law2 = LayoutAlgorithmWrapper.getLayoutAlgorithms().get(BlockingForceDirected.springName);
        AttributeHelper.setPosition(n1, 100, 100);
        AttributeHelper.setPosition(n2, 200, 200);
        AttributeHelper.setPosition(n3, 200, 180);
        SwingUtilities.invokeAndWait(() -> {
            GraphHelper.diplayGraph(g);
        });
        law2.execute(g, new Selection());
        assertTrue(AttributeHelper.getPositionX(n1) != 100 || AttributeHelper.getPositionY(n1) != 100
                || AttributeHelper.getPositionX(n2) != 200 || AttributeHelper.getPositionY(n2) != 200
                || AttributeHelper.getPositionX(n3) != 200 || AttributeHelper.getPositionY(n3) != 180);
    }

    /**
     * @author Gordian
     */
    @Test
    public void executeInEventDispatchThread() throws InvocationTargetException, InterruptedException {
        AdjListGraph g = new AdjListGraph();
        Node n1 = g.addNode();
        Node n2 = g.addNode();
        Node n3 = g.addNode();
        g.addEdge(n1, n2, false);
        g.addEdge(n2, n3, false);
        g.addEdge(n1, n3, false);
        for (Node n : g.getNodes()) { AttributeHelper.setPosition(n, 0, 0); }
        LayoutAlgorithmWrapper law = LayoutAlgorithmWrapper.getLayoutAlgorithms().get(BlockingForceDirected.springName);
        AttributeHelper.setPosition(n1, 100, 100);
        AttributeHelper.setPosition(n2, 200, 200);
        AttributeHelper.setPosition(n3, 200, 180);
        MultilevelFrameworkLayouter.display(g);
        SwingUtilities.invokeAndWait(() -> {
            law.execute(g, new Selection());
        });
        assertTrue(AttributeHelper.getPositionX(n1) != 100 || AttributeHelper.getPositionY(n1) != 100
                || AttributeHelper.getPositionX(n2) != 200 || AttributeHelper.getPositionY(n2) != 200
                || AttributeHelper.getPositionX(n3) != 200 || AttributeHelper.getPositionY(n3) != 180);
    }

    /**
     * @author Gordian
     */
    @Test
    public void executeAndInterruptSleep() throws InterruptedException {
        AdjListGraph g = new AdjListGraph();
        Node n1 = g.addNode();
        Node n2 = g.addNode();
        Node n3 = g.addNode();
        g.addEdge(n1, n2, false);
        g.addEdge(n2, n3, false);
        g.addEdge(n1, n3, false);
        LayoutAlgorithmWrapper law = new LayoutAlgorithmWrapper("asdf", new PatternSpringembedder() {
            @Override public boolean setControlInterface(ThreadSafeOptions options, JComponent jc) { return true; }
            @Override
            public void executeThreadSafe(ThreadSafeOptions options) {
                RandomLayouterAlgorithm rla = new RandomLayouterAlgorithm();
                rla.attach(options.getGraphInstance(), options.getSelection());
                rla.execute();
            }
            @Override public void resetDataCache(ThreadSafeOptions options) { }
            @Override public String getName() { return "asdf"; }
            @Override public void setParameters(Parameter[] params) { }
            @Override public Parameter[] getParameters() { return new Parameter[0]; }
            @Override public void attach(Graph g, Selection selection) { }
            @Override public void check() throws PreconditionException { }
            @Override public void execute() { }
            @Override public void reset() { }
            @Override public String getCategory() { return "Layout"; }
            @Override public Set<Category> getSetCategory() { return null; }
            @Override public String getMenuCategory() { return null; }
            @Override public boolean isLayoutAlgorithm() { return true; }
            @Override public String getDescription() { return ""; }
            @Override public void setActionEvent(ActionEvent a) { }
            @Override public ActionEvent getActionEvent() { return null; }
        }, true);
        AttributeHelper.setPosition(n1, 100, 100);
        AttributeHelper.setPosition(n2, 200, 200);
        AttributeHelper.setPosition(n3, 200, 180);
        MultilevelFrameworkLayouter.display(g);
        Thread t = new Thread(() -> law.execute(g, new Selection()));
        t.start();
        // wait till t is sleeping
        while (!isSleeping(t)) {
            if (!t.isAlive()) fail("Thread died.");
            Thread.sleep(1);
        }
        t.interrupt();
        assertTrue(AttributeHelper.getPositionX(n1) != 100 || AttributeHelper.getPositionY(n1) != 100
                || AttributeHelper.getPositionX(n2) != 200 || AttributeHelper.getPositionY(n2) != 200
                || AttributeHelper.getPositionX(n3) != 200 || AttributeHelper.getPositionY(n3) != 180);
    }

    /**
     * @author Gordian
     */
    @Test
    public void failThreadSafeExecution() {
        boolean[] executeCalled = new boolean[1];
        executeCalled[0] = false;
        LayoutAlgorithmWrapper law = new LayoutAlgorithmWrapper("dummy",
                new ThreadSafeAlgorithm() {
                    @Override public boolean setControlInterface(ThreadSafeOptions options, JComponent jc) { return false; }
                    @Override public void executeThreadSafe(ThreadSafeOptions options) {
                        // make sure executeThreadSafe fails
                        throw new IllegalArgumentException();
                    }
                    @Override public void resetDataCache(ThreadSafeOptions options) { }
                    @Override public String getName() { return null; }
                    @Override public void setParameters(Parameter[] params) { }
                    @Override public Parameter[] getParameters() { return new Parameter[0]; }
                    @Override public void attach(Graph g, Selection selection) { }
                    @Override public void check() throws PreconditionException { }
                    @Override public void execute() { executeCalled[0] = true; }
                    @Override public void reset() { }
                    @Override public String getCategory() { return null; }
                    @Override public Set<Category> getSetCategory() { return null; }
                    @Override public String getMenuCategory() { return null; }
                    @Override public boolean isLayoutAlgorithm() { return true; }
                    @Override public String getDescription() { return null; }
                    @Override public void setActionEvent(ActionEvent a) { }
                    @Override public ActionEvent getActionEvent() { return null; }
                }, true);
        law.execute(new AdjListGraph(), new Selection());
        // assert that the execute() method falls back to the "non-threadsafe" execution method
        assertTrue(executeCalled[0]);
    }

    /**
     * Checks if the {@link Thread} is sleeping by analyzing its stack trace.
     * @param t
     *      The {@link Thread} to analyze. Must not be {@code null}.
     * @return
     *      {@code true} if the thread is sleeping; {@code false} otherwise
     * @author Gordian
     */
    private static boolean isSleeping(Thread t) {
        StackTraceElement[] st = t.getStackTrace();
        return Arrays.stream(st).anyMatch(s -> s.getClassName().equals(Thread.class.getName())
                    && s.getMethodName().equals("sleep"));
    }

    /**
     * @author Gordian
     */
    @Test
    public void getGUIName() {
        LayoutAlgorithmWrapper.getLayoutAlgorithms().forEach((s, law) -> {
            assertEquals(s, law.getGUIName());
        });
    }

    /**
     * @author Gordian
     */
    @Test
    public void getAlgorithm() {
        Map<String, LayoutAlgorithmWrapper> algs = LayoutAlgorithmWrapper.getLayoutAlgorithms();
        assertTrue(algs.get("Circle").getAlgorithm() instanceof CircleLayouterAlgorithm);
    }

    /**
     * @author Gordian
     */
    @Test
    public void getLayoutAlgorithms() {
        assertTrue("More than half of the whitelist not found",
                LayoutAlgorithmWrapper.getLayoutAlgorithms().size()
                        > LayoutAlgorithmWrapper.WHITELIST.size() / 2);
    }

    /**
     * @author Gordian
     */
    @Test
    public void testToString() {
        Map<String, LayoutAlgorithmWrapper> algs = LayoutAlgorithmWrapper.getLayoutAlgorithms();
        String s = algs.get("Circle").toString();
        assertTrue(s.contains("LayoutAlgorithmWrapper"));
        assertTrue(s.contains("Circle"));
    }


    /**
     * @author Gordian
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentConstructor() {
        new LayoutAlgorithmWrapper("MyDummyAlg", new MultilevelFrameworkLayouter(), true);
    }

    /**
     * @author Gordian
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentConstructor2() {
        new LayoutAlgorithmWrapper(" ", new MultilevelFrameworkLayouter(), false);
    }

    /**
     * @author Gordian
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentConstructor3() {
        new LayoutAlgorithmWrapper("Dummy", new Algorithm() {
            @Override public String getName() { return null; }
            @Override public void setParameters(Parameter[] params) { }
            @Override public Parameter[] getParameters() { return new Parameter[0]; }
            @Override public void attach(Graph g, Selection selection) { }
            @Override public void check() throws PreconditionException { }
            @Override public void execute() { }
            @Override public void reset() { }
            @Override public String getCategory() { return null; }
            @Override public Set<Category> getSetCategory() { return null; }
            @Override public String getMenuCategory() { return null; }
            @Override public boolean isLayoutAlgorithm() { return false; }
            @Override public boolean showMenuIcon() { return false; }
            @Override public KeyStroke getAcceleratorKeyStroke() { return null; }
            @Override public String getDescription() { return null; }
            @Override public void setActionEvent(ActionEvent a) { }
            @Override public ActionEvent getActionEvent() { return null; }
            @Override public boolean mayWorkOnMultipleGraphs() { return false; }
        }, true);
    }



//----------------------------------------------------RandomMerger------------------------------------------------------

    /**
     * @author Gordian
     */
    @Test
    public void testRandomMergerBigGraph() {
//        Graph g = ErdosRenyiGraphGenerator.createGraph(1000, false, 1.0, true, false);
        Graph g;
        g = WattsStrogatzGraphGenerator.createGraph(1000, false, 10, 1);
        // shouldn't happen very often if the probability is set to 1
        while (MlfHelper.calculateConnectedComponentsOfSelection(new HashSet<>(g.getNodes())).size() > 1) {
            System.err.println("Random graph contained more than one connected component");
            g = WattsStrogatzGraphGenerator.createGraph(1000, false, 10, 1);
        }
        RandomMerger rM = new RandomMerger();
        MultilevelGraph mlg = new MultilevelGraph(g);
        rM.buildCoarseningLevels(mlg);
        assertTrue(mlg.getNumberOfLevels() > 3);
        while (mlg.getNumberOfLevels() > 1) {
            assertTrue(mlg.isComplete());
            CoarsenedGraph cg = mlg.popCoarseningLevel();
            for (MergedNode mn : cg.getMergedNodes()) {
                assertFalse(mn.getInnerNodes().isEmpty());
            }
        }
    }



//----------------------------------------------------MlfHelper------------------------------------------------------

    /**
     * @author Gordian
     */
    @Test(expected = IllegalArgumentException.class)
    public void validateNumberFail() {
        MlfHelper.validateNumber(100, 200, 300, "asdf");
    }




//----------------------------------------------------MultilevelFrameworkLayouter---------------------------------------

    /**
     * @author Gordian
     */
    @Test
    public void getDescription() {
        assertTrue(new MultilevelFrameworkLayouter().getDescription().toLowerCase().contains("multilevel"));
        assertTrue(new MultilevelFrameworkLayouter().getDescription().toLowerCase().contains("framework"));
    }

    /**
     * @author Gordian
     */
    @Test
    public void reset() {
        MultilevelFrameworkLayouter mfl = new MultilevelFrameworkLayouter();
        Parameter[] before = mfl.getParameters();
        mfl.setParameters(before);
        mfl.reset();
        assertNotSame(before, mfl.getParameters());
    }

    /**
     * @author Gordian
     */
    @Test
    public void getName() {
        MultilevelFrameworkLayouter mfl = new MultilevelFrameworkLayouter();
        mfl.setParameters(mfl.getParameters());
        assertTrue(mfl.getName().toLowerCase().contains("multilevel"));
        assertTrue(mfl.getName().toLowerCase().contains("framework"));
    }

    /**
     * @author Gordian
     */
    @Test(expected = PreconditionException.class)
    public void checkFail() throws PreconditionException {
        MultilevelFrameworkLayouter mlfl = new MultilevelFrameworkLayouter();
        mlfl.attach(new AdjListGraph(), new Selection());
        mlfl.check();
    }

    /**
     * @author Gordian
     */
    @Test(expected = PreconditionException.class)
    public void checkFail2() throws PreconditionException {
        MultilevelFrameworkLayouter mlfl = new MultilevelFrameworkLayouter();
        mlfl.attach(null, null);
        mlfl.check();
    }

    /**
     * @author Gordian
     */
    @Test
    public void executeMultilevelFrameworkLayouter() throws NoSuchFieldException, IllegalAccessException {
        MultilevelFrameworkLayouter mfl = new MultilevelFrameworkLayouter();
        Graph g = ErdosRenyiGraphGenerator.createGraph(100, false, 0.1, true, true);
        Field lastSelectedAlgorithm = MultilevelFrameworkLayouter.class.getDeclaredField("lastSelectedAlgorithm");
        lastSelectedAlgorithm.setAccessible(true);
        lastSelectedAlgorithm.set(mfl, "Null-Layout");
        Parameter[] parameters = mfl.getParameters();
        for (Parameter p : parameters) {
            if (p.getName().matches("(?i)remove.*overlaps") || p.getName().matches("(?i)remove.*bends")) {
                p.setValue(true);
            }
        }
        mfl.setParameters(parameters);
        mfl.attach(g, new Selection());
        mfl.execute();
        assertTrue(g.getBoolean(MultilevelFrameworkLayouter.WORKING_ATTRIBUTE_PATH));
    }

    /**
     * @author Gordian
     */
    @Test
    public void clickSetUpLayoutAlgorithmButton() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        MultilevelFrameworkLayouter mfl = new MultilevelFrameworkLayouter();
        Field lastSelectedAlgorithm = MultilevelFrameworkLayouter.class.getDeclaredField("lastSelectedAlgorithm");
        lastSelectedAlgorithm.setAccessible(true);
        lastSelectedAlgorithm.set(mfl, "Random");
        Window[] windowsBefore = Window.getWindows();
        new Thread(() -> mfl.clickSetUpLayoutAlgorithmButton(null)).start();
        Thread.sleep(100);
        Window[] windowsAfter = Window.getWindows();
        assertTrue(windowsAfter.length > windowsBefore.length);
        assertTrue(Arrays.stream(windowsAfter).anyMatch(w -> w instanceof JDialog
                && ((JDialog)w).getTitle().toLowerCase().contains("set up")
                && ((JDialog)w).getTitle().toLowerCase().contains("random")));
    }

    /**
     * @author Gordian
     */
    @Test
    public void setParameters() {
        MultilevelFrameworkLayouter mfl = new MultilevelFrameworkLayouter();
        Parameter[] parameters = mfl.getParameters();
        boolean newValue = false;
        for (Parameter parameter : parameters) {
            String name = parameter.getName().toLowerCase();
            if (name.contains("random") && name.contains("top")) {
                newValue = ! ((BooleanParameter)parameter).getBoolean();
                parameter.setValue(newValue);
            }
        }
        mfl.setParameters(parameters);
        for (Parameter parameter : mfl.getParameters()) {
            String name = parameter.getName().toLowerCase();
            if (name.contains("random") && name.contains("top")) {
                assertSame(newValue, ((BooleanParameter)parameter).getBoolean());
            }
        }
    }

    /**
     * @author Gordian
     */
    @Test
    public void getCategory() {
        assertEquals("Layout", new MultilevelFrameworkLayouter().getCategory());
    }

    /**
     * @author Gordian
     */
    @Test
    public void isLayoutAlgorithm() {
        assertTrue(new MultilevelFrameworkLayouter().isLayoutAlgorithm());
    }

    /**
     * @author Gordian
     */
    @Test
    public void activeForView() {
        assertFalse(new MultilevelFrameworkLayouter().activeForView(null));
    }

    /**
     * @author Gordian
     */
    @Test
    public void addMerger() {
        Merger m = new Merger() {
            @Override public Parameter[] getParameters() { return new Parameter[0]; }
            @Override public void setParameters(Parameter[] parameters) { }
            @Override public void buildCoarseningLevels(MultilevelGraph multilevelGraph) { }
            @Override public String getName() { return "DummyMerger"; }
            @Override public String getDescription() { return null; }
        };
        assertFalse(MultilevelFrameworkLayouter.getMergers().contains(m));
        MultilevelFrameworkLayouter.addMerger(m);
        assertTrue(MultilevelFrameworkLayouter.getMergers().contains(m));
        Set<Merger> mergersBefore = new HashSet<>(MultilevelFrameworkLayouter.getMergers());
        MultilevelFrameworkLayouter.addMerger(m); // add it again
        Set<Merger> mergersAfter = new HashSet<>(MultilevelFrameworkLayouter.getMergers());
        assertEquals(mergersAfter, mergersBefore);
    }

    /**
     * @author Gordian
     */
    @Test
    public void addPlacer() {
        Placer p = new Placer() {
            @Override public Parameter[] getParameters() { return new Parameter[0]; }
            @Override public void setParameters(Parameter[] parameters) { }
            @Override public void reduceCoarseningLevel(MultilevelGraph multilevelGraph) { }
            @Override public String getName() { return "DummyPlacer"; }
            @Override public String getDescription() { return null; }
        };
        assertFalse(MultilevelFrameworkLayouter.getPlacers().contains(p));
        MultilevelFrameworkLayouter.addPlacer(p);
        assertTrue(MultilevelFrameworkLayouter.getPlacers().contains(p));
        Set<Placer> placersBefore = new HashSet<>(MultilevelFrameworkLayouter.getPlacers());
        MultilevelFrameworkLayouter.addPlacer(p); // add it again
        Set<Placer> placersAfter = new HashSet<>(MultilevelFrameworkLayouter.getPlacers());
        assertEquals(placersAfter, placersBefore);
    }

    /**
     * @author Gordian
     */
    @Test
    public void calculateProgress() {
        AdjListGraph g = new AdjListGraph();
        Node n1 = g.addNode();
        Node n2 = g.addNode();
        Node n3 = g.addNode();
        Node isolated = g.addNode();
        for (Node n : g.getNodes()) { AttributeHelper.setPosition(n, 0, 0); }
        g.addEdge(n1, n2, false);
        g.addEdge(n2, n3, false);
        g.addEdge(n3, n1, false);
        MultilevelGraph mlg = new MultilevelGraph(g);
        List<?extends CoarsenedGraph> con = MlfHelper.calculateConnectedComponentsOfSelection(new HashSet<>(g.getNodes()));
        assertEquals(0.0, MultilevelFrameworkLayouter.calculateProgress(mlg, con, 0, 1), 0.001);
        assertEquals(50.0, MultilevelFrameworkLayouter.calculateProgress(mlg, con, 1, 2), 0.001);
    }

    /**
     * @author Gordian
     */
    @Test
    public void makeStatusMessage() {
        int totalLevels = 123;
        int level = 42;
        AdjListGraph g = new AdjListGraph();
        g.addNode();
        g.addNode();
        g.addNode();
        for (Node n : g.getNodes()) { AttributeHelper.setPosition(n, 0, 0); }
        List<?extends CoarsenedGraph> con = MlfHelper.calculateConnectedComponentsOfSelection(new HashSet<>(g.getNodes()));
        int current = 1;
        String graph = "i use arch btw";
        String msg = MultilevelFrameworkLayouter.makeStatusMessage(totalLevels, level, con, current, graph);
        assertTrue(msg.contains(Integer.toString(totalLevels)));
        assertTrue(msg.contains(Integer.toString(level)));
        assertTrue(msg.contains(Integer.toString(con.size())));
        assertTrue(msg.contains(Integer.toString(current)));
        assertTrue(msg.contains(graph));
    }

    /**
     * @author Gordian
     */
    @Test
    public void removeOverlaps() {
        AdjListGraph g = new AdjListGraph();
        Node n1 = g.addNode();
        Node n2 = g.addNode();
        AttributeHelper.setPosition(n1, 0, 0); AttributeHelper.setPosition(n2, 50, 50);
        AttributeHelper.setSize(n1, 100, 200);
        AttributeHelper.setSize(n2, 100, 200);
        MultilevelFrameworkLayouter.removeOverlaps(g);
        Vector2d size1 = AttributeHelper.getSize(n1);
        Vector2d size2 = AttributeHelper.getSize(n2);
        if (size1 == null || size2 == null) {
            throw new IllegalStateException("The sizes should not be null");
        }
        double minDistX = size1.x/2.0 + size2.x/2.0;
        double minDistY = size1.y/2.0 + size2.y/2.0;
        // if there are no overlaps in either x or y direction (or both) , then the bounding boxes don't overlap
        assertTrue(Math.abs(AttributeHelper.getPositionX(n1) - AttributeHelper.getPositionX(n2)) > minDistX
                || Math.abs(AttributeHelper.getPositionY(n1) - AttributeHelper.getPositionY(n2)) > minDistY);
    }

    /**
     * @author Gordian
     */
    @Test
    public void randomLayout() {
        AdjListGraph g = new AdjListGraph();
        Node n1 = g.addNode();
        Node n2 = g.addNode();
        Node n3 = g.addNode();
        g.getNodes().forEach(n -> AttributeHelper.setPosition(n, 0, 0));
        MultilevelFrameworkLayouter.randomLayout(g);
        Point2D nn = new Point2D.Double(0, 0);
        assertTrue(!nn.equals(AttributeHelper.getPosition(n1))
                || !nn.equals(AttributeHelper.getPosition(n2))
                || !nn.equals(AttributeHelper.getPosition(n3)));
    }

//----------------------------------------------------SolarPlacer---------------------------------------
    // the other tests for SolarPlacer are in SolarPlacerTest

    /**
     * @author Gordian
     */
    @Test
    public void getElement()  {
        Graph g = new AdjListGraph();
        MultilevelGraph mlg = new MultilevelGraph(g);
        mlg.newCoarseningLevel();
        LevelGraph ig = (LevelGraph) mlg.getTopLevel();
        final String myObj = ":(){ :|:& };:";
        ig.setObject("test key", myObj);
        assertSame(myObj, SolarPlacer.getElement(ig, "test key"));
    }

    /**
     * @author Gordian
     */
    @Test(expected = IllegalArgumentException.class)
    public void getElementFail() {
        MultilevelGraph mlg = new MultilevelGraph(new AdjListGraph());
        mlg.newCoarseningLevel();
        LevelGraph ig = (LevelGraph) mlg.getTopLevel();
        SolarPlacer.getElement(ig, "non-existent key.....");
    }
}
