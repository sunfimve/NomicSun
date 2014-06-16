package actions;

import uk.ac.imperial.presage2.core.Action;

/**
 * Parent action to allow for actions to be timestamped in a generic way.
 * @author Stuart Holland
 *
 */
public abstract class TimeStampedAction implements Action {
	/**
	 * t corresponds to the current turn number within the game of Nomic
	 */
	int t;
	
	/**
	 * simTime corresponds to the phases of play in Nomic.
	 * Eg. Turn 0, Propose phase has simTime 0. Turn 0 Vote phase has simTime 1. Turn 1 Propose phase has simTime 2. Etc.
	 */
	int simTime;

	protected TimeStampedAction() {
		super();
	}

	protected TimeStampedAction(int t) {
		super();
		this.t = t;
	}

	public int getT() {
		return t;
	}

	public void setT(int t) {
		this.t = t;
	}

	public int getSimTime() {
		return simTime;
	}

	public void setSimTime(int simTime) {
		this.simTime = simTime;
	}
}
