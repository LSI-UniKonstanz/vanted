/**
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder;

import org.AttributeHelper;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import edu.monash.vanted.test.graph.TestAdjGraphMatrix;

/**
 * @author matthiak
 */
public class UllmannSubraphIsomAdjMatrixState
		implements State
{
	private static final Logger logger = Logger.getLogger(UllmannSubraphIsomAdjMatrixState.class);
	
	static {
		logger.setLevel(Level.INFO);
	}
	
	byte[][] adjMatrixPattern;
	
	byte[][] adjMatrixGraph;
	
	boolean[][] compatibilityMatrix;
	
	Node[] arrayNodesPattern;
	int lenArrayNodesPattern;
	
	Node[] arrayNodesGraph;
	int lenArrayNodesGraph;
	
	int curLengthPattern;
	
	int idxNextPatternNode;
	int idxNextGraphNode;
	
	private int[] matchNodesInGraphForPattern;
	private int[] matchNodesInPatternForGraph;
	
	Graph patternGraph;
	Graph targetGraph;
	boolean ignoreEdgeDirection;
	
	int currentRecursionLevel;
	
	/**
	 * 
	 */
	public UllmannSubraphIsomAdjMatrixState(Graph patternGraph, Graph targetGraph, boolean ignoreEdgeDirection) {
		this.patternGraph = patternGraph;
		this.targetGraph = targetGraph;
		this.ignoreEdgeDirection = ignoreEdgeDirection;
		
		this.curLengthPattern = 0;
		this.idxNextGraphNode = State.NULL_NODE;
		this.idxNextPatternNode = State.NULL_NODE;
		this.currentRecursionLevel = 0;
		
		this.lenArrayNodesGraph = targetGraph.getNodes().size();
		this.lenArrayNodesPattern = patternGraph.getNodes().size();
		
//		logger.setLevel(Level.DEBUG);
	}
	
	public byte[][] getAdjMatrixPattern() {
		if (adjMatrixPattern == null) {
			adjMatrixPattern = GraphHelper.createAdjacencyMatrix(getPatternGraph(), !ignoreEdgeDirection);
			if (logger.getLevel() == Level.DEBUG)
				TestAdjGraphMatrix.printAdjacencyMatrix(getArrayNodesPattern(), adjMatrixPattern);
		}
		return adjMatrixPattern;
	}
	
	public byte[][] getAdjMatrixGraph() {
		if (adjMatrixGraph == null) {
			adjMatrixGraph = GraphHelper.createAdjacencyMatrix(getTargetGraph(), !ignoreEdgeDirection);
			if (logger.getLevel() == Level.DEBUG)
				TestAdjGraphMatrix.printAdjacencyMatrix(getArrayNodesGraph(), adjMatrixGraph);
		}
		return adjMatrixGraph;
	}
	
	public boolean[][] getCompatibilityMatrix() {
		if (compatibilityMatrix == null)
			compatibilityMatrix = createCompatibiliyMatrix();
		return compatibilityMatrix;
	}
	
	/**
	 * @return the matchNodesInGraphForPattern
	 */
	public int[] getMatchNodesInGraphForPattern() {
		if (matchNodesInGraphForPattern == null) {
			matchNodesInGraphForPattern = new int[lenArrayNodesPattern];
			for (int i = 0; i < lenArrayNodesPattern; i++)
				matchNodesInGraphForPattern[i] = -1;
		}
		return matchNodesInGraphForPattern;
	}
	
	/**
	 * @return the matchNodesInPatternForGraph
	 */
	public int[] getMatchNodesInPatternForGraph() {
		if (matchNodesInPatternForGraph == null) {
			matchNodesInPatternForGraph = new int[lenArrayNodesGraph];
			for (int i = 0; i < lenArrayNodesGraph; i++)
				matchNodesInPatternForGraph[i] = -1;
			
		}
		return matchNodesInPatternForGraph;
	}
	
	Node[] getArrayNodesGraph() {
		if (arrayNodesGraph == null)
			arrayNodesGraph = targetGraph.getNodes().toArray(new Node[lenArrayNodesGraph]);
		return arrayNodesGraph;
	}
	
	Node[] getArrayNodesPattern() {
		if (arrayNodesPattern == null)
			arrayNodesPattern = patternGraph.getNodes().toArray(new Node[lenArrayNodesPattern]);
		return arrayNodesPattern;
	}
	
	/**
	 * @return
	 */
	private boolean[][] createCompatibiliyMatrix() {
		
		boolean[][] compatibilityMatrix = new boolean[getArrayNodesPattern().length][getArrayNodesGraph().length];
		
		for (int idxPat = 0; idxPat < lenArrayNodesPattern; idxPat++) {
			
			int inDegreePatternNode = getArrayNodesPattern()[idxPat].getInDegree();
			int outDegreePatternNode = getArrayNodesPattern()[idxPat].getOutDegree();
			
			for (int idxGraph = 0; idxGraph < lenArrayNodesGraph; idxGraph++) {
				
				int inDegreeGraphNode = getArrayNodesGraph()[idxGraph].getInDegree();
				int outDegreeGraphNode = getArrayNodesGraph()[idxGraph].getOutDegree();
				
				if (ignoreEdgeDirection) {
					if ((inDegreePatternNode + outDegreePatternNode) <= (inDegreeGraphNode + outDegreeGraphNode))
						compatibilityMatrix[idxPat][idxGraph] = true;
					else
						compatibilityMatrix[idxPat][idxGraph] = false;
				} else {
					if (inDegreePatternNode <= inDegreeGraphNode
							&& outDegreePatternNode <= outDegreeGraphNode)
						compatibilityMatrix[idxPat][idxGraph] = true;
					else
						compatibilityMatrix[idxPat][idxGraph] = false;
				}
			}
		}
		
		return compatibilityMatrix;
	}
	
	@Override
	public boolean computeNextPair(int prevPatternNodeID, int prevTargetNodeID) {
		boolean retval = false;
		// set pattern node for the next pattern node (length is +1 of index)
		idxNextPatternNode = curLengthPattern;
		
		if (idxNextPatternNode >= lenArrayNodesPattern)
			return false;
		
		if (prevPatternNodeID == State.NULL_NODE) {
			idxNextGraphNode = 0;
		}
		if (prevTargetNodeID == State.NULL_NODE) {
			idxNextGraphNode = 0;
		} else {
			idxNextGraphNode = prevTargetNodeID + 1;
		}
		
		/*
		 * find next compatible node
		 */
		for (; idxNextGraphNode < getArrayNodesGraph().length
				&& !getCompatibilityMatrix()[idxNextPatternNode][idxNextGraphNode]; idxNextGraphNode++);
		// we found a compatible node for the same pattern node
		if (idxNextGraphNode < getArrayNodesGraph().length) {
//			if (curLengthPattern > 1) {
//			getMatchNodesInGraphForPattern()[curLengthPattern] = idxNextGraphNode;
//			retval = checkForVincinity(); //check if the valid found nodes are actually mirroring the connectivity given by pattern
//			}
//			else
			retval = true;
		}
		else
			// we didn't find a compatible node for the pattern node
			retval = false;
		
		return retval;
	}
	
	@Override
	public boolean isFeasiblePair(int node1, int node2) {
		return getCompatibilityMatrix()[node1][node2] && checkForVincinity();
	}
	
	@Override
	public void addPair(int nodeOfPatternGraph, int nodeOfTargetGraph) {
		
		getMatchNodesInGraphForPattern()[nodeOfPatternGraph] = nodeOfTargetGraph;
		getMatchNodesInPatternForGraph()[nodeOfTargetGraph] = nodeOfPatternGraph;
		curLengthPattern++;
		
		for (int y = curLengthPattern; y < lenArrayNodesPattern; y++)
			getCompatibilityMatrix()[y][nodeOfTargetGraph] = false;
		
	}
	
	@Override
	public int getNextNodeOfPattern() {
		return idxNextPatternNode;
	}
	
	@Override
	public int getNextNodeOfTarget() {
		return idxNextGraphNode;
	}
	
	@Override
	public boolean isGoal() {
		if (curLengthPattern == lenArrayNodesPattern) {
			
			/*
			 * check adjacency matrix for both graphs
			 * substracting each must be 0, then the found graph is actually
			 * compatible with the pattern
			 * We have quadratic matrix since we check all pattern nodes
			 * and #foundgraphnodes == numpatternnodes
			 */
			
			return checkForVincinity();
//			return true;
		}
		return false;
	}
	
	@Override
	public boolean isDead() {
		if (getArrayNodesGraph().length < getArrayNodesPattern().length)
			return false;
		
		//false
		for (int y = curLengthPattern; y < lenArrayNodesPattern; y++) {
			boolean foundPossibleState = false;
			for (int x = 0; x < lenArrayNodesGraph; x++) {
				if (getCompatibilityMatrix()[y][x]) {
					// we found possible state, skip the rest of row, check next one
					foundPossibleState = true;
					
				}
			}
			// no next possible state found on current row 
			if (!foundPossibleState)
				return true;
		}
		return false;
	}
	
	@Override
	public void backtrack() {
	}
	
	@Override
	public Graph getPatternGraph() {
		return patternGraph;
	}
	
	@Override
	public Graph getTargetGraph() {
		return targetGraph;
	}
	
	@Override
	public int getCoreLength() {
		return curLengthPattern;
	}
	
	@Override
	public Node[] getMatchingNodesOfPattern() {
		
		return getArrayNodesPattern();
	}
	
	@Override
	public Node[] getMatchingNodesOfTarget() {
		Node[] matchingNodesOfGraph = new Node[lenArrayNodesPattern];
		for (int i = 0; i < lenArrayNodesPattern; i++)
			if (getMatchNodesInGraphForPattern()[i] != State.NULL_NODE)
				matchingNodesOfGraph[i] = getArrayNodesGraph()[getMatchNodesInGraphForPattern()[i]];
		return matchingNodesOfGraph;
	}
	
	@Override
	public Object clone() {
		UllmannSubraphIsomAdjMatrixState newState = new UllmannSubraphIsomAdjMatrixState(patternGraph, targetGraph, ignoreEdgeDirection);
		
		newState.adjMatrixGraph = getAdjMatrixGraph();
		newState.adjMatrixPattern = getAdjMatrixPattern();
		newState.arrayNodesGraph = getArrayNodesGraph();
		newState.arrayNodesPattern = getArrayNodesPattern();
		newState.curLengthPattern = this.curLengthPattern;
		
		newState.lenArrayNodesGraph = this.lenArrayNodesGraph;
		newState.lenArrayNodesPattern = this.lenArrayNodesPattern;
		newState.idxNextGraphNode = this.idxNextGraphNode;
		newState.idxNextPatternNode = this.idxNextPatternNode;
		newState.matchNodesInGraphForPattern = this.matchNodesInGraphForPattern;
		newState.matchNodesInPatternForGraph = this.matchNodesInPatternForGraph;
		
		newState.currentRecursionLevel = this.currentRecursionLevel;
		
		boolean[][] clonedCompatibilityMatrix = new boolean[arrayNodesPattern.length][arrayNodesGraph.length];
		for (int y = 0; y < lenArrayNodesPattern; y++)
			for (int x = 0; x < lenArrayNodesGraph; x++)
				clonedCompatibilityMatrix[y][x] = compatibilityMatrix[y][x];
		newState.compatibilityMatrix = clonedCompatibilityMatrix;
		
		return newState;
	}
	
	private boolean checkForVincinity() {
		if (logger.getLevel() == Level.DEBUG) {
			System.out.print("Checking idx pattern [0.." + lenArrayNodesPattern + "] against ");
			for (int idxNodePatX = 0; idxNodePatX < lenArrayNodesPattern; idxNodePatX++)
				System.out.print(matchNodesInGraphForPattern[idxNodePatX] + ",");
			System.out.println("]");
			
		}
		for (int idxNodePatY = 0; idxNodePatY < curLengthPattern; idxNodePatY++) {
			
			int idxNodeGraphY = matchNodesInGraphForPattern[idxNodePatY];
			
			for (int idxNodePatX = 0; idxNodePatX < curLengthPattern; idxNodePatX++) {
				
				int idxNodeGraphX = matchNodesInGraphForPattern[idxNodePatX];
				int a = getAdjMatrixPattern()[idxNodePatY][idxNodePatX];
				int b = getAdjMatrixGraph()[idxNodeGraphY][idxNodeGraphX];
				if (a - b > 0)
					return false;
				
			}
		}
		return true;
	}
	
	public void printCompatibilityMatrix() {
		System.out.print("patNode\\targetNode\t");
		for (Node n : arrayNodesGraph)
			System.out.print(AttributeHelper.getLabel(n, n.toString()) + "\t");
		System.out.println();
		for (int y = 0; y < arrayNodesPattern.length; y++) {
			System.out.print("\t" + AttributeHelper.getLabel(arrayNodesPattern[y], arrayNodesPattern[y].toString()) + "\t\t");
			for (int x = 0; x < arrayNodesGraph.length; x++) {
				System.out.print(compatibilityMatrix[y][x] + "\t");
			}
			System.out.println();
		}
		
	}
}
