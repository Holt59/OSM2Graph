package org.laas.osm2graph.model;

import java.util.HashMap;
import java.util.Map;

import org.laas.osm2graph.graph.RoadInformation.RoadType;

public class RoadTypeData {

    // Map highway -> Road type
    private final static Map<String, RoadType> HIGHWAY_TO_ROADTYPE = new HashMap<>();

    static {

        // Add all basic roadtype
        for (RoadType roadtype: RoadType.values()) {
            HIGHWAY_TO_ROADTYPE.put(roadtype.toString().toLowerCase(), roadtype);
        }

        // Add extra ones
        HIGHWAY_TO_ROADTYPE.put("footway", RoadType.PEDESTRIAN);
        HIGHWAY_TO_ROADTYPE.put("steps", RoadType.PEDESTRIAN);
        HIGHWAY_TO_ROADTYPE.put("bridleway", RoadType.PEDESTRIAN);

        HIGHWAY_TO_ROADTYPE.put("cycleway", RoadType.BICYCLE);
    }

    /**
     * Find the road type associated with the given way by looking at its highway or
     * natural tag. Way should have been filtered prior to avoid having wrong
     * "highway" value without a natural tag.
     * 
     * Check RoadInformation.RoadType for the list of allowed highway value. If a
     * way has a tag "natural=coastline", the highway tag is discarded.
     * 
     * @param tags Map of key -> value tags.
     * 
     * @return Road type associated to the given way, or null if none was found.
     */
    public static RoadType getRoadType(Map<String, String> tags) {

        // natural=coastline -> Coastline
        if (tags.getOrDefault("natural", "").toLowerCase().equals("coastline")) {
            return RoadType.COASTLINE;
        }

        // junction=roundabout -> Roundabout
        if (tags.getOrDefault("junction", "").toLowerCase().equals("roundabout")) {
            return RoadType.ROUNDABOUT;
        }

        if (!tags.containsKey("highway")) {
            return RoadType.UNCLASSIFIED;
        }

        return HIGHWAY_TO_ROADTYPE.getOrDefault(tags.get("highway").toLowerCase(), RoadType.UNCLASSIFIED);
    }
}
