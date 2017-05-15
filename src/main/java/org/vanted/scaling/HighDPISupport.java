package org.vanted.scaling;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JLabel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.options.PreferencesInterface;
import org.graffiti.plugin.parameter.JComponentParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

public class HighDPISupport implements PreferencesInterface {

	public static final String DESCRIPTION
	= "<html>Emulate your desired DPI by moving the slider.<br>"
			+ "<i>Lifesaver</i> saves you from incautiously setting unhealthy values. It acts on subsequent start-up.<br>"
			+ "Mac OS X Look and Feel is very restrictive, so for better perfromance choose more tolerant<br>"
			+ "alternative. Lastly, for optimal performance you could restart VANTED.<br><br>";
	
	private static final String PREFERENCES_MAC_LAF = "<html>Mac Look and Feel&emsp;";
	
	private static final String QUAQUA = "Quaqua Look and Feel";
	
	
	public HighDPISupport() {}

	@Override
	public List<Parameter> getDefaultParameters() {
		List<Parameter> params = new ArrayList<Parameter>();
		
		params.add(getInformation());
		//here come the slider and lifesaver, added in ParameterOptionPane
		params.add(getMaclaf());
		
		return params;
	}

	@Override
	public void updatePreferences(Preferences preferences) {
		String laf = preferences.get(PREFERENCES_MAC_LAF, null);
		if (laf != null) {
			switch(laf) {
				case QUAQUA:
								setLAF(ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel());
								break;
				case "Mac OS X":
								setLAF("apple.laf.AquaLookAndFeel");
								break;
				case "Windows":
								setLAF("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
								break;
				case "Metal":
								setLAF("javax.swing.plaf.metal.MetalLookAndFeel");
								break;
				case "CDE/Motif":
								setLAF("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
								break;
				case "Windows Classic":
								setLAF("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
								break;
				case "Nimbus":
								setLAF("javax.swing.plaf.nimbus.NimbusLookAndFeel");
								break;
				case "GTK+":
								setLAF("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
								break;
			}
			if (MainFrame.getInstance() != null) {
				if (ReleaseInfo.isRunningAsApplet())
					SwingUtilities.updateComponentTreeUI(ReleaseInfo.getApplet());
				else
					SwingUtilities.updateComponentTreeUI(MainFrame.getInstance());
				MainFrame.getInstance().repaint();
			}
				
		}
	}

	@Override
	public String getPreferencesAlternativeName() {
		return "<html>High DPI Support<sup>BETA</sup></html>";
	}
	
	private void setLAF(LookAndFeel laf) {
		try {
			UIManager.setLookAndFeel(laf);
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}
	
	private void setLAF(String laf) {
		try {
			UIManager.setLookAndFeel(laf);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	private JComponentParameter getInformation() {
		JLabel text = new JLabel();
		text.setVisible(false);
		JComponentParameter information = new JComponentParameter(text, "", DESCRIPTION);
		information.setLeftAligned(true);
		
		return information;
	}
	
	private ObjectListParameter getMaclaf() {
		String description = "<html><br><i>Please, use only on Mac!</i><br><br>";
		LookAndFeel current = UIManager.getLookAndFeel();
		
		if (current != ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel())
			return new ObjectListParameter(current, PREFERENCES_MAC_LAF, description,
					new Object[] {current.getName(), QUAQUA});
		else
			return new ObjectListParameter(current, PREFERENCES_MAC_LAF, description,
					new Object[] {QUAQUA, UIManager.getSystemLookAndFeelClassName().getClass().getSimpleName()});
	}
}
