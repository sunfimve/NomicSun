package facts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import actions.ProposeRuleAddition;
import actions.ProposeRuleChange;
import actions.ProposeRuleModification;
import agents.NomicAgent;
import enums.RuleFlavor;
import exceptions.InvalidRuleStateException;

/**
 * Contains all relevant information about a specific rule of Nomic.
 * @author Stuart Holland
 *
 */
public class RuleDefinition {
	/**
	 * Package for all default rules and any rules added dynamically during a simulation.
	 */
	public static final String RulePackage = "defaultpkg";
	
	/**
	 * The name of the rule, as recognized by Drools. Must match the name specified in ruleContent.
	 */
	String name;
	/**
	 * The body of that rule, formatted to be wrapped in a StringReader and parsed by the Drools engine
	 */
	String ruleContent;
	/**
	 * A list of all rules (if any) that this rule 'replaces' or modifies.
	 */
	ArrayList<RuleDefinition> replacedRules;
	/**
	 * The flavors that define this rule's characteristics. For reference of flavor characteristics, see <code>RuleFlavor</code>.
	 */
	Map<RuleFlavor, Integer> Flavors;
	
	//RuleModificationType Ruletype;
	
	NomicAgent RuleSubject;
	
	int duration = 0;
	
	float Rulevalue;
	/**
	 * Whether or not it is making Agent active to play, (and potentially if rate is variable).
	 */
	boolean RulePositive;
	/**
	 * Whether or not this rule is currently active.
	 */
	boolean Active;
	
	public RuleDefinition(String name, String ruleContent) {
		this.name = name;
		this.ruleContent = ruleContent;
		
		replacedRules = new ArrayList<RuleDefinition>();
		Flavors = new HashMap<RuleFlavor, Integer>();
	}
	
	/**
	 * Automatically generates a valid addition or modification proposal based on this rule definition,
	 * ready for an agent to propose immediately. 
	 * Rule removals must be constructed manually.
	 * @param proposer The agent to propose the change.
	 * @return A valid rule change action corresponding to this definition.
	 */
	public ProposeRuleChange getRuleChange(NomicAgent proposer) {
		// This could be made to work out removals too by using isActive() to work out when
		// it should remove itself.
		if (isReplacesOther()) {
			String replacedName = null;
			
			for (RuleDefinition replacedDef : replacedRules) {
				if (replacedDef.isActive())
					replacedName = replacedDef.getName();
			}
			
			// the replaced rules might all have been removed
			if (replacedName == null) {
				return new ProposeRuleAddition(proposer, name, ruleContent);
			}
			
			return new ProposeRuleModification(proposer, name, ruleContent, replacedName, RulePackage);
		}
		else {
			return new ProposeRuleAddition(proposer, name, ruleContent);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRuleContent() {
		return ruleContent;
	}

	public void setRuleContent(String ruleContent) {
		this.ruleContent = ruleContent;
	}
	
	public int getFlavorAmount(RuleFlavor flavorType) {
		return Flavors.get(flavorType);
	}
	
	/**
	 * Finds the flavor with the highest value for this rule.
	 * @return
	 */
	public RuleFlavor getPrevailingFlavor() {
		Integer maxFlavor = 50;
		RuleFlavor prevailingFlavor = null;
		for (RuleFlavor flavor : Flavors.keySet()) {
			
			Integer currentFlavor = Flavors.get(flavor);
			if (currentFlavor > maxFlavor) {
				maxFlavor = currentFlavor;
				prevailingFlavor = flavor;
			}
		}
		
		return prevailingFlavor;
	}
	
	/**
	 * Finds the flavor with the lowest value for this rule.
	 * @return
	 */
	public RuleFlavor getOpposingFlavor() {
		Integer minFlavor = 50;
		RuleFlavor lowFlavor = null;
		
		for (RuleFlavor flavor : Flavors.keySet()) {
			Integer currentFlavor = Flavors.get(flavor);
			
			if (currentFlavor < minFlavor) {
				minFlavor = currentFlavor;
				lowFlavor = flavor;
			}
		}
		
		return lowFlavor;
	}
	
	/**
	 * True if the flavor amount for this rule of the parameter flavor is greater than 50.
	 * @param flavor
	 * @return
	 */
	public boolean isHasPositiveFlavor(RuleFlavor flavor) {
		return Flavors.get(flavor) > 50;
	}
	
	/**
	 * Returns true if all flavors in the parameter collection have non-negative values (>= 50) for this rule.
	 * @param flavors
	 * @return
	 */
	public boolean isHasPositiveForFlavors(Collection<RuleFlavor> flavors) {
		for (RuleFlavor flavor : flavors) {
			if (Flavors.get(flavor) < 50)
				return false;
		}
		
		return true;
	}
	
	public void setFlavorAmount(RuleFlavor flavorType, Integer amount) {
		Flavors.put(flavorType, amount);
	}
	
	public void setFlavors(Integer complex, Integer destructive, Integer simple,
			Integer desperation, Integer beneficial, Integer wincondition,
			Integer stable, Integer detrimental) {
		Flavors.put(RuleFlavor.COMPLEX, complex);
		Flavors.put(RuleFlavor.DESTRUCTIVE, destructive);
		Flavors.put(RuleFlavor.SIMPLE, simple);
		Flavors.put(RuleFlavor.DESPERATION, desperation);
		Flavors.put(RuleFlavor.BENEFICIAL, beneficial);
		Flavors.put(RuleFlavor.WINCONDITION, wincondition);
		Flavors.put(RuleFlavor.STABLE, stable);
		Flavors.put(RuleFlavor.DETRIMENTAL, detrimental);
	}
	
	public Map<RuleFlavor, Integer> getFlavors() {
		return Flavors;
	}

	/**
	 * True if this rule replaces another rule (whether or not that rule is currently active)
	 * @return
	 */
	public boolean isReplacesOther() {
		return replacedRules.size() > 0;
	}
	
	/**
	 * True if this rule replaces the rule with the parameter name.
	 * @param oldRuleName
	 * @return
	 */
	public boolean isReplaces(String oldRuleName) {
		for (RuleDefinition definition : replacedRules) {
			if (definition.getName().equals(oldRuleName))
				return true;
		}
		
		return false;
	}
	
	/**
	 * True if this rule replaces any of the rules in the parameter list.
	 * @param rules
	 * @return
	 */
	public boolean isReplacesAny(ArrayList<RuleDefinition> rules) {
		if (!isReplacesOther())
			return false;
		
		for (RuleDefinition definition : rules) {
			if (isReplaces(definition.getName()))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Thresholded check for this rule's propensity toward the parameter flavor.
	 * Basically, it's more strict than "isHasPositiveFlavor", requiring a higher
	 * affinity for the given flavor to return true.
	 * @param flavor
	 * @return
	 */
	public boolean is(RuleFlavor flavor) {
		return Flavors.get(flavor) > 67;
	}
	
	/**
	 * Counterpart to "is", can be used to work out if this rule is relatively neutral to the
	 * given flavor  
	 * @param flavor
	 * @return
	 */
	public boolean isNot(RuleFlavor flavor) {
		return Flavors.get(flavor) < 33;
	}
	
	/**
	 * If this rule is within an acceptable threshold of 50 propensity toward the given flavor,
	 * returns true.
	 * @param flavor
	 * @return
	 */
	public boolean isNeutral(RuleFlavor flavor) {
		return !is(flavor) && !isNot(flavor);
	}
	
	/**
	 * Registers this rule as replacing the parameter rule. (Relationship should be reflexive in mot normal cases.)
	 * @param rule
	 */
	public void addReplacedRule(RuleDefinition rule) {
		replacedRules.add(rule);
	}

	public boolean isActive() {
		return Active;
	}

	public void setActive(boolean active) {
		Active = active;
	}
}
