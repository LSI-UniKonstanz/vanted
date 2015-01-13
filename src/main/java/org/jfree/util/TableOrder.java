/*
 * ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Project Info: http://www.jfree.org/jcommon/index.html
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 * ---------------
 * TableOrder.java
 * ---------------
 * (C) Copyright 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: TableOrder.java,v 1.2 2010/12/14 07:02:00 morla Exp $
 * Changes:
 * --------
 * 29-Jan-2004 : Version 1 (DG);
 */

package org.jfree.util;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Used to indicate the processing order for a table (by row or by column).
 */
public final class TableOrder implements Serializable {

	/** By row. */
	public static final TableOrder BY_ROW = new TableOrder("TableOrder.BY_ROW");

	/** By column. */
	public static final TableOrder BY_COLUMN = new TableOrder("TableOrder.BY_COLUMN");

	/** The name. */
	private String name;

	/**
	 * Private constructor.
	 * 
	 * @param name
	 *           the name.
	 */
	private TableOrder(final String name) {
		this.name = name;
	}

	/**
	 * Returns a string representing the object.
	 * 
	 * @return The string.
	 */
	public String toString() {
		return this.name;
	}

	/**
	 * Returns <code>true</code> if this object is equal to the specified object, and <code>false</code> otherwise.
	 * 
	 * @param o
	 *           the other object.
	 * @return A boolean.
	 */
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TableOrder)) {
			return false;
		}

		final TableOrder tableOrder = (TableOrder) o;

		if (!this.name.equals(tableOrder.name)) {
			return false;
		}

		return true;
	}

	/**
	 * Returns a hash code value for the object.
	 * 
	 * @return the hashcode
	 */
	public int hashCode() {
		return this.name.hashCode();
	}

	/**
	 * Ensures that serialization returns the unique instances.
	 * 
	 * @return The object.
	 * @throws ObjectStreamException
	 *            if there is a problem.
	 */
	private Object readResolve() throws ObjectStreamException {
		if (this.equals(TableOrder.BY_ROW)) {
			return TableOrder.BY_ROW;
		} else
			if (this.equals(TableOrder.BY_COLUMN)) {
				return TableOrder.BY_COLUMN;
			}
		return null;
	}

}
