/**
 * Copyright (c) 2008-2010 Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available at
 * http://www.gnu.org/copyleft/gpl.html.
 */
package render;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.Animator;

import jinngine.geometry.Box;
import jinngine.geometry.ConvexHull;
import jinngine.geometry.Geometry;
import jinngine.geometry.UniformCapsule;
import jinngine.math.Matrix4;
import jinngine.math.Vector3;
import jinngine.physics.Body;

public class JinngineRenderer extends Frame implements GLEventListener, MouseListener, MouseMotionListener, KeyListener {
    private static final long serialVersionUID = 1L;
    public List<DrawShape> toDraw = new ArrayList<DrawShape>();
    private final Callback callback;
    private final EventCallback mouseCallback;
    private final GLCanvas canvas = new GLCanvas();
    private Animator animator = new Animator(this.canvas);
    private final Camera camera;

    private interface DrawShape {
        public Iterator<Vector3[]> getFaces();
        public Matrix4 getTransform();
        public Vector3 getColor();
        public Body getReferenceBody();
    }

    public interface Callback {
        public void tick();
    }
    public interface EventCallback {
        public void mousePressed(double x, double y, Vector3 point, Vector3 direction);
        public void mouseDragged(double x, double y, Vector3 point, Vector3 direction);
        public void mouseReleased();
        public void keyPressed(KeyEvent k);
        public void keyReleased(KeyEvent k);
    }

    public JinngineRenderer(Callback callback, EventCallback mouseCallback) {
        this.callback = callback;
        this.mouseCallback = mouseCallback;
        setTitle("jinngine.example");
        setSize(1024, (int) (1024 / (1.77777)));
        canvas.setIgnoreRepaint(true);
        canvas.addGLEventListener(this);
        canvas.setVisible(true);
        //Setup exit function
        addWindowListener(new WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });

        camera = new Camera();
        add(canvas, java.awt.BorderLayout.CENTER);
        setVisible(true);
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addKeyListener(this);
    }

    public void drawMe(final Geometry g, final Vector3 c) {
        System.out.println("color: "+c);
        if (g instanceof ConvexHull) {
            toDraw.add(new DrawShape() {
                @Override
                public Iterator<Vector3[]> getFaces() { return ((ConvexHull) g).getFaces(); }
                @Override
                public Matrix4 getTransform() { return g.getTransform(); }
                @Override
                public Vector3 getColor() { return c; }
                @Override
                public Body getReferenceBody() { return g.getBody(); }
            });
        }

        if (g instanceof Box) {
            final List<Vector3> vertices = new ArrayList<Vector3>();
            vertices.add(new Vector3(0.5, 0.5, 0.5));
            vertices.add(new Vector3(-0.5, 0.5, 0.5));
            vertices.add(new Vector3(0.5, -0.5, 0.5));
            vertices.add(new Vector3(-0.5, -0.5, 0.5));
            vertices.add(new Vector3(0.5, 0.5, -0.5));
            vertices.add(new Vector3(-0.5, 0.5, -0.5));
            vertices.add(new Vector3(0.5, -0.5, -0.5));
            vertices.add(new Vector3(-0.5, -0.5, -0.5));
            final ConvexHull hull = new ConvexHull(vertices);

            toDraw.add(new DrawShape() {
                @Override
                public Iterator<Vector3[]> getFaces() { return hull.getFaces(); }
                @Override
                public Matrix4 getTransform() { return g.getTransform(); }
                @Override
                public Vector3 getColor() { return c; }
                @Override
                public Body getReferenceBody() { return g.getBody(); }
            });
        }

        if (g instanceof UniformCapsule) {
            UniformCapsule cap = (UniformCapsule) g;
            final List<Vector3> vertices = new ArrayList<Vector3>();

            ConvexHull icosphere = buildIcosphere(1, 2);

            // add two icos to vertices
            Iterator<Vector3> iter = icosphere.getVertices();
            while (iter.hasNext()) {
                Vector3 v = iter.next();
                vertices.add(v.multiply(cap.getRadius()).add(0, 0, cap.getLength() / 2));
                vertices.add(v.multiply(cap.getRadius()).add(0, 0, -cap.getLength() / 2));
            }

            final ConvexHull hull = new ConvexHull(vertices);

            toDraw.add(new DrawShape() {
                @Override
                public Iterator<Vector3[]> getFaces() { return hull.getFaces(); }
                @Override
                public Matrix4 getTransform() { return g.getTransform(); }
                @Override
                public Vector3 getColor() { return c; }
                @Override
                public Body getReferenceBody() { return g.getBody(); }
            });
        }

    }

    private ConvexHull buildIcosphere(double r, int depth) {
        final List<Vector3> vertices = new ArrayList<Vector3>();
        // point on icosahedron
        final double t = (1.0 + Math.sqrt(5.0)) / 2.0;
        vertices.add(new Vector3(-1, t, 0).normalize());
        vertices.add(new Vector3(1, t, 0).normalize());
        vertices.add(new Vector3(-1, -t, 0).normalize());
        vertices.add(new Vector3(1, -t, 0).normalize());
        vertices.add(new Vector3(0, -1, t).normalize());
        vertices.add(new Vector3(0, 1, t).normalize());
        vertices.add(new Vector3(0, -1, -t).normalize());
        vertices.add(new Vector3(0, 1, -t).normalize());
        vertices.add(new Vector3(t, 0, -1).normalize());
        vertices.add(new Vector3(t, 0, 1).normalize());
        vertices.add(new Vector3(-t, 0, -1).normalize());
        vertices.add(new Vector3(-t, 0, 1).normalize());

        int n = 0;
        while (true) {
            ConvexHull hull = new ConvexHull(vertices);

            if (n >= depth) {
                return hull;
            }

			// for each face, add a new sphere support 
            // point in direction of the face normal
            Iterator<Vector3[]> iter = hull.getFaces();
            while (iter.hasNext()) {
                Vector3[] face = iter.next();
                Vector3 normal = face[1].sub(face[0]).cross(face[2].sub(face[1])).normalize();
                vertices.add(new Vector3(normal));
            }

            // depth level done
            n++;
        }
    }

    public void start() {
        animator.start();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        // Perform ratio time-steps on the model
        callback.tick();

        // Clear buffer, etc.
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_STENCIL_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);

        gl.glLoadIdentity();
        // Set camera transform
        camera.glSetCamera(gl);

        for (DrawShape shape : toDraw) {
            gl.glPushAttrib(GL2.GL_LIGHTING_BIT);
            //gl.glDisable(GL2.GL_LIGHTING);
            gl.glPushMatrix();
            gl.glMultMatrixd(shape.getTransform().toArray(), 0);

            /*
            if (shape.getReferenceBody().deactivated) {
                float ambientLight[] = {1.5f, 1.5f, 2.0f, 1.0f};
		//float diffuseLight[] = { 0.8f, 0.0f, 0.8f, 1.0f };
                //float specularLight[] = { 0.5f, 0.5f, 0.5f, 1.0f };
                //float position[] = { -1.5f, 1.0f, -4.0f, 1.0f };

                // Assign created components to GL_LIGHT0
                gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambientLight, 0);
		//gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuseLight,0);
                //gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, specularLight,0);
                //gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, position,0);

            }
            */

            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
            Iterator<Vector3[]> i = shape.getFaces();
            Vector3 color = shape.getColor();
            while (i.hasNext()) {
                gl.glBegin(GL2.GL_POLYGON);
                Vector3[] face = i.next();
                //compute normal
                Vector3 n = face[1].sub(face[0]).cross(face[2].sub(face[1])).normalize();

                for (Vector3 v : face) {
                    gl.glNormal3d(n.x, n.y, n.z);
                    //gl.glTexCoord2f(1.0f, 1.0f);
                    gl.glColor3d(color.x, color.y, color.z);
                    gl.glVertex3d(v.x, v.y, v.z);
                    gl.glTexCoord2f(0.0f, 1.0f);
                }
                gl.glEnd();
            }

            gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);
            gl.glLineWidth(1.7f);
            gl.glDisable(GL2.GL_LIGHTING);
            gl.glScaled(1.01, 1.01, 1.01);
            i = shape.getFaces();
            while (i.hasNext()) {
                gl.glBegin(GL2.GL_POLYGON);
                Vector3[] face = i.next();
                //compute normal
                Vector3 n = face[1].sub(face[0]).cross(face[2].sub(face[1])).normalize();

                for (Vector3 v : face) {
                    gl.glNormal3d(n.x, n.y, n.z);
                    //gl.glTexCoord2f(1.0f, 1.0f);
                    gl.glColor3d(0.2, 0.2, 0.2);
                    gl.glVertex3d(v.x, v.y, v.z);
                    gl.glTexCoord2f(0.0f, 1.0f);
                }
                gl.glEnd();
            }

            gl.glEnable(GL2.GL_LIGHTING);

            gl.glPopMatrix();
            gl.glPopAttrib();
        }

        //draw shadows
        /*
        gl.glLoadIdentity();
        gl.glDisable(GL2.GL_LIGHTING);
        // Set camera transform
        camera.glSetCamera(gl);

        gl.glMultMatrixd(shadowProjectionMatrix(new Vector3(75, 350, -75), new Vector3(0, -20 + 0.0, 0), new Vector3(0, -1, 0)), 0);

        gl.glColor3d(0.85, 0.85, 0.85);

        for (DrawShape shape : toDraw) {
            gl.glPushMatrix();
            gl.glMultMatrixd(shape.getTransform().toArray(), 0);

            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
            Iterator<Vector3[]> i = shape.getFaces();
            while (i.hasNext()) {
                gl.glBegin(GL2.GL_POLYGON);
                Vector3[] face = i.next();
                for (Vector3 v : face) {
                    gl.glVertex3d(v.x, v.y, v.z);
                }
                gl.glEnd();
            }

            gl.glPopMatrix();
        }

        */
        
        gl.glEnable(GL2.GL_LIGHTING);

        // Finish this frame
        gl.glFlush();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        // Setup GL 
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_CULL_FACE);
        //gl.glEnable(GL2.GL_LINE_SMOOTH);
        gl.glEnable(GL2.GL_BLEND);
        //gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        //enable vsync
        gl.setSwapInterval(1);

        // init some lighting
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
	//gl.glShadeModel(GL2.GL_FLAT);

        // Create light components
        float ambientLight[] = {2.0f, 2.0f, 2.0f, 1.0f};
        float diffuseLight[] = {0.2f, 0.2f, 0.2f, 1.0f};
        float specularLight[] = {0.5f, 0.5f, 0.5f, 1.0f};
        float position[] = {-1.5f, 25.0f, -4.0f, 1.0f};

        // Assign created components to GL_LIGHT0
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambientLight, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuseLight, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, specularLight, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, position, 0);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        camera.glSetProjection(drawable.getGL(), w, h);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }

    public void getPointerRay(Vector3 p, Vector3 d, double x, double y) {
        // clipping planes
        double width = camera.getDim()[0];
        double height = camera.getDim()[1];
        double drawHeight = camera.getDrawHeight();
        Vector3 near = new Vector3(2 * x / (double) width - 1, -2 * (y - ((height - drawHeight) * 0.5)) / (double) drawHeight + 1, 0.7);
        Vector3 far = new Vector3(2 * x / (double) width - 1, -2 * (y - ((height - drawHeight) * 0.5)) / (double) drawHeight + 1, 0.9);

        //inverse transform
        Matrix4 T = camera.getInverseTransform();

        Vector3 p1 = new Vector3();
        Vector3 p2 = new Vector3();

        Matrix4.multiply(T, near, p1);
        Matrix4.multiply(T, far, p2);

        p.assign(p1);
        d.assign(p2.sub(p1).normalize());
    }

    private double[] shadowProjectionMatrix(Vector3 l, Vector3 e, Vector3 n) {
        double d, c;
        double[] mat = new double[16];

	  // These are c and d (corresponding to the tutorial)
        d = n.x * l.x + n.y * l.y + n.z * l.z;
        c = e.x * n.x + e.y * n.y + e.z * n.z - d;

	  // Create the matrix. OpenGL uses column by column
        // ordering
        mat[0] = l.x * n.x + c;
        mat[4] = n.y * l.x;
        mat[8] = n.z * l.x;
        mat[12] = -l.x * c - l.x * d;

        mat[1] = n.x * l.y;
        mat[5] = l.y * n.y + c;
        mat[9] = n.z * l.y;
        mat[13] = -l.y * c - l.y * d;

        mat[2] = n.x * l.z;
        mat[6] = n.y * l.z;
        mat[10] = l.z * n.z + c;
        mat[14] = -l.z * c - l.z * d;

        mat[3] = n.x;
        mat[7] = n.y;
        mat[11] = n.z;
        mat[15] = -d;

        return mat;
    }

    public Camera getCamera() {
        return camera;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e) {
        Vector3 p = new Vector3();
        Vector3 d = new Vector3();
        getPointerRay(p, d, e.getX(), e.getY());
        mouseCallback.mousePressed((double) e.getX(), (double) e.getY(), p, d);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseCallback.mouseReleased();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Vector3 p = new Vector3();
        Vector3 d = new Vector3();
        getPointerRay(p, d, e.getX(), e.getY());
        mouseCallback.mouseDragged((double) e.getX(), (double) e.getY(), p, d);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        mouseCallback.keyPressed(arg0);
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        mouseCallback.keyReleased(arg0);
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

    }
}
