package tileworld;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.grid.ObjectGridPortrayal2D;
import tileworld.agent.TWAgent;
import tileworld.environment.TWEnvironment;
import tileworld.environment.TWFuelStation;
import tileworld.environment.TWHole;
import tileworld.environment.TWObstacle;
import tileworld.environment.TWTile;

/**
 * TWGUI
 *
 * @author michaellees
 * Created: Apr 19, 2010
 *
 * Copyright michaellees 2010
 *
 * Description:
 *
 * A class implementing the basic TWGUI as required by the MASON agent toolkit.
 * This class is responsible for displaying the model. In MASON the model and
 * the visualizer are completely decoupled. The model contains fields, these
 * fields are visualized using portrayals. Part of the job of this class is to
 * associate portrayals with fields.
 *
 */
public class TWGUI extends GUIState {

    /**
     * Main display 2D
     */
    public Display2D display;
    /**
     * Frame which displays
     */
    public JFrame displayFrame;
    /**
     * Number of pixels that each cell should be represented by (for display).
     */
    private static final int CELL_SIZE_IN_PIXELS = 10;
    public static TWGUI instance;
    private int count=0;
    /**
     * USed constructor, initializes the GUI sim state with the pased
     * @param state
     */
    public TWGUI(SimState state) {    	
        super(state);
        instance = this; 
    }

    /**
     * Default constructor, creates a TWEnvironment
     */
    private TWGUI() {
        this(new TWEnvironment());
    }

    public static String getName() {
        return "Tileworld in MASON";
    }
    /**
     * Portrayal of the main grid which is the environment. Using a standard ObjectGridPortrayal2D.
     */
    ObjectGridPortrayal2D objectGridPortrayal = new ObjectGridPortrayal2D();

    /**
     * Portrayal for agent layer
     */

    ObjectGridPortrayal2D agentGridPortrayal = new ObjectGridPortrayal2D();



    List<ObjectGridPortrayal2D> memoryGridPortrayalList = new ArrayList<ObjectGridPortrayal2D>();

    /**
     * Creates the portrayals for all the relevant objects, including the environment itself.
     * There is a default portrayal for each TWObject which is defined in the appropriate class.
     * Each portrayal describes how the objects appear in the GUI.
     */
    public void setupPortrayals() {
        
        
      
        // tell the portrayals what to portray and how to portray them
        objectGridPortrayal.setField(((TWEnvironment) state).getObjectGrid());


        agentGridPortrayal.setField(((TWEnvironment) state).getAgentGrid());

        agentGridPortrayal.setPortrayalForClass(TWAgent.class, TWAgent.getPortrayal());

       // gridPortrayal.setPortrayalForClass(SimpleTWAgent.class, TWAgent.getPortrayal());
        agentGridPortrayal.setPortrayalForRemainder(TWAgent.getPortrayal());

        objectGridPortrayal.setPortrayalForClass(TWHole.class, TWHole.getPortrayal());
        objectGridPortrayal.setPortrayalForClass(TWTile.class, TWTile.getPortrayal());
        objectGridPortrayal.setPortrayalForClass(TWObstacle.class, TWObstacle.getPortrayal());
        objectGridPortrayal.setPortrayalForClass(TWFuelStation.class, TWFuelStation.getPortrayal());

       

        //reset and repaint after adding portrayals
        display.reset();
        display.repaint();
    }

    /**
     * Called at startup - calls start on parent and then sets up the portrayals
     */
    @Override
    public void start() {
        super.start();
        setupPortrayals();
    }

    /**
     * Init is called when the window is initialized
     * You can use this to set up the windows, then register them with the
     * Controller so it can manage hiding, showing, and moving them
     *
     * @param c controller
     */
    @Override
    public void init(Controller c) {
        super.init(c);

        // Make the Display2D.  We'll have it display stuff later.
        TWEnvironment tw = (TWEnvironment) state;
        display = new Display2D(tw.getxDimension() * CELL_SIZE_IN_PIXELS, tw.getyDimension() * CELL_SIZE_IN_PIXELS, this, 1);

        //create and display frame
        displayFrame = display.createFrame();
        c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
        displayFrame.setVisible(true);

        // attach the portrayal for the grid field
        display.attach(objectGridPortrayal, "Tileworld objects");
        display.attach(agentGridPortrayal, "Tileworld Agents");
        
        

        // specify the backdrop color  -- what gets painted behind the displays
        display.setBackdrop(Color.gray);
    }


    /**
     * Main method called when running Tileworld with a visual display. It is
     * possible to run without any display - see
     * @param args
     */
    public static void main(String[] args) {

        TWGUI twGui = new TWGUI();

        Console c = new Console(twGui);
        c.setVisible(true);
    }


    /**
     * Called by the Console when the user is quitting the SimState.
     */
    public void quit() {
        super.quit();
        //get rid of the frame if necessary
        if (displayFrame != null) {
            displayFrame.dispose();
        }

        //set to null to allow for garbage collection
        displayFrame = null;  // let gc
        display = null;       // let gc
        
        System.out.println("Final reward: "+((TWEnvironment)state).getReward());
    }

    /**
     * String to display in the control window.
     * 
     * @return
     */
    public static Object getInfo() {
        return "<H2>Tileworld</H2><p>An implementation of Tileworld in MASON.";
    }

    /**
     * Adds a portrayal of the memory of the agent.
     * 
     * @param agent 
     */
    public void addMemoryPortrayal(TWAgent agent) {
        ObjectGridPortrayal2D memoryPortrayal = new ObjectGridPortrayal2D();
        memoryPortrayal.setField(agent.getMemory().getMemoryGrid());
        memoryPortrayal.setPortrayalForClass(TWHole.class, TWHole.getPortrayal());
        memoryPortrayal.setPortrayalForClass(TWTile.class, TWTile.getPortrayal());
        memoryPortrayal.setPortrayalForClass(TWObstacle.class, TWObstacle.getPortrayal());
        memoryPortrayal.setPortrayalForClass(TWFuelStation.class, TWFuelStation.getPortrayal());
        display.attach(memoryPortrayal, agent.getName() +"'s Memory");
    }

    public void resetDisplay() {
        display.detatchAll();
        display.attach(objectGridPortrayal, "Tileworld objects");
        display.attach(agentGridPortrayal, "Tileworld Agents");
        
        

        // specify the backdrop color  -- what gets painted behind the displays
        display.setBackdrop(Color.gray);
    }
}
