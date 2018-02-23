package org.laas.osm2graph.graph;

import java.util.ArrayList;

public class Path {
	
	public enum CreationMode {
		SHORTEST_LENGTH,
		SHORTEST_TIME
	};
	
	// Graph containing this path.
	private Graph graph;

	// List of arcs in this path.
	private ArrayList<Arc> arcs;
	
	/**
	 * @param graph
	 * @param nodes
	 * @throws Exception 
	 */
	public Path(Graph graph, ArrayList<Vertex> nodes, CreationMode mode) throws IllegalArgumentException {
		this.graph = graph;
		this.arcs = new ArrayList<Arc>();
		
		// TODO: For students
		Vertex current = nodes.get(0);
		for (int i = 1; i < nodes.size(); ++i) {
			Vertex node = nodes.get(i);
			Arc minArc = null;
			double minCost = Double.POSITIVE_INFINITY;
			for (Arc arc: current.getSuccessors()) {
				double cost = mode == CreationMode.SHORTEST_LENGTH ? 
						arc.getLength() : arc.getMinimumTravelTime();
				if (arc.getDestination().equals(node)
					&& cost < minCost) {
					minArc = arc;
					minCost = cost;
				}
			}
			if (minArc == null) {
				throw new IllegalArgumentException("No arc found between nodes " + current.getId() + " and " + node.getId() + "\n");
			}
			arcs.add(minArc);
			current = node;
		}
	}
	
	/**
	 * @param graph
	 * @param nodes
	 */
	public Path(Graph graph, ArrayList<Arc> arcs) {
		this.graph = graph;
		this.arcs = arcs;
	}
	
	/**
	 * @return Graph containing the path.
	 */
	public Graph getGraph() { return graph; }
	
	/**
	 * @return First node of the path.
	 */
	public Vertex getOrigin() { return arcs.get(0).getOrigin(); }
	
	/**
	 * @return Last node of the path.
	 */
	public Vertex getDestination() { return arcs.get(arcs.size() - 1).getDestination(); }
	
	/**
	 * @return List of nodes of the path.
	 */
	public ArrayList<Arc> getArcs() { return arcs; }
	
	/**
	 * Check if this path is valid.
	 * 
	 * @return true if the path is valid, false otherwise.
	 */
	public boolean isValid() {
		// TODO
		return false;
	}
	
	/**
	 * @return Total length of the path.
	 */
	public int getLength() {
		// TODO
		return 0;
	}
	
	/**
	 * @return Minimum travel time of the path (assuming maximum speed).
	 */
	public float getMinimumTravelTime() {
		// TODO
		return 0f;
	}
	
}
