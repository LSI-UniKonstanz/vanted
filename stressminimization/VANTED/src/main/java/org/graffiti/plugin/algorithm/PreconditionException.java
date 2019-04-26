// ==============================================================================
//
// PreconditionException.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: PreconditionException.java,v 1.6 2010/12/22 13:05:32 klukas Exp $

/*
 * $Id: PreconditionException.java,v 1.6 2010/12/22 13:05:32 klukas Exp $
 */
package org.graffiti.plugin.algorithm;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Throws in the context of precondition failures.
 * 
 * @vanted.revision 2.6.5
 */
public class PreconditionException extends Exception {
	// ~ Instance fields ========================================================

	private static final long serialVersionUID = 1L;
	/** List of error entries. */
	private List<Entry> errors;

	// ~ Constructors ===========================================================

	/**
	 * Creates a new PreconditionException object.
	 * 
	 * @param msg
	 *            error entry describing the cause
	 */
	public PreconditionException(String msg) {
		this();
		add(msg);
	}

	/**
	 * Creates a new PreconditionException object.
	 */
	public PreconditionException() {
		this.errors = new LinkedList<Entry>();
	}

	// ~ Methods ================================================================

	/**
	 * Checks, if this PreconditionException contains any error entries.
	 * 
	 * @return true, when there haven't been added any messages
	 */
	public boolean isEmpty() {
		return errors.isEmpty();
	}

	@Override
	public String getMessage() {
		StringBuffer sb = new StringBuffer();
		sb.append("The following preconditions are not satisfied:<br><ul>");

		for (Iterator<Entry> i = errors.iterator(); i.hasNext();) {
			Entry error = (Entry) i.next();
			sb.append("<li>");
			sb.append(error.cause);
			sb.append("");
		}

		return sb.toString();
	}

	/**
	 * Adds a new cause message, together with the responsible source.
	 * 
	 * @param cause
	 *            message about the reason
	 * @param source
	 *            an Object from which it originated the exception
	 */
	public void add(String cause, Object source) {
		errors.add(new Entry(cause, source));
	}

	/**
	 * Adds a new cause message without a source.
	 * 
	 * @param cause
	 *            message about the reason
	 */
	public void add(String cause) {
		errors.add(new Entry(cause, null));
	}

	/**
	 * Returns an iterator over all <code>Error</code>s.
	 * 
	 * @return an iterator.
	 */
	public Iterator<Entry> iterator() {
		return errors.iterator();
	}

	// ~ Inner Classes ==========================================================

	/**
	 * Contains a cause and the source object (i.e.: a Graph, Node or Edge).
	 * 
	 * @version 1.6
	 * @vanted.revision 2.6.5
	 */
	class Entry {
		/** The source of the exception */
		public Object source;

		/** String message about the reason. */
		public String cause;

		/**
		 * Creates a new Entry object.
		 * 
		 * @param cause
		 *            message about the reason
		 * @param source
		 *            an Object from which it originated the exception
		 */
		public Entry(String cause, Object source) {
			this.cause = cause;
			this.source = source;
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
