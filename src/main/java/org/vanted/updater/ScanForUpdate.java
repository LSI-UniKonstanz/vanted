/**
 * 
 */
package org.vanted.updater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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

import org.ApplicationStatus;
import org.ErrorMsg;
import org.ReleaseInfo;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.graffiti.editor.MainFrame;
import org.graffiti.managers.PreferenceManager;
import org.graffiti.options.PreferencesInterface;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.StringParameter;

import de.ipk_gatersleben.ag_nw.graffiti.FileHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;

/**
 * @author matthiak
 *         Update file has very simple format:
 *         left type of file, right hand side url to file
 *         # comment line
 *         version: 2.5.0
 *         core: vanted-core.jar
 *         lib: lib-name.jar
 *         // # end of update entry
 */
public class ScanForUpdate implements PreferencesInterface {
	
	private static final Logger logger = Logger.getLogger(ScanForUpdate.class);
	
	/**
	 * 
	 */
	private static final int TIME_REMINDER_DAYS = 7;
	
	private static final String[] arrDialogOptions = {
			"Download now",
			"Remind me in " + TIME_REMINDER_DAYS + " days"
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
	private static final String LIBSTRING = "lib";
	
	private static String URL_UPDATE_BASESTRING = "https://immersive-analytics.infotech.monash.edu/vanted-new2.5/release/updates/";
	private static String URL_UPDATE_FILESTRING = URL_UPDATE_BASESTRING + "vanted-update";
	
	private static final String DESTPATHUPDATEDIR = ReleaseInfo.getAppFolderWithFinalSep() + "update/";
	private static final String DESTUPDATEFILE = DESTPATHUPDATEDIR + "do-vanted-update";
	private static final String VANTEDUPDATEOKFILE = DESTPATHUPDATEDIR + "vanted-update-ok";
	
	public static void scanAfterStartup() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				logger.debug("waiting for update scan until app has started");
				while (ErrorMsg.getAppLoadingStatus() != ApplicationStatus.ADDONS_LOADED) {
					try {
						Thread.sleep(1000);//sleep 20 sec
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				try {
					doScan();
				} catch (IOException e) {
					if (Logger.getRootLogger().getLevel() == Level.DEBUG)
						e.printStackTrace();
					System.out.println("cannot scan for updates: " + e.getMessage());
				}
			}
		}).start();
	}
	
	public static void doScan() throws IOException {
		logger.debug("doing update scan at: " + URL_UPDATE_FILESTRING);
		cleanPreviousUpdate();
		
		Date currentDate = new Date();
		
		String version = null;
		boolean prepareUpdate = false;
		
		String strCoreProgramRelativePath = null;
		List<String> listStrCoreLibsRelativePaths = new ArrayList<String>();
		
		InputStream inputstreamURL = null;
		
		URL updateURL = new URL(URL_UPDATE_FILESTRING);
		
		try {
			inputstreamURL = updateURL.openStream();
		} catch (FileNotFoundException e1) {
			System.out.println("no updates available");
			return;
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstreamURL));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.equals("//"))
				break;
			if (line.startsWith("#"))
				continue;
			if (line.toLowerCase().startsWith(VERSIONSTRING)) {
				version = line.substring(VERSIONSTRING.length() + 1).trim();
				prepareUpdate = updateIsNewer(version);
			}
			if (line.toLowerCase().startsWith(CORESTRING)) {
				strCoreProgramRelativePath = line.substring(CORESTRING.length() + 1).trim();
			}
			if (line.toLowerCase().startsWith(LIBSTRING)) {
				listStrCoreLibsRelativePaths.add(line.substring(LIBSTRING.length() + 1).trim());
			}
			
		}
		
		inputstreamURL.close();
		
		if (Logger.getRootLogger().getLevel() == Level.DEBUG) {
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
				if (Logger.getRootLogger().getLevel() == Level.DEBUG)
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
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
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
		File updateDir = new File(DESTPATHUPDATEDIR);
		if (!updateDir.exists())
			updateDir.mkdirs();
		
		// create a copy file for the bootstrap program
		// that will put the files to the right place
		// .. the next thing is for the bootstrap
		
		File updateFile = new File(DESTUPDATEFILE);
		BufferedWriter writer = new BufferedWriter(new FileWriter(updateFile));
		
		writer.write(VERSIONSTRING + ":" + version);
		writer.newLine();
		if (strCoreProgramRelativePath != null) {
			FileHelper.downloadFile(new URL(URL_UPDATE_BASESTRING + strCoreProgramRelativePath), DESTPATHUPDATEDIR, extractFileName(strCoreProgramRelativePath));
			writer.write(CORESTRING + ":" + strCoreProgramRelativePath);
			writer.newLine();
		}
		
		for (String libPath : listStrCoreLibsRelativePaths) {
			
			FileHelper.downloadFile(new URL(URL_UPDATE_BASESTRING + libPath), DESTPATHUPDATEDIR, extractFileName(libPath));
			writer.write(LIBSTRING + ":" + libPath);
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
	 * And by deleting the update folder (including this file) we also tell the bootstrap
	 * that we successfully loaded
	 */
	private static void cleanPreviousUpdate() {
		File checkfile = new File(VANTEDUPDATEOKFILE);
		if (checkfile.exists())
			FileHelper.deleteDirRecursively(new File(DESTPATHUPDATEDIR));
	}
	
	private static boolean updateIsNewer(String remoteVersion) {
		
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
				return true;
				
			}
			
		}
		
		return false;
	}
	
	@Override
	public List<Parameter> getDefaultParameters() {
		List<Parameter> params = new ArrayList<Parameter>();
		params.add(new StringParameter(URL_UPDATE_BASESTRING, "Update URL", "Location of the URL to look for updates"));
		return params;
	}
	
	@Override
	public void updatePreferences(Preferences preferences) {
		URL_UPDATE_BASESTRING = preferences.get("Update URL", URL_UPDATE_BASESTRING);
		
		URL_UPDATE_FILESTRING = URL_UPDATE_BASESTRING + "vanted-update";
	}
	
	@Override
	public String getPreferencesAlternativeName() {
		return "Update";
	}
	
}
