package org.graffiti.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import org.ReleaseInfo;
import org.apache.log4j.Logger;
import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.managers.pluginmgr.PluginManagerListener;
import org.graffiti.options.PreferencesInterface;
import org.graffiti.plugin.GenericPlugin;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.parameter.Parameter;

public class PreferenceManager 
implements PluginManagerListener
{
	private static final String SETTINGSFILENAME = "settings.xml";

	static Logger logger = Logger.getLogger(PreferenceManager.class);
	
	/**
	 * holds the list of all objects/classes that implement the 
	 * PreferencingInterface (can be Algorithms, Views,  etc)
	 */
	List<Class<? extends PreferencesInterface>> listPreferencingObjects;
	
	public PreferenceManager() {
		
		try {
			Preferences.importPreferences(new FileInputStream(new File(ReleaseInfo.getAppFolder()+"/settings.xml")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidPreferencesFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		listPreferencingObjects = new ArrayList<>();
	}

	
	
	
	@SuppressWarnings({ "unchecked", "static-access" })
	@Override
	public void pluginAdded(GenericPlugin plugin, PluginDescription desc) {
		// TODO Auto-generated method stub
		for(Algorithm algo : plugin.getAlgorithms()) {
			logger.debug("checking algo '"+algo.getName()+"'");
			if(algo instanceof PreferencesInterface) {
				
//				logger.debug("algorithm has preferences size: " +((PreferencesInterface)algo).defaultPreferences.size());
				
				checkExistingPreferences(algo.getClass(), ((PreferencesInterface)algo).getDefaultParameters());
				
				listPreferencingObjects.add((Class<? extends PreferencesInterface>)algo.getClass());
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
							
							checkExistingPreferences(forName, pi.getDefaultParameters());
							
						
						} catch (InstantiationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						/*
						try {
							Field field = forName.getField("defaultPreferences");

							List<? extends Parameter> listPreferences = null;
							try {
								Object object = null;
								object = field.get(null);
								
								listPreferences = (List<? extends Parameter>)object;
								
								checkExistingPreferences(forName, listPreferences);
								
								logger.debug("preferences size: "+listPreferences.size());
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							logger.debug(field.getType().getTypeName());
						} catch (NoSuchFieldException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						*/
						listPreferencingObjects.add((Class<? extends PreferencesInterface>)forName);
					}
						
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void checkExistingPreferences(Class<?> clazz, List<? extends Parameter> defaultPreferences) {
		/*
		 * if no preferences found, create a new preferences object from
		 * default parameters
		 */
		try {
			if( !  Preferences.userRoot().nodeExists(clazz.getName().replace(".", "/") ) ) {
				Preferences algoDefaultPrefs = getPreferenceForClass(clazz);
				for(Parameter defaultParameter :defaultPreferences) {
					
					algoDefaultPrefs.put(defaultParameter.getName(), defaultParameter.getValue().toString());
				}
			}
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	

	
	public static void storePreferences() {
		try {
			Preferences.userRoot().exportSubtree(new FileOutputStream(new File(ReleaseInfo.getAppFolder()+"/"+SETTINGSFILENAME)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
