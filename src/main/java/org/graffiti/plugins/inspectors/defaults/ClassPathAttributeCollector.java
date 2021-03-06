// ==============================================================================
//
// ClassPathAttributeCollector.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ClassPathAttributeCollector.java,v 1.6 2010/12/22 13:05:58 klukas Exp $

package org.graffiti.plugins.inspectors.defaults;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.ErrorMsg;
import org.graffiti.attributes.Attribute;
import org.graffiti.util.InstanceLoader;

/**
 * Searches for attributes in the current <code>CLASSPATH</code>.
 * 
 * @version $Revision: 1.6 $
 */
public class ClassPathAttributeCollector {
	// ~ Static fields/initializers =============================================
	
	/** The logger for the current class. */
	// private static final Logger logger =
	// Logger.getLogger(ClassPathAttributeCollector.class.getName());
	
	// ~ Methods ================================================================
	
	/**
	 * Collects all attributes from the given class path.
	 * 
	 * @return An enumeration of all attributes from the given class path.
	 */
	public List<String> collectAttributes() {
		return collectFilesInRoots(
				splitClassPath(System.getProperty("java.class.path"), System.getProperty("path.separator")));
	}
	
	/**
	 * Checks if the given <code>fileName</code> looks like a graffiti plugin
	 * description file. Does no dtd check or XML parsing.
	 * 
	 * @param fileName
	 *           the name of the file to check.
	 * @return DOCUMENT ME!
	 */
	protected String isAttribute(String fileName) {
		if (fileName.endsWith("Attribute.class") && (fileName.indexOf('$') == -1)) {
			try {
				// TODO: remove unique label hack
				// if (fileName.indexOf(System.getProperty("file.separator")) != -1) {
				// fileName = fileName.substring
				// (fileName.lastIndexOf(System.getProperty("file.separator"))+1,
				// fileName.lastIndexOf(".class"));
				// } else {
				fileName = fileName.replace(File.separatorChar, '.');
				fileName = fileName.substring(0, fileName.lastIndexOf(".class"));
				
				// }
				Attribute attr = (Attribute) InstanceLoader.createInstance(fileName, "unique$&%%$%$%");
				
				if (attr == null)
					throw new Exception("Could not create instance.");
				
				return fileName;
			} catch (Exception e) {
				System.out.println(e);
				
				return "";
			}
		} else {
			return "";
		}
	}
	
	/**
	 * Splits the <code>CLASSPATH</code> string and returns the elements of the
	 * <code>CLASSPATH</code> in a list.
	 * 
	 * @param classPath
	 *           DOCUMENT ME!
	 * @param separator
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	List<String> splitClassPath(String classPath, String separator) {
		List<String> result = new LinkedList<String>();
		
		StringTokenizer tokenizer = new StringTokenizer(classPath, separator);
		
		// split
		while (tokenizer.hasMoreTokens()) {
			result.add(tokenizer.nextToken());
		}
		
		// logger.info(result.toString());
		return result;
	}
	
	/**
	 * Returns <code>true</code>, if the given file ends with &quot;.jar&quot; or
	 * &quot;.zip&quot;.
	 * 
	 * @param fileName
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	private static boolean isZipFile(String fileName) {
		// TODO remove hard coded strings
		return fileName.endsWith(".jar") || fileName.endsWith(".zip");
	}
	
	/**
	 * Collects all attributes from the given roots, recursively.
	 * 
	 * @param roots
	 *           the list of roots, which should be searched for.
	 * @return result list from the search.
	 */
	private List<String> collectFilesInRoots(List<String> roots) {
		List<String> acc = new LinkedList<String>();
		
		for (Iterator<String> i = roots.iterator(); i.hasNext();) {
			String next = (String) i.next();
			System.out.println("searching " + next);
			gatherFiles(new File(next), "", next, acc);
		}
		
		return acc;
	}
	
	/**
	 * Checks if the given files contain plugin description files.
	 * 
	 * @param classRoot
	 *           DOCUMENT ME!
	 * @param fileName
	 *           DOCUMENT ME!
	 * @param path
	 *           DOCUMENT ME!
	 * @param acc
	 *           DOCUMENT ME!
	 */
	private void gatherFiles(File classRoot, String fileName, String path, List<String> acc) {
		File root = new File(classRoot, fileName);
		
		if (root.isFile()) {
			// the file is a plugin file. therefore search in the
			// plugin file for attributes, too.
			if (isZipFile(root.toString())) {
				Enumeration<JarEntry> entries;
				
				try (JarFile jarFile = new JarFile(root)) {
					entries = jarFile.entries();
				} catch (IOException e) {
					ErrorMsg.addErrorMessage(e);
					return;
				}
				
				String name = new String();
				
				while (entries.hasMoreElements()) {
					JarEntry jarEntry = (JarEntry) entries.nextElement();
					
					// name = classRoot.getAbsolutePath()+"/"+jarEntry.getName();
					name = isAttribute(jarEntry.getName());
					
					if (!"".equals(name)) {
						System.out.println("is attr: " + name);
						acc.add(name);
					}
				}
				
				// the file is an attribute. Add it to the list of
				// attributes.
			} else {
				String absPath = root.getAbsolutePath();
				String nearlyClassName = absPath.substring(absPath.lastIndexOf(path) + path.length() + 1);
				String name = isAttribute(nearlyClassName);
				
				if (!"".equals(name)) {
					System.out.println("is attr: " + name);
					acc.add(name);
				}
			}
			
			// root is a directory: recursion
		} else {
			String[] contents = root.list();
			
			if (contents != null) {
				for (int i = 0; i < contents.length; i++) {
					// System.out.println(" descending: " + contents[i]);
					gatherFiles(classRoot, fileName + File.separatorChar + contents[i], path, acc);
				}
			}
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
