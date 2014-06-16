package agents;

import java.util.UUID;

import actions.ProposeRuleChange;
import enums.VoteType;

/**
 * 
 * @author Stuart Holland
 *
 */
public class ExampleAgent extends NomicAgent {

	public ExampleAgent(UUID id, String name) {
		super(id, name);
	}
	
	// ---------------------------------------------------------
	// New Agent AI recommended overrides
	// ---------------------------------------------------------
	
	@Override
	public VoteType chooseVote(ProposeRuleChange ruleChange, float intprob) {
		
		// Define your voting logic here. Most agents will follow the simple format shown in SelfishAgent.
		
		return super.chooseVote(ruleChange, intprob);
	}

	@Override
	protected ProposeRuleChange chooseProposal() {
		
		// Define your proposal selection logic here.
		
		return super.chooseProposal();
	}
	
	@Override
	public String getProxyRulesFile() {
		
		// Return a string with the file path to your agent AI's subsimulation preferences here.
		
		return super.getProxyRulesFile();
	}
	
	// ---------------------------------------------------------
	// New Agent AI optional overrides
	// ---------------------------------------------------------
	
	/*@Override
	public ProxyAgent getRepresentativeProxy() {
		
		// If you want this agent's proxy to have specific characteristics, define them here.
		// For an example of this in use, see the VindictiveAgent class.
		
		return super.getRepresentativeProxy();
	}*/
	
	@Override
	public void voteSucceeded(ProposeRuleChange ruleChange) {
		
		// If you have any operations that should occur when rule proposals succeed, do them here.
		
		super.voteSucceeded(ruleChange);
	}
	
	@Override
	public void voteFailed(ProposeRuleChange ruleChange) {
		
		// If you have any operations that should occur when rule proposals fail, do them here.
		// For an example of this function in use, see the VindictiveAgent class.
		
		super.voteFailed(ruleChange);
	}
}
