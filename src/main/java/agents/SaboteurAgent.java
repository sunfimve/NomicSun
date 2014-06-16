package agents;

import java.io.IOException;
import java.util.UUID;

import actions.ProposeRuleChange;

import enums.AgentType;
import enums.VoteType;

public class SaboteurAgent extends NomicAgent {

	public SaboteurAgent(UUID id, String name) {
		super(id, name);
		Type = AgentType.SABOTEUR;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public VoteType chooseVote(ProposeRuleChange ruleChange, float intprob) {
		logger.info("Run subsimulation for rule query now.");
		try {
			scenarioService.RunQuerySimulation(ruleChange, getSubsimulationLength(ruleChange));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("This simulation had a preference of: " + scenarioService.getPreference());
		
		return super.strategyadvisor.getMyVote(ruleChange, intprob, scenarioService.getPreference());
	}
	
	@Override
	public String getProxyRulesFile() {
		return "src/main/resources/SaboteurProxy.drl";
	}
}
