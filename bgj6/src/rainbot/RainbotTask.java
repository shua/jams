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

import render.JinngineRenderer;
/**
 *
 * @author shua
 */
public class RainbotTask {
    public class RainbotContainer {
        public Vector3 color;
        public Geometry geo;
    }
    JinngineRenderer target;
    private List<RainbotContainer> containers = new ArrayList<RainbotContainer>();
    
    public RainbotTask(JinngineRenderer r, RainbotContainer[] conts) {
        for(RainbotContainer c: conts) {
            containers.add(c);
        }
    }
}