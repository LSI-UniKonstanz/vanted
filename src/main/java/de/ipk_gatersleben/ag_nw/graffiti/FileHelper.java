/**
 * Copyright (c) 2003-2014 IPK Gatersleben, Germany
 * Copyright (c) 2014-2015 Monash University, Australia
 */
package de.ipk_gatersleben.ag_nw.graffiti;

/**
 * Created on 13/05/2004
 * Extended on 14/08/2015
 * 
 * @author Tobias Czauderna
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.ErrorMsg;
import org.HelperClass;
import org.OpenFileDialogService;
import org.UNCFileLocationCheck;
import org.graffiti.editor.GravistoService;

@SuppressWarnings("nls")
public class FileHelper implements HelperClass {
	
	public static String getFileName(final String defaultExt,
			final String description, String defaultFileName) {
		JFileChooser fc = new JFileChooser();
		
		OpenFileDialogService.setActiveDirectoryFor(fc);
		if (defaultFileName != null && defaultFileName.length() > 0)
			fc.setSelectedFile(new File(defaultFileName));
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				return f.getName().toUpperCase().endsWith(defaultExt.toUpperCase());
			}
			
			@Override
			public String getDescription() {
				return defaultExt + " files";
			}
		});
		
		String fileName = "";
		File file = null;
		boolean needFile = true;
		
		while (needFile) {
			int returnVal = fc.showDialog(GravistoService.getInstance()
					.getMainFrame(), "Create " + description);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				fileName = file.getName();
				// System.err.println(fileName);
				String ext = defaultExt;
				
				if (fileName.indexOf(".") == -1) {
					fileName = file.getName() + "." + ext;
					file = new File(file.getAbsolutePath() + "." + ext);
				}
				
				// checks, if location is on UNC windows network path
				if (UNCFileLocationCheck.showUNCPathConfirmDialogForPath(file) == UNCFileLocationCheck.CONFIRM) {
					
					// System.err.println(fileName);
					if (file.exists()) {
						if (JOptionPane.showConfirmDialog(GravistoService.getInstance()
								.getMainFrame(),
								"<html>Do you want to overwrite the existing file <i>"
										+ fileName + "</i>?</html>", "Overwrite File?",
								JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							needFile = false;
						} else
							file = null;
					} else {
						needFile = false;
					}
					
				} else {
					file = null;
				}
				
			} else {
				// leave loop
				needFile = false;
			}
		}
		
		if (file != null) {
			OpenFileDialogService.setActiveDirectoryFrom(fc.getCurrentDirectory());
			return file.getAbsolutePath();
		} else {
			return null;
		}
	}
	
	public static String getFileName(final String defaultExt,
			final String description) {
		return getFileName(defaultExt, description, null);
	}
	
	/**
	 * Recursively deletes a directory
	 * 
	 * @param path
	 *           the path of the directory to be deleted
	 */
	public static void deleteDirRecursively(File f) {
		if (!f.exists())
			return;
		if (!f.isDirectory())
			f.delete();
		else {
			for (File file : f.listFiles()) {
				if (file.isDirectory())
					deleteDirRecursively(file);
				else
					file.delete();
			}
		}
	}
	
	/**
	 * Copy file from jar to file system.
	 * 
	 * @param uri
	 *           URI for the jar file
	 * @param sourceFolder
	 *           folder within the jar file
	 * @param targetFolder
	 *           folder on the file system to copy the file to
	 * @param fileName
	 *           file to copy
	 */
	public static void copyFileFromJar(URI uri, String sourceFolder, String targetFolder, String fileName) {
		
		copyFilesFromJar(uri, sourceFolder, targetFolder, new String[] { fileName });
		
	}
	
	/**
	 * Copy files from jar to file system.
	 * 
	 * @param uri
	 *           URI for the jar file
	 * @param sourceFolder
	 *           folder within the jar file
	 * @param targetFolder
	 *           folder on the file system to copy the files to
	 * @param fileNames
	 *           files to copy
	 */
	public static void copyFilesFromJar(final URI uri, final String sourceFolder, final String targetFolder, final String[] fileNames) {
		
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				
				Map<String, String> env = new HashMap<>();
				try (FileSystem fileSystem = FileSystems.newFileSystem(uri, env)) {
					for (String fileName : fileNames) {
						Path sourcePath = fileSystem.getPath("/" + sourceFolder + "/" + fileName);
						Path targetPath = Paths.get(targetFolder, fileName);
						if (Files.exists(sourcePath))
							compareAndCopyFile(sourcePath, targetPath);
					}
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
				
			}
			
		};
		Thread t = new Thread(runnable);
		t.start();
		
	}
	
	/**
	 * Copy file from resource (as stream) to file system.
	 * 
	 * @param sourceFolder
	 *           folder on the resource to copy the files from
	 * @param targetFolder
	 *           folder on the file system to copy the files to
	 * @param fileName
	 *           file to copy
	 */
	public static void copyFileFromStream(final String sourceFolder, final String targetFolder, final String fileName) {
		
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				
				try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(sourceFolder + "/" + fileName)) {
					int index = fileName.lastIndexOf(".");
					Path tempPath = Files.createTempFile(fileName.substring(0, index), fileName.substring(index));
					try (OutputStream outputStream = new FileOutputStream(tempPath.toFile())) {
						byte buffer[] = new byte[1024];
						int length;
						while ((length = inputStream.read(buffer)) != -1)
							outputStream.write(buffer, 0, length);
						outputStream.close();
						inputStream.close();
						if (Files.exists(tempPath))
							FileHelper.compareAndCopyFile(tempPath, Paths.get(targetFolder, fileName));
						Files.deleteIfExists(tempPath);
					}
				} catch (IOException e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
			
		};
		Thread t = new Thread(runnable);
		t.start();
		
	}
	
	/**
	 * Copy file from a folder on the file system to another folder on the file system.
	 * 
	 * @param sourceFolder
	 *           folder on the file system to copy the file from
	 * @param targetFolder
	 *           folder on the file system to copy the file to
	 * @param fileName
	 *           file to copy
	 */
	public static void copyFile(String sourceFolder, String targetFolder, String fileName) {
		
		copyFiles(sourceFolder, targetFolder, new String[] { fileName });
		
	}
	
	/**
	 * Copy files from a folder on the file system to another folder on the file system.
	 * 
	 * @param sourceFolder
	 *           folder on the file system to copy the files from
	 * @param targetFolder
	 *           folder on the file system to copy the files to
	 * @param fileNames
	 *           files to copy
	 */
	public static void copyFiles(final String sourceFolder, final String targetFolder, final String[] fileNames) {
		
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				
				for (String fileName : fileNames) {
					Path sourcePath = Paths.get(sourceFolder, fileName);
					Path targetPath = Paths.get(targetFolder, fileName);
					if (Files.exists(sourcePath))
						compareAndCopyFile(sourcePath, targetPath);
				}
				
			}
			
		};
		Thread t = new Thread(runnable);
		t.start();
		
	}
	
	/**
	 * Compare and copy a file. Copies the file only if it is a newer version.
	 * 
	 * @param sourcePath
	 *           source path
	 * @param targetPath
	 *           target path
	 */
	static void compareAndCopyFile(Path sourcePath, Path targetPath) {
		
		boolean copyFile = true;
		FileTime sourceLastModifiedTime = null;
		try {
			Map<String, Object> sourceMap = Files.readAttributes(sourcePath, "lastModifiedTime");
			sourceLastModifiedTime = (FileTime) sourceMap.get("lastModifiedTime");
			if (sourceLastModifiedTime != null && Files.exists(targetPath)) {
				Map<String, Object> targetMap = Files.readAttributes(targetPath, "creationTime");
				FileTime targetCreationTime = (FileTime) targetMap.get("creationTime");
				if (targetCreationTime != null && sourceLastModifiedTime.to(TimeUnit.SECONDS) <= targetCreationTime.to(TimeUnit.SECONDS))
					copyFile = false;
			}
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
		if (copyFile)
			try {
				Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
				if (sourceLastModifiedTime != null)
					Files.setAttribute(targetPath, "creationTime", FileTime.from(sourceLastModifiedTime.to(TimeUnit.SECONDS), TimeUnit.SECONDS));
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e);
			}
		
	}
	
}
