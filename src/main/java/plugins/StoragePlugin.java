package plugins;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.Date;

import org.apache.log4j.Logger;

import services.NomicService;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.db.StorageService;
import uk.ac.imperial.presage2.core.db.persistent.PersistentEnvironment;
import uk.ac.imperial.presage2.core.db.persistent.TransientAgentState;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.plugin.Plugin;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.util.environment.EnvironmentMembersService;
import actions.ProposeNoRuleChange;
import actions.ProposeRuleAddition;
import actions.ProposeRuleChange;
import actions.ProposeRuleModification;
import actions.ProposeRuleRemoval;
import actions.Vote;
import agents.NomicAgent;

import com.google.inject.Inject;

import enums.TurnType;
import exceptions.NoExistentRuleChangeException;
import facts.ProposalReader;

/**
 * Plugin that stores all data relevant to Nomic from a single simulation. All data is stored when the simulation ends.
 * @author Stuart Holland/Hanguang Sun
 * turn on Agent Detail logging at onSimulationComplete();
 *
 *
 *
 *
 */
public class StoragePlugin implements Plugin {
	
	private final Logger logger = Logger.getLogger(StoragePlugin.class);
	
	private StorageService storage;
	
	private final EnvironmentMembersService membersService;
	private final NomicService nomicService;

	
	private ProposalReader PR;
	
	@Parameter(name="resultpkg")
	private String resultpkg;
	
	private File TLog = new File("TextLog.txt");
	
	private File simreport;
	
	private File agentstatistics;
	
	private FileWriter FW, FWs, FWa; 
	
	public StoragePlugin() {
		super();
		storage = null;
		nomicService = null;
		membersService = null;

		logger.info("No storage service, no plugin.");
	}
	
	@Inject
	public StoragePlugin(EnvironmentServiceProvider serviceProvider,
			Time t) throws UnavailableServiceException {
		this.storage = null;
		this.membersService = serviceProvider.getEnvironmentService(EnvironmentMembersService.class);
		this.nomicService = serviceProvider.getEnvironmentService(NomicService.class);

	}
	
	@Inject(optional = true)
	public void setStorage(StorageService storage) {
		this.storage = storage;
	}
	
	private void printPoints(NomicAgent agent) throws IOException{
		String out = "";
		for (int i = 0; i<= nomicService.getTurnNumber(); ++i){
		out = out + agent.statistics.getPointslog().get(i) + " ";
		}
		FW.write(out);
	}
	
	private void printAuthority(NomicAgent agent) throws IOException{
		String out = "";
		for (int i = 0; i<= nomicService.getTurnNumber(); ++i){
		out = out + agent.statistics.getAuthoritylog().get(i) + " ";
		}
		FW.write(out);
	}
	
	private void printMorality(NomicAgent agent) throws IOException{
		String out = "";
		for (int i = 0; i<= nomicService.getTurnNumber(); ++i){
		out = out + agent.statistics.getMoralitylog().get(i) + " ";
		}
		FW.write(out);
	}
	
	
	private void StoreChange(ProposeRuleChange ruleChange) throws IOException {
		
		PersistentEnvironment env = storage.getSimulation().getEnvironment();
		
		Integer time = ruleChange.getSimTime();
		int Turn = ruleChange.getT();
//		FW.write("Time: " + time + System.lineSeparator());
//		FW.write("Proposer: " + ruleChange.getProposer().getName() + System.lineSeparator());
//		FW.write("Content: " + PR.ReadProposal(ruleChange) + System.lineSeparator());
//		FW.write("Success: " + ruleChange.getSucceeded() + System.lineSeparator());
//		FW.write("Turn" + Turn + System.lineSeparator());
		
		
		env.setProperty("Proposer", time, ruleChange.getProposer().getName());
		env.setProperty("Type", time, ruleChange.getRuleChangeType().toString());
		env.setProperty("Content: ",time, PR.ReadProposal(ruleChange));
		env.setProperty("Success", time, "" + ruleChange.getSucceeded());
		env.setProperty("Turn", time, nomicService.getTurnNumber().toString());
		
		if (ruleChange instanceof ProposeRuleAddition) {
			
			ProposeRuleAddition addition = (ProposeRuleAddition)ruleChange;
			env.setProperty("NewRuleName", time, addition.getNewRuleName());
			env.setProperty("NewRule", time, addition.getNewRule());
			
		}
		else if (ruleChange instanceof ProposeRuleModification) {
			
			ProposeRuleModification modification = (ProposeRuleModification)ruleChange;
			
			env.setProperty("OldRuleName", time, modification.getOldRuleName());
//			env.setProperty("OldRule", time, 
//					ruleClassificationService.getRuleBody(modification.getOldRuleName()));
			env.setProperty("NewRuleName", time, 
					modification.getNewRuleName());
			env.setProperty("NewRule", time, modification.getNewRule());
			
		}
		else if (ruleChange instanceof ProposeRuleRemoval) {
			
			ProposeRuleRemoval removal = (ProposeRuleRemoval)ruleChange;
			env.setProperty("OldRuleName", time, removal.getOldRuleName());
//			env.setProperty("OldRule", time, 
//					ruleClassificationService.getRuleBody(removal.getOldRuleName()));
			
		}
	}
	
	private void StoreVote(Vote vote) throws IOException {
		UUID pid = vote.getVoter().getID();
		
		if (vote != null) {
			TransientAgentState state = storage.getAgentState(pid, vote.getSimTime());
			int Turn = vote.getT();
//			FW.write("CasterName " + nomicService.getAgentName(pid) + System.lineSeparator());
//			FW.write("Vote " + vote.getVote().toString() + System.lineSeparator());
//			FW.write("TurnNumber " + Turn + System.lineSeparator());
			state.setProperty("CasterName", nomicService.getAgentName(pid));
			state.setProperty("Vote", vote.getVote().toString());
			state.setProperty("TurnNumber", nomicService.getTurnNumber().toString());
		}
	}
	
	private void simreportthisturn(int t) throws NoExistentRuleChangeException, IOException{
		ProposeRuleChange p = null;
		if (p == null || p.getT() != t){
			ArrayList<ProposeRuleChange> pastproposals = nomicService.getSimRuleChanges();
			for (ProposeRuleChange CP : pastproposals){
				if(CP.getT() == t){
					p = CP;
				}
			}
		}
		String turnR = System.lineSeparator() + "Turn " + t + ":" + System.lineSeparator();
		if(p != null){
			turnR = turnR + "Proposal by " + p.getProposer().getName() + ":" + System.lineSeparator() + PR.ReadProposal(p) + System.lineSeparator();
			turnR = turnR + "Vote:" + System.lineSeparator();
			ArrayList<Vote> thisTurn = new ArrayList<Vote>();
			
			for (Vote vote : nomicService.getSimVotes()) {
				if (vote.getT() == t)
					thisTurn.add(vote);
			}
			
			for (Vote vote2 : thisTurn){
				turnR = turnR + vote2.getVoter().getName() + "-" + vote2.getVote() + " ";
			}
			turnR = turnR + System.lineSeparator();
			turnR = turnR + "Sucess:" + p.getSucceeded() + System.lineSeparator();
		}
		else{
			turnR = turnR + "It's already over.";
		}
		for(NomicService.simpointschange pc :nomicService.getSimpointchanges()){
			if (pc.getT() == t){
				if(pc.getHarvesting() != null){
				turnR = turnR + pc.getHarvesting().getName() + " takes " + pc.getHarvestamount() + "points." + System.lineSeparator();
				}
				if(pc.getPool() == 0 && t == 0){
				}
				else if (pc.getPool() == 0 && nomicService.getResourcePool() == 0 && t != 0){
					turnR = turnR + "Pool depleted.";
				}
				else if (t != 0 && pc.getReplenished() < 0){
					turnR = turnR + "Finally there is " + nomicService.getResourcePool() + "points left.";
				}
				else{
					turnR = turnR + "Now there is " + pc.getPool() + "points left and getting replenished by " + pc.getReplenished() + "points." + System.lineSeparator();
				}
				
				
			}
		}
		FWs.write(turnR);
		
		
	}
	
	private void agentsummary(NomicAgent agent) throws IOException{
		int numofsucproposals = agent.statistics.getNumofsucproposals();
		int numoffailproposals = agent.statistics.getProposals().size() - numofsucproposals;
//		for (ProposeRuleChange p : agent.statistics.getProposals()){
//			if (p.getSucceeded()){
//				++numofsucproposals;
//			}
//			else{
//				++numoffailproposals;
//			}
//		}
		String summary = agent.getName() + ": " + agent.getType() + System.lineSeparator();
		summary = summary + "Points: " + agent.statistics.finalpoints + System.lineSeparator();
		summary = summary + "Average Morality: " + agent.statistics.getaverageMorality() + System.lineSeparator();
		summary = summary + "Average Authority: " + agent.statistics.getaverageAuthority() + System.lineSeparator();
		summary = summary + "Total time of Exclusion: " + agent.statistics.getNumofexclusions() + System.lineSeparator();
		summary = summary + "Successful Proposals: " + numofsucproposals + System.lineSeparator();
		summary = summary + "Failed Proposals: " + numoffailproposals+ System.lineSeparator();
		summary = summary + "YES Votes: " + agent.statistics.getNumofyesvotes() + System.lineSeparator();
		summary = summary + "NO Votes: " + agent.statistics.getNumofnovotes() + System.lineSeparator();
		summary = summary + System.lineSeparator();
		FWa.write(summary);
	}
	
	@Override
	public void initialise() {
		Date date = new Date();
		resultpkg = "Simulation" + System.currentTimeMillis();
		simreport = new File(resultpkg + "_simreport.txt");
		agentstatistics = new File(resultpkg + "_agentstatistics.txt");
		try {

			FW = new FileWriter(TLog);
			FWs = new FileWriter(simreport);
			FWa = new FileWriter(agentstatistics);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.PR = new ProposalReader("log",this.nomicService.getNumberOfAgents());
	}

	@Override
	@Deprecated
	public void execute() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSimulationComplete() {
		if (storage != null) {
			logger.info("Storing the universe.");
			
			// Generator Sim text report
			nomicService.archivepointchanges();
			for (int i=0; i<= nomicService.getTurnNumber(); ++i){
				try {
					simreportthisturn(i);
				} catch (NoExistentRuleChangeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			//Generate Agent Summary
			for (NomicAgent a : nomicService.getAgents()){
				try {
					agentsummary(a);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			///write agent status history
//			for (NomicAgent a : nomicService.getAgents()){
//				String pre = a.getName() + " ";
//				pre = pre + a.getType() + System.lineSeparator();
//				try {
//					FW.write(pre);
//				
//				FW.write(System.lineSeparator() + "Morality" + System.lineSeparator());
//				printMorality(a);
//				FW.write(System.lineSeparator() + "Authority" + System.lineSeparator());
//				printAuthority(a);
//				FW.write(System.lineSeparator() + "points" + System.lineSeparator());
//				printPoints(a);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
			
			
			// Store all rule changes
			for (ProposeRuleChange ruleChange : nomicService.getSimRuleChanges()) {
				try {
					StoreChange(ruleChange);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			for (Vote vote : nomicService.getSimVotes()) {
				try {
					StoreVote(vote);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// Store final agent information
			for (UUID pid : membersService.getParticipants()) {
				storage.getAgent(pid).setProperty("SeqID", nomicService.getSequentialID(pid).toString());
				storage.getAgent(pid).setProperty("NumSubSims", nomicService.getNumSubSimsRun(pid).toString());
				storage.getAgent(pid).setProperty("AverageSubSimLength", nomicService.getAverageSubSimLength(pid).toString());
			}
			
			// Store final simulation information
			storage.getSimulation().addParameter("NumTurns", nomicService.getTurnNumber().toString());
			storage.getSimulation().addParameter("NumRounds", nomicService.getRoundNumber().toString());
			storage.getSimulation().addParameter("NumAgents", nomicService.getNumberOfAgents().toString());
			
			storage.getSimulation().addParameter("Won", "" + nomicService.isGameOver());
			
			if (nomicService.isGameOver()) {
				storage.getSimulation().addParameter("Winner", nomicService.getLoser().getName());
				storage.getSimulation().addParameter("WinTime", nomicService.getGameOverTime().toString());
			}
		}
		try {
			FW.close();
			FWs.close();
			FWa.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void incrementTime() {
		
	}
}
