package org.vanted.scaling;

import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.Collections;

import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
/**
 * It coordinates the scaling among the possible L&Fs.
 * Through delegation the main 3 types are ordered and
 * then accordingly executed.
 *   
 * @author dim8
 *
 */
public class ScaleCoordinator {
	
	private boolean isNimbus = false;
	
	public ScaleCoordinator(float factor) {
		scaleDefaults(factor);
		//update GUI
		refreshGUI(MainFrame.getInstance());
		
	}
	
	public void scaleDefaults(float factor) {
		float dpiScale = Toolkit.getDefaultToolkit().getScreenResolution() / factor;
		Scaler delegate = createScalerForCurrentLAF(dpiScale);
		  
		adjustDefaults(delegate, dpiScale);
	}

	private Scaler createScalerForCurrentLAF(float dpiScaling) {
		String testString = UIManager.getLookAndFeel().getName().toLowerCase();
		
		if (testString.contains("windows"))
			return new WindowsScaler(dpiScaling);
		if (testString.contains("nimbus")) {
			isNimbus = true;
			return new NimbusScaler(dpiScaling);
		}
		
		return new BasicScaler(dpiScaling);
	}

	private void adjustDefaults(Scaler delegate, float multiplier) {
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

	private Object getScaledValue(Scaler delegate, Object key, Object original) {
		if (original instanceof Font)
			return delegate.modifyFont(key, (Font) original);

		if (original instanceof Icon)
			return delegate.modifyIcon(key, (Icon) original);
		  
		if (original instanceof Integer)
			return delegate.modifyInteger(key, (Integer) original);
		
		if (original instanceof Insets)
			return delegate.modifyInsets(key, (Insets) original);
		
		//sentinel for non-scalable value
		return null;
	}
	
	private void refreshGUI(final Component mainFrame) {
		if (mainFrame != null)
			//repaint() is thread-safe only on OpenJDK, therefore 
			//we cannot rely on that and call EDT explicitly		
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {	
					if (ReleaseInfo.isRunningAsApplet())
						SwingUtilities.updateComponentTreeUI(ReleaseInfo.getApplet());
					else
						SwingUtilities.updateComponentTreeUI(MainFrame.getInstance());

					//We switch on/off the frame to refresh Tabs when using Nimbus
					if (isNimbus) {
						mainFrame.setVisible(false);
						isNimbus = false;
						mainFrame.repaint();
						mainFrame.revalidate();	
						mainFrame.setVisible(true);
					} else {
						mainFrame.repaint();
						mainFrame.revalidate();
					}
						
				}
			});
	}
}