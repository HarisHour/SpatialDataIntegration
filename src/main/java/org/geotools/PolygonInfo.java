package org.geotools;
import com.opencsv.bean.CsvBindByName;


public class PolygonInfo {
    @CsvBindByName
    private long osm_id;

    @CsvBindByName
    private int code;

    @CsvBindByName
    private String fclass;

    @CsvBindByName
    private String geo;


    public long getOsm_id() {
        return osm_id;
    }

    public String getGeo() {
        return geo;
    }

}
