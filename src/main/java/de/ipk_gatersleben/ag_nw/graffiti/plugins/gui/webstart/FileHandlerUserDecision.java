package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.FolderPanel;
import org.StringManipulationTools;

public class FileHandlerUserDecision {
	
	private HashMap<File, ArrayList<DragAndDropHandler>> workingset;
	private FolderPanel fp;
	private HashMap<File, JComboBox> file2result = new HashMap<File, JComboBox>();
	private boolean selectallbuttonsadded = false;
	
	public FileHandlerUserDecision(HashMap<File, ArrayList<DragAndDropHandler>> workingSet) {
		this.workingset = workingSet;
		
		// creates a dialog asking the user to choose for each file, which kind of handler he/she want to use
		fp = new FolderPanel("Please select the appropriate file loader for some dropped files", false, false, false, null);
		fp.addCollapseListenerDialogSizeUpdate();
		
	}
	
	private void addSelectAllButtons() {
		
		// create buttons so the user can select a handler for all files
		ArrayList<JComponent> guiElements = new ArrayList<JComponent>();
		for (DragAndDropHandler dh : getHandlers(false)) {
			JButton jb = new JButton("<html><small><center>" + StringManipulationTools.getWordWrap(dh.toString(), 20));
			final DragAndDropHandler dhf = dh;
			jb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					for (JComboBox jcb : file2result.values()) {
						jcb.setSelectedItem(dhf);
					}
				}
			});
			guiElements.add(jb);
		}
		Collections.sort(guiElements, new Comparator<JComponent>() {

			@Override
			public int compare(JComponent o1, JComponent o2) {
				if(o1 instanceof JButton && o2 instanceof JButton) {
					return ((JButton)o1).getText().compareTo(((JButton)o2).getText());
				} else
					return 0;
			}
			
		});
		fp.addGuiComponentRow(new JLabel("Load all as  "), TableLayout.getMultiSplit(guiElements), false, 2);
	}
	
	public HashSet<DragAndDropHandler> getHandlers(boolean add_also_single_handlers) {
		HashSet<DragAndDropHandler> allPossibleHandlers = new HashSet<DragAndDropHandler>();
		for (File f : workingset.keySet()) {
			if (add_also_single_handlers || workingset.get(f).size() > 1)
				for (DragAndDropHandler dh : workingset.get(f))
					allPossibleHandlers.add(dh);
		}
		return allPossibleHandlers;
	}
	
	public void addRows(File f) {
		if (!selectallbuttonsadded) {
			addSelectAllButtons();
			selectallbuttonsadded = true;
		}
		List<DragAndDropHandler> ddhArray = new ArrayList<DragAndDropHandler>(workingset.get(f));
		Collections.sort(ddhArray, new Comparator<DragAndDropHandler>() {

			@Override
			public int compare(DragAndDropHandler o1, DragAndDropHandler o2) {
				return o1.toString().compareTo(o2.toString());
			}
			
		});
		JComboBox jc = new JComboBox(ddhArray.toArray());
		file2result.put(f, jc);
		if (workingset.get(f).size() != 1)
			fp.addGuiComponentRow(new JLabel(f.getName() + "     "), jc, false, 2);
		
	}
	
	public boolean atLeastOneFileNeedsUserDecision() {
		return fp.getRowCount() > 1;
	}
	
	public Object getFolderPanel() {
		
		fp.setMaximumRowCount(20);
		fp.layoutRows();
		return fp;
	}
	
	public HashMap<File, JComboBox> getUserSelection() {
		return file2result;
	}
	
}
