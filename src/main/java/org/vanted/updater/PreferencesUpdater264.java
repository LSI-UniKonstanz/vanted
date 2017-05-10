package org.vanted.updater;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
		try (BufferedReader br = 
				new BufferedReader(new FileReader(PreferencesUpdater264.class
						.getClassLoader().getResource("build.number")
								.getFile()))) {
			while ((version = br.readLine()) != null) {
				if (version.startsWith("vanted.version.number"))
					break;
				version = br.readLine();
			}
			version = version.substring(version.lastIndexOf('=') + 1).trim();						
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
