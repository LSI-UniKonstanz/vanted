// ==============================================================================
//
// SessionAdapterExt.java
//
// Copyright (c) 2019, University of Konstanz
//
// ==============================================================================
package org.graffiti.session;

/**
 * Adapter for the {@linkplain SessionListenerExt} interface, providing default
 * empty implementation. Useful, given you don't need all interface methods.
 * 
 * @author D. Garkov
 * @since 2.6.6
 *
 */
public class SessionAdapterExt implements SessionListenerExt {

	@Override
	public void sessionChanged(Session s) {
	}

	@Override
	public void sessionDataChanged(Session s) {
	}

	@Override
	public void sessionClosed(Session session) {
	}

}
