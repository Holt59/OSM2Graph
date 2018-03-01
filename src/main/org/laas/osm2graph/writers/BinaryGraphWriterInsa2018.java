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

/**
 * This writer generates files that are used for practice session at INSA of
 * Toulouse.
 * 
 * @author Mikael
 *
 */
public class BinaryGraphWriterInsa2018 implements GraphWriter {

    // Map version and magic number targeted for this reader.
    private static final int VERSION = 6;
    private static final int MAGIC_NUMBER = 0x208BC3B3;

    private static final String DEFAULT_EXTENSION = "mapgr";

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
        case ROAD:
            return 'l';
        case LIVING_STREET:
            return 'm';
        case SERVICE:
            return 'n';
        case ROUNDABOUT:
            return 'o';
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

        dos.writeInt(graph.getMapId());

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
            dos.writeShort((short) info.getAccess());
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
                dos.writeShort(arc.getLength());

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

}
