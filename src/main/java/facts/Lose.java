package facts;

import agents.NomicAgent;

/**
 * Simple object that triggers a win for the agent given in its constructor parameter.
 * @author 
 *
 */
public class Lose {
	NomicAgent causer;

	public Lose(NomicAgent lc) {
		this.causer = lc;
		//this.winner.Win();
	}
	
	public NomicAgent getCauser() {
		return causer;
	}

	public void setCauser(NomicAgent lc) {
		this.causer = lc;
	}
}
