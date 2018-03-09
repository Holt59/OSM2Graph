package org.laas.osm2graph.graph;

/**
 * Class containing information for road that may be shared by multiple arcs.
 * 
 */
public class RoadInformation {

    /**
     * Road type.
     */
    public enum RoadType {
        MOTORWAY,
        TRUNK,
        PRIMARY,
        SECONDARY,
        MOTORWAY_LINK,
        TRUNK_LINK,
        PRIMARY_LINK,
        SECONDARY_LINK,
        TERTIARY,
        TRACK, // TODO: Track
        RESIDENTIAL,
        UNCLASSIFIED,
        LIVING_STREET,
        SERVICE,
        ROUNDABOUT,
        PEDESTRIAN, // TODO: footway, steps, bridleway
        BICYCLE, // TODO: cycleway
        COASTLINE
    }

    // Type of the road (see above).
    private final RoadType type;

    // Access details
    private final long access;

    // One way road?
    private final boolean oneway;

    // Max speed in kilometers per hour.
    private final int maxSpeed;

    // Name of the road.
    private final String name;

    public RoadInformation(RoadType roadType, long access, boolean isOneWay, int maxSpeed,
            String name) {
        this.type = roadType;
        this.access = access;
        this.oneway = isOneWay;
        this.maxSpeed = maxSpeed;
        this.name = name;
    }

    /**
     * @return Type of the road.
     */
    public RoadType getType() {
        return type;
    }

    /**
     * @return Access information.
     */
    public long getAccess() {
        return this.access;
    }

    /**
     * @return true if this is a one-way road.
     */
    public boolean isOneWay() {
        return oneway;
    }

    /**
     * @return Maximum speed for this road (in kmph).
     */
    public int getMaximumSpeed() {
        return maxSpeed;
    }

    /**
     * @return Name of the road.
     */
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String typeAsString = "road";
        if (getType() == RoadType.COASTLINE) {
            typeAsString = "coast";
        }
        if (getType() == RoadType.MOTORWAY) {
            typeAsString = "highway";
        }
        return typeAsString + " : " + getName() + " " + (isOneWay() ? " (oneway) " : "") + maxSpeed
                + " km/h (max.)";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (access ^ (access >>> 32));
        result = prime * result + maxSpeed;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (oneway ? 1231 : 1237);
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RoadInformation other = (RoadInformation) obj;
        if (access != other.access)
            return false;
        if (maxSpeed != other.maxSpeed)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        }
        else if (!name.equals(other.name))
            return false;
        if (oneway != other.oneway)
            return false;
        if (type != other.type)
            return false;
        return true;
    }

}
