package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.r;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import org.graffiti.editor.MainFrame;

/**
 * Zeigt den Dialog zur Variablenbelegung an
 * @author Torsten
 *
 */
public class RVariableSelectionDialog extends JDialog{
	private static final long serialVersionUID = 1L;
	private RVariable var;
	private boolean isSet;//Wenn Variable mit OK eingegeben wird TRUE, bei Abbruch FALSE (ist sie bereits eingegeben und der Dialog ein weiteres mal
						//ge�ffnet, dann trotzdem TRUE)
	
	JTable dataTable;//Tabelle mit den Daten des Graphen
	JScrollPane dataPanel;//Scrollpane die dataTable enth�lt
	Object[][] dataset;//Daten des Graphen
	String[][] tooltips;//Tooltips die in dataTable angezeigt werden
	String[] columnHeader;//columnHeader der dataTable
	JComboBox viewChooser;//DropBox in dataTable oben links, zur Auswahl der RowHeader
	DataRowHeaderListModel dataRowHeaderListModel;//ListModel zur Darstellung der RowHeader der dataTable
	JList dataRowHeader;//Liste die die RowHeader der dataTable enth�lt
	boolean dataMousePressed;//Kontrollvariable zur Unterst�tzung der MouseListener der dataTable
	int dataMouseRow, dataMouseColumn;//Speichern die Mouseposition von MousePressed Ereignissen der dataTable
	
	RDataObject rDataObject;//Klasse zur Speicherung der ausgew�hlten Daten
	int variableMode; // 0: off(gibt es nicht mehr), 1: vector, 2: list, 3: matrix, 4: array, 5: data.frame
	
	JPanel infoPanel;//die gesamte rechte Seite des Dialogs
	JTable infoTable;//die Tabelle die die ausgew�hlten Daten darstellt
	InfoRowHeaderListModel infoRowHeaderListModel;//ListModel zur Darstellung von RowHeadern bei data.frames
	JList infoRowHeader;//Liste die die RowHeader der data.frames enth�lt
	int x1,x2,y1,y2;//Speichern die Mauspositionen von MousePressed und MouseReleased Events auf der infoTable
	JRadioButton infoInsertRB, infoOverwriteRB;//Radiobuttons zur Auwahl, ob Daten einge�fgt oder �berschrieben werden sollen
	JLabel infoSizeLabel, infoIndexLabel;//Label zur Anzeige des Ausma�es der aktuellen Variable, bzw.zur Anzeige der zur Zeit gew�hlten Indices
	boolean infoMousePressed;//Kontrollvariable zur Unterst�tzung der MouseListener der infoTable
	JScrollPane infoScroll;//Scrollpane die infoTable enth�lt
	
	boolean edited;
	
	JPanel arrayDimPanel;	
	
	public RVariableSelectionDialog(RVariable var, Object[][] dataset, String[] columnHeader, String[][] tooltips)
	{
		super(MainFrame.getInstance(), "Configuration of variable "+var.getName(), true);		
		
		this.var = var;
		this.dataset = dataset;
		this.columnHeader = columnHeader;
		this.tooltips = tooltips;
		isSet = false;
		dataTable = new JTable();
		x1 = 0; x2 = 0; y1 = 0; y2 = 0;
		
		if(var.getData() != null)
			rDataObject = var.getData();
		else
			rDataObject = new RDataObject();
		
		dataMousePressed = false;
		infoMousePressed = false;

		if(var.getType().equals("vector")) variableMode = 1;
		else if(var.getType().equals("list")) variableMode = 2;
		else if(var.getType().equals("matrix")) variableMode = 3;
		else if(var.getType().equals("array")) variableMode = 4;
		else if(var.getType().equals("data.frame")) variableMode = 5;		
		
		setLayout();
		
		
		setSize((int)(MainFrame.getInstance().getSize().width*0.9), (int)(MainFrame.getInstance().getSize().height*0.9));
		setLocationRelativeTo(MainFrame.getInstance());
		setVisible(true);
	}
	
	/**
	 * Funktion, die den Dialog aufbaut
	 */
	private void setLayout()
	{
		setLayout(new BorderLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		JPanel left = new JPanel(new GridBagLayout());
		JPanel right = new JPanel(new BorderLayout());
		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,left,right);
		sp.setResizeWeight(0.6);
		add(sp, BorderLayout.CENTER);

		
		JLabel label = new JLabel(" Type: ");
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		left.add(label, gbc);
		
		JLabel cb = new JLabel(var.getType());
		gbc.gridx = 1;
		left.add(cb,gbc);
		
		JSeparator sep2 = new JSeparator(JSeparator.VERTICAL);
		sep2.setPreferredSize(new Dimension(5,10));
		gbc.gridx = 2;
		gbc.insets = new Insets(0,5,0,5);
		left.add(sep2,gbc);
		
		label = new JLabel(" Description: ");
		gbc.gridx = 3;
		gbc.insets = new Insets(0,0,0,0);
		left.add(label,gbc);
		
		JLabel descrLabel = new JLabel("<html>"+var.getDescription());
		descrLabel.setToolTipText(var.getDescription());
		gbc.gridx = 4;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		left.add(descrLabel,gbc);

		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
		sep.setPreferredSize(new Dimension(300,5));
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 5;
		gbc.gridheight = 1;
		gbc.insets = new Insets(5,5,5,5);
		gbc.weighty = 0;
		left.add(sep,gbc);
				
		setDataPanel();
		
		gbc.gridy = 2;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.insets = new Insets(0,0,0,0);
		gbc.fill = GridBagConstraints.BOTH;
		left.add(dataPanel, gbc);
		
		
		setInfoPanel();
		
		right.add(infoPanel, BorderLayout.CENTER);
		
		JPanel buttons = new JPanel(new FlowLayout());
		
		JButton okButton = new JButton();
		okButton.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent arg0) {
				try
				{
					infoTable.getCellEditor().stopCellEditing();
				}
				catch(NullPointerException e)
				{}
				isSet = true;
				dispose();
			}});
		okButton.setText("OK");
		
		JButton cancButton = new JButton();
		cancButton.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent arg0) {
				dataset = null;
				var = null;
				dispose();
			}});
		cancButton.setText("Cancel");
		
		buttons.add(okButton);
		buttons.add(cancButton);
		add(buttons,BorderLayout.SOUTH);
		
	}
	
	/**
	 * Kann aufgerufen werden, um zu pr�fen, ob die Variable eingegeben wurde oder nicht
	 * @return: TRUE, wenn Variable gesetzt wurde
	 */
	public boolean isSet(){
		return isSet;
	}
	
	/**
	 * Gibt die RVariable zur�ck
	 */
	public RVariable getRVariable()
	{
		return var;
	}
	
	/**
	 * Gibt die eingegebenen Daten der RVariable zur�ck
	 * @return
	 */
	public RDataObject getData()
	{
		return rDataObject;
	}
	
	/**
	 * Funktion zum Darstellen der Tabelle mit den Daten des Graphen
	 */
	private void setDataPanel()
	{
		dataPanel = new JScrollPane();
		
		viewChooser = new JComboBox(new String[]{"plant","time","replicate"});
		viewChooser.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				dataRowHeader.repaint();
			}});
		
		dataPanel.setCorner(JScrollPane.UPPER_LEFT_CORNER, viewChooser);

		dataRowHeaderListModel = new DataRowHeaderListModel();
		dataRowHeader = new JList(dataRowHeaderListModel);
		dataRowHeader.setFixedCellWidth(55);
		dataRowHeader.setFixedCellHeight(dataTable.getRowHeight());
		dataRowHeader.setCellRenderer(new RowHeaderRenderer(dataTable));
		dataPanel.setRowHeaderView(dataRowHeader);
		dataTable = new JTable(new DataTableModel());
		dataTable.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e)
			{
				dataMousePressed = true;
				Point p = dataTable.getMousePosition();
				dataMouseColumn = dataTable.columnAtPoint(p);
				dataMouseRow = dataTable.rowAtPoint(p);
			}
			public void mouseReleased(MouseEvent e)
			{
				Point p = dataTable.getMousePosition();
				storeData(dataMouseRow,dataMouseColumn,dataTable.rowAtPoint(p),dataTable.columnAtPoint(p));
				dataMousePressed = false;
				dataTable.repaint();
				if(variableMode!=4)
					infoSizeLabel.repaint();
				infoTable.revalidate();
			}
		});
		dataTable.setDefaultRenderer(Object.class, new DataTableCellRenderer());
		dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		dataPanel.setViewportView(dataTable);
	}
	
	/**
	 * Nach der Auswahl von Daten in der dataTable wird diese Funktion aufgerufen und die Daten in die infoTable �bertragen
	 * @param r1: row mousepressed
	 * @param c1: column mousepressed
	 * @param r2: row mousereleased
	 * @param c2: column mousereleased
	 */
	private void storeData(int r1, int c1, int r2, int c2)
	{
				
		switch(variableMode)//je nach Variablentyp wird ein anderes Verfahren angewandt
		{
			case 1://vector
				boolean horizontal = Math.abs(c2-c1) > Math.abs(r2-r1);
				
				ArrayList<Object> vectorData = new ArrayList<Object>();
				if(horizontal)
					for(int i = Math.min(c1,c2); i <= Math.max(c1, c2); i++)
						vectorData.add(dataTable.getModel().getValueAt(r1,i));
				else
					for(int i = Math.min(r1,r2); i <= Math.max(r1, r2); i++)
						vectorData.add(dataTable.getModel().getValueAt(i,c1));
				
				if(infoInsertRB.isSelected())
				{
					int count = 0;
					for(Object o : vectorData)
					{
						rDataObject.vectorObject.add(Math.min(y1, y2)+count, o);
						count++;
					}
				}
				else
				{
					int count = 0;
					for(Object o : vectorData)
					{
						if(Math.min(y1, y2)+count >= rDataObject.vectorObject.size())
							rDataObject.vectorObject.add(o);
						else
							rDataObject.vectorObject.set(Math.min(y1, y2)+count, o);
						count++;
					}
				}
				infoSizeLabel.setText("size: "+rDataObject.vectorObject.size());
					y1 = Math.min(y1, y2) + vectorData.size();
					y2 = y1;
				infoIndexLabel.setText("selected index: "+(y1+1)+" - "+(y1+1));
				infoPanel.revalidate();
				break;
			case 2://list
				boolean horizontal1 = Math.abs(c2-c1) > Math.abs(r2-r1);
				ArrayList<Object> listData = new ArrayList<Object>();
				if(horizontal1)
					for(int i = Math.min(c1,c2); i <= Math.max(c1, c2); i++)
						listData.add(dataTable.getModel().getValueAt(r1,i));
				else
					for(int i = Math.min(r1,r2); i <= Math.max(r1, r2); i++)
						listData.add(dataTable.getModel().getValueAt(i,c1));
				
				if(x1 == rDataObject.listObject.size())
				{
					rDataObject.listHeader.add(null);
					rDataObject.listObject.add(new ArrayList<Object>());
					for(Object o : listData)
						rDataObject.listObject.get(x1).add(o);
				}
				else if(infoInsertRB.isSelected())
				{
					if(Math.min(y1, y2) >= rDataObject.listObject.get(x1).size())
					{
						rDataObject.listObject.get(x1).addAll(listData);
					}
					else
					{
						int count = 0;
						for(Object o : listData)
						{
							rDataObject.listObject.get(x1).add(Math.min(y1, y2)+count, o);
							count++;
						}
					}
				}
				else
				{
					int count = 0;
					for(Object o : listData)
					{
						if(Math.min(y1, y2)+count >= rDataObject.listObject.size())
							rDataObject.listObject.get(x1).add(o);
						else
							rDataObject.listObject.get(x1).set(Math.min(y1, y2)+count, o);
						count++;
					}
				}
				infoSizeLabel.setText("size: "+rDataObject.listObject.size());
					y1 = Math.min(y1, y2) + listData.size();
					if(y1 > rDataObject.listObject.get(x1).size())
						y1 = rDataObject.listObject.get(x1).size();
					y2 = y1;
				infoIndexLabel.setText("selected index: "+(y1+1)+" - "+(y1+1));
				((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
				infoPanel.revalidate();
				break;
			case 3://Matrix
				Object[][] data = new Object[Math.abs(r1-r2)+1][Math.abs(c1-c2)+1];
				int iCount = 0;
				for(int i = Math.min(r1, r2); i <= Math.max(r1, r2); i++)
				{
					int jCount =0;
					for(int j = Math.min(c1, c2); j <= Math.max(c1, c2); j++)
					{
						data[iCount][jCount] = dataTable.getModel().getValueAt(i, j);
						jCount++;
					}
					iCount++;
				}
				
				if(infoInsertRB.isSelected() && rDataObject.matrixObject.size() > 0)
				{
					int newDataWidth = Math.abs(c1-c2)+1;
					int newDataHeight = Math.abs(r1-r2)+1;
					int posx = Math.min(x1, x2);
					int posy = Math.min(y1, y2);
					
					for(int i = 0; i < data.length; i++)//f�ge neue Zeilen an
					{
						rDataObject.matrixObject.add(new ArrayList<Object>());
						for(int j = 0; j < rDataObject.matrixObject.get(0).size(); j++)
							rDataObject.matrixObject.get(rDataObject.matrixObject.size()-1).add(null);
					}
					
					for(int i = 0; i < rDataObject.matrixObject.size(); i++)//f�ge neue Spalten an
						for(int j = 0; j < data[0].length; j++)
					{
						rDataObject.matrixObject.get(i).add(null);
					}
										
					for(int i = rDataObject.matrixObject.size()-1; i >= posy+newDataHeight; i--)//Daten unterhalb des Cursors nach unten verschieben
					{
						for(int j = 0; j < rDataObject.matrixObject.get(0).size(); j++)
						{
							rDataObject.matrixObject.get(i).set(j,rDataObject.matrixObject.get(i-newDataHeight).get(j));
							rDataObject.matrixObject.get(i-newDataHeight).set(j, null);
						}
					}
					
					for(int i = 0; i < rDataObject.matrixObject.size(); i++)//Daten rechts des Cursors nach rechts verschieben
					{
						for(int j = rDataObject.matrixObject.get(0).size()-1; j >= posx+newDataWidth; j--)
						{
							rDataObject.matrixObject.get(i).set(j, rDataObject.matrixObject.get(i).get(j-newDataWidth));
							rDataObject.matrixObject.get(i).set(j-newDataWidth, null);
						}
					}
										
					for(int i = 0; i < data.length; i++)//neue Daten eintragen
					{
						for(int j = 0; j < data[0].length; j++)
						{
								rDataObject.matrixObject.get(i+posy).set(j+posx,data[i][j]);
						}
					}					
					
				}
				else
				{
					for(int i = 0; i < data.length; i++)
					{
						if(i + Math.min(y1, y2) >= rDataObject.matrixObject.size())
						{
							rDataObject.matrixObject.add(new ArrayList<Object>());
							for(int j = 0; j < Math.min(x1, x2); j++)
								rDataObject.matrixObject.get(rDataObject.matrixObject.size()-1).add(null);
						}
						for(int j = 0; j < data[0].length; j++)
						{
							if(j+Math.min(x1, x2) >= rDataObject.matrixObject.get(i+Math.min(y1, y2)).size())
								rDataObject.matrixObject.get(i+Math.min(y1, y2)).add(data[i][j]);
							else
								rDataObject.matrixObject.get(i+Math.min(y1, y2)).set(j+Math.min(x1, x2), data[i][j]);
						}
					}
				}
				infoSizeLabel.setText("size: "+rDataObject.matrixObject.size()+"x"+rDataObject.matrixObject.get(0).size());
				((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
				infoPanel.revalidate();
				break;
			case 4://array
				Object[][] arrayData = new Object[Math.abs(r1-r2)+1][Math.abs(c1-c2)+1];
				int iCounter = 0;
				for(int i = Math.min(r1, r2); i <= Math.max(r1, r2); i++)
				{
					int jCounter =0;
					for(int j = Math.min(c1, c2); j <= Math.max(c1, c2); j++)
					{
						arrayData[iCounter][jCounter] = dataTable.getModel().getValueAt(i, j);
						jCounter++;
					}
					iCounter++;
				}
	
				int arrayBigIndex = 0;
				for(int i = 2; i < rDataObject.currArrayIndices.size(); i++)
				{
					int sizes = 1;
					for(int j = 0; j < i; j++)
						sizes *= rDataObject.arrayIndices.get(j);
					arrayBigIndex += sizes * rDataObject.currArrayIndices.get(i);
				}
				int arrayWidth = rDataObject.arrayIndices.get(1);
				int arrayHeight = rDataObject.arrayIndices.get(0);
				int y = Math.min(y1, y2);
				int x = Math.min(x1, x2);
				for(int i = 0; i < arrayData.length && (y+i) < arrayHeight; i++)
				{

					for(int j = 0; j < arrayData[0].length && (x+j) < arrayWidth; j++)
					{
						rDataObject.arrayObject.set(arrayBigIndex+((x+j)*arrayHeight)+(y+i), arrayData[i][j]);
					}
				}
				//Zugriff mit arrayBigIndex + (y * rDataObject.ArrayIndices.get(rDataObject.ArrayIndices.size()-1)) + x
				
				break;
			case 5://datafield
				Object[][] dfdata = new Object[Math.abs(r1-r2)+1][Math.abs(c1-c2)+1];
				int dfiCount = 0;
				for(int i = Math.min(r1, r2); i <= Math.max(r1, r2); i++)
				{
					int jCount =0;
					for(int j = Math.min(c1, c2); j <= Math.max(c1, c2); j++)
					{
						dfdata[dfiCount][jCount] = dataTable.getModel().getValueAt(i, j);
						jCount++;
					}
					dfiCount++;
				}
				
				if(infoInsertRB.isSelected() && rDataObject.dataframeObject.size() > 0)//wenn "insert" ausgew�hlt ist
				{
					int newDataWidth = Math.abs(c1-c2)+1;
					int newDataHeight = Math.abs(r1-r2)+1;
					int posx = Math.min(x1, x2);
					int posy = Math.min(y1, y2);
					
					for(int i = 0; i < dfdata.length; i++)//f�ge neue Zeilen an
					{
						rDataObject.dataframeRowHeader.add(posy+i, null);
						rDataObject.dataframeObject.add(new ArrayList<Object>());
						for(int j = 0; j < rDataObject.dataframeObject.get(0).size(); j++)
						{
							rDataObject.dataframeObject.get(rDataObject.dataframeObject.size()-1).add(null);
						}
					}
					
					for(int i = 0; i < dfdata[0].length; i++)//f�ge neue Spalten an
					{
						rDataObject.dataframeColumnHeader.add(posx+i,null);
						for(int j = 0; j < rDataObject.dataframeObject.size(); j++)
						{
							rDataObject.dataframeObject.get(j).add(null);
						}
					}
					
					for(int i = rDataObject.dataframeObject.size()-1; i >= posy+newDataHeight; i--)//Daten unterhalb des Cursors nach unten verschieben
					{
						for(int j = 0; j < rDataObject.dataframeObject.get(0).size(); j++)
						{
							rDataObject.dataframeObject.get(i).set(j,rDataObject.dataframeObject.get(i-newDataHeight).get(j));
							rDataObject.dataframeObject.get(i-newDataHeight).set(j, null);
						}
					}
					
					for(int i = 0; i < rDataObject.dataframeObject.size(); i++)//Daten rechts des Cursors nach rechts verschieben
					{
						for(int j = rDataObject.dataframeObject.get(0).size()-1; j >= posx+newDataWidth; j--)
						{
							rDataObject.dataframeObject.get(i).set(j, rDataObject.dataframeObject.get(i).get(j-newDataWidth));
							rDataObject.dataframeObject.get(i).set(j-newDataWidth, null);
						}
					}
										
					for(int i = 0; i < dfdata.length; i++)//neue Daten eintragen
					{
						for(int j = 0; j < dfdata[0].length; j++)
						{
								rDataObject.dataframeObject.get(i+posy).set(j+posx,dfdata[i][j]);
						}
					}					
					
				}
				else//wenn overwrite ausgew�hlt ist
				{
					for(int i = 0; i < dfdata.length; i++)
					{
						if(i + Math.min(y1, y2) >= rDataObject.dataframeObject.size())
						{
							rDataObject.dataframeRowHeader.add(null);
							rDataObject.dataframeObject.add(new ArrayList<Object>());
							for(int j = 0; j < rDataObject.dataframeObject.get(0).size(); j++)
								rDataObject.dataframeObject.get(rDataObject.dataframeObject.size()-1).add(null);
						}
						for(int j = 0; j < dfdata[0].length; j++)
						{
							if(j+Math.min(x1, x2) >= rDataObject.dataframeObject.get(i+Math.min(y1, y2)).size())
							{
								rDataObject.dataframeColumnHeader.add(null);
								for(int k = 0; k < rDataObject.dataframeObject.size(); k++)
									rDataObject.dataframeObject.get(k).add(null);
							}
							rDataObject.dataframeObject.get(i+Math.min(y1, y2)).set(j+Math.min(x1, x2), dfdata[i][j]);
						}
					}
				}
				infoSizeLabel.setText("size: "+rDataObject.dataframeObject.size()+"x"+rDataObject.dataframeObject.get(0).size());
				infoRowHeader.revalidate();
				((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
				infoPanel.revalidate();
				break;
			}
		infoPanel.repaint();
		
	}
	
	/**
	 * Funktion zum Aufbau des InfoPanels (rechts Seite des Dialogs)
	 */
	private void setInfoPanel()
	{
		infoPanel = new JPanel(new BorderLayout());
		infoTable = new JTable(new InfoTableModel());
		infoTable.setDefaultRenderer(Object.class, new InfoTableCellRenderer());
		infoTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		infoTable.getTableHeader().setReorderingAllowed(false);
		infoTable.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e)
			{
				Point p = infoTable.getMousePosition();
				x1 = infoTable.columnAtPoint(p);
				y1 = infoTable.rowAtPoint(p);
				infoMousePressed = true;
			}
			public void mouseReleased(MouseEvent e)
			{
				infoMousePressed = false;
				if(infoTable.getMousePosition() != null)
				{
					Point p = infoTable.getMousePosition();
					x2 = infoTable.columnAtPoint(p);
					y2 = infoTable.rowAtPoint(p);
	        		infoPanel.repaint();
				}
				else
				{
					x2 = x1; y2 = y1;
				}
				switch(variableMode)
				{
				case 1:
					infoIndexLabel.setText("selected index: "+(Math.min(y1, y2)+1)+" - "+(Math.max(y1, y2)+1));
					break;
				case 2:
					infoIndexLabel.setText("selected column: "+(Math.min(x1, x2))+" selected index: "+(Math.min(y1, y2)+1)+" - "+(Math.max(y1, y2)+1));
					break;
				case 3:
				case 4:
				case 5:
					infoIndexLabel.setText("selected index: ("+(Math.min(x1,x2)+1)+","+(Math.min(y1, y2)+1)+") - ("+(Math.max(x1, x2)+1)+","+(Math.max(y1, y2)+1)+")");
				}
				infoIndexLabel.revalidate();
			}
		});
		
		infoPanel.removeAll();
		switch(variableMode)
		{
			case 1:
				infoPanel.add(infoPanelVector());
				break;
			case 2:
				infoPanel.add(infoPanelList());
				break;
			case 3:
				infoPanel.add(infoPanelMatrix());
				break;
			case 4:
				infoPanel.add(infoPanelArray());
				break;
			case 5:
				infoPanel.add(infoPanelDataFrame());
				break;
		}
		
		
		infoScroll.setViewportView(infoTable);		

		infoPanel.revalidate();
		infoPanel.repaint();
	}

	/**
	 * Passt das infoPanel entsprechend der Vector Variable an
	 */
	private JPanel infoPanelVector() {
		infoSizeLabel = new JLabel("size: "+rDataObject.vectorObject.size());
		infoIndexLabel = new JLabel("selected index: "+(Math.min(y1, y2)+1)+" - "+(Math.max(y1, y2)+1));
		
		infoInsertRB = new JRadioButton("insert");
		infoInsertRB.setSelected(true);
		infoOverwriteRB = new JRadioButton("overwrite");
		ButtonGroup bg = new ButtonGroup();
		bg.add(infoInsertRB);
		bg.add(infoOverwriteRB);
		
		JButton delete = new JButton("delete selection");
		delete.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				for(int i = Math.max(y1, y2); i >= Math.min(y1,y2); i--)
					rDataObject.vectorObject.remove(i);
				y2 = y1;
				infoTable.revalidate();
				
			}});
		delete.setText("delete selection");
		JButton clear = new JButton("clear");
		clear.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				rDataObject.vectorObject.clear();
				y1 = 0; y2 = 0;
				infoTable.revalidate();
			}});
		clear.setText("clear");
		
		JPanel ret = new JPanel(new BorderLayout());
		JPanel top = new JPanel(new GridLayout(2,2));
		JPanel bot = new JPanel(new FlowLayout());
		top.add(infoSizeLabel);
		top.add(infoInsertRB);
		top.add(infoIndexLabel);
		top.add(infoOverwriteRB);
		bot.add(delete);
		bot.add(clear);
		ret.add(top,BorderLayout.NORTH);
		infoScroll = new JScrollPane(infoTable);
		ret.add(infoScroll, BorderLayout.CENTER);
		ret.add(bot, BorderLayout.SOUTH);
		
		return ret;
	}
	
	/**
	 * Passt das infoPanel entsprechend der List Variable an
	 */
	private JPanel infoPanelList() {
		infoSizeLabel = new JLabel("size: "+rDataObject.listObject.size());
		infoIndexLabel = new JLabel("selected column: "+(Math.min(x1, x2))+" selected index: "+(Math.min(y1, y2)+1)+" - "+(Math.max(y1, y2)+1));
		
		infoInsertRB = new JRadioButton("insert");
		infoInsertRB.setSelected(true);
		infoOverwriteRB = new JRadioButton("overwrite");
		ButtonGroup bg = new ButtonGroup();
		bg.add(infoInsertRB);
		bg.add(infoOverwriteRB);
		
		JButton delete = new JButton("delete selection");
		delete.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				if(x1 < rDataObject.listObject.size())
				{
					for(int i = Math.max(y1, y2); i >= Math.min(y1,y2); i--)
					{
						if(i < rDataObject.listObject.get(x1).size())
							rDataObject.listObject.get(x1).remove(i);
					}
					y2 = y1;
					infoIndexLabel.setText("selected column: "+(Math.min(x1, x2))+" selected index: "+(Math.min(y1, y2)+1)+" - "+(Math.max(y1, y2)+1));
					((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
					infoTable.revalidate();
				}
			}});
		delete.setText("delete selection");
		
		JButton deleteCol = new JButton("delete column");
		deleteCol.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				if(x1 < rDataObject.listObject.size())
				{
					rDataObject.listHeader.remove(x1);
					rDataObject.listObject.remove(x1);
					x1 = 0; y1 = 0; y2 = 0;
					infoIndexLabel.setText("selected column: "+(Math.min(x1, x2))+" selected index: "+(Math.min(y1, y2)+1)+" - "+(Math.max(y1, y2)+1));
					((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
					infoTable.revalidate();
				}
					
			}
		});
		deleteCol.setText("delete column");
		
		JButton clear = new JButton("clear");
		clear.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				rDataObject.listObject.clear();
				rDataObject.listHeader.clear();
				y1 = 0; y2 = 0; x1 = 0;
				infoIndexLabel.setText("selected column: "+(Math.min(x1, x2))+" selected index: "+(Math.min(y1, y2)+1)+" - "+(Math.max(y1, y2)+1));
				((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
				infoTable.revalidate();
			}});
		clear.setText("clear");
		
		JPanel ret = new JPanel(new BorderLayout());
		JPanel top = new JPanel(new GridLayout(2,2));
		JPanel bot = new JPanel(new FlowLayout());
		top.add(infoSizeLabel);
		top.add(infoInsertRB);
		top.add(infoIndexLabel);
		top.add(infoOverwriteRB);
		bot.add(delete);
		bot.add(deleteCol);
		bot.add(clear);
		ret.add(top,BorderLayout.NORTH);
		infoScroll = new JScrollPane(infoTable);
		ret.add(infoScroll, BorderLayout.CENTER);
		ret.add(bot, BorderLayout.SOUTH);

		infoTable.getTableHeader().addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e)
			{
				Point p = e.getPoint();
				String ret = JOptionPane.showInputDialog(infoRowHeader, 
						"New title for column \""+infoTable.getColumnName(infoTable.columnAtPoint(p))+"\":", 
						"new label", JOptionPane.PLAIN_MESSAGE);
				if(ret != null)
				{
					if(infoTable.columnAtPoint(p) == rDataObject.listHeader.size())
					{
						rDataObject.listHeader.add(null);
						rDataObject.listObject.add(new ArrayList<Object>());
						((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
						infoTable.repaint();
					}
					rDataObject.listHeader.set(infoTable.columnAtPoint(p), ret);
					infoTable.getTableHeader().getColumnModel().getColumn(infoTable.columnAtPoint(p)).setHeaderValue(ret);
				}
			}
		});
		
		return ret;
	}
	

	/**
	 * Passt das infoPanel entsprechend der Matrix Variable an
	 */
	private JPanel infoPanelMatrix() {
		if(rDataObject.matrixObject.size() > 0)
			infoSizeLabel = new JLabel("size: "+rDataObject.matrixObject.size()+"x"+rDataObject.matrixObject.get(0).size());
		else
			infoSizeLabel = new JLabel("size: 0x0");
		infoIndexLabel = new JLabel("selected index: ("+(Math.min(x1, x2)+1)+","+(Math.min(y1, y2)+1)+") - ("+(Math.max(x1, x2)+1)+","+(Math.max(y1, y2)+1)+")");
		
		infoInsertRB = new JRadioButton("insert");
		infoInsertRB.setSelected(true);
		infoOverwriteRB = new JRadioButton("overwrite");
		ButtonGroup bg = new ButtonGroup();
		bg.add(infoInsertRB);
		bg.add(infoOverwriteRB);
		JButton deleteRow = new JButton("delete row");
		deleteRow.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				int max = Math.max(y1, y2);
				int min = Math.min(y1, y2);
				for(int i = max; i >= min; i--)
				{
					if(i < rDataObject.matrixObject.size())
						rDataObject.matrixObject.remove(i);
				}
				y1 = min; y2= min;
				if(rDataObject.matrixObject.size() > 0)
					infoSizeLabel.setText("size: "+rDataObject.matrixObject.size()+"x"+rDataObject.matrixObject.get(0).size());
				else
					infoSizeLabel.setText("size: 0x0");
				infoIndexLabel.setText("selected index: ("+(Math.min(x1, x2)+1)+","+(Math.min(y1, y2)+1)+") - ("+(Math.max(x1, x2)+1)+","+(Math.max(y1, y2)+1)+")");
				((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
				infoTable.repaint();
			}});
		deleteRow.setText("delete row");
		
		JButton deleteCol = new JButton("delete column");
		deleteCol.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				int max = Math.max(x1, x2);
				int min = Math.min(x1, x2);
				for(ArrayList<Object> al : rDataObject.matrixObject)
				{
					for(int i = max; i >= min; i--)
					{
						if(i < al.size())
							al.remove(i);
					}
				}
				if(rDataObject.matrixObject.size() == 0 || rDataObject.matrixObject.get(0).size() == 0)
				{
					rDataObject.matrixObject.clear();
					y1 = 0; y2 = 0; x1 = 0; x2 = 0;
				}
				x1 = min; x2= min;
				if(rDataObject.matrixObject.size() > 0)
					infoSizeLabel.setText("size: "+rDataObject.matrixObject.size()+"x"+rDataObject.matrixObject.get(0).size());
				else
					infoSizeLabel.setText("size: 0x0");
				infoIndexLabel.setText("selected index: ("+(Math.min(x1, x2)+1)+","+(Math.min(y1, y2)+1)+") - ("+(Math.max(x1, x2)+1)+","+(Math.max(y1, y2)+1)+")");
				((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
				infoTable.repaint();
			}});
		deleteCol.setText("delete column");
		
		JButton delete = new JButton("delete selection");
		delete.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				int maxx = Math.max(x1, x2);
				int minx = Math.min(x1, x2);
				int maxy = Math.max(y1, y2);
				int miny = Math.min(y1, y2);
				for(int i = miny; i<=maxy; i++)
					for(int j = minx; j<=maxx; j++)
						rDataObject.matrixObject.get(i).set(j, null);
				infoTable.repaint();
			}});
		delete.setText("delete selection");
		
		JButton clear = new JButton("clear");
		clear.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				rDataObject.matrixObject.clear();
				y1 = 0; y2 = 0; x1 = 0; x2 = 0;
				infoSizeLabel.setText("size: 0x0");
				infoIndexLabel.setText("selected index: (1,1)-(1,1)");
				((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
				infoTable.repaint();
			}});
		clear.setText("clear");
		
		JPanel ret = new JPanel(new BorderLayout());
		JPanel top = new JPanel(new GridLayout(2,2));
		JPanel bot = new JPanel(new FlowLayout());
		top.add(infoSizeLabel);
		top.add(infoInsertRB);
		top.add(infoIndexLabel);
		top.add(infoOverwriteRB);
		bot.add(deleteRow);
		bot.add(deleteCol);
		bot.add(delete);
		bot.add(clear);
		ret.add(top,BorderLayout.NORTH);
		infoScroll = new JScrollPane(infoTable);
		ret.add(infoScroll, BorderLayout.CENTER);
		ret.add(bot, BorderLayout.SOUTH);

		return ret;	
	}
	
	/**
	 * Funktion zur �nderung der Array-Dimensionen
	 * @param dimensions: Liste mit den Dimensionen-Angaben
	 */
	private void setArrayDimensions(ArrayList<Integer> dimensions)
	{
		arrayDimPanel.removeAll();
		arrayDimPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		if(dimensions.size() == 0)
		{
			dimensions.add(1);
		}
		if(dimensions.size() == 1)
		{
			dimensions.add(1);
		}
		if(!rDataObject.arrayIndices.equals(dimensions))
		{
			rDataObject.currArrayIndices.clear();
			rDataObject.currArrayIndices.addAll(dimensions);
			rDataObject.arrayIndices.clear();
			rDataObject.arrayIndices.addAll(rDataObject.currArrayIndices);
			int number = 1;
			for(int i = 0; i < dimensions.size(); i++)
			{
				number *= dimensions.get(i);
				rDataObject.currArrayIndices.set(i, 0);
			}
			rDataObject.arrayObject.clear();
			for(int i = 0; i < number; i++)
				rDataObject.arrayObject.add(null);
		}
		
		ArrayList<Integer> dim = new ArrayList<Integer>();
		dim.addAll(dimensions);
		
		for(int i = 0; i < 2; i++)
		{
			JLabel l = new JLabel(""+dim.get(0));
			JLabel l2 = new JLabel(" ");
			JPanel p = new JPanel(new GridLayout(2,1));
			p.add(l);
			p.add(l2);
			arrayDimPanel.add(p);
			if(i==0)
			{
				JLabel x = new JLabel("x");
				JLabel xl = new JLabel(" ");
				JPanel xp = new JPanel(new GridLayout(2,1));
				xp.add(x);
				xp.add(xl);
				arrayDimPanel.add(xp);
			}
			dim.remove(0);
		}
		
		int index = 2;
		while(dim.size() > 0)
		{
			Object[] num = new Object[dim.get(0)];
			for(int i = 0; i < dim.get(0); i++)
				num[i] = i+1;
			JLabel l = new JLabel(""+dim.get(0));
			final JComboBox cb = new JComboBox((Object[])num);
			final int i = index;
			cb.setSelectedIndex(rDataObject.currArrayIndices.get(index));
			cb.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					rDataObject.currArrayIndices.set(i, cb.getSelectedIndex());
					infoTable.revalidate();
					infoTable.repaint();
				}
				
			});
			JPanel p = new JPanel(new GridLayout(2,1));
			p.add(l);
			p.add(cb);
			
			JLabel x = new JLabel("x");
			JLabel xl = new JLabel(" ");
			JPanel xp = new JPanel(new GridLayout(2,1));
			xp.add(x);
			xp.add(xl);
			arrayDimPanel.add(xp);
			arrayDimPanel.add(p);
			dim.remove(0);
			index++;
		}
		
		
		((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
		infoTable.repaint();
		arrayDimPanel.revalidate();
	}
	
	/**
	 * Passt das infoPanel entsprechend der Array Variable an
	 */
	private JPanel infoPanelArray() {
		JButton setDim = new JButton("set dimensions");
		setDim.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				setArrayDimensions(RArrayDimensionDialog.showRArrayDimensionDialog());
			}});
		setDim.setText("set dimensions");
		JPanel dimPanel = new JPanel(new GridLayout(1,2));
		infoIndexLabel = new JLabel("selected index: ("+(Math.min(x1, x2)+1)+","+(Math.min(y1, y2)+1)+") - ("+(Math.max(x1, x2)+1)+","+(Math.max(y1, y2)+1)+")");
		dimPanel.add(infoIndexLabel);
		JPanel dimButtonPanel = new JPanel(new BorderLayout());
		dimButtonPanel.add(setDim, BorderLayout.WEST);
		dimPanel.add(dimButtonPanel);
		arrayDimPanel = new JPanel();
		
		if(rDataObject.arrayIndices.size() > 1)
			setArrayDimensions(rDataObject.arrayIndices);
		else
		{
			ArrayList<Integer> tmpAL = new ArrayList<Integer>();
			tmpAL.add(1);tmpAL.add(1);
			setArrayDimensions(tmpAL);
		}
		
		JButton del = new JButton("delete selection");
		del.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				int arrayBigIndex = 0;
				for(int i = 2; i < rDataObject.currArrayIndices.size(); i++)
				{
					int sizes = 1;
					for(int j = 0; j < i; j++)
						sizes *= rDataObject.arrayIndices.get(j);
					arrayBigIndex += sizes * rDataObject.currArrayIndices.get(i);
				}
				int arrayWidth = rDataObject.arrayIndices.get(1);
				int arrayHeight = rDataObject.arrayIndices.get(0);
				for(int i = Math.min(x1, x2); i <= Math.max(x1, x2); i++)
					for(int j = Math.min(y1, y2); j <= Math.max(y1, y2); j++)
					{
						rDataObject.arrayObject.set(arrayBigIndex+(i*arrayHeight+j), null);						
					}
				infoTable.revalidate();
				infoTable.repaint();
			}});
		del.setText("delete selection");

		JButton clear = new JButton("clear view");
		clear.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				int arrayBigIndex = 0;
				for(int i = 2; i < rDataObject.currArrayIndices.size(); i++)
				{
					int sizes = 1;
					for(int j = 0; j < i; j++)
						sizes *= rDataObject.arrayIndices.get(j);
					arrayBigIndex += sizes * rDataObject.currArrayIndices.get(i);
				}
				int arrayWidth = rDataObject.arrayIndices.get(1);
				int arrayHeight = rDataObject.arrayIndices.get(0);
				for(int i = 0; i < arrayHeight; i++)
				{
					for(int j = 0; j < arrayWidth; j++)
					{
						rDataObject.arrayObject.set(arrayBigIndex+(j*arrayHeight+i), null);
					}
				}
				infoTable.revalidate();
				infoTable.repaint();
			}});
		clear.setText("clear view");
		
		JButton clearAll = new JButton("clear everything");
		clearAll.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				setArrayDimensions(new ArrayList<Integer>());
			}});
		clearAll.setText("clear everything");
		
		
		JPanel ret = new JPanel(new BorderLayout());
		JPanel top = new JPanel(new BorderLayout());
		JPanel bot = new JPanel(new FlowLayout());
		
		top.add(dimPanel, BorderLayout.NORTH);
		top.add(arrayDimPanel, BorderLayout.SOUTH);
		
		bot.add(del);
		bot.add(clear);
		bot.add(clearAll);
		
		ret.add(top, BorderLayout.NORTH);
		infoScroll = new JScrollPane(infoTable);
		ret.add(infoScroll, BorderLayout.CENTER);
		ret.add(bot, BorderLayout.SOUTH);
		return ret;
	}
		
	/**
	 * Passt das infoPanel entsprechend der DataFrame Variable an
	 */
	private JPanel infoPanelDataFrame() {
		if(rDataObject.matrixObject.size() > 0)
			infoSizeLabel = new JLabel("size: "+rDataObject.matrixObject.size()+"x"+rDataObject.matrixObject.get(0).size());
		else
			infoSizeLabel = new JLabel("size: 0x0");
		infoIndexLabel = new JLabel("selected index: ("+(Math.min(x1, x2)+1)+","+(Math.min(y1, y2)+1)+") - ("+(Math.max(x1, x2)+1)+","+(Math.max(y1, y2)+1)+")");
		
		infoInsertRB = new JRadioButton("insert");
		infoInsertRB.setSelected(true);
		infoOverwriteRB = new JRadioButton("overwrite");
		ButtonGroup bg = new ButtonGroup();
		bg.add(infoInsertRB);
		bg.add(infoOverwriteRB);
		
		JButton copy = new JButton("use whole dataset");
		copy.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				rDataObject.dataframeObject.clear();
				for(int i = 0; i < dataset.length; i++)
				{
					rDataObject.dataframeObject.add(new ArrayList<Object>());
					for(int j = 0; j < dataset[0].length; j++)
					{
						rDataObject.dataframeObject.get(i).add(dataset[i][j]);
					}	
				}
				rDataObject.dataframeColumnHeader.clear();
				for(int i = 0; i < columnHeader.length; i++)
					rDataObject.dataframeColumnHeader.add(columnHeader[i]);
				rDataObject.dataframeRowHeader.clear();
				for(int i = 0; i < dataRowHeaderListModel.getSize(); i++)
					rDataObject.dataframeRowHeader.add(dataRowHeaderListModel.getElementAt(i));
				((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
				infoTable.repaint();
			}});
		copy.setText("use whole dataset");
		
		JButton deleteRow = new JButton("delete row");
		deleteRow.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				int max = Math.max(y1, y2);
				int min = Math.min(y1, y2);
				for(int i = max; i >= min; i--)
				{
					if(i < rDataObject.dataframeRowHeader.size())
					{
						rDataObject.dataframeObject.remove(i);
						rDataObject.dataframeRowHeader.remove(i);
					}
				}
				y1 = min; y2= min;
				infoIndexLabel.setText("selected index: ("+(Math.min(x1, x2)+1)+","+(Math.min(y1, y2)+1)+") - ("+(Math.max(x1, x2)+1)+","+(Math.max(y1, y2)+1)+")");
				((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
				infoTable.repaint();
			}});
		deleteRow.setText("delete row");
		
		JButton deleteCol = new JButton("delete column");
		deleteCol.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				int max = Math.max(x1, x2);
				int min = Math.min(x1, x2);
				for(ArrayList<Object> al : rDataObject.dataframeObject)
				{
					for(int i = max; i >= min; i--)
					{
						if(i < rDataObject.dataframeColumnHeader.size())
						{
							al.remove(i);
						}
					}
				}
				for(int i = max; i >= min; i--)
					if(i < rDataObject.dataframeColumnHeader.size())
						rDataObject.dataframeColumnHeader.remove(i);
				if(rDataObject.dataframeObject.get(0).size() == 0)
				{
					rDataObject.dataframeObject.clear();
					y1 = 0; y2 = 0; x1 = 0; x2 = 0;
				}
				x1 = min; x2= min;
				infoIndexLabel.setText("selected index: ("+(Math.min(x1, x2)+1)+","+(Math.min(y1, y2)+1)+") - ("+(Math.max(x1, x2)+1)+","+(Math.max(y1, y2)+1)+")");
				((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
				infoTable.repaint();
			}});
		deleteCol.setText("delete column");
		
		JButton delete = new JButton("delete selection");
		delete.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				int maxx = Math.max(x1, x2);
				int minx = Math.min(x1, x2);
				int maxy = Math.max(y1, y2);
				int miny = Math.min(y1, y2);
				for(int i = miny; i<=maxy; i++)
					for(int j = minx; j<=maxx; j++)
						rDataObject.dataframeObject.get(i).set(j, null);
				infoTable.repaint();
			}});
		delete.setText("delete selection");
		
		JButton clear = new JButton("clear");
		clear.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				rDataObject.dataframeObject.clear();
				rDataObject.dataframeColumnHeader.clear();
				rDataObject.dataframeRowHeader.clear();
				y1 = 0; y2 = 0; x1 = 0; x2 = 0;
				infoSizeLabel.setText("size: 0x0");
				infoIndexLabel.setText("selected index: (1,1) - (1,1)");					
				((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
				infoTable.repaint();
			}});
		clear.setText("clear");
		
		JPanel ret = new JPanel(new BorderLayout());
		JPanel top = new JPanel(new GridLayout(2,2));
		JPanel bot = new JPanel(new FlowLayout());
		top.add(infoSizeLabel);
		top.add(infoInsertRB);
		top.add(infoIndexLabel);
		top.add(infoOverwriteRB);
		bot.add(copy);
		bot.add(deleteRow);
		bot.add(deleteCol);
		bot.add(delete);
		bot.add(clear);
		ret.add(top,BorderLayout.NORTH);
		infoScroll = new JScrollPane(infoTable);
		ret.add(infoScroll, BorderLayout.CENTER);
		ret.add(bot, BorderLayout.SOUTH);

		infoRowHeaderListModel = new InfoRowHeaderListModel();
		infoRowHeader = new JList(infoRowHeaderListModel);
		infoRowHeader.setFixedCellWidth(55);
		infoRowHeader.setFixedCellHeight(infoTable.getRowHeight());
		infoRowHeader.setCellRenderer(new RowHeaderRenderer(infoTable));
		infoRowHeader.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e)
			{
				Point p = e.getPoint();
				String ret = JOptionPane.showInputDialog(MainFrame.getInstance(), 
						"New title for row \""+infoRowHeaderListModel.getElementAt((int)(p.y/infoRowHeader.getFixedCellHeight()))+"\":", 
						"new label", JOptionPane.PLAIN_MESSAGE);
				if(ret != null)
				{
						if((int)(p.y/infoRowHeader.getFixedCellHeight()) == rDataObject.dataframeRowHeader.size())
						{
							rDataObject.dataframeRowHeader.add(null);
							rDataObject.dataframeObject.add(new ArrayList<Object>());
							for(int i = 0; i < rDataObject.dataframeObject.get(0).size(); i++)
								rDataObject.dataframeObject.get(rDataObject.dataframeObject.size()-1).add(null);
						}
						rDataObject.dataframeRowHeader.set((int)(p.y/infoRowHeader.getFixedCellHeight()), ret);
					infoRowHeader.revalidate();							
				}
			}
		});
		infoScroll.setRowHeaderView(infoRowHeader);
		
		infoTable.getTableHeader().addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e)
			{
				Point p = e.getPoint();
				String ret = JOptionPane.showInputDialog(infoRowHeader, 
						"New title for column \""+infoTable.getColumnName(infoTable.columnAtPoint(p))+"\":", 
						"new label", JOptionPane.PLAIN_MESSAGE);
				if(ret != null)
				{
					if(infoTable.columnAtPoint(p) == rDataObject.dataframeColumnHeader.size())
					{
						rDataObject.dataframeColumnHeader.add(null);
						for(ArrayList<Object> al : rDataObject.dataframeObject)
						{
							al.add(null);
						}
						((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
						infoTable.repaint();
					}
					rDataObject.dataframeColumnHeader.set(infoTable.columnAtPoint(p), ret);
					infoTable.getTableHeader().getColumnModel().getColumn(infoTable.columnAtPoint(p)).setHeaderValue(ret);
				}
			}
		});
		
		return ret;		
	}

	/**
	 * Renderer f�r die dataTable
	 * @author Torsten
	 * Steuert Farbgebung (Zeilen abwechselnd wei�/grau und die Selektierung in gelb abh�ngig vom Variablentyp)
	 */
	protected class DataTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

	    public Component getTableCellRendererComponent(JTable table,
	            Object value, boolean isSelected, boolean hasFocus, int row, int col) 
	    {
	        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
	        if(row%2 == 0)
	        	c.setBackground(Color.white);
	        else
	        	c.setBackground(new Color(0.9f,0.9f,0.9f));
        	this.setToolTipText(tooltips[row][col]);
        	c.setForeground(Color.black);
        	
        	if(dataMousePressed)
        	{
				Point p = dataTable.getMousePosition();
				int ccol, crow;
				if(p!=null)
				{
					ccol = dataTable.columnAtPoint(p);
					crow = dataTable.rowAtPoint(p);
				}
				else
				{
					ccol = dataMouseColumn;
					crow = dataMouseRow;
				}
				
				switch(variableMode)
				{
					case 1:
					case 2:
						boolean direction = Math.abs(ccol - dataMouseColumn) <= Math.abs(crow - dataMouseRow);//true: nimm Spalte, false: nimm Reihe
			        	if((!direction && ((row == dataMouseRow && col <= ccol && col >= dataMouseColumn)
			        				||(row == dataMouseRow && col >= ccol && col <= dataMouseColumn)))
			        			||(direction && ((col == dataMouseColumn && row <= crow && row >= dataMouseRow)
			        				||(col == dataMouseColumn && row >= crow && row <= dataMouseRow))))
			        		c.setBackground(Color.yellow);
			        	break;
					case 3: case 4: case 5:
						if(((row <= crow && row >= dataMouseRow) || (row >= crow && row <= dataMouseRow)) 
								&& ((col <= ccol && col >= dataMouseColumn) || (col >= ccol && col <= dataMouseColumn)))
							c.setBackground(Color.yellow);
						break;
				}
	        	dataTable.repaint();
	        	
        	}
        		
	        return this;
	    }
	}
		 
	/**
	 * ListModel zur Darstellung der RowHeader des dataTables
	 * @author Torsten
	 *
	 */
	protected class DataRowHeaderListModel extends AbstractListModel{
		private static final long serialVersionUID = 1L;
		public int getSize(){
			return dataset.length;
		}
		 
		public Object getElementAt(int index){
			return dataset[index][viewChooser.getSelectedIndex()];
		}
	}

	/**
	 * ListModel zur Darstellung der RowHeader des infoTables (nur bei data frames)
	 * @author Torsten
	 *
	 */
	protected class InfoRowHeaderListModel extends AbstractListModel{
		private static final long serialVersionUID = 1L;
		public int getSize(){
			return rDataObject.dataframeRowHeader.size()+1;
		}
		 
		public Object getElementAt(int index){
			if(index < rDataObject.dataframeRowHeader.size())
			{
				if(rDataObject.dataframeRowHeader.get(index)!= null)
					return rDataObject.dataframeRowHeader.get(index);
				else
					return (index+1);
			}
			else
				return "";
		}
	}
		 
	/**
	 * Renderer f�r die RowHeader
	 * @author Torsten
	 *
	 */
	protected class RowHeaderRenderer extends JLabel implements ListCellRenderer{
		private static final long serialVersionUID = 1L;
		RowHeaderRenderer(JTable table){
			JTableHeader tableHeader = table.getTableHeader();
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setHorizontalAlignment(CENTER);
			setForeground(tableHeader.getForeground());
			setOpaque(true);  // Damit der Hintergrund nicht ver�ndert wird
			setFont(tableHeader.getFont());
		}
		 
		public Component getListCellRendererComponent(JList list, Object value, int index, 
													boolean fSelected, boolean fCellHasFocus){
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}
		
	/**
	 * TableModel f�r die dataTable
	 * @author Torsten
	 */
	protected class DataTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 1L;

		public DataTableModel()
		{
		}
			
		public boolean isCellEditable(int row, int column){
			return false;
		}	        

		public int getColumnCount() {
			return columnHeader.length;
		}

		public int getRowCount() {
			return dataset.length;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if(rowIndex < getRowCount() && columnIndex < getColumnCount() && rowIndex >= 0 && columnIndex >= 0)
				return dataset[rowIndex][columnIndex];
			return null;
		}	
			
        public String getColumnName(int col) {
            return columnHeader[col];
        }

	}
		
	/**
	 * Renderer f�r die infoTable
	 * @author Torsten
	 * Stellt Funktionen zur F�rbung der infoTable (Zeilen alternierend grau/wei� und Gelbf�rbung der Auswahl)
	 */
	protected class InfoTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

	    public Component getTableCellRendererComponent(JTable table,
	            Object value, boolean isSelected, boolean hasFocus, int row, int col) 
	    {
	        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
	        if(row%2 == 0)
	        	c.setBackground(Color.white);
	        else
	        	c.setBackground(new Color(0.9f,0.9f,0.9f));
        	c.setForeground(Color.black);
        	

        	if(infoMousePressed)
        	{
				Point p = infoTable.getMousePosition();
				x2 = infoTable.columnAtPoint(p);
				y2 = infoTable.rowAtPoint(p);
        	}
        	
        	if((variableMode != 2 && Math.min(x1,x2) <= col && Math.max(x1,x2) >= col 
        			&& Math.min(y1,y2) <= row && Math.max(y1,y2) >= row)
        			|| variableMode == 2 && x1 == col && Math.min(y1,y2) <= row && Math.max(y1,y2) >= row)
        	{
        		c.setBackground(Color.yellow);
        	}
	        return this;
	    }
	}
		
	/**
	 * TableModel for die infoTable
	 * @author Torsten
	 * Stellt den Zugriff der infoTable auf die Daten dar
	 */
	protected class InfoTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 1L;
		String[] columnHeader;

				
		
		public InfoTableModel()
		{
			refreshColumnHeader();
		}
		
		public boolean isCellEditable(int row, int column){
				return true;
		}	        

		public int getColumnCount() {
			switch(variableMode)
			{
			case 1: return 1;
			case 2: return rDataObject.listHeader.size()+1;
			case 3: if(rDataObject.matrixObject.size() == 0)
						return 1;
					else
					{
						int max = 0;
						for(ArrayList<Object> al : rDataObject.matrixObject)
						{
							max = Math.max(max, al.size());
						}
						return max+1;
					}
			case 4: if(rDataObject.arrayObject.size() == 0)
						return 0;
					else
						return rDataObject.arrayIndices.get(1);
			case 5: return rDataObject.dataframeColumnHeader.size()+1;
			default: return 0;
			}
		}

		public int getRowCount() {
			switch(variableMode)
			{
			case 1: return rDataObject.vectorObject.size()+1;
			case 2: int max = 0; 
				for(ArrayList<Object> al : rDataObject.listObject)
					max = Math.max(max, al.size());
				return max+1;
			case 3: return rDataObject.matrixObject.size()+1;
			case 4: if(rDataObject.arrayObject.size() == 0)
						return 0;
					else
						return rDataObject.arrayIndices.get(0);
			case 5: return rDataObject.dataframeRowHeader.size()+1;
			default: return 0;
			}
		}
		
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch(variableMode)
			{
			case 1:
				if(rowIndex < rDataObject.vectorObject.size())
					return rDataObject.vectorObject.get(rowIndex);
				return null;
			case 2: 
				if(columnIndex < rDataObject.listObject.size())
					if(rowIndex < rDataObject.listObject.get(columnIndex).size())
						return rDataObject.listObject.get(columnIndex).get(rowIndex);
				return null;

			case 3:
				if(rowIndex < rDataObject.matrixObject.size())
					if(columnIndex < rDataObject.matrixObject.get(rowIndex).size())
						return rDataObject.matrixObject.get(rowIndex).get(columnIndex);
				return null;
			case 4: 
				int index = rowIndex + columnIndex * rDataObject.arrayIndices.get(0);
				for(int i = 2; i < rDataObject.currArrayIndices.size(); i++)
				{
					int sizes = 1;
					for(int j = 0; j < i; j++)
						sizes *= rDataObject.arrayIndices.get(j);
					index += sizes * rDataObject.currArrayIndices.get(i);
				}
				return rDataObject.arrayObject.get(index);
			case 5:
				if(rowIndex < rDataObject.dataframeObject.size())
					if(columnIndex < rDataObject.dataframeObject.get(0).size())
						return rDataObject.dataframeObject.get(rowIndex).get(columnIndex);
				return null;
			}
			
			
			if(rowIndex < getRowCount() && columnIndex < getColumnCount() && rowIndex >= 0 && columnIndex >= 0)
				return dataset[rowIndex][columnIndex];
			return null;
		}
		
		public void setValueAt(Object o, int row, int col)
		{
			switch(variableMode)
			{
			case 1://vector
				if(row == rDataObject.vectorObject.size())
				{
					rDataObject.vectorObject.add(o);
					((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
					infoTable.repaint();						
				}
				else
					rDataObject.vectorObject.set(row, o);
				break;
			case 2://list
				if(col == rDataObject.listObject.size())
				{
					rDataObject.listHeader.add(null);
					rDataObject.listObject.add(new ArrayList<Object>());
					rDataObject.listObject.get(rDataObject.listObject.size()-1).add(o);
				}
				else if(rDataObject.listObject.get(col).size() <= row)
					rDataObject.listObject.get(col).add(o);
				else
					rDataObject.listObject.get(col).set(row, o);
				break;
			case 3://matrix
				if(rDataObject.matrixObject.size() == 0)
					rDataObject.matrixObject.add(new ArrayList<Object>());
				
				if(col == rDataObject.matrixObject.get(0).size())
				{
					for(int i = 0; i < rDataObject.matrixObject.size(); i++)
						rDataObject.matrixObject.get(i).add(null);
				}
				if(row == rDataObject.matrixObject.size())
				{
					rDataObject.matrixObject.add(new ArrayList<Object>());
					for(int i = 0; i < rDataObject.matrixObject.get(0).size(); i++)
						rDataObject.matrixObject.get(row).add(null);
				}
				rDataObject.matrixObject.get(row).set(col,o);
				((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
				infoTable.repaint();
				break;
			case 4://array
				int index = row + col * rDataObject.arrayIndices.get(0);
				for(int i = 2; i < rDataObject.currArrayIndices.size(); i++)
				{
					int sizes = 1;
					for(int j = 0; j < i; j++)
						sizes *= rDataObject.arrayIndices.get(j);
					index += sizes * rDataObject.currArrayIndices.get(i);
				}
				rDataObject.arrayObject.set(index, o);
				break;
			case 5://data.frame
				if(rDataObject.dataframeObject.size() == 0)
					rDataObject.dataframeObject.add(new ArrayList<Object>());
				
				if(col == rDataObject.dataframeColumnHeader.size())
				{
					rDataObject.dataframeColumnHeader.add(rDataObject.dataframeColumnHeader.size());
					for(int i = 0; i < rDataObject.dataframeObject.size(); i++)
						rDataObject.dataframeObject.get(i).add(null);
				}
				if(row == rDataObject.dataframeRowHeader.size())
				{
					rDataObject.dataframeRowHeader.add(rDataObject.dataframeRowHeader.size());
					rDataObject.dataframeObject.add(new ArrayList<Object>());
					for(int i = 0; i < rDataObject.dataframeObject.get(0).size(); i++)
						rDataObject.dataframeObject.get(row).add(null);
				}
				rDataObject.dataframeObject.get(row).set(col,o);
				((AbstractTableModel)infoTable.getModel()).fireTableStructureChanged();
				infoTable.repaint();			}
		}
		
		public void refreshColumnHeader()
		{
			switch(variableMode)
			{
			case 2:
				columnHeader = new String[rDataObject.listHeader.size()+1];
				for(int i = 0; i < rDataObject.listHeader.size(); i++)
				{
					if(rDataObject.listHeader.get(i) != null)
						columnHeader[i] = rDataObject.listHeader.get(i).toString();
					else
						columnHeader[i] = null;
				}
				break;
			case 5:
				columnHeader = new String[rDataObject.dataframeColumnHeader.size()+1];
				for(int i = 0; i < rDataObject.dataframeColumnHeader.size(); i++)
				{
					if(rDataObject.dataframeColumnHeader.get(i) != null)
						columnHeader[i] = rDataObject.dataframeColumnHeader.get(i).toString();
					else
						columnHeader[i] = null;
				}
			}
		}
		
        public String getColumnName(int col) {
        	switch(variableMode)
        	{
        	case 1: return "data";
        	case 2: 
        		if(columnHeader.length != rDataObject.listHeader.size()+1)
        			refreshColumnHeader();
        		if(col == rDataObject.listHeader.size())
        			return "";
        		if(columnHeader[col] == null)
        			return ""+(col+1);
        		else
        			return columnHeader[col];

        	case 3: return ""+(col+1);
        	case 4: return ""+(col+1);
        	case 5:
        		if(columnHeader.length != rDataObject.dataframeColumnHeader.size()+1)
        			refreshColumnHeader();
        		if(col == rDataObject.dataframeColumnHeader.size())
        			return "";
        		if(columnHeader[col] == null)
        			return ""+(col+1);
        		else
        			return columnHeader[col];
        	}
        	return "";
        }
	}
	
}

