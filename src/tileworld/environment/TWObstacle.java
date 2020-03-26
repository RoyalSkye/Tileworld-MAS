/**
 * 
 */
package tileworld.environment;

import java.awt.Color;
import sim.portrayal.Portrayal;
import sim.portrayal.simple.RectanglePortrayal2D;
import sim.util.Int2D;

/**
 * TWObstacle
 *
 * @author michaellees
 * Created: Apr 15, 2010
 *
 * Copyright michaellees 2010
 *
 *
 * Description:
 *
 * Obstacles in Tileworld
 *
 */
public class TWObstacle extends TWObject {

    /**
     * @param creationTime
     * @param deathTime
     */
    public TWObstacle(int x, int y, TWEnvironment env, double creationTime, double deathTime) {
        super(x, y, env, creationTime, deathTime);

    }

    public TWObstacle(Int2D pos, TWEnvironment env, Double creationTime, Double deathTime) {
        super(pos.x, pos.y, env, creationTime, deathTime);

    }

    public TWObstacle() {
    }

    public static Portrayal getPortrayal() {
        // black filled box
        return new RectanglePortrayal2D(new Color(0.0f, 0.0f, 0.0f), true);
    }
}
