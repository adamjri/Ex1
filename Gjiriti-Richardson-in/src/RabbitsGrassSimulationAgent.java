import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import java.awt.*;

import java.util.Random;
import java.util.ArrayList;

/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {
	public static int getRandomNumberInRange(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
	
	private int x;
	private int y;
	private RabbitsGrassSimulationSpace space;
	private int energy;
	
	public RabbitsGrassSimulationAgent(RabbitsGrassSimulationSpace space, int x, int y) {
		this.space = space;
		this.x = x;
		this.y = y;
		this.energy = this.space.getInitEnergy();
	}
	
	//rabbits move, spend energy
	public void move() {
		// Compute new spot randomly
		// 0=up, 1=right, 2=down, 3=left
		Boolean isCollision = true;
		int nextDirection;
		int newX = this.x;
		int newY = this.y;
		ArrayList<Integer> checkList = new ArrayList<Integer>();
		for(int i=0; i<4; i++) {
			checkList.add(i);
		}
		
		while(isCollision && checkList.size()>0) {
			newX = this.x;
			newY = this.y;
			Random randomizer = new Random();
			nextDirection = checkList.get(randomizer.nextInt(checkList.size()));
			//remove this direction from list
			checkList.remove((Integer) nextDirection);
			//compute new point
			if(nextDirection%2==0) {
				newY+=(nextDirection-1);
			}
			else {
				newX-=(nextDirection-2);
			}
			newX = this.space.getRabbitSpace().xnorm(newX);
			newY = this.space.getRabbitSpace().ynorm(newY);
			
			//check for collision
			isCollision = (this.space.getRabbitAt(newX, newY)!=null);
		}
		// if rabbit has nowhere to move, it won't move
		// only rabbits that move spend energy
		if(!isCollision) {
			this.energy--;
		}
		
		this.space.putRabbitAt(this.x, this.y, null);
		this.x = newX;
		this.y = newY;
		this.space.putRabbitAt(this.x, this.y, this);
	}
	
	// rabbits eat grass and then potentially reproduce
	public RabbitsGrassSimulationAgent eatReproduce() {
		// check if rabbit has landed on grass to eat
		if(this.space.getGrassAt(this.x, this.y)!=null) {
			//rabbit gains energy from eating
			this.energy+=this.space.getGrassEnergyGain();
			//grass is destroyed
			this.space.removeGrassAt(this.x, this.y);
		}
		// if rabbit energy is above threshold, it reproduces
		if(this.energy>this.space.getReproduceEnergyThreshold()) {
			// rabbit loses some energy
			this.energy-=this.space.getReproduceEnergyLoss();
			// randomly place new rabbit
			return this.space.placeRandomRabbit();
		}
		return null;
		
	}

	public void draw(SimGraphics arg0) {
		arg0.drawFastRect(Color.white);
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}
	
	public int getEnergy() {
		return this.energy;
	}

}
