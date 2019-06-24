package org.vanted.addons.multilevelframework;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Gordian
 */
public class MLFBackgroundTaskStatusTest {
    MLFBackgroundTaskStatus bts = new MLFBackgroundTaskStatus();

    @Test
    public void getAndSetCurrentStatusValue() {
        assertEquals(this.bts.getCurrentStatusValue(), -1);
        this.bts.setCurrentStatusValue(42);
        assertEquals(this.bts.getCurrentStatusValue(), 42);
    }

    @Test
    public void getCurrentStatusValueFine() {
        this.bts.setCurrentStatusValue(42);
        assertEquals(42, this.bts.getCurrentStatusValueFine(), 0.0001);
    }

    @Test
    public void getCurrentStatusMessage1() {
        assertTrue(this.bts.getCurrentStatusMessage1().isEmpty());
        String msg = "asdf";
        this.bts.statusMessage = msg;
        assertSame(msg, this.bts.getCurrentStatusMessage1());
    }

    @Test public void getCurrentStatusMessage2() { assertNull(this.bts.getCurrentStatusMessage2()); }

    @Test
    public void pleaseStop() {
        assertFalse(this.bts.isStopped);
        this.bts.pleaseStop();
        assertTrue(this.bts.isStopped);
    }

    @Test public void pluginWaitsForUser() { assertFalse(this.bts.pluginWaitsForUser()); }

    @Test
    public void pleaseContinueRun() {
        // this method should do nothing in the current implementation
        this.bts.pleaseContinueRun();
        assertFalse(this.bts.isStopped);
        this.bts.pleaseStop();
        this.bts.pleaseContinueRun();
        assertTrue(this.bts.isStopped);
    }
}