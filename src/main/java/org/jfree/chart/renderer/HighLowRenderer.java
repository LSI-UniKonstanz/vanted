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
 * --------------------
 * HighLowRenderer.java
 * --------------------
 * (C) Copyright 2001-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Richard Atkinson;
 * Christian W. Zuckschwerdt;
 * $Id: HighLowRenderer.java,v 1.2 2010/12/14 07:02:04 morla Exp $
 * Changes
 * -------
 * 13-Dec-2001 : Version 1 (DG);
 * 23-Jan-2002 : Added DrawInfo parameter to drawItem(...) method (DG);
 * 28-Mar-2002 : Added a property change listener mechanism so that renderers no longer need to be
 * immutable (DG);
 * 09-Apr-2002 : Removed translatedRangeZero from the drawItem(...) method, and changed the return
 * type of the drawItem method to void, reflecting a change in the XYItemRenderer
 * interface. Added tooltip code to drawItem(...) method (DG);
 * 05-Aug-2002 : Small modification to drawItem method to support URLs for HTML image maps (RA);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 01-May-2003 : Modified drawItem(...) method signature (DG);
 * 30-Jul-2003 : Modified entity constructor (CZ);
 * 31-Jul-2003 : Deprecated constructor (DG);
 * 20-Aug-2003 : Implemented Cloneable and PublicCloneable (DG);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 29-Jan-2004 : Fixed bug (882392) when rendering with PlotOrientation.HORIZONTAL (DG);
 * 25-Feb-2004 : Replaced CrosshairInfo with CrosshairState. Renamed XYToolTipGenerator
 * --> XYItemLabelGenerator (DG);
 */

package org.jfree.chart.renderer;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.HighLowDataset;
import org.jfree.data.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.PublicCloneable;

/**
 * A renderer that draws high/low/open/close markers on an {@link XYPlot} (requires
 * a {@link HighLowDataset}).
 * <P>
 * This renderer does not include code to calculate the crosshair point for the plot.
 */
public class HighLowRenderer extends AbstractXYItemRenderer
										implements XYItemRenderer,
														Cloneable,
														PublicCloneable,
														Serializable {

	/** A flag that controls whether the open ticks are drawn. */
	private boolean drawOpenTicks;

	/** A flag that controls whether the close ticks are drawn. */
	private boolean drawCloseTicks;

	/**
	 * The default constructor.
	 */
	public HighLowRenderer() {
		super();
		this.drawOpenTicks = true;
		this.drawCloseTicks = true;
	}

	/**
	 * Returns the flag that controls whether open ticks are drawn.
	 * 
	 * @return A boolean.
	 */
	public boolean getDrawOpenTicks() {
		return this.drawOpenTicks;
	}

	/**
	 * Sets the flag that controls whether open ticks are drawn, and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param draw
	 *           the flag.
	 */
	public void setDrawOpenTicks(boolean draw) {
		this.drawOpenTicks = draw;
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Returns the flag that controls whether close ticks are drawn.
	 * 
	 * @return A boolean.
	 */
	public boolean getDrawCloseTicks() {
		return this.drawCloseTicks;
	}

	/**
	 * Sets the flag that controls whether close ticks are drawn, and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param draw
	 *           the flag.
	 */
	public void setDrawCloseTicks(boolean draw) {
		this.drawCloseTicks = draw;
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Draws the visual representation of a single data item.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param state
	 *           the renderer state.
	 * @param dataArea
	 *           the area within which the plot is being drawn.
	 * @param info
	 *           collects information about the drawing.
	 * @param plot
	 *           the plot (can be used to obtain standard color information etc).
	 * @param domainAxis
	 *           the domain axis.
	 * @param rangeAxis
	 *           the range axis.
	 * @param dataset
	 *           the dataset.
	 * @param series
	 *           the series index (zero-based).
	 * @param item
	 *           the item index (zero-based).
	 * @param crosshairState
	 *           crosshair information for the plot (<code>null</code> permitted).
	 * @param pass
	 *           the pass index.
	 */
	public void drawItem(Graphics2D g2,
									XYItemRendererState state,
									Rectangle2D dataArea,
									PlotRenderingInfo info,
									XYPlot plot,
									ValueAxis domainAxis,
									ValueAxis rangeAxis,
									XYDataset dataset,
									int series,
									int item,
									CrosshairState crosshairState,
									int pass) {

		// first make sure we have a valid x value...
		Number x = dataset.getXValue(series, item);
		if (x == null) {
			return; // if x is null, we can't do anything
		}
		double xdouble = x.doubleValue();
		if (!domainAxis.getRange().contains(xdouble)) {
			return; // the x value is not within the axis range
		}
		double xx = domainAxis.valueToJava2D(xdouble, dataArea, plot.getDomainAxisEdge());

		// setup for collecting optional entity info...
		Shape entityArea = null;
		EntityCollection entities = null;
		if (info != null) {
			entities = info.getOwner().getEntityCollection();
		}

		PlotOrientation orientation = plot.getOrientation();
		RectangleEdge location = plot.getRangeAxisEdge();

		Paint p = getItemPaint(series, item);
		Stroke s = getItemStroke(series, item);
		g2.setPaint(p);
		g2.setStroke(s);

		if (dataset instanceof HighLowDataset) {
			HighLowDataset hld = (HighLowDataset) dataset;

			Number yHigh = hld.getHighValue(series, item);
			Number yLow = hld.getLowValue(series, item);
			if (yHigh != null && yLow != null) {
				double yyHigh = rangeAxis.valueToJava2D(yHigh.doubleValue(), dataArea, location);
				double yyLow = rangeAxis.valueToJava2D(yLow.doubleValue(), dataArea, location);
				if (orientation == PlotOrientation.HORIZONTAL) {
					g2.draw(new Line2D.Double(yyLow, xx, yyHigh, xx));
					entityArea = new Rectangle2D.Double(
										Math.min(yyLow, yyHigh), xx - 1.0, Math.abs(yyHigh - yyLow), 2.0
										);
				} else
					if (orientation == PlotOrientation.VERTICAL) {
						g2.draw(new Line2D.Double(xx, yyLow, xx, yyHigh));
						entityArea = new Rectangle2D.Double(
											xx - 1.0, Math.min(yyLow, yyHigh), 2.0, Math.abs(yyHigh - yyLow)
											);
					}
			}

			double delta = 2.0;
			if (domainAxis.isInverted()) {
				delta = -delta;
			}
			if (getDrawOpenTicks()) {
				Number yOpen = hld.getOpenValue(series, item);
				if (yOpen != null) {
					double yyOpen = rangeAxis.valueToJava2D(
										yOpen.doubleValue(), dataArea, location
										);
					if (orientation == PlotOrientation.HORIZONTAL) {
						g2.draw(new Line2D.Double(yyOpen, xx + delta, yyOpen, xx));
					} else
						if (orientation == PlotOrientation.VERTICAL) {
							g2.draw(new Line2D.Double(xx - delta, yyOpen, xx, yyOpen));
						}
				}
			}

			if (getDrawCloseTicks()) {
				Number yClose = hld.getCloseValue(series, item);
				if (yClose != null) {
					double yyClose = rangeAxis.valueToJava2D(
										yClose.doubleValue(), dataArea, location
										);
					if (orientation == PlotOrientation.HORIZONTAL) {
						g2.draw(new Line2D.Double(yyClose, xx, yyClose, xx - delta));
					} else
						if (orientation == PlotOrientation.VERTICAL) {
							g2.draw(new Line2D.Double(xx, yyClose, xx + delta, yyClose));
						}
				}
			}

		} else {
			// not a HighLowDataset, so just draw a line connecting this point with the previous
			// point...
			if (item > 0) {
				Number x0 = dataset.getXValue(series, item - 1);
				Number y0 = dataset.getYValue(series, item - 1);
				Number y = dataset.getYValue(series, item);
				if (x0 == null || y0 == null || y == null) {
					return;
				}
				double xx0 = domainAxis.valueToJava2D(
									x0.doubleValue(), dataArea, plot.getDomainAxisEdge()
									);
				double yy0 = rangeAxis.valueToJava2D(y0.doubleValue(), dataArea, location);
				double yy = rangeAxis.valueToJava2D(y.doubleValue(), dataArea, location);
				if (orientation == PlotOrientation.HORIZONTAL) {
					g2.draw(new Line2D.Double(yy0, xx0, yy, xx));
				} else
					if (orientation == PlotOrientation.VERTICAL) {
						g2.draw(new Line2D.Double(xx0, yy0, xx, yy));
					}
			}
		}

		// add an entity for the item...
		if (entities != null) {
			String tip = null;
			XYToolTipGenerator generator = getToolTipGenerator(series, item);
			if (generator != null) {
				tip = generator.generateToolTip(dataset, series, item);
			}
			String url = null;
			if (getURLGenerator() != null) {
				url = getURLGenerator().generateURL(dataset, series, item);
			}
			XYItemEntity entity = new XYItemEntity(entityArea, dataset, series, item, tip, url);
			entities.addEntity(entity);
		}

	}

	/**
	 * Returns a clone of the renderer.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            if the renderer cannot be cloned.
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
