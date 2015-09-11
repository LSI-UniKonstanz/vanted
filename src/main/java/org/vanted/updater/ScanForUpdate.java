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
import org.BackgroundTaskStatusProviderSupportingExternalCall;
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
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author matthiak
 *         Update file has very simple format:
 *         first line: version:<version> //x.x.x
 *         <+|-><type>:<filename>
 *         ..
 *         //
 *         + = add the file
 *         - = remove the file
 *         type = type of file (core, lib)
 *         filename = url to file
 *         # comment line
 *         e.g.:
 *         # some comment
 *         version: 2.5.0
 *         +core: vanted-core.jar
 *         +lib: lib-name.jar
 *         -lib: lib-name2.jar
 *         // # end of update entry
 */
public class ScanForUpdate implements PreferencesInterface, Runnable {
	
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
	
	private static String URL_UPDATE_BASESTRING = "https://immersive-analytics.infotech.monash.edu/vanted/release/updates/";
	private static String URL_UPDATE_FILESTRING = URL_UPDATE_BASESTRING + "vanted-update";
	
	private static final String DESTPATHUPDATEDIR = ReleaseInfo.getAppFolderWithFinalSep() + "update/";
	private static final String DESTUPDATEFILE = DESTPATHUPDATEDIR + "do-vanted-update";
	private static final String VANTEDUPDATEOKFILE = DESTPATHUPDATEDIR + "vanted-update-ok";
	
	BackgroundTaskStatusProviderSupportingExternalCall backgroundTaskStatusProvider;
	
	/**
	 * 
	 */
	public ScanForUpdate() {
		backgroundTaskStatusProvider =
				new BackgroundTaskStatusProviderSupportingExternalCallImpl("", "");
	}
	
	public void run() {
		
		try {
			doScan(false); //scan and don't ignore scan-date
		} catch (IOException e) {
			if (Logger.getRootLogger().getLevel() == Level.DEBUG)
				e.printStackTrace();
			System.out.println("cannot scan for updates: " + e.getMessage());
		}
		
		backgroundTaskStatusProvider.setCurrentStatusText1("Download finished");
	}
	
	public static void issueScanAfterStartup() {
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
				logger.debug("starting update scan task");
				final ScanForUpdate scanForUpdate = new ScanForUpdate();
				BackgroundTaskHelper.issueSimpleTask(
						"Downloading Updates",
						"...",
						new Runnable() {
							
							@Override
							public void run() {
								try {
									scanForUpdate.doScan(false);
								} catch (IOException e) {
									if (Logger.getRootLogger().getLevel() == Level.DEBUG)
										e.printStackTrace();
									System.out.println("cannot scan for updates: " + e.getMessage());
								}
								scanForUpdate.backgroundTaskStatusProvider.setCurrentStatusText1("Download finished");
							}
						},
						null,
						scanForUpdate.backgroundTaskStatusProvider, 0);
			}
		}).start();
	}
	
	public static void issueScan() {
		logger.debug("starting update scan task");
		final ScanForUpdate scanForUpdate = new ScanForUpdate();
		
		BackgroundTaskHelper.issueSimpleTask(
				"Downloading Updates",
				"...",
				new Runnable() {
					
					@Override
					public void run() {
						try {
							scanForUpdate.doScan(true);
						} catch (IOException e) {
							if (Logger.getRootLogger().getLevel() == Level.DEBUG)
								e.printStackTrace();
							System.out.println("cannot scan for updates: " + e.getMessage());
						}
						scanForUpdate.backgroundTaskStatusProvider.setCurrentStatusText1("Download finished");
					}
				},
				null,
				scanForUpdate.backgroundTaskStatusProvider, 0);
	}
	
	protected void doScan(boolean ignoreDate) throws IOException {
		
		if (ReleaseInfo.isRunningAsWebstart()) {
			logger.debug("Running as applet..no update check");
			return;
		}
		
		logger.debug("doing update scan at: " + URL_UPDATE_FILESTRING);
		cleanPreviousUpdate();
		
		Date currentDate = new Date();
		Preferences preferenceForClass = PreferenceManager.getPreferenceForClass(ScanForUpdate.class);
		
		if (!ignoreDate) {
			// if there is preference entry for reminder time..  check
			boolean timeout = false;
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
		}
		String version = null;
		boolean prepareUpdate = false;
		
		List<String> listAddCoreJarRelativePath = new ArrayList<String>();
		List<String> listRemoveCoreJarRelativePath = new ArrayList<String>();
		List<String> listAddLibsJarRelativePaths = new ArrayList<String>();
		List<String> listRemoveLibsJarRelativePaths = new ArrayList<String>();
		
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
			// look for jars to add
			if (line.toLowerCase().startsWith("+")) {
				line = line.substring(1);
				if (line.toLowerCase().startsWith(CORESTRING)) {
					listAddCoreJarRelativePath.add(line.substring(CORESTRING.length() + 1).trim());
				}
				if (line.toLowerCase().startsWith(LIBSTRING)) {
					listAddLibsJarRelativePaths.add(line.substring(LIBSTRING.length() + 1).trim());
				}
			} else
				if (line.toLowerCase().startsWith("-")) {
					line = line.substring(1);
					if (line.toLowerCase().startsWith(CORESTRING)) {
						listRemoveCoreJarRelativePath.add(line.substring(CORESTRING.length() + 1).trim());
					}
					if (line.toLowerCase().startsWith(LIBSTRING)) {
						listRemoveLibsJarRelativePaths.add(line.substring(LIBSTRING.length() + 1).trim());
					}
				}
		}
		
		inputstreamURL.close();
		
		if (Logger.getRootLogger().getLevel() == Level.DEBUG) {
			if (prepareUpdate) {
				logger.debug("We found an update.. printing locations:");
				for (String libPath : listAddCoreJarRelativePath) {
					System.out.println(" adding core-jar: " + libPath);
				}
				for (String libPath : listAddLibsJarRelativePaths) {
					System.out.println("  adding lib-jar: " + libPath);
				}
				System.out.println("----------");
				for (String libPath : listRemoveCoreJarRelativePath) {
					System.out.println(" removing core-jar: " + libPath);
				}
				for (String libPath : listRemoveLibsJarRelativePaths) {
					System.out.println("  removing lib-jar: " + libPath);
				}
			} else
				logger.debug("We found update file, but version is not newer");
		}
		
		// the update we found is not newer
		if (!prepareUpdate) {
			backgroundTaskStatusProvider.setCurrentStatusText1("No updates found");
			return;
		}
		
		// popup dialog telling user, there is a new version
		// download now or later
		// or go to website (short version)
		int dialogAskUpdate = JOptionPane.showOptionDialog(MainFrame.getInstance(),
				"<html>A new update to VANTED " + version + " is available<br/><br/>"
						+ "You can download it now or be reminded later.<br/><br/>"
						+ "The update will be installed during the next startup.",
				"Update available",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				arrDialogOptions,
				arrDialogOptions[0]);
		
		if (dialogAskUpdate == JOptionPane.CANCEL_OPTION)
			return;
		
		//later - create entry in preferences for reminder date
		if (dialogAskUpdate == 1) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			Date addDays = DateUtils.addDays(currentDate, TIME_REMINDER_DAYS);
			preferenceForClass.put(REMINDER_DATE, dateFormat.format(addDays));
			PreferenceManager.storePreferences();
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
		// download new jars or jars to replace
		for (String corePath : listAddCoreJarRelativePath) {
			backgroundTaskStatusProvider.setCurrentStatusText1("downloading: " + extractFileName(corePath));
			FileHelper.downloadFile(new URL(URL_UPDATE_BASESTRING + corePath), DESTPATHUPDATEDIR, extractFileName(corePath));
			writer.write("+" + CORESTRING + ":" + corePath);
			writer.newLine();
		}
		for (String libPath : listAddLibsJarRelativePaths) {
			backgroundTaskStatusProvider.setCurrentStatusText1("downloading: " + extractFileName(libPath));
			
			FileHelper.downloadFile(new URL(URL_UPDATE_BASESTRING + libPath), DESTPATHUPDATEDIR, extractFileName(libPath));
			writer.write("+" + LIBSTRING + ":" + libPath);
			writer.newLine();
		}
		
		// add list of entries for jars to be removed
		for (String corePath : listRemoveCoreJarRelativePath) {
			writer.write("-" + CORESTRING + ":" + corePath);
			writer.newLine();
		}
		
		for (String libPath : listRemoveLibsJarRelativePaths) {
			writer.write("-" + LIBSTRING + ":" + libPath);
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
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
		params.add(new StringParameter(simpleDateFormat.format(new Date()), REMINDER_DATE, "Date for the next reminder (DD-MM-YYYY)"));
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