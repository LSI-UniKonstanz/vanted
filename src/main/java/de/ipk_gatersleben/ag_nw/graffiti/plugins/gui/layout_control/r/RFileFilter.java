package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.r;

import java.io.File;

import javax.swing.filechooser.FileFilter;


/**
 * FileFilter der ausschlie�lich Dateien mit der Endung .r bzw. .R zul�sst
 * @author Torsten
 * Ben�tigt f�r den Dateimanager zum �ffnen von R Skripten
 */
public class RFileFilter extends FileFilter{

    public boolean accept(File f) {
        return f.isDirectory()
                || f.getName().toLowerCase().endsWith(".r");
    }

   	public String getDescription() {
   		return "R scripts \".R\"";
   	}

}
