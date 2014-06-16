package facts;

import agents.NomicAgent;

/**
 * Dynamic rule example. Creates a rule that makes an agent in the
 * current simulation win the game as soon as it is voted in.
 * @author Stuart Holland
 *
 */
public class RandomAgentWinsRule extends DynamicRuleDefinition {
	
	public RandomAgentWinsRule(String name) {
		super(name);
	}

	public RandomAgentWinsRule(String name, String ruleContent) {
		super(name, ruleContent);
	}

	@Override
	protected String generateRuleName(NomicAgent proposer) {
		return proposer.getName() + " wins";
	}

	@Override
	protected String generateRuleBody(NomicAgent proposer) {
		return "import agents.NomicAgent; "
				+ "import facts.*; "
				+ "global org.apache.log4j.Logger logger "
				+ "rule \"" + name + "\""
				+ "when "
				+ 	"$agent : NomicAgent(getName() == \"" + proposer.getName() + "\") "
				+ "then "
				+	"logger.info(\"" + generateRuleName(proposer) + "\"); "
				+	"insert(new Win($agent)); "
				+ "end";
	}

}
