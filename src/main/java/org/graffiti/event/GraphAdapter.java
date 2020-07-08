// ==============================================================================
//
// GraphAdapter.java
//
// Copyright (c) 2019, University of Konstanz
//
// ==============================================================================
package org.graffiti.event;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

/**
 * Adapter for the {@linkplain GraphListener} interface, providing default empty
 * implementation. Useful, given you don't need all interface methods.
 * 
 * @author D. Garkov
 * @since 2.7.0
 *
 */
public class GraphAdapter implements GraphListener {

	@Override
	public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall status) {
	}

	@Override
	public void transactionStarted(TransactionEvent e) {
	}

	@Override
	public void postEdgeAdded(GraphEvent e) {
	}

	@Override
	public void postEdgeRemoved(GraphEvent e) {
	}

	@Override
	public void postGraphCleared(GraphEvent e) {
	}

	@Override
	public void postNodeAdded(GraphEvent e) {
	}

	@Override
	public void postNodeRemoved(GraphEvent e) {
	}

	@Override
	public void preEdgeAdded(GraphEvent e) {
	}

	@Override
	public void preEdgeRemoved(GraphEvent e) {
	}

	@Override
	public void preGraphCleared(GraphEvent e) {
	}

	@Override
	public void preNodeAdded(GraphEvent e) {
	}

	@Override
	public void preNodeRemoved(GraphEvent e) {
	}

}
