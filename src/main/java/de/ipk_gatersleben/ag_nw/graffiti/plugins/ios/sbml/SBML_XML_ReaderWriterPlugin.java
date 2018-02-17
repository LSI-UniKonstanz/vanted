/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.graffiti.plugin.io.InputSerializer;
import org.graffiti.plugin.io.OutputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_XML_Writer;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml.SBML_XML_Reader;

/**
 * Importer and Exporter of SBML format Add-on.
 * 
 * @author Christian Klukas
 * @vanted.revision 2.6.5
 */
public class SBML_XML_ReaderWriterPlugin extends AddonAdapter {
	/*
	 * This static variable is to be set to true, when this plug-in is doing
	 * a JUnit test. So, within the plug-in some code will not be executed,
	 * which includes GUI dialogs, etc.
	 */
	public static boolean isTestintMode = false;
	
	@Override
	protected void initializeAddon() {
		System.out.println("initializeAddon called!");
		try {
			//logOperation();
			SBML_XML_Reader reader = new SBML_XML_Reader();
			this.inputSerializers = new InputSerializer[] { reader };
			this.outputSerializers = new OutputSerializer[] { new SBML_XML_Writer() };
			
			System.out.println("SBML_XML_Reader Writer Plugin started");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}	
	}
	
	public SBML_XML_ReaderWriterPlugin() {
		super();
		initializeAddon();
	}
	
	private static Logger logger = Logger.getRootLogger();

	/**
	 * For testing.
	 * 
	 * @throws IOException by the FileAppender
	 */
	@SuppressWarnings("unused")
	private void logOperation() throws IOException {
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender( layout );
		logger.addAppender( consoleAppender );
		FileAppender fileAppender = new FileAppender(layout,
				"log/jsbml.log", false );
		logger.addAppender( fileAppender );
	}
}
