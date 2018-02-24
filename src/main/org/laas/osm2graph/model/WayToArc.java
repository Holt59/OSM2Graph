package org.laas.osm2graph.model;

import java.util.ArrayList;
import java.util.Collection;
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

    // Default maximum speed.
    private final static int DEFAULT_MAXIMUM_SPEED = 0;

    // Default walk speed
    private final static int DEFAULT_WALK_SPEED = 5;

    // Mapping ID (OSM) -> Vertex.
    protected final Map<Long, Vertex> vertices;

    // Mapping ID (OSM) -> Is a Vertex
    Map<Long, Boolean> nodeMarks;

    // Mapping road name -> Road informations
    protected final ArrayList<RoadInformation> roadinfos;

    public WayToArc(Map<Long, Vertex> vertices) {
        this.vertices = vertices;
        this.roadinfos = new ArrayList<RoadInformation>();
    }

    private RoadType getRoadType(Way way) {
        Collection<Tag> tags = way.getTags();
        Iterator<Tag> itTag = tags.iterator();
        RoadType roadtype = null;
        while (itTag.hasNext() && roadtype == null) {
            Tag tag = itTag.next();
            if (tag.getKey().equals("natural") && tag.getValue().equals("coastline")) {
                roadtype = RoadType.COASTLINE;
            }
            else if (tag.getKey().equals("highway")) {
                try {
                    roadtype = RoadType.valueOf(tag.getValue().toUpperCase());
                }
                catch (IllegalArgumentException e) {
                    LOGGER.severe("Unrecognized road type: highway=" + tag.getValue());
                    roadtype = RoadType.UNCLASSIFIED;
                }
            }
        }
        return roadtype;
    }

    private int maxSpeedForRoadType(RoadType roadtype) {
        switch (roadtype) {
        case MOTORWAY:
        case MOTORWAY_LINK:
            return 130;
        case TRUNK:
        case TRUNK_LINK:
            return 110;
        case PRIMARY:
        case PRIMARY_LINK:
        case SECONDARY:
        case SECONDARY_LINK:
        case UNCLASSIFIED:
        case ROAD:
            return 90;
        case TERTIARY:
        case RESIDENTIAL:
            return 50;
        case SERVICE:
        case ROUNDABOUT:
        case LIVING_STREET:
            return 20;
        case COASTLINE:
        default:
            return 0;
        }
    }

    private int getMaximumSpeed(String maxspeed, RoadType roadtype) {
        if (maxspeed == null || maxspeed.equals("none") || maxspeed.equals("signal")) {
            if (roadtype == null) {
                // Something is wrong here...
                return DEFAULT_MAXIMUM_SPEED;
            }
            return maxSpeedForRoadType(roadtype);
        }
        if (maxspeed.equals("walk")) {
            return DEFAULT_WALK_SPEED;
        }
        int speed = DEFAULT_MAXIMUM_SPEED;
        if (maxspeed.contains(":")) {
            // Implicit speed
            speed = SpeedData.speedForCountryAndType(maxspeed);
            if (speed == -1) {
                speed = roadtype != null ? maxSpeedForRoadType(roadtype) : DEFAULT_MAXIMUM_SPEED;
            }
        }
        else {
            // Numeric speed
            String[] parts = maxspeed.split(" ");
            try {
                speed = Integer.valueOf(parts[0]);
            }
            catch (NumberFormatException exception) {
                speed = roadtype != null ? maxSpeedForRoadType(roadtype) : DEFAULT_MAXIMUM_SPEED;
            }

            if (parts.length == 1) {
                return speed;
            }
            String unit = parts[1];
            if (unit.equals("knots")) {
                return (int) (speed * 1.852);
            }
            if (unit.equals("mph")) {
                return (int) (speed * 1.609);
            }
            return speed;
        }
        return speed;
    }

    /**
     * Try to find a matching road information inside roadinfos. If none is found, a
     * new one is created and returned.
     * 
     * @param way
     * 
     * @return
     */
    private RoadInformation getOrCreateRoadInformation(Way way) {
        Collection<Tag> tags = way.getTags();

        String name = "";
        RoadType roadType = null;
        boolean oneWay = false;
        String sMaxSpeed = null;

        for (Tag tag: tags) {
            if (tag.getKey().equals("name")) {
                name = tag.getValue();
            }
            if (tag.getKey().equals("oneway")) {
                oneWay = tag.getValue().equals("yes");
            }
            if (tag.getKey().equals("maxspeed")) {
                sMaxSpeed = tag.getValue();
            }
        }

        roadType = getRoadType(way);
        int maxSpeed = getMaximumSpeed(sMaxSpeed, roadType);

        RoadInformation roadinfo = null;

        for (int i = 0; i < roadinfos.size() && roadinfo == null; ++i) {
            RoadInformation ri = roadinfos.get(i);
            if (ri.getName().equals(name) && (ri.isOneWay() == oneWay) && ri.getType().equals(roadType)
                    && ri.getMaximumSpeed() == maxSpeed) {
                roadinfo = ri;
            }
        }

        if (roadinfo == null) {
            roadinfo = new RoadInformation(roadType, oneWay, maxSpeed, name);
            roadinfos.add(roadinfo);
        }

        return roadinfo;
    }

    /**
     * Create and return a map (NodeID -> Boolean) indicating which node correspond
     * to a vertex in the graph. A vertex is either a node at the beginning or end
     * of a way, or a node used by multiple ways.
     * 
     * @param ways
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
     * @param way
     * @return
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
                arcs.add(new Arc(origin, vertices.get(nodeId), (int) length, roadinfo, points));
                length = 0;
                points = new ArrayList<Point>();
                origin = vertices.get(nodeId);
                points.add(origin.getPoint());
            }
        }
        return arcs;
    }

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
        return arcs;
    }

}
