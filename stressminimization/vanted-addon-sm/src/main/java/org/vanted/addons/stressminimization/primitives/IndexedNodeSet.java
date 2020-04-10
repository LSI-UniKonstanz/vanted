package org.vanted.addons.stressminimization.primitives;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.graffiti.attributes.AttributeManager;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.graph.Node;

/**
 * A node collection that supports indexed access and
 * fast index finding as well as speedy set operations.
 * An IndexedNodeSet is always connected to a basis collection of nodes.
 * Nodes outside this collection cannot be added.
 *
 * The set operations are very fast if copies or subsets of one superset are compared,
 * however set operations on sets not sharing a superset will require linear time in the basis collection.
 *
 * Iteration is guaranteed to be in index order.
 */
public class IndexedNodeSet implements Iterable<Integer> {

	/*
	 * Used for attribute storage. An unique identifier for this IndexedNodeSet.
	 */
	protected final String identifier;

	/**
	 * The basis of this Set.
	 * This list contains a all nodes that *can* be added to this set.
	 */
	protected final List<Node> allNodes;

	/**
	 * Storing which nodes at what index is contained in this set.
	 */
	protected BitSet containedNodes;

	/**
	 * Constructs a new IndexedNodeSet with the given basis collection of nodes,
	 * that is initially empty
	 * @param allNodes the super set of all nodes this set contain nodes from.
	 */
	protected IndexedNodeSet(Collection<Node> allNodes) {
		this(new ArrayList<>(allNodes), new BitSet(allNodes.size()));
	}

	/**
	 * Construct a new IndexedNodeSet with the given
	 * basis collection of nodes and the given contained nodes.
	 * @param allNodes
	 * @param containedNodes
	 */
	protected IndexedNodeSet(List<Node> allNodes, BitSet containedNodes) {

		this.identifier = generateIdentifer();

		this.allNodes = allNodes;
		this.containedNodes = containedNodes;
		indexNodes();

	}

	/**
	 * Constructs a subset with the same index space and identifier as the superset.
	 * @param superset
	 * @param containedNodes
	 */
	protected IndexedNodeSet(IndexedNodeSet superset, BitSet containedNodes) {
		this.identifier = superset.identifier;
		this.allNodes = superset.allNodes;
		this.containedNodes = containedNodes;
	}

	// ==================
	// MARK: construction
	// todo (review bm) remove stuff like this
	// ==================

	/**
	 * Constructs an set with the given node collection as basic collection,
	 * containing all nodes from the basis collection.
	 * @param allNodes
	 */
	public static IndexedNodeSet setOfAllIn(Collection<Node> allNodes) {
		IndexedNodeSet allContained = emptySetOf(allNodes);
		allContained.containedNodes.set(0, allNodes.size(), true);
		return allContained;
	}

	/**
	 * Constructs an empty set with the given node collection as basic collection.
	 * @param allNodes
	 */
	public static IndexedNodeSet emptySetOf(Collection<Node> allNodes) {
		return new IndexedNodeSet(allNodes);
	}

	/**
	 * Constructs an empty subset of this set of nodes.
	 * The sets share the same index space.
	 * Union, intersection and other methods
	 * can be executed very fast between this set and the subset.
	 */
	// todo (review bm) rename?
	public IndexedNodeSet emptySubset() {
		return new IndexedNodeSet(this, new BitSet(allNodes.size()));
	}

	/**
	 * Creates a subset of this set containing
	 * the same nodes with the same basis collection and index space.
	 */
	public IndexedNodeSet copy() {
		return new IndexedNodeSet(this, (BitSet) containedNodes.clone());
	}

	// =====================
	// MARK: accessing nodes
	// =====================

	/**
	 * Returns the index of the contained node with the smallest index.
	 * If the set is empty, this method throws an exception.
	 * @return the smallest index of a contained node
	 */
	public int first() {
		if (isEmpty()) {
			throw new IllegalStateException("set is empty");
		}
		return containedNodes.nextSetBit(0);
	}

	/**
	 * Returns the node at the given index, if it is contained in this set.
	 * Otherwise null is returned.
	 * @param index
	 */
	public Node get(int index) {

		if (index >= allNodes.size()) {
			throw new IndexOutOfBoundsException();
		}

		if (contains(index)) {
			return allNodes.get(index);
		} else {
			return null;
		}
	}

	/**
	 * Returns a IndexedNodeSet containing all neighbors
	 * of the node specified node
	 * that are also contained in this set.
	 * @param of
	 */
	public IndexedNodeSet getNeighbors(Node of) {
		int ofIndex = getIndex(of);
		return getNeighbors(ofIndex);
	}
	/**
	 * Returns a IndexedNodeSet containing all neighbors
	 * of the node at the specified index
	 * that are also contained in this set.
	 * @param ofIndex
	 */
	public IndexedNodeSet getNeighbors(int ofIndex) {

		Node node = get(ofIndex);

		IndexedNodeSet neighbors = emptySubset();
		for (Node neighbor : node.getNeighbors()) {

			// todo (review bm) since this is only about the contains check, we can rewrite this more clearly
			// index of neighbouring node in base set (all nodes that are considered in this alg)
			int neighborIndex;
			try {
				neighborIndex = getIndex(neighbor);
			} catch (NotIndexedException ex) {
				// node is not contained in allNodes (the base set of nodes we consider, e.g. the selection)
				continue;
			}
			// giving the index to this method basically flips the corresponding bit in the vector
			// that describes membership to the `neighbours` set.
			neighbors.add(neighborIndex);

		}

		neighbors.intersection(this);

		return neighbors;

	}

	// ====================
	// MARK: set operations
	// ====================

	public int size() {
		return containedNodes.cardinality();
	}

	public boolean isEmpty() {
		return containedNodes.isEmpty();
	}

	/**
	 * Adds a node from the basis collection passed on construction to this set.
	 * @param node the node to add.
	 * @throws RuntimeException if the node wasn't contained in the basis collection.
	 */
	public void add(Node node) {
		int index = getIndex(node);
		add(index);
	}

	/**
	 * Adds the node at index to this set.
	 * @param index
	 * @throws IndexOutOfBoundsException
	 */
	public void add(int index) {
		if (index >= allNodes.size()) {
			throw new IndexOutOfBoundsException();
		}
		containedNodes.set(index);
	}

	/**
	 * Removes a node from the basis collection passed on construction from this set.
	 * If the nodes was not previously contained, the set is left unchanged.
	 * @param node the node to add.
	 * @throws RuntimeException if the node isn't contained in the basis collection.
	 */
	public void remove(Node node) {
		int index = getIndex(node);
		remove(index);
	}

	/**
	 * Removed the node at index from this set.
	 * If the index was not previously contained, the set is left unchanged.
	 * @param index
	 * @throws IndexOutOfBoundsException
	 */
	public void remove(int index) {
		if (index >= allNodes.size()) {
			throw new IndexOutOfBoundsException();
		}
		containedNodes.clear(index);
	}

	/**
	 * Returns true if node is contained in this sets basis collection as well as in this set itself.
	 * @param node The node to check
	 * @return True if the node is contained in the basis collection and in this set, false otherwise.
	 */
	public boolean isContainedBasisCollectionAndSet(Node node) {
		try {
			return contains(node);
		} catch (NotIndexedException ex) {
			return false;
		}
	}

	/**
	 * Adds a node from the super set passed on construction to this set.
	 * @param node the node to add.
	 * @throws RuntimeException if the node wasn't contained in the super set.
	 */
	public boolean contains(Node node) {
		int index = getIndex(node);
		return contains(index);
	}

	/**
	 * Adds the node at index to this set.
	 * @param index
	 * @throws IndexOutOfBoundsException
	 */
	public boolean contains(int index) {
		return containedNodes.get(index);
	}

	/**
	 * Performs the union operation with the given other IndexedNodeSet.
	 * The node sets needs to have identical super node sets.
	 * After this operation, this node set will contain
	 * all nodes contained in this set before the operation or in the other set (or in both).
	 * @param other
	 */
	public void union(IndexedNodeSet other) {
		checkBaseCollectionsIdentical(other);
		this.containedNodes.or(other.containedNodes);
	}

	/**
	 * Performs the intersection operation with the given other IndexedNodeSet.
	 * The node sets needs to have identical super node sets.
	 * After this operation, this node set will contain
	 * all nodes contained in this set before the operation and in the other set.
	 * @param other
	 */
	public void intersection(IndexedNodeSet other) {
		checkBaseCollectionsIdentical(other);
		this.containedNodes.and(other.containedNodes);
	}

	public void setMinus(IndexedNodeSet other) {
		checkBaseCollectionsIdentical(other);

		IndexedNodeSet complement = other.copy();
		complement.complement();

		intersection(complement);
	}

	private void checkBaseCollectionsIdentical(IndexedNodeSet other) {
		if (other.allNodes != allNodes && !other.allNodes.equals(allNodes)) {
			throw new IllegalArgumentException("base collections need to be identical");
		}
	}
	
	/**
	 * Performs the complement operation on this set.
	 * After the operation this set will contain exactly those nodes
	 * from the super set, that were not contained in this set before the operation.
	 */
	public void complement() {
		this.containedNodes.flip(0, allNodes.size());
	}

	/**
	 * Constructs a new IndexedNodeSet with the nodes contained in this set
	 * as basis collection of the new set. All nodes are contained in the new set.
	 * The new set has a seperate index space.
	 */
	public IndexedNodeSet setOfContainedNodesWithOwnIndices() {

		List<Node> containedNodesList = new ArrayList<>();
		for (int index : this) {
			Node node = get(index);
			containedNodesList.add(node);
		}

		return setOfAllIn(containedNodesList);

	}

	// ===============
	// MARK: iteration
	// ===============

	/**
	 * Returns an iterator for this set, that iterates in index order.
	 */
	@Override
	public Iterator<Integer> iterator() {
		return new IndexIterator();
	}

	private class IndexIterator implements Iterator<Integer> {

		private int currentIndex = -1;

		@Override
		public boolean hasNext() {
			if (currentIndex == -1) {
				return containedNodes.nextSetBit(0) >= 0;
			} else {
				return currentIndex < Integer.MAX_VALUE && containedNodes.nextSetBit(currentIndex + 1) >= 0;
			}
		}

		@Override
		public Integer next() {

			if (currentIndex == -1) {
				currentIndex = containedNodes.nextSetBit(0);
			} else {
				currentIndex = containedNodes.nextSetBit(currentIndex + 1);
			}

			return currentIndex;

		}

	}

	// ==============
	// MARK: indexing
	// ==============

	/**
	 * inits the index finding structures
	 */
	private void indexNodes() {

		// specify that the added attributes shall not be saved
		AttributeManager.getInstance().addUnwrittenAttribute(identifier);
		// todo (review bm) does not seem to work? i get errors when saving the graph after SM execution

		for (int i = 0; i < allNodes.size(); i += 1) {
			allNodes.get(i).addAttribute(new IndexAttribute(i, identifier), "");
		}
	}

	/**
	 * Returns the index in this set, based on the indexing of the based collection. 
	 * If it node is not part of the base collection a NotIndexedException is thrown.
	 * Note that the node may not be contained in this set, although it has an index.
	 */
	public int getIndex(final Node node) {
		int index;
		try {
			index = (int) node.getAttribute(identifier).getValue();
		} catch (AttributeNotFoundException ex) {
			throw new NotIndexedException("the given node isn't indexed.", ex);
		}
		
		return index;
	}

	public class NotIndexedException extends RuntimeException {
		private static final long serialVersionUID = -4294175391400892274L;
		public NotIndexedException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public class NodeNotContainedException extends RuntimeException {
		private static final long serialVersionUID = -1136751379963201755L;
	}
	
	// ====================
	// MARK: identification
	// ====================

	private static final String IDENTIFIER_PREFIX = "IndexedNodeSetIndex";
	private static int nextID = 0;

	private synchronized static String generateIdentifer() {
		nextID += 1;
		return IDENTIFIER_PREFIX + "" + nextID + "t" + System.currentTimeMillis();
	}

}
