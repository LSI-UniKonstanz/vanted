package org.vanted.scaling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.JLabel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.managers.PreferenceManager;
import org.graffiti.options.PreferencesInterface;
import org.graffiti.plugin.parameter.JComponentParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.vanted.VantedPreferences;

public class HighDPISupport implements PreferencesInterface {

	public static final String DESCRIPTION
	= "<html>Emulate your desired DPI by moving the slider.<br>"
			+ "<i>Lifesaver</i> saves you from incautiously setting bad values. It acts on subsequent start-up.<br>"
			+ "Mac OS X Look and Feel is very restrictive, so for better perfromance choose more tolerant<br>"
			+ "alternative. Lastly, for optimal performance you could restart VANTED.<br><br>";
	
	private static final String PREFERENCES_MAC_LAF = "<html>Mac Look and Feel&emsp;";
	
	private static final String QUAQUA = "Quaqua Look and Feel";
	private final LookAndFeel quaqua = ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel();
	
	private HashMap<String, String> lafmap = new HashMap<>();
	
	private Preferences general;
	
	
	public HighDPISupport() {
		general = PreferenceManager.getPreferenceForClass(VantedPreferences.class);
		prepareLafMap();
	}

	@Override
	public List<Parameter> getDefaultParameters() {
		List<Parameter> params = new ArrayList<Parameter>();
		
		params.add(getInformation());
		//here come the slider and lifesaver, added in ParameterOptionPane
		params.add(getMacLaf());
		
		return params;
	}

	@Override
	public void updatePreferences(Preferences preferences) {
		String laf = preferences.get(PREFERENCES_MAC_LAF, null);
		if (laf != null) {
			if (laf.equals(QUAQUA))
				setLAF(quaqua);
			else
				setLAF(queryLafMap(laf, true));
			
			general.put(VantedPreferences.PREFERENCE_LOOKANDFEEL, queryLafMap(laf, true));

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
	
	private ObjectListParameter getMacLaf() {
		String description = "<html><br><i>Please, use only on Mac!</i><br><br>";
		String defaultval = general.get(VantedPreferences.PREFERENCE_LOOKANDFEEL, 
				UIManager.getLookAndFeel().getClass().getName());
		String name = "";
		Object[] values = new Object[2];
		
		if (UIManager.getLookAndFeel().getName().toLowerCase().contains("quaqua"))
			name = QUAQUA;
		
		if (name.isEmpty())
			if (lafmap.containsValue(defaultval))
				name = queryLafMap(defaultval, false);
			else {
				name = defaultval.substring(defaultval.lastIndexOf('.') + 1);
				name = splitAtUppercase(name, " ");				
				lafmap.put(name, defaultval);
			}
			
		
		if (!name.equals(QUAQUA))
			values = new Object[] {name, QUAQUA};
		else
			values = new Object[] {QUAQUA, getDefaultSystemLAFname()};
		
		return new ObjectListParameter(name, PREFERENCES_MAC_LAF, description,
										values);
	}
	
	private void prepareLafMap() {
		lafmap.put(QUAQUA, "ch.randelshofer.quaqua.QuaquaLookAndFeel");
		lafmap.put("Mac OS X", "apple.laf.AquaLookAndFeel");
		lafmap.put("Windows", "com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		lafmap.put("Metal", "javax.swing.plaf.metal.MetalLookAndFeel");
		lafmap.put("CDE/Motif", "com.sun.java.swing.plaf.motif.MotifLookAndFeel");
		lafmap.put("Windows Classic", "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
		lafmap.put("Nimbus", "javax.swing.plaf.nimbus.NimbusLookAndFeel");
		lafmap.put("GTK+", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
	}
	
	private String queryLafMap(String param, boolean key) {
		if (key) { //querying acc. to key, get value
			return lafmap.get(param);
		} else { //param is the value, get key
			if (!lafmap.containsValue(param))
				return null;
			else {
				for (Entry<String, String> entry : lafmap.entrySet())
					if (entry.getValue().equals(param))
						return entry.getKey();
				
				return null;
			}
		}
	}
	
	private String getDefaultSystemLAFname() {
		if (System.getProperty("os.name").toLowerCase().contains("mac"))
			return "Mac OS X";
		if (System.getProperty("os.name").toLowerCase().contains("windows"))
			return "Windows";
		else 
			return "Metal";
	}
	
	private String splitAtUppercase(String name, String delimeter) { 
		String newName = "";
		int j = 0;
		for (int i = 1; i < name.length(); i++)
			if (Character.isUpperCase(name.charAt(i))) {
				newName += name.substring(j, i) + delimeter;
				j = i;
			}
		
		newName += name.substring(j);
		
		return newName;
	}
}
