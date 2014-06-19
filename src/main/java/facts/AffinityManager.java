package facts;


import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import actions.ProposeRuleChange;
import actions.Vote;
import agents.NomicAgent;

import enums.VoteType;
import facts.Affinity;

public class AffinityManager {
	
	enum TargetFlavor{
		ME, LIKED, DISLIKED;
	}

	private final Logger logger = Logger.getLogger(this.getClass());
	
	private ArrayList<NomicAgent> agents;

	private NomicAgent Avatar;
	
	private Random random = new Random();
	
	private ArrayList<Affinity> AffinityTable = new ArrayList<Affinity>();
//	private Map<NomicAgent,Affinity> AffinityTable;
	
	public AffinityManager(ArrayList<NomicAgent> a, NomicAgent s){
		super();
		agents = a;
		Avatar = s;
//		for(NomicAgent sagent : a){
//			ArrayList<NomicAgent> r = a;
//			r.remove(Avatar);
			for(NomicAgent tagent : a){
				Affinity aff = new Affinity(tagent,50);
				AffinityTable.add(aff);
			}
//		}
	}
	
	public NomicAgent getHated(){
//		hating self means no concept about others yet, or any other	players fond. This leads to quitting the idea of proposing to remove someone.	
		NomicAgent Hated = Avatar;
		int currentleastaff = 50;
		for (Affinity CA : AffinityTable){
			if( CA.Aff < currentleastaff ){
				Hated = CA.Target;
				currentleastaff = CA.Aff;
			}
		}
		return Hated;
	}
	
	public NomicAgent getFavored(){
		NomicAgent Favored = Avatar;
		int currentmostaff = 50;
		for (Affinity CA : AffinityTable){
			//difference is that only suspended agents are able to re-invite.
			if( CA.Aff > currentmostaff && CA.Target.Active == false){
				Favored = CA.Target;
				currentmostaff = CA.Aff;
			}
		}
		return Favored;
	}
	
	public int getAffinity(NomicAgent a){
		int result = 50;
		int count = 0;
		for (Affinity aff : AffinityTable){
			if (aff.Target == a){
				result = aff.Aff;
				++count;
			}
		}
		if (count == 0){
			logger.warn(Avatar + ": that agent is not found in my opinion.");
			Affinity makeup = new Affinity(a,50);
			AffinityTable.add(makeup);
		}
		else if (count > 1){
			logger.warn("error: duplicated information");
			for (Affinity aff : AffinityTable){
				if (aff.Target == a){
					AffinityTable.remove(aff);
				}
			}
			Affinity makeup = new Affinity(a,50);
			AffinityTable.add(makeup);
		}
		else{}
		return result;
		
	}
	
	public float getAverageAffinity(){
		float result = 50;
		int count = 0;
		int itm = 0;
		for (Affinity aff : AffinityTable){
			if (aff.Target != Avatar){
				itm = itm + aff.Aff;
				++count;
			}
		}
		if(count != 0){
		result = itm / count;
		}
		return result;
	}
	
	public void changeAffinity(NomicAgent a, int change){
		int count = 0;
		for (Affinity aff : AffinityTable){
			if (aff.Target == a){
				aff.Aff = aff.Aff + change;
				if(aff.Aff < 0){
					aff.Aff = 0;
				}
				if(aff.Aff > 100){
					aff.Aff = 100;
				}
				++count;
			}
		}
		if (count == 0){
			logger.warn(Avatar + ": that agent is not found in my opinion.");
			Affinity makeup = new Affinity(a,50 + change);
			AffinityTable.add(makeup);
		}
		else if (count > 1){
			logger.warn("error: duplicated information");
			for (Affinity aff : AffinityTable){
				if (aff.Target == a){
					AffinityTable.remove(aff);
				}
			}
			Affinity makeup = new Affinity(a,50 + change);
			AffinityTable.add(makeup);
		}
		else{}
		
	}
	
	public boolean isFavored(NomicAgent a){
		return this.getAffinity(a) >= 50;
	}

	public void updateAffinity(ProposeRuleChange p){
//		logger.info(Avatar);
//		logger.info(p.getPurposes());
		switch (p.getPurposes().get(Avatar)){
		case RAISEMYGAIN:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				changeAffinity(p.getProposer(),0);
				break;
			case INVESTOR:
				changeAffinity(p.getProposer(),5);
				break;
			case MISER:
				changeAffinity(p.getProposer(),9);
				break;
			case NONE:
				changeAffinity(p.getProposer(),1);
				break;
			case SABOTEUR:
				changeAffinity(p.getProposer(),3);
				break;
			default:
				break;
			}
			break;
		case RAISEOTHERSGAIN:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				changeAffinity(p.getProposer(),-4);
				break;
			case INVESTOR:
				changeAffinity(p.getProposer(),-2);
				break;
			case MISER:
				changeAffinity(p.getProposer(),-5);
				break;
			case NONE:
				changeAffinity(p.getProposer(),-1);
				break;
			case SABOTEUR:
				changeAffinity(p.getProposer(),2);
				break;
			default:
				break;
			}
			break;
		case RAISEREPLENISH:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				changeAffinity(p.getProposer(),10);
				break;
			case INVESTOR:
				changeAffinity(p.getProposer(),5);
				break;
			case MISER:
				changeAffinity(p.getProposer(),1);
				break;
			case NONE:
				changeAffinity(p.getProposer(),1);
				break;
			case SABOTEUR:
				changeAffinity(p.getProposer(),-12);
				break;
			default:
				break;
			}
			break;
		case REINVATTEMPT:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				changeAffinity(p.getProposer(),25);
				break;
			case INVESTOR:
				changeAffinity(p.getProposer(),20);
				break;
			case MISER:
				changeAffinity(p.getProposer(),20);
				break;
			case NONE:
				changeAffinity(p.getProposer(),20);
				break;
			case SABOTEUR:
				changeAffinity(p.getProposer(),10);
				break;
			default:
				break;
			}
			break;
		case REINVATTEMPT_OTHERS:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				if(isFavored(p.getTAgent())){
					changeAffinity(p.getProposer(),10);
				}
				else{
					changeAffinity(p.getProposer(),-3);
				}
				break;
			case INVESTOR:
				if(isFavored(p.getTAgent())){
					changeAffinity(p.getProposer(),2);
				}
				else{
					changeAffinity(p.getProposer(),-3);
				}
				break;
			case MISER:
				if(isFavored(p.getTAgent())){
					changeAffinity(p.getProposer(),-3);
				}
				else{
					changeAffinity(p.getProposer(),-5);
				}
				break;
			case NONE:
				break;
			case SABOTEUR:
				if(isFavored(p.getTAgent())){
					changeAffinity(p.getProposer(),8);
				}
				else{
					changeAffinity(p.getProposer(),2);
				}
				break;
			default:
				break;
			}
			break;
		case REMOVALATTEMPT:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				changeAffinity(p.getProposer(),-15);
				break;
			case INVESTOR:
				changeAffinity(p.getProposer(),-30);
				break;
			case MISER:
				changeAffinity(p.getProposer(),-25);
				break;
			case NONE:
				changeAffinity(p.getProposer(),-20);
				break;
			case SABOTEUR:
				changeAffinity(p.getProposer(),-12);
				break;
			default:
				break;
			}
			break;
		case REMOVALATTEMPT_OTHERS:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				if(isFavored(p.getTAgent())){
					changeAffinity(p.getProposer(),-10);
				}
				else{
					changeAffinity(p.getProposer(),10);
				}
				break;
			case INVESTOR:
				if(isFavored(p.getTAgent())){
					changeAffinity(p.getProposer(),2);
				}
				else{
					changeAffinity(p.getProposer(),5);
				}
				break;
			case MISER:
				if(isFavored(p.getTAgent())){
					changeAffinity(p.getProposer(),5);
				}
				else{
					changeAffinity(p.getProposer(),10);
				}
				break;
			case NONE:
				break;
			case SABOTEUR:
				if(isFavored(p.getTAgent())){
					changeAffinity(p.getProposer(),1);
				}
				else{
					changeAffinity(p.getProposer(),15);
				}
				break;
			default:
				break;
			}
			break;
		case SUPPRESSOTHERSGAIN:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				if(isFavored(p.getTAgent())){
					changeAffinity(p.getProposer(),1);
				}
				else{
					changeAffinity(p.getProposer(),2);
				}
				break;
			case INVESTOR:
				if(isFavored(p.getTAgent())){
					changeAffinity(p.getProposer(),2);
				}
				else{
					changeAffinity(p.getProposer(),3);
				}
				break;
			case MISER:
				if(isFavored(p.getTAgent())){
					changeAffinity(p.getProposer(),3);
				}
				else{
					changeAffinity(p.getProposer(),5);
				}
				break;
			case NONE:
				break;
			case SABOTEUR:
				if(isFavored(p.getTAgent())){
					changeAffinity(p.getProposer(),-3);
				}
				else{
					changeAffinity(p.getProposer(),-2);
				}
				break;
			default:
				break;
			}
			break;
		case SUPRESSMYGAIN:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				changeAffinity(p.getProposer(),1);
				break;
			case INVESTOR:
				changeAffinity(p.getProposer(),-6);
				break;
			case MISER:
				changeAffinity(p.getProposer(),-10);
				break;
			case NONE:
				changeAffinity(p.getProposer(),-4);
				break;
			case SABOTEUR:
				changeAffinity(p.getProposer(),-2);
				break;
			default:
				break;
			}
			break;
		case SUPRESSREPLENISH:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				changeAffinity(p.getProposer(),-12);
				break;
			case INVESTOR:
				changeAffinity(p.getProposer(),-5);
				break;
			case MISER:
				changeAffinity(p.getProposer(),-1);
				break;
			case NONE:
				changeAffinity(p.getProposer(),-2);
				break;
			case SABOTEUR:
				changeAffinity(p.getProposer(),9);
				break;
			default:
				break;
			}
			break;
		default:
			break;
		
		}
		
		
	}

	public void updateAffinity(ProposeRuleChange p, Vote v){
//		logger.info(Avatar);
//		logger.info(p.getPurposes());
		int change = 0;
		switch (p.getPurposes().get(Avatar)){
		case RAISEMYGAIN:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				change = 0;
				break;
			case INVESTOR:
				change = 2;
				break;
			case MISER:
				change = 4;
				break;
			case NONE:
				change = 1;
				break;
			case SABOTEUR:
				change = 1;
				break;
			default:
				break;
			}
			break;
		case RAISEOTHERSGAIN:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				change = -2;
				break;
			case INVESTOR:
				change = -1;
				break;
			case MISER:
				change = -2;
				break;
			case NONE:
				change = -1;
				break;
			case SABOTEUR:
				change = 1;
				break;
			default:
				break;
			}
			break;
		case RAISEREPLENISH:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				change = 5;
				break;
			case INVESTOR:
				change = 2;
				break;
			case MISER:
				change = 1;
				break;
			case NONE:
				change = 1;
				break;
			case SABOTEUR:
				change = -6;
				break;
			default:
				break;
			}
			break;
		case REINVATTEMPT:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				change = 12;
				break;
			case INVESTOR:
				change = 10;
				break;
			case MISER:
				change = 10;
				break;
			case NONE:
				change = 10;
				break;
			case SABOTEUR:
				change = 5;
				break;
			default:
				break;
			}
			break;
		case REINVATTEMPT_OTHERS:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				if(isFavored(p.getTAgent())){
					change = 5;
				}
				else{
					change = -1;
				}
				break;
			case INVESTOR:
				if(isFavored(p.getTAgent())){
					change = 1;
				}
				else{
					change = -1;
				}
				break;
			case MISER:
				if(isFavored(p.getTAgent())){
					change = -1;
				}
				else{
					change = -5;
				}
				break;
			case NONE:
				break;
			case SABOTEUR:
				if(isFavored(p.getTAgent())){
					change = 4;
				}
				else{
					change = 2;
				}
				break;
			default:
				break;
			}
			break;
		case REMOVALATTEMPT:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				change = -7;
				break;
			case INVESTOR:
				change = -15;
				break;
			case MISER:
				change = -12;
				break;
			case NONE:
				change = -10;
				break;
			case SABOTEUR:
				change = -6;
				break;
			default:
				break;
			}
			break;
		case REMOVALATTEMPT_OTHERS:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				if(isFavored(p.getTAgent())){
					change = -5;
				}
				else{
					change = 5;
				}
				break;
			case INVESTOR:
				if(isFavored(p.getTAgent())){
					change = 1;
				}
				else{
					change = 2;
				}
				break;
			case MISER:
				if(isFavored(p.getTAgent())){
					change = 2;
				}
				else{
					change = 5;
				}
				break;
			case NONE:
				break;
			case SABOTEUR:
				if(isFavored(p.getTAgent())){
					change = 1;
				}
				else{
					change = 7;
				}
				break;
			default:
				break;
			}
			break;
		case SUPPRESSOTHERSGAIN:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				if(isFavored(p.getTAgent())){
					change = 1;
				}
				else{
					change = 1;
				}
				break;
			case INVESTOR:
				if(isFavored(p.getTAgent())){
					change = 1;
				}
				else{
					change = 1;
				}
				break;
			case MISER:
				if(isFavored(p.getTAgent())){
					change = 1;
				}
				else{
					change = 2;
				}
				break;
			case NONE:
				break;
			case SABOTEUR:
				if(isFavored(p.getTAgent())){
					change = -1;
				}
				else{
					change = -1;
				}
				break;
			default:
				break;
			}
			break;
		case SUPRESSMYGAIN:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				change = 1;
				break;
			case INVESTOR:
				change = -3;
				break;
			case MISER:
				change = -5;
				break;
			case NONE:
				change = -2;
				break;
			case SABOTEUR:
				change = -1;
				break;
			default:
				break;
			}
			break;
		case SUPRESSREPLENISH:
			switch(Avatar.getType()){
			case ENVIRONMENTALIST:
				change = -6;
				break;
			case INVESTOR:
				change = -2;
				break;
			case MISER:
				change = -1;
				break;
			case NONE:
				change = -1;
				break;
			case SABOTEUR:
				change = 4;
				break;
			default:
				break;
			}
			break;
		default:
			break;
		
		}
		if(v.getVote() == VoteType.YES){
		}
		else if (v.getVote() == VoteType.NO){
			change = -change;
		}
		else{
		}
		changeAffinity(v.getVoter(),change);
	}

	public void DecayAffinity(){
		int d = 0;
		for (Affinity A : AffinityTable){
			if(A.Aff >= 50){
				d = (A.Aff - 50) / 7;
			}
			else{
				d = -((50 - A.Aff) / 7);
			}
			A.Aff = A.Aff - d;
		}
	}
	
}
