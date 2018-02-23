package org.laas.osm2graph.graph;

import java.util.ArrayList;

public class Vertex implements Comparable<Vertex> {

	// ID of the node.
	private long id;
	
	// Point of this graph.
	private Point point;
	
	// Successors.
	private ArrayList<Arc> successors;
	
	/**
	 * Create a new Node corresponding to the given Point with
	 * an empty list of successors.
	 * 
	 * @param point
	 */
	public Vertex(long id, Point point) {
		this.id = id;
		this.point = point;
		this.successors = new ArrayList<Arc>();
	}

	/**
	 * Add a successor to this node.
	 * 
	 * @param arc Arc to the successor.
	 */
	protected void addSuccessor(Arc arc) {
		successors.add(arc);
	}
	
	/**
	 * @return ID of this node.
	 */
	public long getId() { return id; }
	
	/**
	 * @return List of successors of this node.
	 */
	public ArrayList<Arc> getSuccessors() { return successors; }
	
	/**
	 * @return Point of this node.
	 */
	public Point getPoint() { return point; }
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Vertex) {
			return getId() == ((Vertex) other).getId();
		}
		return false;
	}

	@Override
	public int compareTo(Vertex other) {
		return Long.compare(getId(), other.getId());
	}
	
}
