package org.vanted.scaling.scaler.component;

import java.awt.Font;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.vanted.scaling.resources.ScaledFontUIResource;
import org.vanted.scaling.scaler.BasicScaler;

/**
 * Applicable to initiated Swing Components. It scales specifics that
 * have been overwritten by design only, making sure LAF-Defaults are not scaled
 * again. So, it is a kind of co-scaler to any of the LAF-Scalers.<p>
 * 
 * Current (default) subtypes:<br><br>
 * 
 * {@link AbstractButtonScaler}<br>
 * {@link JLabelScaler}<br>
 * {@link JOptionPaneScaler}<br>
 * {@link JTabbedPaneScaler}<br>
 * {@link JTextComponentScaler}<br>
 * 
 * @author dim8
 *
 */
public class ComponentScaler extends BasicScaler {

	public ComponentScaler(float scaleFactor) {
		super(scaleFactor);
	}
	
	/**
	 * Scales all components that have their font not modified by the
	 * LAF-Scalers for one reason or another up to this point.
	 * 
	 * @param component the JComponent, whose Font is to be scaled
	 */
	public void coscaleFont(JComponent component) {
		/**
		 * We know that instances of ScaledFontUIResource are already
		 * processed. If not we compare the DPIs to make a conclusion.
		 * Then we modify accordingly.
		 */
		Font font = component.getFont();
		
		if (!(font instanceof ScaledFontUIResource) ||
				!((ScaledFontUIResource) font).isScaledWith(scaleFactor))
			component.setFont(modifyFont(null, font));
	}

	/**
	 * Modifies non-null Insets of JComponent.
	 *  
	 * @param component the JComponent, whose Insets are to be scaled
	 *
	 */
	public void coscaleInsets(JComponent component) {
		/** Check if Insets need re-scaling. */
		if (component.getInsets().hashCode() == 0 ||
				/** Motif doesn't react well to Insets scaling, because it 
				 *  already uses overly big Insets. */
				UIManager.getLookAndFeel().getName().equals("CDE/Motif"))
			return;
		
		Insets old;
		
		if (component.getBorder() != null) {
			old = component.getBorder().getBorderInsets(component);
			Insets newi = modifyInsets(old);
			Border empty = BorderFactory.createEmptyBorder(
					newi.top - old.top,
					newi.left - old.left,
					newi.bottom - old.bottom,
					newi.right - old.right);
			Border compound = BorderFactory.createCompoundBorder(
					component.getBorder(), empty);
			
			component.setBorder(compound);
		} else {
			Insets newi = modifyInsets(component.getInsets());
			Border empty = new EmptyBorder(newi);
			//reset border to modify insets
			component.setBorder(empty);
		}
	}
	
	/**
	 * Left to subtypes. There are implementations for the following JComponents:<p>
	 * 
	 * <b>AbstractButton</b>, <b>JLabel</b>, <b>JOptionPane</b>, <b>JTabbedPane</b>.
	 * 
	 * @param component Component having Icon
	 */	
	public void coscaleIcon(JComponent component) {
		//Left to icon-owning components
	}
}