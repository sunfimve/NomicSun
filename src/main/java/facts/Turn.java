package facts;

import enums.TurnType;
import agents.NomicAgent;

/**
 * Defines the current turn of the game of Nomic. A single turn object is managed and updated
 * by the Nomic service to keep track of turn numbers over the course of a simulation.
 * @author Stuart Holland
 *
 */
public class Turn {
	public int number;

	public TurnType type;
	
	public NomicAgent activePlayer;
	
	public boolean allVoted;

	public Turn(int number, TurnType type, NomicAgent activePlayer) {
		super();
		this.number = number;
		this.type = type;
		this.activePlayer = activePlayer;
		allVoted = false;
	}

	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}

	public TurnType getType() {
		return type;
	}
	
	public void setType(TurnType type) {
		this.type = type;
	}
	
	public NomicAgent getActivePlayer() {
		return activePlayer;
	}
	
	/**
	 * True if the game can move on to evaluating proposal success.
	 * @return
	 */
	public boolean isAllVoted() {
		return allVoted;
	}

	public void setAllVoted(boolean allVoted) {
		this.allVoted = allVoted;
	}

	public void setActivePlayer(NomicAgent activePlayer) {
		System.out.println("Turn changing active player from " + this.activePlayer
				+ " to " + activePlayer);
		this.activePlayer = activePlayer;
	}
}
