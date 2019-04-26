/*
 * ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Project Info: http://www.jfree.org/jfreechart/index.html
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
 * ----------------------
 * IntervalXYDataset.java
 * ----------------------
 * (C) Copyright 2001-2004, by Object Refinery Limited and Contributors.
 * Original Author: Mark Watson (www.markwatson.com);
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: IntervalXYDataset.java,v 1.2 2010/12/14 07:02:03 morla Exp $
 * Changes
 * -------
 * 18-Oct-2001 : Version 1, thanks to Mark Watson (DG);
 * 22-Oct-2001 : Renamed DataSource.java --> Dataset.java etc (DG);
 * 06-May-2004 : Added methods that return double primitives (DG);
 */

package org.jfree.data;

/**
 * An extension of the {@link XYDataset} interface that allows a range of data
 * to be defined for the X values, the Y values, or both the X and Y values.
 * <P>
 * This versatile interface will be used to support (among other things) bar
 * plots against numerical axes.
 * 
 * @author Mark Watson
 */
public interface IntervalXYDataset extends XYDataset {

	/**
	 * Returns the starting X value for the specified series and item.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item within a series (zero-based index).
	 * @return the starting X value for the specified series and item.
	 */
	public Number getStartXValue(int series, int item);

	/**
	 * Returns the start x-value (as a double primitive) for an item within a
	 * series.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item (zero-based index).
	 * @return The start x-value.
	 */
	public double getStartX(int series, int item);

	/**
	 * Returns the ending X value for the specified series and item.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item within a series (zero-based index).
	 * @return the ending X value for the specified series and item.
	 */
	public Number getEndXValue(int series, int item);

	/**
	 * Returns the end x-value (as a double primitive) for an item within a series.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item (zero-based index).
	 * @return The end x-value.
	 */
	public double getEndX(int series, int item);

	/**
	 * Returns the starting Y value for the specified series and item.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item within a series (zero-based index).
	 * @return starting Y value for the specified series and item.
	 */
	public Number getStartYValue(int series, int item);

	/**
	 * Returns the start y-value (as a double primitive) for an item within a
	 * series.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item (zero-based index).
	 * @return The start y-value.
	 */
	public double getStartY(int series, int item);

	/**
	 * Returns the ending Y value for the specified series and item.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item within a series (zero-based index).
	 * @return the ending Y value for the specified series and item.
	 */
	public Number getEndYValue(int series, int item);

	/**
	 * Returns the end y-value (as a double primitive) for an item within a series.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item (zero-based index).
	 * @return The end y-value.
	 */
	public double getEndY(int series, int item);

}
