// ==============================================================================
//
// AbstractAttributeComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractAttributeComponent.java,v 1.9 2010/12/22 13:05:55 klukas Exp $

/*
 * $$Id: AbstractAttributeComponent.java,v 1.9 2010/12/22 13:05:55 klukas Exp $$
 */
package org.graffiti.plugin.attributecomponent;

import java.awt.AlphaComposite;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;

import org.AttributeHelper;
import org.graffiti.attributes.Attribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.CoordinateSystem;
import org.graffiti.plugin.view.GraffitiViewComponent;
import org.graffiti.plugin.view.GraphElementShape;
import org.graffiti.plugin.view.ShapeNotFoundException;
import org.graffiti.plugins.views.defaults.DrawMode;
import org.graffiti.plugins.views.defaults.GraffitiView;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.IPKGraffitiView;

/**
 * This component represents a <code>org.graffiti.attributes.Attribute</code>.
 * 
 * @version $Revision: 1.9 $
 */
public abstract class AbstractAttributeComponent extends AttributeComponent implements GraffitiViewComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2418357637318671513L;
	
	// ~ Instance fields ========================================================
	
	/** The attribute that this component displays. */
	protected Attribute attr;
	
	/** The shape of the node or edge to which this attribute belongs. */
	protected GraphElementShape geShape;
	
	/** DOCUMENT ME! */
	protected Point shift;
	
	protected Point loc = new Point();
	
	// ~ Constructors ===========================================================
	
	/**
	 * Instantiates an <code>AttributeComponent</code>
	 */
	public AbstractAttributeComponent() {
		super();
		// setupOpacity(1);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets an instance of attribute which this component displays.
	 * 
	 * @param attr
	 */
	@Override
	public void setAttribute(Attribute attr) {
		this.attr = attr;
		/*
		 * new graphelement transparency attribute
		 */
		double opacity = (double) AttributeHelper.getAttributeValue(attr.getAttributable(),
				GraphicAttributeConstants.GRAPHICS, GraphicAttributeConstants.OPAC, 1.0, Double.valueOf(1));
		setupOpacity(opacity);
	}
	
	protected void setupOpacity(double opacity) {
		
		if (opacity > 1.0)
			opacity = 1.0;
		if (opacity < 0)
			return;
		alpha = ((float) opacity);
		if (opacity < 1.0) {
			setOpaque(false);
			composite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha);
			
		} else {
			
			composite = null;
		}
		
	}
	
	/**
	 * Returns the attribute that is displayed by this component.
	 * 
	 * @return the attribute that is displayed by this component.
	 */
	@Override
	public Attribute getAttribute() {
		return this.attr;
	}
	
	/**
	 * Sets shape of graph element to which the attribute of this component belongs.
	 * 
	 * @param geShape
	 */
	@Override
	public void setGraphElementShape(GraphElementShape geShape) {
		this.geShape = geShape;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param shift
	 *           DOCUMENT ME!
	 */
	@Override
	public void setShift(Point shift) {
		this.shift = shift;
	}
	
	@Override
	public void adjustComponentPosition() {
	}
	
	/**
	 * Called when a graphics attribute of the attribute represented by this
	 * component has changed.
	 * 
	 * @param attr
	 *           the attribute that has triggered the event.
	 */
	@Override
	public abstract void attributeChanged(Attribute attr) throws ShapeNotFoundException;
	
	/**
	 * Called to initialise the component of this attribute correctly. Also calls
	 * <code>repaint()</code>.
	 * 
	 * @exception ShapeNotFoundException
	 *               thrown when the shapeclass couldn't be resolved.
	 */
	public void createNewShape(CoordinateSystem coordSys) throws ShapeNotFoundException {
		this.recreate();
	}
	
	/**
	 * Used when the shape changed in the datastructure. Makes the painter to create
	 * a new shape.
	 */
	@Override
	public abstract void recreate() throws ShapeNotFoundException;
	
	/**
	 * Attribute components can use this method to check if they are or should be
	 * visible in the view. Currently there is hard coded variables defining
	 * visibility such as presumed size of the component and the current drawing
	 * mode Future implementation should parameterize this.
	 * 
	 * @param minimumComponentSize
	 *           TODO
	 * @return
	 */
	public boolean checkVisibility(int minimumComponentSize) {
		/*
		 * only draw component, if printing is in progress, it is graphically visible or
		 * not FAST mode enabled
		 */
		Container parent = getParent();
		if (parent instanceof IPKGraffitiView && ((IPKGraffitiView) getParent()).printInProgress) {
			return true;
		}
		if (parent instanceof GraffitiView) {
			GraffitiView view = (GraffitiView) parent;
			if (view.getDrawMode() == DrawMode.FAST)
				return false;
			AffineTransform zoom = view.getZoom();
			// TODO: parameterize those constants..
			if (getHeight() * zoom.getScaleX() < minimumComponentSize
					|| getWidth() * zoom.getScaleY() < minimumComponentSize)
				return false;
			else
				return true;
			
		} else
			return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		// if (hidden)
		// return;
		// if (composite != null) {
		// g = g.create();
		// ((Graphics2D) g).setComposite(composite);
		// }
		super.paintComponent(g);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		if (hidden)
			return;
		if (composite != null) {
			g = g.create();
			((Graphics2D) g).setComposite(composite);
		}
		super.paint(g);
	}
	
	@Override
	public void adjustComponentSize() {
		// TODO Auto-generated method stub
		
	}
	
	protected DrawMode getDrawingModeOfView() {
		if (getParent() instanceof GraffitiView) {
			GraffitiView view = (GraffitiView) getParent();
			return view.getDrawMode();
		} else
			return DrawMode.NORMAL;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
