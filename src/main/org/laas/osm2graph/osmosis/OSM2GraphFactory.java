package org.laas.osm2graph.osmosis;

import org.laas.osm2graph.model.OSM2GraphConfiguration;
import org.laas.osm2graph.writers.GraphWriterFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkManager;

public class OSM2GraphFactory extends TaskManagerFactory {

    /**
     * Default name for out file.
     */
    public static final String DEFAULT_PARAM_OUTFILE = "mapsforge.map";

    private static final String PARAM_OUTFILE = "file";
    private static final String PARAM_PREFERRED_LANGUAGES = "preferred-languages";
    private static final String PARAM_THREADS = "threads";
    private static final String PARAM_MAPID = "id";
    private static final String PARAM_MAPNAME = "name";
    private static final String PARAM_WRITER = "writer";

    @Override
    protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
        OSM2GraphConfiguration configuration = new OSM2GraphConfiguration();
        configuration.addGraphWriter(
                getStringArgument(taskConfig, PARAM_WRITER, GraphWriterFactory.DEFAULT_WRITER));
        configuration.addOutputFile(getStringArgument(taskConfig, PARAM_OUTFILE));
        configuration.addPreferredLanguages(
                getStringArgument(taskConfig, PARAM_PREFERRED_LANGUAGES, null));
        configuration.setMapId(getStringArgument(taskConfig, PARAM_MAPID, null));
        configuration.setMapName(getStringArgument(taskConfig, PARAM_MAPNAME, null));
        configuration.setThreads(getIntegerArgument(taskConfig, PARAM_THREADS, 1));

        configuration.validate();

        OSM2GraphTask task = new OSM2GraphTask(configuration);
        return new SinkManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
    }

}
