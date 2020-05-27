package org.geotools;
import com.opencsv.bean.CsvBindByName;


public class PointList {
    @CsvBindByName
    private double xCoord;

    @CsvBindByName
    private double yCoord;


    public double getxCoord() {
        return xCoord;
    }

    public double getyCoord() {
        return yCoord;
    }

}
