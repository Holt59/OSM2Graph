package org.laas.osm2graph.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.laas.osm2graph.graph.RoadInformation.RoadType;

public class AccessData {

    /*-
     * Type of "vehicle"
     *   - Non-vehicle:
     *      - Foot
     *   - Vehicle:
     *      - Non-motorized:
     *          - bicycle
     *      - Motorized:
     *          - Motorcycle
     *          - Moped & Mofa
     *          - Motorcar
     *          - Public service:
     *              - Bus / Minibus / Share taxi / Taxi
     */

    // Useful tags for access information, order is important!
    public final static List<String> USEFUL_TAGS = Arrays
            .asList(new String[] { "access", "foot", "vehicle", "bicycle", "motor_vehicle", "motorcycle", "moped",
                    "mofa", "motorcar", "agricultural", "hgv", "psv", "bus", "minibus", "share_taxi" });

    /*
     * 4 bits are associated to each type of vehicle, these 4 bits represents the
     * type of access (see below).
     * 
     * Note: The highest 4 bits of the long are not used, for compatibility issue
     * (unsigned/signed... ).
     */

    // @formatter:off
    // These masks indicates which bit should be set for the access value.
    public static final long MASK_YES = 0x111111111111111L, // *=yes
            MASK_NO           = 0x0L, // *=no,
            MASK_PRIVATE      = 0x222222222222222L, // *=private
            MASK_DESTINATION  = 0x333333333333333L, // *=destination
            MASK_DELIVERY     = 0x444444444444444L, // *=delivery
            MASK_CUSTOMERS    = 0x555555555555555L, // *=customers,
            MASK_FORESTRY     = 0x666666666666666L, // *=forestry,*=agricultural
            MASK_UNKNOWN = 0xfffffffffffffffL;

    // These masks indicates which parts of the long should be set for each type of
    // vehicle
    public static final long 
            MASK_ALL              = 0xfffffffffffffffL, // access=*,
            MASK_FOOT             = 0x00000000000000fL, // foot=*
            MASK_VEHICLE          = 0xfffffffffffff00L, // vehicle=*
            MASK_BICYCLE          = 0x000000000000f00L, // bicycle=*
            MASK_MOTOR_VEHICLE    = 0xffffffffffff000L, // motor_vehicle=*
            MASK_SMALL_MOTORCYCLE = 0x00000000000f000L, // moped,mofa=*
            MASK_AGRICULTURAL     = 0x0000000000f0000L, // agricultural=*
            MASK_MOTORCYCLE       = 0x000000000f00000L, // motorcycle=*
            MASK_MOTORCAR         = 0x00000000f000000L, // motorcar=*
            MASK_HEAVY_GOODS      = 0x0000000f0000000L, // motorcar=*
            MASK_PUBLIC_TRANSPORT = 0x0000f0000000000L; // psv,bus,minibus,share_taxi=*
    // @formatter:on

    // Map key / value with their mask
    private static final Map<String, Long> KEY_TO_MASK = new HashMap<>();
    private static final Map<String, Long> VALUE_TO_MASK = new HashMap<>();

    static {
        // Puts keys
        KEY_TO_MASK.put("access", MASK_ALL);
        KEY_TO_MASK.put("foot", MASK_FOOT);
        KEY_TO_MASK.put("vehicle", MASK_VEHICLE);
        KEY_TO_MASK.put("bicycle", MASK_BICYCLE);
        KEY_TO_MASK.put("motor_vehicle", MASK_MOTOR_VEHICLE);
        KEY_TO_MASK.put("motorcycle", MASK_MOTORCYCLE);
        KEY_TO_MASK.put("moped", MASK_SMALL_MOTORCYCLE);
        KEY_TO_MASK.put("mofa", MASK_SMALL_MOTORCYCLE);
        KEY_TO_MASK.put("motorcar", MASK_MOTORCAR);
        KEY_TO_MASK.put("agricultural", MASK_AGRICULTURAL);
        KEY_TO_MASK.put("hgv", MASK_HEAVY_GOODS);
        KEY_TO_MASK.put("psv", MASK_PUBLIC_TRANSPORT);
        KEY_TO_MASK.put("bus", MASK_PUBLIC_TRANSPORT);
        KEY_TO_MASK.put("minibus", MASK_PUBLIC_TRANSPORT);
        KEY_TO_MASK.put("share_taxi", MASK_PUBLIC_TRANSPORT);

        // Puts value
        KEY_TO_MASK.put("yes", MASK_YES);
        KEY_TO_MASK.put("true", MASK_YES);
        KEY_TO_MASK.put("1", MASK_YES);
        KEY_TO_MASK.put("no", MASK_NO);
        KEY_TO_MASK.put("false", MASK_NO);
        KEY_TO_MASK.put("0", MASK_NO);
        KEY_TO_MASK.put("private", MASK_PRIVATE);
        KEY_TO_MASK.put("permissive", MASK_YES);
        KEY_TO_MASK.put("destination", MASK_DESTINATION);
        KEY_TO_MASK.put("delivery", MASK_DELIVERY);
        KEY_TO_MASK.put("customers", MASK_CUSTOMERS);
        KEY_TO_MASK.put("designated", MASK_YES);
        KEY_TO_MASK.put("use_sidepath", MASK_YES);
        KEY_TO_MASK.put("dismount", MASK_YES);
        KEY_TO_MASK.put("agricultural", MASK_FORESTRY);
        KEY_TO_MASK.put("forestry", MASK_FORESTRY);
        KEY_TO_MASK.put("discouraged", MASK_NO);
        KEY_TO_MASK.put("unknown", MASK_UNKNOWN);

    }

    /**
     * Retrieve access from road type, if possible.
     * 
     * @param roadtype
     * 
     * @return
     */
    public static long getDefaultAccessForRoadType(RoadType roadtype) {
        if (roadtype == null) {
            return MASK_ALL & MASK_UNKNOWN;
        }

        // Handle normal value
        switch (roadtype) {
        case MOTORWAY:
        case MOTORWAY_LINK:
        case TRUNK:
        case TRUNK_LINK:
            return (MASK_MOTOR_VEHICLE & (~MASK_SMALL_MOTORCYCLE) & (~MASK_AGRICULTURAL)) & MASK_YES;
        case PRIMARY:
        case PRIMARY_LINK:
        case SECONDARY:
        case SECONDARY_LINK:
        case TERTIARY:
        case RESIDENTIAL:
        case LIVING_STREET:
        case ROUNDABOUT:
            return MASK_ALL & MASK_YES;
        case SERVICE:
            return MASK_ALL & MASK_YES;
        case TRACK:
            return (MASK_ALL & (~MASK_PUBLIC_TRANSPORT) & (~MASK_HEAVY_GOODS)) & MASK_YES;
        case BICYCLE:
            return MASK_BICYCLE & MASK_YES;
        case PEDESTRIAN:
            return (MASK_FOOT | MASK_BICYCLE) & MASK_YES;
        case COASTLINE:
        case UNCLASSIFIED:
            return MASK_ALL & MASK_UNKNOWN;
        }
        return MASK_ALL & MASK_UNKNOWN;

    }

    /**
     * Get access type associated with the given tags.
     * 
     * @param tags
     */
    public static long getAccessType(Map<String, String> tags, RoadType roadType) {

        long access = getDefaultAccessForRoadType(roadType);

        for (String key: AccessData.USEFUL_TAGS) {
            if (!tags.containsKey(key)) {
                continue;
            }
            String value = tags.get(key);
            long maskKey = KEY_TO_MASK.get(key);
            long maskValue = VALUE_TO_MASK.getOrDefault(value.toLowerCase(), MASK_UNKNOWN);

            access = (maskKey & maskValue) | (access & ~maskKey);
        }

        return access;
    }

}
