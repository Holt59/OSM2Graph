package org.laas.osm2graph.writers;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
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
 * This writer generates files that are used for practice session at INSA of
 * Toulouse.
 * 
 * @author Mikael
 *
 */
public class BinaryGraphWriterInsa2018 implements GraphWriter {

    // Map version and magic number targeted for this reader.
    private static final int VERSION = 8;
    private static final int MAGIC_NUMBER = 0x208BC3B3;

    private static final String DEFAULT_EXTENSION = "mapgr";

    private static final int MAP_ID_FIELD_LENGTH = 32;

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
        case PEDESTRIAN:
            return 'p';
        case BICYCLE:
            return 'q';
        case TRACK:
            return 'r';
        case COASTLINE:
            return 'z';
        }
        return 'k';
    }

    // Data inpout stream
    DataOutputStream dos;

    /**
     * Create a new BinaryGraphReader using the given DataInputStream.
     * 
     * @param dis
     */
    public BinaryGraphWriterInsa2018() {
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

    protected Map<RoadInformation, Integer> getRoadInformations(List<Vertex> nodes) {
        Map<RoadInformation, Integer> rinfos = new HashMap<>();
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

        dos.write(Arrays.copyOf(graph.getMapId().getBytes("UTF-8"), MAP_ID_FIELD_LENGTH));
        dos.writeUTF(graph.getMapName());

        List<Vertex> nodes = graph.getNodes();
        Map<RoadInformation, Integer> infos = getRoadInformations(nodes);

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
            dos.writeLong(info.getAccess());
            dos.writeUTF(info.getName());
        }

        // Check format.
        dos.writeByte(254);

        // Read successors and convert to arcs.
        for (Vertex node: nodes) {
            for (Arc arc: node.getSuccessors()) {

                // Read target node number.
                write24bits((int) arc.getDestination().getId());

                // Read information number.
                write24bits(infos.get(arc.getInfo()));

                // Length of the arc.
                dos.writeInt((int) (arc.getLength() * 1000));

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

    @Override
    public void validate(OSM2GraphConfiguration configuration) throws IllegalArgumentException {
        byte[] bytes = null;
        try {
            bytes = configuration.getMapId().getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Cannot encode the specified ID using UTF-8.");
        }
        if (bytes.length > MAP_ID_FIELD_LENGTH) {
            throw new IllegalArgumentException("Specified ID is too long, ID must be less than "
                    + MAP_ID_FIELD_LENGTH + " bytes long in UTF-8.");
        }

        if (configuration.getMapName() == null) {
            throw new IllegalArgumentException(
                    "A map name must be specified in order to use this writer.");
        }
    }

}
