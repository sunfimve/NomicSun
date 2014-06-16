package actions;

import agents.NomicAgent;
import enums.RuleChangeType;

/**
 * Rule change proposal action for adding a new rule.
 * @author Stuart Holland
 *
 */
public class ProposeRuleAddition extends ProposeRuleChange {
	String newRuleName;
	
	String newRule;
	
	public ProposeRuleAddition(NomicAgent agent, String newRuleName, String newRule) {
		super(agent);
		this.newRuleName = newRuleName;
		this.newRule = newRule;
		Type = RuleChangeType.ADDITION;
	}
	
	public String getNewRuleName() {
		return newRuleName;
	}
	
	public String getNewRule() {
		return newRule;
	}
	
	@Override
	public String toString() {
		return super.toString() + getNewRuleName();
	}
}
