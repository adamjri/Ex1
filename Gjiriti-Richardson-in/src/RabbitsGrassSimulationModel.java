import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;

import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;

import java.util.ArrayList;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {
	// DEFAULT VALUES
	public static int WIDTH = 20;
	public static int HEIGHT = 20;
	public static int INITENERGY = 100;
	public static int NUMRABBITSINIT = 20;
	public static int GRASSENERGYGAIN = 10;
	public static int REPRODUCEENERGYTHRESHOLD = 100;
	public static int REPRODUCEENERGYLOSS = 12;
	public static double GRASSSPAWNPROB = 0.01;
	// Parameters
	private int worldWidth = WIDTH;
	private int worldHeight = HEIGHT;
	private int initialRabbitEnergy = INITENERGY;
	private int initialRabbitsPop = NUMRABBITSINIT;
	private int grassEnergyValue = GRASSENERGYGAIN;
	private int reproduceEnergyThreshold = REPRODUCEENERGYTHRESHOLD;
	private int reproduceEnergyLoss = REPRODUCEENERGYLOSS;
	private double grassSpawnProbability = GRASSSPAWNPROB;
	
	// instantiate model
	private Schedule schedule;
	private RabbitsGrassSimulationSpace space;
	private ArrayList<RabbitsGrassSimulationAgent> rabbitList;
	
	private DisplaySurface dsurf;
	private OpenSequenceGraph plot;
	
	class RabbitsPlot implements Sequence{
		public double getSValue() {
			return rabbitList.size();
		}
	}
	class GrassPlot implements Sequence{
		public double getSValue() {
			return space.getGrassList().size();
		}
	}
	
	// build model at beginning of simulation
	private void buildModel() {
	    this.space = new RabbitsGrassSimulationSpace(this.worldWidth, this.worldHeight,
	    											this.initialRabbitEnergy,
	    											this.grassSpawnProbability, this.grassEnergyValue,
	    											this.reproduceEnergyThreshold, this.reproduceEnergyLoss);
	    for (int i=0; i<this.initialRabbitsPop; i++) {
	    	RabbitsGrassSimulationAgent r = this.space.placeRandomRabbit();
	    	this.rabbitList.add(r);
	    }
	    
	    // build graph for plotting
	    plot = new OpenSequenceGraph("Rabbit-Grass Populations", this);
	    plot.setYRange(0, this.worldWidth*this.worldHeight/4);
	    plot.setAxisTitles("time", "populations");
	    
	    plot.addSequence("Rabbits", new RabbitsPlot());
	    plot.addSequence("Grass", new GrassPlot());
	    
	}
	
	// initialize scheduler
	private void buildSchedule() {
		this.schedule.scheduleActionBeginning(1, this, "step");
	}
	
	// build display object
	private void buildDisplay() {
	    
		Object2DDisplay rabbitDisplay = new Object2DDisplay(space.getRabbitSpace());
	    rabbitDisplay.setObjectList (rabbitList);
	    Object2DDisplay grassDisplay = new Object2DDisplay(space.getGrassSpace());
	    grassDisplay.setObjectList(space.getGrassList());
		
	    this.dsurf.addDisplayable(grassDisplay, "Grass");
	    this.dsurf.addDisplayable(rabbitDisplay, "Rabbits");
	}
	
	// reset and initialize simulation
	public void setup() {
		// clear display and schedule
		if (this.dsurf != null) {
			this.dsurf.dispose();
		}
		this.dsurf = null;
		this.schedule = null;
	    
	    // reset rabbit list and clear simulation space
	    this.rabbitList = new ArrayList<RabbitsGrassSimulationAgent> ();
	    this.space = null;
	    
	    // initialize parameters to default values
	    worldWidth = WIDTH;
	    worldHeight = HEIGHT;
	    initialRabbitEnergy = INITENERGY;
	    initialRabbitsPop = NUMRABBITSINIT;
	    grassEnergyValue = GRASSENERGYGAIN;
	    reproduceEnergyThreshold = REPRODUCEENERGYTHRESHOLD;
	    reproduceEnergyLoss = REPRODUCEENERGYLOSS;
	    grassSpawnProbability = GRASSSPAWNPROB;
	    
	    System.gc();
	    
	    // initialize new display and schedule
	    this.dsurf = new DisplaySurface (this, "Rabbits-Grass Display");
	    registerDisplaySurface ("Main", this.dsurf);
	    this.schedule = new Schedule(1);
	}
	
	// begin a simulation
	public void begin() {
		if(this.assertConstraints()) {
			buildModel();
			buildDisplay();
		    buildSchedule();
		    dsurf.display();
		    plot.display();
		}
	}
	
	// execute step in simulation
	public void step () {
		//rabbits move and spend energy
	    for (int i = 0; i < rabbitList.size (); i++) {
	    	RabbitsGrassSimulationAgent r = rabbitList.get(i);
	    	r.move();
	    }
	    
	    // grass/weeds spawn
	    this.space.spawnGrass();
	    
	    //rabbits eat grass/weeds, reproduce
	    for (int i = 0; i < rabbitList.size (); i++) {
	    	RabbitsGrassSimulationAgent r = rabbitList.get(i);
	    	RabbitsGrassSimulationAgent reproduced = r.eatReproduce();
	    	if(reproduced!=null) {
	    		this.rabbitList.add(reproduced);
	    	}
	    }
	    
	    // kill all dead rabbits
	    ArrayList<Integer> killList = new ArrayList<Integer>();
	    for (int i = rabbitList.size()-1; i >=0; i--) {
	    	RabbitsGrassSimulationAgent r = rabbitList.get(i);
	    	if(r.getEnergy()<1) {
	    		killList.add(i);
	    	}
	    }
	    for (int i=0; i<killList.size(); i++) {
		    // kill rabbit
	    	int index = killList.get(i);
	    	RabbitsGrassSimulationAgent r = rabbitList.get(index);
			this.rabbitList.remove(index);
			this.space.removeRabbitAt(r.getX(), r.getY());
	    }
	    
	    // garbage collect
	    System.gc();
	    
	    // update display
	    plot.step();
	    dsurf.updateDisplay ();
	}

	// get string list of simulation parameters
	public String[] getInitParam() {
		String[] params = {"worldWidth", "worldHeight",
							"initialRabbitEnergy", "initialRabbitsPop",
							"grassEnergyValue", "grassSpawnProbability",
							"reproduceEnergyThreshold", "reproduceEnergyLoss"};
		return params;
	}

	// get name of simulation
	public String getName() {
		return "Rabbits-Grass Simulation";
	}

	// access schedule
	public Schedule getSchedule() {
		return schedule;
	}
	
	// properties
	public int getWorldWidth () {
		return worldWidth;
	}

	public void setWorldWidth (int worldWidth) {
		this.worldWidth = worldWidth;
	}

	public int getWorldHeight () {
		return worldHeight;
	}

	public void setWorldHeight (int worldHeight) {
		this.worldHeight = worldHeight;
	}

	public int getInitialRabbitEnergy () {
		return initialRabbitEnergy;
	}

	public void setInitialRabbitEnergy (int initialRabbitEnergy) {
		this.initialRabbitEnergy = initialRabbitEnergy;
	}

	public int getInitialRabbitsPop () {
		return initialRabbitsPop;
	}

	public void setInitialRabbitsPop (int initialRabbitsPop) {
		this.initialRabbitsPop = initialRabbitsPop;
	}

	public int getGrassEnergyValue () {
		return grassEnergyValue;
	}

	public void setGrassEnergyValue (int grassEnergyValue) {
		this.grassEnergyValue = grassEnergyValue;
	}

	public int getReproduceEnergyThreshold () {
		return reproduceEnergyThreshold;
	}

	public void setReproduceEnergyThreshold (int reproduceEnergyThreshold) {
		this.reproduceEnergyThreshold = reproduceEnergyThreshold;
	}

	public int getReproduceEnergyLoss () {
		return reproduceEnergyLoss;
	}

	public void setReproduceEnergyLoss (int reproduceEnergyLoss) {
		this.reproduceEnergyLoss = reproduceEnergyLoss;
	}

	public double getGrassSpawnProbability () {
		return grassSpawnProbability;
	}

	public void setGrassSpawnProbability (double grassSpawnProbability) {
		this.grassSpawnProbability = grassSpawnProbability;
	}
	
	// assert constraints on parameters
	public Boolean assertConstraints() {
		Boolean isValid = true;
		if(this.grassSpawnProbability<0 || this.grassSpawnProbability>1) {
			System.out.println("GrassSpawnProbability must be between 0 and 1 (inclusive)");
			isValid = false;
		}
		if(this.grassEnergyValue<1) {
			System.out.println("GrassEnergyValue must be positive");
			isValid = false;
		}
		if(this.initialRabbitEnergy<1) {
			System.out.println("InitialRabbitEnergy must be positive");
			isValid = false;
		}
		if(this.initialRabbitsPop<1) {
			System.out.println("InitialRabbitsPop must be positive");
			isValid = false;
		}
		if(this.reproduceEnergyLoss<1) {
			System.out.println("ReproduceEnergyLoss must be positive");
			isValid = false;
		}
		if(this.worldHeight<1 || this.worldWidth<1) {
			System.out.println("World dimensions must be positive integers");
			isValid = false;
		}
		if(this.initialRabbitEnergy>this.reproduceEnergyThreshold) {
			System.out.println("ReproduceEnergyThreshold must be greater than or equal to InitialRabbitEnergy");
			isValid = false;
		}
		return isValid;
	}
	
	// start simulation GUI
	public static void main(String[] args) {
		uchicago.src.sim.engine.SimInit init = new uchicago.src.sim.engine.SimInit ();
		RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel ();
		if (args.length > 0)
	      init.loadModel (model, args[0], false);
	    else
	      init.loadModel (model, null, false);
	}
}
