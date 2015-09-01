package edu.monash.vanted.test.preferences;

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
		PreferenceManager preferenceManager = PreferenceManager.getInstance();
		
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
