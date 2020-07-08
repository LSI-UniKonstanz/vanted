// ==============================================================================
//
// NodeAdapter.java
//
// Copyright (c) 2019, University of Konstanz
//
// ==============================================================================
package org.graffiti.event;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

/**
 * Adapter for the {@linkplain NodeListener} interface, providing default
 * empty implementation. Useful, given you don't need all interface methods.
 * 
 * @author D. Garkov
 * @since 2.7.0
 *
 */
public class NodeAdapter implements NodeListener {

	@Override
	public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall status) {
	}

	@Override
	public void transactionStarted(TransactionEvent e) {
	}

	@Override
	public void postUndirectedEdgeAdded(NodeEvent e) {
	}

	@Override
	public void postUndirectedEdgeRemoved(NodeEvent e) {
	}

	@Override
	public void preUndirectedEdgeAdded(NodeEvent e) {
	}

	@Override
	public void preUndirectedEdgeRemoved(NodeEvent e) {
	}

}
