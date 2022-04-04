/*
 * CustomPaintScale.java
 *
 * Created on 21. August 2007, 15:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.jmonitoring.utils;

import java.awt.Color;
import org.jfree.chart.renderer.LookupPaintScale;

/**
 *
 * @author togro
 */
public class CustomPaintScale {
    
    private LookupPaintScale scale;
    
    /** Creates a new instance of CustomPaintScale */
    public CustomPaintScale(double min, double max, boolean reverse) {
        int r = 255;
        int g = 0;
        int b = 0;
        boolean change;
        double value;
        Color emptyColor = new Color(0,0,0,1);
        scale = new LookupPaintScale(min,max,emptyColor);
        
        
        scale.add(min,new Color(r,g,b));
        double step = (max-min) / 1020;
        if (reverse) {
            value = min;
        } else {
            value = max;
        }
        for (int i = 0; i < 1021; i++) {
            change = false;
            if ((r == 255) && (g < 255) && (b == 0)) {
                g++;
                change = true;
            }
            if ((r > 0) && (g == 255) && (b == 0) && !change) {
                r--;
                change = true;
            }
            if ((r == 0) && (g == 255) && (b < 255) && !change) {
                b++;
                change = true;
            }
            if ((r == 0) && (g > 0) && (b == 255) && !change) {
                g--;
            }
            
            if (reverse) {
                if (i == 1020) {
                    value = max;
                } else {
                    value = min + (i*step);
                }
            } else {
                if (i == 1020) {
                    value = min;
                } else {
                    value = max - (i*step);
                }
            }
            scale.add(value,new Color(r,g,b));
        }
    }
    
    
    public LookupPaintScale getScale() {
        return scale;
    }
}
