package org.vanted.addons.stressminaddon.util.gui;

import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;

import static org.junit.Assert.*;

/**
 * Tests the {@link org.vanted.addons.stressminaddon.util.gui.EnableableNumberParameter} class.
 * @author Jannik
 */
public class EnableableNumberParameterTest {

    /**
     * Test the static method
     * {@link EnableableNumberParameter#canBeEnabledDisabled(Number, Number, Number, Number, boolean, String, String, String, String)}.
     * @author Jannik
     */
    @Test
    @SuppressWarnings("unchecked")
    public void canBeEnabledDisabled() {
        JPanel pane = new JPanel();
        // tests for double and both
        EnableableNumberParameter<Double> enpDouble = (EnableableNumberParameter<Double>) EnableableNumberParameter.canBeEnabledDisabled(
                0.0, 0.0, 0.0, 0.0, true, "", "", "", "").getValue();
        EnableableNumberParameter<Integer> enpInt = (EnableableNumberParameter<Integer>) EnableableNumberParameter.canBeEnabledDisabled(
                0, 0, 5, 3, false, "", "", "", "").getValue();
        pane.add(enpDouble);
        pane.add(enpInt);
        pane.setVisible(true);

        JCheckBox box = null;
        for (Component component : enpDouble.getComponents()) {
            if (component instanceof JCheckBox) {
                box = (JCheckBox) component;
                break;
            }
        }
        assertNotNull("Contains check box", box);
        assertTrue("Is default enabled", enpDouble.value.isEnabled());
        box.doClick();
        assertFalse("Chick is disabled", enpDouble.value.isEnabled());
        box.doClick();
        assertTrue("Chick is enabled", enpDouble.value.isEnabled());
        try {
            ((JSpinner.DefaultEditor) enpDouble.value.getEditor()).getTextField().setValue(3.0);
            enpDouble.value.commitEdit();
            fail("Out of bounds no exception thrown!");
        } catch (ParseException e) {
        } catch (Throwable t) {
            if ("Out of bounds no exception thrown!".equals(t.getMessage()))
                throw t;
            fail("Out of bounds: Wrong exception: " + t.getClass().getSimpleName());
            t.printStackTrace();
        }

        // test for integer
        assertFalse("Is default disabled", enpInt.value.isEnabled());
    }

    /**
     * Test the static method
     * {@link EnableableNumberParameter#alwaysEnabled(Number, Number, Number, Number, String, String)}.
     * @author Jannik
     */
    @Test
    public void alwaysEnabled() {
        EnableableNumberParameter enp = (EnableableNumberParameter) EnableableNumberParameter.alwaysEnabled(
                        0.0, 0.0, 0.0, 0.0, "", "").getValue();

        for (Component component : enp.getComponents()) {
            assertFalse("Contains no checkbox", component instanceof JCheckBox);
        }
        assertTrue("Is enabled", enp.value.isEnabled());
    }

    /**
     * Test the method {@link EnableableNumberParameter#isEnabled()}.
     * @author Jannik
     */
    @Test
    @SuppressWarnings("unchecked")
    public void isEnabled() {
        // default "true"
        EnableableNumberParameter<Double> enpDouble = (EnableableNumberParameter<Double>) EnableableNumberParameter.canBeEnabledDisabled(
                0.0, 0.0, 0.0, 0.0, true, "", "", "", "").getValue();
        assertTrue("Is default true", enpDouble.isEnabled());
        assertEquals("Is same as box (true)", enpDouble.isEnabled(), enpDouble.value.isEnabled());

        // default "false"
        enpDouble = (EnableableNumberParameter<Double>) EnableableNumberParameter.canBeEnabledDisabled(
                0.0, 0.0, 0.0, 0.0, false, "", "", "", "").getValue();
        assertFalse("Is default false", enpDouble.isEnabled());
        assertEquals("Is same as box (false)", enpDouble.isEnabled(), enpDouble.value.isEnabled());
    }

    /**
     * Test the method {@link EnableableNumberParameter#getValue()}.
     * @author Jannik
     */
    @Test
    @SuppressWarnings("unchecked")
    public void getValue() {
        // default "true"
        EnableableNumberParameter<Double> enpDouble = (EnableableNumberParameter<Double>) EnableableNumberParameter.canBeEnabledDisabled(
                10.5, 0.0, 42.0, 0.0, true, "", "", "", "").getValue();
        assertEquals("Get double value", 10.5, enpDouble.getValue(), 0.0001);

        // default "false"
        EnableableNumberParameter<Integer> enpInteger = (EnableableNumberParameter<Integer>) EnableableNumberParameter.canBeEnabledDisabled(
                21, 0, 42, 0, false, "", "", "", "").getValue();
        assertEquals("Get integer value", 21, (int) enpInteger.getValue());
    }
}