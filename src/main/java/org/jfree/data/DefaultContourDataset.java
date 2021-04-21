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
 * --------------------------
 * DefaultContourDataset.java
 * --------------------------
 * (C) Copyright 2002-2004, by David M. O'Donnell and Contributors.
 * Original Author: David M. O'Donnell;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: DefaultContourDataset.java,v 1.2 2010/12/14 07:02:03 morla Exp $
 * Changes (from 23-Jan-2003)
 * --------------------------
 * 23-Jan-2003 : Added standard header (DG);
 * 20-May-2003 : removed member vars numX and numY, which were never used (TM);
 * 06-May-2004 : Now extends AbstractXYZDataset (DG);
 */

package org.jfree.data;

import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

/**
 * A convenience class that provides a default implementation of the
 * {@link ContourDataset} interface.
 * 
 * @author David M. O'Donnell
 */
public class DefaultContourDataset extends AbstractXYZDataset implements ContourDataset {
	
	/** The series name (this dataset supports only one series). */
	protected String seriesName = null;
	
	/** Storage for the x values. */
	protected Number[] xValues = null;
	
	/** Storage for the y values. */
	protected Number[] yValues = null;
	
	/** Storage for the z values. */
	protected Number[] zValues = null;
	
	/** The index for the start of each column in the data. */
	protected int[] xIndex = null;
	
	/** Flags that track whether x, y and z are dates. */
	boolean[] dateAxis = new boolean[3];
	
	/**
	 * Creates a new dataset, initially empty.
	 */
	public DefaultContourDataset() {
		super();
	}
	
	/**
	 * Constructs a new dataset with the given data.
	 * 
	 * @param seriesName
	 *           the series name.
	 * @param xData
	 *           the x values.
	 * @param yData
	 *           the y values.
	 * @param zData
	 *           the z values.
	 */
	public DefaultContourDataset(final String seriesName, final Object[] xData, final Object[] yData,
			final Object[] zData) {
		
		this.seriesName = seriesName;
		initialize(xData, yData, zData);
	}
	
	/**
	 * Initialises the dataset.
	 * 
	 * @param xData
	 *           the x values.
	 * @param yData
	 *           the y values.
	 * @param zData
	 *           the z values.
	 */
	public void initialize(final Object[] xData, final Object[] yData, final Object[] zData) {
		
		this.xValues = new Double[xData.length];
		this.yValues = new Double[yData.length];
		this.zValues = new Double[zData.length];
		
		// We organise the data with the following assumption:
		// 1) the data are sorted by x then y
		// 2) that the data will be represented by a rectangle formed by
		// using x[i+1], x, y[j+1], and y.
		// 3) we march along the y-axis at the same value of x until a new value x is
		// found at which point we will flag the index where x[i+1]<>x[i]
		
		final Vector tmpVector = new Vector(); // create a temporary vector
		double x = 1.123452e31; // set x to some arbitary value (used below)
		for (int k = 0; k < this.xValues.length; k++) {
			if (xData[k] != null) {
				final Number xNumber;
				if (xData[k] instanceof Number) {
					xNumber = (Number) xData[k];
				} else if (xData[k] instanceof Date) {
					this.dateAxis[0] = true;
					final Date xDate = (Date) xData[k];
					xNumber = Long.valueOf(xDate.getTime()); // store data as Long
				} else {
					xNumber = Integer.valueOf(0);
				}
				this.xValues[k] = Double.valueOf(xNumber.doubleValue()); // store Number as Double
				
				// check if starting new column
				if (x != this.xValues[k].doubleValue()) {
					tmpVector.add(Integer.valueOf(k)); // store index where new column starts
					x = this.xValues[k].doubleValue(); // set x to most recent value
				}
			}
		}
		
		final Object[] inttmp = tmpVector.toArray();
		this.xIndex = new int[inttmp.length]; // create array xIndex to hold new column indices
		
		for (int i = 0; i < inttmp.length; i++) {
			this.xIndex[i] = ((Integer) inttmp[i]).intValue();
		}
		for (int k = 0; k < this.yValues.length; k++) { // store y and z axes as Doubles
			this.yValues[k] = (Double) yData[k];
			if (zData[k] != null) {
				this.zValues[k] = (Double) zData[k];
			}
		}
	}
	
	/**
	 * Creates an object array from an array of doubles.
	 * 
	 * @param data
	 *           the data.
	 * @return An array of <code>Double</code> objects.
	 */
	public static Object[][] formObjectArray(final double[][] data) {
		final Object[][] object = new Double[data.length][data[0].length];
		
		for (int i = 0; i < object.length; i++) {
			for (int j = 0; j < object[i].length; j++) {
				object[i][j] = Double.valueOf(data[i][j]);
			}
		}
		return object;
	}
	
	/**
	 * Creates an object array from an array of doubles.
	 * 
	 * @param data
	 *           the data.
	 * @return An array of <code>Double</code> objects.
	 */
	public static Object[] formObjectArray(final double[] data) {
		
		final Object[] object = new Double[data.length];
		for (int i = 0; i < object.length; i++) {
			object[i] = Double.valueOf(data[i]);
		}
		return object;
	}
	
	/**
	 * Returns the number of items in the specified series.
	 * <P>
	 * Method provided to satisfy the {@link XYDataset} interface implementation.
	 * 
	 * @param series
	 *           must be zero, as this dataset only supports one series.
	 * @return the item count.
	 */
	public int getItemCount(final int series) {
		if (series > 0) {
			System.out.println("Only one series for contour");
		}
		return this.zValues.length;
	}
	
	/**
	 * Returns the maximum z-value.
	 * 
	 * @return The maximum z-value.
	 */
	public double getMaxZValue() {
		double zMax = -1.e20;
		for (int k = 0; k < this.zValues.length; k++) {
			if (this.zValues[k] != null) {
				zMax = Math.max(zMax, this.zValues[k].doubleValue());
			}
		}
		return zMax;
	}
	
	/**
	 * Returns the minimum z-value.
	 * 
	 * @return The minimum z-value.
	 */
	public double getMinZValue() {
		
		double zMin = 1.e20;
		for (int k = 0; k < this.zValues.length; k++) {
			if (this.zValues[k] != null) {
				zMin = Math.min(zMin, this.zValues[k].doubleValue());
			}
		}
		return zMin;
	}
	
	/**
	 * Returns the maximum z-value within visible region of plot.
	 * 
	 * @param x
	 *           the x range.
	 * @param y
	 *           the y range.
	 * @return The z range.
	 */
	public Range getZValueRange(final Range x, final Range y) {
		
		final double minX = x.getLowerBound();
		final double minY = y.getLowerBound();
		final double maxX = x.getUpperBound();
		final double maxY = y.getUpperBound();
		
		double zMin = 1.e20;
		double zMax = -1.e20;
		for (int k = 0; k < this.zValues.length; k++) {
			if (this.xValues[k].doubleValue() >= minX && this.xValues[k].doubleValue() <= maxX
					&& this.yValues[k].doubleValue() >= minY && this.yValues[k].doubleValue() <= maxY) {
				if (this.zValues[k] != null) {
					zMin = Math.min(zMin, this.zValues[k].doubleValue());
					zMax = Math.max(zMax, this.zValues[k].doubleValue());
				}
			}
		}
		
		return new Range(zMin, zMax);
	}
	
	/**
	 * Returns the minimum z-value.
	 * 
	 * @param minX
	 *           the minimum x value.
	 * @param minY
	 *           the minimum y value.
	 * @param maxX
	 *           the maximum x value.
	 * @param maxY
	 *           the maximum y value.
	 * @return the minimum z-value.
	 */
	public double getMinZValue(final double minX, final double minY, final double maxX, final double maxY) {
		
		double zMin = 1.e20;
		for (int k = 0; k < this.zValues.length; k++) {
			if (this.zValues[k] != null) {
				zMin = Math.min(zMin, this.zValues[k].doubleValue());
			}
		}
		return zMin;
		
	}
	
	/**
	 * Returns the number of series.
	 * <P>
	 * Required by XYDataset interface (this will always return 1)
	 * 
	 * @return 1.
	 */
	public int getSeriesCount() {
		return 1;
	}
	
	/**
	 * Returns the name of the specified series. Method provided to satisfy the
	 * XYDataset interface implementation
	 * 
	 * @param series
	 *           must be zero.
	 * @return the series name.
	 */
	public String getSeriesName(final int series) {
		if (series > 0) {
			System.out.println("Only one series for contour");
		}
		return this.seriesName;
	}
	
	/**
	 * Returns the index of the xvalues.
	 * 
	 * @return The x values.
	 */
	public int[] getXIndices() {
		return this.xIndex;
	}
	
	/**
	 * Returns the x values.
	 * 
	 * @return The x values.
	 */
	public Number[] getXValues() {
		return this.xValues;
	}
	
	/**
	 * Returns the x value for the specified series and index (zero-based indices).
	 * Required by the XYDataset
	 * 
	 * @param series
	 *           must be zero;
	 * @param item
	 *           the item index (zero-based).
	 * @return The x value.
	 */
	public Number getXValue(final int series, final int item) {
		if (series > 0) {
			System.out.println("Only one series for contour");
		}
		return this.xValues[item];
	}
	
	/**
	 * Returns an x value.
	 * 
	 * @param item
	 *           the item index (zero-based).
	 * @return The X value.
	 */
	public Number getXValue(final int item) {
		return this.xValues[item];
	}
	
	/**
	 * Returns a Number array containing all y values.
	 * 
	 * @return The Y values.
	 */
	public Number[] getYValues() {
		return this.yValues;
	}
	
	/**
	 * Returns the y value for the specified series and index (zero-based indices).
	 * Required by the XYDataset
	 * 
	 * @param series
	 *           the series index (must be zero for this dataset).
	 * @param item
	 *           the item index (zero-based).
	 * @return The Y value.
	 */
	public Number getYValue(final int series, final int item) {
		if (series > 0) {
			System.out.println("Only one series for contour");
		}
		return this.yValues[item];
	}
	
	/**
	 * Returns a Number array containing all z values.
	 * 
	 * @return The Z values.
	 */
	public Number[] getZValues() {
		return this.zValues;
	}
	
	/**
	 * Returns the z value for the specified series and index (zero-based indices).
	 * Required by the XYDataset
	 * 
	 * @param series
	 *           the series index (must be zero for this dataset).
	 * @param item
	 *           the item index (zero-based).
	 * @return The Z value.
	 */
	public Number getZValue(final int series, final int item) {
		if (series > 0) {
			System.out.println("Only one series for contour");
		}
		return this.zValues[item];
	}
	
	/**
	 * Returns an int array contain the index into the x values.
	 * 
	 * @return The X values.
	 */
	public int[] indexX() {
		final int[] index = new int[this.xValues.length];
		for (int k = 0; k < index.length; k++) {
			index[k] = indexX(k);
		}
		return index;
	}
	
	/**
	 * Given index k, returns the column index containing k.
	 * 
	 * @param k
	 *           index of interest.
	 * @return The column index.
	 */
	public int indexX(final int k) {
		final int i = Arrays.binarySearch(this.xIndex, k);
		if (i >= 0) {
			return i;
		} else {
			return -1 * i - 2;
		}
	}
	
	/**
	 * Given index k, return the row index containing k.
	 * 
	 * @param k
	 *           index of interest.
	 * @return The row index.
	 */
	public int indexY(final int k) { // this may be obsolete (not used anywhere)
		return (k / this.xValues.length);
	}
	
	/**
	 * Given column and row indices, returns the k index.
	 * 
	 * @param i
	 *           index of along x-axis.
	 * @param j
	 *           index of along y-axis.
	 * @return The Z index.
	 */
	public int indexZ(final int i, final int j) {
		return this.xValues.length * j + i;
	}
	
	/**
	 * Returns true if axis are dates.
	 * 
	 * @param axisNumber
	 *           The axis where 0-x, 1-y, and 2-z.
	 * @return A boolean.
	 */
	public boolean isDateAxis(final int axisNumber) {
		if (axisNumber < 0 || axisNumber > 2) {
			return false; // bad axisNumber
		}
		return this.dateAxis[axisNumber];
	}
	
	/**
	 * Sets the names of the series in the data source.
	 * 
	 * @param seriesNames
	 *           The names of the series in the data source.
	 */
	public void setSeriesNames(final String[] seriesNames) {
		if (seriesNames.length > 1) {
			System.out.println("Contours only support one series");
		}
		this.seriesName = seriesNames[0];
		fireDatasetChanged();
	}
	
}
