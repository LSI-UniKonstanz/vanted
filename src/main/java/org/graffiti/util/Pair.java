// ==============================================================================
//
// Pair.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: Pair.java,v 1.4 2010/12/22 13:05:33 klukas Exp $

package org.graffiti.util;

/**
 * Encapsulates two values in a pair.
 * 
 * @author Paul, D. Garkov
 * @version 2.7.0
 * @vanted.revision 2.7.0
 */
public class Pair<T, V> {

	private T val1;

	private V val2;

	/**
	 * Creates a new Pair object.
	 * 
	 * @param first
	 *            the first (left) component
	 * @param second
	 *            the second (right) component
	 */
	public Pair(T first, V second) {
		this.val1 = first;
		this.val2 = second;
	}

	/**
	 * Returns the first (left) component of the pair.
	 * 
	 * @return the value of first component
	 */
	public T getFst() {
		return val1;
	}

	/**
	 * Returns the second (right) component of the pair.
	 * 
	 * @return the value of second component
	 */
	public V getSnd() {
		return val2;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Pair))
			return false;

		Pair<?, ?> other = (Pair<?, ?>) obj;

		return this.val1.equals(other.getFst()) && this.val2.equals(other.getSnd());
	}

	@Override
	public int hashCode() {
		return this.val1.hashCode() ^ this.val2.hashCode();
	}

	@Override
	public String toString() {
		return "Pair<" + this.val1.getClass().getSimpleName() + ", " + this.val2.getClass().getSimpleName() + "> = ("
				+ this.val1.toString() + ", " + this.val2.toString() + ")";
	}

}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
