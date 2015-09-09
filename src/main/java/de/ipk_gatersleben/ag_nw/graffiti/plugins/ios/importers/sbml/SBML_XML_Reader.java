/**
 * This class reads in SBML files
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import org.ErrorMsg;
import org.apache.log4j.Logger;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.io.AbstractInputSerializer;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLError;
import org.sbml.jsbml.SBMLErrorLog;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.validator.SBMLValidator.CHECK_CATEGORY;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Logger;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_XML_ReaderWriterPlugin;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskGUIprovider;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskPanelEntry;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class SBML_XML_Reader extends AbstractInputSerializer {
	
	static Logger logger = Logger.getLogger(SBML_XML_Reader.class);
	
	static boolean doValidate = false;
	
	public SBML_XML_Reader() {
		// System.out.println("SBML_XML_Reader with layout constructor");
		// TODO Auto-generated constructor stub
	}
	
	public static void doValidateSBMLOnLoad(boolean validate) {
		doValidate = validate;
	}
	
	public static boolean isValidatingSBMLOnLoad() {
		return doValidate;
	}
	
	/**
	 * Method controls the import of the SBML document.
	 * 
	 * @param document
	 *           contains the data to be imported.
	 * @param g
	 *           is the data structure for reading in the information.
	 */
	
	public void read(final SBMLDocument document, Graph g,
			BackgroundTaskStatusProviderSupportingExternalCallImpl status) {
		
		try {
			
			boolean readIn = false;
			URL url = new URL("http://sbml.org/Facilities/Validator/");
			// URLConnection connection = url.openConnection();
			URLConnection connection;
			InputStream is = null;
			try {
				connection = url.openConnection();
				connection.setConnectTimeout(15000);
				connection.setReadTimeout(15000);
				is = connection.getInputStream();
			} catch (Exception e) {
				// SBML_Logger.addErrorMessage("No internet connection");
				readIn = true;
				/*
				 * JOptionPane.showMessageDialog(null,
				 * "Online validation not possible.");
				 */
			}
			
			if (null != is) {
				
				int validate = 1;
				if (!SBML_XML_ReaderWriterPlugin.isTestintMode && doValidate)
					validate = JOptionPane
							.showConfirmDialog(
									null,
									"Do you want to validate the SBML file against the Level 3 Version 1 specification?");
				if (validate == 0) {
					
					document.setConsistencyChecks(
							CHECK_CATEGORY.GENERAL_CONSISTENCY, true);
					document.setConsistencyChecks(
							CHECK_CATEGORY.IDENTIFIER_CONSISTENCY, true);
					document.setConsistencyChecks(
							CHECK_CATEGORY.UNITS_CONSISTENCY, true);
					document.setConsistencyChecks(
							CHECK_CATEGORY.MATHML_CONSISTENCY, true);
					document.setConsistencyChecks(
							CHECK_CATEGORY.MODELING_PRACTICE, true);
					document.setConsistencyChecks(
							CHECK_CATEGORY.SBO_CONSISTENCY, true);
					document.setConsistencyChecks(
							CHECK_CATEGORY.OVERDETERMINED_MODEL, true);
					
					int numberOfErrors = document.checkConsistency();
					if (numberOfErrors > 0) {
						SBMLErrorLog errorLog = document.getListOfErrors();
						for (int i = 0; i < numberOfErrors; i++) {
							SBML_Logger.addErrorMessage(errorLog.getError(i));
						}
					}
					if (numberOfErrors > 0) {
						int load = JOptionPane
								.showConfirmDialog(null,
										"The online validator detected mistakes in the file. Load anyway?");
						if (load == 0) {
							readIn = true;
						}
					}
					if (numberOfErrors == 0) {
						readIn = true;
					}
				}
				if (validate == 1) {
					readIn = true;
				}
				
				is.close();
			} else {
				readIn = true;
				JOptionPane.showMessageDialog(null,
						"Online validation not possible.");
			}
			
			// to indicate an possible Exception - for example
			// NullPointerException
			
			if (readIn) {
				SBML_SBML_Reader readSBML = new SBML_SBML_Reader();
				readSBML.addSBML(document, g);
				SBML_Model_Reader readModel = new SBML_Model_Reader();
				readModel.controlImport(document, g, status);
				SBMLErrorLog errors = document.getListOfErrors();
				List<SBMLError> validationErrors = errors.getValidationErrors();
				for (SBMLError sbmlError : validationErrors) {
					SBML_Logger.addErrorMessage(sbmlError.getMessage() + ": at "
							+ sbmlError.getLine()
							+ "\n near by: " + sbmlError.getExcerpt());
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			SBML_Logger.addErrorMessage(e.getMessage());
		}
	}
	
	public void read(final Reader reader, final Graph g) throws Exception {
		long starttime = System.currentTimeMillis();
		
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				"Load SBML", "");
		final BackgroundTaskGUIprovider taskWindow;
		taskWindow = new BackgroundTaskPanelEntry(false);
		taskWindow.setStatusProvider(status, "SBML Loader",
				"loading from reader");
		
		final MainFrame mf = GravistoService.getInstance().getMainFrame();
		if (mf != null)
			mf.addStatusPanel((JPanel) taskWindow);
		
		try {
			
			status.setCurrentStatusText2("Please wait. This may take a few moments.");
			
			SAXBuilder builder = new SAXBuilder();
			Document document;
			
			SBMLDocument sbmlDocument;
			
			document = builder.build(reader);
			if (status.wantsToStop()) {
				logger.debug("aborting load");
				taskWindow.setTaskFinished(true, 0);
				return;
			}
			SBMLReader sbmlReader = new SBMLReader();
			status.setCurrentStatusText1("loading SBML document");
			status.setCurrentStatusValue(30);
			try {
				logger.info("start parsing sbml documet (Reader)");
				sbmlDocument = sbmlReader.readSBMLFromString(document
						.toString());
				
				if (sbmlDocument != null) {
					if (status.wantsToStop()) {
						logger.debug("aborting load");
						taskWindow.setTaskFinished(true, 0);
						return;
					}
					status.setCurrentStatusText1("creating SBML graph");
					status.setCurrentStatusValue(60);
					read(sbmlDocument, g, status);
					if (status.wantsToStop()) {
						logger.debug("aborting load");
						taskWindow.setTaskFinished(true, 0);
						return;
					}
					status.setCurrentStatusText1("adding graph to window");
					status.setCurrentStatusValue(90);
					
					status.setCurrentStatusText1("building graph");
				} else {
					ErrorMsg
							.addErrorMessage("Document can not be loaded. Check the document manually with the online validator (http://sbml.org/Facilities/Validator/).");
				}
			} catch (XMLStreamException e) {
				SBML_Logger.addErrorMessage(e);
			}
		} catch (JDOMException e) {
			SBML_Logger.addErrorMessage(e);
		} catch (IOException e) {
			SBML_Logger.addErrorMessage(e);
		} catch (Exception e) {
			SBML_Logger.addErrorMessage(e);
		}
		long endtime = System.currentTimeMillis();
		taskWindow.setTaskFinished(true, endtime - starttime);
		
	}
	
	public void read(final InputStream in, final Graph g) throws IOException {
		
		long starttime = System.currentTimeMillis();
		
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				"Load SBML", "");
		final BackgroundTaskGUIprovider taskWindow;
		taskWindow = new BackgroundTaskPanelEntry(false);
		taskWindow.setStatusProvider(status, "SBML Loader",
				"loading from stream");
		
		final MainFrame mf = GravistoService.getInstance().getMainFrame();
		if (mf != null)
			mf.addStatusPanel((JPanel) taskWindow);
		
		try {
			status.setCurrentStatusText2("Please wait. This may take a few moments.");
			
			SBMLReader reader = new SBMLReader();
			SBMLDocument document = null;
			status.setCurrentStatusText1("loading SBML document");
			status.setCurrentStatusValue(30);
			logger.info("start parsing sbml documet  (InputStream)");
			document = reader.readSBMLFromStream(in);
			if (document != null) {
				if (status.wantsToStop()) {
					logger.debug("aborting load");
					taskWindow.setTaskFinished(true, 0);
					return;
				}
				read(document, g, status);
				if (status.wantsToStop()) {
					logger.debug("aborting load");
					taskWindow.setTaskFinished(true, 0);
					return;
				}
				logger.debug("done reading graph");
				status.setCurrentStatusText1("adding graph to window");
				status.setCurrentStatusValue(100);
			} else {
				logger.info("the sbml document is not valid.");
				ErrorMsg
						.addErrorMessage("Document can not be loaded. Check the document manually with the online validator (http://sbml.org/Facilities/Validator/).");
			}
		} catch (XMLStreamException error) {
			
			SBML_Logger.addErrorMessage(error);
			
			logger.error(error.getMessage());
			try {
				error.printStackTrace(new PrintWriter("stack_trace.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			error.printStackTrace();
			/*
			 * due to a bug in the vanted boot process the SBML reader is broken
			 * and throws classcast exceptions.. reason yet unknown
			 * This will catch it and ask for an update
			 * The boot process has been fixed but the true reason is still unknown
			 */
		} catch (ClassCastException cce) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>Due to a bug in the VANTED start process<br/>"
					+ "the loading of SBML documents is broken.<br/>"
					+ "Please go to the <strong>vanted.org</strong> website and download<br/>"
					+ "and install the fixed version.",
					"VANTED Bug", JOptionPane.ERROR_MESSAGE);
			
		}
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			SBML_Logger.addErrorMessage(e);
		}
		long endtime = System.currentTimeMillis();
		taskWindow.setTaskFinished(true, endtime - starttime);
		
	}
	
	@Override
	public boolean validFor(InputStream reader) {
		return validSBML(reader);
	}
	
	Boolean validSBML(InputStream in) {
		InputStreamReader reader = new InputStreamReader(in);
		
		BufferedReader bufferedReader = new BufferedReader(reader);
		StringBuffer finalString = new StringBuffer();
		try {
			while (bufferedReader.ready()) {
				finalString = finalString.append(bufferedReader.readLine());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String content = finalString.toString();
		content = content.trim();
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			bufferedReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (content.contains("sbml") && content.contains("model")
				&& content.contains("version") && content.contains("level")) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Implemented method of interface InputSerializer.java Returns the
	 * extension of files that can be read in with the SBML importer
	 */
	public String[] getExtensions() {
		
		String[] ext = new String[] { ".sbml", ".xml" };
		return ext;
	}
	
	/**
	 * Implemented method of interface InputSerializer.java Returns the
	 * description of the input format
	 */
	public String[] getFileTypeDescriptions() {
		String[] desc = new String[] { "SBML", "SBML" };
		return desc;
	}
	
}