/**
 * 
 */
package org;

import java.io.File;

import javax.swing.JOptionPane;

import org.graffiti.editor.MainFrame;

/**
 * @author matthiak
 *
 */
public class UNCFileLocationCheck {
	
	public static int CONFIRM = JOptionPane.OK_OPTION;
	public static int DECLINE = JOptionPane.NO_OPTION;

	private static String message = 
			"You're going to save the file on a windows network location\n"
			+ "You might experience delays on saving at that location\n\n"
			+ "Do you want to store at that location anyway?" ;
	
	public static boolean isUNCFilePath(File file) {
		return UNCFileLocationCheck.isUNCFilePath(file.getPath());
	}

	public static boolean isUNCFilePath(String path) {
		
		if( path != null && !path.isEmpty() && path.startsWith("\\") ) {
			return true;
		} else
			return false;
	}

	/**
	 * Checks, if the given file is to be stored at a UNC network path.
	 * If not, it returns JOptionPane.OK_OPTION
	 * If yes, it opens a dialog and let the user decide, if the UNC location is ok.
	 * @param file The File to check
	 * @return UNCFileLocationCheck.CONFIRM if UNC location is accepted
	 * 			UNCFileLocationCheck.DECLINE if UNC location is declined
	 */
	public static int showUNCPathConfirmDialogForPath(File file) {
		return UNCFileLocationCheck.showUNCPathConfirmDialogForPath(file.getPath());
	}
	
	/**
	 * Checks, if the given path is a UNC network path.
	 * If not, it returns JOptionPane.OK_OPTION
	 * If yes, it opens a dialog and let the user decide, if the UNC location is ok.
	 * @param path The path to check
	 * @return UNCFileLocationCheck.CONFIRM if UNC location is accepted
	 * 			UNCFileLocationCheck.DECLINE if UNC location is declined
	 */
	public static int showUNCPathConfirmDialogForPath(String path) {
		if(UNCFileLocationCheck.isUNCFilePath(path))
			return JOptionPane.showConfirmDialog(MainFrame.getInstance(), 
					message, "Storing location Question", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		else
			
			return JOptionPane.OK_OPTION;
	}

}