package org.geotools;
import com.opencsv.bean.CsvBindByName;


public class CirclesInfo {
    @CsvBindByName
    private double xC;

    @CsvBindByName
    private double yC;

    @CsvBindByName
    private double radius;


    public double getxC() {
        return xC;
    }

    public double getyC() {
        return yC;
    }

    public double getRadius() {
        return radius;
    }
}
