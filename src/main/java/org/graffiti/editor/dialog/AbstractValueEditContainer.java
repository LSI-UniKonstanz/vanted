// ==============================================================================
//
// AbstractValueEditContainer.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractValueEditContainer.java,v 1.6 2010/12/22 13:05:54 klukas Exp $

package org.graffiti.editor.dialog;

import java.awt.LayoutManager;
import java.util.List;

import javax.swing.JComponent;

import org.graffiti.plugin.editcomponent.ValueEditComponent;

/**
 * This class provides an abstract implementation of the
 * <code>ValueEditComponent</code>-interface.
 * 
 * @see javax.swing.JComponent
 * @see org.graffiti.plugin.editcomponent.ValueEditComponent
 */
public abstract class AbstractValueEditContainer extends JComponent implements ValueEditContainer {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2719479990330345552L;
	
	/** The <code>LayoutManager</code> for this component. */
	protected LayoutManager lm;
	
	/** The list of <code>ValueEditComponent</code>s the dialog contains. */
	protected List<ValueEditComponent> editComponents;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for AbstractValueEditContainer.
	 */
	protected AbstractValueEditContainer() {
		super();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns a list containing all the <code>ValueEditComponent</code>s of this
	 * value edit container.
	 * 
	 * @return a list containing all the <code>ValueEditComponent</code>s of this
	 *         value edit container.
	 */
	public List<ValueEditComponent> getValueEditComponents() {
		return this.editComponents;
	}
	
	/**
	 * Adds another <code>ValueEditComponent</code> to the current dialog.
	 * 
	 * @param vec
	 *           the <code>ValueEditComponent</code> to be added.
	 */
	public void addValueEditComponent(ValueEditComponent vec) {
		editComponents.add(vec);
		doAddValueEditComponent(vec);
	}
	
	/**
	 * Adds the specified <code>ValueEditComponent</code> to the container.
	 * 
	 * @param vec
	 *           the <code>ValueEditComponent</code> to be added to the container.
	 */
	protected abstract void doAddValueEditComponent(ValueEditComponent vec);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
