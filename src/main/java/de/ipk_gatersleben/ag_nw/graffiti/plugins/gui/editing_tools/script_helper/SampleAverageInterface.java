package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Element;

public interface SampleAverageInterface extends Measurement {

	public abstract void getString(StringBuilder r);

	public abstract double getValue();

	public abstract SampleInterface getParentSample();

	public abstract void calculateValuesFromSampleData();

	public abstract double getStdDev();

	public abstract String getUnit();

	public abstract boolean setData(Element averageElement);

	public abstract void setUnit(String ownUnit);

	public abstract void setReplicateId(int replicates);

	public abstract int getReplicateID();

	public abstract void setMax(double max);

	public abstract double getMax();

	public abstract void setMin(double min);

	public abstract double getMin();

	public abstract void setStddev(double stddev);

	public abstract double getStddev();

	public abstract void setValue(double value);

	public abstract void getStringOfChildren(StringBuilder r);

	public abstract void getXMLAttributeString(StringBuilder r);

	public abstract void setAttribute(Attribute attr);

	public abstract void setDataOfChildElement(Element childElement);

	public abstract void fillAttributeMap(Map<String, Object> attributeValueMap);

}