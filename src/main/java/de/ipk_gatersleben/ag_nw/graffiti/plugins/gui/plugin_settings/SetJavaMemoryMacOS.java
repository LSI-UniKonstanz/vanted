/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.plugin_settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.HomeFolder;
import org.apache.log4j.Logger;
import org.graffiti.editor.MainFrame;

/**
 * @author matthiak
 *
 */
public class SetJavaMemoryMacOS {

	static private Logger logger = Logger.getLogger(SetJavaMemoryMacOS.class);
	
	static final String RAMCONFIG = "vanted_ram_config.cfg";
	
	static final long MAXRAM = 6_000_000_000L;
	
	static final String KEY_RAM = "vanted.ram";
	
	String appExectutableFolder = null;
	/**
	 * 
	 */
	public SetJavaMemoryMacOS() {
		Icon icon = new ImageIcon(getClass().getClassLoader().getResource("images/MemoryIcon.png"));
		Object[] options = { "OK" };
		int showConfirmDialog = JOptionPane.showOptionDialog(MainFrame.getInstance(), 
				"Ram adaption", "macos", JOptionPane.PLAIN_MESSAGE, JOptionPane.INFORMATION_MESSAGE, 
				
				icon,
				options,
				options[0]);
		
		checkFile();
		
	}
	
	private void checkFile() {
		String homeFolder = HomeFolder.getHomeFolder();
		logger.debug(homeFolder);
		File appFolder = new File(homeFolder);
		File configFile = null;
		for(File curFile : appFolder.listFiles()){
			if(curFile.getName().equals(RAMCONFIG)){
				configFile = curFile;
				break;
			}
		}
		if(configFile != null){
			logger.debug("filename: " + configFile.getAbsolutePath());
		}
		else {
			logger.debug("no config file found");
			configFile = new File(homeFolder+"/"+RAMCONFIG);
			createConfigFile(configFile);
			long calculateMemory = calculateMemory();
			logger.debug("available memory: "+calculateMemory/1024/1024+"mb");
		}
	}
	
	private void createConfigFile(File configFile) {
		long calculateMemory = calculateMemory();
		
	}
	
	private long getPhysicalMemory() {
		long mem = -1;
		
		com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
		mem = operatingSystemMXBean.getTotalPhysicalMemorySize();
		
		return mem;
	}
	
	/**
	 * returns the calculated memory in bytes
	 * @return
	 */

	private long calculateMemory() {
		long mem = getPhysicalMemory();
		
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
		return executionpath;
	}
	
	public static void main(String[] args) {
//		BasicConfigurator.configure();
		new SetJavaMemoryMacOS();
	}
}
