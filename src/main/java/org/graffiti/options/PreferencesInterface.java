package org.graffiti.options;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import org.graffiti.managers.PreferenceManager;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.IPKGraffitiView;

/**
 * Classes that want to provide settings that will be stored in a settings file and
 * that want to be user-configurable can implement this interface.
 *
 * Detailed usage:
 * 
 * 
 * 
 * @author matthiak
 *
 */
public interface PreferencesInterface {


	
	/**
	 * On first start or on reset there will be no preferences available. The 
	 * PreferenceManager will read this list containing the set of settings, that
	 * the implementing class thinks can be configured provide default values.
	 * 
	 * All Parameters in this list will also appear in the PreferenceDialog.
	 * 
	 * Entities providing preferences should add parameters to this array.
	 */
	public static List<Parameter> defaultPreferences = new ArrayList<>();
	
	
	public List<Parameter> getDefaultParameters();
	
	/**
	 * a getter that needs to be implemented by the implementing class
	 * Usually it should be the simple retrieval of the Preferences
	 * and put the preferences in a field variable.
	 * 
	 * <code>
	 * 		if(preferences == null)
			preferences = PreferenceManager.getPreferenceForClass(IPKGraffitiView.class);
			return preferences;
	 * </code>
	 * With java 8 this will be a 'default' method
	 * @return
	 */
	public Preferences getPreferences();
}
