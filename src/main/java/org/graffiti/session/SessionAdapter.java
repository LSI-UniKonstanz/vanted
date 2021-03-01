// ==============================================================================
//
// SessionAdapter.java
//
// Copyright (c) 2019, University of Konstanz
//
// ==============================================================================
package org.graffiti.session;

/**
 * Adapter for the {@linkplain SessionListener} interface, providing default
 * empty implementation. Useful, given you don't need all interface methods.
 * 
 * @author D. Garkov
 * @since 2.7.0
 */
public class SessionAdapter implements SessionListener {
	
	@Override
	public void sessionChanged(Session s) {
	}
	
	@Override
	public void sessionDataChanged(Session s) {
	}
	
}
