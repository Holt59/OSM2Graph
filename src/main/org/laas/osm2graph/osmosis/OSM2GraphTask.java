/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org Copyright 2015-2017 devemux86
 * Copyright 2017 Gustl22 This program is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.laas.osm2graph.osmosis;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.laas.osm2graph.graph.Arc;
import org.laas.osm2graph.graph.Graph;
import org.laas.osm2graph.graph.Vertex;
import org.laas.osm2graph.model.NodeToVertex;
import org.laas.osm2graph.model.OSM2GraphConfiguration;
import org.laas.osm2graph.model.WayToArc;
import org.laas.osm2graph.writers.GraphWriter;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

/**
 * An Osmosis plugin that reads OpenStreetMap data and converts it to a graph
 * binary file.
 */
public class OSM2GraphTask implements Sink {

    private static final Logger LOGGER = Logger.getLogger(OSM2GraphTask.class.getName());

    // Accounting
    private int amountOfNodesProcessed = 0;
    private int amountOfWaysProcessed = 0;

    private final OSM2GraphConfiguration configuration;

    // Converters & Filters
    private NodeToVertex nodeToVertex = new NodeToVertex();

    // Nodes and ways
    Map<Long, Vertex> vertices;
    ArrayList<Way> ways;

    OSM2GraphTask(OSM2GraphConfiguration configuration) {
        this.configuration = configuration;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.openstreetmap.osmosis.core.task.v0_6.Initializable#initialize(java.util.
     * Map)
     */
    @Override
    public void initialize(Map<String, Object> metadata) {
        this.vertices = new HashMap<Long, Vertex>();
        this.ways = new ArrayList<Way>();
    }

    @Override
    public final void close() {
    }

    @Override
    public final void complete() {

        NumberFormat nfMegabyte = NumberFormat.getInstance();
        NumberFormat nfCounts = NumberFormat.getInstance();
        nfCounts.setGroupingUsed(true);
        nfMegabyte.setMaximumFractionDigits(2);

        LOGGER.info("creating graph using " + this.configuration.getThreads() + "threads...");

        Instant start = Instant.now();
        ArrayList<Arc> arcs = new WayToArc(this.vertices, this.configuration).convert(this.ways);
        LOGGER.info("converted " + ways.size() + " ways to " + arcs.size() + " arcs in "
                + Duration.between(start, Instant.now()) + ".");

        LOGGER.info("retrieving vertices from arcs... ");
        ArrayList<Vertex> nodes = new ArrayList<Vertex>(2 * arcs.size());
        for (int i = 0; i < arcs.size(); ++i) {
            Arc arc = arcs.get(i);
            Vertex oldOrigin = arc.getOrigin(), oldDestination = arc.getDestination();
            long originId = oldOrigin.getId(), destinationId = oldDestination.getId();
            if (vertices.get(originId) == oldOrigin) {
                oldOrigin = new Vertex(nodes.size(), oldOrigin.getPoint());
                nodes.add(oldOrigin);
                vertices.put(originId, oldOrigin);
            }
            if (vertices.get(destinationId) == oldDestination) {
                oldDestination = new Vertex(nodes.size(), oldDestination.getPoint());
                nodes.add(oldDestination);
                vertices.put(destinationId, oldDestination);
            }
            new Arc(arc.getId(), vertices.get(originId), vertices.get(destinationId),
                    arc.getLength(), arc.getInfo(), arc.getPoints());

            // Hint to GC
            arcs.set(i, null);
            arc = null;
        }
        nodes.sort(new Comparator<Vertex>() {
            @Override
            public int compare(Vertex v1, Vertex v2) {
                return v1.compareTo(v2);
            }
        });

        LOGGER.info("Created " + arcs.size() + " arcs out of " + amountOfWaysProcessed + " ways "
                + "and " + nodes.size() + " vertex out of " + amountOfNodesProcessed + " nodes.");

        LOGGER.info("start writing file...");

        try {
            Graph graph = new Graph(this.configuration.getMapId(), this.configuration.getMapName(),
                    nodes);
            if (this.configuration.getOutputFile().exists()) {
                LOGGER.info(
                        "overwriting file " + this.configuration.getOutputFile().getAbsolutePath());
                this.configuration.getOutputFile().delete();
            }
            GraphWriter writer = this.configuration.getGraphWriter();
            writer.setOutputStream(new BufferedOutputStream(
                    new FileOutputStream(this.configuration.getOutputFile())));
            writer.writeGraph(graph);
        }
        catch (IOException e) {
            LOGGER.log(Level.SEVERE, "error while writing file", e);
        }

        LOGGER.info("finished...");
        LOGGER.fine("total processed nodes: " + nfCounts.format(this.amountOfNodesProcessed));
        LOGGER.fine("total processed ways: " + nfCounts.format(this.amountOfWaysProcessed));

        LOGGER.info("estimated memory consumption: " + nfMegabyte
                .format(+((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                        / Math.pow(1024, 2)))
                + "MB");
    }

    @Override
    public final void process(EntityContainer entityContainer) {
        Entity entity = entityContainer.getEntity();

        switch (entity.getType()) {
        case Bound:
            break;

        // ********************************************************
        // ****************** NODE PROCESSING *********************
        // ********************************************************
        case Node:
            this.vertices.put(entity.getId(), this.nodeToVertex.convert((Node) entity));
            // hint to GC
            entity = null;
            this.amountOfNodesProcessed++;
            break;

        // ********************************************************
        // ******************* WAY PROCESSING *********************
        // ********************************************************
        case Way:
            this.ways.add((Way) entity);
            // hint to GC
            entity = null;
            this.amountOfWaysProcessed++;
            break;

        // ************************************************************
        // ****************** RELATION PROCESSING *********************
        // ************************************************************
        case Relation:
            break;
        }
    }
}