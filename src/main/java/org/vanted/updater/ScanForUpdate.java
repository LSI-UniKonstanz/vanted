/**
 * 
 */
package org.vanted.updater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.graffiti.util.Pair;

import de.ipk_gatersleben.ag_nw.graffiti.FileHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author matthiak
 *         Update file has very simple format:
 *         For the message string.. it only has to be surrounded by '{{' and '}}'
 *         and can go over many lines
 *         first line: version:<version> //x.x.x
 *         <+|-><type>:<filename>
 *         ..
 *         message:{{<MESSAGESTRING>
 *         <MESSAGESTRING>
 *         <MESSAGESTRING>
 *         }}
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
 *         message:{{
 *         some text message
 *         }}
 *         // # end of update entry
 */
public class ScanForUpdate implements PreferencesInterface//, Runnable 
{
	
	private static final Logger logger = Logger.getLogger(ScanForUpdate.class);
	
	/**
	 * 
	 */
	protected static final int TIME_REMINDER_DAYS = 7;
	
	private static final String[] arrDialogOptions = {
			"Download now",
			"Remind me in " + TIME_REMINDER_DAYS + " days"
	};
	
	/**
	 * 
	 */
	protected static final String REMINDER_DATE = "reminder-date";
	/**
	 * 
	 */
	private static final String VERSIONSTRING = "version";
	private static final String CORESTRING = "core";
	private static final String LIBSTRING = "lib";
	private static final String MESSAGE = "message";
	private static final String MESSAGE_START = "{{";
	private static final String MESSAGE_END = "}}";
	
	private static String URL_UPDATE_BASESTRING = "http://kim25.wwwdns.kim.uni-konstanz.de/vanted/release/updates/";
	private static String URL_UPDATE_FILESTRING = URL_UPDATE_BASESTRING + "/" + "vanted-update";
	private static String URL_UPDATEMD5_FILESTRING = URL_UPDATE_BASESTRING + "/" + "vanted-files-md5";
	private static String URL_CHANGELOG_FILESTRING = URL_UPDATE_BASESTRING + "/" + "CHANGELOG";
	
	private static final String DESTPATHUPDATEDIR = ReleaseInfo.getAppFolderWithFinalSep() + "update/";
	private static final String DESTUPDATEFILE = DESTPATHUPDATEDIR + "do-vanted-update";
	private static final String VANTEDUPDATEOKFILE = DESTPATHUPDATEDIR + "vanted-update-ok";
	private static final String CHANGELOGFILE = DESTPATHUPDATEDIR + "CHANGELOG";
	
	BackgroundTaskStatusProviderSupportingExternalCall backgroundTaskStatusProvider;
	
	/**
	 * 
	 */
	public ScanForUpdate() {
		backgroundTaskStatusProvider =
				new BackgroundTaskStatusProviderSupportingExternalCallImpl("", "");
	}
	
//	public void run() {
//		
//		try {
//			doScan(false); //scan and don't ignore scan-date
//		} catch (IOException e) {
//			if (Logger.getRootLogger().getLevel() == Level.DEBUG)
//				e.printStackTrace();
//			System.out.println("cannot scan for updates: " + e.getMessage());
//		}
//		
//		backgroundTaskStatusProvider.setCurrentStatusText1("Download finished");
//	}
	/*
	 * Starts scan for updates.
	 * Scan will only start after application and addons have been loaded
	 * This method runs asynchronous to not block the start up process
	 */
	public static void issueScanAfterStartup() {
		
	}
	
	public static void issueScan(final boolean ignoreDate) {
		
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
				// always try to finish an update
				try {
					finishPreviousUpdate();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				// only scan for new updates, if the date is right
				if (!ignoreDate) {
					Date currentDate = new Date();
					Preferences preferenceForClass = PreferenceManager.getPreferenceForClass(ScanForUpdate.class);
					// if there is preference entry for reminder time..  check
					if (!CheckUpdateDate.isDateAfterUpdateDate(currentDate, preferenceForClass))
						return;
				}
				
				final ScanForAddonUpdates scanForAddonUpdates = new ScanForAddonUpdates();
				BackgroundTaskHelper.issueSimpleTask(
						"Downloading Updates",
						"...",
						new Runnable() {
							
							@Override
							public void run() {
								try {
									scanForAddonUpdates.doScan(ignoreDate);
									scanForUpdate.doScan(ignoreDate);
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
	
	protected void doScan(boolean ignoreDate) throws IOException {
		
		if (ReleaseInfo.isRunningAsWebstart()) {
			logger.debug("Running as applet..no update check");
			return;
		}
		
		logger.debug("doing update scan at: " + URL_UPDATE_FILESTRING);
		
		Date currentDate = new Date();
		Preferences preferenceForClass = PreferenceManager.getPreferenceForClass(ScanForUpdate.class);
		
		if (!ignoreDate) {
			// if there is preference entry for reminder time..  check
			if (!CheckUpdateDate.isDateAfterUpdateDate(currentDate, preferenceForClass))
				return;
		}
		String version = null;
		boolean prepareUpdate = false;
		
		Set<String> listAddCoreJarRelativePath = new HashSet<String>();
		Set<String> listRemoveCoreJarRelativePath = new HashSet<String>();
		Set<String> listAddLibsJarRelativePaths = new HashSet<String>();
		Set<String> listRemoveLibsJarRelativePaths = new HashSet<String>();
		
		// get a list of all paths to the jars in the classpath and their md5
		List<Pair<String, String>> jarMd5Pairs = CalcClassPathJarsMd5.getJarMd5Pairs();
		
		Map<String, String> mapMd5FromUpdateLocation = getMd5FromUpdateLocation(new URL(URL_UPDATEMD5_FILESTRING));
		//now sort them into core and lib jars
		Map<String, String> mapJarMd5PairsInstalledCore = new HashMap<String, String>();
		Map<String, String> mapJarMd5PairsInstalledLibs = new HashMap<String, String>();
		for (Pair<String, String> curPair : jarMd5Pairs) {
			//create new pair that contains only the jar filename and the md5
			if (curPair.getFst().contains("vanted-core")) {
				mapJarMd5PairsInstalledCore.put(curPair.getFst().substring(curPair.getFst().lastIndexOf("/") + 1), curPair.getSnd());
			}
			else {
				mapJarMd5PairsInstalledLibs.put(curPair.getFst().substring(curPair.getFst().lastIndexOf("/") + 1), curPair.getSnd());
			}
			
		}
		
		StringBuffer msgbuffer = new StringBuffer();
		
		InputStream inputstreamURL = null;
		URL updateURL = new URL(URL_UPDATE_FILESTRING);
		
		try {
			inputstreamURL = updateURL.openStream();
		} catch (FileNotFoundException e1) {
			if (Logger.getRootLogger().getLevel() == Level.DEBUG)
				e1.printStackTrace();
			System.err.println("update file not found at location: " + e1.getMessage());
			return;
		}
		
		StringBuffer updFileBuffer = new StringBuffer();
		
		boolean readingMessageAttribute = false;
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstreamURL));
		String line;
		
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.equals("//")) {
				break;
			}
			if (line.startsWith("#")) {
				continue;
			}
			/*
			 * reading message part.
			 * must be done first to make sure, we don't accidently
			 * read another attribute which might be part of the message
			 */
			if (line.startsWith(MESSAGE) && !readingMessageAttribute) {
				readingMessageAttribute = true;
				// catch newline after message-start
				if (!line.endsWith(MESSAGE_START)) {
					int idx = line.indexOf(MESSAGE_START) + MESSAGE_START.length();
					msgbuffer.append(line.substring(idx));
				}
				updFileBuffer.append(line);
				updFileBuffer.append("\n");
				continue;
			} else
			/*
			 * we already started reading a message and we will read lines
			 * as long as we don't find the message-end tag
			 */
			if (readingMessageAttribute) {
				msgbuffer.append("\n");
				int idx = line.indexOf(MESSAGE_END);
				if (idx >= 0) {
					msgbuffer.append(line.substring(0, line.indexOf(MESSAGE_END)));
					readingMessageAttribute = false;
				} else {
					msgbuffer.append(line);
				}
				updFileBuffer.append(line);
				updFileBuffer.append("\n");
				continue;
			}
			
			if (line.toLowerCase().startsWith(VERSIONSTRING)) {
				version = line.substring(VERSIONSTRING.length() + 1).trim();
				updFileBuffer.append(line);
				updFileBuffer.append("\n");
				continue;
			}
			// look for jars to add
			if (line.toLowerCase().startsWith("+")) {
				String parseline = line.substring(1);
				if (parseline.toLowerCase().startsWith(CORESTRING)) {
					
					parseline = parseline.substring(CORESTRING.length() + 1).trim();
					// compare both md5 sums (remote and local
					if (mapJarMd5PairsInstalledCore.get(parseline) == null // new remote jar 
							|| !mapJarMd5PairsInstalledCore.get(parseline).equals(mapMd5FromUpdateLocation.get(parseline))) {
						listAddCoreJarRelativePath.add(parseline);
						continue;
					}
				}
				if (parseline.toLowerCase().startsWith(LIBSTRING)) {
					parseline = parseline.substring(LIBSTRING.length() + 1).trim();
					
					String md5local = mapJarMd5PairsInstalledLibs.get(parseline);
					String md5remote = mapMd5FromUpdateLocation.get(parseline);
					if (mapJarMd5PairsInstalledLibs.get(parseline) == null // new remote jar 
							|| !md5local.equals(md5remote)) {
						listAddLibsJarRelativePaths.add(parseline);
						continue;
					}
				}
			} else if (line.toLowerCase().startsWith("-")) {
				String parseline = line.substring(1);
				if (parseline.toLowerCase().startsWith(CORESTRING)) {
					listRemoveCoreJarRelativePath.add(parseline.substring(CORESTRING.length() + 1).trim());
				}
				if (parseline.toLowerCase().startsWith(LIBSTRING)) {
					listRemoveLibsJarRelativePaths.add(parseline.substring(LIBSTRING.length() + 1).trim());
				}
				updFileBuffer.append(line);
				updFileBuffer.append("\n");
			}
			
		}
		
		inputstreamURL.close();
		
		/*
		 * add changed jars as well, that do not appear in the update file
		 */
		for (String curJar : mapJarMd5PairsInstalledCore.keySet()) {
			String md5remote = mapMd5FromUpdateLocation.get(curJar);
			String md5local = mapJarMd5PairsInstalledCore.get(curJar);
			if (mapMd5FromUpdateLocation.get(curJar) != null
					&& !md5remote.equals(md5local)) {
				listAddCoreJarRelativePath.add(curJar);
				
			}
		}
		for (String curJar : mapJarMd5PairsInstalledLibs.keySet()) {
			String md5remote = mapMd5FromUpdateLocation.get(curJar);
			String md5local = mapJarMd5PairsInstalledLibs.get(curJar);
			if (mapMd5FromUpdateLocation.get(curJar) != null
					&& !md5remote.equals(md5local)) {
				listAddLibsJarRelativePaths.add(curJar);
			}
		}
		
		/*
		 * write the set of jars to add to the update file
		 */
		for (String curCorJar : listAddCoreJarRelativePath) {
			updFileBuffer.append("+core:" + curCorJar);
			updFileBuffer.append("\n");
		}
		
		for (String curLibJar : listAddLibsJarRelativePaths) {
			updFileBuffer.append("+lib:" + curLibJar);
			updFileBuffer.append("\n");
		}
		
		if (updateIsNewer(version) || !listAddCoreJarRelativePath.isEmpty() || !listAddLibsJarRelativePaths.isEmpty())
			prepareUpdate = true;
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
				System.out.println("Update-message: " + msgbuffer.toString());
			} else
				logger.debug("We found update file, but version is not newer");
		}
		
		// the update we found is not newer
		if (!prepareUpdate) {
			logger.debug("no update found");
			backgroundTaskStatusProvider.setCurrentStatusText1("No updates found");
			//we have written the file but it wasn't necessary.. delete it
			return;
		}
		
		String updateMessage = msgbuffer.toString();
		if (updateMessage.toLowerCase().contains("<html>"))
			updateMessage = updateMessage.substring(updateMessage.indexOf("<html>") + 6);
		updateMessage = updateMessage.replace("\n", "");
		/* 
		 * popup dialog telling user, there is a new version
		 * download now or later
		 * or go to website (short version)
		 */ 
		String msgUpdateType;
		if(updateIsNewer(version))
			msgUpdateType = "New VANTED version " + version;
		else
			msgUpdateType = "VANTED library updates";
		
		String msg = "<html>A new update for VANTED is available<br/><br/>"
				+ "<strong>&rarr;</strong>&nbsp;&nbsp;" + msgUpdateType
				+ "<br/><br/>You can download it now or be reminded later.<br/><br/>"
				+ "<strong>The update will be installed during the next startup</strong>.";
		int dialogAskUpdate = JOptionPane.showOptionDialog(MainFrame.getInstance(),
				msg,
				"Update available",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				arrDialogOptions,
				arrDialogOptions[0]);
		
		if (dialogAskUpdate == JOptionPane.CANCEL_OPTION || dialogAskUpdate == JOptionPane.DEFAULT_OPTION) {
			return;
		}
		
		//later - create entry in preferences for reminder date
		if (dialogAskUpdate == 1) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			Date addDays = DateUtils.addDays(currentDate, TIME_REMINDER_DAYS);
			preferenceForClass.put(REMINDER_DATE, dateFormat.format(addDays));
			PreferenceManager.storePreferences();
			return;
		}
		
		// download the changelog file from the server
		// this will be presented in an update dialog after the startup next time
		FileHelper.downloadFile(new URL(URL_CHANGELOG_FILESTRING), DESTPATHUPDATEDIR, "CHANGELOG");
		
		for (String corePath : listAddCoreJarRelativePath) {
			backgroundTaskStatusProvider.setCurrentStatusText1("downloading: " + extractFileName(corePath));
			FileHelper.downloadFile(new URL(URL_UPDATE_BASESTRING + "/" + corePath), DESTPATHUPDATEDIR, extractFileName(corePath));
		}
		for (String libPath : listAddLibsJarRelativePaths) {
			backgroundTaskStatusProvider.setCurrentStatusText1("downloading: " + extractFileName(libPath));
			FileHelper.downloadFile(new URL(URL_UPDATE_BASESTRING + "/libs/" + libPath), DESTPATHUPDATEDIR, extractFileName(libPath));
		}
		
		/* 
		 * create a copy file for the bootstrap program
		 *  that will put the files to the right place
		 *   .. the next thing is for the bootstrap
		 */
		File updateFile = new File(DESTUPDATEFILE);
		BufferedWriter writer = new BufferedWriter(new FileWriter(updateFile));
		writer.write(updFileBuffer.toString());
		writer.close();
		
		// bootstrap will look for that file and does his work 
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
	private static void finishPreviousUpdate() throws IOException {
		File checkfile = new File(VANTEDUPDATEOKFILE);
		if (checkfile.exists()) {
			
			String header = "<html>" + "<h3>Application has been updated to " + DBEgravistoHelper.DBE_GRAVISTO_VERSION + "!</h3>";
			String details = new String();
			File updateFile = new File(DESTUPDATEFILE);
			if (updateFile.exists()) {
				
				boolean readingMessageAttribute = false;
				StringBuffer msgbuffer = new StringBuffer();
				
				BufferedReader breader = new BufferedReader(new FileReader(updateFile));
				String line;
				while ((line = breader.readLine()) != null) {
					/*
					 * reading message part.
					 * must be done first to make sure, we don't accidently
					 * read another attribute which might be part of the message
					 */
					if (line.startsWith(MESSAGE) && !readingMessageAttribute) {
						readingMessageAttribute = true;
						// catch newline after message-start
						if (!line.endsWith(MESSAGE_START)) {
							int idx = line.indexOf(MESSAGE_START) + MESSAGE_START.length();
							msgbuffer.append(line.substring(idx));
						}
						continue;
					}
					/*
					 * we already started reading a message and we will read lines
					 * as long as we don't find the message-end tag
					 */
					if (readingMessageAttribute) {
						int idx = line.indexOf(MESSAGE_END);
						if (idx >= 0) {
							msgbuffer.append(line.substring(0, line.indexOf(MESSAGE_END)));
							readingMessageAttribute = false;
						} else {
							msgbuffer.append(line);
						}
						continue;
					}
				}
				breader.close();
				
				if (msgbuffer.length() > 0) {
					details += "Details:<br>";
					details += msgbuffer.toString();
				}
				
			}
			
			File changelogfile = new File(CHANGELOGFILE);
			StringBuffer buf = new StringBuffer();
			if (changelogfile.exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(changelogfile));
				String line;
				while ((line = reader.readLine()) != null) {
					buf.append(line);
					buf.append("\n");
				}
				reader.close();
			}
			String changelog = null;
			if (buf.length() > 0) {
				changelog = buf.toString();
				if (!changelog.startsWith("<html>")) {
					changelog = "<html>" + changelog;
				}
				
			}
			String footer = "";
			
			UpdateMessageDialog.showUpdateMessageDialog(header, details, changelog, footer);
			
			FileHelper.deleteDirRecursively(new File(DESTPATHUPDATEDIR));
			
		}
	}
	
	private static boolean updateIsNewer(String remoteVersion) {
		
		String[] strSplitMainversion = DBEgravistoHelper.DBE_GRAVISTO_VERSION_CODE.split("\\.");
		
		long[] longArrMainVersion = new long[strSplitMainversion.length];
		
		for (int i = 0; i < strSplitMainversion.length; i++) {
			longArrMainVersion[i] = Long.parseLong(strSplitMainversion[i]);
		}
		String concatVersionWithPadding = String.format("%d%03d%03d", longArrMainVersion[0], longArrMainVersion[1], longArrMainVersion[2]);
		long longVersionWithPadding = Long.parseLong(concatVersionWithPadding);
		
		String[] strSplitRemoteVersion = remoteVersion.split("\\.");
		long[] longRemoteVersion = new long[strSplitRemoteVersion.length];
		
		for (int i = 0; i < strSplitRemoteVersion.length; i++) {
			longRemoteVersion[i] = Long.parseLong(strSplitRemoteVersion[i]);
		}
		String concatRemoteVersionWithPadding = String.format("%d%03d%03d", longRemoteVersion[0], longRemoteVersion[1], longRemoteVersion[2]);
		long longRemoteVersionWithPadding = Long.parseLong(concatRemoteVersionWithPadding);
		
		if (longVersionWithPadding < longRemoteVersionWithPadding)
			return true;
		else
			return false;
		
	}
	
	/**
	 * reads a file located at the given URL.
	 * This file contains lines with filename-md5 pairs.
	 * Each line is the output of 'md5sum' tool:
	 * e.g.
	 * a384114157486d24ed3a83798d21e60a *./vanted-core.jar
	 * 
	 * @param md5fileurl
	 * @return Map with key:jar file name -> value: md5sum
	 * @throws IOException
	 */
	private static Map<String, String> getMd5FromUpdateLocation(URL md5fileurl) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		
		InputStream openStream = md5fileurl.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(openStream));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.indexOf("*") > 0) {
				String[] split = line.trim().split("\\*");
				
				map.put(split[1].substring(split[1].lastIndexOf("/") + 1), split[0].trim());
			}
		}
		return map;
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
		
		URL_UPDATE_FILESTRING = URL_UPDATE_BASESTRING + "/" + "vanted-update";
		URL_UPDATEMD5_FILESTRING = URL_UPDATE_BASESTRING + "/" + "vanted-files-md5";
		URL_CHANGELOG_FILESTRING = URL_UPDATE_BASESTRING + "/" + "CHANGELOG";
	}
	
	@Override
	public String getPreferencesAlternativeName() {
		return "Update";
	}
	
}
