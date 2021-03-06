/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 27.08.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.som;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Category;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;

import qmwi.kseg.som.Tools;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author Christian Klukas (c) 2004 IPK-Gatersleben
 */
public class SOMclusterAnalysis extends AbstractAlgorithm {
	int numberOfNeuronsParm = 6;
	int widthOfSOMparm = 0; // 0=quadratic
	double maxNeighbourHoodParm = 5;
	int decreaseNeighbourhoodAfterXiterationsParam = 10; // 0=off
	int typeOfNeighbourhoodFunctionParam = 2;
	int numberLearnIterationsParam = 100;
	double betaParam = 0.1;
	double gammaParam = 2;
	boolean returnNaN;
	
	boolean useSampleAverages = true;
	boolean addCentroidNodes = false;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Step 1: Train the SOM network";
	}
	
	@Override
	public Set<Category> getSetCategory() {
		return new HashSet<Category>(Arrays.asList(Category.GRAPH, Category.ANALYSIS, Category.CLUSTER));
	}
	
	@Override
	public void reset() {
		super.reset();
		// numberOfNeuronsParm=8;
		// widthOfSOMparm=0; // 0=quadratic
		// maxNeighbourHoodParm=-1;
		// decreaseNeighbourhoodAfterXiterationsParam=0; // 0=off
		// typeOfNeighbourhoodFunctionParam=1;
		// numberLearnIterationsParam=5;
		// betaParam=0.1;
		// gammaParam=2;
	}
	
	@Override
	public Parameter[] getParameters() {
		IntegerParameter numberOfNeurons = new IntegerParameter(Integer.valueOf(numberOfNeuronsParm), Integer.valueOf(1),
				Integer.valueOf(Integer.MAX_VALUE), "Neurons", "Number of target clusters (neurons)");
		
		/**
		 * Width of the SOM, 0 means quadratic (9 nodes, width=0 ==> width=3)
		 */
		IntegerParameter widthOfSOM = new IntegerParameter(Integer.valueOf(widthOfSOMparm), Integer.valueOf(0),
				Integer.valueOf(Integer.MAX_VALUE), "Number of horizontal neurons",
				"SOM neurons are initially layouted on a grid\nThis number sets the number of horizontal neurons\nIf 0, a quadratic grid is created");
		
		/**
		 * Maximum of considered neighbourhood. "<0" means without limit (whole map)
		 */
		DoubleParameter maxNeighbourHood = new DoubleParameter(maxNeighbourHoodParm, "Max.neighborhood",
				"Max. considered neighbourhood, -1=without limit");
		
		/**
		 * Reduce neighbourhood consideration (-1) after X iterations. "0" means do not
		 * reduce neighbourhood, stay at maxNeighbourHood level
		 */
		IntegerParameter decreaseNeighbourHoodAfterXiterations = new IntegerParameter(
				Integer.valueOf(decreaseNeighbourhoodAfterXiterationsParam), Integer.valueOf(0), Integer.valueOf(Integer.MAX_VALUE),
				"Reduce neighborhood", "Reduce neighborhood considerations after X iterations. 0=off");
		
		/**
		 * Number of iterations (of the sample dataset) of the learning process.
		 */
		IntegerParameter repeatLearnCount = new IntegerParameter(Integer.valueOf(numberLearnIterationsParam),
				Integer.valueOf(1), Integer.valueOf(Integer.MAX_VALUE), "Learn-Iterations",
				"Number of iterations of the learning process");
		
		// Typ der Nachbarschaftsfunktion (1=Zylinder, 2=Kegel, 3=Gauss, 4=Mexican
		// Hat, 5=Cosinus)?
		IntegerParameter typeOfNeighbourHoodFunction = new IntegerParameter(
				Integer.valueOf(typeOfNeighbourhoodFunctionParam), Integer.valueOf(1), Integer.valueOf(5), "Neighborhood-Function",
				"1=Zylinder, 2=Kegel, 3=Gauss, 4=Mexican Hat, 5=Cosinus");
		// int nachbarF = Integer.valueOf(nachbarFS).intValue();
		
		/**
		 * Beta, default=0.1
		 */
		// DoubleParameter beta = new DoubleParameter(betaParam, "Beta", "beta");
		// double betaInit = Double.valueOf(betaS).doubleValue();
		
		/**
		 * Gamma, default=2
		 */
		// DoubleParameter gamma = new DoubleParameter(gammaParam, "Gamma", "gamma");
		
		BooleanParameter disableInterpolation = new BooleanParameter(returnNaN,
				"Interpolate missing time series values",
				"If selected, measurement values from missing time points are not interpolated with the measurement value, from the preceding time point.");
		
		return new Parameter[] { numberOfNeurons, widthOfSOM, maxNeighbourHood, decreaseNeighbourHoodAfterXiterations,
				repeatLearnCount, typeOfNeighbourHoodFunction,
				// beta,
				// gamma,
				disableInterpolation, new BooleanParameter(useSampleAverages, "Use Average Sample Values",
						"<html>If enabled (default), the average sample values are used as datapoints,<br>otherwise the replicate values are used.")
				// new BooleanParameter(addCentroidNodes, "Add Centroid Nodes",
				// "If enabled (default), new graph nodes, containing a data-mapping of the
				// centroid-dataset are created.")
		};
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		numberOfNeuronsParm = ((IntegerParameter) params[i++]).getInteger().intValue();
		widthOfSOMparm = ((IntegerParameter) params[i++]).getInteger().intValue();
		if (widthOfSOMparm == 0) {
			widthOfSOMparm = Tools.getBreite(numberOfNeuronsParm);
		}
		maxNeighbourHoodParm = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		decreaseNeighbourhoodAfterXiterationsParam = ((IntegerParameter) params[i++]).getInteger().intValue();
		numberLearnIterationsParam = ((IntegerParameter) params[i++]).getInteger().intValue();
		typeOfNeighbourhoodFunctionParam = ((IntegerParameter) params[i++]).getInteger().intValue();
		// betaParam = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		// gammaParam = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		returnNaN = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		useSampleAverages = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		// addCentroidNodes = ((BooleanParameter)
		// params[i++]).getBoolean().booleanValue();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.extension.Extension#getCategory()
	 */
	@Override
	public String getCategory() {
		return "Analysis";
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (graph == null || graph.getNodes().size() <= 0)
			throw new PreconditionException("No graph available or graph empty!");
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		SOMservice mcs = new SOMservice(numberOfNeuronsParm, widthOfSOMparm, maxNeighbourHoodParm,
				decreaseNeighbourhoodAfterXiterationsParam, typeOfNeighbourhoodFunctionParam,
				numberLearnIterationsParam, betaParam, gammaParam, getSelectedOrAllGraphElements(), returnNaN,
				useSampleAverages, addCentroidNodes, graph);
		
		SOMplugin.setLastUseAverageSetting(useSampleAverages);
		
		BackgroundTaskHelper bth = new BackgroundTaskHelper(mcs, mcs, "SOM Cluster Analysis", "SOM Cluster Analysis",
				true, false);
		bth.startWork(this);
	}
}
