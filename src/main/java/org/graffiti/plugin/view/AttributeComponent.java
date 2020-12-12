// ==============================================================================
//
// AttributeComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AttributeComponent.java,v 1.9 2010/12/22 13:05:54 klukas Exp $

package org.graffiti.plugin.view;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import org.graffiti.attributes.Attribute;

/**
 * This component represents a <code>org.graffiti.attributes.Attribute</code>.
 * 
 * @version $Revision: 1.9 $
 */
public abstract class AttributeComponent extends JComponent implements GraffitiViewComponent {
	// ~ Methods ================================================================

	/**
	 * 
	 */
	private static final long serialVersionUID = -4061031258149050793L;

	protected Composite composite;

	protected float alpha;

	protected boolean hidden;

	/**
	 * Sets an instance of attribute which this component displays.
	 * 
	 * @param attr
	 */
	public abstract void setAttribute(Attribute attr);

	/**
	 * Returns the attribute that is displayed by this component.
	 * 
	 * @return the attribute that is displayed by this component.
	 */
	public abstract Attribute getAttribute();

	/**
	 * Sets shape of graph element to which the attribute of this component belongs.
	 * 
	 * @param geShape
	 */
	public abstract void setGraphElementShape(GraphElementShape geShape);

	/**
	 * adjust the component size and position
	 */
	public abstract void adjustComponentSize();

	/**
	 * adjust the component position only
	 */
	public abstract void adjustComponentPosition();

	/**
	 * DOCUMENT ME!
	 * 
	 * @param shift
	 *            DOCUMENT ME!
	 */
	public abstract void setShift(Point shift);

	/**
	 * Called when a graphics attribute of the attribute represented by this
	 * component has changed.
	 * 
	 * @param attr
	 *            the attribute that has triggered the event.
	 */
	public abstract void attributeChanged(Attribute attr) throws ShapeNotFoundException;

	/**
	 * Used when the shape changed in the datastructure. Makes the painter to create
	 * a new shape.
	 */
	public abstract void recreate() throws ShapeNotFoundException;

	public void highlight(boolean value, MouseEvent e) {

	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
		this.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
