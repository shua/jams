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
public class Matrix4 {
    public double[] m;
    
    public Matrix4(int s) {
        this(s,0,0,0, 0,s,0,0, 0,0,s,0, 0,0,0,s);
    }
    public Matrix4(double[] mat) {
        if(mat.length==16)
            m = mat.clone();
    }
    public Matrix4( double m00, double m01, double m02, double m03,
                    double m10, double m11, double m12, double m13,
                    double m20, double m21, double m22, double m23,
                    double m30, double m31, double m32, double m33) {
        m = new double[16];
        m[0] = m00;
        m[1] = m01;
        m[2] = m02;
        m[3] = m03;
        m[4] = m10;
        m[5] = m11;
        m[6] = m12;
        m[7] = m13;
        m[8] = m20;
        m[9] = m21;
        m[10] = m22;
        m[11] = m23;
        m[12] = m30;
        m[13] = m31;
        m[14] = m32;
        m[15] = m33;
    }
    public Matrix4(Matrix4 c) {
        m=c.m.clone();
    }
    
    public double m(int c, int r) {
        return m[r*4+c];
    }
    public Matrix4 set(int c, int r, double v) {
        m[r*4+c] = v;
        return this;
    }
    public double[] mrow(int r) {
        double[] ret = {m[r*4+0],m[r*4+1],m[r*4+2],m[r*4+3]};
        return ret;
    }
    public double[] mcol(int c) {
        double[] ret = {m[c],m[4+c],m[8+c],m[12+c]};
        return ret;
    }
    
    private double dot(double[] r, double[] c) {
        return r[0]*c[0]+r[1]*c[1]+r[2]*c[2]+r[3]*c[3];
    }
    private double dot(int r, int c) {
        return dot(mrow(r),mcol(c));
    }
    
    public Matrix4 times(Matrix4 r) {
        Matrix4 ret = new Matrix4(1);
        for(int i=0; i<4; i++) {
            for(int j=0; j<4; j++) {
                ret.set(i,j,dot(this.mrow(i),r.mcol(j)));
            }
        }
        return ret;
    }
    public static Matrix4 trans(Vector3 trans) {
        Matrix4 ret = new Matrix4(1);
        ret.set(3,0,trans.x).set(3,1,trans.y).set(3,2,trans.z);
        return ret;
    }
    public static Matrix4 scale(Vector3 scale) {
        Matrix4 ret = new Matrix4(1);
        ret.set(0,0,scale.x).set(1,1,scale.y).set(2,2,scale.z);
        return ret;
    }
    public static Matrix4 rotx(double r) {
        Matrix4 ret = new Matrix4(1);
        double c = Math.cos(r);
        double s = Math.sin(r);
        ret.set(1,1,c).set(2,1,-s).set(1,2,s).set(2,2,c);
        return ret;
    }
    public static Matrix4 roty(double r) {
        Matrix4 ret = new Matrix4(1);
        double c = Math.cos(r);
        double s = Math.sin(r);
        ret.set(2,2,c).set(0,2,-s).set(2,0,s).set(0,0,c);
        return ret;
    }
    public static Matrix4 rotz(double r) {
        Matrix4 ret = new Matrix4(1);
        double c = Math.cos(r);
        double s = Math.sin(r);
        ret.set(0,0,c).set(1,0,-s).set(0,1,s).set(1,1,c);
        return ret;
    }
    public static Matrix4 rot(Vector3 a, double r) {
        if(a==Vector3.X_AXIS)
            return rotx(r);
        if(a==Vector3.Y_AXIS)
            return roty(r);
        if(a==Vector3.Z_AXIS)
            return rotz(r);
        Matrix4 ret = new Matrix4(1);
        Vector3 n = a.normalized();
        double c = Math.cos(r);
        double m = 1-c;
        double s = Math.sin(r);
        ret.set(0,0,c+(n.x*n.x)*m);
        ret.set(1,0,(n.x*n.y)*m - n.z*s);
        ret.set(2,0,(n.x*n.z)*m + n.y*s);
        ret.set(0,1,(n.y*n.x)*m + n.z*s);
        ret.set(1,1,c+(n.y*n.y)*m);
        ret.set(2,1,(n.y*n.z)*m - n.x*s);
        ret.set(0,2,(n.z*n.x)*m - n.y*s);
        ret.set(1,2,(n.z*n.y)*m + n.x*s);
        ret.set(2,2,c+(n.z*n.z)*m);
        return ret;
    }
}
