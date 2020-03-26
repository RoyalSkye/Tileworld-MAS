/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.agent;

import sim.util.Bag;
import sim.util.IntBag;
import tileworld.environment.TWEnvironment;

/**
 * TWContextBuilder
 *
 * @author michaellees
 * Created: Apr 26, 2010
 *
 * Copyright michaellees 2011
 *
 *
 * Description:
 *
 * A sensory system which examines the environment and adds information to the
 * agents working memory about the observed facts. Each objects that is perceived
 * is stored in the agents memory as a TWAgentMemoryFact : this is just a pair
 * <Object, Time> which indicates the object which was seen and the time at which
 * it was seen. Object is actually a reference to the instance of that TWEntity.
 *
 */
public class TWAgentSensor {


    TWAgent me;
    int sensorRange;

    TWAgentSensor(TWAgent moi, int defaultSensorRange) {
       me = moi;
       sensorRange = defaultSensorRange;
    }

    /**
     * Simple sense procedure that gets all neighboring entities within the
     * specified sensor range
     */
    public void sense(){
        Bag sensedObjects = new Bag();
        Bag sensedAgents = new Bag();
        IntBag objectXCoords = new IntBag();
        IntBag objectYCoords = new IntBag();
        IntBag agentXCoords = new IntBag();
        IntBag agentYCoords = new IntBag();
        
        

        //sense objects
        // getNeighborsMaxDistance: Gets all neighbors of a location that satisfy max( abs(x-X) , abs(y-Y) ) <= dist.
        // Note that the order and size of the result sensedObjects (sensedAgents) may not correspond to the objectXCoords (agentXCoords) and objectYCoords (agentYCoords) bags.
        me.getEnvironment().getObjectGrid().getNeighborsMaxDistance(me.getX(), me.getY(), sensorRange, false, sensedObjects, objectXCoords, objectYCoords);
        me.getEnvironment().getAgentGrid().getNeighborsMaxDistance(me.getX(), me.getY(), sensorRange, false, sensedAgents, agentXCoords, agentYCoords);

        //import facts to memory
        me.getMemory().updateMemory(sensedObjects, objectXCoords, objectYCoords, sensedAgents,agentXCoords,agentYCoords);
    }


}
