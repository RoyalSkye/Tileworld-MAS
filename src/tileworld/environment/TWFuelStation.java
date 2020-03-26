/**
 * 
 */
package tileworld.environment;

import java.awt.Color;
import sim.portrayal.Portrayal;
import sim.portrayal.simple.OvalPortrayal2D;

/**
 * TWFuelStation
 *
 * @author michaellees
 * Created: Apr 16, 2010
 *
 * Copyright michaellees 2010
 *
 *
 * Description:
 *
 * Fueling station - one created at the beginning of the simulation.
 *
 */
public class TWFuelStation extends TWEntity {

    public TWFuelStation(int x, int y, TWEnvironment env) {
		super(x,y,env);
    }

    @Override
    protected void move(TWDirection d) {
        throw new UnsupportedOperationException("You cannot move the Fuel Station.");
    }


     public static Portrayal getPortrayal(){
        // yellow filled box.
        return new OvalPortrayal2D(new Color(1.0f,1.0f,0.0f), true);
    }

}
