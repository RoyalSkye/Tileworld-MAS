/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.environment;

import java.util.ArrayList;
import java.util.List;
import sim.util.Int2D;

/**
 * NeighbourSpiral
 *
 * @author michaellees
 * Created: Apr 26, 2010
 *
 * Copyright Michael lees 2010
 *
 * Description:
 *
 * Used in the agent memory to work out the nearest object from the current
 * location. Essentially spirals around a central square using the TWDirection.
 *
 */

public class NeighbourSpiral {
	Int2D point;
	TWDirection direction = TWDirection.E;
	private List<Int2D> list = new ArrayList<Int2D>();
	int maxRadius;
	public NeighbourSpiral(int maxRadius){
		this.maxRadius = maxRadius;
		list=spiral();
	}

	public List<Int2D> spiral() {
		point = TWDirection.ORIGIN;
		int steps = 1;
		while (steps < maxRadius * 2) {
			advance(steps);
			advance(steps);
			steps++;
		}
		return list;
	}

	private void advance(int n) {
		for (int i = 0; i < n; ++i) {
			list.add(point);
			point = direction.advance(point);
		}
		direction = direction.next();
	}

	public static void main(String[] args){
		NeighbourSpiral ns = new NeighbourSpiral(3);
		List<Int2D> list = ns.spiral();
		System.out.println("Spiral");

	}
}