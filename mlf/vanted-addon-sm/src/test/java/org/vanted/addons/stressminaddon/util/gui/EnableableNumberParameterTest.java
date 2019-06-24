package org.vanted.addons.stressminaddon.util.gui;

import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;

import static org.junit.Assert.*;

public class EnableableNumberParameterTest {

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
            t.printStackTrace();
            fail("Out of bounds: Wrong exception: " + t.getClass().getSimpleName());
        }

        // test for integer
        assertFalse("Is default disabled", enpInt.value.isEnabled());
    }

    @Test
    public void alwaysEnabled() {
        EnableableNumberParameter enp = (EnableableNumberParameter) EnableableNumberParameter.alwaysEnabled(
                        0.0, 0.0, 0.0, 0.0, "", "").getValue();

        for (Component component : enp.getComponents()) {
            assertFalse("Contains no checkbox", component instanceof JCheckBox);
        }
        assertTrue("Is enabled", enp.value.isEnabled());
        
    }
}