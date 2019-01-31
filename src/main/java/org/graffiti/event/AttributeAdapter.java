// ==============================================================================
//
// AttributeAdapter.java
//
// Copyright (c) 2019, University of Konstanz
//
// ==============================================================================
package org.graffiti.event;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

/**
 * Adapter for the {@linkplain AttributeListener} interface, providing default
 * empty implementation. Useful, given you don't need all interface methods.
 * 
 * @author D. Garkov
 * @since 2.6.6
 *
 */
public class AttributeAdapter implements AttributeListener {

	@Override
	public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall status) {
	}

	@Override
	public void transactionStarted(TransactionEvent e) {
	}

	@Override
	public void postAttributeAdded(AttributeEvent e) {
	}

	@Override
	public void postAttributeChanged(AttributeEvent e) {
	}

	@Override
	public void postAttributeRemoved(AttributeEvent e) {
	}

	@Override
	public void preAttributeAdded(AttributeEvent e) {
	}

	@Override
	public void preAttributeChanged(AttributeEvent e) {
	}

	@Override
	public void preAttributeRemoved(AttributeEvent e) {
	}

}
