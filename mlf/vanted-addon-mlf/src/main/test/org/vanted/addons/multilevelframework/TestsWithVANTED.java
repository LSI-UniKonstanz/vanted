package org.vanted.addons.multilevelframework;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.circle.CircleLayouterAlgorithm;
import org.AttributeHelper;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.*;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vanted.addons.multilevelframework.pse_hack.BlockingForceDirected;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

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
        Thread.sleep(8000); // increase if your computer takes longer to start VANTED
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
        new LayoutAlgorithmWrapper("", new MultilevelFrameworkLayouter(), true);
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

}
