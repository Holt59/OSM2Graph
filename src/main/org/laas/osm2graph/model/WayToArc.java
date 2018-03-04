package org.laas.osm2graph.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.laas.osm2graph.graph.Arc;
import org.laas.osm2graph.graph.Point;
import org.laas.osm2graph.graph.RoadInformation;
import org.laas.osm2graph.graph.RoadInformation.RoadType;
import org.laas.osm2graph.graph.Vertex;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

public class WayToArc {

    private static final Logger LOGGER = Logger.getLogger(WayToArc.class.getName());

    // Tags to keep:
    private final static List<String> USEFUL_TAGS = Arrays
            .asList(new String[] { "name", "highway", "natural", "junction", "maxspeed", "oneway" });

    // Mapping ID (OSM) -> Vertex.
    protected final Map<Long, Vertex> vertices;

    // Mapping ID (OSM) -> Is a Vertex
    Map<Long, Boolean> nodeMarks;

    // Mapping road name -> Road informations
    protected final ArrayList<RoadInformation> roadinfos;

    /**
     * @param vertices
     */
    public WayToArc(Map<Long, Vertex> vertices) {
        this.vertices = vertices;
        this.roadinfos = new ArrayList<RoadInformation>();
    }

    /**
     * Retrieve one way information from the given tag value. If sOneWay is null,
     * retrieve it from roadType, otherwize return false.
     * 
     * @param sOneWay
     * @param roadType
     * 
     * @return
     */
    protected boolean getOneWay(Map<String, String> tags, RoadType roadType) {
        String sOneWay = tags.getOrDefault("oneway", null);
        if (sOneWay != null) {
            sOneWay = sOneWay.toLowerCase();
            return sOneWay.equals("yes") || sOneWay.equals("true") || sOneWay.equals("1");
        }
        if (roadType != null && (roadType == RoadType.MOTORWAY || roadType == RoadType.MOTORWAY_LINK
                || roadType == RoadType.TRUNK_LINK || roadType == RoadType.PRIMARY_LINK
                || roadType == RoadType.ROUNDABOUT)) {
            return true;
        }
        return false;
    }

    /**
     * @param tag
     * 
     * @return true if this tag is useful, false otherwize.
     */
    protected boolean isUsefulTag(Tag tag) {
        return USEFUL_TAGS.contains((String) tag.getKey()) || AccessData.USEFUL_TAGS.contains((String) tag.getKey());
    }

    /**
     * Try to find a matching road information inside roadinfos. If none is found, a
     * new one is created and returned.
     * 
     * @param way
     * 
     * @return Existing or new RoadInformation for the given way.
     */
    protected RoadInformation getOrCreateRoadInformation(Way way) {

        Map<String, String> tags = new HashMap<>();

        for (Tag tag: way.getTags()) {
            if (isUsefulTag(tag)) {
                tags.put(tag.getKey(), tag.getValue());
            }
        }

        RoadType roadType = RoadTypeData.getRoadType(tags);
        int maxSpeed = SpeedData.getMaximumSpeed(tags, roadType);
        boolean oneWay = getOneWay(tags, roadType);
        long access = AccessData.getAccessType(tags, roadType);

        String name = tags.getOrDefault("name", "");

        if (name.equals("Passerelle des Herbettes")) {
            System.out.print(roadType + ", " + Long.toHexString(access));
            for (Tag tag: way.getTags()) {
                System.out.print(", " + tag.getKey() + "=" + tag.getValue());
            }
            System.out.println();
        }

        RoadInformation roadinfo = null;

        for (int i = 0; i < roadinfos.size() && roadinfo == null; ++i) {
            RoadInformation ri = roadinfos.get(i);
            if (ri.getName().equals(name) && (ri.isOneWay() == oneWay) && ri.getType().equals(roadType)
                    && ri.getMaximumSpeed() == maxSpeed && ri.getAccess() == access) {
                roadinfo = ri;
            }
        }

        if (roadinfo == null) {
            roadinfo = new RoadInformation(roadType, access, oneWay, maxSpeed, name);
            roadinfos.add(roadinfo);
        }

        return roadinfo;
    }

    /**
     * Create and return a map (NodeID -> Boolean) indicating which node correspond
     * to a vertex in the graph. A node is considered a vertex if it is the first or
     * last node of a way, or if it is used by two different ways.
     * 
     * @param ways
     * 
     * @return
     */
    protected Map<Long, Boolean> findVertex(List<Way> ways) {
        Map<Long, Boolean> marks = new HashMap<Long, Boolean>(vertices.size());
        for (Way way: ways) {
            List<WayNode> nodes = way.getWayNodes();
            for (WayNode node: nodes) {
                long id = node.getNodeId();
                if (marks.containsKey(id)) {
                    marks.put(id, true);
                }
                else {
                    marks.put(id, false);
                }
            }
            marks.put(nodes.get(0).getNodeId(), true);
            marks.put(nodes.get(nodes.size() - 1).getNodeId(), true);
        }
        return marks;
    }

    /**
     * Convert a way into a list of arcs - A way might be split if one of its node
     * is shared with another way (in which case this node becomes a vertex).
     * 
     * @param way Way to convert.
     * 
     * @return List of arcs corresponding to the given way.
     */
    protected ArrayList<Arc> convert(Way way) {

        // Get road information
        RoadInformation roadinfo = getOrCreateRoadInformation(way);

        // Way nodes
        List<WayNode> nodes = way.getWayNodes();
        Iterator<WayNode> itNodes = nodes.iterator();

        // Arc and points
        ArrayList<Arc> arcs = new ArrayList<Arc>();
        ArrayList<Point> points = new ArrayList<Point>();
        double length = 0.0;
        Vertex origin = vertices.get(itNodes.next().getNodeId());
        points.add(origin.getPoint());
        while (itNodes.hasNext()) {
            long nodeId = itNodes.next().getNodeId();
            Point newPoint = vertices.get(nodeId).getPoint();
            length += points.get(points.size() - 1).distanceTo(newPoint);
            points.add(newPoint);

            if (this.nodeMarks.get(nodeId)) {
                arcs.add(new Arc(arcs.size(), origin, vertices.get(nodeId), (int) length, roadinfo, points));

                length = 0;
                points = new ArrayList<Point>();
                origin = vertices.get(nodeId);
                points.add(origin.getPoint());
            }
        }

        return arcs;
    }

    /**
     * Convert the given list of ways into a list of arcs.
     * 
     * @param ways List of ways to convert.
     * 
     * @return List of arcs created from the ways.
     */
    public ArrayList<Arc> convert(List<Way> ways) {
        // Find vertex...
        LOGGER.info("finding vertices inside ways... ");
        this.nodeMarks = findVertex(ways);

        // Convert arcs
        LOGGER.info("converting way to arcs... ");

        int nLogs = Math.max(10000, Math.min(50000, (int) (ways.size() * 0.05)));

        ArrayList<Arc> arcs = new ArrayList<Arc>();
        for (int i = 0; i < ways.size(); ++i) {
            arcs.addAll(convert(ways.get(i)));
            ways.set(i, null);

            // log
            if ((i + 1) % nLogs == 0) {
                LOGGER.info("processed " + (i + 1) + " out of " + ways.size() + " ways");
            }
        }

        for (Arc arc: arcs) {
            if (arc.getLength() > (1 << 16) - 1) {
                LOGGER.info("Too long arc: " + arc.getLength());
            }
        }

        return arcs;
    }

}
