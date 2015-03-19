package edu.monash.vanted.test.preferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.ReleaseInfo;
import org.graffiti.managers.PreferenceManager;
import org.graffiti.plugin.GenericPlugin;
import org.junit.Before;
import org.junit.Test;

public class PreferencesTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testPreferenceFieldLoading() {
		PreferenceManager preferenceManager = new PreferenceManager();
		
		GenericPlugin plugin = new TestPlugin();
		
		
		preferenceManager.pluginAdded(plugin, null);
		
		new TestAlgorithm().execute();
		
		System.out.println();
		

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new PreferencesTest().testPreferenceFieldLoading();
	}
}
