// ==============================================================================
//
// SelectionAdapter.java
//
// Copyright (c) 2019, University of Konstanz
//
// ==============================================================================
package org.graffiti.selection;

/**
 * Adapter for the {@linkplain SelectionListener} interface, providing default
 * empty implementation. Useful, given you don't need all interface methods.
 * 
 * @author D. Garkov
 * @since 2.7
 *
 */
public class SelectionAdapter implements SelectionListener {

	@Override
	public void selectionChanged(SelectionEvent e) {
	}

	@Override
	public void selectionListChanged(SelectionEvent e) {
	}

}
