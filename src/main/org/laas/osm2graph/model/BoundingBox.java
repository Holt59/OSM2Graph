/*
 * BoundingBox, simplified version of mapsforge bounding box.
 * 
 */
package org.laas.osm2graph.model;

/**
 * A BoundingBox represents an immutable set of two latitude and two longitude coordinates.
 */
public class BoundingBox {

    /**
     * Creates a new BoundingBox from a comma-separated string of coordinates in the order minLat, minLon, maxLat,
     * maxLon. All coordinate values must be in degrees.
     *
     * @param boundingBoxString the string that describes the BoundingBox.
     * @return a new BoundingBox with the given coordinates.
     * @throws IllegalArgumentException if the string cannot be parsed or describes an invalid BoundingBox.
     */
    public static BoundingBox fromString(String boundingBoxString) {
    		String[] parts = boundingBoxString.split(",");
        double[] coordinates = new double[parts.length];
        for (int i = 0; i < parts.length; ++i) {
        		coordinates[i] = Double.valueOf(parts[i]);
        }
        return new BoundingBox(coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
    }

    /**
     * The maximum latitude coordinate of this BoundingBox in degrees.
     */
    public final double maxLatitude;

    /**
     * The maximum longitude coordinate of this BoundingBox in degrees.
     */
    public final double maxLongitude;

    /**
     * The minimum latitude coordinate of this BoundingBox in degrees.
     */
    public final double minLatitude;

    /**
     * The minimum longitude coordinate of this BoundingBox in degrees.
     */
    public final double minLongitude;

    /**
     * @param minLatitude  the minimum latitude coordinate in degrees.
     * @param minLongitude the minimum longitude coordinate in degrees.
     * @param maxLatitude  the maximum latitude coordinate in degrees.
     * @param maxLongitude the maximum longitude coordinate in degrees.
     * @throws IllegalArgumentException if a coordinate is invalid.
     */
    public BoundingBox(double minLatitude, double minLongitude, double maxLatitude, double maxLongitude) {
        this.minLatitude = minLatitude;
        this.minLongitude = minLongitude;
        this.maxLatitude = maxLatitude;
        this.maxLongitude = maxLongitude;
    }

    /**
     * @param latitude  the latitude coordinate in degrees.
     * @param longitude the longitude coordinate in degrees.
     * @return true if this BoundingBox contains the given coordinates, false otherwise.
     */
    public boolean contains(double latitude, double longitude) {
        return this.minLatitude <= latitude && this.maxLatitude >= latitude
                && this.minLongitude <= longitude && this.maxLongitude >= longitude;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof BoundingBox)) {
            return false;
        }
        BoundingBox other = (BoundingBox) obj;
        if (Double.doubleToLongBits(this.maxLatitude) != Double.doubleToLongBits(other.maxLatitude)) {
            return false;
        } else if (Double.doubleToLongBits(this.maxLongitude) != Double.doubleToLongBits(other.maxLongitude)) {
            return false;
        } else if (Double.doubleToLongBits(this.minLatitude) != Double.doubleToLongBits(other.minLatitude)) {
            return false;
        } else if (Double.doubleToLongBits(this.minLongitude) != Double.doubleToLongBits(other.minLongitude)) {
            return false;
        }
        return true;
    }

    /**
     * @return the latitude span of this BoundingBox in degrees.
     */
    public double getLatitudeSpan() {
        return this.maxLatitude - this.minLatitude;
    }

    /**
     * @return the longitude span of this BoundingBox in degrees.
     */
    public double getLongitudeSpan() {
        return this.maxLongitude - this.minLongitude;
    }

    /**
     * @param boundingBox the BoundingBox which should be checked for intersection with this BoundingBox.
     * @return true if this BoundingBox intersects with the given BoundingBox, false otherwise.
     */
    public boolean intersects(BoundingBox boundingBox) {
        if (this == boundingBox) {
            return true;
        }

        return this.maxLatitude >= boundingBox.minLatitude && this.maxLongitude >= boundingBox.minLongitude
                && this.minLatitude <= boundingBox.maxLatitude && this.minLongitude <= boundingBox.maxLongitude;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("minLatitude=");
        stringBuilder.append(this.minLatitude);
        stringBuilder.append(", minLongitude=");
        stringBuilder.append(this.minLongitude);
        stringBuilder.append(", maxLatitude=");
        stringBuilder.append(this.maxLatitude);
        stringBuilder.append(", maxLongitude=");
        stringBuilder.append(this.maxLongitude);
        return stringBuilder.toString();
    }
}