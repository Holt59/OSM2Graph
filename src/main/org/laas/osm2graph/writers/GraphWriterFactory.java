package org.laas.osm2graph.writers;

import java.util.HashMap;
import java.util.Map;

public class GraphWriterFactory {

    // Default writer.
    public final static String DEFAULT_WRITER = "insa2018";

    // Allowed writer
    public final static Map<String, Class<? extends GraphWriter>> ALLOWED_WRITERS = new HashMap<String, Class<? extends GraphWriter>>();

    /**
     * @param name
     * @return
     */
    public static final GraphWriter graphWriterFromName(String name) {
        try {
            return ALLOWED_WRITERS.getOrDefault(name.toLowerCase(), null).newInstance();
        }
        catch (InstantiationException e) {
            return null;
        }
        catch (IllegalAccessException e) {
            return null;
        }
    }

    static {
        ALLOWED_WRITERS.put("insa2016", BinaryGraphWriterInsa2016.class);
        ALLOWED_WRITERS.put("insa2018", BinaryGraphWriterInsa2018.class);
    }

}
