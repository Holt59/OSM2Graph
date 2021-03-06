package org.laas.osm2graph.writers;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.laas.osm2graph.graph.Arc;
import org.laas.osm2graph.graph.Graph;
import org.laas.osm2graph.graph.Point;
import org.laas.osm2graph.graph.RoadInformation;
import org.laas.osm2graph.graph.RoadInformation.RoadType;
import org.laas.osm2graph.graph.Vertex;
import org.laas.osm2graph.model.OSM2GraphConfiguration;

/**
 * This writer generates files that were used for practice session at INSA of
 * Toulouse. This writer is here for compatibility, the 2018 should be used
 * instead.
 * 
 * @author Mikael
 *
 */
public class BinaryGraphWriterInsa2016 implements GraphWriter {

    // Map version and magic number targeted for this reader.
    private static final int VERSION = 4;
    private static final int MAGIC_NUMBER = 0xbacaff;

    private static final String DEFAULT_EXTENSION = "map";

    private static final int DEFAULT_ZONE = 0;

    /**
     * Convert a character to its corresponding road type.
     * 
     * @param ch Character to convert.
     * 
     * @return Road type corresponding to ch.
     * 
     * @see http://wiki.openstreetmap.org/wiki/Highway_tag_usage.
     */
    public static char getCharFromType(RoadType ch) {
        switch (ch) {
        case MOTORWAY:
            return 'a';
        case TRUNK:
            return 'b';
        case PRIMARY:
            return 'c';
        case SECONDARY:
            return 'd';
        case MOTORWAY_LINK:
            return 'e';
        case TRUNK_LINK:
            return 'f';
        case PRIMARY_LINK:
            return 'g';
        case SECONDARY_LINK:
            return 'h';
        case TERTIARY:
            return 'i';
        case RESIDENTIAL:
            return 'j';
        case UNCLASSIFIED:
            return 'k';
        case LIVING_STREET:
            return 'm';
        case SERVICE:
            return 'n';
        case ROUNDABOUT:
            return 'o';
        case COASTLINE:
            return 'z';
        default:
            return 'k';
        }
    }

    // Data inpout stream
    DataOutputStream dos;

    /**
     * Create a new BinaryGraphReader using the given DataInputStream.
     * 
     * @param dis
     */
    public BinaryGraphWriterInsa2016() {
    }

    @Override
    public void setOutputStream(OutputStream stream) {
        this.dos = new DataOutputStream(stream);
    }

    /**
     * Write 24 bits to the stream in BigEndian order.
     * 
     * @param value
     * @throws IOException
     */
    protected void write24bits(int value) throws IOException {
        dos.writeShort(value >> 8);
        dos.writeByte(value & 0xff);
    }

    protected IdentityHashMap<RoadInformation, Integer> getRoadInformations(List<Vertex> nodes) {
        IdentityHashMap<RoadInformation, Integer> rinfos = new IdentityHashMap<RoadInformation, Integer>();
        for (Vertex node: nodes) {
            for (Arc arc: node.getSuccessors()) {
                if (!rinfos.containsKey(arc.getInfo())) {
                    rinfos.put(arc.getInfo(), rinfos.size());
                }
            }
        }
        return rinfos;
    }

    @Override
    public void writeGraph(Graph graph) throws IOException {

        dos.writeInt(MAGIC_NUMBER);
        dos.writeInt(VERSION);

        dos.writeInt(parseMapId(graph.getMapId()));
        dos.writeInt(DEFAULT_ZONE);

        List<Vertex> nodes = graph.getNodes();
        IdentityHashMap<RoadInformation, Integer> infos = getRoadInformations(nodes);

        RoadInformation[] sortedInfos = new RoadInformation[infos.size()];
        for (Map.Entry<RoadInformation, Integer> entry: infos.entrySet()) {
            sortedInfos[entry.getValue()] = entry.getKey();
        }

        // Number of descriptors and nodes.
        dos.writeInt(infos.size());
        dos.writeInt(nodes.size());

        // Read nodes.
        for (Vertex v: nodes) {
            dos.writeInt((int) (v.getPoint().getLongitude() * 1e6));
            dos.writeInt((int) (v.getPoint().getLatitude() * 1e6));
            dos.writeByte(v.getSuccessors().size());
        }

        // Check format.
        dos.writeByte(255);

        // Read
        for (int descr = 0; descr < sortedInfos.length; ++descr) {
            RoadInformation info = sortedInfos[descr];
            dos.writeByte(getCharFromType(info.getType()));
            int x = info.getMaximumSpeed() / 5;
            if (info.isOneWay()) {
                x = x | 0x80;
            }
            dos.writeByte(x);
            dos.writeUTF(info.getName());
        }

        // Check format.
        dos.writeByte(254);

        // Read successors and convert to arcs.
        for (Vertex node: nodes) {
            for (Arc arc: node.getSuccessors()) {

                // Write fake target zone
                dos.writeByte(DEFAULT_ZONE);

                // Write target node number.
                write24bits((int) arc.getDestination().getId());

                // Write information number.
                write24bits(infos.get(arc.getInfo()));

                // Length of the arc.
                dos.writeShort((int) (arc.getLength() * 1000));

                // Number of segments.
                List<Point> points = arc.getPoints();
                dos.writeShort(points.size() - 2);

                for (int i = 1; i < points.size() - 1; ++i) {
                    dos.writeShort((int) (2.e5
                            * (points.get(i).getLongitude() - points.get(i - 1).getLongitude())));
                    dos.writeShort((int) (2.e5
                            * (points.get(i).getLatitude() - points.get(i - 1).getLatitude())));
                }
            }
        }

        // Check format.
        dos.writeByte(253);

        dos.flush();
        dos.close();
    }

    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    protected int parseMapId(String mapId) {
        int radix = 10;
        if (mapId.startsWith("0x")) {
            radix = 16;
            mapId = mapId.substring(2);
        }
        else if (mapId.startsWith("0b")) {
            radix = 2;
            mapId = mapId.substring(2);
        }
        else if (mapId.startsWith("0")) {
            radix = 8;
            mapId = mapId.substring(1);
        }
        return Integer.parseUnsignedInt(mapId, radix);
    }

    @Override
    public void validate(OSM2GraphConfiguration configuration) throws IllegalArgumentException {
        try {
            parseMapId(configuration.getMapId());
        }
        catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Map ID must be an integer for this writer.");
        }
        if (configuration.getMapName() != null) {
            throw new IllegalArgumentException("Cannot specify a map name for this writer.");
        }
    }
}
