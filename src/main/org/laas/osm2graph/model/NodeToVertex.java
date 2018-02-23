package org.laas.osm2graph.model;

import org.laas.osm2graph.graph.Point;
import org.laas.osm2graph.graph.Vertex;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;

public class NodeToVertex {
	
	/**
	 * @param node
	 * @return
	 */
	public Vertex convert(Node node) {
		return new Vertex(node.getId(), 
			new Point(node.getLongitude(), node.getLatitude()));
	}

}
