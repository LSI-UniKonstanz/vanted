package org.vanted.scaling.scalers.component;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.vanted.scaling.DPIHelper;
import org.vanted.scaling.Toolbox;

/**
 * Extension of {@linkplain ComponentScaler}, responsible for Window-derived
 * components' decorations scaling.
 * 
 * @author dim8
 *
 */
public class WindowScaler extends ComponentScaler {

	private static final float INITIAL_UNSET = -1f;
	
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
	 * Doesn't override {@link ComponentScaler#coscaleIcon(javax.swing.JComponent)}!
	 * 
	 * @see {@linkplain DPIHelper#adjustWindowDecoratations()}
	 *  
	 * @param immediateComponent to be scaled
	 */
	public void scaleComponent(Component immediateComponent) {
		this.coscaleIcon(immediateComponent);
		resizeWindow(immediateComponent);
	}
	
	/**
	 * Scales JFrame and JDialog icons.
	 * 
	 * Doesn't override {@link ComponentScaler#coscaleIcon(javax.swing.JComponent)}!
	 * 
	 * @param component
	 */
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

	/**
	 * Worker mehtod for {@link WindowScaler#coscaleIcon(Component)}.
	 * 
	 * @param li images list
	 * @return list of newly scaled Image instances
	 */
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
	
	private static float previousRatio = INITIAL_UNSET;
	private static ArrayList<Integer> scaledWindows = new ArrayList<>();
	
	/**
	 * Specialized implementation of the {@linkplain AWTEventListener} that resizes any new window.
	 * One notable exception are HeavyWeightWindows, whose scaling is part of the normal DPI emulating
	 * cycle. 
	 * 
	 * @author dim8
	 *
	 */
	private static class WindowResizerListener implements AWTEventListener {
		
		@Override
		public void eventDispatched(AWTEvent event) {
			if (Toolbox.getDPIScalingRatio() == 1f && WindowScaler.getPreviousRatio() == INITIAL_UNSET)
				return;
			
			switch (event.getID()) {
				case WindowEvent.WINDOW_OPENED:
					resizeWindow((Window) event.getSource(), false);
					break;
				case WindowEvent.WINDOW_CLOSED:
					//nothing
					break;
			}
		}
	}
	
	/**
	 * Attach a WindowResizerListener to the default system window Toolkit.
	 */
	public static void attachSystemWindowResizer() {
		Toolkit.getDefaultToolkit().addAWTEventListener(new WindowResizerListener(), AWTEvent.WINDOW_EVENT_MASK);
	}
	
	/**
	 * Resizes window components. If the window is not scaling-persistent then
	 * its actual scaled size is adjusted by scale-back value, meaning it is
	 * scaled only up to a given percentage out of the full scaling factor.
	 * Default system windows (through usage of 
	 * {@link WindowScaler#attachSystemWindowResizer()}) are set to be impersistent,
	 * e.g. dialogs.
	 * 
	 * @param window to be resized
	 * @param isPersistent if the window instance would persist through and get rescaled
	 */
	public static void resizeWindow(Component window, boolean isPersistent) {
		if (!isWindowScalable(window))
			return;

		float scalingFactor = Toolbox.getDPIScalingRatio();
		if (previousRatio == INITIAL_UNSET)
			previousRatio = Toolbox.getDPIScalingRatio();
		//Window already scaled at least once (w/ previous factor)
		else if (scaledWindows.contains(window.hashCode())
				//and the new scaling factor hasn't been adjusted yet
				&& (Toolbox.getDPIScalingRatio() / previousRatio != 1)) {
			//if scaleback is used for re-scalable windows too, should be here
			scalingFactor = Toolbox.getDPIScalingRatio() / previousRatio;
			previousRatio = Toolbox.getDPIScalingRatio();
		}
		
		float scaleback = (isPersistent) ? 1 :
			getScalebackFactor(Toolbox.getDPIScalingRatio());
		
		
		Dimension size = window.getSize();
		size.setSize(
				Math.round(size.width * (scalingFactor * scaleback)),
				Math.round(size.height * (scalingFactor * scaleback)));
		window.setSize(size);
		
		//avoid adding dialog pop-ups and duplicates
		if (isPersistent && !scaledWindows.contains(window.hashCode()))
			scaledWindows.add(window.hashCode());
		
		window.invalidate();
		window.repaint();
	}

	/**
	 * Resize children components of the main container, given there are some.
	 * Currently not part of the central scaling routine.
	 * 
	 * @param window component to scaler
	 */
	private void resizeWindow(Component window) {
		if (!isWindowScalable(window))
			return;
		
		Dimension size = window.getSize();
		size.setSize(
				Math.round(size.width * scaleFactor),
				Math.round(size.height * scaleFactor));
		window.setSize(size);
		
		window.invalidate();
		window.repaint();		
	}

	/**
	 * Certain windows do not tolerate resizing, therefore those remain
	 * non-scaled.
	 * 
	 * @param window to check onto
	 * @return true, if not a heavy weight window
	 */
	private static boolean isWindowScalable(Component window) {
		return !window.getClass().getSimpleName().endsWith("HeavyWeightWindow");
	}
	
	/**
	 * @return last most recently used scaling ratio/factor.
	 */
	public static float getPreviousRatio() {
		return previousRatio;
	}
	
	/**
	 * Returns suitable scaleback-value for window scaling.
	 * Should be called once per overall scaling change, not per window.
	 *  
	 * @param ratio {@link Toolbox#getDPIScalingRatio()}
	 * @return 1 or {@link WindowScaler#SCALEBACK_DECREASE}
	 * 			 or {@link WindowScaler#SCALEBACK_INCREASE}
	 */
	private static float getScalebackFactor(float ratio) {
		if (ratio == previousRatio) {
			if (ratio > 1)
				return SCALEBACK_DECREASE;
			else if (ratio < 1)
				return SCALEBACK_INCREASE;
			else
				return 1;
		} else { //currently unused, because we scale back only impersistent windows (dialog-like)
			if (ratio > previousRatio) {
				previousRatio = ratio;
				return SCALEBACK_DECREASE;
			} else if (ratio < previousRatio) {
				previousRatio = ratio;
				return SCALEBACK_INCREASE;
			} else
				return 1;
		}

	}
	
	/** Used to scale windows' width and height back, since the actual scale by itself is too much. */
	private static final float SCALEBACK_PERCENTAGE = .7f;// percentage to scale back with
	private static final float SCALEBACK_DECREASE = SCALEBACK_PERCENTAGE; // higher scaling, ergo smaller scaleback
	private static final float SCALEBACK_INCREASE = 1 / SCALEBACK_DECREASE; //lower scaling, ergo bigger scaleback		
}
