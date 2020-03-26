/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.planners;

import tileworld.environment.TWDirection;

/**
 * TWContextBuilder
 *
 * @author michaellees
 * Created: Apr 22, 2010
 *
 * Copyright michaellees 2010
 *
 *
 * Description: Contains details of a plan step, the direction and current location.
 *
 */
public class TWPathStep {
    private final int x;
    private final int y;
    private final TWDirection direction;

    public TWPathStep(int x, int y, TWDirection direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    /**
     * @return the x location of step
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y location of step
     */
    public int getY() {
        return y;
    }

    /**
     * @return the direction for this step (ie where to move next)
     */
    public TWDirection getDirection() {
        return direction;
    }


}
