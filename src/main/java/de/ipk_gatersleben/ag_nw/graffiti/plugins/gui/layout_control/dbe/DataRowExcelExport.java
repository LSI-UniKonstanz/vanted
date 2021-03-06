package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/***
 * Holds an ordered List of ConditionInterface, SampleInterface and
 * NumericMeasurementInterface. The List of these Data-Structure can easy use in
 * ExperimentDataFileWriter with an Iterator.
 * 
 * @author Sebastian Fröhlich
 */
public class DataRowExcelExport implements Comparable<DataRowExcelExport>, Comparator<DataRowExcelExport> {
	
	int conditionID;
	int timeID;
	int replicateID;
	HashMap<String, String> values;
	String timeUnit;
	
	public DataRowExcelExport() {
		values = new HashMap<String, String>();
	}
	
	public int getConditionID() {
		return conditionID;
	}
	
	public void setConditionID(int conditionID) {
		this.conditionID = conditionID;
	}
	
	public int getTimeID() {
		return timeID;
	}
	
	public void setTimeID(int timeID) {
		this.timeID = timeID;
	}
	
	public int getReplicateID() {
		return replicateID;
	}
	
	public void setReplicateID(int replicateID) {
		this.replicateID = replicateID;
	}
	
	public String get(String key) {
		return values.get(key);
	}
	
	public String getValue(int i) {
		return values.get(Integer.toString(i));
	}
	
	public void addValue(String substanceName, double value) {
		if (Double.isNaN(value))
			values.put(substanceName, "NaN");
		else
			values.put(substanceName, "" + value);
	}
	
	@Override
	public String toString() {
		String str = "";
		str += "condID: " + conditionID + " ; ";
		str += "timeID: " + timeID + " " + timeUnit + " ; ";
		str += "repID: " + replicateID;
		str += " value: ";
		Iterator<Entry<String, String>> itEntries = values.entrySet().iterator();
		while (itEntries.hasNext()) {
			Entry<String, String> entry = itEntries.next();
			str += entry.getKey() + "=" + entry.getValue() + "; ";
		}
		return str;
	}
	
	public String getTimeUnit() {
		return timeUnit;
	}
	
	public void setTimeUnit(String timeUnit) {
		this.timeUnit = timeUnit;
	}
	
	public HashMap<String, String> getValues() {
		return values;
	}
	
	@Override
	public int compare(DataRowExcelExport arg0, DataRowExcelExport arg1) {
		// negative -> less than
		// positive -> greater than
		// 0 -> equal;
		// global sort key: getCondition
		// second sort key: getTimeID
		// third sort key: getReplicateID
		DataRowExcelExport o1 = arg0;
		DataRowExcelExport o2 = arg1;
		if (o1.getConditionID() < o2.getConditionID())
			return -1;
		else if (o1.getConditionID() > o2.getConditionID())
			return 1;
		else {
			if (o1.getTimeID() < o2.getTimeID())
				return -1;
			else if (o1.getTimeID() > o2.getTimeID())
				return 1;
			else {
				if (o1.getReplicateID() < o2.getReplicateID())
					return -1;
				else if (o1.getReplicateID() > o2.getReplicateID())
					return 1;
				else
					return 0;
			}
		}
	}
	
	@Override
	public int compareTo(DataRowExcelExport arg0) {
		return compare(this, arg0);
	}
	
	@Override
	public boolean equals(Object arg0) {
		if (arg0 == null || !(arg0 instanceof DataRowExcelExport))
			return false;
		
		return this.compareTo((DataRowExcelExport) arg0) == 0;
	}
}
