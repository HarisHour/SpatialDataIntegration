package org.geotools;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.commons.lang3.time.StopWatch;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.grid.Grids;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.factory.Hints;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class App {
    private static final String CSV_FILE_PATH_2 = "C:\\circles.csv";
    private static final String CSV_FILE_PATH_3 = "C:\\polypois.csv";
    private static final String CSV_FILE_PATH_4 = "C:\\rod.csv";
    private static final String CSV_FILE_PATH_5 = "C:\\points.csv";


    public static void main(String[] args) throws IOException {

        //find minimums

        double minX = 999;
        double maxX = 0;

        double minY = 999;
        double maxY = 0;





        try (

                Reader reader3 = Files.newBufferedReader(Paths.get(CSV_FILE_PATH_4));

        ) {
            CsvToBean<RoadsInfo> csvToBean2 = new CsvToBeanBuilder(reader3)
                    .withType(RoadsInfo.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            Iterator<RoadsInfo> roadsInfoIterator = csvToBean2.iterator();


            double min = 999999999;
            long id = -1;


            while (roadsInfoIterator.hasNext()) {
                RoadsInfo roadsInfo = roadsInfoIterator.next();
                try {
//take geometry string, extract coordinates, put them in table and find mins and maxes

                    String coord = roadsInfo.getGeometry();
                    String C2 = coord.replaceAll("[^0-9?!\\.]", " ");
                    String C3 = C2.trim().replaceAll("\\s{2,}", " ");


                    String[] ar = C3.split(" ");

                    double[] table = new double[ar.length];


                    for (int i = 0; i < ar.length; i++) {
                        table[i] = Double.parseDouble(ar[i]);

                    }


                    //for X

                    for (int i = 0; i < table.length; i = i + 2) {


                        if (table[i] < minX) {
                            minX = table[i];
                        }

                        if (table[i] > maxX) {
                            maxX = table[i];
                        }


                    }

                    //for Y

                    for (int i = 1; i <= table.length; i = i + 2) {


                        if (table[i] < minY) {
                            minY = table[i];
                        }

                        if (table[i] > maxY) {
                            maxY = table[i];
                        }


                    }


                } catch (Exception e) {
                    e.printStackTrace();

                }


            }



        }


        System.out.println("minX: " + minX + "\nmaxX: " + maxX);
        System.out.println("minY: " + minY + "\nmaxY: " + maxY);


//create grid
        int n = 50;
        Grid myGrid = new Grid(minX, minY, maxX, maxY, n);

//create Hash Maps

        HashMap<String, ArrayList<GeoID>> myMap = new HashMap<>(); //roads (MultiLineStrings)
        HashMap<String, ArrayList<GeoID>> myPolygonMap = new HashMap<>(); //pois (Polygons)


//fill road Map
        System.out.println("Filling Road Map...");

        try (

                Reader reader4 = Files.newBufferedReader(Paths.get(CSV_FILE_PATH_4));

        ) {
            CsvToBean<RoadsInfo> csvToBean4 = new CsvToBeanBuilder(reader4)
                    .withType(RoadsInfo.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            Iterator<RoadsInfo> roadsInfoIterator = csvToBean4.iterator();


            while (roadsInfoIterator.hasNext()) {
                RoadsInfo roadsInfo = roadsInfoIterator.next();
                try {
//take geometry string, extract coordinates, put them in table

                    String coord = roadsInfo.getGeometry();
                    String C2 = coord.replaceAll("[^0-9?!\\.]", " ");
                    String C3 = C2.trim().replaceAll("\\s{2,}", " ");

                    String[] ar = C3.split(" ");

                    double[] table = new double[ar.length];

                    for (int i = 0; i < ar.length; i++) {
                        table[i] = Double.parseDouble(ar[i]);
                    }

//for every point of the road geometry, find its cell in the grid, make a unique number to use as key in the Hash Map

                    Cell prevCell = new Cell(999, 999);


                    for (int i = 0; i < table.length; i = i + 2) {
                        Cell cellA = myGrid.findGridCell(table[i], table[i + 1]);

                        long iI = (long) cellA.getI();
                        long iJ = (long) cellA.getJ();

                        if ((iI != prevCell.getI()) || (iJ != prevCell.getJ())) {   //(1) if previous point's cell is different from the current's, add GeoID object in the appropriate arraylist of the hashmap.
                            prevCell = new Cell(iI, iJ);


                            String s1 = Long.toString(iI);    // converting long to String
                            String s2 = Long.toString(iJ);
                            String key = s1 +","+ s2;                //merge strings
                            //String key = Long.valueOf(s3).longValue(); //convert back to long



//create GeoID object which contains  Geometry String and Osm_id

                            GeoID obj = new GeoID(roadsInfo.getGeometry(), roadsInfo.getOsm_id());

//If the map already contains the key, put the object in the array list, if not, create the key and the array list and put the object in
                            if (myMap.containsKey(key)) {
                                myMap.get(key).add(obj);

                            } else {
                                ArrayList<GeoID> arli = new ArrayList<>();
                                arli.add(obj);
                                myMap.put(key, arli);


                            }
                        } else { //(2) if not, which means that the point is in the same cell as the previous one, do nothing, because the GeoID obj containing the geometry string of the road has already been added in the appropriate arraylist of the hashmap.
                            continue;
                        }


                    }


                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }


//fill polygon Map
        System.out.println("Filling Polygon Map...");

        try (
                Reader reader3 = Files.newBufferedReader(Paths.get(CSV_FILE_PATH_3));

        ) {
            CsvToBean<PolygonInfo> csvToBean3 = new CsvToBeanBuilder(reader3)
                    .withType(PolygonInfo.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            Iterator<PolygonInfo> polygonInfoIterator = csvToBean3.iterator();

            while (polygonInfoIterator.hasNext()) {
                PolygonInfo polygonInfo = polygonInfoIterator.next();

                try {

                    String coords = polygonInfo.getGeo();
                    String C2 = coords.replaceAll("[^0-9?!\\.]", " ");
                    String C3 = C2.trim().replaceAll("\\s{2,}", " ");

                    String[] ar = C3.split(" ");

                    double[] table = new double[ar.length];

                    for (int i = 0; i < ar.length; i++) {
                        table[i] = Double.parseDouble(ar[i]);
                    }
//for every point of the polygon geometry, find its cell in the grid, make a unique number to use as key in the Hash Map
                    Cell prevCell = new Cell(999, 999);


                    if ((table.length % 2) == 0) {
                        for (int i = 0; i < table.length; i = i + 2) {
                            Cell cellA = myGrid.findGridCell(table[i], table[i + 1]);

                            long iI = (long) cellA.getI();
                            long iJ = (long) cellA.getJ();


                            //System.out.println(iI+ " - " + iJ);


                            if ((iI != prevCell.getI()) || (iJ != prevCell.getJ())) {   //(1) if previous point's cell is different from the current's, add GeoID object in the appropriate arraylist of the hashmap.
                                prevCell = new Cell(iI, iJ);


                                String s1 = Long.toString(iI);    // converting long to String
                                String s2 = Long.toString(iJ);
                                String key = s1 +","+ s2;                //merge strings
                                //long key = Long.valueOf(s3).longValue(); //convert back to long

//create GeoID object which contains  Geometry String and Osm_id
                                GeoID obj = new GeoID(polygonInfo.getGeo(), polygonInfo.getOsm_id());

                                if (myPolygonMap.containsKey(key)) {
                                    myPolygonMap.get(key).add(obj);

                                } else {
                                    ArrayList<GeoID> arli2 = new ArrayList<>();
                                    arli2.add(obj);
                                    myPolygonMap.put(key, arli2);

                                }
                            } else { //(2) if not, which means that the point is in the same cell as the previous one, do nothing, because the GeoID obj containing the geometry string of the road has already been added in the appropriate arraylist of the hashmap.
                                continue;
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }


        } catch (Exception e) {
            e.printStackTrace();

        }








        //search for nearest road




        long id = -1;
        long id2 = -1;
        try (Reader reader5 = Files.newBufferedReader(Paths.get(CSV_FILE_PATH_5));
        ) {

            Hints hints = new Hints(Hints.CRS, DefaultGeographicCRS.WGS84);
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(hints);
            WKTReader2 reader = new WKTReader2(geometryFactory);
            org.opengis.referencing.crs.CoordinateReferenceSystem auto = CRS.decode("AUTO:42001,13.45,52.3");
            MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84,
                    auto);



            CsvToBean<PointList> csvToBean = new CsvToBeanBuilder(reader5)
                    .withType(PointList.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            Iterator<PointList> pointListIterator = csvToBean.iterator();

            while (pointListIterator.hasNext()) {
                PointList pointList = pointListIterator.next();

                double min = 999999999;
                double min2 = 999999999;

                Cell cellA = myGrid.findGridCell(pointList.getxCoord(), pointList.getyCoord());


                long iI = (long) cellA.getI();

                long iJ = (long) cellA.getJ();



                String s1 = Long.toString(iI);    // converting long to String
                String s2 = Long.toString(iJ);
                String key = s1 +","+ s2;                //merge strings


                ArrayList<GeoID> list = myMap.get(key);

                if (list == null){ //if there are no roads in the cell, look into nearby cells
                    /*
                    long i = key / 100;
                    System.out.println(i);
                    long j = key % 100;
                    System.out.println(j);

                     */

                    String[] split = key.split(",");
                    String iS = split[0];
                    String jS = split[1];
                    long i = Long.valueOf(iS).longValue();
                    //System.out.println(i);
                    long j = Long.valueOf(jS).longValue();
                    //System.out.println(j);
                    for(long k = i-8; k <= i+8; k++){
                        for(long l = j-8; l <= j+8; l++){

                            String s4 = Long.toString(k);    // converting long to String
                            String s5 = Long.toString(l);
                            String key2 = s4 +","+ s5;                //merge strings
                            //long key2 = Long.valueOf(s6).longValue(); //convert back to long
                            ArrayList<GeoID> list2 = myMap.get(key2);
                            if (list2 != null) {


                                for (int m = 0; m < list2.size(); m++) {


                                    Geometry g5 = reader.read(list2.get(m).getGeometry());
                                    Geometry g6 = reader.read("Point (" + pointList.getxCoord() + " " + pointList.getyCoord() + ")");

                                    Geometry g7 = JTS.transform(g5, transform);
                                    Geometry g8 = JTS.transform(g6, transform);

                                    double dist2 = g7.distance(g8);
                                    if (dist2 < min2) {

                                        min2 = dist2;
                                        id2 = list2.get(m).getId();


                                    }
                                }
                            }


                        }
                    }
                    System.out.println("The nearest road to point " + pointList.getxCoord() + " " + pointList.getyCoord() + " is: " + id2 + " at " + min2 + " meters");
                    System.out.println("===");

                } else {

                    for (int i = 0; i < list.size(); i++) {


                        Geometry g1 = reader.read(list.get(i).getGeometry());
                        Geometry g2 = reader.read("Point (" + pointList.getxCoord() + " " + pointList.getyCoord() + ")");

                        Geometry g3 = JTS.transform(g1, transform);
                        Geometry g4 = JTS.transform(g2, transform);

                        double dist = g3.distance(g4);
                        if (dist < min) {

                            min = g3.distance(g4);
                            id = list.get(i).getId();


                        }
                    }
                    System.out.println("The nearest road to point " + pointList.getxCoord() + " " + pointList.getyCoord() + " is: " + id + " at " + min + " meters");
                    System.out.println("===");
                }


            }
        } catch (Exception e) {
            e.printStackTrace();

        }



        //find pois
        double sum = 0;

        try (Reader reader2 = Files.newBufferedReader(Paths.get(CSV_FILE_PATH_2));
        ) {

            CsvToBean<CirclesInfo> csvToBean = new CsvToBeanBuilder(reader2)
                    .withType(CirclesInfo.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            Iterator<CirclesInfo> circlesInfoIterator = csvToBean.iterator();

            while (circlesInfoIterator.hasNext()) {
                CirclesInfo circlesInfo = circlesInfoIterator.next();


                try {

                    Hints hints = new Hints(Hints.CRS, DefaultGeographicCRS.WGS84);
                    org.locationtech.jts.geom.GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(hints);
                    WKTReader2 reader = new WKTReader2(geometryFactory);
                    org.opengis.referencing.crs.CoordinateReferenceSystem auto = CRS.decode("AUTO:42001,13.45,52.3");
                    MathTransform transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84,
                            auto);



                    Geometry cent = reader.read("Point (" + circlesInfo.getxC() + "  " + circlesInfo.getyC() + ")"); //center geometry

                    Geometry g3 = JTS.transform(cent, transform); //transform center
                    Geometry buffer = g3.buffer(circlesInfo.getRadius()); //buffer transformed center


                    org.locationtech.jts.geom.GeometryFactory GEOMETRY_FACTORY = JTSFactoryFinder.getGeometryFactory();

//center of the buffer (circle)
                    Coordinate center = new Coordinate(circlesInfo.getxC(), circlesInfo.getyC());
                    Point point = GEOMETRY_FACTORY.createPoint(center);



// Buffer selected meters around the point, then get the envelope

                    Envelope envelopeInternal = buffer(point, circlesInfo.getRadius()).getEnvelopeInternal();


//get mins and maxes of the box
                    double minimumX = envelopeInternal.getMinX();
                    double maximumX = envelopeInternal.getMaxX();

                    double minimumY = envelopeInternal.getMinY();
                    double maximumY = envelopeInternal.getMaxY();



                    ArrayList<Cell> boxPointCells = new ArrayList<>();

//find the cells in which the points of the bounding box belong
                    Cell leftDown = myGrid.findGridCell(minimumX, minimumY);
                    Cell leftUp = myGrid.findGridCell(minimumX, maximumY);
                    Cell rightDown = myGrid.findGridCell(maximumX, minimumY);
                    Cell rightUp = myGrid.findGridCell(maximumX, maximumY);

                    boxPointCells.add(leftDown);
                    boxPointCells.add(leftUp);
                    boxPointCells.add(rightDown);
                    boxPointCells.add(rightUp);

                    long minI = 999;
                    long maxI = -1;

                    long minJ = 999;
                    long maxJ = -1;


                    for (int i = 0; i < boxPointCells.size(); i++) {
                        if (boxPointCells.get(i).getI() < minI) {
                            minI = (long) boxPointCells.get(i).getI();
                        }

                        if (boxPointCells.get(i).getI() > maxI) {
                            maxI = (long) boxPointCells.get(i).getI();
                        }

                        if (boxPointCells.get(i).getJ() < minJ) {
                            minJ = (long) boxPointCells.get(i).getJ();
                        }

                        if (boxPointCells.get(i).getJ() > maxJ) {
                            maxJ = (long) boxPointCells.get(i).getJ();
                        }

                    }


                    ArrayList<String> keyList = new ArrayList<>();

                    for (long i = minI; i <= maxI; i++) {
                        for (long j = minJ; j <= maxJ; j++) {

                            String s1 = Long.toString(i);    // converting long to String
                            String s2 = Long.toString(j);
                            String key = s1 +","+ s2;                //merge strings
                           // long key = Long.valueOf(s3).longValue(); //convert back to long

                            keyList.add(key);
                            System.out.println("(" + i + "," + j + ")");
                        }
                    }

                    System.out.println("The POIs located within " + circlesInfo.getRadius() + " meters from point (" + circlesInfo.getxC() + " , " + circlesInfo.getyC() + ") are: \n");


                    for (int i = 0; i < keyList.size(); i++) {
                        ArrayList<GeoID> myPolygonList = myPolygonMap.get(keyList.get(i));
                        if(myPolygonList != null) {
                            for (int k = 0; k < myPolygonList.size(); k++) {


                                Geometry geo1 = reader.read(myPolygonList.get(k).getGeometry());
                                Geometry polyGeometry = JTS.transform(geo1, transform);

                                if (buffer.intersects(polyGeometry) == true) {  //if buffer of transformed point geometry (circle center) intersects with polygon geometry print the polygon osm_id
                                    System.out.println(myPolygonList.get(k).getId());

                                }
                            }
                        }
                    }


                } catch (Exception e) {
                    e.printStackTrace();

                }
            }


        } catch (Exception e) {  //last opencsv bracket
            e.printStackTrace();

        }






    }

    private static Geometry buffer(Geometry geometry, double distanceInMeters) throws FactoryException, TransformException {
        String code = "AUTO:42001," + geometry.getCentroid().getCoordinate().x + "," + geometry.getCentroid().getCoordinate().y;
        org.opengis.referencing.crs.CoordinateReferenceSystem auto = CRS.decode(code);


        MathTransform toTransform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, auto);
        MathTransform fromTransform = CRS.findMathTransform(auto, DefaultGeographicCRS.WGS84);

        Geometry pGeom = JTS.transform(geometry, toTransform);
        Geometry pBufferedGeom = pGeom.buffer(distanceInMeters);
        return JTS.transform(pBufferedGeom, fromTransform);
    }




    }

