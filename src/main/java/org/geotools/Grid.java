package org.geotools;

public class Grid {
    private double minX;
    private double minY;
    private double maxX;
    private double maxY;

    private double n;

    public Grid(double minX, double minY, double maxX, double maxY, double n) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.n = n;
    }


    public Cell findGridCell (double x, double y){
        double difX = maxX - minX;
        double difY = maxY - minY;

        double stepX = difX/n;
        double stepY = difY/n;
        double i = ((x - minX)/ stepX);
        if (i >= n){i = n-1;}
        double j = ((y - minY)/ stepY);
        if (j >= n){j = n-1;}
        Cell myCell = new Cell(i, j);
        return  myCell;
    }

}
