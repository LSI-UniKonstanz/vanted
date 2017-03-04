package org.vanted.scaling;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;

import org.graffiti.editor.MainFrame;


/**
 * <i>Notice:</i> it is advisable to not access it directly, but through
 * the provided {@link ScaleCoordinator}, because of factor conversions.
 * 
 * This could be used for any user-related modifications
 * that are not LAF-related. For example, user-specified icons are outside
 * of scope of the LAF Defaults and thus may be mined and rescaled here. 
 * 
 * @param mainFrame the application's main frame to get the children 
 * 					components therewith.
 */
public class ExternalScaler extends BasicScaler {

	public ExternalScaler(float scaleFactor) {
		super(scaleFactor);
	}
	
	/**
	 * Scan and scale the defaults of the components placed in the specified
	 * {@link Container} <code>container</code>.
	 * 
	 * @param container, whose components are to be modified
	 */
	public void init(Container container) {
		if (container == null)
			return;
		
		//ensure some extra capacity
		System.gc();
		
		doExternalScaling(container);
	}
	
	/**
	 * Specify here application specific update of the 
	 * affected listeners, given there are any affected.
	 */
	public void notifyListeners() {
		//Vanted specific: update the state of the underlying Action
		MainFrame.getInstance().updateActions();
	}
	
	public void doExternalScaling(Container c) {		
		Container container;
		
		if (c instanceof Frame)
			container = ((JRootPane) ((Frame) c).getComponents()[0])
							.getContentPane();
		else
			container = c;
		
		scaleComponentsOf(container);
	}	
	
	private void scaleComponentsOf(Container container) {				
		for (Component c : container.getComponents()) {
			
			//delegate further extraction
			if (c instanceof JComponent)
				scaleExternalComponents((JComponent) c);
			
			//go further down recursively
			if (c instanceof Container)
				scaleComponentsOf((Container) c);
		}
	}

	/**
	 * Modifies the direct icons of the following JComponents:<p>
	 * 
	 * <b>AbstractButton</b>,<br>
	 * <b>JLabel</b>,<br>
	 * <b>JOptionPane</b>,<br>
	 * <b>JTabbedPane</b>.<br>
	 * 
	 * @param component a possible Component containing Icon
	 */
	private void scaleExternalComponents(JComponent component) {
		//some common cases of JComponents not having Icon
		//TODO
		
		if (component instanceof AbstractButton) {
			AbstractButton a = (AbstractButton) component;
			modifyExternalIcon(a, null, null);
		} else if (component instanceof JLabel) {
			JLabel b = (JLabel) component;
			modifyExternalIcon(null, b, null);
		} else if (component instanceof JOptionPane) {
			JOptionPane c = (JOptionPane) component;
			modifyExternalIcon(null, null, c);
		} else if (component instanceof JTabbedPane) {
			JTabbedPane d = (JTabbedPane) component;
			Icon i;
			int j = 0;
			
			while(d.getTabCount() > 0 && (i = d.getIconAt(j)) != null) {
				d.setIconAt(j, setModifiedIcon(null, i));
				j++;
			}
		}
		
		//Update Listeners (app-specific)
		notifyListeners();
	}
	
	/**
	 * Scales the icon(s) and icon-related attributes of the three 
	 * icon-containing JComponents.<p>
	 * 
	 * <b>Only one should be non-null!</b>
	 * @param a AbstractButton
	 * @param b JLabel
	 * @param c JOptionPane
	 */
	private void modifyExternalIcon(AbstractButton a, JLabel b, JOptionPane c) {
		//skip illegalAgrumentsException, since only for internal use
				
		Icon i; //any icon
		
		if (a != null) {
			Icon disabled = null;
			Icon disabledSelected = null;
			Icon pressed = null;
			
			if ((i = a.getDisabledIcon()) != null && !i.equals(a.getIcon()))
				//save it (setIcon nullifies disabledIcon)
				disabled = i;
			
			if ((i = a.getDisabledSelectedIcon()) != null &&
					!i.equals(a.getIcon()))
				//again save it!
				disabledSelected = i;
			
			if ((i = a.getPressedIcon()) != null && !i.equals(a.getIcon()))
				//again save it!
				pressed = i;
			
			if ((i = a.getIcon()) != null)
				a.setIcon(setModifiedIcon(null, i));
			
			if ((i = a.getRolloverIcon()) != null && !i.equals(a.getIcon()))
				a.setRolloverIcon(setModifiedIcon(null, i));
			
			if ((i = a.getRolloverSelectedIcon()) != null && !i.equals(a.getIcon()))
				a.setRolloverSelectedIcon(setModifiedIcon(null, i));
			
			if ((i = a.getSelectedIcon()) != null && !i.equals(a.getIcon()))
				a.setSelectedIcon(setModifiedIcon(null, i));
			
			if (disabled != null)
				a.setDisabledIcon(setModifiedIcon(null, disabled));
			
			if (disabledSelected != null)
				a.setDisabledSelectedIcon(setModifiedIcon(null, disabledSelected));
			
			if (pressed != null)
				a.setPressedIcon(setModifiedIcon(null, pressed));

			a.setIconTextGap((int) (a.getIconTextGap() * scaleFactor));	
			
			a.validate();
			
		} else if (b != null) {
			Icon disabled = null;
			
			if ((i = b.getDisabledIcon()) != null && !i.equals(b.getIcon()))
				//see above
				disabled = i;
			
			if ((i = b.getIcon()) != null)
				b.setIcon(setModifiedIcon(null, i));
			
			if (disabled != null)
				b.setDisabledIcon(setModifiedIcon(null, disabled));
			
			b.setIconTextGap((int) (b.getIconTextGap() * scaleFactor));	
			
			b.validate();
		} else { //c != null
			if ((i = c.getIcon()) != null)
				c.setIcon(setModifiedIcon(null, i));
			
			c.validate();
		}
	}
}