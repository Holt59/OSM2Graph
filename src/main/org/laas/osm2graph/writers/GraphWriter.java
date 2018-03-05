package org.laas.osm2graph.writers;

import java.io.IOException;
import java.io.OutputStream;

import org.laas.osm2graph.graph.Graph;
import org.laas.osm2graph.model.OSM2GraphConfiguration;

public interface GraphWriter {

    /**
     * Set the output stream for this writer.
     * 
     * @param stream
     */
    public void setOutputStream(OutputStream stream);

    /**
     * Write a graph using this writer.
     * 
     * @throws IOException
     */
    public void writeGraph(Graph graph) throws IOException;

    /**
     * @return Default extension for this writer.
     */
    public String getDefaultExtension();

    /**
     * Validate the given configuration for the writer.
     * 
     * @param configuration Configuration to validate.
     * 
     * @throws IllegalArgumentException if the configuration is invalid.
     */
    public void validate(OSM2GraphConfiguration configuration) throws IllegalArgumentException;

}
