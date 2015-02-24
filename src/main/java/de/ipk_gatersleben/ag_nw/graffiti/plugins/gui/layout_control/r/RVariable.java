package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.r;


/**
 * Datentyp zur Verwaltung der R Variablen
 * @author Torsten
 *
 */
public class RVariable {
	String name;
	String description;
//    TODO: Output-Variablen
//	boolean inOutput; //true - input; false - output
	String type;
	
	RDataObject data;
	
	
	
	public RVariable()
	{
		name = "";
		type = "";
		description = "";
		data = new RDataObject();
	}
	
	public RVariable(String name, String type, String description)
//    TODO: Output-Variablen
//	public RVariable(String name, String type, String inOutput, String description)
	{
		this.name = name;
		this.type = type;
//        TODO: Output-Variablen
//		if(inOutput.toLowerCase().equals("input"))
//			this.inOutput = true;
//		else if(inOutput.toLowerCase().equals("output"))
//			this.inOutput = false;
		this.description = description;
		
		data = new RDataObject();
	}
	
//    TODO: Output-Variablen
//	public RVariable(String name, String type, boolean inOutput, String description)
//	{
//		this.name = name;
//		this.type = type;
//		this.inOutput = inOutput;
//		this.description = description;
//
//		data = new RDataObject();
//	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setType(String type)
	{
		this.type = type;
	}
	
	public String getType()
	{
		return type;
	}
	
//    TODO: Output-Variablen
//	public void setInOutput(String inOutput)
//	{
//		if(inOutput.toLowerCase().equals("input"))
//			this.inOutput = true;
//		else if(inOutput.toLowerCase().equals("output"))
//			this.inOutput = false;		
//	}
//	
//	public void setInOutput(boolean inOutput)
//	{
//		this.inOutput = inOutput;
//	}
//	
//	public boolean getInOutput()
//	{
//		return inOutput;
//	}
	
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public void setData(RDataObject data)
	{
		this.data = data;
	}
	
	public RDataObject getData()
	{
		return data;
	}
}
