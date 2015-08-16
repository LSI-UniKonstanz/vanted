/**
 * Adaptagrams Edge Routing Plugin class.
 * Copyright (c) 2014-2015 Monash University, Australia
 */
package org.vanted.plugins.layout.adaptagrams.edgerouting;

import java.io.File;

import org.ReleaseInfo;
import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;
import org.vanted.plugins.layout.adaptagrams.AdaptagramsLibrary;

import de.ipk_gatersleben.ag_nw.graffiti.FileHelper;

/**
 * @author Tobias Czauderna
 */
@SuppressWarnings("nls")
public class EdgeRoutingPlugin extends GenericPluginAdapter {
	
	/**
	 * Initializes the edge routing plugin.
	 * Registers the edge routing algorithm and copies the native Adaptagrams libraries if necessary.
	 */
	public EdgeRoutingPlugin() {
		
		// register the edge routing algorithm
		this.algorithms = new Algorithm[] { new EdgeRoutingAlgorithm() };
		
		// initial step to use the Adaptagrams libraries in Vanted
		// search the adaptagrams.jar
		String classPath = System.getProperty("java.class.path");
		String[] classPathEntries = classPath.split(File.pathSeparator);
		String jarFileName = null;
		for (String entry : classPathEntries)
			if (entry.contains("adaptagrams.jar"))
				jarFileName = entry;
		String sourceFolder = "libs";
		String targetFolder = ReleaseInfo.getAppSubdirFolderWithFinalSep("plugins", "Adaptagrams");
		String[] libNames = AdaptagramsLibrary.getLibraryNames();
		// copy native Adaptagrams libraries from the jar to folder on the file system
		// (compares and copies only new versions of the libraries)
		if (jarFileName != null && libNames != null)
			FileHelper.copyFilesFromJar(jarFileName, sourceFolder, targetFolder, libNames);
		
	}
	
}
