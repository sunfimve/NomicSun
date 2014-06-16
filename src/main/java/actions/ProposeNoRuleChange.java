package actions;

import enums.RuleChangeType;
import agents.NomicAgent;

/**
 * 'Blank' rule change, often useful for agents that want to make predictions about the current state of
 * the game of Nomic. If an agent cannot decide what proposal to make in a timely manner, returning
 * an instance of ProposeNoRuleChange will constitute passing their turn.
 * @author Stuart Holland
 *
 */
public class ProposeNoRuleChange extends ProposeRuleChange {

	public ProposeNoRuleChange(NomicAgent proposer) {
		super(proposer);
		Type = RuleChangeType.NONE;
	}

}
