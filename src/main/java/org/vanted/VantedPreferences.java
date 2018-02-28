package org.vanted;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import org.ReleaseInfo;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.managers.PreferenceManager;
import org.graffiti.options.PreferencesInterface;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.StringParameter;

import org.vanted.scaling.ScalerLoader;

/**
 * Global Preference class for VANTED. Every Preference regarding global
 * settings of VANTED should be put in here. As usual Classes implementing the
 * PreferencesInterface provide an array of parameters giving default values and
 * else get their values from the java.util.Preferences object representing this
 * class.
 * 
 * @author matthiak
 */
public class VantedPreferences implements PreferencesInterface {

	public static final String PREFERENCE_LOOKANDFEEL = "Look and Feel";
	public static final String PREFERENCE_PROXYHOST = "Proxy Host";
	public static final String PREFERENCE_PROXYPORT = "Proxy Port";

	public static final String PREFERENCE_SHOWALL_ALGORITHMS = "Show all (hidden) algortihms";

	public static final String PREFERENCE_DEBUG_SHOWPANELFRAMES = "Debug: Show GraphElement Panels";
	public static boolean PREFERENCE_DEBUG_SHOWPANELFRAMES_VALUE;
	public static final String PREFERENCE_STANDARD_SAVE_FILEFORMAT = "Standard save file format";

	private static VantedPreferences instance;

	public VantedPreferences() {

		/* Set any UI Constants before any scaling! */
		VantedPreferences.styleUIDefaults();

		/**
		 * Scale starting splash banner.
		 * 
		 * Additionally, re-scale Metal L&F, which does not update Preferences on
		 * start-up.
		 */
		ScalerLoader.init(MainFrame.getInstance(), MainFrame.class);
	}

	public static VantedPreferences getInstance() {
		if (instance == null)
			instance = new VantedPreferences();
		return instance;
	}

	@Override
	public List<Parameter> getDefaultParameters() {
		ArrayList<Parameter> params = new ArrayList<>();
		params.add(getLookAndFeelParameter());
		params.add(new StringParameter("", PREFERENCE_PROXYHOST, "Name or IP  of the proxy host"));
		params.add(new IntegerParameter(0, PREFERENCE_PROXYPORT, "Port number of the proxy"));
		params.add(new BooleanParameter(false, PREFERENCE_SHOWALL_ALGORITHMS,
				"Show algorithms, not normally shown, as they might confuse users with their sole functionality"));
		/*
		 * This only works, if Vanted has been started and all output file formats have
		 * been made available
		 */
		if (MainFrame.getInstance() != null) {
			Set<String> graphFileExtensions = MainFrame.getInstance().getIoManager().getGraphFileExtensions();
			String[] possibleValues = graphFileExtensions.toArray(new String[graphFileExtensions.size()]);
			params.add(new ObjectListParameter("", PREFERENCE_STANDARD_SAVE_FILEFORMAT,
					"Standard file format, that is selected for file saving", possibleValues));
		} else {
			String[] possibleValues = new String[] { "" };
			params.add(new ObjectListParameter("", PREFERENCE_STANDARD_SAVE_FILEFORMAT,
					"Standard file format, that is selected for file saving", possibleValues));

		}

		if (Logger.getRootLogger().getLevel() == Level.DEBUG)
			params.add(new BooleanParameter(false, PREFERENCE_DEBUG_SHOWPANELFRAMES,
					"For debugging purposes, show frames from each graph and attribute component."));

		return params;
	}

	@Override
	public void updatePreferences(Preferences preferences) {
		/**
		 * Handle look and feel parameter.
		 */
		final String lafName = preferences.get(PREFERENCE_LOOKANDFEEL, "");
		String selLAF = UIManager.getLookAndFeel().getClass().getCanonicalName();
		if (!selLAF.equals(lafName)) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					try {
						UIManager.setLookAndFeel(lafName);
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException e) {
						e.printStackTrace();
					}

					/**
					 * Scale only LAF Defaults again on a LAF-change.
					 */
					ScalerLoader.doScaling(false);

					/*
					 * show changes for current instance running This will only be executed when the
					 * preferences are updated using the Preferences Dialog
					 */
					if (MainFrame.getInstance() != null) {
						if (ReleaseInfo.isRunningAsApplet())
							SwingUtilities.updateComponentTreeUI(ReleaseInfo.getApplet());
						else
							SwingUtilities.updateComponentTreeUI(MainFrame.getInstance());
						MainFrame.getInstance().repaint();
					}

				}
			});
		}

		/**
		 * Handle proxy parameter.
		 */
		String proxyhostname = preferences.get(PREFERENCE_PROXYHOST, null);
		String proxyport = preferences.get(PREFERENCE_PROXYPORT, null);
		if (proxyhostname != null && proxyport != null && !proxyhostname.isEmpty()) {
			try {
				Integer portnumber = Integer.parseInt(proxyport);
				System.setProperty("http.proxySet", "true");
				System.setProperty("http.proxyHost", proxyhostname);
				System.setProperty("http.proxyPort", proxyport);

			} catch (NumberFormatException e) {
				System.setProperty("http.proxySet", "false");

			}
		} else {
			System.setProperty("http.proxySet", "false");
		}

		PREFERENCE_DEBUG_SHOWPANELFRAMES_VALUE = Boolean.valueOf(
				preferences.get(PREFERENCE_DEBUG_SHOWPANELFRAMES, "false"));
	}

	@Override
	public String getPreferencesAlternativeName() {
		return "Vanted Preferences";
	}

	/**
	 * retrieves all available LookandFeels from the system and creates a new
	 * ObjectListparameter object, that will be used to display a JComboBox
	 * 
	 * @return
	 */
	private ObjectListParameter getLookAndFeelParameter() {

		ObjectListParameter objectlistparam;
		Object[] possibleValues;

		String canonicalName = UIManager.getLookAndFeel().getClass().getCanonicalName();

		// check if this is the first start and there is no preference.. then set the
		// default look and feel accordingly
		if (PreferenceManager.getPreferenceForClass(VantedPreferences.class).get(PREFERENCE_LOOKANDFEEL,
				null) == null) {
			String os = (String) System.getProperties().get("os.name");
			if (os != null && !os.toUpperCase().contains("LINUX") && !os.toUpperCase().contains("SUN")
					&& !os.toUpperCase().contains("MAC"))
				if (!ReleaseInfo.isRunningAsApplet())
					canonicalName = UIManager.getSystemLookAndFeelClassName();
		}

		// temp variable to add the active LAF to the beginning of the
		// objectlistparameter variable
		LookAndFeelNameAndClass activeLaF = null;

		List<LookAndFeelNameAndClass> listLAFs = new ArrayList<>();

		for (LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels()) {
			LookAndFeelNameAndClass d = new LookAndFeelNameAndClass(lafi);
			if (d.toString().equals(canonicalName))
				activeLaF = d;
			else
				listLAFs.add(d);
		}

		// Disable Nimbus (multithreading issues)
		listLAFs.remove(new LookAndFeelNameAndClass("Nimbus", "javax.swing.plaf.nimbus.NimbusLookAndFeel"));

		// Check, because in add-ons a new skin could be used => NPE.
		if (activeLaF != null) {
			listLAFs.add(0, activeLaF); // add active LaF to the beginning of the List
		} else {
			String activeLaF_Name = UIManager.getLookAndFeel().getName();
			activeLaF = new LookAndFeelNameAndClass(activeLaF_Name, canonicalName);
			listLAFs.add(0, activeLaF);
			// Then install it, to enable resetting, later use
			UIManager.installLookAndFeel(activeLaF_Name, canonicalName);
		}

		possibleValues = listLAFs.toArray();

		objectlistparam = new ObjectListParameter(activeLaF, PREFERENCE_LOOKANDFEEL,
				"<html>Set the look and feel of the application<br/>Current: <b>"
						+ (activeLaF.name.equals("GTK+") ? activeLaF.name + " (No Hi-DPI)" : activeLaF.name),
				possibleValues);
		objectlistparam.setRenderer(new LookAndFeelWrapperListRenderer());
		return objectlistparam;
	}

	/**
	 * Modifications to the standard UI default values. Added to the LAF Defaults,
	 * so that they can be scaled, if necessary.
	 */
	private static void styleUIDefaults() {
		UIManager.getLookAndFeelDefaults().put("SplitPaneDivider.border", new EmptyBorder(0, 0, 0, 0));
		UIManager.getLookAndFeelDefaults().put("Tree.leafIcon",
				GravistoService.loadIcon(VantedPreferences.class, "images/node.png"));
	}

	/**
	 * Custom list cell renderer, that will have the LookAndFeelNameAndClass as
	 * object and displays the human readable name of the LookAndFeel class name
	 * 
	 * @author matthiak
	 */
	class LookAndFeelWrapperListRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value instanceof LookAndFeelNameAndClass) {
				setText(((LookAndFeelNameAndClass) value).getName());
			}

			return this;
		}

	}
}