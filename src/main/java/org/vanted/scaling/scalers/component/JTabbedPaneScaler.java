package org.vanted.scaling.scalers.component;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;

/**
 * A {@linkplain JTabbedPane}-specific extension of {@link ComponentScaler}.
 * 
 * @author dim8
 *
 */
public class JTabbedPaneScaler extends ComponentScaler implements HTMLScaler{

	public JTabbedPaneScaler(float scaleFactor) {
		super(scaleFactor);
	}
	
	/**
	 * A method to be called when this {@linkplain JTabbedPaneScaler} has been
	 * dispatched to some immediate Component to be scaled. This tackles the problem
	 * that after a complete application scaling, through the ScalingSlider, further
	 * components, initialized posterior, are not scaled. In order to do so, attach a
	 * scaler and call this method upon initialization.
	 *  
	 * @param immediateComponent to be scaled
	 */
	public void scaleComponent(JComponent immediateComponent) {
		this.coscaleFont(immediateComponent);
		coscaleInsets(immediateComponent);
		this.coscaleIcon(immediateComponent);
	}

	
	@Override
	public void coscaleIcon(JComponent component) {
		modifyIcon((JTabbedPane) component);
	}

	/**
	 * Scales the already set icon(s) and icon-related attributes of the given
	 * JTabbedPane.
	 *
	 * @param pane JTabbedPane
	 */
	private void modifyIcon(JTabbedPane pane) {
		Icon i;

		for (int j = 0; j < pane.getTabCount(); j++)
			if ((i = pane.getIconAt(j)) != null)
				pane.setIconAt(j, modifyIcon(null, i));
	}
	
	

	@Override
	public void coscaleFont(JComponent component) {
		super.coscaleFont(component);
		
		for (int j = 0; j < ((JTabbedPane) component).getTabCount(); j++)
			if (((JTabbedPane) component).getComponentAt(j) != null)
				super.coscaleFont((JComponent) 
						((JTabbedPane) component).getComponentAt(j));
		
		coscaleHTML(component);
	}

	/**
	 * Interface method for 
	 * {@link JTabbedPaneScaler#modifyHTML(JTabbedPane, int)}. Part of the 
	 * HTML-supporting interface contract.<p>
	 * 
	 * Be careful to update the font too, because this is taken as basis and
	 * thus the end HTML scaling depends on it.
	 */
	@Override
	public void coscaleHTML(JComponent component) {
		JTabbedPane pane = (JTabbedPane) component;
		
		for (int j = 0; j < pane.getTabCount(); j++) {
			if (pane.getComponentAt(j) == null)
				continue;

			modifyHTML(pane, j);
			modifyHTMLTooltip(pane, j);
		}
		
	}
	
	/** 
	 * Worker method processing the title of a TabComponent, given it is HTML-styled, see 
	 * {@link HTMLSupport#isHTMLStyled(String)}, by performing 
	 * parsing, substitution, removal and installation of {@link TextListener}.
	 * 
	 * @param pane the JTabbedPane whose tab is to be modified
	 * @param index the TabComponent at this index
	 */
	private void modifyHTML(JTabbedPane pane, int index) {
		String t = pane.getTitleAt(index);
		
		if (!HTMLSupport.isHTMLStyled(t))
			return;
		
		JComponent c = (JComponent) pane.getComponentAt(index);
		//save the initial tags and their order for later
		HTMLSupport.storeTags(c, t);
				
		//convert tags to font size tag
		t = HTMLSupport.parseHTMLtoFontSize(t, c);

		if (t.equals(pane.getTitleAt(index)))
			return;

		//remove listener to avoid looping
		HTMLSupport.handleTextListener(c, true);

		pane.setTitleAt(index, t);

		//install listener for subsequent dynamic changes
		HTMLSupport.handleTextListener(c, false);
	}
	
	private void modifyHTMLTooltip(JTabbedPane pane, int index) {
		String t = pane.getToolTipTextAt(index);
		
		if (!HTMLSupport.isHTMLStyled(t))
			return;
		
		JComponent c = (JComponent) pane.getComponentAt(index);
		
		HTMLSupport.storeTags(c, t);
		t = HTMLSupport.parseHTMLtoFontSize(t, c);
		
		if (t.equals(pane.getToolTipTextAt(index)))
			return;

		HTMLSupport.handleTextListener(c, true);
		pane.setToolTipTextAt(index, t);
		HTMLSupport.handleTextListener(c, false);
	}
}