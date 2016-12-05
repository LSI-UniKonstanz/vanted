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
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;
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
public class EdgeBorder
		extends AbstractBorder {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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
	 * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component, java.awt.Insets)
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
	public void paintBorder(Component c, Graphics g, int bx, int by, int width,
			int height) {
		AffineTransform at = ((Graphics2D) c.getParent().getGraphics()).getTransform();
		Point pWH = new Point(bulletSize, bulletSize);
		try {
			at.inverseTransform(pWH, pWH);
		} catch (NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double factor = pWH.x / bulletSize;
		
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
			
			//Angle to adjust ellipses
			double theta = this.getTheta(c);
			
			float ellWidth = 2*bulletSize;
			float ellHeight = bulletSize/2;
			
			pi.currentSegment(coords);
			float[] ellFactors = calcFactors((float) factor, grShape.getPathIterator(null), theta,
									ellWidth, ellHeight, c.getWidth());
			Ellipse2D ell = new Ellipse2D.Float(coords[0] + ellFactors[0], coords[1] + ellFactors[1],
							ellWidth, ellHeight);
			rotateAndFillEllipse(theta, ell, cg, ellFactors[4]);
			
			pi.next();
			pi.currentSegment(coords);
			ell = new Ellipse2D.Float(coords[0] + ellFactors[2], coords[1] + ellFactors[3],
					ellWidth, ellHeight);
			rotateAndFillEllipse(theta, ell, cg, ellFactors[5]);
		} else {
			
			if (showBends) {
				Color lightColor = this.color.darker().darker();
				cg.setColor(lightColor);
				
				int bendBulletSize = (int) (bulletSize);
				
				if (bendBulletSize == 0) {
					bendBulletSize = 1;
				}
				
				SortedCollectionAttribute bends = (SortedCollectionAttribute) ((EdgeComponentInterface) c).getGraphElement()
						.getAttribute(GraphicAttributeConstants.BENDS_PATH);
				
				for (Iterator<?> it = bends.getCollection().values().iterator(); it.hasNext();) {
					CoordinateAttribute bendCoord = (CoordinateAttribute) it.next();
					
					// cg.setClip(0, 0, width, height);
					// cg.fillOval((int)bendCoord.getX()-(c.getBounds().x),
					// (int)bendCoord.getY()-(c.getBounds().y),
					// 2*bulletSize, 2*bulletSize);
					cg.fillOval((int) (bendCoord.getX() - c.getX() -
							(bendBulletSize / 2d)),
							(int) (bendCoord.getY() - c.getY() - (bendBulletSize / 2d)),
							bendBulletSize, bendBulletSize);
					
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
				cg.fillRect((int) (x ), (int) (y),
						bulletSize,bulletSize);
				
				// cg.fillOval((int)x-2, (int)y-2, bulletSize, bulletSize);
				while (!pi.isDone()) {
					pi.next();
					type = pi.currentSegment(seg);
					
					switch (type) {
						case java.awt.geom.PathIterator.SEG_MOVETO:
							
//							 x = seg[0];
//							 y = seg[1];
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
					cg.fillRect((int) (x - bulletSizeHalf),
							(int) (y - bulletSizeHalf), bulletSize, bulletSize);
				}
			} catch (java.util.NoSuchElementException e) {
			} catch (ArrayIndexOutOfBoundsException e) {
				// why does this happen?!?
			}
		}
		cg.dispose();
	}
	
	/**
	 * This method calculates the position of the both marking bodies as an offset from
	 * the segment coordinates of an edge. The edge should not be bended. 
	 * 
	 * @param factor base factor
	 * @param pi a PathIterator with two segments to parse the edge head and tail
	 * @param theta the rotation angle in degrees
	 * @param w the width of the body
	 * @param h the width of the body
	 * @param cW the width of the component
	 * @return a float array where the offset factors are stored. It is of the form
	 * 		   {x1,y1,x2,y2, s1, s2}, where (x1,y1) are the upper left corner coordinates of the
	 *         source body and (x2,y2) those of the destination's. Additionally the last
	 *         two (s1, s2) are reserved for the signing of the theta, useful for later 
	 *         rotating the body itself.
	 * @author dim8
	 */
	private float[] calcFactors(float factor, PathIterator pi, double theta, float w, float h, int cW) {
		float factors[] = new float[6]; //to return as new {x1,y1,x2,y2, thetaSign1, thetaSign2}
		float srcCoords[] = new float[2];
		float destCoords[] = new float[2];
		
		//get edge coordinates
		pi.currentSegment(srcCoords);
		pi.next();
		pi.currentSegment(destCoords);
		
		//src x-offset
		float srcX = 22 * factor;
		//dest x-offset
		float destX = 2*factor;
		//src & dest y-offset
		float y = (2*factor + 0.3f);
		
		//set up theta's
		factors[4] = 0; //src
		factors[5] = 0; //dest

		//determine edge direction and assign factor
		if (srcCoords[0] > destCoords[0]) { //going Left
			if (srcCoords[1] > destCoords[1]) { //Up
				//TODO: better function approximation!
				//Alignment Coefficient 'ac' to align body with edge line on X-Coordinate
				float ac = (float) (Math.exp(Math.cos(Math.toRadians(90 - theta)) - 0.37)) * h;
				//'Offset from Node'-Coefficient to set separation from Node
				float oc = (float) Math.exp(Math.cos(Math.toRadians(90 - theta)) - 0.67);
				
				//rotate srcPoint
				Point2D p = rotatePoint(theta, srcCoords[0], srcCoords[1],
						srcCoords[0] - srcX + oc*h, srcCoords[1]);
				factors[0] = - (srcCoords[0] - (float) p.getX() + ac);
				factors[1] = - (srcCoords[1] - (float) p.getY());
				
				//rotate destPoint
				p = rotatePoint(theta, destCoords[0], destCoords[1],
						destCoords[0] + destX + (oc + 0.25f)*h, destCoords[1]);
				factors[2] = (float) p.getX() - destCoords[0] - ac;
				factors[3] = ((float) p.getY() - destCoords[1]);
				
				//theta signs for rotateAndFill()
				factors[4] = 1f;
				factors[5] = 1f;
			} else if (srcCoords[1] < destCoords[1]) { //Down
				//TODO: better function approximation!
				//Alignment Coefficient 'ac' to align body with edge line on X-Coordinate
				float ac = (float) (1 + Math.exp(Math.cos(Math.toRadians(theta)) - 0.24)) * h;
				//'Offset from Node'-Coefficient to set separation from Node
				float oc = (float) (2.25 + Math.exp(Math.cos(Math.toRadians(theta)) - 1.25));
				
				//rotate scrPoint
				Point2D p = rotatePoint(-theta, srcCoords[0], srcCoords[1],
						srcCoords[0] - srcX + oc*h, srcCoords[1]);
				factors[0] = -(srcCoords[0] - (float) p.getX() + ac);
				factors[1] = (float) p.getY() - srcCoords[1];
				
				//rotate destPoint
				p = rotatePoint(-theta, destCoords[0], destCoords[1],
						destCoords[0] + destX + (oc + 0.25f)*h, destCoords[1]);
				factors[2] = (float) p.getX() - destCoords[0] - ac;
				factors[3] = - (destCoords[1] - (float) p.getY());
				
				//theta signs for rotateAndFill()
				factors[4] = -1f;
				factors[5] = -1f;
			} else { //y1 = y2
				factors[0] = - srcX;
				factors[1] = - y;
				factors[2] = destX;
				factors[3] = - y;
			}
		} else if (srcCoords[0] < destCoords[0]) { //going Right
			if (srcCoords[1] > destCoords[1]) { //Up
				//TODO: better function approximation!
				//Alignment Coefficient 'ac' to align body with edge line on X-Coordinate
				float ac = (float) (1 + Math.exp(Math.cos(Math.toRadians(theta)) - 0.24)) * h;
				//'Offset from Node'-Coefficient to set separation from Node
				float oc = (float) (Math.exp(Math.cos(Math.toRadians(90 - theta)) - 0.67));
				
				//rotate srcPoint
				Point2D p = rotatePoint(-theta, srcCoords[0], srcCoords[1],
						srcCoords[0] + srcX - oc*h, srcCoords[1]);
				factors[0] = (float) p.getX() - srcCoords[0] - ac;
				factors[1] = - (srcCoords[1] - (float) p.getY());
				
				//rotate destPoint
				p = rotatePoint(-theta, destCoords[0], destCoords[1],
						destCoords[0] - destX - (oc + 0.25f)*h, destCoords[1]);
				factors[2] = - (destCoords[0] - (float) p.getX() + ac);
				factors[3] = (float) p.getY() - destCoords[1];

				//theta signs for rotateAndFill()
				factors[4] = -1f;
				factors[5] = -1f;
			} else if (srcCoords[1] < destCoords[1]) { //Down
				//TODO: better function approximation!
				//Alignment Coefficient 'ac' to align body with edge line on X-Coordinate
				float ac = (float) (Math.exp(Math.cos(Math.toRadians(90 - theta)) - 0.37)) * h;
				//'Offset from Node'-Coefficient to set separation from Node
				float oc = (float) (0.1 + Math.exp(Math.cos(Math.toRadians(theta) - 0.67)));
				
				//rotate srcPoint
				Point2D p = rotatePoint(theta, srcCoords[0], srcCoords[1],
						srcCoords[0] + srcX - oc*h, srcCoords[1]);
				factors[0] = (float) p.getX() - srcCoords[0] - ac;
				factors[1] = (float) p.getY() - srcCoords[1];
				
				//rotate destPoint
				p = rotatePoint(theta, destCoords[0], destCoords[1],
						destCoords[0] - destX - (oc + 0.25f)*h, destCoords[1]);
				factors[2] = - (destCoords[0] - (float) p.getX() + ac);
				factors[3] = - (destCoords[1] - (float) p.getY());
				
				//theta signs for rotateAndFill()
				factors[4] = 1f;
				factors[5] = 1f;
			} else { //y1=y2
				factors[0] = srcX - w;
				factors[1] = - y;
				factors[2] = - (destX + w);
				factors[3] = - y;
			}			
		} else { //x1 = x2
			if (srcCoords[1] > destCoords[1]) { //going straight Up
				factors[0] = - (cW + y);
				factors[1] = - (srcX - 1.5f*h);
				factors[2] = - (cW + y);
				factors[3] = destX + 1.5f*h;
				
				//theta = 90 || -90
				factors[4] = -2f;
				factors[5] = 2f;
			} else {//going straight Down
				factors[0] = - (cW + y);
				factors[1] = srcX - w + 1.5f*h;
				factors[2] = - (cW + y);
				factors[3] = - (destX + 2.5f*h);
				
				//theta = 90 || -90
				factors[4] = -2f;
				factors[5] = 2f;
			}
			
			//no detection for single points, if given, interpreted as last else
		}
		
		return factors;
		
	}
	
	/**
	 * Rotates a point around an angle.
	 * 
	 * @param theta rotation angle in degrees
	 * @param anchorX the X coordinate of the rotation anchor point
	 * @param anchorY the Y coordinate of the rotation anchor point
	 * @param px the X coordinate of the point to rotate
	 * @param py the Y coordinate of the point to rotate
	 * @return an instance of the newly rotated point
	 * 
	 * @author dim8
	 */
	private Point2D rotatePoint(double theta, float anchorX, float anchorY, float px, float py) {
		
		//rotate point
		float[] ct = {px, py};
		AffineTransform.getRotateInstance(Math.toRadians(theta), anchorX, anchorY)
		  .transform(ct, 0, ct, 0, 1); //write the new coords in ct
			
		return new Point2D.Float(ct[0], ct[1]);
	}
	/**
	 * Rotates the Ellipse object by rotating the containing graphics pane. The rotation angle is set 
	 * by theta and the flag value. The flag parameter should be {1,-1,2,-2}. This determines the sign and
	 * whether the angle is right. At last the {@link Graphics2D} object is disposed - similar to OpenGL push 
	 * and pop matrix.  
	 * @param theta rotation angle in degrees
	 * @param rs the object to rotate and fill
	 * @param g the Graphics pane of rs
	 * @param flag should be {1,-1,2,-2}. 2 if rotation angle must be 90
	 * 
	 * @author dim8
	 */
	private void rotateAndFillEllipse(double theta, RectangularShape rs, Graphics2D g, float flag) {
		if (flag == 2f || flag == -2f)
			theta = Math.toRadians(flag * 45);
		else
			theta = Math.toRadians(flag * theta);
		
		Graphics2D gg = (Graphics2D) g.create();
		gg.rotate(theta, rs.getCenterX(), rs.getCenterY());
	    gg.fill(new Ellipse2D.Double(rs.getX(), rs.getY(), rs.getWidth(), rs.getHeight()));
	    gg.dispose();
	}
	
	/**
	 * Calculates the theta angle (the angle used for rotation later) of a Component c. 
	 * As theta is marked the angle, opposite to the height of the Component c. 
	 * @param c the Component, for which the angle would be calculated.
	 * @return the newly calculated theta angle in degrees
	 * @author dim8
	 */
	private double getTheta(Component c) {
		int opposite = c.getHeight();
		int adjacent = c.getWidth();
		double hypo = Math.sqrt(Math.pow(opposite, 2) + Math.pow(adjacent, 2));
		
		double theta = Math.asin(opposite/hypo) * (180/Math.PI);
		
		return theta;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------