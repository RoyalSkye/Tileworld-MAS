/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.agent;

import tileworld.environment.TWDirection;
import tileworld.environment.TWEnvironment;
import tileworld.exceptions.CellBlockedException;

/**
 * TWContextBuilder
 *
 * @author michaellees
 * Created: Feb 6, 2011
 *
 * Copyright michaellees Expression year is undefined on line 16, column 24 in Templates/Classes/Class.java.
 *
 *
 * Description:
 *
 */
public class SimpleTWAgent extends TWAgent{
	private String name;
    public SimpleTWAgent(String name, int xpos, int ypos, TWEnvironment env, double fuelLevel) {
        super(xpos,ypos,env,fuelLevel);
        this.name = name;
    }

    protected TWThought think() {
//        getMemory().getClosestObjectInSensorRange(Tile.class);
        System.out.println("Simple Score: " + this.score);
        return new TWThought(TWAction.MOVE,getRandomDirection());
    }
    
    @Override
    protected void act(TWThought thought) {
        try {
            this.move(thought.getDirection());
        } catch (CellBlockedException ex) {
        }
    }


    private TWDirection getRandomDirection(){

        TWDirection randomDir = TWDirection.values()[this.getEnvironment().random.nextInt(5)];

        if(this.getX()>=this.getEnvironment().getxDimension() ){
            randomDir = TWDirection.W;
        }else if(this.getX()<=1 ){
            randomDir = TWDirection.E;
        }else if(this.getY()<=1 ){
            randomDir = TWDirection.S;
        }else if(this.getY()>=this.getEnvironment().getxDimension() ){
            randomDir = TWDirection.N;
        }

       return randomDir;

    }

    @Override
    public String getName() {
        return name;
    }
}
