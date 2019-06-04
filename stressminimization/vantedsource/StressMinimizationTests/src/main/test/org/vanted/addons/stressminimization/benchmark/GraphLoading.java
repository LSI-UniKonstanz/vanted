package org.vanted.addons.stressminimization.benchmark;

import java.io.File;
import java.io.IOException;

import org.graffiti.graph.Graph;
import org.graffiti.managers.DefaultIOManager;
import org.graffiti.managers.IOManager;
import org.graffiti.managers.MyInputStreamCreator;
import org.graffiti.plugin.io.InputSerializer;

public class GraphLoading {

	private static GraphLoading instance;
	
	public static GraphLoading getInstance() {
		if (instance == null) {
			instance = new GraphLoading();
		}
		return instance;
	}
	
	private IOManager ioManager;
	
	private GraphLoading() {
		ioManager = new DefaultIOManager();
	}
	
	public Graph loadGraph(File file) {
		
		Graph newGraph = null;
	
		String fileName = file.getName();
		String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";

		try {
			InputSerializer is;
			MyInputStreamCreator inputStream = new MyInputStreamCreator(false, file.getAbsolutePath());
			is = ioManager.createInputSerializer(inputStream.getNewInputStream(), ext);
			synchronized (ioManager) {
				newGraph = is.read(inputStream.getNewInputStream());
			}
		} catch (IllegalAccessException | InstantiationException | IOException e) {
			System.err.println("Could not load graph from file: " + file.getAbsolutePath());
		}
			
		if (newGraph != null) {
			newGraph.setName(file.getAbsolutePath());
			newGraph.setModified(false);
		}
	
		return newGraph;
		
	}
	
}
