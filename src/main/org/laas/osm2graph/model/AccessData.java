package org.laas.osm2graph.model;

import java.util.Arrays;
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

    // These masks indicates which bit should be set for the access value.
    public static final long MASK_YES = 0x111111111111111L, // *=yes
            MASK_NO = 0x0L, // *=no,
            MASK_PRIVATE = 0x222222222222222L, // *=private
            MASK_TARGET = 0x333333333333333L, // *=delivery,destination,customers
            MASK_FORESTRY = 0x444444444444444L, // *=agricultural,forestry
            MASK_UNKNOWN = 0xfffffffffffffffL;

    // These masks indicates which parts of the long should be set for each type of
    // vehicle
    // @formatter:off
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

    /**
     * Retrieve access from road type, if possible.
     * 
     * @param roadtype
     * 
     * @return
     */
    public static long accessForRoadType(RoadType roadtype) {
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
            return MASK_ALL & MASK_TARGET;
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
    public static short getAccessType(Map<String, String> tags, RoadType roadType) {

        int access = 0;
        for (int i = 0; i < AccessData.USEFUL_TAGS.size(); ++i) {
            String key = AccessData.USEFUL_TAGS.get(i);
            if (tags.containsKey(key)) {
                String value = tags.get(key).toLowerCase();

                // If authorized
                if (!value.equals("no") && !value.equals("false") && !value.equals("0")) {
                    access = access | (AccessData.KEY_MASK[i] << 8);
                }

                // Check the actual value...
                if (key.equals("access") && (value.equals("yes") || value.equals("true") || value.equals("1"))) {
                    access = access | AccessData.MASK_ALL;
                }
                else if (value.equals("private")) {
                    // Nothing to do...
                    access = access | AccessData.MASK_PRIVATE;
                }
                else if (value.equals("delivery") || value.equals("customers")) {
                    access = access | AccessData.MASK_SERVICE;
                }
                else if (value.equals("agricultural") || value.equals("forestry")) {
                    access = access | AccessData.MASK_AGRICULTURAL;
                }
            }
        }

        // Access = 0, try to find access from roadtype
        if (access == 0) {
            access = AccessData.accessForRoadType(roadType);
        }
        return (short) access;
    }

}
