/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package render;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.*;
import java.awt.event.KeyEvent;

import jinngine.physics.Body;
import jinngine.geometry.Box;
import jinngine.math.*;
/**
 *
 * @author shua
 */
public class Renderer extends Frame implements GLEventListener, KeyListener {
    private final Callback callback;
    private final List<RenderBox> toDraw = new ArrayList<RenderBox>();
    private final GLCanvas canvas;
    public Camera camera = new Camera();
    
    public interface RenderBox {
        public Vector3 getColor();
        public Matrix4 getTransform();
        public Body getReferenceBody();
    }
    
    public interface Callback {
        public void tick();
    }
    
    public Renderer(Callback c, KeyListener k) {
        callback = c;
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        canvas = new GLCanvas(caps);
        setTitle("Rainbot");
        setSize(camera.getDim()[0], camera.getDim()[1]);
        canvas.setIgnoreRepaint(true);
        canvas.addGLEventListener(this);
        if(k==null)
            k=this;
        canvas.addKeyListener(k);
        canvas.setVisible(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        add(canvas, java.awt.BorderLayout.CENTER);
        setVisible(true);
    }
    public Renderer(Callback c) {
        this(c, null);
    }
    
    public void addGLKeyListener(KeyListener k) {
        canvas.addKeyListener(k);
    }
    
    public void start() {
        AnimatorBase animator = new FPSAnimator(canvas, 60);
        animator.start();
    }
    
    public static void main(String[] args) {
        Renderer win = new Renderer(
            new Callback() {public void tick() {}});
        win.start();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glDepthFunc(GL2.GL_LEQUAL);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        update();
        render(drawable);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        camera.glSetProjection(drawable.getGL(), w, h);
    }
    
    public void draw(RenderBox b) {
        toDraw.add(b);
    }
    public void drawBox(final Box b, final Vector3 c) {
        toDraw.add(new RenderBox() {
            @Override
            public Vector3 getColor() {
                return c;
            }

            @Override
            public Matrix4 getTransform() {
                return b.getTransform();
            }

            @Override
            public Body getReferenceBody() {
                return b.getBody();
            }
        });
    }
    
    private void update() {
        callback.tick();
    }
    
    private void render(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        
        camera.glSetCamera(gl);
        
        /*gl.glBegin(GL2.GL_TRIANGLES);
        gl.glColor3f(1,0,0);
        gl.glVertex3f(-1, -1, -1);
        gl.glColor3f(0, 1, 0);
        gl.glVertex3f(0, 1, -1);
        gl.glColor3f(0, 0, 1);
        gl.glVertex3f(1, -1, -1);
        gl.glEnd();*/
        
        for(RenderBox box: toDraw) {
            gl.glPushMatrix();
            gl.glMultMatrixd(box.getTransform().toArray(), 0);
            
            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
            gl.glBegin(GL2.GL_QUADS);
            Vector3 c = box.getColor();
            gl.glColor3d(c.x, c.y, c.z);
            gl.glVertex3d(0.5,0.5,0.5);
            gl.glVertex3d(-0.5,0.5,0.5);
            gl.glVertex3d(-0.5,-0.5,0.5);
            gl.glVertex3d(0.5, -0.5, 0.5);
            
            gl.glVertex3d(0.5,0.5,0.5);
            gl.glVertex3d(-0.5,0.5,0.5);
            gl.glVertex3d(-0.5,0.5,-0.5);
            gl.glVertex3d(0.5,0.5,-0.5);
            
            gl.glVertex3d(0.5,0.5,0.5);
            gl.glVertex3d(0.5,-0.5,0.5);
            gl.glVertex3d(0.5,-0.5,-0.5);
            gl.glVertex3d(0.5,0.5,-0.5);
            
            gl.glVertex3d(-0.5,0.5,0.5);
            gl.glVertex3d(-0.5,-0.5,0.5);
            gl.glVertex3d(-0.5,-0.5,-0.5);
            gl.glVertex3d(-0.5,0.5,-0.5);
            
            gl.glVertex3d(0.5,-0.5,0.5);
            gl.glVertex3d(-0.5,-0.5,0.5);
            gl.glVertex3d(-0.5,-0.5,-0.5);
            gl.glVertex3d(0.5,-0.5,-0.5);
            
            gl.glVertex3d(0.5,0.5,-0.5);
            gl.glVertex3d(-0.5,0.5,-0.5);
            gl.glVertex3d(-0.5,-0.5,-0.5);
            gl.glVertex3d(0.5,-0.5,-0.5);
            gl.glEnd();
            
            
            
            gl.glPopMatrix();
        }
        
        gl.glPopMatrix(); // pop camera matrix
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyChar()==',') {
            camera.setPitch(camera.getEul().x+.1);
        } else if(e.getKeyChar()=='o') {
            camera.setPitch(camera.getEul().x-.1);
        } else if(e.getKeyChar()=='a') {
            camera.setYaw(camera.getEul().y+.1);
        } else if(e.getKeyChar()=='e') {
            camera.setYaw(camera.getEul().y-.1);
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
