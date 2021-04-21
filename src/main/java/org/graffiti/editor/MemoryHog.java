/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Feb 2, 2010 by Christian Klukas
 */

package org.graffiti.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

/**
 * <p>
 * Memory hogs can be classes that tend to gather a noticeable set of resources,
 * that due to being still referenced, cannot be taken care of by the GC and
 * must be released explicitly from memory. Therefore to allow such deallocation
 * and improve memory performance by preventing memory leaks and overheads for
 * data structures (e.g. expansion of maps or lists). A typical example would be
 * a service, which has to perform some pooling on a given time interval.
 * </p>
 * 
 * @author Klukas, D. Garkov
 * @vanted.revision 2.7.0
 * @version v2.0: refactored as interface.
 */
public interface MemoryHog {
	
	/**
	 * To enable free-up on application clear memory event, when the user requests
	 * such, the hog has to be registered beforehand.
	 */
	public default void registerMemoryHog() {
		GravistoService.addKnownMemoryHog(this);
	}
	
	/**
	 * It sets up a Timer that would free memory after 1 minute passes by
	 * continuously during the life of the application. The Timer's delay shouldn't
	 * be too small, as it could lead to excessive reallocation and memory
	 * overheads.
	 */
	default void enablePeriodicFreeMemory() {
		Timer t = new Timer(60000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (doFreeMemory()) {
					freeMemory();
				}
			}
		});
		t.setRepeats(true);
		t.start();
	}
	
	/**
	 * We use an array as wrapper to access the static value in a non-static method.
	 * Do not set explicitly, use {@link MemoryHog#noteRequest()} to set.
	 */
	static long[] lastUsageTime = new long[] { 0 };
	
	default boolean doFreeMemory() {
		return System.currentTimeMillis() - lastUsageTime[0] > 2000;
	}
	
	default void noteRequest() {
		lastUsageTime[0] = System.currentTimeMillis();
	}
	
	/**
	 * <p>
	 * To release a data structure, just re-instantiate it, and GC would collect the
	 * now unreachable object some time in the future.
	 * </p>
	 * Apart for deallocation purposes, it can also be used to reset any other
	 * dependencies.
	 */
	public void freeMemory();
}
