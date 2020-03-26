/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.planners;

/**
 * Interface for all path generators, see AstarPathGenerator as an example.
 *
 *
 * @author michaellees
 */

public interface TWPathGenerator {

    /**
     * Returns a TWPath calculated between the start sqaure sx,sy and the target
     * square at tx,ty.
     *
     * @param sx x-coordinate of start location
     * @param sy y-coordinate of start location
     * @param tx x-coordinate of target location
     * @param ty y-coordinate of target location
     * @return A path between (sx,sy) and (tx,ty)
     */
    public TWPath findPath(int sx, int sy, int tx, int ty);
}
