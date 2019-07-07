package org.vanted.addons.MultilevelFramework;

import java.util.LinkedList;
import java.util.List;

/**
 * Singleton class storing a list of names of layout algorithms that shall be
 * available as selectable options.
 */
public class LayoutersWhitelist {
	private static LayoutersWhitelist instance = null;

	private List<String> layouters;

	private LayoutersWhitelist() {
		layouters = new LinkedList<String>();
	}

	/**
	 * @return Returns the list of the names of the whitelisted layout algorithms.
	 */
	public static List<String> getLayouters() {
		testNull();
		return instance.layouters;
	}

	/**
	 * Adds an algorithm name to the list of layout algorithms.
	 * 
	 * @param algorithm algorithm to add
	 */
	public static void add(String algorithm) {
		testNull();
		instance.layouters.add(algorithm);
	}

	/**
	 * Removes all algorithms from the list.
	 */
	public static void clear() {
		testNull();
		instance.layouters.clear();
	}

	private static void testNull() {
		if (instance == null) {
			instance = new LayoutersWhitelist();
		}
	}
}
