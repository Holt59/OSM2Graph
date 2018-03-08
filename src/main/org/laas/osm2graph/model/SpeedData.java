package org.laas.osm2graph.model;

import java.util.HashMap;
import java.util.Map;

import org.laas.osm2graph.graph.RoadInformation.RoadType;

/**
 * This class contains speed data extracted from OpenStreetMap website.
 * 
 * @author Mikael
 *
 */
public class SpeedData {

    //
    private static final Map<String, Integer> SPEED_FOR_COUNTRIES = new HashMap<String, Integer>();

    // Default maximum speed.
    private final static int DEFAULT_MAXIMUM_SPEED = 50;

    // Default walk speed
    private final static int DEFAULT_WALK_SPEED = 5;

    // Default bicycle speed
    private final static int DEFAULT_BICYCLE_SPEED = 14;

    /**
     * Try to infer the maximum speed from the given string (which should come from
     * a "maxspeed" tag), and use the given road type as a fallback.
     * 
     * @param maxspeed Value of a "maxspeed" tag, or null.
     * @param roadtype Road type to infer speed if "maxspeed" was not sufficient.
     * 
     * @return Maximum speed in kmph.
     */
    public static int getMaximumSpeed(Map<String, String> tags, RoadType roadtype) {
        String maxspeed = tags.getOrDefault("maxspeed", null);
        final int defaultSpeed = SpeedData.maxSpeedForRoadType(roadtype, DEFAULT_MAXIMUM_SPEED);
        if (maxspeed == null) {
            return defaultSpeed;
        }
        maxspeed = maxspeed.toLowerCase();
        if (maxspeed.equals("none") || maxspeed.equals("signal")) {
            return defaultSpeed;
        }
        if (maxspeed.equals("walk")) {
            return DEFAULT_WALK_SPEED;
        }
        int speed = DEFAULT_MAXIMUM_SPEED;
        if (maxspeed.contains(":")) {
            // Implicit speed
            speed = SpeedData.speedForCode(maxspeed, defaultSpeed);
        }
        else {
            // Numeric speed
            String[] parts = maxspeed.split(" ");
            try {
                speed = Integer.valueOf(parts[0]);
            }
            catch (NumberFormatException exception) {
                speed = defaultSpeed;
            }

            if (parts.length == 1) {
                return speed;
            }
            String unit = parts[1];
            if (unit.equals("knots")) {
                return (int) (speed * 1.852);
            }
            if (unit.equals("mph")) {
                return (int) (speed * 1.609);
            }
            return speed;
        }
        return speed;
    }

    /**
     * Fetch the speed for the given code in the static table. If the code is not
     * found, return defaultSpeed.
     * 
     * @param code Code to fetch.
     * @param defaultSpeed Default speed to return if their is no speed associated
     *        to the given code.
     * 
     * @return The speed limits for the given code, or defaultSpeed if it was not
     *         found.
     */
    public static int speedForCode(String code, int defaultSpeed) {
        return SPEED_FOR_COUNTRIES.getOrDefault(code, defaultSpeed);
    }

    /**
     * Get the maximum speed for the given road type.
     * 
     * @param roadtype
     * @param defaultSpeed Default speed to return if roadtype is null or does not
     *        have an associated maximum speed.
     * 
     * @return Maximum speed for the given road type, or defaultSpeed is none
     *         exists.
     */
    public static int maxSpeedForRoadType(RoadType roadtype, int defaultSpeed) {
        // Handle null value...
        if (roadtype == null) {
            return defaultSpeed;
        }

        // Handle normal value
        switch (roadtype) {
        case MOTORWAY:
            return 130;
        case TRUNK:
            return 110;
        case PRIMARY:
            return 90;
        case SECONDARY:
            return 70;
        case PRIMARY_LINK:
        case SECONDARY_LINK:
        case MOTORWAY_LINK:
        case TRUNK_LINK:
        case TERTIARY:
            return 50;
        case RESIDENTIAL:
        case SERVICE:
        case ROUNDABOUT:
        case LIVING_STREET:
        case UNCLASSIFIED:
        case TRACK:
            return 30;
        case BICYCLE:
            return DEFAULT_BICYCLE_SPEED;
        case PEDESTRIAN:
            return DEFAULT_WALK_SPEED;
        case COASTLINE:
            return 0;
        }

        return 0;
    }

    static {
        SPEED_FOR_COUNTRIES.put("AT:urban", 50);
        SPEED_FOR_COUNTRIES.put("AT:rural", 100);
        SPEED_FOR_COUNTRIES.put("AT:trunk", 100);
        SPEED_FOR_COUNTRIES.put("AT:motorway", 130);
        SPEED_FOR_COUNTRIES.put("BE:urban", 50);
        SPEED_FOR_COUNTRIES.put("BE:rural", 90);
        SPEED_FOR_COUNTRIES.put("BE:trunk", 120);
        SPEED_FOR_COUNTRIES.put("BE:motorway", 120);
        SPEED_FOR_COUNTRIES.put("CH:urban", 50);
        SPEED_FOR_COUNTRIES.put("CH:rural", 80);
        SPEED_FOR_COUNTRIES.put("CH:trunk", 100);
        SPEED_FOR_COUNTRIES.put("CH:motorway", 120);
        SPEED_FOR_COUNTRIES.put("CZ:urban", 50);
        SPEED_FOR_COUNTRIES.put("CZ:rural", 90);
        SPEED_FOR_COUNTRIES.put("CZ:trunk", 110);
        SPEED_FOR_COUNTRIES.put("CZ:motorway", 130);
        SPEED_FOR_COUNTRIES.put("DK:urban", 50);
        SPEED_FOR_COUNTRIES.put("DK:rural", 80);
        SPEED_FOR_COUNTRIES.put("DK:motorway", 130);
        SPEED_FOR_COUNTRIES.put("DE:living_street", 7);
        SPEED_FOR_COUNTRIES.put("DE:urban", 50);
        SPEED_FOR_COUNTRIES.put("DE:rural", 100);
        SPEED_FOR_COUNTRIES.put("DE:motorway", 130);
        SPEED_FOR_COUNTRIES.put("FI:urban", 50);
        SPEED_FOR_COUNTRIES.put("FI:rural", 80);
        SPEED_FOR_COUNTRIES.put("FI:trunk", 100);
        SPEED_FOR_COUNTRIES.put("FI:motorway", 120);
        SPEED_FOR_COUNTRIES.put("FR:urban", 50);
        SPEED_FOR_COUNTRIES.put("FR:rural", 90);
        SPEED_FOR_COUNTRIES.put("FR:trunk", 110);
        SPEED_FOR_COUNTRIES.put("FR:motorway", 130);
        SPEED_FOR_COUNTRIES.put("GR:urban", 50);
        SPEED_FOR_COUNTRIES.put("GR:rural", 90);
        SPEED_FOR_COUNTRIES.put("GR:trunk", 110);
        SPEED_FOR_COUNTRIES.put("GR:motorway", 130);
        SPEED_FOR_COUNTRIES.put("HU:urban", 50);
        SPEED_FOR_COUNTRIES.put("HU:rural", 90);
        SPEED_FOR_COUNTRIES.put("HU:trunk", 110);
        SPEED_FOR_COUNTRIES.put("HU:motorway", 130);
        SPEED_FOR_COUNTRIES.put("IT:urban", 50);
        SPEED_FOR_COUNTRIES.put("IT:rural", 90);
        SPEED_FOR_COUNTRIES.put("IT:trunk", 110);
        SPEED_FOR_COUNTRIES.put("IT:motorway", 130);
        SPEED_FOR_COUNTRIES.put("JP:national", 60);
        SPEED_FOR_COUNTRIES.put("JP:motorway", 100);
        SPEED_FOR_COUNTRIES.put("LT:living_street", 20);
        SPEED_FOR_COUNTRIES.put("LT:urban", 50);
        SPEED_FOR_COUNTRIES.put("LT:rural", 90);
        SPEED_FOR_COUNTRIES.put("LT:trunk", 120);
        SPEED_FOR_COUNTRIES.put("LT:motorway", 130);
        SPEED_FOR_COUNTRIES.put("PL:living_street", 20);
        SPEED_FOR_COUNTRIES.put("PL:urban", 50);
        SPEED_FOR_COUNTRIES.put("PL:rural", 90);
        SPEED_FOR_COUNTRIES.put("PL:trunk", 100);
        SPEED_FOR_COUNTRIES.put("PL:motorway", 140);
        SPEED_FOR_COUNTRIES.put("RO:urban", 50);
        SPEED_FOR_COUNTRIES.put("RO:rural", 90);
        SPEED_FOR_COUNTRIES.put("RO:trunk", 100);
        SPEED_FOR_COUNTRIES.put("RO:motorway", 130);
        SPEED_FOR_COUNTRIES.put("RU:living_street", 20);
        SPEED_FOR_COUNTRIES.put("RU:rural", 90);
        SPEED_FOR_COUNTRIES.put("RU:urban", 60);
        SPEED_FOR_COUNTRIES.put("RU:motorway", 110);
        SPEED_FOR_COUNTRIES.put("SK:urban", 50);
        SPEED_FOR_COUNTRIES.put("SK:rural", 90);
        SPEED_FOR_COUNTRIES.put("SK:trunk", 130);
        SPEED_FOR_COUNTRIES.put("SK:motorway", 130);
        SPEED_FOR_COUNTRIES.put("SI:urban", 50);
        SPEED_FOR_COUNTRIES.put("SI:rural", 90);
        SPEED_FOR_COUNTRIES.put("SI:trunk", 110);
        SPEED_FOR_COUNTRIES.put("SI:motorway", 130);
        SPEED_FOR_COUNTRIES.put("ES:urban", 50);
        SPEED_FOR_COUNTRIES.put("ES:rural", 90);
        SPEED_FOR_COUNTRIES.put("ES:trunk", 100);
        SPEED_FOR_COUNTRIES.put("ES:motorway", 120);
        SPEED_FOR_COUNTRIES.put("SE:urban", 50);
        SPEED_FOR_COUNTRIES.put("SE:rural", 70);
        SPEED_FOR_COUNTRIES.put("SE:trunk", 90);
        SPEED_FOR_COUNTRIES.put("SE:motorway", 110);
        SPEED_FOR_COUNTRIES.put("GB:nsl_single", 48);
        SPEED_FOR_COUNTRIES.put("GB:nsl_dual", 113);
        SPEED_FOR_COUNTRIES.put("GB:motorway", 113);
        SPEED_FOR_COUNTRIES.put("UA:urban", 50);
        SPEED_FOR_COUNTRIES.put("UA:rural", 90);
        SPEED_FOR_COUNTRIES.put("UA:trunk", 110);
        SPEED_FOR_COUNTRIES.put("UA:motorway", 130);
        SPEED_FOR_COUNTRIES.put("UZ:living_street", 30);
        SPEED_FOR_COUNTRIES.put("UZ:urban", 70);
        SPEED_FOR_COUNTRIES.put("UZ:rural", 100);
        SPEED_FOR_COUNTRIES.put("UZ:motorway", 110);
    }
}
