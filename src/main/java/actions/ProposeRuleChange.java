package actions;

import java.util.Map;

import agents.NomicAgent;
import enums.ParticipationProposalType;
import enums.PurposeType;
import enums.RuleChangeType;
import enums.RuleValueType;
import facts.RateChange;

/**
 * Parent class to structure common elements in all rule change proposals.
 * If trying to make a new proposal, you want to use <code>ProposeRuleAddition</code>,
 * <code>ProposeRuleRemoval</code>, <code>ProposalRuleModification</code>, or <code>ProposeNoRuleChange</code>.
 * 
 * Proposals are only that, proposals. They will not be applied to the currently active rules until voting
 * has taken place (unless some previous inventive rule changes has removed the need to vote).
 * @author Stuart Holland
 *
 */
public class ProposeRuleChange extends TimeStampedAction {
	protected NomicAgent proposer;
	
	protected RuleChangeType Type;
	
	protected RuleValueType vType;
	
	protected ParticipationProposalType pType;
	
	protected Map<NomicAgent,PurposeType> purposes;


	protected NomicAgent TAgent;
	
	public RateChange rate = new RateChange();
	
	protected boolean succeeded;
	
	
	
	protected int Envthreat;
	
	

	protected int Targetthreat;
	
	protected int MoralChangeLevel = 0;
	
	/**
	 * The time this change remains effective.
	 */
	protected int timer = -1;
	
	public ProposeRuleChange(NomicAgent proposer) {
		super();
		this.proposer = proposer;
	}
	
	public Map<NomicAgent, PurposeType> getPurposes() {
		return purposes;
	}


	public void setPurposes(Map<NomicAgent, PurposeType> purposes) {
		this.purposes = purposes;
	}

	
	

	public ParticipationProposalType getpType() {
		return pType;
	}


	public void setpType(ParticipationProposalType pType) {
		this.pType = pType;
	}
	
	public NomicAgent getTAgent() {
		return TAgent;
	}

	public void setTAgent(NomicAgent tAgent) {
		TAgent = tAgent;
	}

	
	
	public int getTimer() {
		return timer;
	}

	public void setTimer(int timer) {
		this.timer = timer;
	}

	public void setRuleChangeType(RuleChangeType t) {
		this.Type = t;
	}
	
	public RuleChangeType getRuleChangeType() {
		return Type;
	}
	
	public RuleValueType getRuleValueType() {
		return vType;
	}
	
	public void setValueType(RuleValueType vType) {
		this.vType = vType;
	}
	
	public int getEnvthreat() {
		return Envthreat;
	}


	public void setEnvthreat(int envthreat) {
		Envthreat = envthreat;
	}


	public int getTargetthreat() {
		return Targetthreat;
	}


	public void setTargetthreat(int targetthreat) {
		Targetthreat = targetthreat;
	}


	public int getMoralChangeLevel() {
		return MoralChangeLevel;
	}


	public void setMoralChangeLevel(int moralChangeLevel) {
		MoralChangeLevel = moralChangeLevel;
	}
	
	

	public boolean getSucceeded() {
		return succeeded;
	}
	
	public void setSucceeded(boolean succeeded) {
		this.succeeded = succeeded;
		System.out.println("Successful proposal object from " + proposer.getName());
	}

	public NomicAgent getProposer() {
		return proposer;
	}
	
	public void decrementTimer(){
		if (this.timer > 0){
			--this.timer;
		}
		if (timer == 0){
			this.succeeded = false;
		}
	}
	
	@Override
	public String toString() {
		return getRuleChangeType().toString() + " ";
	}
}
