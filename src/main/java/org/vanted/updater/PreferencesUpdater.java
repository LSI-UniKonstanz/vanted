package org.vanted.updater;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.graffiti.managers.PreferenceManager;

/**
 * Updates any previously stored old links from preferences. 
 * Only present for version 2.6.4 during the transition period
 * to force such update.
 * 
 * @since 2.6.4
 * @author dim8
 *
 */
public class PreferencesUpdater {
	
	private static String version = "";

	public PreferencesUpdater() {
	}
	
	/**
	 * Checks for the current version and accordingly updates.
	 * 
	 * @param preferences the respective class preferences
	 * @param key the key to get the value
	 * @param updated the new value
	 * @return
	 */
	public static boolean checkAndUpdateMonashLink(Preferences preferences, 
			String key, String updated) {		
		/**
		 * Force such update only in version 2.6.4!
		 */
		if (!isVersion("2.6.4"))
			return false;
			
		String old;
		if ((old = preferences.get(key, null)) != null)
			if (old.contains("monash.edu")) {
				preferences.put(key, updated);
				try {
					preferences.flush();
				} catch (BackingStoreException e) {
					e.printStackTrace();
				}
				PreferenceManager.storePreferences();
				return true;
			}
		
		return false;
	}
	
	/**
	 * Check current version.
	 * 
	 * @param version to check
	 * @return true if current equals version
	 */
	public static boolean isVersion(String version) {
		if (PreferencesUpdater.version.equals(version))
			return true;
		
		try (InputStream stream = PreferencesUpdater.class.getClassLoader()
				.getResourceAsStream("build.number")) {
			Properties build_props = new Properties();
			build_props.load(stream);
			PreferencesUpdater.version = build_props.getProperty("vanted.version.number");				
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (PreferencesUpdater.version.equals(version))
			return true;
		
		return false;
	}

}
