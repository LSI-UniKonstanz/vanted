package org.vanted.scaling.scaler.component;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

/**
 * A {@linkplain JTextComponent}-specific extension of 
 * {@link ComponentScaler}.
 * 
 * @author dim8
 *
 */
public class JTextComponentScaler extends ComponentScaler implements HTMLScaler {

	public JTextComponentScaler(float scaleFactor) {
		super(scaleFactor);
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
	 * 
	 * @param component
	 */
	@Override
	public void coscaleHTML(JComponent component) {
		JTextComponent text = (JTextComponent) component;
		/** The order of the HTML texts is preserved and acts as second
		 *  implicit key to allow mapping of multiple texts to a single component.
		 *  
		 *  Just implement and add your HTML-modification method below following
		 *  the template. */
		modifyHTML(text.getText(), text);
		modifyHTMLTooltip(text.getToolTipText(), text);
	}
	
	/** 
	 * Worker method processing the text, given it is HTML-styled, see 
	 * {@link HTMLSupport#isHTMLStyled(String)}, by performing 
	 * parsing, substitution, removal and installation of {@link TextListener}
	 * plus text setting, if necessary.
	 * 
	 * @param t text
	 * @param text JTextComponent
	 */
	private void modifyHTML(String t, JTextComponent text) {
		if (!HTMLSupport.isHTMLStyled(t))
			return;

		//save the initial tags for later
		HTMLSupport.storeTags(text, t);
		//convert tags to font size
		t = HTMLSupport.parseHTMLtoFontSize(t, text);

		if (t.equals(text.getText()))
			return;

		//remove listener to avoid looping
		HTMLSupport.handleTextListener(text, true);

		text.setText(t);

		//install listener for subsequent dynamic changes
		HTMLSupport.handleTextListener(text, false);
	}
	
	private void modifyHTMLTooltip(String tooltip, JTextComponent text) {
		if (!HTMLSupport.isHTMLStyled(tooltip))
			return;
		
		HTMLSupport.storeTags(text, tooltip);
		
		tooltip = HTMLSupport.parseHTMLtoFontSize(tooltip, text);
		if (tooltip.equals(text.getToolTipText()))
			return;
		
		HTMLSupport.handleTextListener(text, true);
		text.setToolTipText(tooltip);
		HTMLSupport.handleTextListener(text, false);
	}
}