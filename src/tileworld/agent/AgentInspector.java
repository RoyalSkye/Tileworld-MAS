/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tileworld.agent;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import sim.display.Controller;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;

/**
 * AgentInspector
 *
 * @author michaellees
 * Created: Apr 26, 2010
 *
 * Copyright michaellees 2010
 *
 *
 * Description:
 * 
 * An Inspector implementation for the TWAgent class.
 * This class implements the controller display which will be displayed when
 * the user clicks on the agent in the GUI.
 *
 * Currently this inspector outputs some information about the agent and adds
 * a button to randomize the agent. This isn't very useful and really for
 * demonstration purpose.
 *
 */
import sim.portrayal.grid.ObjectGridPortrayal2D;

import sim.util.Int2D;

class AgentInspector extends Inspector {

    public Inspector originalInspector;

    /**
     * Constructor for the agent inspector, adds a JBUtton to the existing
     * (original) inspector
     *
     * @param originalInspector
     * @param wrapper
     * @param guiState
     */
    public AgentInspector(Inspector originalInspector,
            LocationWrapper wrapper,
            GUIState guiState) {
        this.originalInspector = originalInspector;

        // get info out of the wrapper
        ObjectGridPortrayal2D gridportrayal = (ObjectGridPortrayal2D) wrapper.getFieldPortrayal();
        // these are final so that we can use them in the anonymous inner class below...
        final ObjectGrid2D grid = (ObjectGrid2D) (gridportrayal.getField());
        final TWAgent agent = (TWAgent) wrapper.getObject();
        final SimState state = guiState.state;
        final Controller console = guiState.controller;  // The Console (it's a Controller subclass)

        // now let's add a Button
        Box box = new Box(BoxLayout.X_AXIS);
        JButton button = new JButton("Teleport Agent");
        box.add(button);
        
        box.add(Box.createGlue());

        // set up our inspector: keep the properties inspector around too
        setLayout(new BorderLayout());

        add(originalInspector, BorderLayout.CENTER);

        add(box, BorderLayout.SOUTH);

        // set what the button does
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                synchronized (state.schedule) {
                    // reset plan if we have one
                
                    // randomize location
                    Int2D loc = agent.getEnvironment().generateRandomLocation();

//                    agent.getEnvironment().getGrid().set(agent.getX(), agent.getY(), null);
                    agent.getEnvironment().getAgentGrid().set(agent.getX(), agent.getY(), null);
//                    agent.getEnvironment().getObjectGrid().set(loc.x, loc.y, agent);
                    agent.setLocation(loc);

                    // repaint everything: console, inspectors, displays,
                    // everything that might be affected by randomization
                    console.refresh();
                }
            }
        });
    }

    public void updateInspector() {
        originalInspector.updateInspector();
    }
}
