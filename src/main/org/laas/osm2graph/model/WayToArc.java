package org.laas.osm2graph.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    // Logger
    private static final Logger LOGGER = Logger.getLogger(WayToArc.class.getName());

    private class WayToArcProcessor implements Runnable {

        // List of ways to process
        private final List<Way> ways;

        // first / last index
        private final int first, last;
        private int nProcessed;

        // List of arcs processed
        private final List<Arc> arcs;

        /**
         * Create a new WayToArcProcessor that should convert ways starting at first up
         * to last (not included).
         * 
         * @param ways
         * @param first
         * @param last
         */
        public WayToArcProcessor(List<Way> ways, int first, int last) {
            this.ways = ways;
            this.first = first;
            this.last = last;
            this.nProcessed = 0;
            this.arcs = new ArrayList<>(last - first);
        }

        /**
         * @return The list of converted arcs.
         */
        public synchronized List<Arc> getArcs() {
            return this.arcs;
        }

        /**
         * @return Number of ways that have been processed.
         */
        public synchronized int getNumberOfWaysProcessed() {
            return this.nProcessed;
        }

        @Override
        public void run() {
            for (int i = first; i < last; ++i) {
                List<Arc> sarcs = convert(this.ways.get(i));
                arcs.addAll(sarcs);
                synchronized (this) {
                    this.nProcessed += 1;
                }
            }
        }

    };

    // Tags to keep:
    private final static List<String> USEFUL_TAGS = Arrays.asList(
            new String[] { "name", "highway", "natural", "junction", "maxspeed", "oneway" });

    // Mapping ID (OSM) -> Vertex.
    protected final Map<Long, Vertex> vertices;

    // Set of vertex IDs.
    protected final Set<Long> nodeMarks;

    // Set of road informations.
    protected final Map<RoadInformation, RoadInformation> roadinfos;

    // Configuration.
    protected final OSM2GraphConfiguration configuration;

    /**
     * @param vertices
     */
    public WayToArc(Map<Long, Vertex> vertices, OSM2GraphConfiguration configuration) {
        this.vertices = vertices;
        this.roadinfos = Collections.synchronizedMap(new HashMap<>());
        this.configuration = configuration;
        this.nodeMarks = new HashSet<>(vertices.size());
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
        return USEFUL_TAGS.contains((String) tag.getKey())
                || AccessData.USEFUL_TAGS.contains((String) tag.getKey());
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

        RoadInformation roadinfo = new RoadInformation(roadType, access, oneWay, maxSpeed, name);
        RoadInformation previous = roadinfos.getOrDefault(roadinfo, null);

        if (previous == null) {
            roadinfos.put(roadinfo, roadinfo);
        }
        else {
            roadinfo = previous;
        }

        return roadinfo;

    }

    /**
     * Update the `nodesToMark` attributes to indicate which node correspond to a
     * vertex in the graph. A node is considered a vertex if it is the first or last
     * node of a way, or if it is used by two different ways.
     * 
     * @param ways
     * 
     */
    protected void findVertex(List<Way> ways) {
        Set<Long> current = new HashSet<>(vertices.size());
        for (Way way: ways) {
            List<WayNode> nodes = way.getWayNodes();
            for (WayNode node: nodes) {
                long id = node.getNodeId();
                if (current.contains(id)) {
                    this.nodeMarks.add(id);
                }
                else {
                    current.add(id);
                }
            }
            this.nodeMarks.add(nodes.get(0).getNodeId());
            this.nodeMarks.add(nodes.get(nodes.size() - 1).getNodeId());
        }
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

            if (this.nodeMarks.contains(nodeId)) {
                arcs.add(new Arc(arcs.size(), origin, vertices.get(nodeId), (int) (length * 1000),
                        roadinfo, points));

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
        findVertex(ways);

        // Convert arcs
        LOGGER.info("converting way to arcs... ");

        int nPerThread = ways.size() / configuration.getThreads() + 1;

        WayToArcProcessor[] processors = new WayToArcProcessor[configuration.getThreads()];
        Thread[] threads = new Thread[configuration.getThreads()];
        for (int i = 0; i < threads.length; ++i) {
            processors[i] = new WayToArcProcessor(ways, i * nPerThread,
                    Math.min((i + 1) * nPerThread, ways.size()));
            threads[i] = new Thread(processors[i]);
            threads[i].start();
        }

        Thread logger = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        int count = 0;
                        for (int i = 0; i < processors.length; ++i) {
                            count += processors[i].getNumberOfWaysProcessed();
                        }
                        LOGGER.info("processed " + count + " out of " + ways.size() + " ways.");
                        Thread.sleep(5000);
                    }
                    catch (InterruptedException ex) {
                        break;
                    }
                }
            }
        });
        logger.start();

        ArrayList<Arc> arcs = new ArrayList<Arc>();
        for (int i = 0; i < processors.length; ++i) {
            try {
                threads[i].join();
            }
            catch (InterruptedException e) {
                LOGGER.warning("Exception when joining thread " + i);
                e.printStackTrace();
                return null;
            }
            arcs.addAll(processors[i].getArcs());
        }

        logger.interrupt();

        return arcs;
    }

}
