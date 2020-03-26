
package tileworld.agent;

import tileworld.environment.TWEntity;

/**
 * TWAgentPercept
 *
 * @author michaellees
 * Created: Apr 15, 2010
 *
 * Copyright michaellees 2010
 *
 *
 * Description:
 *
 * Stores a sensed object from the environment. Used in the Working Memory of
 * the agent. Has two main fields, TWEntity: a reference to the sensed object
 * and t: the time at which the object was seen
 *
 */
public class TWAgentPercept{


    final int BEFORE = -1;
    final int EQUAL = 0;
    final int AFTER = 1;

	private TWEntity o;
	private double t;  //最后一次观测的时间
    private double firstT;   //第一次观测到的时间
	
	/**
	 * @return the t
	 */
	public double getT() {
		return t;
	}
    public double getFirstT(){
        return firstT;
    }
	/**
	 * @param t the t to set
	 */
    public void setFirstT(double t){
        this.firstT = t;
    }
	public void setT(double t) {
		this.t = t;
	}
	/**
	 * @return the o
	 */
	public TWEntity getO() {
		return o;
	}
	/**
	 * @param o the o to set
	 */
	public void setO(TWEntity o) {
		this.o = o;
	}
	
	
	/**
	 * @param t time at which the memory item was created
	 * @param o the object which was observed
	 */
	public TWAgentPercept(TWEntity o, double t, double ft) {
		super();
		this.t = t;
		this.o = o;
        this.firstT = ft;
	}

	/**
	 * true if fact is a newer version of the same memory (ie., see the same
	 * tile twice)
	 *
	 * @param fact
	 * @return
	 */
    public boolean newerFact(Object fact) {
        if(!(fact instanceof TWAgentPercept) ) {return false;}
        TWAgentPercept twf= (TWAgentPercept)fact;
        if(twf.o == this.o){
            if(this.t <= twf.t){
                return true;
            }else if(this.t > twf.t){
                return false;
            }
        }
        return false;
    }

    /**
     * Facts are equal if they consider the same object, regardless of time.
     * @param fact
     * @return
     */
    public boolean sameObject (Object fact){
        if(this == fact) return true;
        if(!(fact instanceof TWAgentPercept )) return false;
        TWAgentPercept twf= (TWAgentPercept)fact;
        return (this.o == twf.o);
    }
	


    @Override
    public boolean equals(Object o) {return sameObject(o);}
	
}
