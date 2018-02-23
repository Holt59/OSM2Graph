package org.laas.osm2graph.writers;

import java.io.IOException;
import java.io.OutputStream;

import org.laas.osm2graph.graph.Graph;

public interface GraphWriter {

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

}
