package org.vanted.scaling.scalers.component;

import java.awt.Component;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * An {@linkplain AbstractButton}-specific extension of {@link ComponentScaler}.
 * 
 * @author dim8
 *
 */
public class AbstractButtonScaler extends ComponentScaler implements HTMLScaler {

	public AbstractButtonScaler(float scaleFactor) {
		super(scaleFactor);
	}

	/**
	 * A method to be called when this {@linkplain AbstractButtonScaler} has been
	 * dispatched to some immediate Component to be scaled. This tackles the problem
	 * that after a complete application scaling, through the ScalingSlider, further
	 * components, initialized posterior, are not scaled. In order to do so, attach a
	 * scaler and call this method upon initialization.
	 *  
	 * @param immediateComponent to be scaled
	 */
	@Override
	public void scaleComponent(JComponent immediateComponent) {
		this.coscaleFont(immediateComponent);
		coscaleInsets(immediateComponent);
		this.coscaleIcon(immediateComponent);
	}
	
	@Override
	public void coscaleFont(JComponent component) {
		super.coscaleFont(component);
		
		coscaleHTML(component);
	}

	@Override
	public void coscaleIcon(JComponent component) {
		if (component instanceof JMenu) {
			for (Component item: ((JMenu) component).getMenuComponents())
				if (item instanceof AbstractButton) {
					if (item instanceof JMenu)
						coscaleIcon((JComponent) item);
					modifyIcon((AbstractButton) item);
				}
		} else
			modifyIcon((AbstractButton) component);
	}
	
	/**
	 * Interface method for 
	 * {@link AbstractButtonScaler#modifyHTML(String, AbstractButton)}. Part of the 
	 * HTML-supporting interface contract.<p>
	 * 
	 * Be careful to update the font too, because this is taken as basis and
	 * thus the end HTML scaling depends on it.
	 *
	 * @param component
	 */
	@Override
	public void coscaleHTML(JComponent component) {
		AbstractButton button = (AbstractButton) component;
		/** The order of the HTML texts is preserved and acts as second
		 *  implicit key to allow mapping of multiple texts to a single component.
		 *  
		 *  Just implement and add your HTML-modification method below following
		 *  the template. */
		modifyHTML(button.getText(), button);
		modifyHTMLTooltip(button.getToolTipText(), button);
	}
	
	/**
	 * Scales the already set icon(s) and icon-related attributes of the given
	 * AbstractButton.
	 * 
	 * @param button AbstractButton
	 */
	private void modifyIcon(AbstractButton button) {
		Icon i; //any icon
		Icon disabled = null;
		Icon disabledSelected = null;
		Icon pressed = null;

		if ((i = button.getDisabledIcon()) != null && !i.equals(button.getIcon()))
			//save it (setIcon nullifies disabledIcon)
			disabled = i;

		if ((i = button.getDisabledSelectedIcon()) != null &&
				!i.equals(button.getIcon()))
			//again save it!
			disabledSelected = i;

		if ((i = button.getPressedIcon()) != null && !i.equals(button.getIcon()))
			//again save it!
			pressed = i;

		if ((i = button.getIcon()) != null)
			button.setIcon(modifyIcon(null, i));

		if ((i = button.getRolloverIcon()) != null && !i.equals(button.getIcon()))
			button.setRolloverIcon(modifyIcon(null, i));

		if ((i = button.getRolloverSelectedIcon()) != null && !i.equals(button.getIcon()))
			button.setRolloverSelectedIcon(modifyIcon(null, i));

		if ((i = button.getSelectedIcon()) != null && !i.equals(button.getIcon()))
			button.setSelectedIcon(modifyIcon(null, i));

		if (disabled != null)
			button.setDisabledIcon(modifyIcon(null, disabled));

		if (disabledSelected != null)
			button.setDisabledSelectedIcon(modifyIcon(null, disabledSelected));

		if (pressed != null)
			button.setPressedIcon(modifyIcon(null, pressed));

		if (button instanceof JMenuItem) {
			//JMenuItem specifics come here
		} else
			//we do not scale menu gaps (some are unset)
			button.setIconTextGap((int) (button.getIconTextGap() * scaleFactor));	

		button.validate();
	}
	
	/** 
	 * Worker method processing the text, given it is HTML-styled, see 
	 * {@link HTMLSupport#isHTMLStyled(String)}, by performing 
	 * parsing, substitution, removal and installation of {@link TextListener}
	 * plus text setting, if necessary.
	 * 
	 * @param t text
	 * @param button AbstractButton
	 */
	private void modifyHTML(String t, AbstractButton button) {
		if (!HTMLSupport.isHTMLStyled(t))
			return;

		//save the initial tags for later
		HTMLSupport.storeTags(button, t);
		//convert tags to font size
		t = HTMLSupport.parseHTMLtoFontSize(t, button);

		if (t.equals(button.getText()))
			return;

		//remove listener to avoid looping
		HTMLSupport.handleTextListener(button, true);

		button.setText(t);

		//install listener for subsequent dynamic changes
		HTMLSupport.handleTextListener(button, false);
	}
	
	/**
	 * Similar to {@link AbstractButtonScaler#modifyHTML(String, AbstractButton)},
	 * but for Tooltip texts.
	 * 
	 * @param tooltip text of the tooltip
	 * @param button AbstractButton
	 */
	private void modifyHTMLTooltip(String tooltip, AbstractButton button) {
		if (!HTMLSupport.isHTMLStyled(tooltip))
			return;
		
		HTMLSupport.storeTags(button, tooltip);
		
		tooltip = HTMLSupport.parseHTMLtoFontSize(tooltip, button);
		if (tooltip.equals(button.getToolTipText()))
			return;
		
		HTMLSupport.handleTextListener(button, true);
		button.setToolTipText(tooltip);
		HTMLSupport.handleTextListener(button, false);
	}
}