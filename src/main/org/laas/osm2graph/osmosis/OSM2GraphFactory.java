package org.laas.osm2graph.osmosis;

import org.laas.osm2graph.model.OSM2GraphConfiguration;
import org.laas.osm2graph.utils.Constants;
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

    private static final String PARAM_BBOX = "bbox";
    private static final String PARAM_OUTFILE = "file";
    private static final String PARAM_PREFERRED_LANGUAGES = "preferred-languages";
    private static final String PARAM_THREADS = "threads";
    private static final String PARAM_MAPID = "map-id";
    private static final String PARAM_WRITER = "writer";

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
    protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
        OSM2GraphConfiguration configuration = new OSM2GraphConfiguration();
        configuration.addGraphWriter(
                getStringArgument(taskConfig, PARAM_WRITER, GraphWriterFactory.DEFAULT_WRITER));
        configuration.addOutputFile(
                getStringArgument(taskConfig, PARAM_OUTFILE, Constants.DEFAULT_PARAM_OUTFILE));
        configuration.addBboxConfiguration(getStringArgument(taskConfig, PARAM_BBOX, null));
        configuration.addPreferredLanguages(
                getStringArgument(taskConfig, PARAM_PREFERRED_LANGUAGES, null));
        configuration.setMapId(parseMapId(getStringArgument(taskConfig, PARAM_MAPID)));
        configuration.setThreads(getIntegerArgument(taskConfig, PARAM_THREADS, 1));

        configuration.validate();

        OSM2GraphTask task = new OSM2GraphTask(configuration);
        return new SinkManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
    }

}
