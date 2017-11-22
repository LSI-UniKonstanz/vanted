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
import org.vanted.scaling.scaler.BasicScaler;
import org.vanted.scaling.scaler.NimbusScaler;
import org.vanted.scaling.scaler.Scaler;
import org.vanted.scaling.scaler.WindowsScaler;

/**
 * It coordinates the scaling among the possible L&Fs and initiates Components-
 * scaling. Through delegation the main types are ordered and then accordingly
 * scaled. First LAF-Defaults, afterwards all the user-set resizable components.<p>
 * 
 * Big Scale Operations (e.g. DPI-Scale factor of 50) require additional heap space!
 *   
 * @author dim8
 *
 */
public class ScalingCoordinator {
	
	//for better refresh of Nimbus
	private static boolean isNimbus = false;
	
	/**
	 * 
	 * @param factor the processed Slider value
	 * @param main the main container
	 */
	public ScalingCoordinator(float factor, Container main) {
		//scale all defaults
		scaleDefaults(factor);
		//scale all components
		scaleComponents(factor, main);
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
		int value = DPIHelper.managePreferences(DPIHelper.VALUE_DEFAULT,
				DPIHelper.PREFERENCES_GET);
		float factor = DPIHelper.processEmulatedDPIValue(value);
		
		//scale all defaults
		scaleDefaults(factor);
		//scale all components
		scaleComponents(factor, main);
		//update GUI
		refreshGUI(main);
	}
	
	/**
	 * 
	 * @param main the main container
	 * @param components <b>false</b>: scale LAF Defaults only
	 */
	public ScalingCoordinator(Container main, boolean components) {
		int value = DPIHelper.managePreferences(DPIHelper.VALUE_DEFAULT,
				DPIHelper.PREFERENCES_GET);
		float factor = DPIHelper.processEmulatedDPIValue(value);
		
		//scale all defaults
		scaleDefaults(factor);
		
		if (components)
			scaleComponents(factor, main);
		
		//update GUI
		refreshGUI(main);
	}
	
	/**
	 * Empty Coordinator. You should refresh the GUI thereafter by yourself.
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
	 *  <b>Important Note:</b> To scale any added defaults, these must be
	 *  added to the {@link UIManager#getLookAndFeelDefaults()}.
	 * 
	 * @param delegate
	 */
	public void adjustDefaults(Scaler delegate) {
		/*
		 * We use getLookAndFeelDefaults() instead of just getDefaults(),
		 * because the developer defaults contain doubled references and
		 * thus result in doubled scaling. */
		UIDefaults defaults = UIManager.getLookAndFeelDefaults();
		
		delegate.initialScaling();

		for (Object key : Collections.list(defaults.keys())) {
			Object original = defaults.get(key);
			Object newValue = getScaledValue(delegate, key, original);

			//update with new value
			if (newValue != null && newValue != original)
				defaults.put(key, newValue);
		}
	}
	
	/**
	 * Scales JComponents.
	 * 
	 * @param factor the scaling ratio from system DPI & requested DPI
	 */
	public void scaleComponents(float factor, Container main) {
		float dpiRatio = Toolkit.getDefaultToolkit().getScreenResolution() / factor;
		ComponentRegulator regulator = new ComponentRegulator(dpiRatio);
		
		regulator.init(main);
	}
	
	/**
	 * Transforms the <code>original</code> LAf-Default using the dispatched
	 * <code>delegate</code> Scaler.
	 * 
	 * @param delegate appropriate scaler
	 * @param key the therewith associated defaults key
	 * @param original to be scaled object
	 * 
	 * @return newly scaled LAF-Defaults Object
	 */
	private Object getScaledValue(Scaler delegate, Object key, Object original) {
		if (original instanceof Font)
			return delegate.modifyFont(key, (Font) original);

		if (original instanceof Icon)
			return delegate.modifyIcon(key, (Icon) original);
		  
		if (original instanceof Integer)
			return delegate.modifyInteger(key, (Integer) original);
		
		if (original instanceof Insets)
			return delegate.modifyInsets((Insets) original);
		
		//sentinel for non-scalable values
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