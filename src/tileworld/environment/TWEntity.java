/**
 * 
 */
package tileworld.environment;
import sim.util.Int2D;
import tileworld.exceptions.CellBlockedException;
import tileworld.exceptions.InsufficientFuelException;

/**
 * TWEntity
 *
 * @author michaellees
 * Created: Apr 15, 2010
 *
 * Copyright michaellees 2010
 *
 *
 * Description:
 *
 * Base class from which all entities in the Tileworld inherit.
 * TWObject is a sub-class of this, which is used to specify something with
 * a lifetime (i.e., Tile, Hole, Obstacle)
 */


public abstract class TWEntity{

    private TWEnvironment environment;

    protected int x;
    protected int y;

    /**
     * SHould not be used, defined for creating instances for ObjectCreators
     */
    public TWEntity(){
        x=0;y=0;
    }

    public TWEntity(int xpos, int ypos, TWEnvironment env){
        
        environment = env;
        //Set location of entity when it's created
//        x = xpos; y = ypos;
//        environment.getGrid().set(xpos,ypos,this);
        this.setLocation(xpos, ypos);
    }

     public TWEntity(Int2D pos, TWEnvironment env){
        this(pos.x, pos.y, env);
     }

    protected abstract void move(TWDirection d) throws InsufficientFuelException, CellBlockedException;

    protected void setLocation(int xpos, int ypos){
        x=xpos;y=ypos;
        //Set location of entity when it's created
        environment.getObjectGrid().set(x, y, this);
    }

    public void setLocation(Int2D pos){

        this.setLocation(pos.x,pos.y);
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the manhattan distance from this entity to a specified location.
     *
     * @param x1 x coordinate to check to
     * @param y1 y coordinate to check to
     * @return the manhattan distance from this entity to the coordinate x1,y1
     */
    private double getManhattanDistanceTo(int x1, int y1){
        return Math.abs(x1-x) + Math.abs(y1-y);
    }
    
     /**
     * Returns the manhattan distance from this entity to another specified entity.
     *
     * @param ent other entity to check distance to
     * @return the manhattan distance from this entity to ent
     */
    private double getManhattanDistanceTo(TWEntity ent){
        return getManhattanDistanceTo(ent.x, ent.y);
    }

    public double getDistanceTo(TWEntity a){
        return getDistanceTo(a.x, a.y);
    }

     public double getDistanceTo(int x, int y){
        return getManhattanDistanceTo(x,y);
    }

    /**
     * @return the environment
     */
    public TWEnvironment getEnvironment() {
        return environment;
    }

    /**
     * return True if a is closer than b
    */
    public boolean closerTo(TWEntity a, TWEntity b){
        return this.getDistanceTo(a) < getDistanceTo(b);
    }

    
    public boolean sameLocation(TWEntity a){
        return (this.x == a.x && this.y == a.y);
    }


}
