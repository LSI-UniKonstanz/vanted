package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.xml.transform.TransformerException;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.ReleaseInfo;
import org.graffiti.plugin.XMLHelper;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.JDOM2DOM;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.TimeAndTimeUnit;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.XPathHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * @author klukas
 */
public class Experiment implements ExperimentInterface {
	
	ArrayList<SubstanceInterface> md;
	private ExperimentHeaderInterface header;
	static DataMappingTypeManagerInterface typemanager = new DataMappingTypeManager();
	
	public Experiment() {
		md = new ArrayList<SubstanceInterface>();
		header = new ExperimentHeader();
	}
	
	public Experiment(SubstanceInterface data) {
		this();
		add(data);
	}
	
	public Experiment(List<SubstanceInterface> data) {
		this();
		addAll(data);
	}
	
	public Experiment(ExperimentInterface data) {
		this();
		for (SubstanceInterface m : data)
			add(m);
	}
	
	public Experiment(Document doc) {
		this();
		addAll(getExperimentFromDOM(doc));
	}
	
	public void addAll(ExperimentInterface m) {
		md.addAll(m);
		if (isEmpty())
			setHeader(m.getHeader());
	}
	
	public Experiment filter(Collection<String> validNames, Collection<String> validTimes) {
		HashSet<String> vn = new HashSet<String>(validNames);
		HashSet<String> vt = new HashSet<String>(validTimes);
		Experiment filtered = clone();
		ArrayList<ConditionInterface> delSeries = new ArrayList<ConditionInterface>();
		ArrayList<SampleInterface> delSamples = new ArrayList<SampleInterface>();
		for (SubstanceInterface m : filtered) {
			for (ConditionInterface s : m) {
				if (!vn.contains(s.getConditionName()))
					delSeries.add(s);
				else
					for (SampleInterface ss : s) {
						TimeAndTimeUnit tt = new TimeAndTimeUnit("" + ss.getTime(), ss.getTimeUnit());
						if (!vt.contains(tt.toString()))
							delSamples.add(ss);
					}
			}
		}
		for (ConditionInterface sd : delSeries)
			sd.getParentSubstance().remove(sd);
		for (SampleInterface sd : delSamples)
			sd.getParentCondition().remove(sd);
		return filtered;
	}
	
	public String getName() {
		if (isEmpty())
			return header.getExperimentname();
		else
			return findHeader(null, this).getExperimentname();
	}
	
	public String getRemark() {
		if (isEmpty())
			return header.getRemark();
		else
			return findHeader(header, this).getRemark();
	}
	
	public String getCoordinator() {
		if (isEmpty())
			return header.getCoordinator();
		else
			return findHeader(header, this).getCoordinator();
	}
	
	public Date getImportDate() {
		if (isEmpty())
			return header.getImportdate();
		else
			return findHeader(header, this).getImportdate();
	}
	
	public Date getStartDate() {
		if (isEmpty())
			return header.getStartdate();
		else
			return findHeader(header, this).getStartdate();
	}
	
	public Collection<ExperimentInterface> splitOldStyle() {
		HashMap<String, ExperimentInterface> result = new HashMap<String, ExperimentInterface>();
		
		ArrayList<Document> dl = Experiment.getDocuments(this);
		for (Document d : dl) {
			ExperimentInterface e = new Experiment(d);
			String experimentName = e.getName();
			result.put(experimentName, e);
		}
		return result.values();
	}
	
	public Collection<ExperimentInterface> split() {
		HashMap<String, ExperimentInterface> result = new HashMap<String, ExperimentInterface>();
		
		HashMap<String, ArrayList<ConditionInterface>> expname2cons = new HashMap<String, ArrayList<ConditionInterface>>();
		
		// first: group all conditions with the same experiment header
		for (SubstanceInterface s : this)
			for (ConditionInterface c : s) {
				if (!expname2cons.containsKey(c.getExperimentName()))
					expname2cons.put(c.getExperimentName(), new ArrayList<ConditionInterface>());
				expname2cons.get(c.getExperimentName()).add(c);
			}
		
		// for (String expn : expname2cons.keySet()) {
		// int cnt = 0;
		// for (ConditionInterface con : expname2cons.get(expn))
		// for (SampleInterface sam : con)
		// cnt += sam.size();
		// System.out.println(expn + " " + cnt);
		// }
		
		// second: clone all conditions (including their substance and all samples
		// recursively
		for (String expn : expname2cons.keySet()) {
			ExperimentInterface e = new Experiment();
			
			for (ConditionInterface con : expname2cons.get(expn)) {
				SubstanceInterface sub = con.getParentSubstance().clone();
				ConditionInterface c = con.clone(sub);
				sub.add(c);
				for (SampleInterface sam : con) {
					SampleInterface samnew = sam.clone(con);
					c.add(samnew);
					for (NumericMeasurementInterface m : sam) {
						NumericMeasurementInterface mnew = m.clone(samnew);
						samnew.add(mnew);
					}
				}
				Substance.addAndMerge(e, sub);
			}
			result.put(expn, e);
		}
		if (result.size() <= 0 && isEmpty())
			result.put("doesnt matter", this.clone());
		return result.values();
	}
	
	public List<NumericMeasurementInterface> getAllMeasurements() {
		List<NumericMeasurementInterface> list = new ArrayList<NumericMeasurementInterface>();
		for (SubstanceInterface sub : md)
			for (ConditionInterface series : sub)
				for (SampleInterface sample : series)
					for (NumericMeasurementInterface meas : sample)
						list.add(meas);
					
		return list;
	}
	
	public void setHeader(ExperimentHeaderInterface header) {
		this.header = header;
		if (!isEmpty())
			for (SubstanceInterface m : this)
				for (ConditionInterface s : m)
					s.setExperimentInfo(header);
	}
	
	public ExperimentHeaderInterface getHeader() {
		if (isEmpty())
			return header;
		else {
			for (SubstanceInterface m : this)
				for (ConditionInterface s : m)
					return s.getExperimentHeader();
			ErrorMsg.addErrorMessage("Internal error, isEmpty false, but no seriesData!");
			return null;
		}
	}
	
	public Collection<ExperimentHeaderInterface> getHeaders() {
		if (isEmpty()) {
			ArrayList<ExperimentHeaderInterface> result = new ArrayList<ExperimentHeaderInterface>();
			result.add(getHeader());
			return result;
		} else {
			ArrayList<ExperimentHeaderInterface> result = new ArrayList<ExperimentHeaderInterface>();
			if (header != null)
				result.add(header);
			for (SubstanceInterface m : this)
				for (ConditionInterface s : m)
					result.add(s.getExperimentHeader());
			return result;
		}
	}
	
	public static Document getEmptyDocument(ExperimentHeaderInterface header) {
		StringBuilder r = new StringBuilder();
		r.append("</measurements>");
		r.append("</experimentdata>");
		StringBuilder r2 = new StringBuilder();
		r2.append("<experimentdata>");
		
		r2.append(header.toString());
		r2.append("<measurements>");
		
		r2.append(r);
		
		return XMLHelper.getDocumentFromXMLstring(r2.toString());
	}
	
	public static ArrayList<Document> getDocuments(ExperimentInterface mappingDataList,
			BackgroundTaskStatusProviderSupportingExternalCall status, boolean mergeExperimentsReturnOnlyOne) {
		ArrayList<Document> docList = new ArrayList<Document>();
		for (String s : getStrings(mappingDataList, status, mergeExperimentsReturnOnlyOne))
			docList.add(XMLHelper.getDocumentFromXMLstring(s));
		return docList;
	}
	
	public static ArrayList<String> getStrings(ExperimentInterface mappingDataList,
			BackgroundTaskStatusProviderSupportingExternalCall status, boolean mergeExperimentsReturnOnlyOne) {
		
		HashMap<String, LinkedHashMap<String, LinkedHashMap<String, ConditionInterface>>> experimentName2substanceName2Conditions = new HashMap<String, LinkedHashMap<String, LinkedHashMap<String, ConditionInterface>>>();
		
		String experimentNameForAll = null;
		
		// HashMap<String, Integer> conditionOffsetsForExperiments = new HashMap<String,
		// Integer>();
		// int experimentINDEX = 0;
		// int experimentINDEXoffset = 50000; // maximale anzahl von experimenten
		// und/oder conditions !!!
		
		if (status != null)
			status.setCurrentStatusText2("Extracting metadata from elements");
		if (mappingDataList != null)
			for (SubstanceInterface substance : mappingDataList) {
				String substanceName = substance.getName();
				if (substanceName == null)
					ErrorMsg.addErrorMessage("INTERNAL ERROR: Substance-Name is NULL!");
				for (ConditionInterface condition : substance) {
					String expName = condition.getExperimentName();
					if (mergeExperimentsReturnOnlyOne && experimentNameForAll == null)
						experimentNameForAll = expName;
					if (mergeExperimentsReturnOnlyOne)
						expName = experimentNameForAll;
					
					// if
					// (!conditionOffsetsForExperiments.containsKey(condition.getExperimentName()))
					// {
					// conditionOffsetsForExperiments.put(condition.getExperimentName(),
					// (experimentINDEX++)
					// * experimentINDEXoffset);
					// }
					
					if (!experimentName2substanceName2Conditions.containsKey(expName))
						experimentName2substanceName2Conditions.put(expName,
								new LinkedHashMap<String, LinkedHashMap<String, ConditionInterface>>());
					
					if (!experimentName2substanceName2Conditions.get(expName).containsKey(substanceName))
						experimentName2substanceName2Conditions.get(expName).put(substanceName,
								new LinkedHashMap<String, ConditionInterface>());
					
					if (!experimentName2substanceName2Conditions.get(expName).get(substanceName)
							.containsKey(((Condition) condition).getConditionName(false))) {
						experimentName2substanceName2Conditions.get(expName).get(substanceName)
								.put(((Condition) condition).getConditionName(false), condition);
					} else {
						experimentName2substanceName2Conditions.get(expName).get(substanceName)
								.get(((Condition) condition).getConditionName(false)).addAll(condition);
					}
				}
			}
		
		ArrayList<String> docList = new ArrayList<String>();
		if (status != null)
			status.setCurrentStatusText2("Creating experiment header");
		if (experimentName2substanceName2Conditions.isEmpty()) {
			
			StringBuilder r = new StringBuilder();
			int measurementcount = 0;
			
			r.append("</measurements>");
			r.append("</experimentdata>");
			
			StringBuilder r2 = new StringBuilder();
			r2.append("<experimentdata>");
			
			ExperimentHeaderInterface eh = findHeader(mappingDataList != null ? mappingDataList.getHeader() : null,
					mappingDataList);
			eh.toString(r2, measurementcount);
			r2.append("<measurements>");
			
			r2.append(r);
			
			docList.add(r2.toString());
		} else {
			for (String expName : experimentName2substanceName2Conditions.keySet()) {
				
				LinkedHashMap<String, LinkedHashMap<String, ConditionInterface>> substances2conditions = experimentName2substanceName2Conditions
						.get(expName);
				ConditionInterface c1 = substances2conditions.values().iterator().next().values().iterator().next();
				
				StringBuilder r = new StringBuilder();
				int measurementcount = 0;
				for (String substance : substances2conditions.keySet()) {
					
					SubstanceInterface s = null;
					
					for (SubstanceInterface sub : mappingDataList)
						if (sub.getName().equals(substance))
							s = sub;
						
					s.getSubstanceString(r);
					for (ConditionInterface sd : substances2conditions.get(substance).values()) {
						// int oldid = sd.getRowId();
						// sd.setRowId(oldid +
						// conditionOffsetsForExperiments.get(sd.getExperimentName()));
						sd.getStringForDocument(r);
						// sd.setRowId(oldid);
						for (SampleInterface sample : sd)
							measurementcount += sample.size();
					}
					r.append("</substance>");
				}
				
				r.append("</measurements>");
				r.append("</experimentdata>");
				
				StringBuilder r2 = new StringBuilder();
				r2.append("<experimentdata>");
				
				ExperimentHeaderInterface eh = findHeader(c1.getExperimentHeader(), mappingDataList);
				
				eh.toString(r2, measurementcount);
				r2.append("<measurements>");
				
				r2.append(r);
				
				docList.add(r2.toString());
			}
		}
		
		return docList;
	}
	
	private static ExperimentHeaderInterface findHeader(ExperimentHeaderInterface suggestion,
			ExperimentInterface mappingDataList) {
		if (mappingDataList == null || mappingDataList.isEmpty()) {
			return new ExperimentHeader(suggestion);
		} else {
			for (SubstanceInterface s : mappingDataList)
				for (ConditionInterface c : s)
					if (c.getExperimentHeader() != null)
						return c.getExperimentHeader();
		}
		return null;
	}
	
	/**
	 * &lt;experimentdata&gt; &lt;experiment experimentid="-1"&gt;
	 * &lt;experimentname&gt;Gluthation&lt;/experimentname&gt; &lt;remark&gt;SXD plants&lt;/remark&gt;
	 * &lt;coordinator&gt;Mohammad Hajirezaei&lt;/coordinator&gt; &lt;excelfileid/&gt;
	 * &lt;importusername/&gt; &lt;importdate&gt;Tue Mar 10 11:39:36 CET 2009&lt;/importdate&gt;
	 * &lt;startdate&gt;Sun Jan 18 00:00:00 CET 2004&lt;/startdate&gt;
	 * &lt;measurements&gt;92&lt;/measurements&gt; &lt;imagefiles&gt;0&lt;/imagefiles&gt; &lt;sizekb&gt;0&lt;/sizekb&gt;
	 * &lt;/experiment&gt; &lt;measurements&gt; &lt;substance id="column 6" name="Cys"&gt; ...
	 * 
	 * @param mappingDataList
	 * @param status
	 */
	public static ArrayList<Document> getDocuments(ExperimentInterface mappingDataList) {
		return getDocuments(mappingDataList, null, false);
	}
	
	public static String getString(ExperimentInterface mappingDataList) {
		ArrayList<String> res = getStrings(mappingDataList, null, true);
		if (res.size() > 1)
			ErrorMsg.addErrorMessage(
					"Internal error: request for merged string representation returned more than one string!");
		return res.iterator().next();
	}
	
	public static void setTypeManager(DataMappingTypeManagerInterface m) {
		Experiment.typemanager = m;
	}
	
	public static DataMappingTypeManagerInterface getTypeManager() {
		return Experiment.typemanager;
	}
	
	public static Experiment getExperimentFromDOM(org.w3c.dom.Document doc) {
		return getExperimentFromJDOM(JDOM2DOM.getJDOMfromDOM(doc));
	}
	
	// public static List<Substance> getExperimentFromDocuments(List<Document>
	// documents) {
	//
	// List<org.jdom.Document> list = new ArrayList<org.jdom.Document>();
	// for(Document doc : documents)
	// list.add(JDOM2DOM.getJDOMfromDOM(doc));
	//
	// return getExperiment(list);
	// }
	
	public static ExperimentInterface getExperiment(List<org.jdom2.Document> documents) {
		List<Experiment> results = new ArrayList<Experiment>();
		for (org.jdom2.Document doc : documents)
			results.add(Substance.getData(doc.getRootElement()));
		
		if (results.size() == 0)
			return new Experiment();
		if (results.size() == 1) {
			return results.get(0);
		} else {
			Experiment mainDataset = results.get(0);
			for (int i = 1; i < results.size(); i++) {
				ExperimentInterface toBeAdded = results.get(i);
				Experiment.addAndMerge(mainDataset, toBeAdded);
			}
			return mainDataset;
		}
	}
	
	public static Experiment getExperimentFromJDOM(org.jdom2.Document doc) {
		Experiment e = Substance.getData(doc.getRootElement());
		if (e.isEmpty()) {
			Element temp = doc.getRootElement().getChild("experiment").getChild("experimentname");
			if (temp != null)
				e.getHeader().setExperimentname(temp.getValue());
		}
		return e;
	}
	
	/*
	 * Delegate methods
	 */
	
	public boolean isEmpty() {
		for (SubstanceInterface s : this)
			for (ConditionInterface c : s)
				return c == null;
			
		return true;
	}
	
	public void add(int index, SubstanceInterface element) {
		md.add(index, element);
	}
	
	public boolean add(SubstanceInterface e) {
		return md.add(e);
	}
	
	public boolean addAll(Collection<? extends SubstanceInterface> c) {
		return md.addAll(c);
	}
	
	public boolean addAll(int index, Collection<? extends SubstanceInterface> c) {
		return md.addAll(index, c);
	}
	
	public boolean contains(Object o) {
		return md.contains(o);
	}
	
	public boolean containsAll(Collection<?> arg0) {
		return md.containsAll(arg0);
	}
	
	public void ensureCapacity(int minCapacity) {
		md.ensureCapacity(minCapacity);
	}
	
	public int indexOf(Object o) {
		return md.indexOf(o);
	}
	
	public int lastIndexOf(Object o) {
		return md.lastIndexOf(o);
	}
	
	public ListIterator<SubstanceInterface> listIterator() {
		return md.listIterator();
	}
	
	public ListIterator<SubstanceInterface> listIterator(int index) {
		return md.listIterator(index);
	}
	
	public SubstanceInterface remove(int index) {
		return md.remove(index);
	}
	
	public boolean remove(Object o) {
		boolean success = md.remove(o);
		if (size() == 0 && o instanceof Substance) {
			for (ConditionInterface c : (SubstanceInterface) o) {
				setHeader(c.getExperimentHeader());
				break;
			}
		}
		return success;
	}
	
	public boolean removeAll(Collection<?> arg0) {
		return md.removeAll(arg0);
	}
	
	public boolean retainAll(Collection<?> arg0) {
		return md.retainAll(arg0);
	}
	
	public SubstanceInterface set(int index, SubstanceInterface element) {
		return md.set(index, element);
	}
	
	public List<SubstanceInterface> subList(int fromIndex, int toIndex) {
		return md.subList(fromIndex, toIndex);
	}
	
	public Object[] toArray() {
		return md.toArray();
	}
	
	public <T> T[] toArray(T[] a) {
		return md.toArray(a);
	}
	
	@Override
	public String toString() {
		try {
			return toStringWithErrorThrowing();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return "<html>Could not retrieve complete document!<br>" + md.toString();
		}
	}
	
	public String toStringWithErrorThrowing() throws IOException, TransformerException, JDOMException {
		return getString(this);
	}
	
	public void trimToSize() {
		md.trimToSize();
	}
	
	public int size() {
		return md.size();
	}
	
	public SubstanceInterface get(int index) {
		return md.get(index);
	}
	
	public Iterator<SubstanceInterface> iterator() {
		return md.iterator();
	}
	
	public void clear() {
		md.clear();
	}
	
	public String getSequence() {
		if (isEmpty())
			return header.getSequence();
		else
			return md.iterator().next().iterator().next().getSequence();
	}
	
	public void fillAttributeMap(Map<String, Object> attributeValueMap) {
		getHeader().fillAttributeMap(attributeValueMap, Experiment.getNumberOfMeasurementValues(this));
	}
	
	public void getStringOfChildren(StringBuilder r) {
		// empty
	}
	
	public void getXMLAttributeString(StringBuilder r) {
		// empty
	}
	
	public void setAttribute(Attribute attr) {
		// empty
	}
	
	public boolean setData(Element xmlElement) {
		return false;
	}
	
	public void setDataOfChildElement(Element childElement) {
		// empty
	}
	
	public void writeToFile(File targetDir, boolean asDocument) {
		
		if (asDocument) {
			int cnt = 1;
			for (Document doc : getDocuments(this)) {
				String name = "Omics-Data " + (cnt++);
				Node temp = doc.getElementsByTagName("experimentname").item(0).getFirstChild();
				if (temp != null)
					name = temp.getNodeValue();
				XMLHelper.writeXMLDataToFile(doc, targetDir.getAbsolutePath() + "/" + name + ".xml");
			}
		} else
			try {
				TextFile.write(targetDir.getAbsolutePath() + ReleaseInfo.getFileSeparator() + getName() + ".xml",
						toString());
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e);
			}
	}
	
	public Experiment cloneOldStyle() {
		Experiment clone = null;
		ArrayList<Document> dl = Experiment.getDocuments(this);
		for (Document d : dl) {
			if (clone == null)
				clone = new Experiment(d);
			else
				clone.addAll(new Experiment(d));
		}
		for (ExperimentHeaderInterface eh : clone.getHeaders()) {
			eh.setImportusergroup(getHeader().getImportusergroup());
			eh.setImportusername(getHeader().getImportusername());
		}
		return clone;
	}
	
	@Override
	public Experiment clone() {
		Experiment clone = new Experiment();
		clone.header = header.clone();
		for (SubstanceInterface sub : md) {
			SubstanceInterface newsub = sub.clone();
			clone.add(newsub);
			for (ConditionInterface c : sub) {
				ConditionInterface newc = c.clone(newsub);
				newsub.add(newc);
				for (SampleInterface s : c) {
					SampleInterface news = s.clone(newc);
					newc.add(news);
					for (NumericMeasurementInterface m : s)
						news.add(m.clone(news));
				}
			}
		}
		return clone;
	}
	
	/**
	 * Use experiment.addAndMerge instead.
	 */
	@Deprecated
	public static void addAndMerge(ExperimentInterface result, ExperimentInterface toBeAdded) {
		result.addAndMerge(toBeAdded);
	}
	
	@Override
	public void addAndMerge(ExperimentInterface toBeAdded) {
		if (isEmpty() && toBeAdded.isEmpty())
			header = toBeAdded.getHeader().clone();
		else
			for (SubstanceInterface tobeMerged : toBeAdded)
				Substance.addAndMerge(this, tobeMerged);
	}
	
	public static String[] getTimes(ExperimentInterface experimentData) {
		TreeSet<String> times = new TreeSet<String>();
		for (SubstanceInterface md : experimentData) {
			for (ConditionInterface sd : md) {
				for (SampleInterface s : sd) {
					String t = s.getSampleTime();
					if (!t.equals("-1 -1"))
						times.add(t);
				}
			}
		}
		if (times.size() > 0)
			return times.toArray(new String[] {});
		else
			return new String[] { XPathHelper.noGivenTimeStringConstant };
	}
	
	public static String[] getConditionsAsString(ExperimentInterface experimentData) {
		TreeSet<String> plants = new TreeSet<String>();
		for (SubstanceInterface md : experimentData) {
			for (ConditionInterface sd : md) {
				plants.add(sd.getConditionName());
			}
		}
		if (plants.size() > 0)
			return plants.toArray(new String[] {});
		else
			return new String[] { XPathHelper.noGivenTimeStringConstant };
	}
	
	public static boolean isReplicateDataMissing(ExperimentInterface md) {
		boolean oneWithMoreThanOne = false;
		all: for (SubstanceInterface m : md)
			for (ConditionInterface c : m)
				for (SampleInterface s : c) {
					if (s.size() > 0) {
						oneWithMoreThanOne = true;
						break all;
					}
				}
		return !oneWithMoreThanOne;
	}
	
	public static boolean isBiologicalAndTechnicalReplicateDataAvailable(ExperimentInterface md) {
		for (SubstanceInterface m : md) {
			ArrayList<String> condnames = new ArrayList<String>();
			for (ConditionInterface c : m) {
				String cname = c.getSpecies() + c.getGenotype() + c.getVariety() + c.getGrowthconditions()
						+ c.getTreatment();
				if (condnames.contains(cname))
					return true;
				else
					condnames.add(cname);
			}
		}
		
		return false;
	}
	
	/**
	 * Use experiment.getNumberOfMeasurementValues() instead.
	 */
	@Deprecated
	public static int getNumberOfMeasurementValues(ExperimentInterface md) {
		int res = 0;
		for (SubstanceInterface m : md) {
			res += m.getDataPointCount(false);
		}
		return res;
	}
	
	@Override
	public int getNumberOfMeasurementValues() {
		int res = 0;
		for (SubstanceInterface m : this) {
			res += m.getDataPointCount(false);
		}
		return res;
	}
	
	/**
	 * Use experiment.getMeasurementValuesSum() instead.
	 */
	@Deprecated
	public static double getMeasurementValuesSum(ExperimentInterface md) {
		double res = 0;
		for (SubstanceInterface m : md) {
			res += m.getSum();
		}
		return res;
	}
	
	public double getMeasurementValuesSum() {
		double res = 0;
		for (SubstanceInterface m : this) {
			res += m.getSum();
		}
		return res;
	}
	
	@Override
	public void mergeBiologicalReplicates(BackgroundTaskStatusProviderSupportingExternalCall status) {
		int cnt = 0, all = size();
		for (SubstanceInterface m : this) {
			if (status != null) {
				status.setCurrentStatusText1("Processing substance " + m.getName());
				status.setCurrentStatusValue(100 * (cnt++) / all);
			}
			HashMap<String, ArrayList<ConditionInterface>> condnames = new HashMap<String, ArrayList<ConditionInterface>>();
			for (ConditionInterface c : m) {
				String cname = c.getSpecies() + c.getGenotype() + c.getVariety() + c.getGrowthconditions()
						+ c.getTreatment();
				if (!condnames.containsKey(cname))
					condnames.put(cname, new ArrayList<ConditionInterface>());
				condnames.get(cname).add(c);
				for (SampleInterface sam : c)
					if (sam.size() > 0) {
						double value = sam.getSampleAverage().getValue();
						// remove all technical replicates and replace by mean value
						NumericMeasurementInterface val = sam.iterator().next().clone(sam);
						val.setValue(value);
						sam.clear();
						sam.add(val);
					}
			}
			
			for (Entry<String, ArrayList<ConditionInterface>> entry : condnames.entrySet())
				if (entry.getValue().size() > 1) {
					Iterator<ConditionInterface> it = entry.getValue().iterator();
					ConditionInterface firstcondition = it.next();
					
					while (it.hasNext()) {
						ConditionInterface tobemerged = it.next();
						SampleInterface savesamfirst = null;
						
						// temporary list of samples, that couldn't be found in the "master" condition
						// used to later add to "firstcondition"
						ArrayList<SampleInterface> tobelatermerged = new ArrayList<SampleInterface>();
						for (SampleInterface sammerged : tobemerged) {
							for (SampleInterface samfirst : firstcondition)
								if (isSampleEqualForMerge(samfirst, sammerged)) {
									savesamfirst = samfirst;
									break;
								}
							
							if (savesamfirst != null) {
								if (sammerged.iterator().hasNext()) {
									NumericMeasurementInterface val = sammerged.iterator().next();
									val.setParentSample(savesamfirst);
									savesamfirst.add(val);
								}
							} else {
								savesamfirst = sammerged;
								sammerged.setParent(firstcondition);
								tobelatermerged.add(sammerged);
							}
						}
						
						firstcondition.addAll(tobelatermerged);
						
						if (tobemerged.getParentSubstance() != null)
							tobemerged.getParentSubstance().remove(tobemerged);
						tobemerged.setParent(null);
					}
					
					for (SampleInterface sam : firstcondition)
						sam.recalculateSampleAverage();
					
				}
		}
		if (status != null) {
			status.setCurrentStatusText1("Processing finished!");
			status.setCurrentStatusText2("All biological replicates were merged");
		}
		
	}
	
	private static boolean isSampleEqualForMerge(SampleInterface sample1, SampleInterface sample2) {
		if (sample1 == null || sample2 == null)
			return false;
		String s1 = sample1.getMeasurementtool() + ";" + sample1.getTime() + ";" + sample1.getTimeUnit() + ";"
				+ sample1.getTtestInfo().name();
		String s2 = sample2.getMeasurementtool() + ";" + sample2.getTime() + ";" + sample2.getTimeUnit() + ";"
				+ sample2.getTtestInfo().name();
		return s1.equals(s2);
		
	}
}
