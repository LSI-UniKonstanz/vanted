package org.vanted.addons.stressminaddon.util.gui;

import org.FolderPanel;
import org.GuiRow;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.dialog.ParameterEditPanel;
import org.graffiti.graph.Node;
import org.graffiti.managers.pluginmgr.DefaultPluginManager;
import org.graffiti.plugin.editcomponent.JComponentParameterEditor;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.JComponentParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.SelectionParameter;
import org.graffiti.plugins.editcomponents.defaults.BooleanEditComponent;
import org.graffiti.selection.Selection;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vanted.addons.stressminaddon.InitialPlacer;
import org.vanted.addons.stressminaddon.IterativePositionAlgorithm;
import org.vanted.addons.stressminaddon.util.NodeValueMatrix;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Permission;
import java.util.List;
import java.util.*;
import java.util.prefs.Preferences;

import static org.junit.Assert.*;

/**
 * Test class {@link ParameterizableSelectorParameter}
 * @author Jannik
 */
public class ParameterizableSelectorParameterTest {

    /** The object to work with. */
    private ParameterizableSelectorParameter psp;

    /** The list containing the {@link Parameterizable}s to work with (untyped to allow {@link Parameterizable}s and {@link Describable}s. */
    private List list = new ArrayList();

    /** Make needed VANTED interaction somewhat functional. @author Jannik */
    @BeforeClass
    public static void setUpVANTED() {
        new MainFrame(new DefaultPluginManager(Preferences.userRoot()), Preferences.userRoot());
        MainFrame.getInstance().getEditComponentManager().getEditComponents().put(JComponentParameter.class, JComponentParameterEditor.class);
        MainFrame.getInstance().getEditComponentManager().getEditComponents().put(BooleanParameter.class, BooleanEditComponent.class);
    }

    /**
     * Set up needed list.
     * @author Jannik
     */
    @Before
    public void setUp() {
        // add some dummy objects
        list.add(new InitialPlacer() {
            @Override public List<Vector2d> calculateInitialPositions(List<Node> nodes, NodeValueMatrix distances) { return null; }
            @Override public String getName() { return "TEST1"; }
            @Override public String getDescription() { return null; }
            @Override public void setParameters(Parameter[] parameters) {}
            @Override public Parameter[] getParameters() {
                return  new Parameter[]{
                        new BooleanParameter(true, "1", "Test1"),
                        new BooleanParameter(false, "2", "Test2"),
                };
            }
        });
        list.add(new IterativePositionAlgorithm() {
            @Override public List<Vector2d> nextIteration(List<Node> nodes, List<Vector2d> positions, NodeValueMatrix distances, NodeValueMatrix weights) { return null; }
            @Override public String getName() { return "TEST2"; }
            @Override public String getDescription() { return "Description"; }
            @Override public void setParameters(Parameter[] parameters) {}
            @Override public Parameter[] getParameters() {
                return  new Parameter[] {
                        new BooleanParameter(true, "3", "Test3"),
                        new SelectionParameter("4", "Test4"),
                        new BooleanParameter(true, "5", "Test5"),
                };
            }
        });
    }

    /**
     * Test static method
     * {@link ParameterizableSelectorParameter#getFromList(int, java.util.List, org.graffiti.selection.Selection, java.lang.String, java.lang.String)}
     * and default behaviour.
     * @author Jannik
     */
    @Test
    public void getFromList() {
        this.psp = ((ParameterizableSelectorParameter) ParameterizableSelectorParameter.getFromList(
                0, list, new Selection(), "Test", "TestDesc").getValue());
        ParameterEditPanel pep = new ParameterEditPanel(
                new Parameter[]{
                        new BooleanParameter(true, "Before", ""),
                        new JComponentParameter(this.psp, "jctest", "jctesting"),
                        new BooleanParameter(true, "After", ""),
                },
                MainFrame.getInstance().getEditComponentManager().getEditComponents(),
                new Selection(), "Title", true, "Heading");

        final FolderPanel panel = ((FolderPanel) ((JScrollPane) pep.getComponent(0)).getViewport().getComponent(0));
        // should start with 5 (Before + ParameterizableSelectorParameter + 2 Parameters of first list entry + After)
        assertEquals("Loaded parameters", 5, panel.getRowCount());
        ArrayList<GuiRow> rows = panel.getVisibleGuiRows();
        assertEquals("Before not changed", "Before", ((JLabel) rows.get(0).left).getText());
        assertEquals("After not changed", "After", ((JLabel) rows.get(4).left).getText());

        final JComboBox box = (JComboBox) psp.getComponent(0);
        box.setSelectedIndex(1);
        // should start with 6 (Before + ParameterizableSelectorParameter + 3 Parameters of first list entry + After)
        assertEquals("Change selected", 6, panel.getRowCount());
        rows = panel.getVisibleGuiRows();
        assertEquals("Before not changed", "Before", ((JLabel) rows.get(0).left).getText());
        assertEquals("After not changed", "After", ((JLabel) rows.get(5).left).getText());

        // now click on the checkbox
        ((JCheckBox) panel.getVisibleGuiRows().get(2).right).doClick();
        assertFalse("Clicked on parameter checkbox", ((BooleanParameter) psp.getUpdatedParameters()[0]).getBoolean());
    }

    /**
     * Test method {@link ParameterizableSelectorParameter#getUpdatedParameters()}
     * @author Jannik
     */
    @Test
    public void getUpdatedParameters() {
        this.psp = ((ParameterizableSelectorParameter) ParameterizableSelectorParameter.getFromList(
                0, list, new Selection(), "Test", "TestDesc").getValue());
        Parameter[] result = psp.getUpdatedParameters();
        Parameter[] expected = ((Parameterizable) list.get(0)).getParameters();

        for (int param = 0; param < result.length; param++) {
            assertTrue("Same type", result[param] instanceof BooleanParameter);
            assertEquals("Same value", ((BooleanParameter) expected[param]).getBoolean(), ((BooleanParameter) result[param]).getBoolean());
        }
    }

    /**
     * Test method {@link ParameterizableSelectorParameter#getSelectedParameterizable()}
     * @author Jannik
     */
    @Test
    public void getSelectedParameterizable() {
        this.psp = ((ParameterizableSelectorParameter) ParameterizableSelectorParameter.getFromList(
                0, list, new Selection(), "Test", "TestDesc").getValue());
        assertSame(this.list.get(0), psp.getSelectedParameterizable());
    }

    /**
     * Test static method {@link ParameterizableSelectorParameter#getFromMap(String, Map, Map, Selection, String, String)}
     * @author Jannik
     */
    @Test
    public void getFromMapDescriptions() {
        Map<String, Parameterizable> map = new HashMap<>();
        map.put("Item1", (Parameterizable) list.get(1));
        map.put("Item2", (Parameterizable) list.get(0));
        Map<String, String> map2 = new HashMap<>();
        map2.put("Item2", "Nice!");
        this.psp = ((ParameterizableSelectorParameter) ParameterizableSelectorParameter.getFromMap(
                "Item1", map, map2, new Selection(), "Test", "TestDesc").getValue());
        final JComboBox box = (JComboBox) psp.getComponent(0);
        // No tooltip set for
        assertNull("No tooltip", box.getToolTipText());

        // Provided tooltip set
        box.setSelectedIndex(box.getSelectedIndex() == 0 ? 1 : 0); // set to other value
        assertEquals("Description set as tooltip", map2.get("Item2"), box.getToolTipText());
    }

    /**
     * Test static method {@link ParameterizableSelectorParameter#getFromMap(String, Map, Selection, String, String)}
     * @author Jannik
     */
    @Test
    public void getFromMapInferredDescriptions() {
        Map<String, Parameterizable> map = new HashMap<>();
        map.put("Item1", (Parameterizable) list.get(1));
        map.put("Item2", (Parameterizable) list.get(0));
        this.psp = ((ParameterizableSelectorParameter) ParameterizableSelectorParameter.getFromMap(
                "Item2", map, new Selection(), "Test", "TestDesc").getValue());
        final JComboBox box = (JComboBox) psp.getComponent(0);
        // No tooltip set for
        assertNull("No tooltip", box.getToolTipText());

        // Internal tooltip set
        box.setSelectedIndex(box.getSelectedIndex() == 0 ? 1 : 0); // set to other value
        assertEquals("'Description' set as tooltip", ((Describable) list.get(1)).getDescription(), box.getToolTipText());
    }

    /**
     * Test behaviour if provided list is empty
     * @author Jannik
     */
    @Test
    public void empty() {
        this.psp = ((ParameterizableSelectorParameter) ParameterizableSelectorParameter.getFromList(
                0, Collections.emptyList(), new Selection(), "Test", "TestDesc").getValue());
        final JComboBox box = (JComboBox) psp.getComponent(0);
        final JButton button = (JButton) psp.getComponent(1);
        assertFalse(box.isEnabled());
        assertFalse(button.isEnabled());
        assertFalse(button.isVisible());
    }

    /**
     * Test fallback behaviour
     */
    @Test
    public void fallback() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        this.psp = ((ParameterizableSelectorParameter) ParameterizableSelectorParameter.getFromList(
                0, list, new Selection(), "Test", "TestDesc").getValue());
        JComboBox box = (JComboBox) psp.getComponent(0);
        final JButton button = (JButton) psp.getComponent(1);
        new ParameterEditPanel(new Parameter[]{new JComponentParameter(this.psp, "", "")},
                MainFrame.getInstance().getEditComponentManager().getEditComponents(),
                new Selection(), "", true, "");
        final Field field = psp.getClass().getDeclaredField("editPanelAccessible");
        field.setAccessible(true);
        field.set(psp, false);
        box.setSelectedIndex(1);

        int activeWindows = Window.getWindows().length;

        assertTrue("Button enabled", button.isEnabled());
        Thread t = new Thread(button::doClick);
        t.start();
        Thread.sleep(100);
        Window[] windows = Window.getWindows();
        assertEquals("Window opened", activeWindows+1, windows.length);
        ((JButton)((JPanel)((JOptionPane)((JDialog)
                windows[1]).getContentPane().getComponent(0)).getComponent(1)).getComponent(0))
                .doClick(); // click on okay button to close

        // check no window opened
        Map<String, Parameterizable> emptyParams = new HashMap<>(2);
        emptyParams.put("1", new InitialPlacer(){
                    @Override public Parameter[] getParameters() { return null; }
                    @Override public void setParameters(Parameter[] parameters) { }
                    @Override public String getName() { return ""; }
                    @Override public String getDescription() { return null; }
                    @Override public List<Vector2d> calculateInitialPositions(List<Node> nodes, NodeValueMatrix distances) { return null; }});
        emptyParams.put("2", emptyParams.get("1"));
        this.psp = ((ParameterizableSelectorParameter) ParameterizableSelectorParameter.getFromMap(
                "1", emptyParams, null, new Selection(), "Test", "TestDesc").getValue());
        final JButton button2 = (JButton) psp.getComponent(1);
        new ParameterEditPanel(new Parameter[]{new JComponentParameter(this.psp, "", "")},
                MainFrame.getInstance().getEditComponentManager().getEditComponents(),
                new Selection(), "", true, "");
        field.setAccessible(true);
        field.set(psp, false);
        box = (JComboBox) psp.getComponent(0);
        box.setSelectedIndex(box.getSelectedIndex() == 0 ? 1 : 0); // set to other value

        assertFalse("Button disabled", button2.isEnabled());
        button2.setEnabled(true);
        activeWindows = Window.getWindows().length;
        t = new Thread(button2::doClick);
        t.start();
        Thread.sleep(100);
        assertEquals("Window not opened", activeWindows, Window.getWindows().length);
        t.join();
    }

    /**
     * Test possible thrown exceptions.
     * @author Jannik
     */
    @Test
    public void exceptions() throws NoSuchFieldException, IllegalAccessException {
        // test vanilla exceptions
        try {
            try { // initial selection not in map (with descriptions)
                ParameterizableSelectorParameter.getFromMap("Something",
                        Collections.singletonMap("Test1", (Parameterizable) list.get(0)), null, new Selection(), null, null);
                fail("No exception thrown");
            } catch (IllegalArgumentException e) {}
            try { // initial selection not in map (with derived descriptions)
                ParameterizableSelectorParameter.getFromMap("Something",
                        Collections.singletonMap("Test1", (Parameterizable) list.get(0)), new Selection(), null, null);
                fail("No exception thrown");
            } catch (IllegalArgumentException e) {}
            try { // initial selection not in list
                ParameterizableSelectorParameter.getFromList(-1,
                        list, new Selection(), null, null);
                fail("No exception thrown");
            } catch (IndexOutOfBoundsException e) {}
            try { // element without name
                ParameterizableSelectorParameter.getFromList(0,
                        Collections.singletonList(new InitialPlacer(){
                            @Override public Parameter[] getParameters() { return new Parameter[0]; }
                            @Override public void setParameters(Parameter[] parameters) {}
                            @Override public String getName() { return null; }
                            @Override public String getDescription() { return null; }
                            @Override public List<Vector2d> calculateInitialPositions(List<Node> nodes, NodeValueMatrix distances) { return null; }
                        }), new Selection(), null, null);
                fail("No exception thrown");
            } catch (IllegalArgumentException e) {}
        } catch (Throwable t) {
            if (t instanceof AssertionError) throw t;
            fail("Wrong exception thrown: " + t.getClass().getSimpleName());
        }

        // test exceptions that shouldn't be thrown for coverage
        // (kids, don't do this at home)
        this.psp = ((ParameterizableSelectorParameter) ParameterizableSelectorParameter.getFromList(
                0, list, new Selection(), "Test", "TestDesc").getValue());

        final PrintStream oldErr = System.err;
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stream));
        // Disable 'setAccessible()' for a bit
        SecurityManager sm = System.getSecurityManager();
        SecurityManager testingSecurityManager = new SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {
                if ("suppressAccessChecks".equals(perm.getName())) {
                    throw new SecurityException("No, no, no");
                }
            }
        };
        System.setSecurityManager(testingSecurityManager);

        // this should now throw a security exception
        new ParameterEditPanel(new Parameter[]{new JComponentParameter(this.psp, "", "")},
                MainFrame.getInstance().getEditComponentManager().getEditComponents(),
                new Selection(), "", true, "");

        System.setSecurityManager(sm);

        assertFalse("Error to out", stream.toString().trim().isEmpty());

        this.psp = ((ParameterizableSelectorParameter) ParameterizableSelectorParameter.getFromList(
                0, list, new Selection(), "Test", "TestDesc").getValue());
        ParameterEditPanel pep = new ParameterEditPanel(new Parameter[]{new JComponentParameter(this.psp, "", "")},
                MainFrame.getInstance().getEditComponentManager().getEditComponents(),
                new Selection(), "", true, "");

        final Field pspMethodToChange = this.psp.getClass().getDeclaredField("editPanelAddRow");
        pspMethodToChange.setAccessible(true);
        ((Method) pspMethodToChange.get(this.psp)).setAccessible(false); // psp should not be able to call this method
        final JComboBox box = (JComboBox) this.psp.getComponent(0);
        box.setSelectedIndex(box.getSelectedIndex() == 0 ? 1 : 0); // set to other value
        final FolderPanel panel = ((FolderPanel) ((JScrollPane) pep.getComponent(0)).getViewport().getComponent(0));
        // should be 1 (only PSP) because of error
        assertEquals("parameters not loaded", 1, panel.getRowCount());
        System.setErr(oldErr);
    }
}