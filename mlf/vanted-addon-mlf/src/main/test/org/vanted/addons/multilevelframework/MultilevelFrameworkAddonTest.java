package org.vanted.addons.multilevelframework;

import org.ErrorMsg;
import org.junit.After;
import org.junit.Test;

import javax.swing.*;
import java.util.Arrays;

import static org.junit.Assert.*;

public class MultilevelFrameworkAddonTest {

    private String old_name;

    @Test
    public void initializeAddon() {
        MultilevelFrameworkAddon a = new MultilevelFrameworkAddon();
        a.initializeAddon();
        assertTrue(a.getAlgorithms()[0] instanceof MultilevelFrameworkLayouter);
    }

    @Test
    public void getIcon() {
        MultilevelFrameworkAddon a = new MultilevelFrameworkAddon();
        ImageIcon old = a.getIcon();
        assertNotNull(old);
        old_name = MultilevelFrameworkAddon.ICON_NAME;
        MultilevelFrameworkAddon.ICON_NAME = "invalid";
        assertNotSame(a.getIcon(), old);
    }

    @After
    public void restore() {
        MultilevelFrameworkAddon.ICON_NAME = old_name;
    }
}