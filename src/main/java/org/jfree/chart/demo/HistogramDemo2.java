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
 * -------------------
 * HistogramDemo2.java
 * -------------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: HistogramDemo2.java,v 1.2 2010/12/14 07:02:01 morla Exp $
 * Changes
 * -------
 * 01-Mar-2004 : Version 1 (DG);
 */

package org.jfree.chart.demo;

import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.IntervalXYDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 * A demo of the {@link HistogramDataset} class.
 */
public class HistogramDemo2 extends ApplicationFrame {
	
	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public HistogramDemo2(final String title) {
		super(title);
		final IntervalXYDataset dataset = createDataset();
		final JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
	}
	
	/**
	 * Creates a sample {@link HistogramDataset}.
	 * 
	 * @return the dataset.
	 */
	private IntervalXYDataset createDataset() {
		final HistogramDataset dataset = new HistogramDataset();
		final double[] values = { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0 };
		dataset.addSeries("H1", values, 10, 0.0, 10.0);
		return dataset;
	}
	
	// ****************************************************************************
	// * JFREECHART DEVELOPER GUIDE *
	// * The JFreeChart Developer Guide, written by David Gilbert, is available *
	// * to purchase from Object Refinery Limited: *
	// * *
	// * http://www.object-refinery.com/jfreechart/guide.html *
	// * *
	// * Sales are used to provide funding for the JFreeChart project - please *
	// * support us so that we can continue developing free software. *
	// ****************************************************************************
	
	/**
	 * Creates a chart.
	 * 
	 * @param dataset
	 *           a dataset.
	 * @return The chart.
	 */
	private JFreeChart createChart(final IntervalXYDataset dataset) {
		final JFreeChart chart = ChartFactory.createHistogram("Histogram Demo", null, null, dataset,
				PlotOrientation.VERTICAL, true, false, false);
		chart.getXYPlot().setForegroundAlpha(0.75f);
		return chart;
	}
	
	/**
	 * The starting point for the demo.
	 * 
	 * @param args
	 *           ignored.
	 * @throws IOException
	 *            if there is a problem saving the file.
	 */
	public static void main(final String[] args) throws IOException {
		
		final HistogramDemo2 demo = new HistogramDemo2("Histogram Demo 2");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
		
	}
	
}
