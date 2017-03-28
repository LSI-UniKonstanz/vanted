package org.vanted.scaling;

import java.awt.Container;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.Collections;

import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.ReleaseInfo;

/**
 * It coordinates the scaling among the possible L&Fs. Through delegation
 * the main types are ordered and then accordingly executed. Firstly, LAF-
 * Defaults are scaled, afterwards all user-set resizable components.  <p>
 * 
 * Big Scale Operations (e.g. DPI-Scale factor of 50) require additional heap space!
 *   
 * @author dim8
 *
 */
public class ScalingCoordinator {
	
	//for better refresh of Nimbus
	private static boolean isNimbus = false;
	
	/** The main Container, containing all others. */
	private Container main;
	
	/**
	 * 
	 * @param factor the processed Slider value
	 * @param main the main container
	 */
	public ScalingCoordinator(float factor, Container main) {
		this.main = main;
		//scale all defaults
		scaleDefaults(factor);
		//update GUI
		refreshGUI(main);
	}
	
	/**
	 * This uses the preferences-stored factor. Suitable for
	 * initial modifications.
	 * 
	 * @param main the main container
	 */
	public ScalingCoordinator(Container main) {
		this.main = main;		
		
		int value = ScalingSlider.managePreferences(-1, true);
		float factor = ScalingSlider.processSliderValue(value);
		
		//scale all defaults
		scaleDefaults(factor);
		//update GUI
		refreshGUI(main);
	}
	
	/**
	 * You should supposedly collect your garbage yourself, unlike with the
	 * other constructors.
	 */
	public ScalingCoordinator() {}
	
	/**
	 * Scaling Look & Feel Defaults.
	 * 
	 * @param factor the required new DPI Factor, from which a scaling ratio
	 * would be constructed. 
	 */
	public void scaleDefaults(float factor) {
		float dpiRatio = Toolkit.getDefaultToolkit().getScreenResolution() / factor;
		Scaler delegate = createScalerForCurrentLAF(dpiRatio);
		  
		adjustDefaults(delegate);
		
		adjustUserComponents(dpiRatio, main);
	}
	
	/**
	 * Construct LAF-Scaler.
	 * 
	 * @param dpiRatio 
	 * @return the adequate type of LAF-Scaler
	 */
	private Scaler createScalerForCurrentLAF(float dpiRatio) {
		String testString = UIManager.getLookAndFeel().getName().toLowerCase();
		
		if (testString.contains("windows"))
			return new WindowsScaler(dpiRatio);
		if (testString.contains("nimbus")) {
			isNimbus = true;
			return new NimbusScaler(dpiRatio);
		}
		
		return new BasicScaler(dpiRatio);
	}

	/**
	 * Scan Defaults and re-scale the appropriate values. 
	 * 
	 * @param delegate
	 */
	public void adjustDefaults(Scaler delegate) {
		UIDefaults defaults = UIManager.getLookAndFeelDefaults();
		
		delegate.initialScaling();

		for (Object key: Collections.list(defaults.keys())) {
			Object original = defaults.get(key);
			Object newValue = getScaledValue(delegate, key, original);

			//update with new value
			if (newValue != null && newValue != original)
				defaults.put(key, newValue);
		}
	}
	
	/**
	 * Scaler for external, i.e. user-set, components.
	 * 
	 * @param dpiRatio the scaling ratio from system DPI & requested DPI
	 */
	public void adjustUserComponents(float dpiRatio, Container main) {
		XScaler external = new XScaler(dpiRatio);
		external.init(main);
	}
	
	private Object getScaledValue(Scaler delegate, Object key, Object original) {
		if (original instanceof Font)
			return delegate.modifyFont(key, (Font) original);

		if (original instanceof Icon)
			return delegate.getModifiedIcon(key, (Icon) original);
		  
		if (original instanceof Integer)
			return delegate.modifyInteger(key, (Integer) original);
		
		if (original instanceof Insets)
			return delegate.getModifiedInsets((Insets) original);
		
		//sentinel for non-scalable value
		return null;
	}
	
	/**
	 * Call after each major scaling operation. 
	 * 
	 * @param main the main container
	 */
	public static void refreshGUI(final Container main) {
		if (main != null)
			/**
			 * repaint() is thread-safe only on OpenJDK, therefore 
			 * we cannot rely on that and call EDT explicitly.		
			 */
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {	
					if (ReleaseInfo.isRunningAsApplet())
						SwingUtilities.updateComponentTreeUI(ReleaseInfo.getApplet());
					else
						SwingUtilities.updateComponentTreeUI(main);

					//We switch on/off the frame to refresh Tabs when using Nimbus
					if (isNimbus) {
						main.setVisible(false);
						isNimbus = false;
						main.repaint();
						main.revalidate();	
						main.setVisible(true);
					} else {
						main.repaint();
						main.revalidate();
					}
						
				}
			});
		
		//collect trash
		System.gc();
	}
}