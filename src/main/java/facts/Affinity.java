package facts;

import enums.TurnType;
import agents.NomicAgent;

/**
 * Defines the current turn of the game of Nomic. A single turn object is managed and updated
 * by the Nomic service to keep track of turn numbers over the course of a simulation.
 * @author Hanguang Sun
 *
 */
public class Affinity {
	public int Aff = 50;
	
	public NomicAgent Target;
	

	public Affinity(NomicAgent t, int a) {
		super();
		this.Aff = a;
		this.Target = t;
	}

	
}
