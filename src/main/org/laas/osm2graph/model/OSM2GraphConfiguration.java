/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org Copyright 2015 lincomatic
 * Copyright 2015-2017 devemux86 Copyright 2016 mikes222 Copyright 2017 Gustl22
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.laas.osm2graph.model;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.laas.osm2graph.writers.GraphWriter;
import org.laas.osm2graph.writers.GraphWriterFactory;

/**
 * Configuration for the map file writer.
 */
public class OSM2GraphConfiguration {

    private File outputFile;
    private List<String> preferredLanguages;
    private String writerVersion;
    private GraphWriter writer;
    private int threads;

    private String mapId = null;
    private String mapName = null;

    /**
     * Convenience method - The writer should be set before calling this method so
     * that default extension can be infered.
     *
     * @param file the path to the output file
     */
    public void addOutputFile(String file) {
        if (this.writer == null) {
            throw new InternalError("must set writer before adding an output file");
        }
        if (file != null) {
            if (file.lastIndexOf('.') == -1) {
                file = file + '.' + this.writer.getDefaultExtension();
            }
            File f = new File(file);
            if (f.isDirectory()) {
                throw new IllegalArgumentException(
                        "output file parameter points to a directory, must be a file");
            }
            else if (f.exists() && !f.canWrite()) {
                throw new IllegalArgumentException(
                        "output file parameter points to a file we have no write permissions");
            }

            setOutputFile(f);
        }
    }

    /**
     * Convenience method.
     * 
     * @param writerType name of the writer to use.
     */
    public void addGraphWriter(String writerType) {
        GraphWriter writer = GraphWriterFactory.graphWriterFromName(writerType);
        if (writer == null) {
            throw new IllegalArgumentException("unrecognized writer: " + writerType);
        }
        setGraphWriter(writer);
    }

    /**
     * Convenience method.
     *
     * @param preferredLanguages the preferred language(s) separated with ','
     */
    public void addPreferredLanguages(String preferredLanguages) {
        if (preferredLanguages != null && !preferredLanguages.trim().isEmpty()) {
            setPreferredLanguages(Arrays.asList(preferredLanguages.split(",")));
        }
    }

    /**
     * @return the outputFile
     */
    public File getOutputFile() {
        return this.outputFile;
    }

    /**
     * @return the map ID.
     */
    public String getMapId() {
        return this.mapId;
    }

    /**
     * @return the map name.
     */
    public String getMapName() {
        return this.mapName;
    }

    /**
     * @return the preferred language(s)
     */
    public List<String> getPreferredLanguages() {
        return this.preferredLanguages;
    }

    /**
     * @return the graph writer
     */
    public GraphWriter getGraphWriter() {
        return this.writer;
    }

    /**
     * @return the threads
     */
    public int getThreads() {
        return this.threads;
    }

    /**
     * @return the writerVersion
     */
    public String getWriterVersion() {
        return this.writerVersion;
    }

    /**
     * @param outputFile the outputFile to set
     */
    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    /**
     * @param id Map ID to set.
     */
    public void setMapId(String id) {
        this.mapId = id;
    }

    /**
     * @param name Map name to set.
     */
    public void setMapName(String name) {
        this.mapName = name;
    }

    /**
     * @param writer writer to set
     */
    public void setGraphWriter(GraphWriter writer) {
        this.writer = writer;
    }

    /**
     * @param preferredLanguages the preferred language(s) to set
     */
    public void setPreferredLanguages(List<String> preferredLanguages) {
        this.preferredLanguages = preferredLanguages;
    }

    /**
     * @param threads the threads to set
     */
    public void setThreads(int threads) {
        this.threads = threads;
    }

    /**
     * @param writerVersion the writerVersion to set
     */
    public void setWriterVersion(String writerVersion) {
        this.writerVersion = writerVersion;
    }

    /**
     * Validates this configuration.
     *
     * @throws IllegalArgumentException thrown if configuration is invalid
     */
    public void validate() throws IllegalArgumentException {
        File parent = getOutputFile().getAbsoluteFile().getParentFile();
        if (!parent.exists()) {
            throw new IllegalArgumentException(
                    "Directory " + parent.getAbsolutePath() + " does not exist.");
        }
        getGraphWriter().validate(this);
    }
}