/**
 * 
 */
package tileworld.environment;

import ec.util.MersenneTwisterFast;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import sim.util.Bag;
import sim.util.Int2D;
import tileworld.Parameters;

/**
 * TWObjectCreator
 * 
 * @author michaellees Created: Apr 16, 2010
 * 
 * Copyright michaellees 2010
 * 
 * Description:
 *
 * A class to create all types of TWObjects, the generic type T is the type of
 * TWObject.
 */
public class TWObjectCreator<T> {

    double mean;
    double dev;
    MersenneTwisterFast random;
    TWEnvironment env;
    private T instance;
    Class[] classes = {Int2D.class, TWEnvironment.class, Double.class, Double.class};

    /**
     * @param mean
     * @param dev
     * @param tileCreationDistribution
     */
    public TWObjectCreator(double mean, double dev,
            Bag context, MersenneTwisterFast random, T inst, TWEnvironment env) {
        super();
        this.mean = mean;
        this.dev = dev;
        instance = inst;

        this.env = env;
        // create object creation distributions (assumed normal for now)
        this.random = random;

    }

    public Bag createTWObjects(double time) throws IllegalAccessException, InstantiationException {

        //translate the random variable between [0,1] to specified mean and std deviation
        double numberObjects = mean + dev * this.random.nextGaussian();

        Bag items = new Bag();
        for (int i = 0; i < Math.floor(numberObjects); i++) {
            T o = null;
            try {
                o = create((Class<T>) instance.getClass(), time);
            } catch (InstantiationException e) {
                e.printStackTrace();
                throw e;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw e;
            }
            items.add((TWObject) o);
        }

        if (numberObjects - Math.floor(numberObjects) > this.random.nextDouble()) {
            try {
                items.add((TWObject) create((Class<T>) instance.getClass(), time));
            } catch (InstantiationException e) {
                e.printStackTrace();
                throw e;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw e;
            }
        }



        return items;
    }

    /**
     * Creates an instance of the appropriate type (according to T) and then
     * sets the relevant parameters such as deathtime and location
     *
     * @param clazz class of type to create
     * @param time timestamp of creation
     * @return the created object
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private T create(Class<T> clazz, double time) throws InstantiationException, IllegalAccessException {

        Int2D pos = env.generateRandomLocation();

        T o = null;
        try {
            o = clazz.getDeclaredConstructor(classes).newInstance(pos, env, time, (time + Parameters.lifeTime));
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(TWObjectCreator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(TWObjectCreator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(TWObjectCreator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(TWObjectCreator.class.getName()).log(Level.SEVERE, null, ex);
        }

        assert (o instanceof TWObject);
        return o;
    }
}
