// ==============================================================================
//
// EdgeAdapter.java
//
// Copyright (c) 2019, University of Konstanz
//
// ==============================================================================
package org.graffiti.event;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

/**
 * Adapter for the {@linkplain EdgeListener} interface, providing default
 * empty implementation. Useful, given you don't need all interface methods.
 * 
 * @author D. Garkov
 * @since 2.7.0
 *
 */
public class EdgeAdapter implements EdgeListener {

	@Override
	public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall status) {
	}

	@Override
	public void transactionStarted(TransactionEvent e) {
	}

	@Override
	public void postDirectedChanged(EdgeEvent e) {
	}

	@Override
	public void postEdgeReversed(EdgeEvent e) {
	}

	@Override
	public void postSourceNodeChanged(EdgeEvent e) {
	}

	@Override
	public void postTargetNodeChanged(EdgeEvent e) {
	}

	@Override
	public void preDirectedChanged(EdgeEvent e) {
	}

	@Override
	public void preEdgeReversed(EdgeEvent e) {
	}

	@Override
	public void preSourceNodeChanged(EdgeEvent e) {
	}

	@Override
	public void preTargetNodeChanged(EdgeEvent e) {
	}

}
