package services;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.drools.definition.KnowledgePackage;
import org.drools.runtime.StatefulKnowledgeSession;

import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBusModule;
import uk.ac.imperial.presage2.core.participant.Participant;
import actions.ProposeRuleChange;
import actions.Vote;
import agents.NomicAgent;

import com.google.inject.AbstractModule;

import enums.ParticipationProposalType;
import enums.RateChangeType;
import enums.RuleValueType;
import enums.VoteType;
import facts.Affinity;
import facts.AffinityManager;
import facts.ProposalGenerator;
import facts.RuleDefinition;

public class StrategyBoardService extends EnvironmentService {
	
	enum ProposalFlavor{
		BENIFICIAL,DETRIMENTAL,NONE;
	}

	private final Logger logger = Logger.getLogger(this.getClass());
	
	final private EnvironmentServiceProvider serviceProvider;
	
	private NomicService superNomicService;
	
	
	private StatefulKnowledgeSession testSession;
	
	private NomicAgent controller;
	
	private ProposalGenerator PG = null;
	
	Random rand = new Random();
	
	private AffinityManager AM; 
	
	public void updateAffinity(ProposeRuleChange currentRuleChange){
		AM.updateAffinity(currentRuleChange);
	}
	
	
	

	public StrategyBoardService(EnvironmentSharedStateAccess ss, EnvironmentServiceProvider provider,
			Participant p) throws UnavailableServiceException {
		super(ss);
		this.serviceProvider = provider;
		superNomicService = provider.getEnvironmentService(NomicService.class);
		if (p instanceof NomicAgent) {
			controller = (NomicAgent)p;
			AM = new AffinityManager(superNomicService.getAgents(),controller);
		}
		
	}
	

	
	
	public NomicService getSuperNomicService() {
		if (superNomicService == null) {
			try {
				superNomicService = serviceProvider.getEnvironmentService(NomicService.class);
			} catch (UnavailableServiceException e) {
				logger.warn("Unable to get superNomicService for " + controller.getName(), e);
			}
		}
		
		return superNomicService;
	}
	
	public void setController(NomicAgent controller) {
		this.controller = controller;
	}
	



	
	/**
	 * Used by subsimulations to access supersimulation state data for initialization.
	 * @return
	 */
	public StatefulKnowledgeSession getReplacementSession() {
		return testSession;
	}
	
	
	/**
	 * True if the parameter agent is this scenario service's controller.
	 * @param agent
	 * @return
	 */
	public boolean IsController(NomicAgent agent) {
		return agent.getID() == controller.getID();
	}
	
	public NomicAgent getController() {
		return controller;
	}
	
	//this flavor is whether the proposal is beneficial to target Agent in controller's opinion. 
	//if the "target" actually go to the replenishment, they consider their own goal.
	//Environmentalists and Saboteurs have different ideas on replenishment manipulations.
	private ProposalFlavor getProposalFlavor(ProposeRuleChange currentRuleChange){
		ProposalFlavor out = ProposalFlavor.NONE;
		switch(currentRuleChange.getPurposes().get(controller)){
		case RAISEMYGAIN:
			out = ProposalFlavor.BENIFICIAL;
			break;
		case RAISEOTHERSGAIN:
			out = ProposalFlavor.BENIFICIAL;
			break;
		case RAISEREPLENISH:
			switch(controller.getType()){
			case ENVIRONMENTALIST:
				out = ProposalFlavor.BENIFICIAL;
				break;
			case INVESTOR:
				out = ProposalFlavor.BENIFICIAL;
				break;
			case MISER:
				out = ProposalFlavor.BENIFICIAL;
				break;
			case NONE:
				out = ProposalFlavor.BENIFICIAL;
				break;
			case SABOTEUR:
				out = ProposalFlavor.DETRIMENTAL;
				break;
			default:
				break;
			}
			break;
		case REINVATTEMPT:
			out = ProposalFlavor.BENIFICIAL;
			break;
		case REINVATTEMPT_OTHERS:
			out = ProposalFlavor.BENIFICIAL;
			break;
		case REMOVALATTEMPT:
			out = ProposalFlavor.DETRIMENTAL;
			break;
		case REMOVALATTEMPT_OTHERS:
			out = ProposalFlavor.DETRIMENTAL;
			break;
		case SUPPRESSOTHERSGAIN:
			out = ProposalFlavor.DETRIMENTAL;
			break;
		case SUPRESSMYGAIN:
			out = ProposalFlavor.DETRIMENTAL;
			break;
		case SUPRESSREPLENISH:
			switch(controller.getType()){
				case ENVIRONMENTALIST:
					out = ProposalFlavor.DETRIMENTAL;
					break;
				case INVESTOR:
					out = ProposalFlavor.DETRIMENTAL;
					break;
				case MISER:
					out = ProposalFlavor.DETRIMENTAL;
					break;
				case NONE:
					out = ProposalFlavor.DETRIMENTAL;
					break;
				case SABOTEUR:
					out = ProposalFlavor.BENIFICIAL;
					break;
				default:
					break;
			}
			break;
		default:
			break;
		
		}
		return out;
	}


	
	
	
	
	public ProposeRuleChange getMyProposal(float p){
		if (PG == null){
			PG = new ProposalGenerator(controller,getSuperNomicService());
			
		}
		if (AM == null){
			
		AM = new AffinityManager(getSuperNomicService().getAgents(),controller);
		}
		ProposeRuleChange advice = new ProposeRuleChange(controller);
	//generate a intelligent, objective-oriented proposal
		if( rand.nextFloat() < p) {
			int MoveType = rand.nextInt(100);
			switch(controller.getType()){
			case MISER:
				if(MoveType < 20) {
					//if it gets someone really annoying
					if (AM.getHated() != controller){
					
						advice = PG.excludeplayer(AM.getHated());
					}
					else {
						MoveType = MoveType + 20;
					}
				}
				// besides I'd rather earn more
				if(MoveType >= 20) {
					advice = PG.raisemygain();
				}
				break;
			case ENVIRONMENTALIST:
				//Prioritising getting rid of those who are threat to my concept
				if(MoveType < 30){
					if (AM.getHated() != controller){
						
						advice = PG.excludeplayer(AM.getHated());
					}
					else {
						MoveType = MoveType + 30;
					}
				}
				//get backed by some friends!
				if(MoveType >= 30 && MoveType < 50){
					if (AM.getFavored() != controller){
						advice = PG.rejoinplayer(AM.getFavored());
					}
					else {
						MoveType = MoveType + 20;
					}
				}
				//open the source!
				if(MoveType >= 50 && MoveType < 80){
					advice = PG.raisereplenishment();
				}
				//punish the greedy
				if(MoveType >= 80){
					advice = PG.lowerrichonegain();
				}
				break;
			case INVESTOR:
				if(MoveType < 15){
					if (AM.getHated() != controller){
						
						advice = PG.excludeplayer(AM.getHated());
					}
					else {
						MoveType = MoveType + 10;
					}
				}
				if(MoveType >= 15 && MoveType < 35){
					advice = PG.raisereplenishment();
				}
				if(MoveType >= 35 && MoveType < 70){
					advice = PG.raisemygain();
				}
				if(MoveType >= 70 && MoveType < 80){
					if (AM.getFavored() != controller){
						advice = PG.rejoinplayer(AM.getFavored());
					}
					else {
						MoveType = MoveType + 10;
					}
				}
				if(MoveType >= 80 && MoveType < 90){
					advice = PG.lowerrichonegain();
				}
				if(MoveType >= 90){
					advice = PG.lowergain(AM.getHated());
				}
				break;
			case NONE:
				advice = PG.randomall((float) 0.7);
				break;
			case SABOTEUR:
				if(MoveType < 25){
					if (AM.getHated() != controller){
						
						advice = PG.excludeplayer(AM.getHated());
					}
					else {
						MoveType = MoveType + 25;
					}
				}
				//I seek destruction-
				if(MoveType >= 25 && MoveType < 75){
					advice = PG.lowerreplenishment();
				}
				if(MoveType >= 90){
					advice = PG.raisemygain();
				}
				break;
			default:
				advice = PG.randomall((float) 0.7);
				break;
			}
		
		}
		//otherwise generate a random proposal
		else{
			advice = PG.randomall((float) 0.7);
		}
		return advice;
	}

	public VoteType getMyVote(ProposeRuleChange change, float p, int subsimpref){
		NomicAgent Target = controller;
		VoteType advice = VoteType.YES;
		//generate a random vote
		if ( rand.nextBoolean()){
			advice = VoteType.NO;
		}
		//probability of p to do a intelligent vote instead.
		if( rand.nextFloat() < p) {
		float voteworth;
		int flavor = 0;
		if(getProposalFlavor(change) == ProposalFlavor.BENIFICIAL){
			flavor = 1;
		}
		else if(getProposalFlavor(change) == ProposalFlavor.DETRIMENTAL){
			flavor = -1;
		}
		else{}
			if(change.getRuleValueType() == RuleValueType.REPLENISHMENTBOOST || change.getRuleValueType() == RuleValueType.REPLENISHMENT){
				//target remains controller;
			}
			else{
				Target = change.getTAgent();
			}
			// opinion * benefit = positive resulting in larger value to vote for.
			if(Target != controller){
				voteworth = (AM.getAffinity(Target)-AM.getAverageAffinity())*flavor - 20 + rand.nextInt(41);
				logger.info(controller + " - I like the target agent" + AM.getAffinity(Target));
			}
			else{
				voteworth = ( 100 -AM.getAverageAffinity())*flavor - 45 + rand.nextInt(41) + subsimpref / 2;
			}
			logger.info(controller + " - this vote's value is " + voteworth);
			if (voteworth >= 0){
				advice = VoteType.YES;
			}
			else{
				advice = VoteType.NO;
			}
		
		}
		return advice;
	}




	public void updateAffinity(ProposeRuleChange currentRuleChange, Vote vote) {
		AM.updateAffinity(currentRuleChange, vote);
		
	}
	

}
