package model;

/**
 * 벡터 연산을 위한 벡터, 점 클래스
 */
class Point{
    double x;
    double y;

    Point(double x, double y){
        this.x = x;
        this.y = y;
    }
}

public class Vector {
    public Point p0;
    public Point p1;
    public double vx;
    public double vy;
    public double dx;
    public double dy;

    public Vector(Point p0, Point p1){
        this.p0 = p0;
        this.p1 = p1;
        vx = p1.x - p0.x;
        vy = p1.y - p0.y;
        setD();
    }

    public Vector(double vx, double vy){
        this.vx = vx;
        this.vy = vy;
        setD();
    }

    public Vector(Point p, double vx, double vy){
        this.p0 = p;
        this.vx = vx;
        this.vy = vy;
        this.p1 = new Point(p.x + vx, p.y + vy);
        setD();
    }

    public void setD(){
        if (getLength() == 0){
            this.dx = 0;
            this.dy = 0;
            return;
        }
        this.dx = vx / getLength();
        this.dy = vy / getLength();
    }

    public double getLength(){
        return Math.sqrt(vx * vx + vy * vy);
    }

    public double dotProd(double vx, double vy){
        return this.vx * vx + this.vy * vy;
    }

    public Vector getProjVector(double dx, double dy){
        double dp = dotProd(dx, dy);
        return new Vector(dp * dx, dp * dy);
    }

    public Vector add(Vector v){
        return new Vector(vx + v.vx, vy + v.vy);
    }
}