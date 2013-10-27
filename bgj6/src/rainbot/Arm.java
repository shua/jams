/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rainbot;

import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

import jinngine.math.Vector3;
import jinngine.geometry.Geometry;
import jinngine.geometry.Box;
import jinngine.math.Matrix4;
import jinngine.physics.Body;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.constraint.joint.HingeJoint;
import jinngine.physics.constraint.joint.JointAxisController;
import jinngine.physics.Scene;

import render.JinngineRenderer;
/**
 *
 * @author shua
 */
public class Arm {
    public interface Segment {
        public double getStepDelta();
        public Geometry getGeo();
        public Vector3 getColor();
        public HingeJoint getConstraint();
        public Segment getParent();
    }
    private final Segment[] segments;
    public Arm(Segment[] segs) {
        segments = segs.clone();
    }
    
    public void addBodies(Scene scene) {
        for(Segment s: segments) {
            scene.addBody(s.getGeo().getBody());
            scene.addConstraint(s.getConstraint());
            scene.addLiveConstraint(s.getConstraint());
        }
    }
    public void draw(JinngineRenderer window) {
        for(Segment seg: segments) {
            window.drawMe(seg.getGeo(), seg.getColor());
        }
    }
    
    public void power(int seg, double steps) {
        if(seg>=0&&seg<segments.length) {
            JointAxisController jc = segments[seg].getConstraint().getHingeControler();
            if(jc!=null)
                jc.setMotorForce((steps==0)?100:10.0/segments[seg].getStepDelta(), steps*segments[seg].getStepDelta());
        }
    }
    
    public void printi(int seg) {
        if(seg>=0&&seg<segments.length) {
            JointAxisController jc = segments[seg].getConstraint().getHingeControler();
            if(jc!=null)
                System.out.println("seg"+seg+": "+jc.getPosition()+"; "+jc.getVelocity());
        }
    }
}
