/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tileworld.planners;

import java.util.ArrayList;
import java.util.LinkedList;
import tileworld.environment.TWDirection;

/**
 * TWPath
 *
 * @author michaellees
 * Created: Apr 22, 2010
 *
 * Copyright michaellees 2010
 *
 *
 * Description:
 *
 * A class to store a path which can be used to move the agent
 *
 * The path is a linked list of PathSteps, Each path step is and x,y coordinate and a direction.
 *
 */
public class TWPath {

    /**
     * The steps of the path.
     */
    private LinkedList<TWPathStep> path;

    /**
     * X and Y coordinate of the last path step added.
     */
    private int lastAddedX, lastAddedY;

    public TWPath(int targetx, int targety) {
        lastAddedX = targetx;
        lastAddedY = targety;
        this.path = new LinkedList<TWPathStep>();
    }

    /**
     * @return the path
     */
    public LinkedList<TWPathStep> getpath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(LinkedList<TWPathStep> path) {
        this.path = path;
    }

    /**
     * Modifys the path with a new path subset, useful when slight modification is needed.
     * @param pathSubSet
     * @param startIndex
     */
    public void modifyPath(ArrayList<TWPathStep> pathSubSet, int startIndex) {
        for (int i = 0; i < pathSubSet.size(); i++) {
            path.set(i+startIndex, pathSubSet.get(i));
        }
    }

    /**
     * Gets step of plan at specified index, not used and is expensive in linked list
     * Her for completeness.
     * @param index
     * @return
     */
    public TWPathStep getStep(int index) {
        return path.get(index);
    }

    private void updateLastAdded(int x, int y) {
        lastAddedX = x;
        lastAddedY = y;
    }

    public void appendStep(TWPathStep d) {
        path.addLast(d);
        updateLastAdded(d.getX(), d.getY());
    }

    //do we need this??
    public void prependStep(TWPathStep d) {
        path.addFirst(d);
        updateLastAdded(d.getX(), d.getY());

    }

    public void appendStep(int x, int y) {
        this.appendStep(new TWPathStep(x, y, this.getDirection(this.lastAddedX, this.lastAddedY, x, y)));
    }

    //do we need this??
    //We are adding in reverse order so go from current x,y to last added to get direction
    public void prependStep(int x, int y) {

        this.prependStep(new TWPathStep(x, y, this.getReverseDirection(this.lastAddedX, this.lastAddedY, x, y)));
    }

    public boolean contains(int x, int y) {
        for (TWPathStep step : path) {
            if (step.getX() == x && step.getY() == y) {
                return true;
            }
        }
        return false;

    }

    /**
     * Returns the direction from startx,starty to goalx, goaly
     * We assume that the two coordinates can only differ in one direction along
     * one dimension. ie., 4 options x++, x--, y++, y--
     *
     * We do acount for the situation where they are the same and return z;
     *
     * @param sx
     * @param sy
     * @param gx
     * @param gy
     * @return
     */
    private TWDirection getDirection(int sx, int sy, int gx, int gy) {
        if (gx > sx) {
            return TWDirection.E;
        } else if (gx < sx) {
            return TWDirection.W;
        } else if (gy > sy) {
            return TWDirection.S;
        } else if (gy < sy) {
            return TWDirection.N;
        } else {
            return TWDirection.Z;
        }
    }

    private TWDirection getReverseDirection(int sx, int sy, int gx, int gy) {
        if (gx > sx) {
            return TWDirection.W;
        } else if (gx < sx) {
            return TWDirection.E;
        } else if (gy > sy) {
            return TWDirection.N;
        } else if (gy < sy) {
            return TWDirection.S;
        } else {
            return TWDirection.Z;
        }
    }

    /**
     * Used for executing the path, removes and returns the first step in the plan.
     * @return
     */
    public TWPathStep popNext() {
        return this.path.pop();
    }

    public boolean hasNext() {
        return this.path.peekFirst() != null;
    }
}
