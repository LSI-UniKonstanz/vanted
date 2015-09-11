/**
 * Adaptagrams Edge Routing Plugin class.
 * Copyright (c) 2014-2015 Monash University, Australia
 */
package org.vanted.plugins.layout.adaptagrams.edgerouting;

import java.net.URI;
import java.net.URL;

import org.ErrorMsg;
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
		// copy native Adaptagrams libraries from the jar to folder on the file system
		String sourceFolder = "libs";
		String targetFolder = ReleaseInfo.getAppSubdirFolderWithFinalSep("plugins", "Adaptagrams");
		String[] libNames = AdaptagramsLibrary.getLibraryNames();
		URL url = null;
		int k = 0;
		// the native libraries are all in the same jar, it's enough to find the url for one library
		while (url == null && k < libNames.length) {
			url = this.getClass().getClassLoader().getResource(sourceFolder + "/" + libNames[k]);
			k++;
		}
		if (url != null) {
			// no Java Web Start, access the libraries directly as files in the jar
			// copies only new versions of the libraries
			if (!url.toString().contains("http")) {
				String[] splitArr = url.toString().split("!");
				try {
					URI uri = URI.create(splitArr[0]);
					FileHelper.copyFilesFromJar(uri, sourceFolder, targetFolder, libNames);
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
			// Java Web Start, access the libraries from resource (as stream)
			// this will always copy the libraries since the files in the resource are always newer than the libraries on the file system
			else {
				for (String libName : libNames)
					if (this.getClass().getClassLoader().getResourceAsStream(sourceFolder + "/" + libName) != null)
						FileHelper.copyFileFromStream(sourceFolder, targetFolder, libName);
			}
		}
		
	}
	
}
