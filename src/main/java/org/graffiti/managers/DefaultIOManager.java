// ==============================================================================
//
// DefaultIOManager.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DefaultIOManager.java,v 1.20.4.1.2.1 2014/12/21 23:35:22 klapperipk Exp $

package org.graffiti.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFileChooser;

import org.ErrorMsg;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.core.GenericFileFilter;
import org.graffiti.graph.Graph;
import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.plugin.GenericPlugin;
import org.graffiti.plugin.io.InputSerializer;
import org.graffiti.plugin.io.OutputSerializer;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

/**
 * Handles the editor's IO serializers.
 * 
 * @version $Revision: 1.20.4.1.2.1 $
 * @vanted.revision 2.7.0 Standard save format
 */
public class DefaultIOManager implements IOManager {
	
	private class GravistoFileOpenFilter extends GenericFileFilter {
		
		/**
		 * @param extension
		 */
		public GravistoFileOpenFilter(String extension) {
			super(extension);
		}
		
		@Override
		public boolean accept(File file) {
			if (file.isDirectory())
				return true;
			if (file.getName().lastIndexOf(".") > 0) {
				String fileExt = file.getName().substring(file.getName().lastIndexOf("."));
				if (fileExt.equalsIgnoreCase(".GZ")) {
					String fileName = file.getName();
					fileName = fileName.substring(0, fileName.length() - ".gz".length());
					fileExt = fileName.substring(fileName.lastIndexOf("."));
				}
				for (Iterator<InputSerializer> itr = inputSerializer.iterator(); itr.hasNext();) {
					InputSerializer is = itr.next();
					String[] ext = is.getExtensions();
					for (int i = 0; i < ext.length; i++)
						if (ext[i].equalsIgnoreCase(fileExt))
							return true;
				}
			}
			return false;
		}
		
		@Override
		public String getDescription() {
			// return "Graph Files (" + getSupported("*", "; ") + ")";
			return "Supported Graph Files";
		}
	}
	
	// ~ Static fields/initializers =============================================
	
	// ~ Instance fields ========================================================
	
	/** The set of input serializers. */
	private final List<InputSerializer> inputSerializer;
	
	/** The set of output serializers. */
	private final List<OutputSerializer> outputSerializer;
	
	/** The file chooser used to open and save graphs. */
	private JFileChooser fc;
	
	/** The list of listeners. */
	private final List<IOManagerListener> listeners;
	
	public static final String STANDARD_SAVE_FORMAT = ".gml";
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new io manager.
	 */
	public DefaultIOManager() {
		inputSerializer = new ArrayList<InputSerializer>();
		outputSerializer = new ArrayList<OutputSerializer>();
		listeners = new LinkedList<IOManagerListener>();
		try {
			/*
			 * there might be a problem, when starting vanted, because if network drives are
			 * mapped (on windows) but the server is not reachable, the JFileChooser
			 * constructor stops This also means Vanted doesn't boot up.. To prevent this, a
			 * getter method was implemented so that the startup is garuanteed
			 */
			// fc = new JFileChooser();
			// System.out.println("created filechooser");
		} catch (AccessControlException ace) {
			// ErrorMsg.addErrorMessage(ace);
		}
	}
	
	// ~ Methods ================================================================
	
	public JFileChooser getFileChooser() {
		if (fc == null)
			fc = new JFileChooser();
		
		return fc;
	}
	
	@Override
	public void addInputSerializer(InputSerializer is) {
		// String[] inExtensions = is.getExtensions();
		
		// for (int j = 0; j < inExtensions.length; j++) {
		inputSerializer.add(is);
		// }
		fireInputSerializerAdded(is);
	}
	
	@Override
	public void addListener(IOManagerListener ioManagerListener) {
		listeners.add(ioManagerListener);
	}
	
	@Override
	public void addOutputSerializer(OutputSerializer os) {
		// String[] outExtensions = os.getExtensions();
		//
		// for (int j = 0; j < outExtensions.length; j++)
		outputSerializer.add(os);
		
		fireOutputSerializerAdded(os);
	}
	
	@Override
	public InputSerializer createInputSerializer(InputStream in, String extSearch) throws FileNotFoundException {
		ArrayList<InputSerializer> ins = new ArrayList<InputSerializer>();
		for (InputSerializer is : inputSerializer) {
			String[] ext = is.getExtensions();
			extsearch: for (int i = 0; i < ext.length; i++)
				if (ext[i].equalsIgnoreCase(extSearch)) {
					// System.out.println("Possible reader: "+is.getClass().getCanonicalName());
					ins.add(is);
					break extsearch;
				}
		}
		if (in == null)
			return ins.iterator().next();
		MyByteArrayOutputStream out = new MyByteArrayOutputStream();
		try {
			ResourceIOManager.copyContent(in, out, 5000);
			for (InputSerializer is : ins) {
				try {
					InputStream inps = new MyByteArrayInputStream(out.getBuff());
					if (is.validFor(inps)) {
						inps.close();
						return is;
					}
					
					inps.close();
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		} catch (IOException e1) {
			ErrorMsg.addErrorMessage(e1);
		}
		return null;
	}
	
	@Override
	public Set<String> getGraphFileExtensions() {
		Set<String> knownExt = new TreeSet<String>((String s1, String s2) -> {
			if (s1.equals(STANDARD_SAVE_FORMAT))
				return -1;
			else if (s2.equals(STANDARD_SAVE_FORMAT))
				return 1;
			
			return s1.compareTo(s2);
		});
		for (Iterator<InputSerializer> itr = inputSerializer.iterator(); itr.hasNext();) {
			InputSerializer is = itr.next();
			String[] ext = is.getExtensions();
			for (int i = 0; i < ext.length; i++) {
				knownExt.add(ext[i]);
			}
		}
		
		return knownExt;
	}
	
	@Override
	public JFileChooser createOpenFileChooser() {
		fc = getFileChooser();
		fc.resetChoosableFileFilters();
		// Set<String> knownExt = new TreeSet<String>();
		for (Iterator<InputSerializer> itr = inputSerializer.iterator(); itr.hasNext();) {
			InputSerializer is = itr.next();
			String[] ext = is.getExtensions();
			String[] desc = is.getFileTypeDescriptions();
			if (ext.length != desc.length) {
				ErrorMsg.addErrorMessage(
						"Error: File-Type descriptions do not match extensions - Class: " + is.toString());
				continue;
			}
			for (int i = 0; i < ext.length; i++) {
				// if (knownExt.contains(ext[i]))
				// ErrorMsg.addErrorMessage("Internal Error: Duplicate Input File Type Extension
				// - "
				// + ext[i] + " Class: " + is.toString());
				// knownExt.add(ext[i]);
				// System.out.println("Output: " + ext[i] + " Class: " +
				// is.toString());
				GravistoFileFilter gff = new GravistoFileFilter(ext[i], desc[i]);
				fc.addChoosableFileFilter(gff);
			}
		}
		
		fc.addChoosableFileFilter(new GravistoFileOpenFilter(null));
		
		return fc;
	}
	
	@Override
	public OutputSerializer createOutputSerializer(String extSearch) {
		
		return createOutputSerializer(extSearch, null);
		
	}
	
	@Override
	public OutputSerializer createOutputSerializer(String extSearch, String fileTypeDescription) {
		for (Iterator<OutputSerializer> itr = outputSerializer.iterator(); itr.hasNext();) {
			OutputSerializer os = itr.next();
			String[] ext = os.getExtensions();
			String[] fileTypeDescriptions = os.getFileTypeDescriptions();
			if (fileTypeDescriptions != null && fileTypeDescriptions.length > 0 && fileTypeDescription != null) {
				boolean descriptionMatches = false;
				for (int i = 0; i < fileTypeDescriptions.length; i++)
					if (fileTypeDescriptions[i].equalsIgnoreCase(fileTypeDescription)) {
						descriptionMatches = true;
						break;
					}
				for (int i = 0; i < ext.length; i++)
					if (ext[i].equalsIgnoreCase(extSearch) && descriptionMatches)
						return os;
			} else
				for (int i = 0; i < ext.length; i++)
					if (ext[i].equalsIgnoreCase(extSearch))
						return os;
		}
		return null;
	}
	
	@Override
	public JFileChooser createSaveFileChooser() {
		return createSaveFileChooser(null);
	}
	
	@Override
	public JFileChooser createSaveFileChooser(Graph g) {
		fc = getFileChooser();
		String defaultExt = STANDARD_SAVE_FORMAT;
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			defaultExt = ".xml";
		GravistoFileFilter defaultFileFilter = null;
		fc.resetChoosableFileFilters();
		fc.setAcceptAllFileFilterUsed(false);
		// Set<String> knownExt = new TreeSet<String>();
		for (OutputSerializer os : outputSerializer) {
			/*
			 * if the given graph cannot be serialized with the current output serializer,
			 * skip it
			 */
			if (g != null && !os.validFor(g))
				continue;
			
			String[] ext = os.getExtensions();
			String[] desc = os.getFileTypeDescriptions();
			if (ext.length != desc.length) {
				ErrorMsg.addErrorMessage(
						"Error: File-Type descriptions do not match extensions - Class: " + os.toString());
				continue;
			}
			for (int i = 0; i < ext.length; i++) {
				// if (knownExt.contains(ext[i]))
				// ErrorMsg.addErrorMessage("Error: Duplicate Output File Type Extension - " +
				// ext[i] + " Class: "
				// + os.toString());
				// knownExt.add(ext[i]);
				GravistoFileFilter gff = new GravistoFileFilter(ext[i], desc[i]);
				
				if (defaultFileFilter == null && gff.getExtension().equalsIgnoreCase(defaultExt)) {
					defaultFileFilter = gff;
				} else {
					fc.addChoosableFileFilter(gff);
				}
			}
		}
		if (defaultFileFilter != null)
			fc.addChoosableFileFilter(defaultFileFilter);
		return fc;
	}
	
	@Override
	public boolean hasInputSerializer() {
		return !inputSerializer.isEmpty();
	}
	
	@Override
	public boolean hasOutputSerializer() {
		return !outputSerializer.isEmpty();
	}
	
	@Override
	public void pluginAdded(GenericPlugin plugin, PluginDescription desc) {
		// register add input serializers
		InputSerializer[] is = plugin.getInputSerializers();
		
		for (int i = 0; i < is.length; i++) {
			addInputSerializer(is[i]);
		}
		
		// register all output serializers
		OutputSerializer[] os = plugin.getOutputSerializers();
		
		for (int i = 0; i < os.length; i++) {
			addOutputSerializer(os[i]);
		}
	}
	
	@Override
	public boolean removeListener(IOManagerListener l) {
		return listeners.remove(l);
	}
	
	/**
	 * Returns a string representation of the io manager. Useful for debugging.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "inputSerializer: " + inputSerializer + " outputSerializer: " + outputSerializer;
	}
	
	/**
	 * Informs every registered io manager listener about the addition of the given
	 * input serializer.
	 * 
	 * @param is
	 *           the input serializer, which was added.
	 */
	private void fireInputSerializerAdded(InputSerializer is) {
		for (Iterator<IOManagerListener> i = listeners.iterator(); i.hasNext();) {
			IOManager.IOManagerListener l = i.next();
			
			l.inputSerializerAdded(is);
		}
	}
	
	/**
	 * Informs every output serializer about the addition of the given output
	 * serializer.
	 * 
	 * @param os
	 *           the output serializer, which was added.
	 */
	private void fireOutputSerializerAdded(OutputSerializer os) {
		for (Iterator<IOManagerListener> i = listeners.iterator(); i.hasNext();) {
			IOManager.IOManagerListener l = i.next();
			
			l.outputSerializerAdded(os);
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
