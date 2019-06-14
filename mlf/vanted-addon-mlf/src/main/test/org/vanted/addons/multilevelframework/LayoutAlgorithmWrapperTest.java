package org.vanted.addons.multilevelframework;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.circle.CircleLayouterAlgorithm;
import org.AttributeHelper;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Node;
import org.graffiti.selection.Selection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vanted.addons.multilevelframework.pse_hack.BlockingForceDirected;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Warning: This test is slow as hell, because it starts up VANTED.
 * @author Gordian
 */
public class LayoutAlgorithmWrapperTest {
    static Thread t;

    @BeforeClass
    public static void startVanted() throws InterruptedException {
        String addOnName = "MLF-Add-On.xml";
        t = new Thread(() -> Main.startVanted(new String[0], addOnName));
        t.start();
        Thread.sleep(10000);
    }

    @Test
    public void getGUI() throws InvocationTargetException, InterruptedException {
        assertNotNull(LayoutAlgorithmWrapper.getLayoutAlgorithms().get("Circle").getGUI());
        SwingUtilities.invokeAndWait(() -> assertNotNull(LayoutAlgorithmWrapper.getLayoutAlgorithms()
                        .get(BlockingForceDirected.springName).getGUI()));
    }

    @Test
    public void execute() {
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
    }

    @Test
    public void getGUIName() {
        LayoutAlgorithmWrapper.getLayoutAlgorithms().forEach((s, law) -> {
            assertEquals(s, law.getGUIName());
        });
    }

    @Test
    public void getAlgorithm() {
        Map<String, LayoutAlgorithmWrapper> algs = LayoutAlgorithmWrapper.getLayoutAlgorithms();
        assertTrue(algs.get("Circle").getAlgorithm() instanceof CircleLayouterAlgorithm);
    }

    @Test
    public void getLayoutAlgorithms() {
        assertTrue("More than half of the whitelist not found",
                LayoutAlgorithmWrapper.getLayoutAlgorithms().size()
                        > LayoutAlgorithmWrapper.WHITELIST.size() / 2);
    }

    @Test
    public void testToString() {
        Map<String, LayoutAlgorithmWrapper> algs = LayoutAlgorithmWrapper.getLayoutAlgorithms();
        String s = algs.get("Circle").toString();
        assertTrue(s.contains("LayoutAlgorithmWrapper"));
        assertTrue(s.contains("Circle"));
    }

    @AfterClass
    public static void stopVanted() {
        t.stop();
    }
}