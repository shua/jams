
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package render;
import jinngine.math.*;
import javax.media.opengl.GL2;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
/**
 *
 * @author shua
 */
public class Camera {
    private GLU glu = new GLU();
    private int width = 800;
    private int height = 600;
    private int drawHeight = (int)(800.0/1.333333);
    private double zoom = 1;
    private double[] proj = null;
    private double[] mat = new double[16];
    private Matrix4 inv;
    
    private boolean dirty = true;
    private boolean dirtyrot = true;
    private boolean dirtyinv = true;
    private Vector3 pos = new Vector3(0,0,0);
    private double pitch = 0;
    private double yaw = 0;
    private double roll = 0;
    private Quaternion rot = new Quaternion();
    
    public Camera() {
        this.getGLMat();
    }
    
    public Camera(Vector3 ps, double p, double y, double r) {
        pos = ps;
        this.setPitch(p);
        this.setYaw(y);
        this.setRoll(r);
        this.getGLMat();
    }
    
    public void glSetProjection(GL c, int w, int h) {
        if(proj == null)
            proj = new double[16];
        //GL2 gl = GLU.getCurrentGL().getGL2();
        GL2 gl = c.getGL2();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        //gl.glFrustum(-1.33333333*zoom, 1.3333333*zoom, -1.0*zoom, 1.0*zoom, 0.1, 50.0);
        glu.gluPerspective(60.0*zoom,((double)w)/h,1.0,100.0);
        height = h; 
        width = w;
        drawHeight = (int)((double)w/1.3333333);
        gl.glViewport (0, (int)((h-drawHeight)/2.0), (int)w, (int)drawHeight);
        gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, proj, 0);
        dirtyinv = true;
    }
    public void glSetCamera(GL c) {
        //GL2 gl = GLU.getCurrentGL().getGL2();
        GL2 gl = c.getGL2();
        gl.glLoadIdentity();
        gl.glMultMatrixd(this.getGLMat(), 0);
    }
    
    public Matrix4 getInverseTransform() {
        if(dirty|dirtyrot|dirtyinv) {
            if(proj==null) {
                proj = new Matrix4().toArray();
            }
            inv = (new Matrix4(proj).multiply(new Matrix4(getGLMat())).inverse());
            dirtyinv = false;
        }
        return inv;
    }
    
    public int[] getDim() {
        int[] ret = {width, height};
        return ret;
    }
    public double getZoom() {
        return zoom;
    }
    public double getDrawHeight() {
        return drawHeight;
    }
    
    public Vector3 getPos() {
        return new Vector3(pos);
    }
    public Vector3 getEul() {
        return new Vector3(pitch, yaw, roll);
    }
    public Quaternion getInvQuat() {
        if(dirtyrot) {
            Quaternion r = Quaternion.rotation(-roll, new Vector3(0,0,1));
            Quaternion p = Quaternion.rotation(-pitch, new Vector3(1,0,0));
            Quaternion y = Quaternion.rotation(-yaw, new Vector3(0,1,0));
            rot = r.multiply(p.multiply(y));
        }
        dirtyrot = false;
        return rot;
    }
    public double[] getGLMat() {
        if(dirty | dirtyrot) {
            mat = Transforms.rotateAndTranslate4(getInvQuat(), pos.negate()).toArray();
        }
        dirty = false;
        dirtyrot = false;
        return mat;
    }
    
    public Camera setGLU(GLU v) {
        glu = v;
        return this;
    }
    public Camera setZoom(double z) {
        zoom = Math.max(0.01,z);
        glSetProjection(GLU.getCurrentGL(), width, height);
        return this;
    }
    
    public Camera setPos(Vector3 p) {
        dirty |= (pos != (pos = p));
        return this;
    }
    public Camera setPitch(double p) {
        dirtyrot |= (pitch != (pitch = Math.min(Math.PI/2, Math.max(p, Math.PI/-2))));
        return this;
    }
    public Camera setYaw(double y) {
        dirtyrot |= (yaw != (yaw = y%(2*Math.PI)));
        return this;
    }
    public Camera setRoll(double r) {
        dirtyrot |= (roll != (roll = r%(2*Math.PI)));
        return this;
    }
    
    public Camera lookAt(Vector3 what) {
        Vector3 local = what.add(new Vector3(pos).negate());
        this.setYaw(Math.asin(local.x/Math.sqrt(local.x*local.x+local.z*local.z)));
        this.setPitch(Math.asin(local.y/Math.sqrt(local.z*local.z+local.y*local.y)));
        return this;
    }
}
