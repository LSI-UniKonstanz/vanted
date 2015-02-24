package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.r;

import java.util.ArrayList;


/**
 * Eine leider vollkommen unn�tige Klasse.
 * @author Torsten
 * Aufgrund von anderweiten Planungen, war erst angedacht die Variablen auch nach dem Anlegen noch im Typ wechseln zu k�nnen.
 * Da die vorher eingegebenen Daten nicht verschwinden sollten, wurde pro Variable f�r jeden Typ ein Objekt angelegt.
 * Inzwischen w�rde eins f�r alle reichen, aber daf�r reicht die Zeit die ich noch habe leider nicht aus...
 */
public class RDataObject {
	
	public ArrayList<Object> vectorObject;
	public ArrayList<ArrayList<Object>> listObject;
	public ArrayList<Object> listHeader;
	public ArrayList<ArrayList<Object>> matrixObject;
	public ArrayList<Object> arrayObject;
	public ArrayList<Integer> arrayIndices;
	public ArrayList<Integer> currArrayIndices;
	public ArrayList<ArrayList<Object>> dataframeObject;
	public ArrayList<Object> dataframeColumnHeader;
	public ArrayList<Object> dataframeRowHeader;

	public RDataObject()
	{
		vectorObject = new ArrayList<Object>();
		listObject = new ArrayList<ArrayList<Object>>();
		listHeader = new ArrayList<Object>();
		matrixObject = new ArrayList<ArrayList<Object>>();
		arrayObject = new ArrayList<Object>();
		arrayIndices = new ArrayList<Integer>();
		currArrayIndices = new ArrayList<Integer>();
		dataframeObject = new ArrayList<ArrayList<Object>>();
		dataframeColumnHeader = new ArrayList<Object>();
		dataframeRowHeader = new ArrayList<Object>();
	}
	
}
