package actions;

import agents.NomicAgent;
import enums.RuleChangeType;
import uk.ac.imperial.presage2.core.Action;

/**
 * Rule change proposal action for modifying an existing rule.
 * Modifications are effectively replacing an existing rule with a new one in a single rule change,
 * though convention says these rules should be related in some way.
 * @author Stuart Holland
 *
 */
public class ProposeRuleModification extends ProposeRuleChange implements Action {
	String newRuleName;
	
	String newRule;
	
	String oldRuleName;
	
	String oldRulePackage;

	/**
	 * 
	 * @param agent Proposer of this change
	 * @param newRuleName
	 * @param newRule
	 * @param oldRuleName
	 * @param oldRulePackage <code>RuleDefinition.RulePackage</code> is the package for all default and newly added rules.
	 */
	public ProposeRuleModification(NomicAgent agent, 
			String newRuleName, String newRule, String oldRuleName, String oldRulePackage) {
		super(agent);
		this.newRuleName = newRuleName;
		this.newRule = newRule;
		this.oldRuleName = oldRuleName;
		this.oldRulePackage = oldRulePackage;
		Type = RuleChangeType.MODIFICATION;
	}

	public String getNewRuleName() {
		return newRuleName;
	}
	
	public String getNewRule() {
		return newRule;
	}
	
	public String getOldRuleName() {
		return oldRuleName;
	}
	
	public String getOldRulePackage() {
		return oldRulePackage;
	}
	
	@Override
	public String toString() {
		return super.toString() + getOldRuleName() + " replaced by " + getNewRuleName();
	}
}
