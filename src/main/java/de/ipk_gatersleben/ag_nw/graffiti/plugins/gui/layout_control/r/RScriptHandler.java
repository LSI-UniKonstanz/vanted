package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.r;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;


import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.R.RService;


/**
 * Bearbeitet die eingegebenen Variablen, setzt diese in R um und f�hrt anschlie�end das Skript aus
 */
public class RScriptHandler {
	
	String script;
	ArrayList<RVariable> inputVariables;
//    TODO: Output-Variablen
//	ArrayList<RVariable> outputVariables;
	
	TabRControl tab;
	
	RService rs;
	
	ArrayList<ExperimentInterface> expInts;
	Object[][] dataset;
	String[][] tooltips;
	String[] columnHeader;
	
	public RScriptHandler(Component parentComponent)
	{
		inputVariables = new ArrayList<RVariable>();
//        TODO: Output-Variablen
//		outputVariables = new ArrayList<RVariable>();
		expInts = null;
		script = null;
		if(parentComponent instanceof TabRControl)
			tab = (TabRControl)parentComponent;
		else
			tab = null;
	}
	
	public void loadScript(File f)
	{
		inputVariables = new ArrayList<RVariable>();
//        TODO: Output-Variablen
//		outputVariables = new ArrayList<RVariable>();
		loadScriptFromFile(f);		
	}
	
	/**
	 * Funktion zum Ausf�hren des Skripts (quasi der Kern der Klasse)
	 */
	public void runScript()
	{
		rs = new RService();//Starten von RServe
		
		for(RVariable var : inputVariables)//Fehlermeldung, falls eine der Variablen nicht belegt ist
		{
			if((var.type.equals("vector") && var.getData().vectorObject.size() == 0)
					||(var.type.equals("matrix") && var.getData().matrixObject.size() == 0)
					||(var.type.equals("array") && var.getData().arrayObject.size() == 0)
					||(var.type.equals("list") && var.getData().listObject.size() == 0)
					||(var.type.equals("data.frame") && var.getData().dataframeObject.size() == 0))
			{
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "You have to assign all variables!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		for(RVariable var : inputVariables)//hier werden die einzelnen Variablen in R definiert
		{
			if(var.type.equals("vector") && var.getData().vectorObject.size() > 0)//Vectoren
			{
				String definition = var.getName()+" <- c("+var.getData().vectorObject.get(0);
				for(int i = 1; i < var.getData().vectorObject.size(); i++)
				{
					if(var.getData().vectorObject.get(i) == null)
						definition += ",NaN";
					else
						definition += ","+var.getData().vectorObject.get(i);
				}
				definition +=")";
				evaluate(definition);
			}
			else if(var.type.equals("list") && var.getData().listObject.size() > 0)//Listen
			{
				String definition = var.getName()+" <- list(";
				for(int i = 0; i < var.getData().listObject.size(); i++)
				{
					if(var.getData().listHeader.get(i) != null)
						definition += var.getData().listHeader.get(i)+"=c(";
					else
						definition += "c(";
					for(int j = 0; j < var.getData().listObject.get(i).size(); j++)
					{
						if(var.getData().listObject.get(i).get(j) == null)
							definition += "NaN,";
						else if(var.getData().listObject.get(i).get(j).toString().matches("\\d+([.]{1}\\d+)?"))
							definition += var.getData().listObject.get(i).get(j) + ",";
						else
							definition += "\"" + var.getData().listObject.get(i).get(j) + "\",";
					}
					definition = definition.substring(0, definition.length()-1);
					definition += "),";
				}
				definition = definition.substring(0, definition.length()-1);
				definition +=")";
				evaluate(definition);
			}
			else if(var.type.equals("matrix") && var.getData().matrixObject.size() > 0)//Matritzen
			{
				ArrayList<ArrayList<Object>> matrix = var.getData().matrixObject;
				String definition = var.getName()+" <- matrix(0,"+matrix.size()+","+matrix.get(0).size()+")";
				evaluate(definition);
				for(int i = 0; i < matrix.size(); i++)
					for(int j = 0; j < matrix.get(i).size(); j++)
					{
							if(matrix.get(i).get(j) != null)
							{
								if(matrix.get(i).get(j).toString().matches("\\d+([.]{1}\\d+)?"))
									evaluate(var.getName()+"["+(i+1)+","+(j+1)+"]<-"+matrix.get(i).get(j));
								else
									evaluate(var.getName()+"["+(i+1)+","+(j+1)+"]<-\""+matrix.get(i).get(j)+"\"");
							}
							else
								evaluate(var.getName()+"["+(i+1)+","+(j+1)+"]<-NaN");
					}
			}
			else if(var.type.equals("array") && var.getData().arrayObject.size() > 0)//Arrays
			{				
				String definition = var.getName()+"<-c(";
				for(Object data : var.getData().arrayObject)
				{
					if(data == null)
						definition += "NaN,";
					else
						definition += data.toString()+",";
				}
				definition = definition.substring(0, definition.length()-1);
				definition +=")";
				evaluate(definition);
				definition = "dim("+var.getName()+")<-c(";
				for(Object ind : var.getData().arrayIndices)
				{
					definition += ind.toString()+",";
				}
				definition = definition.substring(0, definition.length()-1);
				definition +=")";
				evaluate(definition);
			}
			else if(var.type.equals("data.frame") && var.getData().dataframeObject.size() > 0)//und Dataframes
			{
				String definition = var.getName()+" <- data.frame(cbind(";
				for(int i = 0; i < var.getData().dataframeColumnHeader.size(); i++)
				{
					if(var.getData().dataframeColumnHeader.get(i) != null)
						definition += var.getData().dataframeColumnHeader.get(i).toString() + "=c(";
					else
						definition += "no_Header=c(";
					for(int j = 0; j < var.getData().dataframeRowHeader.size(); j++)
					{
						if(var.getData().dataframeObject.get(j).get(i) == null)
							definition += "NaN" + ",";
						else 
						if(var.getData().dataframeObject.get(j).get(i).toString().matches("\\d+([.]{1}\\d+)?"))
							definition += var.getData().dataframeObject.get(j).get(i) + ",";
						else
							definition += "\""+var.getData().dataframeObject.get(j).get(i) + "\",";							
					}
					definition = definition.substring(0,definition.length()-1);
					definition += "),";
				}
				definition = definition.substring(0,definition.length()-1);
				definition += ")";
				
				boolean found = false;
				for(Object rowHeader : var.getData().dataframeRowHeader)
				{
					if(rowHeader != null)
					{
						found = true;
						break;
					}
				}
				if(found)
				{
					ArrayList<String> names = new ArrayList<String>();
					ArrayList<Integer> counter = new ArrayList<Integer>();
					definition += ", row.names = c(";
					
					for(Object rowHeader : var.getData().dataframeRowHeader)
					{
						if(rowHeader == null)
							rowHeader = new String("no_Header");
						if(names.contains(rowHeader.toString()))
						{
							definition += "\"" + rowHeader.toString() + "." + counter.get(names.indexOf(rowHeader.toString())) + "\",";
							counter.set(names.indexOf(rowHeader.toString()),counter.get(names.indexOf(rowHeader.toString()))+1);
						}
						else
						{
							names.add(rowHeader.toString());
							counter.add(1);
							definition += "\"" + rowHeader.toString() + "\",";
						}
					}
					definition = definition.substring(0, definition.length()-1);
					definition += "))";
				}
				else
				{
					definition += ")";
				}
				evaluate(definition);
			}
		}
		
		for(String s : script.split("\n"))//Ausf�hrung des eigentlichen Skrips
			if(s.length()>0 && !s.startsWith("#"))
			{
				evaluate(s);
			}
		RService.closeRserve();
	}
	
	/**
	 * Funktion zur Eingabe von Strings in R 
	 * @param s: R Kommando
	 */
	private void evaluate(String s)
	{
		try {
			rs.safeEval(s);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "R error: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
	}

	/**
	 * Funktion zum Laden von Messdaten o.�. aus einem Graphen
	 * @param g: Graph mit Daten
	 */
	public void loadGraphData(Graph g)
	{
		ArrayList<GraphElement> allGraphElements = new ArrayList<GraphElement>();
		allGraphElements.addAll(g.getGraphElements());
		expInts = new ArrayList<ExperimentInterface>();
		for(GraphElement ge : allGraphElements)
		{
			ExperimentInterface ei = Experiment2GraphHelper.getMappedDataListFromGraphElement(ge);
			if(ei != null)
				expInts.add(ei);
		}

		if(tab != null)
		{
			tab.setGraphDataLoadButton();
			tab.setGraph(g);
		}
		
		arrangeData();
	}
	
	/**
	 * Funktion, die die aus dem Graphen gesammelten Daten in Tabellenform bringt und abspeichert
	 */
	private void arrangeData()
	{
		// plant    messurementtool  time       substance   rest
		Map<String, Map<String, Map<Integer, Map<String, dataField>>>> data = new HashMap<String, Map<String, Map<Integer, Map<String, dataField>>>>();
		Map<String, ArrayList<String>> mmtsSubstances = new HashMap<String, ArrayList<String>>();
		ArrayList<String> plants = new ArrayList<String>();
		ArrayList<String> mmts = new ArrayList<String>();
		ArrayList<Integer> times = new ArrayList<Integer>();
		ArrayList<String> substances = new ArrayList<String>();
		
		for(ExperimentInterface ei : expInts)
			for(SubstanceInterface si : ei)
				for(ConditionInterface ci : si.getConditions(null))
					for(SampleInterface sami : ci)
					{
						if(!plants.contains(ci.getName()))
							plants.add(ci.getName());
						if(!data.containsKey(ci.getName()))
							data.put(ci.getName(), new HashMap<String, Map<Integer, Map<String, dataField>>>());
						if(!mmts.contains(sami.getMeasurementtool()))
							mmts.add(sami.getMeasurementtool());
						if(!data.get(ci.getName()).containsKey(sami.getMeasurementtool()))
							data.get(ci.getName()).put(sami.getMeasurementtool(), new HashMap<Integer, Map<String, dataField>>());
						if(!times.contains(sami.getTime()))
							times.add(sami.getTime());
						if(!data.get(ci.getName()).get(sami.getMeasurementtool()).containsKey(sami.getTime()))
							data.get(ci.getName()).get(sami.getMeasurementtool()).put(sami.getTime(),new HashMap<String, dataField>());
						if(!substances.contains(si.getName()))
							substances.add(si.getName());
						if(!data.get(ci.getName()).get(sami.getMeasurementtool()).get(sami.getTime()).containsKey(si.getName()))
						{
							String tooltip = "<html>Start of Experiment: "+ci.getExperimentHeader().getStartdate()
							+"<br>Remark: "+ci.getExperimentHeader().getRemark()
							+"<br>Experiment name: "+ci.getExperimentName()
							+"<br>Coordinator: "+ci.getCoordinator()
							+"<br>Sequence name: "+ci.getSequence()
							+"<br>Growth conditions: "+ci.getGrowthconditions()
							+"<br>Unit: "+sami.getAverageUnit()
							+"<br>Measurement tool: "+sami.getMeasurementtool();							
							ArrayList<Double> mess = new ArrayList<Double>();
							for(double d : sami.getDataList())
								mess.add(d);
							data.get(ci.getName()).get(sami.getMeasurementtool()).get(sami.getTime()).put(si.getName(),new dataField(mess,tooltip,sami.getSampleTime()));
						}
						
						if(!mmtsSubstances.containsKey(sami.getMeasurementtool()))
							mmtsSubstances.put(sami.getMeasurementtool(), new ArrayList<String>());
						if(!mmtsSubstances.get(sami.getMeasurementtool()).contains(si.getName()))
							mmtsSubstances.get(sami.getMeasurementtool()).add(si.getName());
					}

		
		
		plants = sortArray(plants);
		mmts = sortArray(mmts);
		substances = sortArray(substances);
		
		for(int i = times.size(); i > 1; i--)
			for(int j = 0; j < i-1; j++)
			{
				if(times.get(j) > times.get(j+1))
				{
					int tmp = times.get(j);
					times.set(j, times.get(j+1));
					times.set(j+1, tmp);
				}
			}
		
		int rows = 0;
		int columns = 3;

		for(String plant : plants)
			for(String mmt : mmts)
				if(data.get(plant).containsKey(mmt))
				for(int time : times)
					if(data.get(plant).get(mmt).containsKey(time))
					{
						int maxRow = 0;
						for(String substance : substances)
							if(data.get(plant).get(mmt).get(time).containsKey(substance))
								maxRow = Math.max(maxRow, data.get(plant).get(mmt).get(time).get(substance).messurements.size());
						rows+=maxRow;
					}			
						

		for(String mmt : mmts)
		{
			for(String substance : substances)
			{
				if(mmtsSubstances.get(mmt).contains(substance))
				{
					columns++;
				}
			}	
		}
		dataset = new Object[rows][columns];
		tooltips = new String[rows][columns];
		columnHeader = new String[columns];
		columnHeader[0] = "plant";
		columnHeader[1] = "time";
		columnHeader[2] = "replicate";
		
		int currRow = 0;
		for(String plant : plants)
		{
			int currCol = 3;
			for(String mmt : mmts)
			{
				if(data.get(plant).containsKey(mmt))
				{
					for(int time : times)
					{
						if(data.get(plant).get(mmt).containsKey(time))
						{
							int maxNumMess = 0;
							int addColInd = 0;
							for(String substance : substances)
							{
								
								if(data.get(plant).get(mmt).get(time).containsKey(substance))
								{
									dataField df = data.get(plant).get(mmt).get(time).get(substance);
									int addRowInd = 0;
									for(Double d : df.messurements)
									{
										dataset[currRow+addRowInd][0] = plant;
										dataset[currRow+addRowInd][1] = df.time;
										dataset[currRow+addRowInd][2] = addRowInd + 1;
										dataset[currRow+addRowInd][currCol+addColInd] = d;
										tooltips[currRow+addRowInd][currCol+addColInd] = df.tooltip;
										addRowInd++;
									}
									columnHeader[currCol+addColInd] = substance;
									maxNumMess = Math.max(maxNumMess, df.messurements.size());
									addColInd++;
								}
							}
							currRow+=maxNumMess;
						}
					}
				}
				currCol+=mmtsSubstances.get(mmt).size();
			}
		}
	}	
	
	private ArrayList<String> sortArray(ArrayList<String> al)
	{
		for(int i = al.size(); i > 1; i--)
		{
			for(int j = 0; j < i-1; j++)
			{
				if(al.get(j).compareTo(al.get(j+1)) > 0)
				{
					String tmp = al.get(j);
					al.set(j, al.get(j+1));
					al.set(j+1, tmp);
				}
			}
		}
		return al;
		
	}	
	
	public void graphClosed()
	{
		expInts = null;
		if(tab != null)
			tab.setGraph(null);
	}
	
	/**
	 * Funktion gibt das JPanel zur�ck, dass die Eingabe-Variablen enth�lt
	 * @return JPanel mit Variablen
	 */
	public JPanel getInputVariablePanel()
	{
		return getVariablePanel(inputVariables, "input");
	}
//    TODO: Output-Variablen	
//	public JPanel getOutputVariablePanel()
//	{
//		return getVariablePanel(outputVariables, "output");
//	}
	
	public boolean isScriptLoaded()
	{
		return script != null;
	}
	
	public boolean isGraphDataLoaded()
	{
		return expInts != null;
	}
	
	/**
	 * Funktion die die Variablen-Auswahl-Panel definiert
	 * @param variables: Liste aller vorhandenen Variablen
	 * @param inout: Angabe, ob es sich um das Ein- oder das Ausgabepanel handelt
	 * @return JPanel mit Variablenauswahl
	 */
	private JPanel getVariablePanel(final ArrayList<RVariable> variables, final String inout)
	{
		JPanel panel = new JPanel(new BorderLayout());
		
		Object[][] data = new Object[variables.size()][4];
		String[] columnNames = {"name", "type", "set data", "is set"};
		
		for(int i = 0; i < variables.size(); i++)
		{
			data[i][0] = variables.get(i).getName();
			data[i][1] = variables.get(i).getType();	
			TableButton button = new TableButton();
			data[i][2] = button;
			data[i][3] = new Boolean(false);
		}
		
		final myTableModel model= new myTableModel(data, columnNames); 
		final JTable table = new JTable(model);
		table.setName(inout+"Table");
				
		table.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());
                if (table.getValueAt(row, column) instanceof TableButton) {
                	if(expInts == null)
                	{
                		if(MainFrame.getInstance().getActiveSession() != null)
                		{
                			loadGraphData(MainFrame.getInstance().getActiveEditorSession().getGraph());
                		}
                		else
	                	{
	                		JOptionPane.showMessageDialog(MainFrame.getInstance(), "Please open a graph first!"
	                				, "Error", JOptionPane.ERROR_MESSAGE);
	                		return;
	                	}
                	}
                	RVariableSelectionDialog rVarSelDia = new RVariableSelectionDialog(variables.get(row), dataset, columnHeader, tooltips);
                	if(rVarSelDia.isSet())
                	{
                		model.setValueAtSave(true, row, 3);
                		variables.get(row).setData(rVarSelDia.getData());
                	}
                }
            }
		});
        table.setDefaultRenderer(Object.class, new ButtonRenderer());
		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(1).setPreferredWidth(80);
		table.getColumnModel().getColumn(1).setMaxWidth(130);		
		table.getColumnModel().getColumn(2).setPreferredWidth(60);
		table.getColumnModel().getColumn(2).setMaxWidth(70);
		table.getColumnModel().getColumn(3).setPreferredWidth(40);		
		table.getColumnModel().getColumn(3).setMaxWidth(45);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setPreferredScrollableViewportSize(new Dimension(233,150));
		
		panel.add(scrollPane, BorderLayout.CENTER);
		
		
		JButton addVar = new JButton();
		addVar.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				RVariable rv = newRVariableDialog.showNewVariableDialog();
//	            TODO: Output-Variablen
//				RVariable rv = newRVariableDialog.showNewVariableDialog(inout);
				if( rv != null)
				{
					model.addRow(rv);
					if(inout.equals("input"))
						inputVariables.add(rv);
//		            TODO: Output-Variablen
//					else
//						outputVariables.add(rv);
					model.fireTableDataChanged();
				}
			}});
		addVar.setText("add Variable");
		JButton delVar = new JButton();
		delVar.setAction(new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				model.removeRow(table.getSelectedRow());
				if(inout.equals("input"))
					inputVariables.remove(table.getSelectedRow());
//	            TODO: Output-Variablen
//				else
//					outputVariables.remove(table.getSelectedRow());
				model.fireTableDataChanged();
			}});
		delVar.setText("delete selected");
		
		JPanel buttons = new JPanel(new FlowLayout());
		buttons.add(addVar);
		buttons.add(delVar);
		panel.add(buttons, BorderLayout.SOUTH);
		
		
		return panel;		
	}
		
	/**
	 * Funktion, die das Script-File einlie�t und die entsprechenden Variablen, falls vorhanden, anlegt
	 * @param f: R-Script
	 */
	private void loadScriptFromFile(File f)
	{
		script = "";
		try 
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			while(true)
			{
				String tmp = in.readLine();
				if(tmp == null)
					break;
				script += tmp+"\n";
			}
			in.close();
			if(script.length() == 0)
			{
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "Your script file is empty!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			script = script.substring(0, script.length()-1);
			String searchString = script;
			
			ArrayList<String> typeCheck = new ArrayList<String>();
			typeCheck.add("vector"); 
			typeCheck.add("list"); 
			typeCheck.add("matrix"); 
			typeCheck.add("array"); 
			typeCheck.add("data.frame");
//          TODO: Output-Variablen
//			ArrayList<String> inoutCheck = new ArrayList<String>();
//			inoutCheck.add("input");
//			inoutCheck.add("output");
			
			ArrayList<String> multipleDefinitionErrorHelperInput = new ArrayList<String>();
			ArrayList<String> multipleDefinitionErrorHelperOutput = new ArrayList<String>();
			
			while(searchString.contains("#variable"))
			{
				searchString = searchString.substring(searchString.indexOf("#variable"));
				
				String tmp;
				if(searchString.indexOf("\n") != -1)
				{
					tmp = searchString.substring(0, searchString.indexOf("\n"));
					searchString = searchString.substring(searchString.indexOf("\n")+1);
				}
				else
				{
					tmp = searchString;
					searchString = "";
				}
				String[] temp = tmp.split(" ");
				if(temp.length < 4)
//		        TODO: Output-Variablen
//				if(temp.length < 5)
				{
					if(temp.length > 0)
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid definition of variable "+temp[1], "Warning", JOptionPane.WARNING_MESSAGE);
					continue;
				}
				String name = temp[1];
				
				if(!typeCheck.contains(temp[2].toLowerCase()))
				{
					if(temp.length > 0)
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid definition of variable "+temp[1], "Warning", JOptionPane.WARNING_MESSAGE);
					continue;
				}
				String type = temp[2];
//	            TODO: Output-Variablen
//				if(!inoutCheck.contains(temp[3].toLowerCase()))
//				{
//					if(temp.length > 0)
//						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid definition of variable "+temp[1], "Warning", JOptionPane.WARNING_MESSAGE);
//					continue;
//				}
//				String inOutput = temp[3];
				if(!tmp.contains("\""))
				{
					if(temp.length > 0)
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid definition of variable "+temp[1], "Warning", JOptionPane.WARNING_MESSAGE);
					continue;
				}
				String description = tmp.substring(tmp.indexOf("\"")+1,tmp.indexOf("\"", tmp.indexOf("\"")+1));
//	            TODO: Output-Variablen
//				if(inOutput.toLowerCase().equals("output"))
//				{
//					boolean found = false;
//					for(RVariable rv : outputVariables)
//					{
//						if(rv.getName().equals(temp[1]))
//						{
//							found = true;
//							if(!multipleDefinitionErrorHelperOutput.contains(temp[1]))
//								multipleDefinitionErrorHelperOutput.add(temp[1]);
//						}
//					}
//					if(!found)
//						outputVariables.add(new RVariable(name,type,inOutput,description));
//				}
//				else
				{
					boolean found = false;
					for(RVariable rv : inputVariables)
					{
						if(rv.getName().equals(temp[1]))
						{
							found = true;
							if(!multipleDefinitionErrorHelperInput.contains(temp[1]))
								multipleDefinitionErrorHelperInput.add(temp[1]);
						}
					}
					if(!found)
						inputVariables.add(new RVariable(name,type,description));
				}
			}
			
			if(multipleDefinitionErrorHelperInput.size() > 0 || multipleDefinitionErrorHelperOutput.size() > 0)
			{
				String errorMessage = "Multiple definitions for the following variables found:\n";
				if(multipleDefinitionErrorHelperInput.size() > 0)
				{
					errorMessage += "input:\n";
					for(int i = 0; i < multipleDefinitionErrorHelperInput.size(); i++)
					{
						errorMessage += multipleDefinitionErrorHelperInput.get(i)+"; ";
					}
					errorMessage = errorMessage.substring(0, errorMessage.length()-2)+"\n";
				}
				if(multipleDefinitionErrorHelperOutput.size() > 0)
				{
					errorMessage += "output:\n";
					for(int i = 0; i < multipleDefinitionErrorHelperOutput.size(); i++)
					{
						errorMessage += multipleDefinitionErrorHelperOutput.get(i)+"; ";
					}
					errorMessage = errorMessage.substring(0, errorMessage.length()-2)+"\n";				
				}
				errorMessage += "For each multiple definition only the first valid definition is applied";
				JOptionPane.showMessageDialog(MainFrame.getInstance(), errorMessage, "Warning", JOptionPane.WARNING_MESSAGE);
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Erweiterte JCheckboxklasse
	 * @author Torsten
	 * Wird zur Anzeige in den Tabellen ben�tigt. Ist �berlagert, um die MouseEvents beim klicken zu verhindern
	 */
	protected class KCheckBox extends JCheckBox {
		private static final long serialVersionUID = 1L;
		boolean state = true;

		public KCheckBox(boolean state) {
			super("",state);
		}
		public void processMouseEvent(MouseEvent me) {
			return;
		}
	}
	
	/**
	 * TableModel f�r die Variablenauswahl-Panel
	 * @author Torsten
	 * Stellt Funktionen zur Verf�gung, um Zeilen hinzuzuf�gen bzw. zu l�schen und setzt die Datentypen der einzelnen Spalten
	 */
	protected class myTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 1L;
				
		Object[][] data;
		String[] columnNames;
		@SuppressWarnings("rawtypes")
		Class[] types = {java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class}; 
		
		public myTableModel(Object[][] data, String[] columnNames)
		{
			this.data = data;
			this.columnNames = columnNames;
		}
		
		public boolean isCellEditable(int row, int column){
			return false;
		}
		
		public void setValueAtSave(Object value, int row, int col)
		{
    		data[row][col] = value;
    		fireTableCellUpdated(row, col);			
		}
		
        public void setValueAt(Object value, int row, int col) {
        	if(isCellEditable(row,col))
        	{
        		data[row][col] = value;
        		fireTableCellUpdated(row, col);
        	}
        }
        
        public void addRow(RVariable var)
        {
        	int datalength = data.length;
        	Object[][] newdata = new Object[datalength+1][4];
        	for(int i = 0; i < datalength; i++)
        		for(int j = 0; j < 4; j++)
        			newdata[i][j] = data[i][j];
        	newdata[datalength][0]=	var.getName();
			newdata[datalength][1] = var.getType();	
			newdata[datalength][2] = new TableButton();
			newdata[datalength][3] = new Boolean(false);
			data = newdata;
        }
        
        public void removeRow(int row)
        {
        	Object[][] newdata = new Object[data.length-1][4];
        	int index = 0;
        	for(int i = 0; i < data.length - 1; i++)
        	{
        		if(index == row)
        			index++;
        		for(int j = 0; j < 4; j++)
        			newdata[i][j] = data[index][j];
        		index++;
        	}
        	data = newdata;
        }
        

        @SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int col) {
            return types[col];
        }

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if(rowIndex < getRowCount() && columnIndex < getColumnCount() && rowIndex >= 0 && columnIndex >= 0)
				return data[rowIndex][columnIndex];
			return null;
		}	
		
        public String getColumnName(int col) {
            return columnNames[col];
        }


	}
	
	//Dummy-Klasse f�r die Button in den Tabellen
	class TableButton{}
	
	/**
	 * Renderer um Buttons in JTables anzuzeigen
	 * @author Torsten
	 *
	 */
    class ButtonRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		private JButton button = new JButton("set");
 
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            if(table.getName().equals("inputTable"))
            {
            	button.setToolTipText(inputVariables.get(row).getDescription());
            	this.setToolTipText(inputVariables.get(row).getDescription());
            }
//            TODO: Output-Variablen
//            else if(table.getName().equals("outputTable"))
//            {
//            	button.setToolTipText(outputVariables.get(row).getDescription());
//            	this.setToolTipText(outputVariables.get(row).getDescription());
//            }

            if (value instanceof TableButton)
                return button;
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            return this;
        }
    }

    /**
     * Klasse zum Abspeichern und bearbeiten der Messwerte des Graphen
     * @author Torsten
     *
     */
	class dataField
	{
		public ArrayList<Double> messurements;
		public String tooltip;
		public String time;
		
		public dataField(ArrayList<Double> messurements, String tooltip, String time)
		{
			this.messurements = messurements;
			this.tooltip = tooltip;
			this.time = time;
		}
	}
}
