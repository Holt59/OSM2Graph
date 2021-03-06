package org.laas.osm2graph.graph;

import java.util.Collections;
import java.util.List;

public class Graph {

    // Map identifier.
    private final String mapId;

    // Map name
    private final String mapName;

    // Nodes of the graph.
    private final List<Vertex> nodes;

    /**
     * @param mapId ID of this graph.
     * @param name Name of the graph.
     * @param list List of nodes for this graph.
     */
    public Graph(String mapId, String mapName, List<Vertex> list) {
        this.mapId = mapId;
        this.mapName = mapName;
        this.nodes = list;
    }

    /**
     * @return Immutable list of nodes of this graph.
     */
    public List<Vertex> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

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
        for (int n = 0; n < nodes.size(); ++n) {
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
    public String getMapId() {
        return mapId;
    }

    /**
     * @return Map ID of this graph.
     */
    public String getMapName() {
        return mapName;
    }

}
