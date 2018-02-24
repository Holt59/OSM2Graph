package org.laas.osm2graph.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Path {

    /**
     * Create a new path that goes through all nodes in the given list, choosing the
     * fastest route if multiple are available.
     * 
     * @param graph Graph containing the path.
     * @param nodes List of nodes to build the path.
     * 
     * @return A path that goes through the given list of nodes.
     * 
     * @throws IllegalArgumentException If some nodes in the list are not linked by
     *         an arc.
     */
    public static Path createFastestPathFromNodes(Graph graph, List<Vertex> nodes) throws IllegalArgumentException {
        List<Arc> arcs = new ArrayList<Arc>();

        // TODO: For students
        Vertex current = nodes.get(0);
        for (int i = 1; i < nodes.size(); ++i) {
            Vertex node = nodes.get(i);
            Arc minArc = null;
            double minCost = Double.POSITIVE_INFINITY;
            for (Arc arc: current.getSuccessors()) {
                double cost = arc.getMinimumTravelTime();
                if (arc.getDestination().equals(node) && cost < minCost) {
                    minArc = arc;
                    minCost = cost;
                }
            }
            if (minArc == null) {
                throw new IllegalArgumentException(
                        "No arc found between nodes " + current.getId() + " and " + node.getId() + "\n");
            }
            arcs.add(minArc);
            current = node;
        }

        return new Path(graph, arcs);
    }

    /**
     * Create a new path that goes through all nodes in the given list, choosing the
     * using the shortest route if multiple are available.
     * 
     * @param graph Graph containing the path.
     * @param nodes List of nodes to build the path.
     * 
     * @return A path that goes through the given list of nodes.
     * 
     * @throws IllegalArgumentException If some nodes in the list are not linked by
     *         an arc.
     */
    public static Path createShortestPathFromNodes(Graph graph, List<Vertex> nodes) throws IllegalArgumentException {
        List<Arc> arcs = new ArrayList<Arc>();

        // TODO: For students
        Vertex current = nodes.get(0);
        for (int i = 1; i < nodes.size(); ++i) {
            Vertex node = nodes.get(i);
            Arc minArc = null;
            double minCost = Double.POSITIVE_INFINITY;
            for (Arc arc: current.getSuccessors()) {
                double cost = arc.getLength();
                if (arc.getDestination().equals(node) && cost < minCost) {
                    minArc = arc;
                    minCost = cost;
                }
            }
            if (minArc == null) {
                throw new IllegalArgumentException(
                        "No arc found between nodes " + current.getId() + " and " + node.getId() + "\n");
            }
            arcs.add(minArc);
            current = node;
        }

        return new Path(graph, arcs);
    }

    // Graph containing this path.
    private final Graph graph;

    // List of arcs in this path.
    private final List<Arc> arcs;

    /**
     * @param graph
     * @param nodes
     */
    public Path(Graph graph, List<Arc> arcs2) {
        this.graph = graph;
        this.arcs = arcs2;
    }

    /**
     * @return Graph containing the path.
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * @return First node of the path.
     */
    public Vertex getOrigin() {
        return arcs.get(0).getOrigin();
    }

    /**
     * @return Last node of the path.
     */
    public Vertex getDestination() {
        return arcs.get(arcs.size() - 1).getDestination();
    }

    /**
     * @return List of nodes of the path.
     */
    public List<Arc> getArcs() {
        return Collections.unmodifiableList(arcs);
    }

    /**
     * @return true if this path is empty, false otherwize.
     */
    public boolean isEmpty() {
        return arcs.isEmpty();
    }

    /**
     * Check if this path is valid.
     * 
     * @return true if the path is valid, false otherwise.
     */
    public boolean isValid() {
        // TODO
        if (isEmpty()) {
            return true;
        }
        Iterator<Arc> itArc = arcs.iterator();
        Arc prev = itArc.next();
        boolean valid = true;
        while (valid && itArc.hasNext()) {
            Arc curr = itArc.next();
            if (!prev.getDestination().equals(curr.getOrigin())) {
                valid = false;
            }
            prev = curr;
        }
        return valid;
    }

    /**
     * @return Total length of the path.
     */
    public int getLength() {
        // TODO
        int length = 0;
        for (Arc arc: arcs) {
            length += arc.getLength();
        }
        return length;
    }

    /**
     * @return Minimum travel time of the in seconds (assuming maximum speed).
     */
    public double getMinimumTravelTime() {
        // TODO
        double time = 0;
        for (Arc arc: arcs) {
            time += arc.getMinimumTravelTime();
        }
        return time;
    }

}
