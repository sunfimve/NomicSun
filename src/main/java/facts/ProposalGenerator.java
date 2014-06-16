package facts;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import services.NomicService;

import enums.ParticipationProposalType;
import enums.PurposeType;
import enums.RateChangeType;
import enums.RuleChangeType;
import enums.RuleValueType;
import actions.ProposeRuleChange;
import agents.NomicAgent;

/**
 *	generates proposals for strategy service by some factors of random.
 * @author Hanguang Sun
 *
 */
public class ProposalGenerator {
	
	NomicAgent controller;
	
	NomicService nomicService;
	
	Random rand = new Random();
	
	private NomicAgent getrichone() throws NullPointerException{
		NomicAgent richone = null;
		float highestcurrentincome = 0;
		for ( NomicAgent a : nomicService.getAgents()){
			if (a.mycurrentrate() > highestcurrentincome){
				highestcurrentincome = a.mycurrentrate();
				richone = a;
			}
			
		}
		return richone;
	}
	
	private ProposeRuleChange eligibilityfilter(ProposeRuleChange input){
//		System.out.println(nomicService.getAgents());
		ProposeRuleChange output = input;
		if (controller.Active == false){
			output.setTAgent(controller);
			Map<NomicAgent,PurposeType> purposes;
			purposes = new HashMap<NomicAgent,PurposeType>();
			for (NomicAgent CA : nomicService.getAgents()){
				
				if(CA == controller){
					purposes.put(CA, PurposeType.REINVATTEMPT);
				}
				else{
					purposes.put(CA, PurposeType.REINVATTEMPT_OTHERS);
				}
			}
			output.setValueType(RuleValueType.PARTICIPATION);
			output.setpType(ParticipationProposalType.JOIN);
			output.setPurposes(purposes);
		}
		output.setRuleChangeType(RuleChangeType.MODIFICATION);
		return output;
	}

	public ProposeRuleChange increaserate(NomicAgent Target){
		ProposeRuleChange advice = new ProposeRuleChange(controller);
		advice.setTAgent(Target);
		Map<NomicAgent,PurposeType> purposes;
		purposes = new HashMap<NomicAgent,PurposeType>();
		for (NomicAgent CA : nomicService.getAgents()){
			if(CA == Target){
				purposes.put(CA, PurposeType.RAISEMYGAIN);
			}
			else{
				purposes.put(CA, PurposeType.RAISEOTHERSGAIN);
			}
		}
		int increase = 1+ rand.nextInt(3);
		advice.setValueType(RuleValueType.HARVEST);
		advice.rate.RCType = RateChangeType.ADDSUB;
		advice.rate.addsub = increase;
		advice.setPurposes(purposes);
		return eligibilityfilter(advice);
	}
	
	public ProposeRuleChange decreaserate(NomicAgent Target){
		ProposeRuleChange advice = new ProposeRuleChange(controller);
		advice.setTAgent(Target);
		Map<NomicAgent,PurposeType> purposes;
		purposes = new HashMap<NomicAgent,PurposeType>();
		for (NomicAgent CA : nomicService.getAgents()){
			if(CA == Target){
				purposes.put(CA, PurposeType.SUPRESSMYGAIN);
			}
			else{
				purposes.put(CA, PurposeType.SUPPRESSOTHERSGAIN);
			}
		}
		int decrease = 1+ rand.nextInt(3);
		advice.setValueType(RuleValueType.HARVEST);
		advice.rate.RCType = RateChangeType.ADDSUB;
		advice.rate.addsub = - decrease;
		advice.setPurposes(purposes);
		return eligibilityfilter(advice);
	}

	public ProposeRuleChange boostrate(NomicAgent Target){
		ProposeRuleChange advice = new ProposeRuleChange(controller);
		advice.setTAgent(Target);
		Map<NomicAgent,PurposeType> purposes;
		purposes = new HashMap<NomicAgent,PurposeType>();
		for (NomicAgent CA : nomicService.getAgents()){
			if(CA == Target){
				purposes.put(CA, PurposeType.RAISEMYGAIN);
			}
			else{
				purposes.put(CA, PurposeType.RAISEOTHERSGAIN);
			}
		}
		float Boostfold = 2 + (float)rand.nextInt(3);
		advice.setValueType(RuleValueType.RATEBOOST);
		advice.rate.RCType = RateChangeType.MODIFY;
		advice.rate.mod = Boostfold;
		advice.setTimer((2+rand.nextInt(3)) * nomicService.getNumberOfAgents());
		advice.setPurposes(purposes);
		return eligibilityfilter(advice);
	}
	
	public ProposeRuleChange supressrate(NomicAgent Target){
		ProposeRuleChange advice = new ProposeRuleChange(controller);
		advice.setTAgent(Target);
		Map<NomicAgent,PurposeType> purposes;
		purposes = new HashMap<NomicAgent,PurposeType>();
		for (NomicAgent CA : nomicService.getAgents()){
			if(CA == Target){
				purposes.put(CA, PurposeType.SUPRESSMYGAIN);
			}
			else{
				purposes.put(CA, PurposeType.SUPPRESSOTHERSGAIN);
			}
		}
		float Boostfold = 1;
		int R = rand.nextInt(4);
		if (R == 3){
			Boostfold = 0;
			advice.setMoralChangeLevel(-1);
		}
		else{
			Boostfold = 1 / (2 + (float)R);
		}
		advice.setValueType(RuleValueType.RATEBOOST);
		advice.rate.RCType = RateChangeType.MODIFY;
		advice.rate.mod = Boostfold;
		advice.setTimer((2+rand.nextInt(3)) * nomicService.getNumberOfAgents());
		advice.setPurposes(purposes);
		return eligibilityfilter(advice);
	}

	public ProposeRuleChange increasereplenishmentrate(){
		ProposeRuleChange advice = new ProposeRuleChange(controller);
		int increase = 1+ rand.nextInt(3);
		Map<NomicAgent,PurposeType> purposes;
		purposes = new HashMap<NomicAgent,PurposeType>();
		for (NomicAgent CA : nomicService.getAgents()){
			purposes.put(CA, PurposeType.RAISEREPLENISH);
		}
		advice.setValueType(RuleValueType.REPLENISHMENT);
		advice.rate.RCType = RateChangeType.ADDSUB;
		advice.rate.addsub = increase;
		advice.setPurposes(purposes);
		return eligibilityfilter(advice);
	}

	public ProposeRuleChange decreasereplenishmentrate(){
		ProposeRuleChange advice = new ProposeRuleChange(controller);
		Map<NomicAgent,PurposeType> purposes;
		purposes = new HashMap<NomicAgent,PurposeType>();
		for (NomicAgent CA : nomicService.getAgents()){
			purposes.put(CA, PurposeType.SUPRESSREPLENISH);
		}
		int decrease = 1+ rand.nextInt(3);
		advice.setValueType(RuleValueType.REPLENISHMENT);
		advice.rate.RCType = RateChangeType.ADDSUB;
		advice.rate.addsub = - decrease;
		advice.setPurposes(purposes);
		return eligibilityfilter(advice);
	}


	public ProposeRuleChange boostreplenishmentrate(){
		ProposeRuleChange advice = new ProposeRuleChange(controller);
		Map<NomicAgent,PurposeType> purposes;
		purposes = new HashMap<NomicAgent,PurposeType>();
		for (NomicAgent CA : nomicService.getAgents()){
			purposes.put(CA, PurposeType.RAISEREPLENISH);
		}
		float Boostfold = 2 + (float)rand.nextInt(3);
		advice.setValueType(RuleValueType.REPLENISHMENTBOOST);
		advice.rate.RCType = RateChangeType.MODIFY;
		advice.rate.mod = Boostfold;
		advice.setTimer((2+rand.nextInt(3)) * nomicService.getNumberOfAgents());
		advice.setPurposes(purposes);
		return eligibilityfilter(advice);
	}
	
	public ProposeRuleChange suppressreplenishmentrate(){
		ProposeRuleChange advice = new ProposeRuleChange(controller);
		Map<NomicAgent,PurposeType> purposes;
		purposes = new HashMap<NomicAgent,PurposeType>();
		for (NomicAgent CA : nomicService.getAgents()){
			purposes.put(CA, PurposeType.SUPRESSREPLENISH);
		}
		float Boostfold = 1;
		int R = rand.nextInt(4);
		if (R == 3){
			Boostfold = 0;
			advice.setMoralChangeLevel(-1);
		}
		else{
			Boostfold = 1 / (2 + (float)R);
		}
		advice.setValueType(RuleValueType.REPLENISHMENTBOOST);
		advice.rate.RCType = RateChangeType.MODIFY;
		advice.rate.mod = Boostfold;
		advice.setTimer((2+rand.nextInt(3)) * nomicService.getNumberOfAgents());
		advice.setPurposes(purposes);
		return eligibilityfilter(advice);
	}

	public ProposeRuleChange excludeplayer(NomicAgent Target){
		ProposeRuleChange advice = new ProposeRuleChange(controller);
		advice.setTAgent(Target);
		Map<NomicAgent,PurposeType> purposes;
		purposes = new HashMap<NomicAgent,PurposeType>();
		for (NomicAgent CA : nomicService.getAgents()){
			if(CA == Target){
				purposes.put(CA, PurposeType.REMOVALATTEMPT);
			}
			else{
				purposes.put(CA, PurposeType.REMOVALATTEMPT_OTHERS);
			}
		}
		advice.setValueType(RuleValueType.PARTICIPATION);
		advice.setpType(ParticipationProposalType.EXCLUDE);
		advice.setMoralChangeLevel(-2);
		advice.setPurposes(purposes);
		return eligibilityfilter(advice);
	}
	
	public ProposeRuleChange rejoinplayer(NomicAgent Target){
		ProposeRuleChange advice = new ProposeRuleChange(controller);
		advice.setTAgent(Target);
		Map<NomicAgent,PurposeType> purposes;
		purposes = new HashMap<NomicAgent,PurposeType>();
		for (NomicAgent CA : nomicService.getAgents()){
			if(CA == Target){
				purposes.put(CA, PurposeType.REINVATTEMPT);
			}
			else{
				purposes.put(CA, PurposeType.REINVATTEMPT_OTHERS);
			}
		}
		advice.setValueType(RuleValueType.PARTICIPATION);
		advice.setpType(ParticipationProposalType.JOIN);
		advice.setMoralChangeLevel(2);
		advice.setPurposes(purposes);
		return eligibilityfilter(advice);
	}
	
	
	public ProposeRuleChange randomparticipation(){
		ProposeRuleChange advice = new ProposeRuleChange(controller);
		NomicAgent Target = nomicService.getRandAgent();
		advice.setValueType(RuleValueType.PARTICIPATION);
		if(Target.Active){
			advice = this.excludeplayer(Target);
		}
		else{
			advice = this.rejoinplayer(Target);
		}
		advice.setTAgent(Target);
		return eligibilityfilter(advice);
	}
	
	public ProposeRuleChange raisemygain(){
		ProposeRuleChange advice = new ProposeRuleChange(controller);
		if (rand.nextInt(2) == 0){
			advice = this.increaserate(controller);
		}
		else {
			advice = this.boostrate(controller);
		}
		return eligibilityfilter(advice);
	}
	
	public ProposeRuleChange lowergain(NomicAgent Target){
		ProposeRuleChange advice = new ProposeRuleChange(controller);
		if (rand.nextInt(2) == 0){
			advice = this.decreaserate(Target);
		}
		else {
			advice = this.supressrate(Target);
		}
		return eligibilityfilter(advice);
	}
	
	public ProposeRuleChange lowerrichonegain(){
		ProposeRuleChange advice = new ProposeRuleChange(controller);
		if (rand.nextInt(2) == 0){
			advice = this.decreaserate(getrichone());
		}
		else {
			advice = this.supressrate(getrichone());
		}
		return eligibilityfilter(advice);
	}
	
	public ProposeRuleChange raisereplenishment(){
		ProposeRuleChange advice = new ProposeRuleChange(controller);
		if (rand.nextInt(2) == 0){
			advice = this.increasereplenishmentrate();
		}
		else {
			advice = this.boostreplenishmentrate();
		}
		return eligibilityfilter(advice);
	}
	
	public ProposeRuleChange lowerreplenishment(){
		ProposeRuleChange advice = new ProposeRuleChange(controller);
		if (rand.nextInt(2) == 0){
			advice = this.decreasereplenishmentrate();
		}
		else {
			advice = this.suppressreplenishmentrate();
		}
		return eligibilityfilter(advice);
	}
	
	public ProposeRuleChange randomrate(){
		ProposeRuleChange advice = new ProposeRuleChange(controller);
		NomicAgent Target = nomicService.getRandAgent();
		//half probability to do rate add/sub, ow boost/suppress
		if(rand.nextInt(2) == 0){
			if(rand.nextInt(nomicService.GetNoActiveAgents() + 1) == 0){
				if(rand.nextInt(2) == 0){
					advice = this.increasereplenishmentrate();
				}
				else{
					advice = this.decreasereplenishmentrate();
				}
			}
			else{
				if(rand.nextInt(2) == 0){
					advice = this.increaserate(Target);
				}
				else{
					advice = this.decreaserate(Target);
				}
			}
		}
		else{
			if(rand.nextInt(nomicService.GetNoActiveAgents()) == 0){
				if(rand.nextInt(2) == 0){
					advice = this.boostreplenishmentrate();
				}
				else{
					advice = this.suppressreplenishmentrate();
				}
			}
			else{
				if(rand.nextInt(2) == 0){
					advice = this.boostrate(Target);
				}
				else{
					advice = this.supressrate(Target);
				}
			}
		}
		return eligibilityfilter(advice);
	}
	
	
	public ProposeRuleChange randomall(float p){
		//p stands for the probability to choose a rate-related proposal, ow participation related.
		ProposeRuleChange advice = new ProposeRuleChange(controller);
		if(rand.nextFloat() < p){
			advice = this.randomrate();
		}
		else{
			advice = this.randomparticipation();
		}
		return eligibilityfilter(advice);
	}

	public ProposalGenerator(NomicAgent controller, NomicService nomicService) {
		super();
		this.controller = controller;
		this.nomicService = nomicService;
	}


}