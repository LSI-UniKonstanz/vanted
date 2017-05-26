package org.vanted.updater;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Updates any previously stored old links from preferences. 
 * Only present for version 2.6.4 during the transition period
 * to force such update.
 * 
 * @since 2.6.4
 * @author dim8
 *
 */
public class PreferencesUpdater264 {

	public PreferencesUpdater264() {
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
		String version = "";
		try (InputStream stream = PreferencesUpdater264.class.getClassLoader()
				.getResourceAsStream("build.number")) {
			Properties build_props = new Properties();
			build_props.load(stream);
			version = build_props.getProperty("vanted.version.number");				
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/**
		 * Force such update only in version 2.6.4!
		 */
		if (!version.equals("2.6.4"))
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
				return true;
			}
		
		return false;
	}

}
