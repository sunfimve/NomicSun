package facts;

import java.util.ArrayList;
//import java.util.Optional;

import services.NomicService;

import actions.ProposeRuleChange;


/**
 * converts a multi-attribute expressed rule into a string
 * @author Hanguang Sun
 *
 */
public class ProposalReader {
	
	String output;
	
	int NumofAgents;
	
	ProposeRuleChange change;
	
	String Avatar;

	public ProposalReader(String Avatar, int NA) {
		super();
		this.Avatar = Avatar;
		this.NumofAgents = NA;
	}
	
	public String ReadProposal(ProposeRuleChange change){
		String out;
		if(Avatar == "log"){
			out = " ";
		}
		else{
			out = Avatar + "'s Reader: the current proposal is: \n";
		}
//		System.out.println(change);
//		System.out.println(change.getRuleValueType());
		
		switch (change.getRuleValueType()){
		case HARVEST:
			switch (change.rate.RCType){
			case ADDSUB:
				out = out + "To add/subtract " + change.getTAgent().getName() + "'s harvest rate by " + change.rate.addsub;
				break;
			case MODIFY:
				out = out + "To apply a modifier on " + change.getTAgent().getName() + "that is " + change.rate.mod + " for " + change.getTimer()/NumofAgents;
				break;
			case NONE:
				out = out + "No change at all :(";
				break;
			default:
				out = out + "No change at all :(";
				break;
			}
			break;
		case NONE:
			break;
		case PARTICIPATION:
			switch (change.getpType()){
			case EXCLUDE:
				out = out + "To exclude " + change.getTAgent().getName() + " from the game.";
				break;
			case JOIN:
				out = out + "To invite " + change.getTAgent().getName() + " back to the game.";
				break;
			default:
				break;
			
			}
			break;
		case RATEBOOST:
			switch (change.rate.RCType){
			case ADDSUB:
				out = out + "To add/subtract " + change.getTAgent().getName() + "'s harvest rate by " + change.rate.addsub;
				break;
			case MODIFY:
				out = out + "To apply a modifier on " + change.getTAgent().getName() + "that is " + change.rate.mod + " for " + change.getTimer()/NumofAgents + " turns.";
				break;
			case NONE:
				out = out + "No change at all :(";
				break;
			default:
				out = out + "No change at all :(";
				break;
			}
			break;
		case REPLENISHMENTBOOST:
			switch (change.rate.RCType){
			case ADDSUB:
				out = out + "To add/subtract " + change.rate.addsub + " to the replenishment rate.";
				break;
			case MODIFY:
				out = out + "To apply a modifier on replenishment that is " + + change.rate.mod + " for " + change.getTimer()/NumofAgents + " turns.";
				break;
			case NONE:
				out = out + "No change at all :(";
				break;
			default:
				out = out + "No change at all :(";
				break;
			}
			break;
		case REPLENISHMENT:
			switch (change.rate.RCType){
			case ADDSUB:
				out = out + "To add/subtract " + change.rate.addsub + " to the replenishment rate.";
				break;
			case MODIFY:
				out = out + "To apply a modifier on replenishment that is " + + change.rate.mod + " for " + change.getTimer()/NumofAgents + " turns.";
				break;
			case NONE:
				out = out + "No change at all :(";
				break;
			default:
				out = out + "No change at all :(";
				break;
			}
			break;
		default:
			out = out + "No change at all :(";
			break;
		
		}
		return out;
	}

	public String ReadProposal(ProposeRuleChange change,int noa){
		String out;
		if(Avatar == "log"){
			out = " ";
		}
		else{
			out = Avatar + "'s Reader: the current proposal is: \n";
		}
		switch (change.getRuleValueType()){
		case HARVEST:
			switch (change.rate.RCType){
			case ADDSUB:
				out = out + "To add/subtract " + change.getTAgent().getName() + "'s harvest rate by " + change.rate.addsub;
				break;
			case MODIFY:
				out = out + "To apply a modifier on " + change.getTAgent().getName() + "that is " + change.rate.mod + " for " + change.getTimer()/noa;
				break;
			case NONE:
				out = out + "No change at all :(";
				break;
			default:
				out = out + "No change at all :(";
				break;
			}
			break;
		case NONE:
			break;
		case PARTICIPATION:
			switch (change.getpType()){
			case EXCLUDE:
				out = out + "To exclude " + change.getTAgent().getName() + " from the game.";
				break;
			case JOIN:
				out = out + "To invite " + change.getTAgent().getName() + " back to the game.";
				break;
			default:
				break;
			
			}
			break;
		case RATEBOOST:
			switch (change.rate.RCType){
			case ADDSUB:
				out = out + "To add/subtract " + change.getTAgent().getName() + "'s harvest rate by " + change.rate.addsub;
				break;
			case MODIFY:
				out = out + "To apply a modifier on " + change.getTAgent().getName() + "that is " + change.rate.mod + " for " + change.getTimer()/noa + " turns.";
				break;
			case NONE:
				out = out + "No change at all :(";
				break;
			default:
				out = out + "No change at all :(";
				break;
			}
			break;
		case REPLENISHMENTBOOST:
			switch (change.rate.RCType){
			case ADDSUB:
				out = out + "To add/subtract " + change.rate.addsub + " to the replenishment rate.";
				break;
			case MODIFY:
				out = out + "To apply a modifier on replenishment that is " + + change.rate.mod + " for " + change.getTimer()/noa + " turns.";
				break;
			case NONE:
				out = out + "No change at all :(";
				break;
			default:
				out = out + "No change at all :(";
				break;
			}
			break;
		case REPLENISHMENT:
			switch (change.rate.RCType){
			case ADDSUB:
				out = out + "To add/subtract " + change.rate.addsub + " to the replenishment rate.";
				break;
			case MODIFY:
				out = out + "To apply a modifier on replenishment that is " + + change.rate.mod + " for " + change.getTimer()/noa + " turns.";
				break;
			case NONE:
				out = out + "No change at all :(";
				break;
			default:
				out = out + "No change at all :(";
				break;
			}
			break;
		default:
			out = out + "No change at all :(";
			break;
		
		}
		return out;
	}
	
	
	
}
