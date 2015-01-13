// ==============================================================================
//
// TransactionEvent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: TransactionEvent.java,v 1.7 2010/12/22 13:05:34 klukas Exp $

package org.graffiti.event;

/**
 * Contains a transaction event. A <code>TransactionEvent</code> object is
 * passed to every <code>TransactionListener</code> object which is registered
 * to receive a transaction event.
 * 
 * @version $Revision: 1.7 $
 * @see TransactionListener
 */
public class TransactionEvent
					extends AbstractEvent {
	// ~ Instance fields ========================================================
	
	private static final long serialVersionUID = 1L;
	/**
	 * Contains the objects that have been changed during the lifetime of a
	 * transaction.
	 */
	private TransactionHashMap changedObjects;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a transaction event object with the specified source
	 * component.
	 * 
	 * @param source
	 *           the source component of the transaction.
	 * @param changedObjects
	 *           DOCUMENT ME!
	 */
	public TransactionEvent(Object source, TransactionHashMap changedObjects) {
		this(source);
		this.changedObjects = changedObjects;
	}
	
	/**
	 * Constructs a transaction event object with the specified source
	 * component.
	 * 
	 * @param source
	 *           the graph that originated the event.
	 */
	public TransactionEvent(Object source) {
		super(source);
		changedObjects = null;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the <code>Set</code> of objects that have been changed during
	 * the transaction.
	 * 
	 * @return the graph that originated this event.
	 */
	public TransactionHashMap getChangedObjects() {
		return changedObjects;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
