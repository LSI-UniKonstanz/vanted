// ==============================================================================
//
// FileSaveAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: FileSaveAction.java,v 1.15.2.1 2013/03/27 13:45:00 tczauderna Exp $

package org.graffiti.editor.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.help.HelpContext;
import org.graffiti.managers.IOManager;
import org.graffiti.plugin.actions.GraffitiAction;
import org.graffiti.plugin.io.OutputSerializer;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOManager;
import org.graffiti.plugin.view.SuppressSaveActionsView;
import org.graffiti.session.EditorSession;
import org.graffiti.session.SessionManager;

/**
 * The action for saving a graph.
 * 
 * @version $Revision: 1.15.2.1 $
 */
public class FileSaveAction extends GraffitiAction {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6260210660304675944L;
	
	/** DOCUMENT ME! */
	private final IOManager ioManager;
	
	/** DOCUMENT ME! */
	private final SessionManager sessionManager;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new FileSaveAction object.
	 * 
	 * @param mainFrame
	 *           DOCUMENT ME!
	 * @param ioManager
	 *           DOCUMENT ME!
	 * @param sessionManager
	 *           DOCUMENT ME!
	 */
	public FileSaveAction(MainFrame mainFrame, IOManager ioManager, SessionManager sessionManager) {
		super("file.save", mainFrame, "filemenu_save");
		this.ioManager = ioManager;
		this.sessionManager = sessionManager;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return <code>true</code>, if the io manager contains a working output
	 *         serializer and if the file is writeable.
	 */
	@Override
	public boolean isEnabled() {
		String fullName;
		String fileTypeDescription;
		EditorSession session = null;
		try {
			// these commands fail if the session has not yet been saved to
			// a file
			session = (EditorSession) mainFrame.getActiveSession();
			if (session == null)
				return false;
			fullName = session.getFileNameFull();
			fileTypeDescription = session.getFileTypeDescription();
		} catch (Exception e) {
			return false;
		}
		
		if (session != null && session.getActiveView() instanceof SuppressSaveActionsView)
			return false;
		
		try {
			String ext = getFileExt(fullName);
			
			File file = new File(fullName);
			
			if (file.canWrite()) {
				ioManager.createOutputSerializer("." + ext, fileTypeDescription);
				
				// runtime error check, if exception, ioManager can not
				// handle current file for saving.
			} else {
				if (true) // new method for vanted v2.1
					return false;
				IOurl url = new IOurl(fullName);
				if (ResourceIOManager.getHandlerFromPrefix(url.getPrefix()) != null)
					return true;
				else
					return false;
			}
		} catch (Exception e) {
			return false;
		}
		
		return (ioManager.hasOutputSerializer() && sessionManager.isSessionActive());
	}
	
	/**
	 * @see org.graffiti.plugin.actions.GraffitiAction#getHelpContext()
	 */
	@Override
	public HelpContext getHelpContext() {
		return null;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *           DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		// CK, 1.Juli.2003 Copied and modified from SaveAsAction
		EditorSession session;
		String fullName;
		String fileTypeDescription;
		
		try {
			session = (EditorSession) mainFrame.getActiveSession();
			if (session == null)
				throw new Exception("No active Session found");
			fullName = session.getFileNameFull();
			fileTypeDescription = session.getFileTypeDescription();
			if (session != null && session.getUndoManager() != null)
				session.getUndoManager().discardAllEdits();
		} catch (Exception err) {
			MainFrame.showMessageDialog("Could not save network.", "Error");
			ErrorMsg.addErrorMessage(err);
			
			// signal any graphs waiting on it to close
			MainFrame.getInstance().cancelledSaveAction.set(true);
			
			return;
		}
		
		String ext = getFileExt(fullName);
		
		File file = new File(fullName);
		
		if (file.canWrite()) {
			try {
				OutputSerializer os = ioManager.createOutputSerializer(ext, fileTypeDescription);
				if (os == null) {
					MainFrame.showMessageDialog("Unknown outputserializer for file extension " + ext, "Error");
				} else {
					os.write(new FileOutputStream(file), getGraph());
					FileHandlingManager.getInstance().throwFileSaved(file, ext, getGraph());
					getGraph().setModified(false);
					long fs = file.length();
					MainFrame.showMessage(
							"Network saved to file " + file.getAbsolutePath() + " (" + (fs / 1024) + "KB)",
							MessageType.INFO);
					// a recent menu entry will be already built, if a graph is loaded or "saved
					// as"... so we dont need to add a menu entry here
					// MainFrame.getInstance().addNewRecentFileMenuItem(file);
				}
			} catch (Exception ioe) {
				ErrorMsg.addErrorMessage(ioe);
				MainFrame.getInstance().warnUserAboutFileSaveProblem(ioe);
				
				// signal any graphs waiting on it to close
				MainFrame.getInstance().cancelledSaveAction.set(true);
			}
			
			mainFrame.fireSessionDataChanged(session);
		} else {
			// signal any graphs waiting on it to close
			MainFrame.getInstance().cancelledSaveAction.set(true);
			
			MainFrame.showMessageDialog("<html>Error: Network could not be saved (file not writeable).", "Error");
			System.err.println("Error: file not writable. (FileSave-Action).");
		}
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param fileName
	 * @return Returns file extension from a given filename.
	 */
	public static String getFileExt(String fileName) {
		String workName;
		
		int lastSep = fileName.lastIndexOf(File.pathSeparator);
		
		if (lastSep == -1) {
			// no extension
			workName = fileName;
		} else {
			workName = fileName.substring(lastSep + 1);
		}
		
		int lastDot = workName.lastIndexOf('.');
		
		if (lastDot == -1) {
			return "";
		} else {
			boolean gz = false;
			if (workName.toUpperCase().endsWith(".GZ")) {
				workName = workName.substring(0, workName.length() - ".gz".length());
				gz = true;
			}
			lastDot = workName.lastIndexOf('.');
			String extension = workName.substring(lastDot);
			if (gz)
				extension = extension + ".gz";
			return extension;
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
