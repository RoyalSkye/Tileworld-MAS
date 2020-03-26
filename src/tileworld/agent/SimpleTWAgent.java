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
        /**
         *agent根据现在情况，判断出希望到达的地方(x,y)
         *也可以发送消息到消息总站或者查看里面的消息
         *但是注意在TWAgent.java的194行知道，每一步
         *只能执行一次thought和act
         *
         *注意generatePlan()函数返回的是TWPath
         *有了想要到的地方可以用这个函数生成路径
         *源代码是AstarPathGenerator生成想要的路径
         *
         *有了路径就传入TWTought到act执行
         */
//        getMemory().getClosestObjectInSensorRange(Tile.class);
        System.out.println("Simple Score: " + this.score);
        return new TWThought(TWAction.MOVE,getRandomDirection());
    }
    
    @Override
    protected void act(TWThought thought) {

        //判断如果现在的位置是tile就pickUpTile(Tile)
        //如果是hole或者fuel就执行相应的操作。
        //也可以加在try：move了之后判断。
        //You can do:
        //move(thought.getDirection())
        //pickUpTile(Tile)
        //putTileInHole(Hole)
        //refuel()

        try {
            this.move(thought.getDirection());
        } catch (CellBlockedException ex) {
        // 这里可以在action不成功的时候重新来
        // 比如先自己给自己发送一个消息
        // communicate(new Message("自己","自己","重新规划路径"))
        // 然后再调用一次this.act(this.think())   
           // Cell is blocked, replan?
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
