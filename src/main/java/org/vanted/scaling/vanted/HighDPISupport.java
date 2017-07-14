package org.vanted.scaling.vanted;

import java.io.File;
import java.lang.reflect.Field;
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
import org.vanted.scaling.DPIHelper;
import org.vanted.scaling.ScalerLoader;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.zoomfit.ZoomFitChangeComponent;

public class HighDPISupport implements PreferencesInterface {
	
	private static final boolean IS_MAC = System.getProperty("os.name")
			.toLowerCase().contains("mac");
	private static final boolean DEV = System.getProperty("java.class.path")
			.contains("target" + File.separator + "classes");
	
	private final static String macText = (IS_MAC || DEV) ? "Mac Look and Feel is restrictive,"
			+ " so for superior performance choose more tolerant alternative.<br>" : "";

	public static final String DESCRIPTION
	= "<html>&#10148; Emulate your desired DPI by moving the slider.<br>"
			+ "&#10148; <i>Lifesaver</i> guards you against bad values. It acts upon subsequent start-up.<br>"
			+ "&#10148; " + macText
			+ "&#10148; Lastly, for optimal performance you could restart VANTED.<br><br>";
	
	public static final String PREFERENCES_MAC_LAF = "<html>Mac Look and Feel&emsp;";
	
	private static final String description = "  (Please, use only on Mac!)";
	private static final String QUAQUA = "Quaqua Look and Feel" + description;
	
	private final LookAndFeel quaqua = ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel();
	
	private HashMap<String, String> lafmap = new HashMap<>();
	private Preferences general;
	private static String activeLaf;
//	private static boolean startup = true;
	
	public HighDPISupport() {
		general = PreferenceManager.getPreferenceForClass(VantedPreferences.class);
		prepareLafMap();			
	}

	@Override
	public List<Parameter> getDefaultParameters() {
		List<Parameter> params = new ArrayList<Parameter>();
		
		if (UIManager.getLookAndFeel().getClass().getCanonicalName().contains("GTK")) {
			params.add(getAlternativeInformation());
			
			return params;
		}
		
		params.add(getInformation());

		//here come the slider and lifesaver, added in ParameterOptionPane
		
		/**
		 * We enable S-Quaqua only on Mac and, of course, while developing,
		 * because of copyright reasons on behalf of Apple, Inc. 
		 */
		if (IS_MAC || DEV)
			params.add(getMacLaf());
		
		return params;
	}

	@Override
	public void updatePreferences(Preferences preferences) {
		if (getClassInstance(ZoomFitChangeComponent.class) == null) {
			GraphScaler.setOldValueZooming(DPIHelper.managePreferences(
					DPIHelper.VALUE_DEFAULT, DPIHelper.PREFERENCES_GET));
			//load the zoomer once on initial update
			new GraphScaler();
		}
		
		GraphScaler.readdChangeListener();
		
		/**
		 * Mac LAF update, if necessary. */
		String laf = preferences.get(PREFERENCES_MAC_LAF, null);
		
		if (laf == null || laf.equals(activeLaf)) //no LAF change
			return;
		
		if (laf.equals(QUAQUA))
			setLAF(quaqua);
		else
			setLAF(queryLafMap(laf, true));

		//update main preferences
		general.put(VantedPreferences.PREFERENCE_LOOKANDFEEL, queryLafMap(laf, true));
		//scale new LAF
		ScalerLoader.doScaling(false);

		if (MainFrame.getInstance() != null) {
			if (ReleaseInfo.isRunningAsApplet())
				SwingUtilities.updateComponentTreeUI(ReleaseInfo.getApplet());
			else
				SwingUtilities.updateComponentTreeUI(MainFrame.getInstance());
			MainFrame.getInstance().repaint();
		}
	}

	@Override
	public String getPreferencesAlternativeName() {
		return "<html>High DPI Support<sup>BETA</sup></html>";
	}
	
	/**
	 * Through reflection we get an instance of the specified class, given
	 * there is such static field defined.
	 * @param clazz the Class to extract the instance from
	 * @return a clazz instance
	 */
	public static Object getClassInstance(Class <?> clazz) {
		Object instance = null;
		try {
			Field f = clazz.getDeclaredField("instance");
			f.setAccessible(true);
			instance = f.get(null);
		} catch (NoSuchFieldException | SecurityException |
				IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return instance;
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

	private JComponentParameter getAlternativeInformation() {
		String disabled = UIManager.getLookAndFeel().getName();
		JLabel text = new JLabel();
		text.setVisible(false);
		return new JComponentParameter(text, "", "<html>High DPI Support is not available for <b>" + disabled + "</b>!<br>"
				+ "Please, consider switching to another look and feel to use this feature.");
	}
	
	private ObjectListParameter getMacLaf() {
		String defaultval = general.get(VantedPreferences.PREFERENCE_LOOKANDFEEL, 
				UIManager.getLookAndFeel().getClass().getName());
		String name = "";
		Object[] values = new Object[2];
		
		if (defaultval.toLowerCase().contains("quaqua")) {
			name = QUAQUA;
			values = new Object[] {QUAQUA, getDefaultSystemLAFname()};
			activeLaf = QUAQUA;
		} else {
			Preferences prefs = DPIHelper.loadPreferences(this.getClass());
			
			if (lafmap.containsValue(defaultval))
				name = queryLafMap(defaultval, false);
			else {
				name = mapLAFClassToName(defaultval);			
				lafmap.put(name, defaultval);
			}
			
			prefs.put(PREFERENCES_MAC_LAF, name);
			values = new Object[] {name, QUAQUA};
			activeLaf = name;
		}	
		
		return new ObjectListParameter(name, PREFERENCES_MAC_LAF, "",
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
		if (IS_MAC)
			return "Mac OS X";
		if (System.getProperty("os.name").toLowerCase().contains("windows"))
			return "Windows";
		else 
			return "Metal";
	}
	
	private String mapLAFClassToName(String clazz) {
		String name = clazz.substring(clazz.lastIndexOf('.') + 1);
		name = trimLAFEnding(name);
		name = splitAtUppercase(name, " ");
		
		return name;
	}
	
	private String trimLAFEnding(String clazz) {
		if (clazz.endsWith("LookAndFeel"))
			return clazz.replaceAll("LookAndFeel\\b", "");
		else
			return clazz;
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

//	
//	public static void loadWhitelistedContainers() {
//		if (!startup) //ensures one-time only functionality
//			return;
//		
//		startup = false;
//		Collection<Class<? extends Container>> parents = new ArrayList<>();
//
//		//add here
//		
//		DPIHelper.setWhitelisted(parents);
//	}
}
