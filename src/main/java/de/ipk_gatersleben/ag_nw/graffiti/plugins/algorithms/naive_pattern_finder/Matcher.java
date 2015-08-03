/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder;

import java.util.HashSet;

import org.AttributeHelper;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.graffiti.graph.Node;

/**
 * Graph matcher for graph and subgraph isomorphism.
 * 
 * @author Dirk Koschützki
 */
class Matcher {
	private static final Logger logger = Logger.getLogger(Matcher.class);
	
	static {
		logger.setLevel(Level.INFO);
	}
	/**
	 * The matching nodes from the pattern graph.
	 */
	private Node[] matchingNodesOfPattern;
	
	/**
	 * The matching nodes from the target graph.
	 */
	private Node[] matchingNodesOfTarget;
	
	/**
	 * Number of nodes in this special match.
	 */
	private int numberOfMatchingNodes;
	
	private boolean allowOverlap;
	
	/**
	 * Finds a matching between two graph, if it exists, given the initial
	 * state of the matching process. Returns true if a match has been found.
	 * numberOfMatchingNodes is assigned the number of matched nodes, and
	 * matchingNodesOfPattern and matchingNodesOfTarget will contain the
	 * corresponding nodes in the two graphs.
	 * 
	 * @param state
	 *           the state for the matching process
	 * @return true, if a match was found
	 */
	public boolean match(State state) {
		return match2(state);
	}
	
	/**
	 * Visits all the matches between two graphs, given the initial state of
	 * the match. Stops when there are no more matches, or the visitor
	 * returns true.
	 * 
	 * @param state
	 *           the state for the matching process
	 * @param visitor
	 *           the visitor to call after every match
	 * @param patternName
	 *           the name of the pattern
	 */
	public void match(State state, PatternVisitor visitor,
			PatternVisitor optVisitor2, HashSet<Node> resultNodes, String patternName, boolean allowOverlap) {
		match2(state, visitor, optVisitor2, resultNodes, patternName, allowOverlap);
	}
	
	/**
	 * Returns the matching nodes from the pattern graph.
	 * 
	 * @return the matching nodes
	 */
	public Node[] getMatchingNodesOfPattern() {
		return matchingNodesOfPattern;
	}
	
	/**
	 * Returns the matching nodes from the target graph.
	 * 
	 * @return the matching nodes
	 */
	public Node[] getMatchingNodesOfTarget() {
		return matchingNodesOfTarget;
	}
	
	/**
	 * Returns the number of matching nodes.
	 * 
	 * @return the number of matching nodes
	 */
	public int getNumberOfMatchingNodes() {
		return numberOfMatchingNodes;
	}
	
	public boolean isAllowOverlap() {
		return allowOverlap;
	}
	
	/**
	 * Recursive implementation of the matching function.
	 * 
	 * @param state
	 *           the state for the matching process
	 * @return true, if a match was found
	 */
	private boolean match2(State state) {
		if (state.isGoal()) {
			numberOfMatchingNodes = state.getCoreLength();
			matchingNodesOfPattern = state.getMatchingNodesOfPattern();
			matchingNodesOfTarget = state.getMatchingNodesOfTarget();
			return true;
		}
		
		if (state.isDead()) {
			return false;
		}
		
		int n1 = State.NULL_NODE;
		int n2 = State.NULL_NODE;
		boolean found = false;
		
		while (!found && state.computeNextPair(n1, n2)) {
			
			n1 = state.getNextNodeOfPattern();
			n2 = state.getNextNodeOfTarget();
			
			if (state.isFeasiblePair(n1, n2)) {
				State nextState = (UllmannSubraphIsomAdjMatrixState) state.clone();
				
				nextState.addPair(n1, n2);
				found = match2(nextState);
				nextState.backtrack();
			}
		}
		
		return found;
	}
	
	/**
	 * Recursive implementation of the matching function.
	 * 
	 * @param state
	 *           the state for the matching process
	 * @param visitor
	 *           the visitor to call after every match
	 * @param additionalInformation
	 *           the name of the pattern
	 * @return true, if a match was found
	 */
	
	private boolean match2(State state, PatternVisitor visitor, PatternVisitor optVisitor2,
			HashSet<Node> resultNodes,
			String additionalInformation, boolean allowOverlap) {
		logger.setLevel(Level.DEBUG);
		logger.debug("--> Entering Level: " + ((UllmannSubraphIsomAdjMatrixState) state).currentRecursionLevel);
		if (logger.getLevel() == Level.DEBUG) {
			System.out.print("matching pattern nodes: ");
			int numFound = 0;
			for (int i = 0; i < ((UllmannSubraphIsomAdjMatrixState) state).getMatchingNodesOfPattern().length
					&& numFound < ((UllmannSubraphIsomAdjMatrixState) state).currentRecursionLevel; i++) {
				Node n = state.getMatchingNodesOfPattern()[i];
				if (n != null) {
					numFound++;
					System.out.print(AttributeHelper.getLabel(n, Long.toString(n.getID())) + " ");
				}
			}
			System.out.println();
			System.out.print("matching Target  nodes: ");
			numFound = 0;
			for (int i = 0; i < ((UllmannSubraphIsomAdjMatrixState) state).getMatchingNodesOfTarget().length
					&& numFound < ((UllmannSubraphIsomAdjMatrixState) state).currentRecursionLevel; i++) {
				Node n = state.getMatchingNodesOfTarget()[i];
				if (n != null) {
					numFound++;
					System.out.print(AttributeHelper.getLabel(n, Long.toString(n.getID())) + " ");
				}
			}
			System.out.println();
		}
		
		if (state.isGoal()) {
			logger.debug("Pattern found: marking and returning");
			numberOfMatchingNodes = state.getCoreLength();
			matchingNodesOfPattern = state.getMatchingNodesOfPattern();
			
			matchingNodesOfTarget = state.getMatchingNodesOfTarget();
			if (resultNodes != null)
				for (Node n : matchingNodesOfTarget)
					resultNodes.add(n);
			if (optVisitor2 != null)
				optVisitor2.visitPattern(numberOfMatchingNodes,
						matchingNodesOfPattern,
						matchingNodesOfTarget,
						additionalInformation, allowOverlap);
			return visitor.visitPattern(numberOfMatchingNodes,
					matchingNodesOfPattern,
					matchingNodesOfTarget,
					additionalInformation, allowOverlap);
		}
		
		if (state.isDead()) {
			logger.debug("<-- Leaving Level: " + ((UllmannSubraphIsomAdjMatrixState) state).currentRecursionLevel + ": isDead:");
//			if (logger.getLevel() == Level.DEBUG)
//				((UllmannSubraphIsomAdjMatrixState) state).printCompatibilityMatrix();
			return false;
		}
		
		int nodeOfPattern = State.NULL_NODE;
		int nodeOfTarget = State.NULL_NODE;
		
		while (state.computeNextPair(nodeOfPattern, nodeOfTarget)) {
			logger.debug("level[" + +((UllmannSubraphIsomAdjMatrixState) state).currentRecursionLevel + "] while.computeNextPair");
			logger.debug("previous pair: " + nodeOfPattern + " " + nodeOfTarget);
			nodeOfPattern = state.getNextNodeOfPattern();
			nodeOfTarget = state.getNextNodeOfTarget();
			logger.debug("    next pair: " + nodeOfPattern + " " + nodeOfTarget);
			if (state.isFeasiblePair(nodeOfPattern, nodeOfTarget)) {
				State nextState = (UllmannSubraphIsomAdjMatrixState) state.clone();
				
//				logger.debug("matrix before addPair---");
//				if (logger.getLevel() == Level.DEBUG)
//					((UllmannSubraphIsomAdjMatrixState) state).printCompatibilityMatrix();
				
				nextState.addPair(nodeOfPattern, nodeOfTarget);
				
				logger.debug("matrix after addPair---");
				if (logger.getLevel() == Level.DEBUG)
					((UllmannSubraphIsomAdjMatrixState) nextState).printCompatibilityMatrix();
				
				((UllmannSubraphIsomAdjMatrixState) nextState).currentRecursionLevel++;
				
				if (match2(nextState, visitor, optVisitor2, resultNodes, additionalInformation, allowOverlap)) {
					nextState.backtrack();
					logger.debug("<-- Leaving Level: " + ((UllmannSubraphIsomAdjMatrixState) nextState).currentRecursionLevel);
					
					return true;
				} else {
					nextState.backtrack();
				}
			}
		}
		
		logger.debug("<-- Leaving Level: " + ((UllmannSubraphIsomAdjMatrixState) state).currentRecursionLevel);
		return false;
	}
}
