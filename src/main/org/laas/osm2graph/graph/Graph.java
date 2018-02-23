package org.laas.osm2graph.graph ;

import java.util.ArrayList;

public class Graph {

	// Map identifier.
	private int mapId;
	
	// Nodes of the graph.
	private ArrayList<Vertex> nodes;

	/**
	 * @param mapId
	 * @param nodes
	 */
	public Graph(int mapId, ArrayList<Vertex> nodes) {
		this.mapId = mapId;
		this.nodes = nodes;
	}
	
	/**
	 * @return Nodes of this graph.
	 */
	public ArrayList<Vertex> getNodes() { return nodes; }
	
	/**
	 * Find the closet node to the given point.
	 * 
	 * @param point
	 * 
	 * @return Closest node to the given point.
	 */
	public Vertex findClosestNode(Point point) {
		Vertex node = null;
		double minDis = Double.POSITIVE_INFINITY;
		for (int n = 0 ; n < nodes.size(); ++n) {
			double dis = point.distanceTo(nodes.get(n).getPoint());
			if (dis < minDis) {
				node = nodes.get(n);
				minDis = dis;
			}
        }
		return node;
	}
	
	/**
	 * @return Map ID of this graph.
	 */
	public int getMapId() { return mapId; }
	
	/**
	 * @return Return the transpose graph of this graph.
	 */
	public Graph transpose() {
		// TODO: 
		return null;
	}

}
