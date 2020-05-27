package org.geotools;

public class DistanceFrom {
    private double distance;
    private String id;

    public DistanceFrom(double distance, String id) {
        this.distance = distance;
        this.id = id;
    }

    public double getDistance() {
        return distance;
    }

    public String getId() {
        return id;
    }
}
