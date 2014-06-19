package facts;

import java.util.ArrayList;

import enums.VoteType;

import services.NomicService;

import actions.ProposeRuleChange;
import actions.Vote;
import agents.NomicAgent;


/**
 * applies rule change when required by NomicService.
 * @author Hanguang Sun
 *
 */
public class RuleChangeApplier {
	
	NomicService superservice;
	

	public RuleChangeApplier(NomicService superservice) {
		super();
		this.superservice = superservice;
	}
	
	public float MoralityUpdate(ProposeRuleChange Proposal, NomicAgent TAgent, NomicAgent PVAgent, Vote vote){
		float finalvalue = 0;
		float Targetbonus = 0;
		float Systembonus = 0;
		float Basevalue = 2 * Proposal.getMoralChangeLevel();
		for ( NomicAgent A : superservice.getAgents()){
			Systembonus = Systembonus + A.getMorality();
		}
		Systembonus = (float) (Systembonus * (-1));
		if(TAgent != null){
			Targetbonus = Basevalue * (float) (TAgent.getMorality() * 0.5);
		}
		if(vote == null){
			finalvalue = (float) (0.5 * (Basevalue + Systembonus + Targetbonus));
		}
		else{
			if(vote.getVote() == VoteType.YES){
				finalvalue = (float) (Basevalue + Systembonus + Targetbonus) / superservice.GetNoActiveAgents();
			}
			else{
				finalvalue = - (float) (Basevalue + Systembonus + Targetbonus) / superservice.GetNoActiveAgents();
			}
		}
		if(PVAgent.Active == false){
			finalvalue = 0;
		}
		PVAgent.changeMorarlity(finalvalue);
		return finalvalue;

	}
	
	public void ApplyChange(ProposeRuleChange change){
		switch (change.getRuleValueType()){
		case HARVEST:
			switch (change.rate.RCType){
			case ADDSUB:
				float updatedrate = change.getTAgent().getHarvestrate() + change.rate.addsub;
				if(updatedrate < 0){
					change.getTAgent().setHarvestrate(0);
				}
				else{
					change.getTAgent().setHarvestrate(updatedrate);
				}
				break;
			case MODIFY:
				change.getTAgent().Modifiers.insertmodifier(change.rate.mod, change.getTimer());
				break;
			case NONE:
				break;
			default:
				break;
			}
			break;
		case NONE:
			break;
		case PARTICIPATION:
			switch (change.getpType()){
			case EXCLUDE:
				change.getTAgent().Exclude();
				break;
			case JOIN:
				change.getTAgent().Rejoin();
				break;
			default:
				break;
			
			}
			break;
		case RATEBOOST:
			switch (change.rate.RCType){
			case ADDSUB:
				float updatedrate = change.getTAgent().getHarvestrate() + change.rate.addsub;
				if(updatedrate < 0){
					change.getTAgent().setHarvestrate(0);
				}
				else{
					change.getTAgent().setHarvestrate(updatedrate);
				}
				break;
			case MODIFY:
				change.getTAgent().Modifiers.insertmodifier(change.rate.mod, change.getTimer());
				break;
			case NONE:
				break;
			default:
				break;
			}
			break;
		case REPLENISHMENTBOOST:
			switch (change.rate.RCType){
			case ADDSUB:
				float updatedrate = superservice.getReplenishment() + change.rate.addsub;
				if(updatedrate < 1){
					superservice.setReplenishment(1);
				}
				else{
					superservice.setReplenishment(updatedrate);
				}
				break;
			case MODIFY:
				superservice.repmod.insertmodifier(change.rate.mod, change.getTimer());
				break;
			case NONE:
				break;
			default:
				break;
			}
			break;
		case REPLENISHMENT:
			switch (change.rate.RCType){
			case ADDSUB:
				float updatedrate = superservice.getReplenishment() + change.rate.addsub;
				if(updatedrate < 1){
					superservice.setReplenishment(1);
				}
				else{
					superservice.setReplenishment(updatedrate);
				}
				break;
			case MODIFY:
				superservice.repmod.insertmodifier(change.rate.mod, change.getTimer());
				break;
			case NONE:
				break;
			default:
				break;
			}
			break;
		default:
			break;
		
		}
	}

	
}
