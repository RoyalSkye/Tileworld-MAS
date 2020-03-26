/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.planners;

import sim.util.Int2D;
import tileworld.environment.TWDirection;

/**
 *
 * @author michaellees
 */
public interface TWPlanner {

    TWPath generatePlan();
    boolean hasPlan();
    void voidPlan();
    Int2D getCurrentGoal();
    TWDirection execute();

}
