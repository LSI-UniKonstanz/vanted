/**
 * This class provides methods to load the native Adaptagrams libraries.
 * Copyright (c) 2014-2025 Monash University, Australia
 *               2025      University of Applied Sciences Mittweida, Germany
 */
package org.vanted.plugins.layout.adaptagrams;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.ReleaseInfo;

/**
 * Loads the native Adaptagrams JNI library shipped inside adaptagrams.jar.
 * <p>
 * The jar carries one shared library per platform below
 * <code>native/&lt;os&gt;-&lt;arch&gt;/</code>, named the way
 * {@link System#mapLibraryName(String)} expects it. A shared library cannot be
 * loaded out of a jar, so the matching file is unpacked into the Vanted user
 * folder once and loaded from there.
 *
 * @author Tobias Czauderna
 */
public class AdaptagramsLibrary {
	
	/**
	 * Version of the Adaptagrams library in Vanted
	 */
	public static final String VERSION = "1.1";
	
	private static final String LIBRARY_NAME = "adaptagrams";
	
	/**
	 * Layout used by adaptagrams.jar before 2026, kept so that an older jar keeps
	 * working.
	 */
	private static final String LEGACY_FOLDER = "libs";
	
	private static boolean loaded = false;
	
	/**
	 * Return the version of the Adaptagrams library in Vanted.
	 *
	 * @return version of the Adaptagrams library in Vanted
	 */
	public static String getVersion() {
		
		return VERSION;
		
	}
	
	/**
	 * Unpack and load the native Adaptagrams layout library for the current
	 * platform. Repeated calls are cheap, the library is loaded once per JVM.
	 *
	 * @return empty string on success, otherwise a message describing what failed
	 */
	public static synchronized String loadLibrary() {
		
		if (loaded)
			return "";
		
		String platform = getPlatform();
		if (platform == null)
			return "Layout library not available for " + System.getProperty("os.name") + " ("
					+ System.getProperty("os.arch") + ")!";
		
		String fileName = System.mapLibraryName(LIBRARY_NAME);
		String resource = "native/" + platform + "/" + fileName;
		ClassLoader classLoader = AdaptagramsLibrary.class.getClassLoader();
		if (classLoader.getResource(resource) == null) {
			// older adaptagrams.jar
			String legacyResource = getLegacyResource(platform);
			if (legacyResource == null || classLoader.getResource(legacyResource) == null)
				return "Could not find " + resource + " in adaptagrams.jar!";
			resource = legacyResource;
			fileName = legacyResource.substring(legacyResource.lastIndexOf('/') + 1);
		}
		
		try {
			Path libraryFile = unpack(classLoader, resource, platform, fileName);
			String path = libraryFile.toAbsolutePath().toString();
			// The jar carries its own loader (org.adaptagrams.NativeLoader), which runs
			// from the static initializer of the SWIG class and would otherwise unpack a
			// second copy into a temp folder. This property points it at the file
			// unpacked here; its System.load on the same path is then a no-op.
			System.setProperty("adaptagrams.library.path", path);
			System.load(path);
			loaded = true;
			return "";
		} catch (IOException ioException) {
			return "Could not unpack the layout library:<br>" + ioException.getMessage();
		} catch (UnsatisfiedLinkError unsatisfiedLinkError) {
			return "Could not load the layout library:<br>" + unsatisfiedLinkError.getMessage();
		}
		
	}
	
	/**
	 * Copy the library out of the jar into the Vanted user folder, into a
	 * per-platform subfolder so that a home directory shared between machines
	 * doesn't mix up architectures. Copies only if the file is missing or differs
	 * in size, unpacking tens of megabytes on every start would be wasteful.
	 *
	 * @param classLoader
	 *           class loader holding the jar
	 * @param resource
	 *           path of the library within the jar
	 * @param platform
	 *           platform key, e.g. linux-x86_64
	 * @param fileName
	 *           name of the library file
	 * @return path of the unpacked library
	 * @throws IOException
	 *            if the library cannot be read or written
	 */
	private static Path unpack(ClassLoader classLoader, String resource, String platform, String fileName)
			throws IOException {
		
		Path folder = Paths.get(ReleaseInfo.getAppSubdirFolderWithFinalSep("plugins", "Adaptagrams"), platform);
		Files.createDirectories(folder);
		Path libraryFile = folder.resolve(fileName);
		
		URL url = classLoader.getResource(resource);
		long size = url.openConnection().getContentLengthLong();
		if (Files.exists(libraryFile) && size >= 0 && Files.size(libraryFile) == size)
			return libraryFile;
		
		try (InputStream inputStream = classLoader.getResourceAsStream(resource)) {
			if (inputStream == null)
				throw new IOException("Could not read " + resource + " from adaptagrams.jar!");
			Files.copy(inputStream, libraryFile, StandardCopyOption.REPLACE_EXISTING);
		}
		return libraryFile;
		
	}
	
	/**
	 * Return the platform key for the running JVM, matching the folder names in
	 * adaptagrams.jar.
	 *
	 * @return platform key, e.g. mac-aarch64, or null if the platform is not
	 *         supported
	 */
	private static String getPlatform() {
		
		String osName = System.getProperty("os.name", "").toLowerCase();
		String osArch = System.getProperty("os.arch", "").toLowerCase();
		
		String os;
		if (osName.contains("windows"))
			os = "windows";
		else if (osName.contains("linux"))
			os = "linux";
		else if (osName.contains("mac"))
			os = "mac";
		else
			return null;
		
		String arch;
		if (osArch.equals("amd64") || osArch.equals("x86_64") || osArch.equals("x64"))
			arch = "x86_64";
		else if (osArch.equals("aarch64") || osArch.equals("arm64"))
			arch = "aarch64";
		else
			return null;
		
		return os + "-" + arch;
		
	}
	
	/**
	 * Return the library path within an older adaptagrams.jar.
	 *
	 * @param platform
	 *           platform key
	 * @return path within the jar, or null if that jar never carried the platform
	 */
	private static String getLegacyResource(String platform) {
		
		if (platform.startsWith("windows"))
			return LEGACY_FOLDER + "/" + LIBRARY_NAME + ".dll";
		if (platform.startsWith("linux"))
			return LEGACY_FOLDER + "/" + LIBRARY_NAME + ".so";
		if (platform.equals("mac-x86_64"))
			return LEGACY_FOLDER + "/" + LIBRARY_NAME + "x64.dylib";
		if (platform.equals("mac-aarch64"))
			return LEGACY_FOLDER + "/" + LIBRARY_NAME + "aarch64.dylib";
		return null;
		
	}
	
}
