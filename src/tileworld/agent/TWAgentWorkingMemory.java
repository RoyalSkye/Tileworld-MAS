package tileworld.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.text.html.HTMLDocument;
import sim.engine.Schedule;
import sim.field.grid.ObjectGrid2D;
import sim.util.Bag;
import sim.util.Int2D;
import sim.util.IntBag;
import tileworld.environment.NeighbourSpiral;
import tileworld.Parameters;
import tileworld.environment.TWEntity;


import tileworld.environment.TWHole;
import tileworld.environment.TWObject;
import tileworld.environment.TWObstacle;
import tileworld.environment.TWTile;
import tileworld.environment.TWFuelStation;

/**
 * TWAgentMemory
 * 
 * @author michaellees
 * 
 *         Created: Apr 15, 2010 Copyright michaellees 2010
 * 
 *         Description:
 * 
 *         This class represents the memory of the TileWorld agents. It stores
 *         all objects which is has observed for a given period of time. You may
 *         want to develop an entirely new memory system by extending this one.
 * 
 *         The memory is supposed to have a probabilistic decay, whereby an element is
 *         removed from memory with a probability proportional to the length of
 *         time the element has been in memory. The maximum length of time which
 *         the agent can remember is specified as MAX_TIME. Any memories beyond
 *         this are automatically removed.
 */
public class TWAgentWorkingMemory {

	/**
	 * Access to Scedule (TWEnvironment) so that we can retrieve the current timestep of the simulation.
	 */
	private Schedule schedule;
	private TWAgent me;
	private final static int MAX_TIME = 10;
	private final static float MEM_DECAY = 0.5f;
	private ObjectGrid2D memoryGrid;
	private TWAgentPercept[][] objects;
	public double estimateLifeTime=50; // estiimation of lifetime
	public Class<?>[][] estimatedRemoveObject;
	public int[][][] estimatedRemoveObjectTime;
	public int[][] lastNullPerceptTime; // for objectLifetimeEstimate

	public TWAgentPercept[][] getObjects(){
		return objects;
	}
	/**
	 * Number of items recorded in memory, currently doesn't decrease as memory
	 * is not degraded - nothing is ever removed!
	 */
	private int memorySize;
	/**
	 * Stores (for each TWObject type) the closest object within sensor range,
	 * null if no objects are in sensor range
	 */
	private HashMap<Class<?>, TWEntity> closestInSensorRange;
	static private List<Int2D> spiral = new NeighbourSpiral(Parameters.defaultSensorRange * 4).spiral();
	//    private List<TWAgent> neighbouringAgents = new ArrayList<TWAgent>();

	// x, y: the dimension of the grid
	public TWAgentWorkingMemory(TWAgent moi, Schedule schedule, int x, int y) {
		closestInSensorRange = new HashMap<Class<?>, TWEntity>(4);
		this.me = moi;
		this.objects = new TWAgentPercept[x][y]; 
		this.estimatedRemoveObject = new Class<?>[x][y];
		this.estimatedRemoveObjectTime = new int[x][y][2];
		this.lastNullPerceptTime = new int[x][y];
		this.schedule = schedule;
		this.memoryGrid = new ObjectGrid2D(me.getEnvironment().getxDimension(), me.getEnvironment().getyDimension());
	}

	public void addObject(Object obj, int x, int y, int firstT, int lastT){
		addObject(obj, x,  y, firstT, lastT, 100, 100);
	}

	public void printTWPerception(TWAgentPercept per){
		System.out.println("current Time Step:"+schedule.getTime());
		System.out.println("current ELT:"+estimateLifeTime);
		System.out.println("First observe time: "+ per.getFirstT());
		System.out.println("Last observe time: " + per.getT());
		System.out.println("object life estimate: "+objectLifetimeEstimate(per));
	}

	public void addObject(Object obj, int x, int y, int firstT, int lastT, int ax, int ay){
		if (obj == null){
			if (objects[x][y] != null){
				memorySize--;
				TWAgentPercept memoryOBJ = objects[x][y];
				// learn estimateLifeTime
				assert ((estimateLifeTime - objectLifetimeEstimate(memoryOBJ)) >= 0);
				if (Math.abs(x-ax)+Math.abs(y-ay) >= 2){ 
					// printTWPerception(memoryOBJ);
					estimateLifeTime -= Math.min((estimateLifeTime - objectLifetimeEstimate(memoryOBJ)), estimateLifeTime/5) * AgentParameter.lifetimeLearningRate * estimateLifeTime/100; 
					// if(AgentParameter.lifetimeLearningRate > 0.001)AgentParameter.lifetimeLearningRate *= 0.99;
				}
				removeObject(x,y);
			}
			lastNullPerceptTime[x][y] = (int) schedule.getTime();
		} else if (obj instanceof TWObject){
			TWObject twObj = (TWObject) obj;
			if(objects[x][y] == null) {
				memorySize++;
				if (estimatedRemoveObject[x][y]==twObj.getClass()){
					int fT = estimatedRemoveObjectTime[x][y][0];
					int rT = estimatedRemoveObjectTime[x][y][1];
					if (schedule.getTime() - fT > estimateLifeTime*1.5){

					} else { // remove
						assert ((schedule.getTime() - rT) > 0);
						estimateLifeTime += Math.min((schedule.getTime() - rT), estimateLifeTime/2) * AgentParameter.lifetimeLearningRate * (50.0/estimateLifeTime+estimateLifeTime/50.0);
						// if(AgentParameter.lifetimeLearningRate > 0.001)AgentParameter.lifetimeLearningRate *= 0.99;
					}
				}
			} else {
				TWAgentPercept memoryOBJ = objects[x][y];
				if (memoryOBJ.getO().getClass() != twObj.getClass()){
					assert ((estimateLifeTime - objectLifetimeEstimate(memoryOBJ)) >= 0);
					if (Math.abs(x-ax)+Math.abs(y-ay) >= 2){ 
						System.out.println("Penalty time: "+(estimateLifeTime - objectLifetimeEstimate(memoryOBJ)));
						estimateLifeTime -= Math.min((estimateLifeTime - objectLifetimeEstimate(memoryOBJ)), estimateLifeTime/5) * AgentParameter.lifetimeLearningRate* estimateLifeTime/100;
						// if(AgentParameter.lifetimeLearningRate > 0.001)AgentParameter.lifetimeLearningRate *= 0.99;
					}
				} else {

				}
			}

			objects[x][y] = new TWAgentPercept(twObj, lastT, firstT);
			memoryGrid.set(x, y, twObj);
			updateClosest(twObj);
		} 
		// else if (obj instanceof TWHole){

		// } else if (obj instanceof TWObstacle){

		// }
	}

	public void updateMemory(String message){
		updateMemory(message, -1, -1);
	}

	public void updateMemory(String message,int oAx,int oAy){
		String[] meSplit = message.split(" ");
		String typeObject = meSplit[3];
		int xPos= Integer.parseInt(meSplit[1]);
		int yPos= Integer.parseInt(meSplit[2]);
		if ("null".equals(typeObject)){
			addObject(null, xPos, yPos, -1, -1, oAx, oAy);
		} else if ( "object".equals(typeObject)){
			int time0 = (int) Float.parseFloat(meSplit[4]);
			int timeF = (int) Float.parseFloat(meSplit[5]);
			TWEntity o = (TWEntity) this.me.getEnvironment().getObjectGrid().get(xPos, yPos);
			if (o != null){
				addObject(o, xPos, yPos, timeF, time0, oAx, oAy);
			}
		}
	}

	/**
	 * Called at each time step, updates the memory map of the agent.
	 * Note that some objects may disappear or be moved, in which case part of
	 * sensed may contain null objects
	 *
	 * Also note that currently the agent has no sense of moving objects, so
	 * an agent may remember the same object at two locations simultaneously.
	 *
	 * Other agents in the grid are sensed and passed to this function. But it
	 * is currently not used for anything. Do remember that an agent sense itself
	 * too.
	 *
	 * @param sensedObjects bag containing the sensed objects
	 * @param objectXCoords bag containing x coordinates of objects
	 * @param objectYCoords bag containing y coordinates of object
	 * @param sensedAgents bag containing the sensed agents
	 * @param agentXCoords bag containing x coordinates of agents
	 * @param agentYCoords bag containing y coordinates of agents
	 */
	public void updateMemory(Bag sensedObjects, IntBag objectXCoords, IntBag objectYCoords, Bag sensedAgents, IntBag agentXCoords, IntBag agentYCoords) {
		//reset the closest objects for new iteration of the loop (this is short
		//term observation memory if you like) It only lasts one timestep
		closestInSensorRange = new HashMap<Class<?>, TWEntity>(4);
		decayMemory();
		System.out.println("-------------------------Step " + schedule.getSteps() + "----------------------------------");

		//must all be same size.
		assert (sensedObjects.size() == objectXCoords.size() && sensedObjects.size() == objectYCoords.size());
		//System.out.println(sensedObjects.size()); // 7*7 if the sensor range doesn't exceed the env range

		for (int i = 0; i < sensedObjects.size(); i++) {
			TWEntity o = (TWEntity) sensedObjects.get(i);
			if (o instanceof TWFuelStation){
				this.me.addTempAllMessage("FindFuelStation " + o.getX() + " " + o.getY());
				// addObject(null, objectXCoords.get(i), objectYCoords.get(i), -1, -1, this.me.getX(), this.me.getY());
				continue;
			}

			if (!(o instanceof TWObject)) {
				if (memoryGrid.get(objectXCoords.get(i), objectYCoords.get(i)) instanceof TWObject){ 
					addObject(null, objectXCoords.get(i), objectYCoords.get(i), -1, -1, this.me.getX(), this.me.getY());
					memorySize--;
					this.me.addTempMessage("UpdateMemoryMap " + objectXCoords.get(i)+" "+ objectYCoords.get(i)+ " " + "null");
				}
				continue;
			}

			double firstFoundTime=this.getSimulationTime();
			if(objects[o.getX()][o.getY()] == null) {
				memorySize++;
			} else if (objects[o.getX()][o.getY()].getO().getClass() == o.getClass()){
				firstFoundTime=objects[o.getX()][o.getY()].getFirstT();
				if (this.getSimulationTime() - firstFoundTime > estimateLifeTime * 1.25){
					firstFoundTime = this.getSimulationTime();
				}
			}
			addObject(o, o.getX(), o.getY(), (int) firstFoundTime, (int) this.getSimulationTime(), this.me.getX(), this.me.getY());
			this.me.addTempMessage("UpdateMemoryMap " + objectXCoords.get(i)+
				" "+ objectYCoords.get(i)+ " " + "object "+this.getSimulationTime() + " " + firstFoundTime);

			//Add the object to memory
			updateClosest(o);

		}
	}


	/**
	 * updates memory using 2d array of sensor range - currently not used
	 * @see TWAgentWorkingMemory#updateMemory(sim.util.Bag, sim.util.IntBag, sim.util.IntBag)
	 */
	// public void updateMemory(TWEntity[][] sensed, int xOffset, int yOffset) {
	// 	for (int x = 0; x < sensed.length; x++) {
	// 		for (int y = 0; y < sensed[x].length; y++) {
	// 			objects[x + xOffset][y + yOffset] = new TWAgentPercept(sensed[x][y], this.getSimulationTime());
	// 		}
	// 	}
	// }

	/**
	 * removes all facts earlier than now - max memory time.
	 * remove probabilistically (exponential decay of memory)
	 */

	public double objectLifetimeEstimate(TWAgentPercept percept){
		int limitPreTime = (int) percept.getFirstT() - lastNullPerceptTime[percept.getO().getX()][percept.getO().getY()];
		assert (limitPreTime >= 0);
		return schedule.getTime() - (percept.getFirstT()-
			(estimateLifeTime+percept.getFirstT()-percept.getT())/estimateLifeTime*Math.min(estimateLifeTime*0.5, limitPreTime));
	}

	public double objectLifeRemainEstimate(TWAgentPercept percept){
		return Math.max(estimateLifeTime - objectLifetimeEstimate(percept), 0);
	}

	public void decayMemory() { 
		for (int x = 0; x < this.objects.length; x++) {
			for (int y = 0; y < this.objects[x].length; y++) {
				TWAgentPercept currentMemory =  objects[x][y];
				if(currentMemory!=null && objectLifetimeEstimate(currentMemory) > estimateLifeTime){
					this.estimatedRemoveObject[x][y] = currentMemory.getO().getClass();
					this.estimatedRemoveObjectTime[x][y][0] = (int) currentMemory.getFirstT();
					this.estimatedRemoveObjectTime[x][y][1] = (int) schedule.getTime();
					removeObject(x,y);
					memorySize--;
		        }
		    }
		}
	}

	public void removeObject(TWEntity o){
		memoryGrid.set(o.getX(), o.getY(), null);
		objects[o.getX()][o.getY()] = null;
		if (closestInSensorRange.get(o.getClass()) != null){
			closestInSensorRange.put(o.getClass(), null);
		}
	}

	public void removeObject(int posx, int posy){
		memoryGrid.set(posx,posy, null);
		objects[posx][posy] = null;
		for (Class<?> key : closestInSensorRange.keySet()){
			TWEntity value = closestInSensorRange.get(key);
			if (value != null && value.getX()==posx && value.getY()==posy){
				closestInSensorRange.put(key,null);
			}
		}
	}


	/**
	 * @return
	 */
	private double getSimulationTime() {
		return schedule.getTime();
	}

	/**
	 * Finds a nearby tile we have seen less than threshold timesteps ago
	 *
	 * @see TWAgentWorkingMemory#getNearbyObject(int, int, double, java.lang.Class)
	 */
	public TWTile getNearbyTile(int x, int y, double threshold) {
		return (TWTile) this.getNearbyObject(x, y, threshold, TWTile.class);
	}

	/**
	 * Finds a nearby hole we have seen less than threshold timesteps ago
	 *
	 * @see TWAgentWorkingMemory#getNearbyObject(int, int, double, java.lang.Class)
	 */
	public TWHole getNearbyHole(int x, int y, double threshold) {
		return (TWHole) this.getNearbyObject(x, y, threshold, TWHole.class);
	}


	/**
	 * Returns the number of items currently in memory
	 */
	public int getMemorySize() {
		return memorySize;
	}



	/**
	 * Returns the nearest object that has been remembered recently where recently
	 * is defined by a number of timesteps (threshold)
	 *
	 * If no Object is in memory which has been observed in the last threshold
	 * timesteps it returns the most recently observed object. If there are no objects in
	 * memory the method returns null. Note that specifying a threshold of one
	 * will always return the most recently observed object. Specifying a threshold
	 * of MAX_VALUE will always return the nearest remembered object.
	 *
	 * Also note that it is likely that nearby objects are also the most recently observed
	 *
	 *
	 * @param x coordinate from which to check for objects
	 * @param y coordinate from which to check for objects
	 * @param threshold how recently we want to have seen the object
	 * @param type the class of object we're looking for (Must inherit from TWObject, specifically tile or hole)
	 * @return
	 */
	private TWObject getNearbyObject(int sx, int sy, double threshold, Class<?> type) {

		//If we cannot find an object which we have seen recently, then we want
		//the one with maxTimestamp
		double maxTimestamp = 0;
		TWObject o = null;
		double time = 0;
		TWObject ret = null;
		int x, y;
		for (Int2D offset : spiral) {
			x = offset.x + sx;
			y = offset.y + sy;

			if (me.getEnvironment().isInBounds(x, y) && objects[x][y] != null) {
				o = (TWObject) objects[x][y].getO();//get mem object
				if (type.isInstance(o)) {//if it's not the type we're looking for do nothing

					time = objects[x][y].getT();//get time of memory

					if (this.getSimulationTime() - time <= threshold) {
						//if we found one satisfying time, then return
						return o;
					} else if (time > maxTimestamp) {
						//otherwise record the timestamp and the item in case
						//it's the most recent one we see
						ret = o;
						maxTimestamp = time;
					}
				}
			}
		}

		//this will either be null or the object of Class type which we have
		//seen most recently but longer ago than now-threshold.
		return ret;
	}

	/**
	 * Used for invalidating the plan, returns the object of a particular type
	 * (Tile or Hole) which is closest to the agent and within it's sensor range
	 *
	 * @param type
	 * @return
	 */
	public TWEntity getClosestObjectInSensorRange(Class<?> type) {
		return closestInSensorRange.get(type);
	}

	private void updateClosest(TWEntity o) {
		assert (o != null);
		if (closestInSensorRange.get(o.getClass()) == null || me.closerTo(o, closestInSensorRange.get(o.getClass()))) {
			closestInSensorRange.put(o.getClass(), o);
		}
	}

	/**
	 * Is the cell blocked according to our memory?
	 * 
	 * @param tx x position of cell
	 * @param ty y position of cell
	 * @return true if the cell is blocked in our memory
	 */
	public boolean isCellBlocked(int tx, int ty) {
		//no memory at all, so assume not blocked
		if (objects[tx][ty] == null) {
			return false;
		}
		TWEntity e = (TWEntity) objects[tx][ty].getO();
		//is it an obstacle?
		return (e instanceof TWObstacle);
	}

	public ObjectGrid2D getMemoryGrid() {
		return this.memoryGrid;
	}
}
