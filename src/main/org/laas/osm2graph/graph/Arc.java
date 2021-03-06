package org.laas.osm2graph.graph;

import java.util.Collections;
import java.util.List;

public class Arc {

    // Id of this arc
    private final int id;

    // Destination node.
    private final Vertex origin, destination;

    // Length of the road (in meters).
    private final double length;

    // Road information.
    private final RoadInformation info;

    // Segments.
    private final List<Point> points;

    /**
     * Create a new arc and automatically link it with the given origin.
     * 
     * @param origin Origin of this arc.
     * @param dest Destination of this arc.
     * @param length Length of this arc (in meters).
     * @param roadInformation Road information for this arc.
     * @param list Points representing this arc.
     */
    public Arc(int id, Vertex origin, Vertex dest, double length, RoadInformation roadInformation,
            List<Point> list) {
        this.id = id;
        this.origin = origin;
        this.destination = dest;
        this.length = length;
        this.info = roadInformation;
        this.points = list;
        origin.addSuccessor(this);
    }

    /**
     * @return Id of this arc.
     */
    public int getId() {
        return id;
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
    public double getLength() {
        return length;
    }

    /**
     * @return Road information for this arc.
     */
    public RoadInformation getInfo() {
        return info;
    }

    /**
     * @return Points representing segments of this arc.
     */
    public List<Point> getPoints() {
        return Collections.unmodifiableList(points);
    }

}
