/**
 * This class provides methods to load the native Adaptagrams libraries.
 * Copyright (c) 2014-2025 Monash University, Australia
 *               2025      University of Applied Sciences Mittweida, Germany
 */
package org.vanted.plugins.layout.adaptagrams;

import java.io.File;

/**
 * @author Tobias Czauderna
 */
public class AdaptagramsLibrary {

	/**
	 * Version of the Adaptagrams library in Vanted
	 */
	public static final String VERSION = "1.1";
	
	/**
	 * Return the version of the Adaptagrams library in Vanted.
	 *
	 * @return version of the Adaptagrams library in Vanted
	 */
	public static String getVersion() {
		
		return VERSION;
		
	}
	
	/**
	 * Return the names of the native Adaptagrams libraries for the different
	 * operating systems. There is one library for Windows and one library 
	 * for Linux. There are two libraries for for MacOS (x64 and arm64).
	 * 
	 * @return library names for the different operating systems
	 */
	public static String[] getLibraryNames() {
		
		// the layout library is available on Windows, Linux, and Mac OS (x64 and arm64)
		String[] availableOSs = new String[] { "windows", "linux", "mac" };
		String osName = System.getProperty("os.name");
		String[] libraryNames = null;
		if (osName.toLowerCase().contains(availableOSs[0]))
			libraryNames = new String[] { "adaptagrams.dll" };
		else if (osName.toLowerCase().contains(availableOSs[1]))
			libraryNames = new String[] { "adaptagrams.so" };
		else if (osName.toLowerCase().contains(availableOSs[2]))
			libraryNames = new String[] { "adaptagramsx64.dylib", "adaptagramsaarch64.dylib" };
		return libraryNames;
		
	}
	
	/**
	 * Load native Adaptagrams layout library. Tries to load the layout library from
	 * the working directory.
	 * 
	 * @param libraryName
	 *           name of the library
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
	 *           name of the library
	 * @param libraryPath
	 *           path to the library
	 * @return error message
	 */
	public static String loadLibrary(String libraryName, String libraryPath) {
		
		// the layout library is available on x64 (Windows, Linux, MacOS) and arm64 architectures (MacOS)
		String[] availableArchitectures = new String[] { "amd64", "x64", "x86_64", "aarch64" };
		String osArch = System.getProperty("os.arch");
		// the layout library is available on Windows, Linux, and Mac OS
		String[] availableOSs = new String[] { "windows", "linux", "mac" };
		String windowsExt = ".dll";
		String linuxExt = ".so";
		// two libraries are available for MacOS (x64 and arm64)
		String macExtx64 = "x64.dylib";
		String macExtaarch64 = "aarch64.dylib";
		String ext = "";
		String extx64 = "";
		String extaarch64 = "";
		String osName = System.getProperty("os.name");
		
		// for debugging
		// System.out.println("Current architecture: \"" + osArch + "\"");
		// System.out.println("Current OS: \"" + osName + "\"");
		
		// check whether the library is available for OS
		if (!osName.toLowerCase().contains(availableOSs[0]) && !osName.toLowerCase().contains(availableOSs[1]) && !osName.toLowerCase().contains(availableOSs[2]))
			return "Layout library not available for " + osName + "!";
		
		// check whether the library is available for architecture
		if (!osArch.toLowerCase().contains(availableArchitectures[0]) && !osArch.toLowerCase().contains(availableArchitectures[1]) && !osArch.toLowerCase().contains(availableArchitectures[2]))
			return "Layout library not available for " + osName + "!";
		
		if (osName.toLowerCase().contains(availableOSs[0]))
			ext = windowsExt;
		else if (osName.toLowerCase().contains(availableOSs[1]))
			ext = linuxExt;
		else if (osName.toLowerCase().contains(availableOSs[2])) {
			extx64 = macExtx64;
			extaarch64 = macExtaarch64;
		}
		
		// check whether the library can be found
		if (!ext.isEmpty() && !(new File(libraryPath + libraryName + ext)).exists())
			return "Could not find " + libraryName + ext + " in<br>" + libraryPath;
		else if (!extx64.isEmpty() && !extaarch64.isEmpty() && !(new File(libraryPath + libraryName + extx64)).exists() && !(new File(libraryPath + libraryName + extaarch64)).exists())
			return "Could not find " + libraryName + extx64 + " or " + libraryName + extaarch64 + " in<br>" + libraryPath;
		
		String errorMessage = "";
		// try to load library
		if (!ext.isEmpty())
			try {
				System.load(libraryPath + libraryName + ext);
			} catch (UnsatisfiedLinkError unsatisfiedLinkError) {
				errorMessage = unsatisfiedLinkError.getMessage();
			}
		// try to load both versions of the library on MacOS if necessary
		else if (!extx64.isEmpty() && !extaarch64.isEmpty()) {
			boolean tryaarch64 = true;
			if ((new File(libraryPath + libraryName + extx64)).exists())
				try {
					System.load(libraryPath + libraryName + extx64);
					tryaarch64 = false;
				} catch (UnsatisfiedLinkError unsatisfiedLinkError) {
					errorMessage = unsatisfiedLinkError.getMessage();
				}
			if (tryaarch64 && (new File(libraryPath + libraryName + extaarch64)).exists())
				try {
					System.load(libraryPath + libraryName + extaarch64);
					errorMessage = "";
				} catch (UnsatisfiedLinkError unsatisfiedLinkError) {
					errorMessage = errorMessage + "<br>" + unsatisfiedLinkError.getMessage();
				}
		}
		
		return errorMessage;
		
	}
	
}
