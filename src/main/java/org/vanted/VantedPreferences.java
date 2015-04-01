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
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

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

	public static final String PREFERENCE_LOOKANDFEEL = "lookandfeel";
	
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
		
		return params;
	}



	@Override
	public void updatePreferences(Preferences preferences) {
		final String lafName = preferences.get(PREFERENCE_LOOKANDFEEL,"");
		String selLAF = UIManager.getLookAndFeel().getClass().getCanonicalName();
		if(MainFrame.getInstance() != null && !selLAF.equals(lafName)) {
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
					if (ReleaseInfo.isRunningAsApplet())
						SwingUtilities.updateComponentTreeUI(ReleaseInfo.getApplet());
					else
						SwingUtilities.updateComponentTreeUI(MainFrame.getInstance());
					MainFrame.getInstance().repaint();

				}
			});
		}
	}
	
	@Override
	public String getPreferencesAlternativeName() {
		// TODO Auto-generated method stub
		return "Vanted Preferences";
	}
	private ObjectListParameter getLookAndFeelParameter() {
		
		ObjectListParameter objectlistparam;
		Object[] possibleValues;
		String canonicalName = UIManager.getLookAndFeel().getClass().getCanonicalName();
		
		LookAndFeelNameAndClass avtiveLaF = null;
		List<LookAndFeelNameAndClass> listLAFs = new ArrayList<>();
		
		for (LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels()) {
			LookAndFeelNameAndClass d = new LookAndFeelNameAndClass(lafi.getName(), lafi.getClassName());
			listLAFs.add(d);
			if (d.toString().equals(canonicalName))
				avtiveLaF = d;
		}
		
		
		possibleValues = listLAFs.toArray();
		
		objectlistparam = new ObjectListParameter(
				avtiveLaF, 
				PREFERENCE_LOOKANDFEEL, 
				"<html>Set the look and feel of the application<br/>Current: <b>"+avtiveLaF.name, 
				possibleValues);
		objectlistparam.setRenderer(new LookAndFeelWrapperListRenderer());
		return objectlistparam;
	}
	
	
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
