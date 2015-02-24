package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.r;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.AbstractAction;

import org.ReleaseInfo;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.TextEditor.TextEditor;

public class ScriptEditor extends TextEditor{
	
	private static final long serialVersionUID = 1L;

	static TabRControl rTab; //aurufendes Tab
	
	//entspricht dem Button new script
	ScriptEditor(TabRControl tab)
	{
		super();
		setRConfig();
		setEditorText(newRScriptText());
		rTab = tab;
	}
	
	//entspricht dem Button load script
	ScriptEditor(File f)
	{
		super();
		setRConfig();
		if(f.isFile())
		{
			String text = new String();
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				while(true)
				{
					String input = in.readLine();
					if(input == null)
						break;
					text += input + "\n";
				}
				if(text.length()>0)
					text = text.substring(0, text.length()-1);
			} catch (Exception e) {
				e.printStackTrace();
			}
			setEditorText(text);
			file = f;
		}
		
	}
	
	/**
	 * F�gt dem Men� noch ein Untermen� "R" hinzu und diesem den Punkt "load script in VANTED"
	 * Daf�r wird im AppFolder von VANTED eine tempor�re Datei angelegt und sofort nach dem Einlesen wieder gel�scht
	 */
	private void setRConfig()
	{
		setFrameTitle("R Script Editor");
		setFileFilter(new RFileFilter());
		addMenuEntry("R", "load script in VANTED", new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				File tmpFile = new File(ReleaseInfo.getAppFolderWithFinalSep() + "tmp.R");
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(tmpFile));
					out.write(getEditorText());
					out.flush();
					out.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				tmpFile = new File(ReleaseInfo.getAppFolderWithFinalSep() + "tmp.R");
				System.out.println(tmpFile);
				if(tmpFile.exists())
				{
					rTab.loadScript(tmpFile);
					System.out.println(tmpFile.delete());
				}
			}});
	}
	
	private String newRScriptText()
	{
		String text =
			 "## build with the VANTED R script editor\n"
			+"## ---------------------------------------------------------\n"
			+"## To use this Vanted add on as comfortable as possible,\n"
			+"## please comment your variables as followed:\n"
			+"##\n"
			+"## #variable [name] [type] \"[description]\"\n"
			+"##\n"
			+"## type = {vector, list, factor, matrix, array, data.frame}\n"
			+"## Please use a new line for each variable.\n"
			+"## One variable can not be defined twice.\n"
			+"##\n"
			+"## For example:\n"
			+"## #variable a vector \"simple numerical vector\"\n"
			+"## #variable b array \"array to store something\"\n"
			+"##\n"
			+"## This helps Vanted to predefine your variables, so you\n"
			+"## do not need to define them all by hand.\n"
			+"## Vanted sets the variables in R before the script starts.\n"
			+"## ----------------------------------------------------------\n\n\n"
			+"";

//        TODO: Output-Variablen		
//			 "## build with the VANTED R script editor\n"
//			+"## ---------------------------------------------------------\n"
//			+"## To use this Vanted add on as comfortable as possible,\n"
//			+"## please comment your variables as followed:\n"
//			+"##\n"
//			+"## #variable [name] [type] [input/output] \"[description]\"\n"
//			+"##\n"
//			+"## type = {vector, list, factor, matrix, array, data.frame}\n"
//			+"## input/output = {input, output}\n"
//			+"## Please use a new line for each variable.\n"
//			+"## One variable can be used for in- and output, but can not\n"
//			+"## be defined twice for one direction.\n"
//			+"##\n"
//			+"## For example:\n"
//			+"## #variable a vector input \"simple numerical vector\"\n"
//			+"## #variable b array input \"array to store something\"\n"
//			+"## #variable c vector output \"simple output vector\"\n"
//			+"##\n"
//			+"## This helps Vanted to predefine your variables, so you\n"
//			+"## do not need to define them all by hand.\n"
//			+"## Vanted sets the input variables before the script starts\n"
//			+"## and reads the output variables after it finished.\n"
//			+"## ----------------------------------------------------------\n\n\n"
//			+"";
		
		return text;
	}
	
}
