/**
 * This class provides methods to load the native Adaptagrams libraries.
 * Copyright (c) 2014-2015 Monash University, Australia
 */
package org.vanted.plugins.layout.adaptagrams;

import java.io.File;

/**
 * @author Tobias Czauderna
 */
@SuppressWarnings("nls")
public class AdaptagramsLibrary {

	/**
	 * Return the names of the native Adaptagrams libraries for the different
	 * operating systems. There is a 32 bit and 64 bit version of the library for
	 * Windows and Linux. There is only one library necessary for MacOS.
	 * 
	 * @return library names for the different operating systems
	 */
	public static String[] getLibraryNames() {

		// the layout library is available on Windows, Linux, and Mac OS
		String[] availableOSs = new String[] { "windows", "linux", "mac" };
		String osName = System.getProperty("os.name");
		String[] libraryNames = null;
		if (osName.toLowerCase().contains(availableOSs[0]))
			libraryNames = new String[] { "adaptagrams32.dll", "adaptagrams64.dll" };
		if (osName.toLowerCase().contains(availableOSs[1]))
			libraryNames = new String[] { "adaptagrams32.so", "adaptagrams64.so" };
		if (osName.toLowerCase().contains(availableOSs[2]))
			libraryNames = new String[] { "adaptagrams.dylib" };
		return libraryNames;

	}

	/**
	 * Load native Adaptagrams layout library. Tries to load the layout library from
	 * the working directory.
	 * 
	 * @param libraryName
	 *            name of the library
	 * @return error message
	 */
	public static String loadLibrary(String libraryName) {

		String libraryPath = System.getProperty("user.dir").replace("\\", "/") + "/";
		return loadLibrary(libraryName, libraryPath);

	}

	/**
	 * Load native Adaptagrams layout library.
	 * 
	 * @param libraryName
	 *            name of the library
	 * @param libraryPath
	 *            path to the library
	 * @return error message
	 */
	public static String loadLibrary(String libraryName, String libraryPath) {

		// the layout library is available on Windows, Linux, and Mac OS
		String[] availableOSs = new String[] { "windows", "linux", "mac" };
		// 32 bit and 64 bit OSs are possible
		String windowsExt32 = "32.dll";
		String windowsExt64 = "64.dll";
		String linuxExt32 = "32.so";
		String linuxExt64 = "64.so";
		String macExt = ".dylib";
		String osName = System.getProperty("os.name");
		String ext32 = "";
		String ext64 = "";
		String ext = "";

		if (!osName.toLowerCase().contains(availableOSs[0]) && !osName.toLowerCase().contains(availableOSs[1])
				&& !osName.toLowerCase().contains(availableOSs[2]))
			return "Layout library not available for " + osName + "!";

		// on Windows and on Linux two versions of the native library exist (one for 32
		// bit and one for 64 bit)
		if (osName.toLowerCase().contains(availableOSs[0])) {
			ext32 = windowsExt32;
			ext64 = windowsExt64;
		}
		if (osName.toLowerCase().contains(availableOSs[1])) {
			ext32 = linuxExt32;
			ext64 = linuxExt64;
		}
		// on Mac OS only one fat native library exists (for both 32 bit and 64 bit)
		if (osName.toLowerCase().contains(availableOSs[2]))
			ext = macExt;

		// check whether the library can be found
		if (!ext32.isEmpty() && !ext64.isEmpty() && !(new File(libraryPath + libraryName + ext32)).exists()
				&& !(new File(libraryPath + libraryName + ext64)).exists())
			return "Could not find " + libraryName + ext32 + " or " + libraryName + ext64 + " in<br>" + libraryPath
					+ "!";
		else if (!ext.isEmpty() && !(new File(libraryPath + libraryName + ext)).exists())
			return "Could not find " + libraryName + ext + " in<br>" + libraryPath + "!";

		// on Windows and on Linux it is a bit hard to figure out if the Java runtime is
		// 32 bit or 64 bit
		// therefore try to load both versions of the library if necessary
		String errorMessage = "";
		boolean try64bit;
		if (!ext32.isEmpty() && !ext64.isEmpty()) {
			try64bit = true;
			if ((new File(libraryPath + libraryName + ext32)).exists())
				try {
					System.load(libraryPath + libraryName + ext32);
					try64bit = false;
				} catch (UnsatisfiedLinkError unsatisfiedLinkError) {
					errorMessage = unsatisfiedLinkError.getMessage();
				}
			if (try64bit && (new File(libraryPath + libraryName + ext64)).exists())
				try {
					System.load(libraryPath + libraryName + ext64);
					errorMessage = "";
				} catch (UnsatisfiedLinkError unsatisfiedLinkError) {
					errorMessage = errorMessage + "<br>" + unsatisfiedLinkError.getMessage();
				}
		} else if (!ext.isEmpty())
			try {
				System.load(libraryPath + libraryName + ext);
			} catch (UnsatisfiedLinkError unsatisfiedLinkError) {
				errorMessage = unsatisfiedLinkError.getMessage();
			}

		return errorMessage;

	}

}
