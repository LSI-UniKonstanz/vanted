package org.vanted.addons.MultilevelFramework;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeConsumer;
import org.graffiti.attributes.AttributeExistsException;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.AttributeTypesManager;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.FieldAlreadySetException;
import org.graffiti.attributes.NoCollectionAttributeException;
import org.graffiti.attributes.UnificationException;
import org.graffiti.event.ListenerManager;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.GraphElementNotFoundException;
import org.graffiti.graph.Node;

public class GraphLevel implements Graph {

	@Override
	public Attribute getAttribute(String path) throws AttributeNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CollectionAttribute getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBoolean(String path, boolean value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getBoolean(String path) throws AttributeNotFoundException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setByte(String path, byte value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte getByte(String path) throws AttributeNotFoundException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setDouble(String path, double value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getDouble(String path) throws AttributeNotFoundException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setFloat(String path, float value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getFloat(String path) throws AttributeNotFoundException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setInteger(String path, int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getInteger(String path) throws AttributeNotFoundException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ListenerManager getListenerManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLong(String path, long value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getLong(String path) throws AttributeNotFoundException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setShort(String path, short value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public short getShort(String path) throws AttributeNotFoundException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setString(String path, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getString(String path) throws AttributeNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addAttribute(Attribute attr, String path)
			throws AttributeExistsException, NoCollectionAttributeException, FieldAlreadySetException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addBoolean(String path, String id, boolean value)
			throws NoCollectionAttributeException, AttributeExistsException, FieldAlreadySetException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addByte(String path, String id, byte value)
			throws NoCollectionAttributeException, AttributeExistsException, FieldAlreadySetException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addDouble(String path, String id, double value)
			throws NoCollectionAttributeException, AttributeExistsException, FieldAlreadySetException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addFloat(String path, String id, float value)
			throws NoCollectionAttributeException, AttributeExistsException, FieldAlreadySetException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addInteger(String path, String id, int value)
			throws NoCollectionAttributeException, AttributeExistsException, FieldAlreadySetException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addLong(String path, String id, long value)
			throws NoCollectionAttributeException, AttributeExistsException, FieldAlreadySetException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addShort(String path, String id, short value)
			throws NoCollectionAttributeException, AttributeExistsException, FieldAlreadySetException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addString(String path, String id, String value)
			throws NoCollectionAttributeException, AttributeExistsException, FieldAlreadySetException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeBoolean(String path, boolean value) throws AttributeNotFoundException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeByte(String path, byte value) throws AttributeNotFoundException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeDouble(String path, double value) throws AttributeNotFoundException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeFloat(String path, float value) throws AttributeNotFoundException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeInteger(String path, int value) throws AttributeNotFoundException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeLong(String path, long value) throws AttributeNotFoundException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeShort(String path, short value) throws AttributeNotFoundException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeString(String path, String value) throws AttributeNotFoundException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Attribute removeAttribute(String path) throws AttributeNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AttributeTypesManager getAttTypesManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDirected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isModified() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setModified(boolean modified) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDirected(boolean directed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDirected(boolean directed, boolean adjustArrows) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long generateNextUniqueGraphElementId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getCurrentMaxGraphElementId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<Edge> getEdges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Edge> getEdges(Node n1, Node n2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Edge> getEdgesIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<GraphElement> getGraphElements() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Node> getNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Node> getNodesIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfDirectedEdges() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfEdges() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfNodes() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfUndirectedEdges() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isUndirected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addAttributeConsumer(AttributeConsumer attConsumer) throws UnificationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Edge addEdge(Node source, Node target, boolean directed) throws GraphElementNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Edge addEdge(Node source, Node target, boolean directed, CollectionAttribute col)
			throws GraphElementNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Edge addEdgeCopy(Edge edge, Node source, Node target) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<GraphElement> addGraph(Graph g) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node addNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node addNode(CollectionAttribute col) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node addNodeCopy(Node node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean containsEdge(Edge e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsNode(Node n) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void deleteEdge(Edge e) throws GraphElementNotFoundException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteNode(Node n) throws GraphElementNotFoundException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean removeAttributeConsumer(AttributeConsumer attConsumer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void deleteAll(Collection<? extends GraphElement> graphelements) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName(boolean fullName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void numberGraphElements() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkMaxGraphElementId(long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setListenerManager(ListenerManager object) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFileTypeDescription(String fileTypeDescription) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getFileTypeDescription() {
		// TODO Auto-generated method stub
		return null;
	}

}
