package org.geotools;

public class GeoID {
    private String geometry;
    private long id;

    public GeoID(String geometry, long id) {
        this.geometry = geometry;
        this.id = id;
    }

    public String getGeometry() {
        return geometry;
    }

    public long getId() {
        return id;
    }
}
