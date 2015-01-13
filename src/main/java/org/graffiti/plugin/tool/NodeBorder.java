// ==============================================================================
//
// NodeBorder.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: NodeBorder.java,v 1.6.4.1 2012/12/13 12:51:12 klapperipk Exp $

package org.graffiti.plugin.tool;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import javax.swing.border.AbstractBorder;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.6.4.1 $ Provides a border used to mark selected nodes.
 */
public class NodeBorder
					extends AbstractBorder {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** DOCUMENT ME! */
	// private final AffineTransform IDENTITY = new AffineTransform();
	
	/** Color used to paint border. */
	private Color color;
	
	/** Width of the border. */
	private int borderWidth;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for NodeBorder.
	 * 
	 * @param color
	 *           DOCUMENT ME!
	 * @param width
	 *           DOCUMENT ME!
	 */
	public NodeBorder(Color color, int width) {
		super();
		this.color = color;
		this.borderWidth = width;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the insets to the value of <code>width</code>.
	 * 
	 * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component, java.awt.Insets)
	 */
	@Override
	public Insets getBorderInsets(Component c, Insets insets) {
		// if (graphics == null) {
		insets.top = this.borderWidth;
		insets.left = this.borderWidth;
		insets.bottom = this.borderWidth;
		insets.right = this.borderWidth;
		
		// } else {
		// AffineTransform trafo = ((Graphics2D)graphics).getTransform();
		// int horSize = (int)Math.ceil(this.borderWidth * trafo.getScaleX());
		// int vertSize = (int)Math.ceil(this.borderWidth * trafo.getScaleY());
		//
		// insets.top = vertSize;
		// insets.left = horSize;
		// insets.bottom = vertSize;
		// insets.right = horSize;
		// }
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
	 * @param x
	 *           DOCUMENT ME!
	 * @param y
	 *           DOCUMENT ME!
	 * @param width
	 *           DOCUMENT ME!
	 * @param height
	 *           DOCUMENT ME!
	 */
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
						int height) {
		int zoomedBorderWidth;
		AffineTransform at = ((Graphics2D) c.getParent().getGraphics()).getTransform();
		Point pWH = new Point(borderWidth, borderWidth);
		try {
			at.inverseTransform(pWH, pWH);
		} catch (NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double factor = (double)pWH.x / (double)borderWidth;
		
//		if ((c.getX() % 2) == 1) {
//			((Graphics2D) g).translate(at.getTranslateX() - 1,
//								at.getTranslateY());
//			width += 1;
//		}
//		
//		if ((c.getY() % 2) == 1) {
//			((Graphics2D) g).translate(at.getTranslateX(),
//								at.getTranslateY() - 1);
//			height += 1;
//		}
		Insets insets = getBorderInsets(c);
		Color oldColor = g.getColor();
		g.translate(x, y);
		g.setColor(this.color);
		zoomedBorderWidth = (int)(factor * (double)borderWidth);
		
		if(zoomedBorderWidth <= 1)
			zoomedBorderWidth = 1;
		if(zoomedBorderWidth >= 15)
			zoomedBorderWidth = 15;

		// Paint top left and right
		Graphics cg;
		cg = g.create();
		
		// cg.setClip(0, 0, 2*width, insets.top);
		cg.fillRect(1, 1, zoomedBorderWidth, zoomedBorderWidth);
		
		// Point p = new Point((int)Math.ceil(width/((Graphics2D)cg).getTransform().getScaleX() - insets.right), 0);
		// cg.fillRect(p.x, p.y, zoomedBorderWidth, zoomedBorderWidth);
//		cg.fillRect(width - insets.right, 0, zoomedBorderWidth, zoomedBorderWidth);
		cg.fillRect(width - zoomedBorderWidth, 1, zoomedBorderWidth, zoomedBorderWidth);
		
		cg.dispose();
		
		// Paint bottom left and right
		cg = g.create();
		
		// int h_ib = (int)Math.ceil(height/((Graphics2D)cg).getTransform().getScaleY() - insets.bottom);
		// cg.setClip(0, h_ib, width, insets.bottom);
		// Point p = new Point(0, h_ib);
		cg.fillRect(1, height - zoomedBorderWidth, zoomedBorderWidth,
							zoomedBorderWidth);
		
		// p = new Point((int)Math.ceil(width/((Graphics2D)cg).getTransform().getScaleX() - insets.right),
		// (int)Math.ceil(height/((Graphics2D)cg).getTransform().getScaleY() - insets.bottom));
		cg.fillRect(width - zoomedBorderWidth, height - zoomedBorderWidth,
							zoomedBorderWidth, zoomedBorderWidth);
		
		cg.dispose();
		
		g.translate(-x, -y);
		g.setColor(oldColor);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
