package org.geotools;
import com.opencsv.bean.CsvBindByName;


public class RoadsInfo {
    @CsvBindByName
    private long osm_id;

    @CsvBindByName
    private int code;

    @CsvBindByName
    private String fclass;

    @CsvBindByName
    private String name;

    @CsvBindByName
    private String geometry;

    public long getOsm_id() {
        return osm_id;
    }

    public int getCode() {
        return code;
    }

    public String getFclass() {
        return fclass;
    }

    public String getName() {
        return name;
    }

    public String getGeometry() {
        return geometry;
    }
}
