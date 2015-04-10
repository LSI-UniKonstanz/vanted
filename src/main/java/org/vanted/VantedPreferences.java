package org.vanted;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.options.PreferencesInterface;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.StringParameter;

/**
 * Global Preference class for VANTED.
 * 
 * Every Preference regarding global settings of VANTED should be
 * put in here.
 * 
 * As usual Classes implementing the PreferencesInterface provide an array
 * of parameters giving default values and else get their values from the
 * java.util.Preferences object representing this class.
 * @author matthiak
 *
 */
public class VantedPreferences implements PreferencesInterface{

	public static final String PREFERENCE_LOOKANDFEEL = "Look and Feel";
	public static final String PREFERENCE_PROXYHOST = "Proxy Host";
	public static final String PREFERENCE_PROXYPORT = "Proxy Port";
	
	private static VantedPreferences instance;
	
	public VantedPreferences() {
		instance = this;
	}
	
	public static VantedPreferences getInstance() {
		return instance;
	}
	
	@Override
	public List<Parameter> getDefaultParameters() {
		ArrayList<Parameter> params = new ArrayList<>();
		params.add(getLookAndFeelParameter()); 
		params.add(new StringParameter("", PREFERENCE_PROXYHOST, "Name or IP  of the proxy host"));
		params.add(new IntegerParameter(0, PREFERENCE_PROXYPORT, "Port number of the proxy"));
		return params;
	}



	@Override
	public void updatePreferences(Preferences preferences) {
		
		/*
		 * handle look and feel parameter
		 */
		final String lafName = preferences.get(PREFERENCE_LOOKANDFEEL,"");
		String selLAF = UIManager.getLookAndFeel().getClass().getCanonicalName();
		if(!selLAF.equals(lafName)) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					try {
						UIManager.setLookAndFeel(lafName);
					} catch (ClassNotFoundException | InstantiationException
							| IllegalAccessException
							| UnsupportedLookAndFeelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					/*
					 * show changes for current instance running
					 * This will only be executed when the preferences are updated using
					 * the Preferences Dialog
					 */
					if(MainFrame.getInstance() != null) {
						if (ReleaseInfo.isRunningAsApplet())
							SwingUtilities.updateComponentTreeUI(ReleaseInfo.getApplet());
						else
							SwingUtilities.updateComponentTreeUI(MainFrame.getInstance());
						MainFrame.getInstance().repaint();
					}

				}
			});
		}
		
		/*
		 * handle proxy parameter
		 */
		String proxyhostname = preferences.get(PREFERENCE_PROXYHOST, null);
		String proxyport = preferences.get(PREFERENCE_PROXYPORT, null);
		if(proxyhostname != null && proxyport != null 
				&& ! proxyhostname.isEmpty()) {
			try {
				Integer portnumber = Integer.parseInt(proxyport);
				System.setProperty("http.proxySet", "true");
				System.setProperty("http.proxyHost", proxyhostname);
				System.setProperty("http.proxyPort", proxyport);
				
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				System.setProperty("http.proxySet", "false");
				
			}
		} else {
			System.setProperty("http.proxySet", "false");
		}
			
	}
	
	@Override
	public String getPreferencesAlternativeName() {
		// TODO Auto-generated method stub
		return "Vanted Preferences";
	}
	
	/**
	 * retrieves all available LookandFeels from the system and creates
	 * a new ObjectListparameter object, that will be used to display a
	 * JComboBox
	 * @return
	 */
	private ObjectListParameter getLookAndFeelParameter() {
		
		ObjectListParameter objectlistparam;
		Object[] possibleValues;
		String canonicalName = UIManager.getLookAndFeel().getClass().getCanonicalName();
		
		// temp variable to add the active LAF to the beginning of the objectlistparameter variable
		LookAndFeelNameAndClass avtiveLaF = null;

		List<LookAndFeelNameAndClass> listLAFs = new ArrayList<>();
		
		
		for (LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels()) {
			LookAndFeelNameAndClass d = new LookAndFeelNameAndClass(lafi.getName(), lafi.getClassName());
			if (d.toString().equals(canonicalName))
				avtiveLaF = d;
			else
				listLAFs.add(d);
				
		}
		// add active LaF to the beginning of the List
		listLAFs.add(0, avtiveLaF);
		
		
		possibleValues = listLAFs.toArray();
		
		objectlistparam = new ObjectListParameter(
				avtiveLaF, 
				PREFERENCE_LOOKANDFEEL, 
				"<html>Set the look and feel of the application<br/>Current: <b>"+avtiveLaF.name, 
				possibleValues);
		objectlistparam.setRenderer(new LookAndFeelWrapperListRenderer());
		return objectlistparam;
	}
	
	/**
	 * Custom list cell renderer, that will have the LookAndFeelNameAndClass as object
	 * and displays the human readable name of the lookandfeel class name
	 * @author matthiak
	 *
	 */
	class LookAndFeelWrapperListRenderer extends DefaultListCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			// TODO Auto-generated method stub
			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			
			if(value instanceof LookAndFeelNameAndClass) {
				setText(((LookAndFeelNameAndClass)value).getName());
			}
			
			
			
			return this;
		}
		
	}
	
	class LookAndFeelNameAndClass{
		String name; 
		String className;
		public LookAndFeelNameAndClass(String name, String className) {
			this.name = name;
			this.className = className;
		}
		public String getName() {
			return name;
		}
		public String toString() {
			return className;
		}
	}
}
