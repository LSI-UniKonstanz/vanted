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
		modifyHTML(text.getText(), text);
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
}