package org.graffiti.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import org.ReleaseInfo;
import org.apache.log4j.Logger;
import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.managers.pluginmgr.PluginManagerListener;
import org.graffiti.options.PreferencesInterface;
import org.graffiti.plugin.EditorPlugin;
import org.graffiti.plugin.GenericPlugin;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.tool.Tool;

public class PreferenceManager 
implements PluginManagerListener
{


	private static final String SETTINGSFILENAME = "settings.xml";

	static Logger logger = Logger.getLogger(PreferenceManager.class);

	/**
	 * holds the list of all objects/classes that implement the 
	 * PreferencingInterface (can be Algorithms, Views,  etc)
	 */
	private List<Class<? extends PreferencesInterface>> listPreferencingObjects;

	private static PreferenceManager instance;

	private PreferenceManager() {

		try {
			Preferences.importPreferences(new FileInputStream(new File(ReleaseInfo.getAppFolder()+"/settings.xml")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidPreferencesFormatException e) {
			e.printStackTrace();
		}

		listPreferencingObjects = new ArrayList<>();
	}

	public static PreferenceManager getInstance() {
		if(instance == null)
			instance = new PreferenceManager();
		return instance;
	}

	public List<Class<? extends PreferencesInterface>> getPreferencingClasses(){
		return listPreferencingObjects;
	}


	public void addPreferencingClass(Class<? extends PreferencesInterface> preferencingClass)
	{
		logger.debug("checking general preferencing class '"+preferencingClass.getName()+"'");
		try {
			PreferencesInterface piClass = (PreferencesInterface)preferencingClass.newInstance();
			checkAddAndSetClassesPreferences(piClass);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked"})
	@Override
	public void pluginAdded(GenericPlugin plugin, PluginDescription desc) {
		for(Algorithm algo : plugin.getAlgorithms()) {
			logger.debug("checking algo '"+algo.getName()+"'");
			if(algo instanceof PreferencesInterface) {

				//				logger.debug("algorithm has preferences size: " +((PreferencesInterface)algo).defaultPreferences.size());
				checkAddAndSetClassesPreferences((PreferencesInterface)algo);
				//				checkExistingPreferences(algo.getClass(), ((PreferencesInterface)algo).getDefaultParameters());

				//				listPreferencingObjects.add((Class<? extends PreferencesInterface>)algo.getClass());
			}
		}

		for(String viewname : plugin.getViews()) {
			try {

				Class<?> forName = Class.forName(viewname);

				logger.debug("view name: "+forName.getName());

				Class<?>[] interfaces = forName.getInterfaces();
				for(Class<?> curInterface : interfaces) {
					if(curInterface.getName().equals(PreferencesInterface.class.getName())) {
						try {

							Object viewobject = forName.newInstance();

							PreferencesInterface pi = (PreferencesInterface)viewobject;
							checkAddAndSetClassesPreferences((PreferencesInterface)viewobject);
							//							checkExistingPreferences(forName, pi.getDefaultParameters());

							//							listPreferencingObjects.add((Class<? extends PreferencesInterface>)forName);


						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}

				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		if(plugin instanceof EditorPlugin) {
			EditorPlugin editorPlugin = (EditorPlugin)plugin;
			if(editorPlugin.getInspectorTabs() != null) {
				for(InspectorTab tab : editorPlugin.getInspectorTabs()) {
					if(tab instanceof PreferencesInterface) {
						checkAddAndSetClassesPreferences((PreferencesInterface)tab);
						//						checkExistingPreferences(tab.getClass(), ((PreferencesInterface)tab).getDefaultParameters());

						//						listPreferencingObjects.add((Class<? extends PreferencesInterface>)tab.getClass());
					}
				}
			}

			if(editorPlugin.getTools() != null) {
				for(Tool tool : editorPlugin.getTools()) {
					if(tool instanceof PreferencesInterface) {
						checkAddAndSetClassesPreferences((PreferencesInterface)tool);
						//						checkExistingPreferences(tool.getClass(), ((PreferencesInterface)tool).getDefaultParameters());

						//						listPreferencingObjects.add((Class<? extends PreferencesInterface>)tool.getClass());
					}
				}
			}
		}
	}
	/**
	 * if no preferences found, create a new preferences object from
	 * default parameters
	 */
	private Preferences checkExistingPreferences(Class<?> clazz, List<? extends Parameter> defaultPreferences) {

		/*
		 * inheriting classes, where the superclass already implemented the defaultparameters
		 * but the child class doesn't have default parameters needs to return null to
		 * have no default parameters
		 */
		if(defaultPreferences == null)
			return null;

		Preferences defaultPrefs = null;
		try {
			defaultPrefs = getPreferenceForClass(clazz);
			// keys that will be deleted, if they have been removed from this class
			Set<String> prefKeysToDelete = new HashSet<>(Arrays.asList(defaultPrefs.keys()));

			for(Parameter defaultParameter :defaultPreferences) {
				if( ! prefKeysToDelete.remove(defaultParameter.getName()))
					defaultPrefs.put(defaultParameter.getName(), defaultParameter.getValue().toString());
			}

			for(String key : prefKeysToDelete)
				defaultPrefs.remove(key);
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		return defaultPrefs;
	}

	private void checkAddAndSetClassesPreferences(PreferencesInterface prefInterface) {
		Preferences defaultPrefs = checkExistingPreferences(prefInterface.getClass(), ((PreferencesInterface)prefInterface).getDefaultParameters());

		if(defaultPrefs != null) {
			listPreferencingObjects.add((Class<? extends PreferencesInterface>)prefInterface.getClass());

			prefInterface.updatePreferences(defaultPrefs);
		}
	}

	/**
	 * This method should be used when getting Preferences for a specific class/object.
	 * It'll supply a method that supports easy creation of 
	 * Preference Objects regarding a Class. Normally Preferences can be created for
	 * Class objects but then the Preference relates to the package.
	 */
	public static Preferences getPreferenceForClass(Class<?> clazz) {

		String pathName = clazz.getName().replace(".", "/");
		return Preferences.userRoot().node(pathName);
	}

	/**
	 * This method should be used when getting Preferences for a specific category for
	 * a class/object. This can be used to structure paramters in categories
	 */
	public static Preferences getPreferenceCategoryForClass(Class<?> clazz, String category) {

		String pathName = clazz.getName().replace(".", "/");
		return Preferences.userRoot().node(pathName+"/"+category);
	}

	public static void updatePreferencesForClass(Class<? extends PreferencesInterface> clazz, Preferences preferences) {
		try {
			((PreferencesInterface)clazz.newInstance()).updatePreferences(preferences);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static void storePreferences() {
		try {
			Preferences.userRoot().exportSubtree(new FileOutputStream(new File(ReleaseInfo.getAppFolder()+"/"+SETTINGSFILENAME)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
}
