// ==============================================================================
//
// EdgeBorder.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: EdgeBorder.java,v 1.7.2.1 2012/12/13 12:51:12 klapperipk Exp $

package org.graffiti.plugin.tool;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.util.Iterator;

import javax.swing.border.AbstractBorder;

import org.graffiti.attributes.SortedCollectionAttribute;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.view.EdgeComponentInterface;
import org.graffiti.plugin.view.GraphElementShape;
import org.graffiti.plugins.views.defaults.StraightLineEdgeShape;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.7.2.1 $ Provides a border used to mark selected nodes.
 */
public class EdgeBorder extends AbstractBorder {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2536785806090003576L;
	
	/** Color used to paint border. */
	protected Color color;
	
	/** DOCUMENT ME! */
	protected boolean showBends;
	
	/** Size of bullets used to mark bends. */
	protected int bulletSize;
	
	/** DOCUMENT ME! */
	// private final AffineTransform IDENTITY = new AffineTransform();
	
	// ~ Constructors ===========================================================
	
	// /**
	// * Edge to mark.
	// */
	// protected Edge edge;
	// /**
	// * Collection of bends.
	// */
	// protected SortedCollectionAttribute bends;
	
	/**
	 * Constructor for EdgeBorder.
	 * 
	 * @param color
	 *           The color of the edge border.
	 * @param size
	 *           Must be between 1 and 15.
	 * @param showBends
	 *           True to show bends.
	 */
	
	// public EdgeBorder(Color color, int size, Edge edge) {
	public EdgeBorder(Color color, int size, boolean showBends) {
		super();
		this.color = color;
		this.bulletSize = size;
		this.showBends = showBends;
		
		// this.edge = edge;
		// this.bends = (SortedCollectionAttribute)edge.getAttribute
		// (GraphicAttributeConstants.GRAPHICS +
		// Attribute.SEPARATOR + GraphicAttributeConstants.BENDS);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the insets to the value of <code>width</code>.
	 * 
	 * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component,
	 *      java.awt.Insets)
	 */
	@Override
	public Insets getBorderInsets(Component c, Insets insets) {
		// insets.top = this.borderWidth;
		// insets.left = this.borderWidth;
		// insets.bottom = this.borderWidth;
		// insets.right = this.borderWidth;
		Rectangle bounds = c.getBounds();
		insets.top = bounds.height;
		insets.left = bounds.width;
		insets.bottom = bounds.height;
		insets.right = bounds.width;
		
		return insets;
	}
	
	/**
	 * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component)
	 */
	@Override
	public Insets getBorderInsets(Component c) {
		return getBorderInsets(c, new Insets(0, 0, 0, 0));
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return true.
	 * @see javax.swing.border.AbstractBorder#isBorderOpaque() Returns true.
	 */
	@Override
	public boolean isBorderOpaque() {
		return true;
	}
	
	/**
	 * Paints the border.
	 * 
	 * @param c
	 *           DOCUMENT ME!
	 * @param g
	 *           DOCUMENT ME!
	 * @param bx
	 *           DOCUMENT ME!
	 * @param by
	 *           DOCUMENT ME!
	 * @param width
	 *           DOCUMENT ME!
	 * @param height
	 *           DOCUMENT ME!
	 */
	@Override
	public void paintBorder(Component c, Graphics g, int bx, int by, int width, int height) {
		AffineTransform at = ((Graphics2D) c.getParent().getGraphics()).getTransform();
		Point pWH = new Point(bulletSize, bulletSize);
		try {
			at.inverseTransform(pWH, pWH);
		} catch (NoninvertibleTransformException e) {
			
			e.printStackTrace();
		}
		double factor = (double) pWH.x / (double) bulletSize;
		
		int bulletSize = (int) (factor * (double) this.bulletSize);
		if (bulletSize <= 1)
			bulletSize = 1;
		if (bulletSize >= 15)
			bulletSize = 15;
		
		double bulletSizeHalf = bulletSize / 2d;
		
		Graphics2D cg = (Graphics2D) g.create();
		cg.setComposite(AlphaComposite.SrcOver);
		
		cg.translate(bx, by);
		cg.setColor(this.color);
		
		GraphElementShape grShape = ((EdgeComponentInterface) c).getShape();
		if (grShape instanceof StraightLineEdgeShape) {
			PathIterator pi = grShape.getPathIterator(null);
			float[] coords = new float[2];
			
			pi.currentSegment(coords);
			// save X-coordinates to differ edge direction
			float srcCoordsX = coords[0];
			
			cg.fillRect((int) ((int) (coords[0]) - bulletSize * 0.4), (int) (coords[1] - bulletSize / 2), bulletSize,
					bulletSize);
			
			pi.next();
			pi.currentSegment(coords);
			
			if (srcCoordsX > coords[0]) // going left
				cg.fillRect((int) (coords[0] - factor), (int) (coords[1] - factor * 4), (int) (bulletSize * 0.5),
						(int) (bulletSize * 0.7));
			else // going right (or straight)
				cg.fillRect((int) (coords[0] - factor * 4), (int) (coords[1] - factor), (int) (bulletSize * 0.7),
						(int) (bulletSize * 0.5));
		} else {
			
			if (showBends) {
				Color lightColor = this.color.darker().darker();
				cg.setColor(lightColor);
				
				int bendBulletSize = (int) (bulletSize);
				
				if (bendBulletSize == 0) {
					bendBulletSize = 1;
				}
				
				SortedCollectionAttribute bends = (SortedCollectionAttribute) ((EdgeComponentInterface) c)
						.getGraphElement().getAttribute(GraphicAttributeConstants.BENDS_PATH);
				
				for (Iterator<?> it = bends.getCollection().values().iterator(); it.hasNext();) {
					CoordinateAttribute bendCoord = (CoordinateAttribute) it.next();
					
					// cg.setClip(0, 0, width, height);
					// cg.fillOval((int)bendCoord.getX()-(c.getBounds().x),
					// (int)bendCoord.getY()-(c.getBounds().y),
					// 2*bulletSize, 2*bulletSize);
					cg.fillOval((int) (bendCoord.getX() - c.getX() - (bendBulletSize / 2d)),
							(int) (bendCoord.getY() - c.getY() - (bendBulletSize / 2d)), bendBulletSize,
							bendBulletSize);
					
					// cg.fillOval((int) (bendCoord.getX() - c.getX() -
					// (bendBulletSize / 2d)),
					// (int) (bendCoord.getY() - c.getY() - (bendBulletSize / 2d)),
					// bendBulletSize, bendBulletSize);
				}
				
				cg.setColor(this.color);
			}
			
			// GeneralPath grPath = new GeneralPath(grShape);
			PathIterator pi = grShape.getPathIterator(null);
			double[] seg = new double[6];
			int type;
			double x = 0;
			double y = 0;
			
			try {
				type = pi.currentSegment(seg);
				x = seg[0];
				y = seg[1];
				cg.fillRect((int) (x), (int) (y), bulletSize, bulletSize);
				
				// cg.fillOval((int)x-2, (int)y-2, bulletSize, bulletSize);
				while (!pi.isDone()) {
					pi.next();
					type = pi.currentSegment(seg);
					
					switch (type) {
						case java.awt.geom.PathIterator.SEG_MOVETO:
							
							// x = seg[0];
							// y = seg[1];
							break;
						
						case java.awt.geom.PathIterator.SEG_LINETO:
							x = seg[0];
							y = seg[1];
							
							break;
						
						case java.awt.geom.PathIterator.SEG_QUADTO:
							x = seg[2];
							y = seg[3];
							
							break;
						
						case java.awt.geom.PathIterator.SEG_CUBICTO:
							x = seg[4];
							y = seg[5];
							
							break;
					}
					
					// cg.fillOval((int)x-2, (int)y-2, bulletSize, bulletSize);
					cg.fillRect((int) (x - bulletSizeHalf), (int) (y - bulletSizeHalf), bulletSize, bulletSize);
				}
			} catch (java.util.NoSuchElementException e) {
			} catch (ArrayIndexOutOfBoundsException e) {
				// why does this happen?!?
			}
		}
		cg.dispose();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------