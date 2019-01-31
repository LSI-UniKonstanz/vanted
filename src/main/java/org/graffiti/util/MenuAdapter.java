// ==============================================================================
//
// MenuAdapter.java
//
// Copyright (c) 2019, University of Konstanz
//
// ==============================================================================
package org.graffiti.util;

import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * Adapter for the {@linkplain MenuListener} interface, providing default
 * empty implementation. Useful, given you don't need all interface methods.
 * 
 * @author D. Garkov
 * @since 2.6.6
 *
 */
public class MenuAdapter implements MenuListener {

	@Override
	public void menuCanceled(MenuEvent e) {
	}

	@Override
	public void menuDeselected(MenuEvent e) {
	}

	@Override
	public void menuSelected(MenuEvent e) {
	}

}
