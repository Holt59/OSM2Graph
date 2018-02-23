package org.laas.osm2graph.graph;

import java.util.ArrayList;

public class Arc {
	
	// Destination node.
	private Vertex origin, destination;

	// Length of the road (in meters).
	private int length;
	
	// Road information.
	RoadInformation info;
	
	// Segments.
	ArrayList<Point> points;
	
	/**
	 * @param dest
	 * @param length
	 * @param roadInformation
	 * @param points
	 */
	public Arc(Vertex origin, Vertex dest, int length, RoadInformation roadInformation) {
		this.origin = origin;
		this.destination = dest;
		this.length = length;
		this.info = roadInformation;
		this.points = new ArrayList<Point>();
		origin.addSuccessor(this);
	}

	/**
	 * @param dest
	 * @param length
	 * @param roadInformation
	 * @param points
	 */
	public Arc(Vertex origin, Vertex dest, int length, RoadInformation roadInformation, ArrayList<Point> points) {
		this.origin = origin;
		this.destination = dest;
		this.length = length;
		this.info = roadInformation;
		this.points = points;
		origin.addSuccessor(this);
	}
	
	/**
	 * @return Origin node of this arc.
	 */
	public Vertex getOrigin() {
		return origin;
	}
	
	/**
	 * @return Destination node of this arc.
	 */
	public Vertex getDestination() {
		return destination;
	}

	/**
	 * @return Length of this arc, in meters.
	 */
	public int getLength() {
		return length;
	}
	
	/**
	 * @return Minimum time required to travel this arc, in seconds.
	 */
	public double getMinimumTravelTime() {
		return getLength() * 3600.0 / (info.getMaximumSpeed() * 1000.0);
	}

	/**
	 * @return Road information for this arc.
	 */
	public RoadInformation getInfo() {
		return info;
	}

	/**
	 * @return Points representing segments of this arc. This function may return an empty
	 * ArrayList if the segments are stored in the reversed arc (for two-ways road).
	 */
	public ArrayList<Point> getPoints() {
		return points;
	}

}
