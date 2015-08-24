/**
 * 
 */
package org.vanted.updater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import org.ReleaseInfo;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.graffiti.editor.MainFrame;
import org.graffiti.managers.PreferenceManager;

import de.ipk_gatersleben.ag_nw.graffiti.FileHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;

/**
 * @author matthiak
 *         Update file has very simple format:
 *         left type of file, right hand side url to file
 *         # comment line
 *         version: 2.5.0
 *         core: vanted-core.jar
 *         core-lib: lib-name.jar
 *         // # end of update entry
 */
public class ScanForUpdate {
	
	/**
	 * 
	 */
	private static final int TIME_REMINDER_DAYS = 7;
	
	private static final Logger logger = Logger.getLogger(ScanForUpdate.class);
	
	private static final String[] arrDialogOptions = {
			"Update now",
			"Remind me in 7 days"
	};
	
	/**
	 * 
	 */
	private static final String REMINDER_DATE = "reminder-date";
	/**
	 * 
	 */
	private static final String VERSIONSTRING = "version";
	private static final String CORESTRING = "core";
	private static final String CORELIBSTRING = "core-lib";
	
	private static final String urlUpdateBaseString = "https://immersive-analytics.infotech.monash.edu/vanted-new2.5/release/updates/";
	private static final String urlUpdateFileString = urlUpdateBaseString + "vanted-update";
	
	private static final String destPathUpdateDir = ReleaseInfo.getAppFolderWithFinalSep() + "update/";
	private static final String destUpdateFile = ReleaseInfo.getAppFolderWithFinalSep() + "do-vanted-update";
	private static final String vantedUpdateOKFile = ReleaseInfo.getAppFolderWithFinalSep() + "vanted-update-ok";
	
	public static void doScan() throws IOException {
		
		cleanPreviousUpdate();
		
		Date currentDate = new Date();
		
		String version = null;
		boolean prepareUpdate = false;
		
		String strCoreProgramRelativePath = null;
		List<String> listStrCoreLibsRelativePaths = new ArrayList<String>();
		
		URL updateURL = new URL(urlUpdateFileString);
		InputStream openStream = updateURL.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(openStream));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.equals("//"))
				break;
			if (line.startsWith("#"))
				continue;
			if (line.toLowerCase().startsWith(VERSIONSTRING)) {
				version = line.substring(VERSIONSTRING.length() + 2).trim();
				prepareUpdate = updateIsNewer(version);
			}
			if (line.toLowerCase().startsWith(CORESTRING)) {
				strCoreProgramRelativePath = line.substring(CORESTRING.length() + 2).trim();
			}
			if (line.toLowerCase().startsWith(CORELIBSTRING)) {
				listStrCoreLibsRelativePaths.add(line.substring(CORELIBSTRING.length() + 2).trim());
			}
			
		}
		
		if (logger.getLevel() == Level.DEBUG) {
			logger.debug("We found an update.. printing locations:");
			System.out.println("Core: " + strCoreProgramRelativePath);
			for (String libPath : listStrCoreLibsRelativePaths) {
				System.out.println(" lib: " + libPath);
			}
		}
		
		// the update we found is not newer
		if (!prepareUpdate)
			return;
		
		// if there is preference entry for reminder time..  check
		boolean timeout = false;
		Preferences preferenceForClass = PreferenceManager.getPreferenceForClass(ScanForUpdate.class);
		String strReminderDate = preferenceForClass.get(REMINDER_DATE, null);
		if (strReminderDate != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			try {
				Date storedDate = dateFormat.parse(strReminderDate);
				
				if (currentDate.after(storedDate))
					timeout = true;
			} catch (ParseException e) {
				if (logger.getLevel() == Level.DEBUG)
					e.printStackTrace();
				timeout = true;
			}
			// if we're still not after the X days of reminder.. don't ask the user
			if (!timeout)
				return;
		}
		// popup dialog telling user, there is a new version
		// download now or later
		// or go to website (short version)
		Object dialogAskUpdate = JOptionPane.showOptionDialog(MainFrame.getInstance(),
				"<html>A new update to VANTED " + version + " is available<br/><br/>"
						+ "You can download it now or be reminded later.<br/><br/>"
						+ "The update will be installed during the next startup.",
				"Update available",
				JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_OPTION,
				null,
				arrDialogOptions,
				arrDialogOptions[0]);
		
		//later - create entry in preferences for reminder date
		if (dialogAskUpdate == null || (dialogAskUpdate != null && arrDialogOptions[1].equals(dialogAskUpdate))) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			Date addDays = DateUtils.addDays(currentDate, TIME_REMINDER_DAYS);
			preferenceForClass.put(REMINDER_DATE, dateFormat.format(addDays));
			return;
		}
		
		// if ok.. download files
		// create directories and 
		File updateDir = new File(destPathUpdateDir);
		if (!updateDir.exists())
			updateDir.mkdirs();
		else {
			logger.error("directory should never previously exist.. bootstrapupdater failed");
		}
		
		// create a copy file for the bootstrap program
		// that will put the files to the right place
		// .. the next thing is for the bootstrap
		
		File updateFile = new File(destUpdateFile);
		BufferedWriter writer = new BufferedWriter(new FileWriter(updateFile));
		
		writer.write(VERSIONSTRING + ":" + version);
		writer.newLine();
		if (strCoreProgramRelativePath != null) {
			FileHelper.downloadFile(new URL(urlUpdateBaseString + strCoreProgramRelativePath), destPathUpdateDir, extractFileName(strCoreProgramRelativePath));
			writer.write(CORESTRING + ":" + strCoreProgramRelativePath);
			writer.newLine();
		}
		
		for (String libPath : listStrCoreLibsRelativePaths) {
			
			FileHelper.downloadFile(new URL(urlUpdateBaseString + libPath), destPathUpdateDir, extractFileName(libPath));
			writer.write(CORESTRING + ":" + libPath);
			writer.newLine();
		}
		
		writer.close();
		// bootstrap will look for that file and does his work 
		// 
		
	}
	
	private static String extractFileName(String path) {
		int idx = path.lastIndexOf("/");
		return path.substring(idx + 1);
	}
	
	/**
	 * This will check, if the bootstrap was successfully
	 * updating vanted by checking an OK file and then
	 * delete the update folder
	 */
	private static void cleanPreviousUpdate() {
		File checkfile = new File(vantedUpdateOKFile);
		if (checkfile.exists())
			FileHelper.deleteDirRecursively(new File(destPathUpdateDir));
	}
	
	private static boolean updateIsNewer(String remoteVersion) {
		boolean isNewer = false;
		
		String[] strSplitMainversion = DBEgravistoHelper.DBE_GRAVISTO_VERSION_CODE.split("\\.");
		
		long[] longArrMainVersion = new long[strSplitMainversion.length];
		
		for (int i = 0; i < strSplitMainversion.length; i++) {
			longArrMainVersion[i] = Long.parseLong(strSplitMainversion[i]);
		}
		
		String[] strSplitRemoteVersion = remoteVersion.split("\\.");
		for (int i = 0; i < strSplitRemoteVersion.length; i++) {
			long longPartVersion;
			try {
				longPartVersion = Long.parseLong(strSplitRemoteVersion[i]);
			} catch (NumberFormatException e) {
				longPartVersion = longArrMainVersion[i];
			};
			/*
			 * if the main / sub version is definitely larger then the min-main/sub version
			 * we don't need to check further.
			 */
			if (longPartVersion > longArrMainVersion[i]) {
				return isNewer;
				
			}
			
		}
		
		return isNewer;
	}
}
