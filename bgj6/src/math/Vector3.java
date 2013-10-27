/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package math;

/**
 *
 * @author shua
 */
public class Vector3 {
    public static final Vector3 X_AXIS = new Vector3(1,0,0);
    public static final Vector3 Y_AXIS = new Vector3(0,1,0);
    public static final Vector3 Z_AXIS = new Vector3(0,0,1);
    public double x,y,z;
    
    public Vector3(double xi, double yi, double zi) {
        x = xi;
        y = yi;
        z = zi;
    }
    
    public Vector3(double[] xyz) {
        x = ((xyz.length>0)?xyz[0]:0);
        y = ((xyz.length>1)?xyz[1]:0);
        z = ((xyz.length>2)?xyz[2]:0);
    }
    
    public Vector3(Vector3 v) {
        x=v.x;
        y=v.y;
        z=v.z;
    }
    
    public double[] xyz() {
        double[] xyz = {x,y,z};
        return xyz;
    }
    
    public Vector3 scale(double s) {
        x*=s;
        y*=s;
        z*=s;
        return this;
    }
    public Vector3 times(double s) {
        return new Vector3(x*s, y*s, z*s);
    }
    
    public Vector3 negate() {
        return times(-1);
    }
    public Vector3 plus(Vector3 r) {
        return new Vector3(x+r.x, y+r.y, z+r.z);
    }
    public Vector3 minus(Vector3 r) {
        return this.plus(r.negate());
    }
    
    public double dot(Vector3 r) {
        return (x*r.x + y*r.y + z*r.z);
    }
    
    public double sqmag() {
        return x*x + y*y + z*z;
    }
    public double mag() {
        return Math.sqrt(sqmag());
    }
    public Vector3 normalize() {
        double invmag = 1/mag();
        scale(invmag);
        return this;
    }
    public Vector3 normalized() {
        Vector3 ret = new Vector3(this);
        return ret.normalize();
    }
}
