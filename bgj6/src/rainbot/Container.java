/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rainbot;

import java.util.List;
import java.util.ArrayList;

import jinngine.math.Vector3;
import jinngine.geometry.Geometry;
import jinngine.geometry.Box;
import jinngine.physics.Scene;
import jinngine.physics.Body;
import jinngine.physics.force.GravityForce;
/**
 *
 * @author shua
 */
public class Container {
    public Geometry box;
    public Vector3 color;
    public List<Geometry> geos = new ArrayList<Geometry>();
    public Container(Vector3 p, Vector3 c) {
        box = new Box(5,1,5);
        Body boxb = new Body("box"+c, box);
        boxb.setPosition(p);
        boxb.setFixed(true);
        color = c;
    }
    public void addGeo(Geometry b) {
        geos.add(b);
    }
    public void addBodies(Scene s) {
        s.addBody(box.getBody());
        for(Geometry b: geos) {
            s.addBody(b.getBody());
            s.addForce(new GravityForce(b.getBody()));
        }
    }
}
