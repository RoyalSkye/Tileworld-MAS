/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.agent;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;

/**
 * TWContextBuilder
 *
 * @author michaellees
 * Created: Jan 23, 2011
 *
 * Copyright michaellees 2011
 *
 * Description:
 *
 * Simple Portrayal based on the rectangle 2d portrayal.
 * This just adds an extra rectangle to display the sensor range of the
 * TWAgent
 *
 */
public class TWAgentPortrayal extends SimplePortrayal2D
    {
    public Paint paint;
    public double scale;
    public boolean filled;
    private int sensorRange;
    public boolean showMemory;


    public TWAgentPortrayal() { this(Color.gray,1.0, true); }
    public TWAgentPortrayal(Paint paint, int sensorRange) { this(paint,1.0, true); this.sensorRange = sensorRange; }
    public TWAgentPortrayal(double scale) { this(Color.gray,scale, true); }
    public TWAgentPortrayal(Paint paint, double scale) { this(paint, scale, true); }
    public TWAgentPortrayal(Paint paint, boolean filled) { this(paint, 1.0, filled); }
    public TWAgentPortrayal(double scale, boolean filled) { this(Color.gray, scale, filled); }

    public TWAgentPortrayal(Paint paint, double scale, boolean filled)
        {
        this.paint = paint;
        this.scale = scale;
        this.filled = filled;
        }

    /** If drawing area intersects selected area, add last portrayed object to the bag */
    @Override
    public boolean hitObject(Object object, DrawInfo2D range)
        {
        final double width = range.draw.width*scale;
        final double height = range.draw.height*scale;
        return( range.clip.intersects( range.draw.x-width/2, range.draw.y-height/2, width, height ) );
        }

    Rectangle2D.Double preciseRectangle = new Rectangle2D.Double();
    // assumes the graphics already has its color set
    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info){
            
            
        Rectangle2D.Double draw = info.draw;
        final double width = draw.width*scale;
        final double height = draw.height*scale;

        if (info.precise){
            preciseRectangle.setFrame(info.draw.x - width/2.0, info.draw.y - height/2.0, width, height);
            if (filled) graphics.fill(preciseRectangle);
            else graphics.draw(preciseRectangle);
            return;
        }

        graphics.setPaint(paint);
        // we are doing a simple draw, so we ignore the info.clip

        final int x = (int)(draw.x - width / 2.0);
        final int y = (int)(draw.y - height / 2.0);
        final int w = (int)(width);
        final int h = (int)(height);

        // draw centered on the origin
        if (filled){
            graphics.fillRect(x,y,w,h);
            int x2 = x - (int)((sensorRange) * width);
            int y2 = y - (int)((sensorRange) * height);
            Stroke s = graphics.getStroke();
            graphics.setStroke (new BasicStroke(
                1f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND,
                1f,
                new float[] {2f},
                0f)
            );
            graphics.drawRect(x2, y2, (int)((width*2*sensorRange)+width), (int)((height*2*sensorRange)+height) );
            graphics.setStroke(s);
        }
        else
            graphics.drawRect(x,y,w,h);
        
        
        
    }
}

    

