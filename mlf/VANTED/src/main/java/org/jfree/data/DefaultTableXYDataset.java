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
 * DefaultTableXYDataset.java
 * --------------------------
 * (C) Copyright 2003, 2004, by Richard Atkinson and Contributors.
 * Original Author: Richard Atkinson;
 * Contributor(s): Jody Brownell;
 * David Gilbert (for Object Refinery Limited);
 * Andreas Schroeder;
 * $Id: DefaultTableXYDataset.java,v 1.2 2010/12/14 07:02:03 morla Exp $
 * Changes:
 * --------
 * 27-Jul-2003 : XYDataset that forces each series to have a value for every X-point
 * which is essential for stacked XY area charts (RA);
 * 18-Aug-2003 : Fixed event notification when removing and updating series (RA);
 * 22-Sep-2003 : Functionality moved from TableXYDataset to DefaultTableXYDataset (RA);
 * 23-Dec-2003 : Added patch for large datasets, submitted by Jody Brownell (DG);
 * 16-Feb-2004 : Added pruning methods (DG);
 * 31-Mar-2004 : Provisional implementation of IntervalXYDataset (AS);
 * 01-Apr-2004 : Sound implementation of IntervalXYDataset (AS);
 * 05-May-2004 : Now extends AbstractIntervalXYDataset (DG);
 */
package org.jfree.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.jfree.util.ObjectUtils;

/**
 * An {@link XYDataset} where every series shares the same x-values (required
 * for generating stacked area charts).
 * 
 * @author Richard Atkinson
 */
public class DefaultTableXYDataset extends AbstractIntervalXYDataset
		implements TableXYDataset, IntervalXYDataset, DomainInfo {

	/** Storage for the data. */
	private List data = null;

	/** Storage for the x values. */
	private HashSet xPoints = null;

	/** A flag that controls whether or not events are propogated. */
	private boolean propagateEvents = true;

	/** A flag that controls auto pruning. */
	private boolean autoPrune = false;

	/** The delegate used to control the interval width. */
	private IntervalXYDelegate intervalDelegate;

	/**
	 * Creates a new empty dataset.
	 */
	public DefaultTableXYDataset() {
		this(false);
	}

	/**
	 * Creates a new empty dataset.
	 * 
	 * @param autoPrune
	 *            a flag that controls whether or not x-values are removed whenever
	 *            the corresponding y-values are all <code>null</code>.
	 */
	public DefaultTableXYDataset(final boolean autoPrune) {
		this.autoPrune = autoPrune;
		this.data = new ArrayList();
		this.xPoints = new HashSet();
		this.intervalDelegate = new IntervalXYDelegate(this, false);
	}

	/**
	 * Constructs a dataset and populates it with a single time series.
	 * 
	 * @param series
	 *            the series.
	 * @deprecated Use regular constructor then add series.
	 */
	public DefaultTableXYDataset(final XYSeries series) {

		this.data = new ArrayList();
		this.xPoints = new HashSet();
		if (series != null) {
			if (series.getAllowDuplicateXValues()) {
				throw new IllegalArgumentException("Cannot accept a series that allows duplicate values. "
						+ "Use XYSeries(seriesName, false) constructor.");
			}
			updateXPoints(series);
			this.data.add(series);
			series.addChangeListener(this);
		}
		this.autoPrune = false;

	}

	/**
	 * Returns the flag that controls whether or not x-values are removed from the
	 * dataset when the corresponding y-values are all <code>null</code>.
	 * 
	 * @return a boolean.
	 */
	public boolean isAutoPrune() {
		return this.autoPrune;
	}

	/**
	 * Adds a series to the collection and sends a {@link DatasetChangeEvent} to all
	 * registered listeners. The series should be configured to NOT allow duplicate
	 * x-values.
	 * 
	 * @param series
	 *            the series (<code>null</code> not permitted).
	 */
	public void addSeries(final XYSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Null 'series' argument.");
		}
		if (series.getAllowDuplicateXValues()) {
			throw new IllegalArgumentException("Cannot accept XY Series that allow duplicate "
					+ "values.  Use XYSeries(seriesName, false) constructor.");
		}

		updateXPoints(series);
		this.data.add(series);
		series.addChangeListener(this);
		fireDatasetChanged();
	}

	/**
	 * Adds any unique x-values from 'series' to the dataset, and also adds any
	 * x-values that are in the dataset but not in 'series' to the series.
	 * 
	 * @param series
	 *            the series (<code>null</code> not permitted).
	 */
	private void updateXPoints(final XYSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Null 'series' not permitted.");
		}
		final HashSet seriesXPoints = new HashSet();
		final boolean savedState = this.propagateEvents;
		this.propagateEvents = false;
		for (int itemNo = 0; itemNo < series.getItemCount(); itemNo++) {
			final Number xValue = series.getXValue(itemNo);
			seriesXPoints.add(xValue);
			if (!this.xPoints.contains(xValue)) {
				this.xPoints.add(xValue);
				for (int seriesNo = 0; seriesNo < this.data.size(); seriesNo++) {
					final XYSeries dataSeries = (XYSeries) this.data.get(seriesNo);
					if (!dataSeries.equals(series)) {
						dataSeries.add(xValue, null);
					}
				}
			}
		}
		final Iterator iterator = this.xPoints.iterator();
		while (iterator.hasNext()) {
			final Number xPoint = (Number) iterator.next();
			if (!seriesXPoints.contains(xPoint)) {
				series.add(xPoint, null);
			}
		}
		this.propagateEvents = savedState;
	}

	/**
	 * Updates the x-values for all the series in the dataset.
	 */
	public void updateXPoints() {
		this.propagateEvents = false;
		for (int s = 0; s < this.data.size(); s++) {
			updateXPoints((XYSeries) this.data.get(s));
		}
		if (this.autoPrune) {
			prune();
		}
		this.propagateEvents = true;
	}

	/**
	 * Returns the number of series in the collection.
	 * 
	 * @return the number of series in the collection.
	 */
	public int getSeriesCount() {
		return this.data.size();
	}

	/**
	 * Returns the number of x values in the dataset.
	 * 
	 * @return the number of x values in the dataset.
	 */
	public int getItemCount() {
		if (this.xPoints == null) {
			return 0;
		} else {
			return this.xPoints.size();
		}
	}

	/**
	 * Returns a series.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @return the series (never <code>null</code>).
	 */
	public XYSeries getSeries(final int series) {
		if ((series < 0) || (series > getSeriesCount())) {
			throw new IllegalArgumentException("XYSeriesCollection.getSeries(...): index outside valid range.");
		}

		return (XYSeries) this.data.get(series);
	}

	/**
	 * Returns the name of a series.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @return the name of a series.
	 */
	public String getSeriesName(final int series) {
		// check arguments...delegated
		return getSeries(series).getName();
	}

	/**
	 * Returns the number of items in the specified series.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @return the number of items in the specified series.
	 */
	public int getItemCount(final int series) {
		// check arguments...delegated
		return getSeries(series).getItemCount();
	}

	/**
	 * Returns the x-value for the specified series and item.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item (zero-based index).
	 * @return the x-value for the specified series and item.
	 */
	public Number getXValue(final int series, final int item) {
		final XYSeries s = (XYSeries) this.data.get(series);
		final XYDataItem dataItem = s.getDataItem(item);
		return dataItem.getX();
	}

	/**
	 * Returns the starting X value for the specified series and item.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item (zero-based index).
	 * @return The starting X value.
	 */
	public Number getStartXValue(final int series, final int item) {
		return intervalDelegate.getStartXValue(series, item);
	}

	/**
	 * Returns the ending X value for the specified series and item.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item (zero-based index).
	 * @return The ending X value.
	 */
	public Number getEndXValue(final int series, final int item) {
		return intervalDelegate.getEndXValue(series, item);
	}

	/**
	 * Returns the y-value for the specified series and item.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param index
	 *            the index of the item of interest (zero-based).
	 * @return the y-value for the specified series and item (possibly
	 *         <code>null</code>).
	 */
	public Number getYValue(final int series, final int index) {
		final XYSeries ts = (XYSeries) this.data.get(series);
		final XYDataItem dataItem = ts.getDataItem(index);
		return dataItem.getY();
	}

	/**
	 * Returns the starting Y value for the specified series and item.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item (zero-based index).
	 * @return The starting Y value.
	 */
	public Number getStartYValue(final int series, final int item) {
		return getYValue(series, item);
	}

	/**
	 * Returns the ending Y value for the specified series and item.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item (zero-based index).
	 * @return The ending Y value.
	 */
	public Number getEndYValue(final int series, final int item) {
		return getYValue(series, item);
	}

	/**
	 * Removes all the series from the collection and sends a
	 * {@link DatasetChangeEvent} to all registered listeners.
	 */
	public void removeAllSeries() {

		// Unregister the collection as a change listener to each series in the
		// collection.
		for (int i = 0; i < this.data.size(); i++) {
			final XYSeries series = (XYSeries) this.data.get(i);
			series.removeChangeListener(this);
		}

		// Remove all the series from the collection and notify listeners.
		this.data.clear();
		this.xPoints.clear();
		this.intervalDelegate.seriesRemoved();
		fireDatasetChanged();
	}

	/**
	 * Removes a series from the collection and sends a {@link DatasetChangeEvent}
	 * to all registered listeners.
	 * 
	 * @param series
	 *            the series (<code>null</code> not permitted).
	 */
	public void removeSeries(final XYSeries series) {

		// check arguments...
		if (series == null) {
			throw new IllegalArgumentException("Null 'series' argument.");
		}

		// remove the series...
		if (this.data.contains(series)) {
			series.removeChangeListener(this);
			this.data.remove(series);
			if (this.data.size() == 0) {
				this.xPoints.clear();
			}
			this.intervalDelegate.seriesRemoved();
			fireDatasetChanged();
		}

	}

	/**
	 * Removes a series from the collection and sends a {@link DatasetChangeEvent}
	 * to all registered listeners.
	 * 
	 * @param series
	 *            the series (zero based index).
	 */
	public void removeSeries(final int series) {

		// check arguments...
		if ((series < 0) || (series > getSeriesCount())) {
			throw new IllegalArgumentException("XYSeriesCollection.removeSeries(...): index outside valid range.");
		}

		// fetch the series, remove the change listener, then remove the series.
		final XYSeries s = (XYSeries) this.data.get(series);
		s.removeChangeListener(this);
		this.data.remove(series);
		if (this.data.size() == 0) {
			this.xPoints.clear();
		} else if (this.autoPrune) {
			prune();
		}
		this.intervalDelegate.seriesRemoved();
		fireDatasetChanged();

	}

	/**
	 * Removes the items from all series for a given x value.
	 * 
	 * @param x
	 *            the x-value.
	 */
	public void removeAllValuesForX(final Number x) {
		if (x == null) {
			throw new IllegalArgumentException("Null 'x' argument.");
		}
		final boolean savedState = this.propagateEvents;
		this.propagateEvents = false;
		for (int s = 0; s < this.data.size(); s++) {
			final XYSeries series = (XYSeries) this.data.get(s);
			series.remove(x);
		}
		this.propagateEvents = savedState;
		this.xPoints.remove(x);
		this.intervalDelegate.seriesRemoved();
		fireDatasetChanged();
	}

	/**
	 * Returns <code>true</code> if all the y-values for the specified x-value are
	 * <code>null</code> and false otherwise.
	 * 
	 * @param x
	 *            the x-value.
	 * @return a boolean.
	 */
	protected boolean canPrune(final Number x) {
		for (int s = 0; s < this.data.size(); s++) {
			final XYSeries series = (XYSeries) this.data.get(s);
			if (series.getYValue(series.indexOf(x)) != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Removes all x-values for which all the y-values are <code>null</code>.
	 */
	public void prune() {
		final HashSet hs = (HashSet) this.xPoints.clone();
		final Iterator iterator = hs.iterator();
		while (iterator.hasNext()) {
			final Number x = (Number) iterator.next();
			if (canPrune(x)) {
				removeAllValuesForX(x);
			}
		}
	}

	/**
	 * This method receives notification when a series belonging to the dataset
	 * changes. It responds by updating the x-points for the entire dataset and
	 * sending a {@link DatasetChangeEvent} to all registered listeners.
	 * 
	 * @param event
	 *            information about the change.
	 */
	public void seriesChanged(final SeriesChangeEvent event) {
		if (this.propagateEvents) {
			updateXPoints();
			fireDatasetChanged();
		}
	}

	/**
	 * Tests this collection for equality with an arbitrary object.
	 * 
	 * @param obj
	 *            the object (<code>null</code> permitted).
	 * @return A boolean.
	 */
	public boolean equals(final Object obj) {

		/*
		 * I wonder if these implementations of equals and hashCode are sound... (AS)
		 */

		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj instanceof DefaultTableXYDataset) {
			final DefaultTableXYDataset c = (DefaultTableXYDataset) obj;
			return ObjectUtils.equal(this.data, c.data);
		}

		return false;

	}

	/**
	 * Returns a hash code.
	 * 
	 * @return a hash code.
	 */
	public int hashCode() {
		int result;
		result = (this.data != null ? this.data.hashCode() : 0);
		result = 29 * result + (this.xPoints != null ? this.xPoints.hashCode() : 0);
		result = 29 * result + (this.propagateEvents ? 1 : 0);
		result = 29 * result + (this.autoPrune ? 1 : 0);
		return result;
	}

	/**
	 * @return the domain range
	 */
	public Range getDomainRange() {
		return intervalDelegate.getDomainRange();
	}

	/**
	 * @return the maximum domain value.
	 */
	public Number getMaximumDomainValue() {
		return intervalDelegate.getMaximumDomainValue();
	}

	/**
	 * @return the minimum domain value.
	 */
	public Number getMinimumDomainValue() {
		return intervalDelegate.getMinimumDomainValue();
	}

	/**
	 * Returns the interval position factor.
	 * 
	 * @return the interval position factor.
	 */
	public double getIntervalPositionFactor() {
		return intervalDelegate.getIntervalPositionFactor();
	}

	/**
	 * Sets the interval position factor. Must be between 0.0 and 1.0 inclusive. If
	 * the factor is 0.5, the gap is in the middle of the x values. If it is lesser
	 * than 0.5, the gap is farther to the left and if greater than 0.5 it gets
	 * farther to the right.
	 * 
	 * @param d
	 *            the new interval position factor.
	 */
	public void setIntervalPositionFactor(final double d) {
		intervalDelegate.setIntervalPositionFactor(d);
		fireDatasetChanged();
	}

	/**
	 * returns the full interval width.
	 * 
	 * @return the interval width to use.
	 */
	public double getIntervalWidth() {
		return intervalDelegate.getIntervalWidth();
	}

	/**
	 * Sets the interval width manually.
	 * 
	 * @param d
	 *            the new interval width.
	 */
	public void setIntervalWidth(final double d) {
		intervalDelegate.setIntervalWidth(d);
		fireDatasetChanged();
	}

	/**
	 * Returns wether the interval width is automatically calculated or not.
	 * 
	 * @return wether the width is automatically calcualted or not.
	 */
	public boolean isAutoWidth() {
		return intervalDelegate.isAutoWidth();
	}

	/**
	 * Sets the flag that indicates wether the interval width is automatically
	 * calculated or not.
	 * 
	 * @param b
	 *            a boolean.
	 */
	public void setAutoWidth(final boolean b) {
		intervalDelegate.setAutoWidth(b);
		fireDatasetChanged();
	}
}
