// ==============================================================================
//
// SpinnerEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: SpinnerEditComponent.java,v 1.8 2010/12/22 13:05:54 klukas Exp $

package org.graffiti.plugin.editcomponent;

import java.awt.Dimension;
import java.text.ParseException;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;

import org.graffiti.attributes.ByteAttribute;
import org.graffiti.attributes.IntegerAttribute;
import org.graffiti.attributes.LongAttribute;
import org.graffiti.attributes.ShortAttribute;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.parameter.AbstractLimitableParameter;

/**
 * An <code>EditComponent</code>, displaying values in a JSpinner-manner.
 * 
 * @version 2.6.5
 * @vanted.revision 2.6.5
 */
public class SpinnerEditComponent extends AbstractValueEditComponent {
	// ~ Instance fields ========================================================

	/** The default step width for floating point numbers. */
	private final Double DEFAULT_STEP = Double.valueOf(0.5);

	/** The spinner component used. */
	private JSpinner jSpinner;

	// ~ Constructors ===========================================================

	/**
	 * Constructor for SpinnerEditComponent.
	 * 
	 * @param disp
	 *            containing the attributes to be displayed
	 */
	public SpinnerEditComponent(Displayable disp) {
		super(disp);
		SpinnerNumberModel model;

		if (disp instanceof IntegerAttribute || disp instanceof ByteAttribute || disp instanceof LongAttribute
				|| disp instanceof ShortAttribute) {
			model = new SpinnerNumberModel(Integer.valueOf(0), null, null, Integer.valueOf(1));

		} else if (disp instanceof AbstractLimitableParameter) {
			Comparable<?> min = ((AbstractLimitableParameter) disp).getMin();
			Comparable<?> max = ((AbstractLimitableParameter) disp).getMax();
			Number stepSize = ((AbstractLimitableParameter) disp).getValuesBall();
			if (min instanceof Double) {
				Double dMin = (Double) min;
				Double dMax = (Double) max;
				min = dMin.compareTo(Double.MIN_VALUE) == 0 ? null : min;
				max = dMax.compareTo(Double.MAX_VALUE) == 0 ? null : max;
				model = new SpinnerNumberModel((Double) disp.getValue(), min, max, stepSize);
			} else if (min instanceof Float) {
				Float fMin = (Float) min;
				Float fMax = (Float) max;
				model = new SpinnerNumberModel((Float) disp.getValue(),
						fMin.compareTo(Float.MIN_VALUE) == 0 ? null : min,
						fMax.compareTo(Float.MAX_VALUE) == 0 ? null : max, stepSize);
			} else {
				Integer iMin = (Integer) min;
				Integer iMax = (Integer) max;
				model = new SpinnerNumberModel((Integer) disp.getValue(),
						iMin.compareTo(Integer.MIN_VALUE) == 0 ? null : min,
						iMax.compareTo(Integer.MAX_VALUE) == 0 ? null : max, stepSize);
			}
		} else {
			model = new SpinnerNumberModel(0d, null, null, DEFAULT_STEP);
		}
		this.jSpinner = new JSpinner(model);
		jSpinner.setOpaque(false);
		displayable = null; // ensure setDisplayable really does sth
		this.setDisplayable(disp);
	}

	// ~ Methods ================================================================

	/**
	 * Returns the <code>ValueEditComponent</code>'s <code>JComponent</code>.
	 * 
	 * @return underlying JSpinner Component
	 */
	public JComponent getComponent() {
		jSpinner.setMinimumSize(new Dimension(0, 30));
		jSpinner.setPreferredSize(new Dimension(50, 30));
		jSpinner.setMaximumSize(new Dimension(2000, 30));
		return jSpinner;
	}

	/**
	 * Sets the displayable.
	 * 
	 * @param disp
	 *            new displayable
	 */
	@Override
	public void setDisplayable(Displayable disp) {
		this.displayable = disp;
	}

	/**
	 * Sets the current value of the <code>Attribute</code> in the corresponding
	 * <code>JComponent</code>.
	 */
	@Override
	public void setEditFieldValue() {
		if (showEmpty) {
			((JSpinner.DefaultEditor) this.jSpinner.getEditor()).getTextField().setText(EMPTY_STRING);
		} else {
			jSpinner.setValue(this.displayable.getValue());

			ChangeEvent ce = new ChangeEvent(jSpinner);

			for (int i = 0; i < jSpinner.getChangeListeners().length; i++) {
				jSpinner.getChangeListeners()[i].stateChanged(ce);
			}
		}
	}

	@Override
	public void setShowEmpty(boolean showEmpty) {
		if (this.showEmpty != showEmpty) {
			super.setShowEmpty(showEmpty);
		}

		this.setEditFieldValue();
	}

	/**
	 * Sets the value of the displayable, specified in the <code>JComponent</code>.
	 * But only, if it is different.
	 */
	@Override
	public void setValue() {
		if (jSpinner.getEditor() != null && jSpinner.getEditor() instanceof NumberEditor) {
			NumberEditor ne = (NumberEditor) jSpinner.getEditor();
			String txt = ne.getTextField().getText();
			try {
				if (txt.equals(EMPTY_STRING)) {
					return;
				}
				if (txt.startsWith("*")) {
					Double p = Double.parseDouble(txt.substring("*".length()));
					if (this.displayable.getValue() instanceof Double) {
						this.displayable.setValue((Double) this.displayable.getValue() * p);
						return;
					} else if (this.displayable.getValue() instanceof Integer) {
						this.displayable.setValue((int) ((Integer) this.displayable.getValue() * p));
						return;
					}
				} else if (txt.startsWith("/")) {
					Double p = Double.parseDouble(txt.substring("/".length()));
					if (this.displayable.getValue() instanceof Double) {
						this.displayable.setValue((Double) this.displayable.getValue() / p);
						return;
					} else if (this.displayable.getValue() instanceof Integer) {
						this.displayable.setValue((int) ((Integer) this.displayable.getValue() / p));
						return;
					}
				} else if (txt.startsWith("+")) {
					Double p = Double.parseDouble(txt.substring("+".length()));
					if (this.displayable.getValue() instanceof Double) {
						this.displayable.setValue((Double) this.displayable.getValue() + p);
						return;
					} else if (this.displayable.getValue() instanceof Integer) {
						this.displayable.setValue((int) ((Integer) this.displayable.getValue() + p));
						return;
					}
				}

			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		try {
			jSpinner.getEditor();
			jSpinner.commitEdit();
			if (!this.displayable.getValue().equals(this.jSpinner.getValue()))
				this.displayable.setValue(this.jSpinner.getValue());
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
