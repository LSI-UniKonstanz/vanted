package de.ipk_gatersleben.ag_nw.graffiti.plugins.addons;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;

import org.ErrorMsg;
import org.graffiti.managers.pluginmgr.PluginDescription;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;

public class Addon {

	public static URL[] getURLfromJarfile(File jarfiles) {
		try {
			return new URL[] { jarfiles.toURI().toURL() };
		} catch (MalformedURLException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;

	}

	public static URL getXMLURL(ClassLoader loader, File jarFile) {
		return loader.getResource(jarFile.getName().replaceAll(".jar", ".xml"));
	}

	private File jarfile;
	private URL xmlURL;
	private PluginDescription description;
	private Boolean isactive;
	private ImageIcon icon;

	public Addon(File file, URL xmlURL, PluginDescription pd, boolean active, ImageIcon icon) {
		this.jarfile = file;
		this.xmlURL = xmlURL;
		this.description = pd;
		this.isactive = active;
		this.icon = icon;
	}

	public URL getXMLURL() {
		return xmlURL;
	}

	public PluginDescription getDescription() {
		return description;
	}

	public Boolean isActive() {
		return isactive;
	}

	void setIsActive(boolean active) {
		isactive = active;
	}

	public String getName() {
		return jarfile.getName().toLowerCase().replaceAll(".jar", "");
	}

	public File getJarFile() {
		return jarfile;
	}

	public ImageIcon getIcon() {
		return icon;
	}

	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}

	/**
	 * Tests, if this addon is compatible with the currently running version of
	 * VANTED
	 * 
	 * @return
	 */
	public boolean isTestedWithRunningVersion() {
		// 1.8,2.1, //// 1.8,
		StringTokenizer tok = new StringTokenizer(description.getCompatibleVersion(), ",");
		boolean iscompatible = false;

		/*
		 * split version number into separate main/min/build version number string array
		 */
		String[] strSplitMainversion = DBEgravistoHelper.DBE_GRAVISTO_VERSION_CODE.split("\\.");
		String[] strSplitMinCompVersion = DBEgravistoHelper.DBE_MIN_COMPATIBILITY_VERSION.split("\\.");

		/*
		 * we define N part version number, where N is defined by the number of sub
		 * version numbers in the main version number string
		 */
		long[] longArrMainVersion = new long[strSplitMainversion.length];
		long[] longArrMinVersion = new long[strSplitMainversion.length];

		for (int i = 0; i < strSplitMainversion.length; i++) {
			longArrMainVersion[i] = Long.parseLong(strSplitMainversion[i]);
			longArrMinVersion[i] = Long.parseLong(strSplitMinCompVersion[i]);
		}

		while (tok.hasMoreTokens()) {
			String[] strSplitAddonVersion = tok.nextToken().split("\\.");

			for (int i = 0; i < strSplitAddonVersion.length; i++) {
				long longPartVersion;
				try {
					longPartVersion = Long.parseLong(strSplitAddonVersion[i]);
				} catch (NumberFormatException e) {
					longPartVersion = longArrMainVersion[i];
				}
				/*
				 * if the main / sub version is definitely larger then the min-main/sub version
				 * we don't need to check further.
				 */
				if (longArrMinVersion[i] < longPartVersion && longPartVersion <= longArrMainVersion[i]) {
					return true;

				}
				/*
				 * But if it is equal to some minimum value we need to check the next part of
				 * the version as well
				 */
				else if (longArrMinVersion[i] <= longPartVersion)
					iscompatible = true;
				else {
					iscompatible = false;
					break;
				}
			}
		}
		return iscompatible;
	}

}
