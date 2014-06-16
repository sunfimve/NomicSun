package actions;

import java.util.UUID;

import uk.ac.imperial.presage2.core.util.random.Random;
import agents.NomicAgent;
import enums.VoteType;

/**
 * Vote action. Usable with environment.act from within an a class extending <code>AbstractParticipant</code>.
 * @author Stuart Holland
 *
 */
public class Vote extends TimeStampedAction {
	protected NomicAgent Voter;
	
	protected VoteType Vote;
	
	/**
	 * Votes have unique IDs, otherwise they are indistinguishable within the Drools knowledge base.
	 */
	protected UUID voteID;
	
	protected float voteWeight;
	
	/**
	 * Action should be performed when the agent is prompted for a vote.
	 * @param voter Agent casting this vote.
	 * @param vote Vote to be cast.
	 */
	public Vote(NomicAgent voter, VoteType vote) {
		Voter = voter;
		
		Vote = vote;
		
		voteID = Random.randomUUID();
		
		this.voteWeight = 0;
		
		if (Vote == VoteType.YES){
			voteWeight = Voter.getAuthority();
		}
		else if (Vote == VoteType.NO){
			voteWeight = - Voter.getAuthority();
		}
	}

	public NomicAgent getVoter() {
		return Voter;
	}

	public VoteType getVote() {
		return Vote;
	}

	/**
	 * Vote ID does not correspond to the ID of the agent that performed this vote.
	 * @return
	 */
	public UUID getVoteID() {
		return voteID;
	}
	
	public float getVoteValue(){
		return voteWeight;
	}
}
