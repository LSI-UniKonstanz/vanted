package org.vanted.addons.stressminaddon;

import org.graffiti.editor.MainFrame;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the class {@link StartVantedWithAddon} for coverage reasons.
 * @author Jannik
 */
public class StartVantedWithAddonTest {

    /**
     * Test main method {@link StartVantedWithAddon#main(String[])}
     * @author Jannik
     */
    @Test
    public void main() {
        StartVantedWithAddon.main(new String[]{"--debug"});
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue("Is installed",
                MainFrame.getInstance().getPluginManager().getPluginEntries().stream()
                        .anyMatch(x -> x.getPlugin().getClass().equals(StressMinimizationAddon.class)));
        for (Window window : Window.getWindows()) {
            window.dispose();
        }
    }

    /**
     * Test the static method {@link StartVantedWithAddon#getAddonName()}
     * @author Jannik
     */
    @Test
    public void getAddonName() {
        assertEquals("Addon name",
                "Add-on-Stress-Minimization.xml", StartVantedWithAddon.getAddonName());
    }
}