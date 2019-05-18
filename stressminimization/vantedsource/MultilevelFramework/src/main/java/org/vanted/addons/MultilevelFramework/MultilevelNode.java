package org.vanted.addons.MultilevelFramework;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeExistsException;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.FieldAlreadySetException;
import org.graffiti.attributes.NoCollectionAttributeException;
import org.graffiti.event.ListenerManager;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;

public class MultilevelNode implements Node {

	@Override
	public Graph getGraph() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setID(long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getViewID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setViewID(int id) {
		// TODO Auto-generated method stub
		
	}

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
	public int compareTo(GraphElement arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<Edge> getAllInEdges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Node> getAllInNeighbors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Edge> getAllOutEdges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Node> getAllOutNeighbors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Edge> getDirectedInEdges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Edge> getDirectedInEdgesIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Edge> getDirectedOutEdges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Edge> getDirectedOutEdgesIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Edge> getEdges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Edge> getEdgesIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getInDegree() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Set<Node> getInNeighbors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Node> getInNeighborsIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Node> getNeighbors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Node> getNeighborsIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getOutDegree() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Set<Node> getOutNeighbors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Node> getOutNeighborsIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Edge> getUndirectedEdges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Edge> getUndirectedEdgesIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Node> getUndirectedNeighbors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Node> getUndirectedNeighborsIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setGraph(Graph graph) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getDegree() {
		// TODO Auto-generated method stub
		return 0;
	}

}
