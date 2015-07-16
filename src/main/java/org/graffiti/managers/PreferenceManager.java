package org.graffiti.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import org.ReleaseInfo;
import org.apache.log4j.Level;
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
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.util.InstanceLoader;

public class PreferenceManager
		implements PluginManagerListener
{
	
	private static final String SETTINGSFILENAME = "settings.xml";
	
	static Logger logger = Logger.getLogger(PreferenceManager.class);
	
	static {
		logger.setLevel(Level.INFO);
	}
	
	/**
	 * holds the list of all objects/classes that implement the
	 * PreferencingInterface (can be Algorithms, Views, etc)
	 */
	private Set<Class<? extends PreferencesInterface>> setPreferencingObjects;
	
	private static PreferenceManager instance;
	
	private PreferenceManager() {
		
		try {
			Preferences.importPreferences(new FileInputStream(new File(ReleaseInfo.getAppFolder() + "/settings.xml")));
		} catch (FileNotFoundException e) {
			logger.debug("no preference file found " + e.getMessage());
//			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
//			e.printStackTrace();
		} catch (InvalidPreferencesFormatException e) {
			logger.error(e.getMessage());
//			e.printStackTrace();
		}
		
		setPreferencingObjects = new HashSet<>();
	}
	
	public static PreferenceManager getInstance() {
		if (instance == null)
			instance = new PreferenceManager();
		return instance;
	}
	
	public Set<Class<? extends PreferencesInterface>> getPreferencingClasses() {
		return setPreferencingObjects;
	}
	
	/**
	 * Another way of adding custom classes that implement the PreferencesInterface
	 * which will be added to the list of preferencing classes.
	 * Use this method if your class is not part of a plugin which provides
	 * new Views, Tabs, Algorithms, etc
	 * 
	 * @param preferencingClass
	 */
	public void addPreferencingClass(Class<? extends PreferencesInterface> preferencingClass)
	{
		logger.debug("checking general preferencing class '" + preferencingClass.getName() + "'");
		try {
			PreferencesInterface piClass = (PreferencesInterface) preferencingClass.newInstance();
			checkAddAndSetClassesPreferences(piClass);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * called each time a plugin gets loaded
	 * Checks the Plugins entries (Views, Algorithms, Tabs, ViewComponents, Tools) if they implement the PreferenceInterface
	 * If so, the preferences of this class will be
	 * a) read (and stored), if this is a new class (first time)
	 * b) set, if there are present preferences stored
	 * Also this algorithm will synchronize the preferences, in case preferences have been created or deleted.
	 * 
	 * @param plugin
	 * @param desc
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public void pluginAdded(GenericPlugin plugin, PluginDescription desc) {
		for (Algorithm algo : plugin.getAlgorithms()) {
			logger.debug("checking algo '" + algo.getName() + "'");
			if (algo instanceof PreferencesInterface) {
				checkAddAndSetClassesPreferences((PreferencesInterface) algo);
			}
		}
		
		for (String viewname : plugin.getViews()) {
			try {
				
				Class<?> forName = InstanceLoader.getCurrentLoader().loadClass(viewname);
				
				logger.debug("view name: " + forName.getName());
				
				Class<?>[] interfaces = forName.getInterfaces();
				for (Class<?> curInterface : interfaces) {
					if (curInterface.getName().equals(PreferencesInterface.class.getName())) {
						try {
							
							Object viewobject = forName.newInstance();
							
							PreferencesInterface pi = (PreferencesInterface) viewobject;
							checkAddAndSetClassesPreferences((PreferencesInterface) viewobject);
							
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
					
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			
		}
		if (plugin instanceof EditorPlugin) {
			EditorPlugin editorPlugin = (EditorPlugin) plugin;
			if (editorPlugin.getInspectorTabs() != null) {
				for (InspectorTab tab : editorPlugin.getInspectorTabs()) {
					if (tab instanceof PreferencesInterface) {
						checkAddAndSetClassesPreferences((PreferencesInterface) tab);
					}
				}
			}
			
			if (editorPlugin.getTools() != null) {
				for (Tool tool : editorPlugin.getTools()) {
					if (tool instanceof PreferencesInterface) {
						checkAddAndSetClassesPreferences((PreferencesInterface) tool);
					}
				}
			}
			if (editorPlugin.getAttributeComponents() != null) {
				for (Class<? extends AttributeComponent> attrComponent : editorPlugin.getAttributeComponents().values()) {
					try {
						AttributeComponent attrCompInstance = (AttributeComponent) attrComponent.newInstance();
						if (attrCompInstance instanceof PreferencesInterface)
							checkAddAndSetClassesPreferences((PreferencesInterface) attrCompInstance);
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * if no preferences found, create a new preferences object from
	 * default parameters.
	 * if parameters in already present preferences that are not anymore
	 * part of the given preferences, the parameters in the preferences
	 * gets deleted
	 */
	private Preferences checkExistingPreferences(Class<?> clazz, List<? extends Parameter> defaultPreferences) {
		
		/*
		 * inheriting classes, where the superclass already implemented the defaultparameters
		 * but the child class doesn't have default parameters needs to return null to
		 * have no default parameters
		 */
		if (defaultPreferences == null)
			return null;
		
		Preferences defaultPrefs = null;
		try {
			defaultPrefs = getPreferenceForClass(clazz);
			// keys that will be deleted, if they have been removed from this class
			Set<String> prefKeysToDelete = new HashSet<>(Arrays.asList(defaultPrefs.keys()));
			
			for (Parameter defaultParameter : defaultPreferences) {
				if (!prefKeysToDelete.remove(defaultParameter.getName()))
					defaultPrefs.put(defaultParameter.getName(), defaultParameter.getValue().toString());
			}
			
			for (String key : prefKeysToDelete)
				defaultPrefs.remove(key); //removes key from preferences object
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		return defaultPrefs;
	}
	
	/**
	 * This method will do several things, that are repeatedly done to
	 * get possibly existing Preferences and add the class to the list of
	 * Preferencing Classes and also call the implementing object for first-time
	 * setup during startup of the program
	 * 
	 * @param prefInterface
	 */
	private void checkAddAndSetClassesPreferences(PreferencesInterface prefInterface) {
		Preferences defaultPrefs = checkExistingPreferences(prefInterface.getClass(), ((PreferencesInterface) prefInterface).getDefaultParameters());
		
		if (defaultPrefs != null) {
			setPreferencingObjects.add((Class<? extends PreferencesInterface>) prefInterface.getClass());
			
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
	 * This method should be used when getting Preferences for a specific category (folder) for
	 * a class/object. This can be used to structure paramters in categories (folders)
	 */
	public static Preferences getPreferenceCategoryForClass(Class<?> clazz, String category) {
		
		String pathName = clazz.getName().replace(".", "/");
		return Preferences.userRoot().node(pathName + "/" + category);
	}
	
	/**
	 * This method will instantiate an Object from the given class and
	 * call the updatePreferences method, which must be implemented by any
	 * PreferencesInterface implementing class.
	 * The PreferencesInterface-object should then store the given preferences
	 * in static variables, since those parameters are with regard to the class
	 * The static variables will give fast access VS querying the Preferences
	 * each time.
	 *
	 * @param clazz
	 * @param preferences
	 */
	public static void updatePreferencesForClass(Class<? extends PreferencesInterface> clazz, Preferences preferences) {
		try {
			((PreferencesInterface) clazz.newInstance()).updatePreferences(preferences);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Stores the preferences in 'settings.xml' in the Vanted program directory
	 */
	public static void storePreferences() {
		try {
			Preferences.userRoot().exportSubtree(new FileOutputStream(new File(ReleaseInfo.getAppFolder() + "/" + SETTINGSFILENAME)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
}
