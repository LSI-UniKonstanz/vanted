/**
 * Copyright (c) 2016 University of Konstanz
 */
package org.graffiti.plugin.tool;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;

import org.graffiti.plugin.view.EdgeComponentInterface;
import org.graffiti.plugin.view.GraphElementShape;

/**
 * Optional edge selection that would be implemented soon. There are multiple
 * TODOs for now!
 * 
 * @author dim8
 *
 */
class EdgeEllipseSelection {
	// The EdgeComponent we use
	private Component mComponent;

	// predefined ellipse width and height
	private float eWidth;
	private float eHeight;

	// the common factor, coarse
	private float factor;

	EdgeEllipseSelection(Component c, int bulletSize, float factor) {

		this.mComponent = c;
		this.eWidth = 2 * bulletSize;
		this.eHeight = bulletSize / 2;
		this.factor = factor;
	}

	/**
	 * This method encapsulates all the work for creating ellipse selections over an
	 * edge. After initializing the class and passing an edgeComponent instance
	 * among others, one needs here only to specify the graphics, where the ellipses
	 * should be painted.
	 * 
	 * @param cg
	 *            the graphics to paint on
	 */
	// Only for StraightLineEdgeShape for now!
	void paintEllipses(Graphics2D cg) {
		GraphElementShape geShape = ((EdgeComponentInterface) mComponent).getShape();
		PathIterator pi = geShape.getPathIterator(null);
		float[] coords = new float[2];

		// Angle to adjust ellipses
		double theta = this.getTheta(mComponent);

		pi.currentSegment(coords);

		float[] ellFactors = calcFactors((float) factor, geShape.getPathIterator(null), theta, eWidth, eHeight,
				mComponent.getWidth());

		Ellipse2D ell = new Ellipse2D.Float(coords[0] + ellFactors[0], coords[1] + ellFactors[1], eWidth, eHeight);
		rotateAndFillEllipse(theta, ell, cg, ellFactors[4]);

		pi.next();
		pi.currentSegment(coords);

		ell = new Ellipse2D.Float(coords[0] + ellFactors[2], coords[1] + ellFactors[3], eWidth, eHeight);
		rotateAndFillEllipse(theta, ell, cg, ellFactors[5]);
	}

	/**
	 * This method calculates the position of the both marking bodies as an offset
	 * from the segment coordinates of an edge. The edge should not be bended.
	 * 
	 * @param factor
	 *            base factor
	 * @param pi
	 *            a PathIterator with two segments to parse the edge head and tail
	 * @param theta
	 *            the rotation angle in degrees
	 * @param w
	 *            the width of the body
	 * @param h
	 *            the width of the body
	 * @param cW
	 *            the width of the component
	 * @return a float array where the offset factors are stored. It is of the form
	 *         {x1,y1,x2,y2, s1, s2}, where (x1,y1) are the upper left corner
	 *         coordinates of the source body and (x2,y2) those of the
	 *         destination's. Additionally the last two (s1, s2) are reserved for
	 *         the signing of the theta, useful for later rotating the body itself.
	 */
	private float[] calcFactors(float factor, PathIterator pi, double theta, float w, float h, int cW) {
		float factors[] = new float[6]; // to return as new {x1,y1,x2,y2, thetaSign1, thetaSign2}
		float srcCoords[] = new float[2];
		float destCoords[] = new float[2];

		// get edge coordinates
		pi.currentSegment(srcCoords);
		pi.next();
		pi.currentSegment(destCoords);

		// src x-offset
		float srcX = 22 * factor;
		// dest x-offset
		float destX = 2 * factor;
		// src & dest y-offset
		float y = (2 * factor + 0.3f);

		// set up theta's
		factors[4] = 0; // src
		factors[5] = 0; // dest

		// determine edge direction and assign factor
		if (srcCoords[0] > destCoords[0]) { // going Left
			if (srcCoords[1] > destCoords[1]) { // Up
				// TODO: better function approximation!
				// Alignment Coefficient 'ac' to align body with edge line on X-Coordinate
				float ac = (float) (Math.exp(Math.cos(Math.toRadians(90 - theta)) - 0.37)) * h;
				// 'Offset from Node'-Coefficient to set separation from Node
				float oc = (float) Math.exp(Math.cos(Math.toRadians(90 - theta)) - 0.67);

				// rotate srcPoint
				Point2D p = rotatePoint(theta, srcCoords[0], srcCoords[1], srcCoords[0] - srcX + oc * h, srcCoords[1]);
				factors[0] = -(srcCoords[0] - (float) p.getX() + ac);
				factors[1] = -(srcCoords[1] - (float) p.getY());

				// rotate destPoint
				p = rotatePoint(theta, destCoords[0], destCoords[1], destCoords[0] + destX + (oc + 0.25f) * h,
						destCoords[1]);
				factors[2] = (float) p.getX() - destCoords[0] - ac;
				factors[3] = ((float) p.getY() - destCoords[1]);

				// theta signs for rotateAndFill()
				factors[4] = 1f;
				factors[5] = 1f;
			} else if (srcCoords[1] < destCoords[1]) { // Down
				// TODO: better function approximation!
				// Alignment Coefficient 'ac' to align body with edge line on X-Coordinate
				float ac = (float) (1 + Math.exp(Math.cos(Math.toRadians(theta)) - 0.24)) * h;
				// 'Offset from Node'-Coefficient to set separation from Node
				float oc = (float) (2.25 + Math.exp(Math.cos(Math.toRadians(theta)) - 1.25));

				// rotate scrPoint
				Point2D p = rotatePoint(-theta, srcCoords[0], srcCoords[1], srcCoords[0] - srcX + oc * h, srcCoords[1]);
				factors[0] = -(srcCoords[0] - (float) p.getX() + ac);
				factors[1] = (float) p.getY() - srcCoords[1];

				// rotate destPoint
				p = rotatePoint(-theta, destCoords[0], destCoords[1], destCoords[0] + destX + (oc + 0.25f) * h,
						destCoords[1]);
				factors[2] = (float) p.getX() - destCoords[0] - ac;
				factors[3] = -(destCoords[1] - (float) p.getY());

				// theta signs for rotateAndFill()
				factors[4] = -1f;
				factors[5] = -1f;
			} else { // y1 = y2
				factors[0] = -srcX;
				factors[1] = -y;
				factors[2] = destX;
				factors[3] = -y;
			}
		} else if (srcCoords[0] < destCoords[0]) { // going Right
			if (srcCoords[1] > destCoords[1]) { // Up
				// TODO: better function approximation!
				// Alignment Coefficient 'ac' to align body with edge line on X-Coordinate
				float ac = (float) (1 + Math.exp(Math.cos(Math.toRadians(theta)) - 0.24)) * h;
				// 'Offset from Node'-Coefficient to set separation from Node
				float oc = (float) (Math.exp(Math.cos(Math.toRadians(90 - theta)) - 0.67));

				// rotate srcPoint
				Point2D p = rotatePoint(-theta, srcCoords[0], srcCoords[1], srcCoords[0] + srcX - oc * h, srcCoords[1]);
				factors[0] = (float) p.getX() - srcCoords[0] - ac;
				factors[1] = -(srcCoords[1] - (float) p.getY());

				// rotate destPoint
				p = rotatePoint(-theta, destCoords[0], destCoords[1], destCoords[0] - destX - (oc + 0.25f) * h,
						destCoords[1]);
				factors[2] = -(destCoords[0] - (float) p.getX() + ac);
				factors[3] = (float) p.getY() - destCoords[1];

				// theta signs for rotateAndFill()
				factors[4] = -1f;
				factors[5] = -1f;
			} else if (srcCoords[1] < destCoords[1]) { // Down
				// TODO: better function approximation!
				// Alignment Coefficient 'ac' to align body with edge line on X-Coordinate
				float ac = (float) (Math.exp(Math.cos(Math.toRadians(90 - theta)) - 0.37)) * h;
				// 'Offset from Node'-Coefficient to set separation from Node
				float oc = (float) (0.1 + Math.exp(Math.cos(Math.toRadians(theta) - 0.67)));

				// rotate srcPoint
				Point2D p = rotatePoint(theta, srcCoords[0], srcCoords[1], srcCoords[0] + srcX - oc * h, srcCoords[1]);
				factors[0] = (float) p.getX() - srcCoords[0] - ac;
				factors[1] = (float) p.getY() - srcCoords[1];

				// rotate destPoint
				p = rotatePoint(theta, destCoords[0], destCoords[1], destCoords[0] - destX - (oc + 0.25f) * h,
						destCoords[1]);
				factors[2] = -(destCoords[0] - (float) p.getX() + ac);
				factors[3] = -(destCoords[1] - (float) p.getY());

				// theta signs for rotateAndFill()
				factors[4] = 1f;
				factors[5] = 1f;
			} else { // y1=y2
				factors[0] = srcX - w;
				factors[1] = -y;
				factors[2] = -(destX + w);
				factors[3] = -y;
			}
		} else { // x1 = x2
			if (srcCoords[1] > destCoords[1]) { // going straight Up
				factors[0] = -(cW + y);
				factors[1] = -(srcX - 1.5f * h);
				factors[2] = -(cW + y);
				factors[3] = destX + 1.5f * h;

				// theta = 90 || -90
				factors[4] = -2f;
				factors[5] = 2f;
			} else {// going straight Down
				factors[0] = -(cW + y);
				factors[1] = srcX - w + 1.5f * h;
				factors[2] = -(cW + y);
				factors[3] = -(destX + 2.5f * h);

				// theta = 90 || -90
				factors[4] = -2f;
				factors[5] = 2f;
			}

			// no detection for single points, if given, interpreted as last else
		}

		return factors;

	}

	/**
	 * Rotates a point around an angle.
	 * 
	 * @param theta
	 *            rotation angle in degrees
	 * @param anchorX
	 *            the X coordinate of the rotation anchor point
	 * @param anchorY
	 *            the Y coordinate of the rotation anchor point
	 * @param px
	 *            the X coordinate of the point to rotate
	 * @param py
	 *            the Y coordinate of the point to rotate
	 * @return an instance of the newly rotated point
	 * 
	 */
	private Point2D rotatePoint(double theta, float anchorX, float anchorY, float px, float py) {

		// rotate point
		float[] ct = { px, py };
		AffineTransform.getRotateInstance(Math.toRadians(theta), anchorX, anchorY).transform(ct, 0, ct, 0, 1); // write
																												// the
																												// new
																												// coords
																												// in ct

		return new Point2D.Float(ct[0], ct[1]);
	}

	/**
	 * Rotates the Ellipse object by rotating the containing graphics pane. The
	 * rotation angle is set by theta and the flag value. The flag parameter should
	 * be {1,-1,2,-2}. This determines the sign and whether the angle is right. At
	 * last the {@link Graphics2D} object is disposed - similar to OpenGL push and
	 * pop matrix.
	 * 
	 * @param theta
	 *            rotation angle in degrees
	 * @param rs
	 *            the object to rotate and fill
	 * @param g
	 *            the Graphics pane of rs
	 * @param flag
	 *            should be {1,-1,2,-2}. 2 if rotation angle must be 90
	 * 
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
	 * Calculates the theta angle (the angle used for rotation later) of a Component
	 * c. As theta is marked the angle, opposite to the height of the Component c.
	 * 
	 * @param c
	 *            the Component, for which the angle would be calculated.
	 * @return the newly calculated theta angle in degrees
	 *
	 */
	private double getTheta(Component c) {
		int opposite = c.getHeight();
		int adjacent = c.getWidth();
		double hypo = Math.sqrt(Math.pow(opposite, 2) + Math.pow(adjacent, 2));

		double theta = Math.asin(opposite / hypo) * (180 / Math.PI);

		return theta;
	}

}