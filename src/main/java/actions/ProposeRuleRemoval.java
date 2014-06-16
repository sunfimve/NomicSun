package actions;

import enums.RuleChangeType;
import agents.NomicAgent;

/**
 * Rule change proposal action for removing an existing rule from the game.
 * @author Stuart Holland
 *
 */
public class ProposeRuleRemoval extends ProposeRuleChange {
	String oldRuleName, oldRulePackage;
	
	/**
	 * 
	 * @param agent Proposer of this change.
	 * @param oldRuleName
	 * @param oldRulePackage <code>RuleDefinition.RulePackage</code> is the package for all default and newly added rules.
	 */
	public ProposeRuleRemoval(NomicAgent agent, String oldRuleName, String oldRulePackage) {
		super(agent);
		this.oldRuleName = oldRuleName;
		this.oldRulePackage = oldRulePackage;
		this.Type = RuleChangeType.REMOVAL;
	}

	public String getOldRuleName() {
		return oldRuleName;
	}

	public String getOldRulePackage() {
		return oldRulePackage;
	}
	
	@Override
	public String toString() {
		return super.toString() + getOldRuleName();
	}
}
