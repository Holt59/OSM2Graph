package org.laas.osm2graph.model;

import java.util.Arrays;
import java.util.List;

import org.laas.osm2graph.graph.RoadInformation.RoadType;

public class AccessData {

    /*- // Hyphen preventing Eclipse from formatting this block....
     * 
     * 16 bits: 
     *   - 0 Unknown
     *   - 1 Private
     *   - 2 Agricultural/Forestry
     *   - 3 Service
     *   - 4 Public Transport
     *   - 5
     *   - 6
     *   - 7
     *   - 8 Foot
     *   - 9 Bicycle 
     *   - 10 Small motorcycle
     *   - 11 Motorcycle 
     *   - 12 Motorcar 
     *   - 13 Bus
     *   - 14
     *   - 15
     */

    // Somem asks...
    public static final int MASK_UNKNOWN = 0x01;
    public static final int MASK_PRIVATE = 0x02;
    public static final int MASK_AGRICULTURAL = 0x04;
    public static final int MASK_SERVICE = 0x08;
    public static final int MASK_PUBLIC_TRANSPORT = 0x10;

    public static final int MASK_ALL = 0xFF;
    public static final int MASK_FOOT = 0x01;
    public static final int MASK_VEHICLE = 0xFE;
    public static final int MASK_BICYCLE = 0x02;
    public static final int MASK_MOTOR_VEHICLE = 0xFC;
    public static final int MASK_MOTORCYCLE = 0x0C;
    public static final int MASK_SMALL_MOTORCYCLE = 0x08;
    public static final int MASK_MOTORCAR = 0x10;
    public static final int MASK_BUS = 0x20;

    // Useful tags for access information, order is important!
    public final static List<String> USEFUL_TAGS = Arrays
            .asList(new String[]{ "access", "foot", "vehicle", "bicycle", "motor_vehicle",
                    "motorcycle", "moped", "mofa", "motorcar", "psv", "bus", "minibus" });

    // Masks for each tag (if != no), this correspond to the second byte.
    public final static int[] KEY_MASK = new int[]{ //
            MASK_ALL, // all access
            MASK_FOOT, // foot (bits 0)
            MASK_VEHICLE, // vehicle (bits 1 to 7)
            MASK_BICYCLE, // bicycle (bits 1)
            MASK_MOTOR_VEHICLE, // motor_vehicle (bits 2 to 7)
            MASK_MOTORCYCLE, // motorcycle (bits 2 and 3)
            MASK_SMALL_MOTORCYCLE, // moped (bits 3)
            MASK_SMALL_MOTORCYCLE, // mofa (bits 3)
            MASK_MOTORCAR, // motorcar (bits 4)
            MASK_BUS, // PSV (bits 5)
            MASK_BUS, // buses (bits 5)
            MASK_BUS // minibuses (bits 5)
    };

    /**
     * Retrieve access from road type, if possible.
     * 
     * @param roadtype
     * 
     * @return
     */
    public static int accessForRoadType(RoadType roadtype) {
        if (roadtype == null) {
            return MASK_UNKNOWN;
        }

        // Handle normal value
        switch (roadtype) {
        case MOTORWAY:
        case TRUNK:
        case PRIMARY_LINK:
        case SECONDARY_LINK:
        case MOTORWAY_LINK:
        case TRUNK_LINK:
            return (MASK_MOTOR_VEHICLE & (~MASK_SMALL_MOTORCYCLE)) << 8;
        case PRIMARY:
            return MASK_ALL << 8;
        case SERVICE:
            return (MASK_ALL << 8) | MASK_SERVICE;
        default:
            return MASK_UNKNOWN;
        }

    }

}
