package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.TextEditor;

/*

 * %W% %E%
 * 
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright notice, 
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * 
 * Neither the name of Oracle or the names of contributors may 
 * be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL 
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST 
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY 
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, 
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

/*
 * %W% %E%
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import javax.swing.text.Segment;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;

import org.graffiti.editor.MainFrame;

public class TextEditor extends JPanel {

	private static final long serialVersionUID = 1L;

    
    private JFrame frame;
    
    private JMenuBar menuBar;
    
    private static int windowWidth = 750;
    private static int windowHeight = 550;
    
    private String title = "TextEditor";
    
    private boolean titleChanging = true;
    
    private FileFilter fileFilter;
    
    private JTextArea editor;

    protected FileDialog fileDialog;

    protected UndoableEditListener undoHandler = new UndoHandler();
    
    protected JMenuItem saveDocAs, saveDoc;

    protected UndoManager undo = new UndoManager();

    public static final String openAction = "open";
    public static final String newAction  = "new";
    public static final String saveAction = "save";
    public static final String saveAsAction = "save as";
    
    protected File file;
    
    protected boolean saved;
    

    public TextEditor() {
		super(true);
		frame = new JFrame();

		fileFilter = null;
		
		file = null;
		saved = true;
	
		setBorder(BorderFactory.createEtchedBorder());
		setLayout(new BorderLayout());
	
		// create the embedded JTextComponent
		JTextArea tc = new JTextArea();
		tc.setDragEnabled(true);
		Font f = (new Font("Courier New", Font.PLAIN, 13));
		tc.setFont(f);
		
		editor = tc;
		editor.setTabSize(4);
		// Add this as a listener for undoable edits.
		editor.getDocument().addUndoableEditListener(undoHandler);
		JScrollPane scroller = new JScrollPane();
		JViewport port = scroller.getViewport();
		port.add(editor);
	
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());	
		panel.add("Center", scroller);
		add("Center", panel);
	    
	    menuBar = new JMenuBar();
	    JMenu fileMenu = new JMenu("File");
	    
	    JMenuItem newDoc = new JMenuItem("New");
	    newDoc.setActionCommand("new");
	    newDoc.setAction(new NewAction());
	    newDoc.setAccelerator(KeyStroke.getKeyStroke('N',InputEvent.CTRL_DOWN_MASK));
	    JMenuItem openDoc = new JMenuItem("Open");
	    openDoc.setActionCommand("open");
	    openDoc.setAction(new OpenAction());
	    openDoc.setAccelerator(KeyStroke.getKeyStroke('O',InputEvent.CTRL_DOWN_MASK));	    
	    saveDoc = new JMenuItem("Save");
	    saveDoc.setActionCommand("save");
	    saveDoc.setAction(new SaveAction());
	    saveDoc.setAccelerator(KeyStroke.getKeyStroke('S',InputEvent.CTRL_DOWN_MASK));
	    saveDocAs = new JMenuItem("Save As");
	    saveDocAs.setActionCommand("saveas");
	    saveDocAs.setAction(new SaveAsAction());
	    JMenuItem closeEditor = new JMenuItem("Exit");
	    closeEditor.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				new AppCloser().windowClosing(new WindowEvent(getFrame(),WindowEvent.WINDOW_CLOSING));
			}});

	    fileMenu.add(newDoc);
	    fileMenu.add(openDoc);
	    fileMenu.add(saveDoc);
	    fileMenu.add(saveDocAs);
	    fileMenu.addSeparator();
	    fileMenu.add(closeEditor);
	    
	    menuBar.add(fileMenu);
	    
	    JMenu editMenu = new JMenu("Edit");
	    
	    JMenuItem undo = new JMenuItem("Undo");
	    undo.setActionCommand("Undo");
	    undo.setAction(this.undoAction);
	    undo.setAccelerator(KeyStroke.getKeyStroke('Z',InputEvent.CTRL_DOWN_MASK));
	    JMenuItem redo = new JMenuItem("Redo");
	    redo.setActionCommand("redo");
	    redo.setAction(this.redoAction);
	    redo.setAccelerator(KeyStroke.getKeyStroke('Y',InputEvent.CTRL_DOWN_MASK));
	    JMenuItem cut = new JMenuItem("Cut");
	    cut.setActionCommand("Cut");
	    cut.setAction(findAction(DefaultEditorKit.cutAction));
	    cut.setAccelerator(KeyStroke.getKeyStroke('X',InputEvent.CTRL_DOWN_MASK));
	    JMenuItem copy = new JMenuItem("Copy");
	    copy.setActionCommand("Copy");
	    copy.setAction(findAction(DefaultEditorKit.copyAction));
	    copy.setAccelerator(KeyStroke.getKeyStroke('C',InputEvent.CTRL_DOWN_MASK));
	    JMenuItem paste = new JMenuItem("Paste");
	    paste.setActionCommand("Paste");
	    paste.setAction(findAction(DefaultEditorKit.pasteAction));
	    paste.setAccelerator(KeyStroke.getKeyStroke('V',InputEvent.CTRL_DOWN_MASK));
	    
	    editMenu.add(undo);
	    editMenu.add(redo);
	    editMenu.addSeparator();
	    editMenu.add(cut);
	    editMenu.add(copy);
	    editMenu.add(paste);
	    
	    menuBar.add(editMenu);
	    frame.setJMenuBar(menuBar);
	    frame.setTitle(title);
	    frame.setBackground(Color.lightGray);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    frame.addWindowListener(new AppCloser());
	    frame.getContentPane().add("Center", this);
		frame.pack();
		frame.setSize(windowWidth, windowHeight);
		frame.setLocationRelativeTo(MainFrame.getInstance());
	    frame.setVisible(true);
	    
    }

    /**
     * Setzt die aktuelle Titelleiste auf title.
     * @param title
     */
    public void setFrameTitle(String title)
    {
    	this.title = title;
    	frame.setTitle(title);
    }
    
    
    /**
     * Gibt die aktuelle Titelleiste zurück.
     * @return
     */
    public String getFrameTitle()
    {
    	return title;
    }
    
    /**
     * Der Editor kann den Titel je nach Dokument öffnen oder den Titel behalten der mit setFrameTitle() übergeben wird
     * true (Standard) - Dokumentname wird zum Titel
     * false - festgelegter Name bleibt bestehen
     * @param changing setzt Verhalten fest
     */
    public void setTitleChanging(boolean changing)
    {
    	titleChanging = changing;
    }
    
    /**
     * true, wenn der Editor den Dokumentnamen als Title einsetzt 
     * false, wenn er den eingestellten Title behält
     */
    public boolean isTitleChanging()
    {
    	return titleChanging;
    }
    
    
    /**
     * Setzt den FileFilter der beim Öffnen und Speichern von Dokumenten angewandt wird.
     * @param ff
     */
    public void setFileFilter(FileFilter ff)
    {
    	fileFilter = ff;
    }
    
    /**
     * Gibt den gesetzten FileFilter zurück oder null wenn keiner vorhanden ist.
     * @return
     */
    public FileFilter getFileFilter()
    {
    	return fileFilter;
    }
    
    /**
     * Setzt den Text im Editor. Bei Aufruf wird der bestehende Text entfernt. Zum Anhängen von Text: setEditorText(getEditorText().concat(text));
     * @param text
     */
    public void setEditorText(String text)
    {
    	editor.setText(text);
    }
    
    
    /**
     * Gibt den gesamten Text im Editor zurück.
     */
    public String getEditorText()
    {
    	return editor.getText();
    }
    
    /** 
     * Fetch the editor contained in this panel
     */
    protected JTextComponent getEditor() {
	return editor;
    }


    /**
     * Find the hosting frame, for the file-chooser dialog.
     */
    protected Frame getFrame() {
	for (Container p = getParent(); p != null; p = p.getParent()) {
	    if (p instanceof Frame) {
		return (Frame) p;
	    }
	}
	return null;
    }
    
    /**
     * Fügt ein bereits existierendes Menü in der Menüleiste ein.
     * @param menu
     */
    public void addMenu(JMenu menu)
    {
    	menuBar.add(menu);
    }
    
    /**
     * 
     * @param menuPath gibt den Menüpfad bis zum menuItem fest. Trennzeichen: '.' Bsp.: 'Ansicht.Symbolleisten'
     * @param menuItem das neue menuItem an der Stelle des Pfades. Bsp.: 'Menüleiste'
     * @param action Action die ausgeführt wird.
     */
    public void addMenuEntry(String menuPath, String menuItem, Action action)
    {
    	String[] path = menuPath.split("\\.");
    	JMenu menu = null;
    	boolean found = false;
    	for(int i = 0; i < menuBar.getMenuCount(); i++)
    	{
    		if(((JMenuItem)menuBar.getSubElements()[i]).getText().equals(path[0]))
			{
    			menu = ((JMenu)menuBar.getSubElements()[i]);
    			found = true;
    			break;
			}
    	}
    	if(!found)
    	{
    		menu = new JMenu(path[0]);
    		menuBar.add(menu);
    	}
    	
    	for(int i = 1; i < path.length; i++)
    	{
    		found = false;
    		for(int j = 0; j < menu.getItemCount(); j++)
    		{
    			if(menu.getItem(j).getText().equals(path[i])&& menu.getItem(j) instanceof JMenu)
    			{
    				
    				menu = (JMenu)menu.getItem(j);
    				found = true;
    				break;
    			}
    		}
    		if(!found)
    		{
    			JMenu m = new JMenu(path[i]);
    			menu.add(m);
    			menu = m;
    		}
    	}
    	JMenuItem item = new JMenuItem();
    	item.setAction(action);
    	item.setText(menuItem);
    	menu.add(item);
    }
    
    /**
     * Gibt die Menüleiste zurück
     * @return
     */
    public JMenuBar getMenuBar()
    {
    	return menuBar;
    }
    
    /**
     * Entfernt das durch mi spezifizierte MenuItem.
     * @param mi
     */
    public void removeMenuItem(JMenuItem mi)
    {
    	for(int i =0; i < menuBar.getMenuCount(); i++)
    		if(menuBar.getMenu(i).equals(mi))
    			menuBar.remove(mi);
    		else
    			remMenIt(menuBar.getMenu(i), mi);
    }
    
    /**
     * Rekursive Hilfsfunktion für removeMenuItem()
     * @param menu
     * @param mi
     * @return
     */
    private boolean remMenIt(JMenuItem menu, JMenuItem mi)
    {	
    	for(int i=0; i < ((JMenu)menu).getMenuComponentCount(); i++)
    		if(((JMenu)menu).getMenuComponent(i).equals(mi))
    		{
    			menu.remove(i);
    			return true;
    		}
    		else if(((JMenu)menu).getMenuComponent(i) instanceof JMenu)
    			if(remMenIt((JMenuItem)((JMenu)menu).getMenuComponent(i), mi))
    				return true;
    	return false;
    }
    
    /**
     * Durchsucht die gesamte Menüstruktur nach Einträgen mit dem Label label.
     * @param label
     * @return
     */
    public ArrayList<JMenuItem> getMenuItems(String label)
    {
    	ArrayList<JMenuItem> ret = new ArrayList<JMenuItem>();
    	for(int i = 0; i < menuBar.getMenuCount(); i++)
    	{
    		ret = getMenIt(label, menuBar.getMenu(i), ret);
    		if(menuBar.getMenu(i).getText().equals(label))
    			ret.add(menuBar.getMenu(i));
    	}
    	
    	return ret;
    }
    
    /**
     * Rekursive Hilfsfunktion für getMenuItems
     * @param label
     * @param item
     * @param ret
     * @return
     */
    private ArrayList<JMenuItem> getMenIt(String label, JMenuItem item, ArrayList<JMenuItem> ret)
    {
    	for(int i = 0; i < ((JMenu)item).getMenuComponentCount(); i++)
    	{
    		if(!(((JMenu)item).getMenuComponent(i) instanceof JSeparator))
    		{
    		if(((JMenu)item).getMenuComponent(i) instanceof JMenu)
    			ret = getMenIt(label, (JMenuItem)((JMenu)item).getMenuComponent(i), ret);
    		if(((JMenuItem)((JMenu)item).getMenuComponent(i)).getText().equals(label))
    			ret.add((JMenuItem)((JMenu)item).getMenuComponent(i));
    		}
    	}
    	return ret;
    }

    private Action findAction(String key) {
    	Hashtable<Object,Action> commands = new Hashtable<Object,Action>();
    	for (Action action : editor.getActions()) {
    		commands.put(action.getValue(Action.NAME), action);
    	}
    	return (Action) commands.get(key);
    }

    /**
     * Resets the undo manager.
     */
    protected void resetUndoManager() {
	undo.discardAllEdits();
	undoAction.update();
	redoAction.update();
    }
    


    class UndoHandler implements UndoableEditListener {

	/**
	 * Messaged when the Document has created an edit, the edit is
	 * added to <code>undo</code>, an instance of UndoManager.
	 */
        public void undoableEditHappened(UndoableEditEvent e) {
	    undo.addEdit(e.getEdit());
	    undoAction.update();
	    redoAction.update();
	    saved = false;
	}
    }


    // --- action implementations -----------------------------------

    private UndoAction undoAction = new UndoAction();
    private RedoAction redoAction = new RedoAction();

    /**
     * Actions defined by the TextEditor class
     */

    class UndoAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public UndoAction() {
		    super("Undo");
		    setEnabled(false);
		}
	
		public void actionPerformed(ActionEvent e) {
		    try {
			undo.undo();
		    } catch (CannotUndoException ex) {
			System.out.println("Unable to undo: " + ex);
			ex.printStackTrace();
		    }
		    update();
		    redoAction.update();
		}
	
		protected void update() {
		    if(undo.canUndo()) {
			setEnabled(true);
			putValue(Action.NAME, undo.getUndoPresentationName());
		    }
		    else {
			setEnabled(false);
			putValue(Action.NAME, "Undo");
		    }
		}
    }

    class RedoAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public RedoAction() {
		    super("Redo");
		    setEnabled(false);
		}
	
		public void actionPerformed(ActionEvent e) {
		    try {
			undo.redo();
		    } catch (CannotRedoException ex) {
			System.out.println("Unable to redo: " + ex);
			ex.printStackTrace();
		    }
		    update();
		    undoAction.update();
		}
	
		protected void update() {
		    if(undo.canRedo()) {
			setEnabled(true);
			putValue(Action.NAME, undo.getRedoPresentationName());
		    }
		    else {
			setEnabled(false);
			putValue(Action.NAME, "Redo");
		    }
		}
    }

    class OpenAction extends NewAction {
		private static final long serialVersionUID = 1L;

		OpenAction() {
		    super(openAction);
		}

        public void actionPerformed(ActionEvent e) {
		    Frame frame = getFrame();
	            JFileChooser chooser = new JFileChooser();
	            chooser.setFileFilter(fileFilter);
	            int ret = chooser.showOpenDialog(frame);
	
	            if (ret != JFileChooser.APPROVE_OPTION) {
			return;
		    }
	
	            File f = chooser.getSelectedFile();
		    if (f.isFile() && f.canRead()) {
			Document oldDoc = getEditor().getDocument();
			if(oldDoc != null)
			    oldDoc.removeUndoableEditListener(undoHandler);
			getEditor().setDocument(new PlainDocument());
			if(titleChanging)
				frame.setTitle(f.getName());
			
			Thread loader = new FileLoader(f, editor.getDocument());
			loader.start();
		    } else {
	                JOptionPane.showMessageDialog(getFrame(),
	                        "Could not open file: " + f,
	                        "Error opening file",
	                        JOptionPane.ERROR_MESSAGE);
		    }
		    
		    saved = true;
		}
    }
    
    class SaveAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		SaveAction() {
		    super(saveAction);
		}
	
	        public void actionPerformed(ActionEvent e) {
	        	if(!saved)
	        	{
		        	if(file != null)
		        	{
			            Thread saver = new FileSaver(file, editor.getDocument());
		        		saver.start();
		        		saved = true;
		        	}
		        	else
		        		saveDocAs.doClick();
	        	}
		}
    }
    
    class SaveAsAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		SaveAsAction() {
		    super(saveAsAction);
		}
	
	        public void actionPerformed(ActionEvent e) {
	            Frame frame = getFrame();
	            JFileChooser chooser = new JFileChooser();
	            chooser.setFileFilter(fileFilter);
	            int ret = chooser.showSaveDialog(frame);
	
	            if (ret != JFileChooser.APPROVE_OPTION) {
	                return;
	            }
	
	            File f = chooser.getSelectedFile();
	            if(titleChanging)
	            	frame.setTitle(f.getName());
	            Thread saver = new FileSaver(f, editor.getDocument());
	            saver.start();
	            saved = true;
		}
    	
    }
    
    
    class NewAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		NewAction() {
		    super(newAction);
		}
	
		NewAction(String nm) {
		    super(nm);
		}
	
	    public void actionPerformed(ActionEvent e) {
		    Document oldDoc = getEditor().getDocument();
		    if(oldDoc != null)
			oldDoc.removeUndoableEditListener(undoHandler);
		    getEditor().setDocument(new PlainDocument());
		    getEditor().getDocument().addUndoableEditListener(undoHandler);
		    resetUndoManager();
	        getFrame().setTitle(title);
	        file = null;
	        saved = true;
		    revalidate();
		}
    }

    /**
     * Thread to load a file into the text storage model
     */
    class FileLoader extends Thread {
		Document doc;
		File f;

		FileLoader(File f, Document doc) {
		    setPriority(4);
		    this.f = f;
		    this.doc = doc;
		}
	
	        public void run() {
		    try {
	
			// try to start reading
			Reader in = new FileReader(f);
			char[] buff = new char[4096];
			int nch;
			while ((nch = in.read(buff, 0, buff.length)) != -1) {
			    doc.insertString(doc.getLength(), new String(buff, 0, nch), null);
			}
		    }
		    catch (IOException e) {
	                final String msg = e.getMessage();
	                SwingUtilities.invokeLater(new Runnable() {
	                    public void run() {
	                        JOptionPane.showMessageDialog(getFrame(),
	                                "Could not open file: " + msg,
	                                "Error opening file",
	                                JOptionPane.ERROR_MESSAGE);
		    }
	                });
	            }
		    catch (BadLocationException e) {
			System.err.println(e.getMessage());
		    }
            doc.addUndoableEditListener(undoHandler);
	
            resetUndoManager();
            file = f;
		}
	
    }

    /**
     * Thread to save a document to file
     */
    class FileSaver extends Thread {
        Document doc;
        File f;

		FileSaver(File f, Document doc) {
		    setPriority(4);
		    this.f = f;
		    this.doc = doc;
		}
	
	        public void run() {
	        	try {
	
	        		// 	start writing
	        		Writer out;
	        		if(f.getAbsolutePath().toLowerCase().endsWith(".r"))
	        			out = new FileWriter(f);
	        		else
	        			out = new FileWriter(f+".R"); 
	                Segment text = new Segment();
	                text.setPartialReturn(true);
	                int charsLeft = doc.getLength();
	                int offset = 0;
	                while (charsLeft > 0) {
	                    doc.getText(offset, Math.min(4096, charsLeft), text);
	                    out.write(text.array, text.offset, text.count);
	                    charsLeft -= text.count;
	                    offset += text.count;
	                    try {
	                        Thread.sleep(10);
	                    } catch (InterruptedException e) {
	                        e.printStackTrace();
	                    }
	                }
	                out.flush();
	                out.close();
		    }
		    catch (IOException e) {
	                final String msg = e.getMessage();
	                SwingUtilities.invokeLater(new Runnable() {
	                    public void run() {
	                        JOptionPane.showMessageDialog(getFrame(),
	                                "Could not save file: " + msg,
	                                "Error saving file",
	                                JOptionPane.ERROR_MESSAGE);
		    }
	                });
		    }
		    catch (BadLocationException e) {
			System.err.println(e.getMessage());
		    }
	        file = f;
		}
    }
    
    protected final class AppCloser extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
        	if(saved)
        	{
        		((JFrame)e.getSource()).dispose();
        	}
        	else
        	{
        		String dialogText;
        		if(file!= null)
        			dialogText = "Save file '"+file+"'?";
        		else
        			dialogText = "Save in file?";
        		switch(JOptionPane.showConfirmDialog(frame, dialogText, "Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE))
   				{
   					case JOptionPane.YES_OPTION:
   						saveDoc.doClick();
   						break;
   					case JOptionPane.NO_OPTION:
   		        		((JFrame)e.getSource()).setVisible(false);
   						break;
   					case JOptionPane.CANCEL_OPTION:
   						break;
   				}
        	}
        }
    }
    
}
