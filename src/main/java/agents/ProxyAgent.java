package agents;

import java.util.ArrayList;
import java.util.UUID;

import actions.ProposeRuleChange;
import actions.ProposeRuleRemoval;
import enums.RuleFlavor;
import enums.VoteType;
import facts.RuleDefinition;

public class ProxyAgent extends NomicAgent {
	
	private NomicAgent owner;
	
	private boolean Winner = false;
	
	private Integer preference = 50;
	
	private boolean preferenceLocked = false;
	
	private boolean avatar;

	public ProxyAgent(UUID id, String name) {
		super(id, name);
	}
	
	@Override
	public void Lose() {
		Winner = true;
		super.Lose();
	}
	
	public void setOwner(NomicAgent owner) {
		this.owner = owner;
	}
	
	public NomicAgent getOwner() {
		return owner;
	}
	
	public UUID GetOwnerID() {
		return owner.getID();
	}
	
	public boolean isWinner() {
		return Winner;
	}

	public void setWinner(boolean winner) {
		Winner = winner;
	}

	@Override
	public String getProxyRulesFile() {
		return owner.getProxyRulesFile();
	}
	
	@Override
	protected ProposeRuleChange chooseProposal() {

		
		return super.chooseProposal();
	}
	
	@Override
	public VoteType chooseVote(ProposeRuleChange ruleChange, float intprob) {
		return super.chooseVote(ruleChange, intprob);
	}
	
	/**
	 * Returns true if this agent is the representative of the agent that created
	 * the subsimulation.
	 * @return
	 */
	public boolean IsAvatar() {
		return avatar;
	}
	
	public void SetAvatar(boolean avatar) {
		this.avatar = avatar;
	}

	public Integer getPreference() {
		return preference;
	}

	public void setPreference(Integer preference) {
		if (!preferenceLocked)
			this.preference = preference;
	}
	
	public void increasePreference(Integer amount) {
		if (!preferenceLocked)
			this.preference += amount;
	}
	
	public void decreasePreference(Integer amount) {
		if (!preferenceLocked)
			this.preference -= amount;
	}

	/**
	 * If true, preference will not be changed by setPreference(Integer),
	 * increasePreference(Integer), or decreasePreference(Integer)
	 * @return
	 */
	public boolean isPreferenceLocked() {
		return preferenceLocked;
	}

	/**
	 * Fixes current preference value when set to true.
	 * Useful if certain events fix preference at some value, regardless of what happens after.
	 * @param preferenceLocked
	 */
	public void setPreferenceLocked(boolean preferenceLocked) {
		this.preferenceLocked = preferenceLocked;
	}
}
