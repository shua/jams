package rainbot;

import render.Renderer;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.Random;

import jinngine.collision.SAP2;
import jinngine.geometry.Geometry;
import jinngine.geometry.Box;
import jinngine.math.Matrix4;
import jinngine.math.Vector3;
import jinngine.physics.*;
import jinngine.physics.constraint.joint.HingeJoint;
import jinngine.physics.constraint.joint.JointAxisController;
import jinngine.physics.force.GravityForce;
import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;
import render.Renderer.RenderBox;
import render.JinngineRenderer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author shua
 */
public class Rainbot implements JinngineRenderer.Callback, JinngineRenderer.EventCallback {
    private final Scene scene;
    private final JinngineRenderer window;
    private final Arm arm;
    
    private class SegmentImpl implements Arm.Segment {
        private Geometry box;
        private HingeJoint constraint;
        private SegmentImpl parent;
        private Vector3 color;
        private double step;
        public final int ind;
        public SegmentImpl (Scene scene, int i, Vector3 b, Vector3 c, double s) {
            box = new Box(b.x, b.y, b.z);
            color = c;
            step = s;
            ind = i;
            Body boxbdy = new Body("seg"+ind, box);
            boxbdy.setPosition(0,-20,-20);
        }
        public SegmentImpl(SegmentImpl p, Scene scene, int i, Vector3 c, double s) {
            this(scene, i, new Vector3(1,(20.0-i)/3,1), c, s);
            Body boxbdy = box.getBody();
            parent = p;
            Vector3 ppos = parent.box.getBody().getPosition();
            double hlen = ((20.0-i)/3)/2;
            double hplen = (i==1)?2:((21.0-i)/3)/2;
            boxbdy.setPosition(new Vector3(0,(hlen+hplen)-1,(i%2==0)?-1.1:1.1).add(ppos));
            System.out.println(ppos.x + ", " + ppos.y + ", " + ppos.z);
            Vector3 hpos = new Vector3(0,-(hlen-0.5),0).add(boxbdy.getPosition());
            constraint = new HingeJoint(boxbdy, parent.box.getBody(), hpos, new Vector3(0,0,1));
            JointAxisController jc = constraint.getHingeControler();
            //jc.setFrictionMagnitude(0.9);
            jc.setLimits(-2.7, 2.7);
            jc.setMotorForce(100, 0);
        }
        public void setConstraint(HingeJoint c) {
            constraint = c;
        }
        @Override
        public double getStepDelta() { return step; }
        @Override
        public Geometry getGeo() { return box; }
        @Override
        public Vector3 getColor() { return color; }
        @Override
        public HingeJoint getConstraint() { return constraint; }
        @Override
        public Arm.Segment getParent() { return parent; }
    }

    public Rainbot(){
        scene = new DefaultScene(new SAP2(), new NonsmoothNonlinearConjugateGradient(44), new DefaultDeactivationPolicy());
        scene.setTimestep(0.1);
        
        Box floorb = new Box(100, 20, 100);;
        Body floor = new Body("floor", floorb);
        floor.setPosition(new Vector3(0,-30,0));
        floor.setFixed(true);
    
        Box backb = new Box(200, 200, 20);
        Body back = new Body("back", backb);
        back.setPosition(new Vector3(0, 0, -74));
        back.setFixed(true);

        Box frontb = new Box(200, 200, 20);
        Body front = new Body("front", frontb);
        front.setPosition(new Vector3(0, 0, 0));
        front.setFixed(true);

        Box leftb = new Box(20, 200, 200);
        Body left = new Body("left", leftb);
        left.setPosition(new Vector3(-35, 0, 0));
        left.setFixed(true);

        Box rightb = new Box(20, 200, 200);
        Body right = new Body("right", rightb);
        right.setPosition(new Vector3(35, 0, 0));
        right.setFixed(true);
        
        SegmentImpl[] segs = new SegmentImpl[4];
        segs[0] = new SegmentImpl(scene, 0, new Vector3(3,1,3), new Vector3(0.1,0.1,0.1), 0.5);
        segs[0].setConstraint(new HingeJoint(segs[0].getGeo().getBody(),floor,new Vector3(0,-20,-20),new Vector3(0,1,0)));
        //scene.addConstraint(segs[0].getConstraint());
        //scene.addLiveConstraint(segs[0].getConstraint());
        for(int i=1; i<segs.length; i++) {
            segs[i] = new SegmentImpl(segs[i-1], scene, i, new Vector3(0.2,0.2,0.2), 1);
            //scene.addConstraint(segs[i].getConstraint());
            //scene.addLiveConstraint(segs[i].getConstraint());
        }
        arm = new Arm(segs);

        Container white = new Container(new Vector3(-10,-20,-10), new Vector3(1,1,1));
        Container black = new Container(new Vector3(10,-20,-10), new Vector3(0,0,0));
        
        for(int i=0; i<4; i++) {
            Box box = new Box(1,1,1);
            Body boxb = new Body("whiteBox"+i, box);
            boxb.setPosition(2*i,0,-10);
            white.addGeo(box);
        }
        for(int i=0; i<4; i++) {
            Box box = new Box(1,1,1);
            Body boxb = new Body("blackBox"+i, box);
            boxb.setPosition(-2*i, 0, -10);
            black.addGeo(box);
        }

        // add all to scene
        arm.addBodies(scene);
        white.addBodies(scene);
        black.addBodies(scene);
        scene.addBody(floor);
        scene.addBody(back);
        scene.addBody(front);
        scene.addBody(left);
        scene.addBody(right);
        
        scene.addForce(new GravityForce(segs[0].getGeo().getBody()));
        
        window = new JinngineRenderer(this, this);
        window.getCamera().setPos(new Vector3(0,10,10));;
        window.getCamera().setPitch(-0.95);
        //Random randy = new Random();
        //double c = randy.nextInt(256)/256.0;
        //window.drawMe(boxgeo, new Vector3(c,c,c));
        //c = randy.nextInt(256)/256.0;
        //window.drawMe(box2geo, new Vector3(c,c,c));
        window.drawMe(white.box, white.color);
        for(Geometry b: white.geos) {
            window.drawMe(b, white.color);
        }
        window.drawMe(black.box, black.color);
        for(Geometry b: black.geos) {
            window.drawMe(b, black.color);
        }
        window.drawMe(floorb, new Vector3(0.2, 0.2, 0.2));
        //window.drawMe(rightb, new Vector3(1.0,0.5,0.5));
        //window.drawMe(leftb, new Vector3(0.5,0.5,0.5));
        //window.drawMe(backb, new Vector3(0.6,0.6,0.6));;
        
        arm.draw(window);
        
        window.start();
    }
    @Override
    public void tick() {
        scene.tick();
    }
    
    @Override
    public void mousePressed(double x, double y, Vector3 point, Vector3 direction) {
    }

    @Override
    public void mouseDragged(double x, double y, Vector3 point, Vector3 direction) {
    }

    @Override
    public void mouseReleased() {
    }
    
    @Override
    public void keyPressed(KeyEvent e){
        int k = e.getKeyCode();
        switch(k) {
            case KeyEvent.VK_UP:
                window.getCamera().setPitch(window.getCamera().getEul().x+.1);break;
            case KeyEvent.VK_DOWN:
                window.getCamera().setPitch(window.getCamera().getEul().x-.1);break;
            case KeyEvent.VK_LEFT:
                window.getCamera().setYaw(window.getCamera().getEul().y+.1);break;
            case KeyEvent.VK_RIGHT:
                window.getCamera().setYaw(window.getCamera().getEul().y-.1);break;
            case KeyEvent.VK_Q:
                arm.power(0, 1);break;
            case KeyEvent.VK_A:
                arm.power(0, -1);break;
            case KeyEvent.VK_W:
                arm.power(1, 1);break;
            case KeyEvent.VK_S:
                arm.power(1, -1);break;
            case KeyEvent.VK_E:
                arm.power(2, 1);break;
            case KeyEvent.VK_D:
                arm.power(2, -1);break;
            case KeyEvent.VK_R:
                arm.power(3, 1);break;
            case KeyEvent.VK_F:
                arm.power(3, -1);break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if(k==KeyEvent.VK_Q||k==KeyEvent.VK_A) {
            arm.power(0, 0);
        } else if(k==KeyEvent.VK_W||k==KeyEvent.VK_S) {
            arm.power(1, 0);
        } else if(k==KeyEvent.VK_E||k==KeyEvent.VK_D) {
            arm.power(2, 0);
        } else if(k==KeyEvent.VK_R||k==KeyEvent.VK_F) {
            arm.power(3, 0);
        }
    }
    
    public static void main(String[] args) {
        new Rainbot();
    }
}
