package org.vanted.scaling.scaler.component;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
/**
 * A {@linkplain JLabel}-specific extension of {@link ComponentScaler}.
 * 
 * @author dim8
 *
 */
public class JLabelScaler extends ComponentScaler implements HTMLScaler {

	public JLabelScaler(float scaleFactor) {
		super(scaleFactor);
	}

	@Override
	public void coscaleFont(JComponent component) {
		super.coscaleFont(component);
		
		coscaleHTML(component);
	}

	@Override
	public void coscaleIcon(JComponent component) {
		modifyIcon((JLabel) component);
	}
	
	/**
	 * Interface method for 
	 * {@link JTextComponentScaler#modifyHTML(String, JLabel)}. Part of the 
	 * HTML-supporting interface contract.
	 * 
	 * @param component
	 */
	@Override
	public void coscaleHTML(JComponent component) {
		JLabel label = (JLabel) component;
		modifyHTML(label.getText(), label);
	}

	/**
	 * Scales the already set icon(s) and icon-related attributes of the given
	 * JLabel.
	 * 
	 * @param label JLabel
	 */
	private void modifyIcon(JLabel label) {
		Icon i; //any icon
		Icon disabled = null;
			
		if ((i = label.getDisabledIcon()) != null && !i.equals(label.getIcon()))
			//see above
			disabled = i;
			
		if ((i = label.getIcon()) != null)
			label.setIcon(modifyIcon(null, i));
			
		if (disabled != null)
			label.setDisabledIcon(modifyIcon(null, disabled));
			
		label.setIconTextGap((int) (label.getIconTextGap() * scaleFactor));	
			
		label.validate();
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
	private void modifyHTML(String t, JLabel label) {
		if (!HTMLSupport.isHTMLStyled(t))
			return;

		//save the initial tags and their order for later 
		HTMLSupport.storeTags(label, t);
		//convert tags to font size tag
		t = HTMLSupport.parseHTMLtoFontSize(t, label);
		
		if (t.equals(label.getText()))
			return;

		//remove listener to avoid looping
		HTMLSupport.handleTextListener(label, true);

		label.setText(t);

		//install listener for subsequent dynamic changes
		HTMLSupport.handleTextListener(label, false);
	}
}
