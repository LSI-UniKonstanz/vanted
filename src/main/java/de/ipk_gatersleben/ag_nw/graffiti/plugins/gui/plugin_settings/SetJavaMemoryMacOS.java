/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.plugin_settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.AttributeHelper;
import org.apache.log4j.Logger;
import org.graffiti.editor.MainFrame;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * This class will take care of setting the available memory for Vanted by manipulating the Pinfo.list
 * file found in the app bundle of Mac application distributions.
 * To have the best experience Java programs should be started using a java starter wrapper. This wrapper starter
 * will read necessary JVM parameters from the Pinfo.list file. We are interested in the Xmx parameter of the JVM
 * Since we want to give Java 1/3 or max 8GB of RAM, which depends on the machine, we need to be able to change the 
 * Xmx setting in this file.
 * This class will read and write this file. 
 * No fancy XML parsing involved, just simple read and replace
 * 
 * @author matthiak
 *
 */
public class SetJavaMemoryMacOS {

	static private Logger logger = Logger.getLogger(SetJavaMemoryMacOS.class);
	
	static private boolean DEBUG = true;
	static private String staticAppFolder = "/Applications/Vanted.app/Contents/Java/"; //for debugging
	
	static final String RAMCONFIG = "vanted_ram_config.cfg";
	
	static final long MAXRAM = 6_000_000_000L;
	
	static final String KEY_RAM = "vanted.ram";
	
	String appExectutableFolder = null;
	
	String pinfoFileContent;

	/** 
	 * will be used to find the string that needs to be replaced
	 */
	private String xmxStringVal;
	
	private Long xmxVal;
	
	File infoplistFile;

	private long maxVantedmem;
	/**
	 * 
	 */
	public SetJavaMemoryMacOS() {
		if( ! AttributeHelper.macOSrunning())
			return;
		xmxVal = readXmxFromInfoPlist();
		logger.debug("xmxval = "+xmxVal);
		logger.debug("xmxstringval:"+xmxStringVal);
		maxVantedmem = calculateMaxVantedMemory();
		logger.debug("max automatic vanted mem:"+maxVantedmem);
		
		writeInfoPlistWithMemXmx(maxVantedmem);
		showConfirmation();
	}
	
	public static void showDialog() {
		new SetJavaMemoryMacOS();
	}
	
	private void showConfirmation() {
		Icon icon = new ImageIcon(getClass().getClassLoader().getResource("images/MemoryIcon.png"));
		Object[] options = { "OK" };
		JOptionPane.showOptionDialog(MainFrame.getInstance(), 
				"The size of used RAM has been optimized."
				+ "\nVanted will use "+maxVantedmem/1024/1024+"mb"
				+ "\nYou see the effect upon the next start", "macos", JOptionPane.PLAIN_MESSAGE, JOptionPane.INFORMATION_MESSAGE, 
				icon,
				options,
				options[0]);
		
	}
	
	private void writeInfoPlistWithMemXmx(long memory) {
		String newXmxVal = Long.toString(memory /1024 / 1024);
		String newInfoPlist = pinfoFileContent.replace("-Xmx"+xmxStringVal, "-Xmx"+newXmxVal+"m");
		
		try (FileWriter writer = new FileWriter(infoplistFile) ){
			writer.write(newInfoPlist);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * returns Xmx value in byte
	 * @return
	 */
	private Long readXmxFromInfoPlist() {
		
		Long xmxRetVal = null;
		
		infoplistFile = findInfoPlistConfigFile();
		
		pinfoFileContent = loadPfileAsString(infoplistFile);
		
		if(pinfoFileContent == null)
			return null;
		
		String searchTerm = "<string>-Xmx";
		int start = pinfoFileContent.indexOf(searchTerm);
		String stringStartofXmx = pinfoFileContent.substring(
				start + searchTerm.length());
		
		int indexOfCloseTag = stringStartofXmx.indexOf("</string>"); //first encounter of closing tag
		
		xmxStringVal = stringStartofXmx.substring(
				0,
				indexOfCloseTag);
		xmxStringVal = xmxStringVal.toLowerCase();
		if(xmxStringVal.endsWith("g")) {
			try {
				xmxRetVal = Long.parseLong(xmxStringVal.substring(0, xmxStringVal.length() - 1));
				xmxRetVal = xmxRetVal * 1024 * 1024 * 1024;
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(xmxStringVal.endsWith("m")) {
			try {
				xmxRetVal = Long.parseLong(xmxStringVal.substring(0, xmxStringVal.length() - 1));
				xmxRetVal = xmxRetVal * 1024 * 1024;
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		
		
		return xmxRetVal;
	}
	
	/**
	 * tries to find the Pfile in the directory Vanted runs.
	 * This will usually only work if Vanted is installed and running out of its app Bundle
	 * @param configFile
	 * @return
	 */
	private File findInfoPlistConfigFile() {
		String appFolderLocation = null;
		
		if(DEBUG)
			appFolderLocation = staticAppFolder;
		
		/* find execution path of program */
		if(appFolderLocation == null)
			appFolderLocation = getAppFolderLocation();
		
		/* search for Info.plist */
		String searchhint = "/Contents/";
		if(appFolderLocation.contains(searchhint)) {
			String basePath = appFolderLocation.substring(0,  appFolderLocation.indexOf(searchhint) + searchhint.length());
			File contentDir = new File(basePath);
			if (contentDir.isDirectory()){
				File[] listFiles = contentDir.listFiles();
				if(listFiles != null && listFiles.length > 0) {
					for(File curFile : listFiles) {
						if(curFile.getName().equals("Info.plist")) {
							return curFile;
						}
							
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * returns the pfile as string or NULL if it wasn't able to read it.
	 * @param pfile
	 * @return
	 */
	String loadPfileAsString(File pfile) {
		if(pfile == null)
			return null;
		
		StringBuffer buf = new StringBuffer();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(pfile)) ){
			String line;
			while((line = reader.readLine()) != null) {
				buf.append(line).append("\n");
			}
			return buf.toString();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * returns the calculated memory in bytes
	 * @return
	 */

	private long calculateMaxVantedMemory() {
		long mem = SystemAnalysis.getRealSystemMemoryInByte();
		
		mem = mem / 3 > MAXRAM ? MAXRAM : mem /3; 
		
		return mem;
	}
	
	private String getAppFolderLocation() {
		String executionpath = null;

		URL[] urls = ((URLClassLoader)Thread.currentThread().getContextClassLoader()).getURLs();
		logger.debug("printing all urls for currents threads context classloader");
		
		
		for(URL curURL : urls) {
			logger.debug(curURL.getPath() + " " + curURL.getFile());
			if(curURL.getPath().endsWith(".jar")) {
				executionpath = curURL.getPath().substring(0, curURL.getPath().lastIndexOf("/"));
				executionpath = executionpath.replace("%20", " ");
				break;
			}
		}
		logger.debug("executionpath: "+executionpath);
		return executionpath;
	}
	
	public static void main(String[] args) {
//		BasicConfigurator.configure();
		new SetJavaMemoryMacOS();
	}
}
