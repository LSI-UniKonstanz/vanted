package org.graffiti.options;

import java.util.List;
import java.util.prefs.Preferences;

import org.graffiti.plugin.parameter.Parameter;

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
//	public static List<Parameter> defaultPreferences = new ArrayList<>();
	
	
	public List<Parameter> getDefaultParameters();
	

	
	/**
	 * this method will be called, when preferences for this class has changed
	 * and the implementing class gets the chance of setting class (static) variables
	 * having the values of the parameters.
	 * Setting static class variables will help increasing speed, when querying
	 * the parameters. Direct variable access VS querying the Preferences Object for this class
	 * @param preferences
	 */
	public void updatePreferences(Preferences preferences);
	
	/**
	 * return a custom preference category, that this class will reside in.
	 * If null is returned, this class will be put into a standard category
	 * dependent on its Super class
	 * @return
	 */
	public String getPreferencesAlternativeName();
}
