package org.laas.osm2graph.osmosis;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;

import java.util.HashMap;
import java.util.Map;

public class OSM2GraphPluginLoader implements PluginLoader {

    @Override
    public Map<String, TaskManagerFactory> loadTaskFactories() {
        OSM2GraphFactory osm2GraphFactory = new OSM2GraphFactory();
        HashMap<String, TaskManagerFactory> map = new HashMap<>();
        map.put("osm2graph", osm2GraphFactory);
        return map;
    }
}
