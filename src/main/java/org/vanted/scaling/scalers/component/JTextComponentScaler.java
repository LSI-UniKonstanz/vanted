// ==============================================================================
//
// JTextComponentScaler.java
//
// Copyright (c) 2017-2019, University of Konstanz
//
// ==============================================================================
package org.vanted.scaling.scalers.component;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;

/**
 * A {@linkplain JTextComponent}-specific extension of {@link ComponentScaler}.
 * 
 * @author D. Garkov
 */
public class JTextComponentScaler extends ComponentScaler implements HTMLScaler {
	
	public JTextComponentScaler(float scaleFactor) {
		super(scaleFactor);
	}
	
	/**
	 * A method to be called when this {@linkplain JTextComponentScaler} has been
	 * dispatched to some immediate Component to be scaled. This tackles the problem
	 * that after a complete application scaling, through the ScalingSlider, further
	 * components, initialized posterior, are not scaled. In order to do so, attach
	 * a scaler and call this method upon initialization.
	 * 
	 * @param immediateComponent
	 *           to be scaled
	 */
	public void scaleComponent(JComponent immediateComponent) {
		this.coscaleFont(immediateComponent);
		coscaleInsets(immediateComponent);
	}
	
	@Override
	public void coscaleFont(JComponent component) {
		super.coscaleFont(component);
		
		coscaleHTML(component);
	}
	
	/**
	 * Interface method for
	 * {@link JTextComponentScaler#modifyHTML(String, JTextComponent)}. Part of the
	 * HTML-supporting interface contract.
	 * <p>
	 * Be careful to update the font too, because this is taken as basis and thus
	 * the end HTML scaling depends on it.
	 *
	 * @param component
	 */
	@Override
	public void coscaleHTML(JComponent component) {
		/* Scale the HTML label only for emulated DPIs. */
		if (scaleFactor == 1f)
			return;
		
		JTextComponent text = (JTextComponent) component;
		
		/**
		 * Set the HTML, in our use-case, font size of the JEditorPane to the freshly
		 * scaled system LookAndFeel font size.
		 */
		alignJEP(text);
		
		/**
		 * The order of the HTML texts is preserved and acts as second implicit key to
		 * allow mapping of multiple texts to a single component.
		 * Just implement and add your HTML-modification method below following the
		 * template.
		 */
		modifyHTML(text.getText(), text);
		modifyHTMLTooltip(text.getToolTipText(), text);
	}
	
	/**
	 * Worker method processing the text, given it is HTML-styled, see
	 * {@link HTMLScaleSupport#isHTMLStyled(String)}, by performing parsing,
	 * substitution, removal and installation of {@link TextListener} plus text
	 * setting, if necessary.
	 * 
	 * @param t
	 *           text
	 * @param text
	 *           JTextComponent
	 */
	private static void modifyHTML(String t, JTextComponent text) {
		if (!HTMLScaleSupport.isHTMLStyled(t))
			return;
		
		// save the initial tags for later
		HTMLScaleSupport.storeTags(text, t);
		// convert tags to font size
		t = HTMLScaleSupport.parseHTMLtoFontSize(t, text);
		
		if (t.equals(text.getText()))
			return;
		
		// remove listener to avoid looping
		HTMLScaleSupport.handleTextListener(text, true);
		
		text.setText(t);
		
		// install listener for subsequent dynamic changes
		HTMLScaleSupport.handleTextListener(text, false);
	}
	
	private static void modifyHTMLTooltip(String tooltip, JTextComponent text) {
		if (!HTMLScaleSupport.isHTMLStyled(tooltip))
			return;
		
		HTMLScaleSupport.storeTags(text, tooltip);
		
		tooltip = HTMLScaleSupport.parseHTMLtoFontSize(tooltip, text);
		if (tooltip.equals(text.getToolTipText()))
			return;
		
		HTMLScaleSupport.handleTextListener(text, true);
		text.setToolTipText(tooltip);
		HTMLScaleSupport.handleTextListener(text, false);
	}
	
	/**
	 * JEditorPane has a couple of intrinsic properties, with the help of which, one
	 * could control to a certain extent, the layout (e.g. HTML) globally. These are
	 * <code>JEditorPane.HONOR_DISPLAY_PROPERTIES</code> and
	 * <code>JEditorPane.W3C_LENGTH_UNITS</code>.
	 * 
	 * @param text
	 *           JComponent to be checked, only JEditorPanes possess the system
	 *           property to be set.
	 */
	public static void alignJEP(JTextComponent text) {
		if (text instanceof JEditorPane)
			((JEditorPane) text).putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
	}
}