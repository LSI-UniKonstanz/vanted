package org.vanted.updater;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.ApplicationStatus;
import org.AttributeHelper;
import org.ErrorMsg;
import org.ReleaseInfo;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.graffiti.editor.MainFrame;
import org.graffiti.managers.PreferenceManager;
import org.graffiti.options.PreferencesInterface;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.FileHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;

/**
 * Tells the user when GitHub has a newer VANTED release and opens the download
 * page on request. Nothing is downloaded or replaced.
 * <p>
 * Replaces the in-place updater ({@link ScanForUpdate}), which compared jar MD5
 * sums with the former update server: that server is no longer maintained, so
 * it would have replaced current jars with old ones, and changing files inside
 * the signed macOS app breaks its signature.
 * <p>
 * The latest release is read from the redirect of
 * {@code github.com/<repo>/releases/latest} to {@code .../releases/tag/v<version>},
 * which needs no API token and is not rate limited like the REST API.
 */
public class UpdateCheck implements PreferencesInterface {
	
	public static final String RELEASES_URL = "https://github.com/LSI-UniKonstanz/vanted/releases";
	private static final String LATEST_URL = RELEASES_URL + "/latest";
	
	private static final String CHECK_ON_STARTUP = "Check for updates on startup";
	private static final String NEXT_CHECK = "next-check";
	private static final int DAYS_BETWEEN_CHECKS = 7;
	private static final int TIMEOUT_MS = 10000;
	
	/** Folder of the old updater; see {@link #removeOldUpdaterFolder()}. */
	private static final String OLD_UPDATE_DIR = ReleaseInfo.getAppFolderWithFinalSep() + "update";
	
	private static final Logger logger = Logger.getLogger(UpdateCheck.class);
	
	private static volatile boolean checkOnStartup = true;
	
	/**
	 * @param manual
	 *           true: check now and always report the result (menu item); false:
	 *           check at most once a week after start-up and only report a newer
	 *           version
	 */
	public static void issueCheck(final boolean manual) {
		Thread thread = new Thread(() -> {
			if (!manual) {
				removeOldUpdaterFolder();
				waitForStartup();
				if (!checkOnStartup || !isDue())
					return;
				scheduleNextCheck();
			}
			String latest = null;
			try {
				latest = fetchLatestVersion();
			} catch (IOException | RuntimeException e) {
				logger.debug("update check failed: " + e.getMessage());
			}
			final String current = DBEgravistoHelper.DBE_GRAVISTO_VERSION_CODE;
			final String latestVersion = latest;
			boolean newer = latest != null && isNewer(latest, current);
			if (!manual && !newer)
				return;
			SwingUtilities.invokeLater(() -> showResult(manual, current, latestVersion, newer));
		}, "VANTED update check");
		thread.setDaemon(true);
		thread.start();
	}
	
	/**
	 * @return version of the latest GitHub release without the leading "v", or
	 *         null if there is none
	 */
	static String fetchLatestVersion() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(LATEST_URL).openConnection();
		try {
			connection.setInstanceFollowRedirects(false);
			connection.setRequestMethod("HEAD");
			connection.setConnectTimeout(TIMEOUT_MS);
			connection.setReadTimeout(TIMEOUT_MS);
			connection.setRequestProperty("User-Agent", "VANTED/" + DBEgravistoHelper.DBE_GRAVISTO_VERSION_CODE);
			int code = connection.getResponseCode();
			String location = connection.getHeaderField("Location");
			if (code / 100 != 3 || location == null)
				return null;
			int idx = location.lastIndexOf("/tag/");
			if (idx < 0)
				return null; // no release yet: redirects to the release list
			String tag = URLDecoder.decode(location.substring(idx + "/tag/".length()), "UTF-8").trim();
			return tag.startsWith("v") ? tag.substring(1) : tag;
		} finally {
			connection.disconnect();
		}
	}
	
	/** Compares MAJOR.MINOR.PATCH numerically; a suffix such as "-rc1" is ignored. */
	static boolean isNewer(String candidate, String current) {
		if (candidate == null || current == null)
			return false;
		int[] a = parse(candidate);
		int[] b = parse(current);
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i])
				return a[i] > b[i];
		}
		return false;
	}
	
	private static int[] parse(String version) {
		int[] parts = new int[3];
		String[] split = version.trim().split("\\.");
		for (int i = 0; i < parts.length && i < split.length; i++) {
			String digits = split[i].replaceFirst("^(\\d*).*$", "$1");
			parts[i] = digits.isEmpty() ? 0 : Integer.parseInt(digits);
		}
		return parts;
	}
	
	private static void showResult(boolean manual, String current, String latest, boolean newer) {
		String title = "VANTED update";
		if (newer) {
			String msg = "<html>VANTED " + latest + " is available (installed: " + current + ").<br/><br/>"
					+ "Download and install it from the release page.";
			Object[] options = { "Open download page", "Later" };
			int choice = JOptionPane.showOptionDialog(MainFrame.getInstance(), msg, title,
					JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
			if (choice == 0)
				AttributeHelper.showInBrowser(LATEST_URL);
		} else if (latest == null) {
			String msg = "<html>Could not check for updates.<br/><br/>Releases: " + RELEASES_URL;
			Object[] options = { "Open release page", "Close" };
			int choice = JOptionPane.showOptionDialog(MainFrame.getInstance(), msg, title,
					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			if (choice == 0)
				AttributeHelper.showInBrowser(RELEASES_URL);
		} else if (manual) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "VANTED " + current + " is up to date.", title,
					JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	private static void waitForStartup() {
		while (ErrorMsg.getAppLoadingStatus() != ApplicationStatus.ADDONS_LOADED) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return;
			}
		}
	}
	
	private static boolean isDue() {
		String next = prefs().get(NEXT_CHECK, null);
		if (next == null)
			return true;
		try {
			return !new Date().before(dateFormat().parse(next));
		} catch (ParseException e) {
			return true;
		}
	}
	
	private static void scheduleNextCheck() {
		prefs().put(NEXT_CHECK, dateFormat().format(DateUtils.addDays(new Date(), DAYS_BETWEEN_CHECKS)));
		PreferenceManager.storePreferences();
	}
	
	/**
	 * The old updater downloaded jars into this folder for the bootstrap to copy
	 * into the installation. Leftovers would keep old jars coming back.
	 */
	private static void removeOldUpdaterFolder() {
		File dir = new File(OLD_UPDATE_DIR);
		if (dir.isDirectory()) {
			logger.info("removing folder of the old updater: " + dir);
			FileHelper.deleteDirRecursively(dir);
		}
	}
	
	private static Preferences prefs() {
		return PreferenceManager.getPreferenceForClass(UpdateCheck.class);
	}
	
	private static SimpleDateFormat dateFormat() {
		return new SimpleDateFormat("dd-MM-yyyy");
	}
	
	@Override
	public List<Parameter> getDefaultParameters() {
		List<Parameter> params = new ArrayList<Parameter>();
		params.add(new BooleanParameter(true, CHECK_ON_STARTUP,
				"Once a week, check GitHub for a newer VANTED release. Nothing is downloaded automatically."));
		return params;
	}
	
	@Override
	public void updatePreferences(Preferences preferences) {
		checkOnStartup = preferences.getBoolean(CHECK_ON_STARTUP, true);
	}
	
	@Override
	public String getPreferencesAlternativeName() {
		return "Update";
	}
}
