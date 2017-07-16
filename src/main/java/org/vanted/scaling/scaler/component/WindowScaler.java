package org.vanted.scaling.scaler.component;

import java.awt.Component;
import java.awt.Image;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.vanted.scaling.DPIHelper;

/**
 * Extension of {@linkplain ComponentScaler}, responsible for Window-derived
 * components' decorations scaling. 
 * 
 * @author dim8
 *
 */
public class WindowScaler extends ComponentScaler {

	public WindowScaler(float scaleFactor) {
		super(scaleFactor);
	}

	/**
	 * A method to be called when this {@linkplain WindowScaler} has been
	 * dispatched to some immediate Component to be scaled.<p>
	 * 
	 * <b>Note:</b> Use this only when JFrame or JDialog, are *not* LookAndFeel decorated.
	 * Otherwise, this has no effect (Metal LookAndFeel tested).
	 * 
	 * @see {@linkplain DPIHelper#adjustWindowDecoratations()}
	 *  
	 * @param immediateComponent to be scaled
	 */
	public void scaleComponent(Component immediateComponent) {
		this.coscaleIcon(immediateComponent);
	}
	
	public void coscaleIcon(Component component) {
			if (component instanceof JFrame) {
				JFrame frame = (JFrame) component;
				List<Image> li = frame.getIconImages();
				frame.setIconImages(scaleIconImages(li));
			} else if (component instanceof JDialog) {
				JDialog dialog = (JDialog) component;
				List<Image> li = dialog.getIconImages();
				dialog.setIconImages(scaleIconImages(li));
			}
	}

	private List<Image> scaleIconImages(List<Image> li) {
		final int size = li.size();
		for (int i = 0; i < size; i++) {
			Image im = li.get(i);
			li.remove(i);
			li.add(i, ((ImageIcon) modifyIcon(null,
					new ImageIcon(im))).getImage());
		}
		
		return li;
	}
	
}
