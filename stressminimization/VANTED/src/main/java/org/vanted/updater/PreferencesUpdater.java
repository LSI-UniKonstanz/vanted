package org.vanted.updater;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.graffiti.managers.PreferenceManager;
import org.vanted.BuildInfo;

/**
 * Updates any previously stored old links from preferences. Created for version
 * 2.6.4 during the transition period to force links update. It could be
 * extended or re-used to force such deep updates, if needed.
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
	 * Checks for the current version and updates accordingly.
	 * 
	 * @param preferences
	 *            the respective class preferences
	 * @param key
	 *            the key to get the value
	 * @param toUpdate
	 *            the new value
	 * @return true, if the link has been successfully updated
	 */
	@Deprecated
	public static boolean checkAndUpdateMonashLink(Preferences preferences, String key, String toUpdate) {
		/**
		 * Force such update only in version 2.6.4!
		 */
		if (!isVersion("2.6.4"))
			return false;

		String old;
		if ((old = preferences.get(key, null)) != null)
			if (old.contains("monash.edu")) {
				preferences.put(key, toUpdate);
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
	 * Update any deeply stored preferences values. To make sure there is no
	 * unnecessary propagation of changes into multiple versions, better check the
	 * version with {@link PreferencesUpdater#isVersion(String)} to restrict to only
	 * it.
	 * 
	 * @param preferences
	 *            the respective class preferences
	 * @param key
	 *            the key to get the value
	 * @param toRemove
	 *            the old value, can also be only partial
	 * @param toUpdate
	 *            the new value
	 * @return true, if the link has been successfully updated
	 * @since 2.6.5
	 */
	public static boolean checkAndUpdateLink(Preferences preferences, String key, String toRemove, String toUpdate) {

		String old;
		if ((old = preferences.get(key, null)) != null)
			if (old.contains(toRemove)) {
				preferences.put(key, toUpdate);
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
	 * @param version
	 *            to check
	 * @return true if current equals version
	 */
	public static boolean isVersion(String version) {
		if (PreferencesUpdater.version.equals(version))
			return true;

		PreferencesUpdater.version = BuildInfo.getCurrentVersion();

		if (PreferencesUpdater.version.equals(version))
			return true;

		return false;
	}

}
