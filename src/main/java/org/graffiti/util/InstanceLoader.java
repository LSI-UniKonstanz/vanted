// ==============================================================================
//
// InstanceLoader.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: InstanceLoader.java,v 1.11 2011/06/30 06:53:46 morla Exp $

package org.graffiti.util;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Policy;
import java.util.HashSet;

/**
 * Represents an instance loader, which can be used to instantiate a class with
 * the given name.
 * 
 * @since 1.0
 * @version 1.11
 * @vanted.revision 2.6.5
 */
public class InstanceLoader {
	
	private static ClassLoader storedLoader = InstanceLoader.class.getClassLoader();
	
	private static Policy allRight = new AllPermissionPolicy();
	
	public static synchronized void overrideLoader(ClassLoader loader) {
		if (loader == null)
			return;
		
		Policy.setPolicy(allRight); // :-D
		
		if (storedLoader != null && (storedLoader instanceof URLClassLoader) && (loader instanceof URLClassLoader)) {
			// update stored loader with new URLs
			URLClassLoader ucl = (URLClassLoader) storedLoader;
			URLClassLoader newUCL = (URLClassLoader) loader;
			HashSet<URL> knownURLs = new HashSet<URL>();
			for (URL knownURL : ucl.getURLs())
				knownURLs.add(knownURL);
			
			for (URL newURL : newUCL.getURLs()) {
				if (!knownURLs.contains(newURL)) {
					storedLoader = loader;
					return;
				}
			}
		} else {
			storedLoader = loader;
		}
		Policy.setPolicy(allRight); // :-D
		
		Policy.setPolicy(allRight); // :-D
	}
	
	public static synchronized void setClassLoader(ClassLoader newClassloader) {
		storedLoader = newClassloader;
	}
	
	public static synchronized ClassLoader getCurrentLoader() {
		return storedLoader;
	}
	
	/**
	 * Returns a new instance of the specified class.
	 * 
	 * @param theClass
	 *          the class to instantiate.
	 * @return
	 * 			newly instantiated instance of type theClass
	 * @throws InstanceCreationException
	 * 			when an instance couldn't be created
	 */
	public static Object createInstance(Class<?> theClass)
						throws InstanceCreationException {
		try {
			return theClass.getDeclaredConstructor().newInstance();
		} catch (InstantiationException|
				 IllegalAccessException|
				 IllegalArgumentException|
				 InvocationTargetException|
				 NoSuchMethodException|
				 SecurityException e) {
			throw new InstanceCreationException(e);
		}
	}
	
	/**
	 * Returns a new instance of the specified class.
	 * 
	 * @param name
	 *          the name of the class to instantiate.
	 * @return
	 * 			newly instantiated instance of type theClass
	 * @throws InstanceCreationException
	 * 			when an instance couldn't be created
	 */
	public static synchronized Object createInstance(String name)
						throws InstanceCreationException {
		try {
			Class<?> c = storedLoader.loadClass(name);
			
			return c.getDeclaredConstructor().newInstance();
		} catch (NullPointerException|
				 ClassNotFoundException|
				 InstantiationException|
				 IllegalAccessException|
				 ClassCastException e) {
			e.printStackTrace();
			throw new InstanceCreationException(e);	
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new InstanceCreationException(exception);
		}
	}
	
	/**
	 * Returns a new instance of the specified class. Uses a constructor taking
	 * one argument.
	 * 
	 * @param name
	 * 			the name of the class to instantiate.
	 * @param param
	 * 			specified class' constructor argument
	 * @return newly
	 * 			instantiated instance of type theClass
	 * @throws InstanceCreationException
	 * 			when an instance couldn't be created
	 */
	public static Object createInstance(String name, Object param)
						throws InstanceCreationException {
		try {
			return Class.forName(name).getConstructor(param.getClass())
					.newInstance(param);
		} catch (InvocationTargetException|
				 NoSuchMethodException| 
				 NullPointerException| 
				 ClassNotFoundException| 
				 InstantiationException| 
				 IllegalAccessException ite) {
			throw new InstanceCreationException(ite);
		}
	}
	
	/**
	 * Returns a new instance of the specified class. Uses a constructor taking
	 * one argument.
	 * 
	 * @param theClass
	 * 			the class to instantiate.
	 * @param paramClassname
	 * 			the fully qualified name of the parameter class to instantiate.
	 * @param param
	 * 			specified class' constructor argument
	 * @return 
	 * 			newly instantiated instance of type theClass
	 * @throws InstanceCreationException
	 * 			when an instance couldn't be created
	 */
	public static Object createInstance(Class<?> theClass,
						String paramClassname, Object param)
										throws InstanceCreationException {
		try {
			return theClass.getConstructor(Class.forName(paramClassname))
					.newInstance(param);
		} catch (InvocationTargetException|
				NoSuchMethodException|
				NullPointerException|
				ClassNotFoundException|
				InstantiationException|
				IllegalAccessException ite) {
			throw new InstanceCreationException(ite);
		}
	}
	
	/**
	 * Returns a new instance of the specified class. Uses a constructor taking
	 * one argument.
	 * 
	 * @param theClass
	 *          the name of the class to instantiate.
	 * @param param
	 * 			specified class' constructor argument
	 * @return newly 
	 * 			instantiated instance of type theClass
	 * @throws InstanceCreationException
	 * 			when an instance couldn't be created
	 */
	public static Object createInstance(Class<?> theClass, Object param)
						throws InstanceCreationException {
		try {
			return theClass.getConstructor(param.getClass())
					.newInstance(param);
		} catch (InvocationTargetException|
				 NoSuchMethodException|
				 NullPointerException|
				 InstantiationException|
				 IllegalAccessException ite) {
			throw new InstanceCreationException(ite);
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
