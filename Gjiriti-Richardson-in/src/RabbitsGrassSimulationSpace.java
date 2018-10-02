import uchicago.src.sim.space.Object2DTorus;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace{
	// the following 2 methods are used to find a uniformly random open space for a rabbit to spawn
	private Dimension linearToTuple(int position) {
		int maxX = this.dim.width;
		int x = position%maxX;
		int y = (position-x)/maxX;
		return new Dimension(x, y);
	}
	private Dimension getRandomOpenSpace() {
		Dimension d = new Dimension();
		ArrayList<Integer> positionList = new ArrayList<Integer>();
		int maxX = this.dim.width;
		int maxY = this.dim.height;
		for(int i=0; i<maxX*maxY; i++) {
			positionList.add(i);
		}
		Boolean isCollision = true;
		while(isCollision && positionList.size()>0) {
			Random randomizer = new Random();
			int position = positionList.get(randomizer.nextInt(positionList.size()));
			//remove this position from list
			positionList.remove((Integer) position);
			//compute new point
			d = this.linearToTuple(position);
			
			//check for collision
			isCollision = (this.getRabbitAt(d.width, d.height)!=null);
		}
		if(isCollision) {
			d.setSize(-1, -1);
		}
		return d;	
	}
	
	// class for a grass "agent"
	private class Grass implements Drawable{
		private int x;
		private int y;
		
		public Grass(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public void draw(SimGraphics arg0) {
			arg0.drawFastRect(Color.green);
		}

		public int getX() {
			return this.x;
		}

		public int getY() {
			return this.y;
		}
	}
	
	private Dimension dim;
	private Object2DTorus rabbitSpace;
	private Object2DTorus grassSpace;
	private ArrayList<Grass> grassList;
	
	private int initEnergy;
	private double grassSpawnProb;
	private int grassEnergyGain;
	private int reproduceEnergyThreshold;
	private int reproduceEnergyLoss;
	
	public RabbitsGrassSimulationSpace(int xSize, int ySize, int initEnergy,
										double grassSpawnProb, int grassEnergyGain,
										int reproduceEnergyThreshold, int reproduceEnergyLoss) {
		this.dim = new Dimension(xSize, ySize);
		this.rabbitSpace = new Object2DTorus(xSize, ySize);
		this.grassSpace = new Object2DTorus(xSize, ySize);
		this.grassList = new ArrayList<Grass>();
		
		this.initEnergy = initEnergy;
		this.grassSpawnProb = grassSpawnProb;
		this.grassEnergyGain = grassEnergyGain;
		this.reproduceEnergyThreshold = reproduceEnergyThreshold;
		this.reproduceEnergyLoss = reproduceEnergyLoss;
	}
	
	public void putRabbitAt(int x, int y, RabbitsGrassSimulationAgent r) {
		this.rabbitSpace.putObjectAt(x, y, r);
	}
	
	public RabbitsGrassSimulationAgent placeRandomRabbit() {
		Dimension new_location = this.getRandomOpenSpace();
    	RabbitsGrassSimulationAgent r = new RabbitsGrassSimulationAgent(this,
    																	new_location.width,
    																	new_location.height);
    	this.putRabbitAt(new_location.width, new_location.height, r);
    	return r;
	}
	
	public Object2DTorus getRabbitSpace() {
		return this.rabbitSpace;
	}
	public Object2DTorus getGrassSpace() {
		return this.grassSpace;
	}
	
	public Object getRabbitAt(int x, int y) {
		return this.rabbitSpace.getObjectAt(x, y);
	}
	
	public void removeRabbitAt(int x, int y) {
		this.rabbitSpace.putObjectAt(x, y, null);
	}
	
	public void putGrassAt(int x, int y) {
		if (this.getGrassAt(x, y)==null) {
			Grass g = new Grass(x, y);
			this.grassList.add(g);
			this.grassSpace.putObjectAt(x, y, g);
		}
	}
	public ArrayList<Grass> getGrassList() {
		return this.grassList;
	}
	
	public Object getGrassAt(int x, int y) {
		return this.grassSpace.getObjectAt(x, y);
	}
	
	public void removeGrassAt(int x, int y) {
		grassList.remove(this.getGrassAt(x, y));
		this.grassSpace.putObjectAt(x, y, null);
	}
	
	public void spawnGrass() {
		// for each cell, randomly spawn grass 
		for(int i=0; i<this.dim.width*this.dim.height; i++) {
			if(Math.random()<this.grassSpawnProb) {
				//spawn grass in this cell
				Dimension cellD = this.linearToTuple(i);
				this.putGrassAt(cellD.width, cellD.height);
			}
		}
	}
	
	public Dimension getSize() {
		return this.dim;
	}
	
	public int getInitEnergy() {
		return this.initEnergy;
	}
	
	public int getReproduceEnergyThreshold() {
		return this.reproduceEnergyThreshold;
	}
	
	public int getReproduceEnergyLoss() {
		return this.reproduceEnergyLoss;
	}
	
	public int getGrassEnergyGain() {
		return this.grassEnergyGain;
	}
	
}
