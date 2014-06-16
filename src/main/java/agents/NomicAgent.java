package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import services.NomicService;
import services.ScenarioService;
import services.StrategyBoardService;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;
import actions.ProposeNoRuleChange;
import actions.ProposeRuleAddition;
import actions.ProposeRuleChange;
import actions.ProposeRuleModification;
import actions.ProposeRuleRemoval;
import actions.Vote;
import enums.AgentType;
import enums.RuleFlavor;
import enums.VoteType;
import exceptions.NoExistentRuleChangeException;
import facts.ModifierManager;
import facts.ProposalReader;
import facts.RuleDefinition;
import plugins.AgentStatistics;

/**
 * Parent class for all agents that wish to play Nomic. For an example of which functions should be overriden
 * to define agent AI behavior, see <code>ExampleAgent</code>.
 * @author Stuart Holland
 *
 */
public class NomicAgent extends AbstractParticipant {

	public boolean Active;
	
	public boolean Locked;
	
	private int SequentialID;
	
	NomicService nomicService;
	
	ScenarioService scenarioService;
	
	StrategyBoardService strategyadvisor;
	
	public AgentStatistics statistics;
	
	
	public ModifierManager Modifiers = new ModifierManager();
	
	ProposalReader myReader = null;
	
	AgentType Type = AgentType.NONE;
	
	
	Random rand = new Random();
	
	float points = 0;
	
	float Morality = 0;
	
	float Authority = 10;
	
	float harvestrate = 5;
	
	
	int ratevary = 1;
	
	float ratevarydice = 2;
	
	float harvestmodifier = 1;
	

	

	
	Map<RuleFlavor, Integer> Flavors;
	

	
	
	
	public float mycurrentrate(){
		float amount = harvestrate + (float)(ratevary * ratevarydice) / 2;
		amount = amount * Modifiers.getOverallModifier();
		return amount;
	}
	
	public float getAverageHarvestRate(){
		return (harvestrate + ratevarydice * ratevary / 2) * Modifiers.getOverallModifier();
	}
	
	public float harvest(){
		if (Active){
			int randomvary = 0;
			for(int i=0; i<ratevarydice; i++){
				randomvary = randomvary + new Random().nextInt(ratevary + 1);
			}
			float amount = harvestrate + (float)randomvary;
			logger.info(this.getName() + " applying " + amount + " points with modifier" + Modifiers.getOverallModifier());
			amount = amount * Modifiers.getOverallModifier();
			
			float actualgain = nomicService.harvest(this, amount);
			points = points + actualgain;

			logger.info(this.getName() + " grabbing " + actualgain + " points at the end of their turn.");
			return actualgain;
		}
		else {
			logger.info(this.getName() + "is currently excluded and cannot take any point");
			return 0;
		}
	}
	
	private void MoralityDecay(){
		if (Morality > 0 && Active){
			Authority = (float) (Authority + Morality * 0.2);
		}
		Morality = (float) (Morality * 0.8);
	}
	
	
	
	public void Exclude() {
		Active = false;
		Authority = 10;
		
		logger.info("so bad! I'm out");
	}
	
	public void Rejoin() {
		Active = true;
		Authority = 10;
		logger.info( Type + " @I'm back for vengance!ahaha!");
	}
	

	public NomicAgent(UUID id, String name) {
		super(id, name);
		Modifiers = new ModifierManager();
	}
	
	/**
	 * This is part of an incomplete functionality set that allows agents to define their preferences for certain
	 * flavors at a class level.
	 * @return
	 */
	protected Map<RuleFlavor, Integer> chooseFlavorPreferences() {
		return new HashMap<RuleFlavor, Integer>();
	}

		
	public NomicService getNomicService() {
		return nomicService;
	}

	@Override
	protected void processInput(Input arg0) {
		
	}
	
	@Override
	protected Set<ParticipantSharedState> getSharedState() {
		Set<ParticipantSharedState> ss = super.getSharedState();
		ss.add(new ParticipantSharedState("test", getName(), getID()));
		return ss;
	}
	
	@Override
	public void initialise() {
		super.initialise();
		Active = true;
		try {
			this.nomicService = getEnvironmentService(NomicService.class);
			this.scenarioService = getEnvironmentService(ScenarioService.class);
			this.strategyadvisor = getEnvironmentService(StrategyBoardService.class);
			this.statistics = new AgentStatistics(this.getName());
		} catch (UnavailableServiceException e) {
			logger.warn("Couldn't get Nomic Environment Service.", e);
		}
	}
	/**
	 * Most agent AIs should not override this function (though it is left non-final for cases where that may be
	 * useful). The default behavior will query chooseProposal() and chooseVote(ProposeRuleChange) for information
	 * when it is relevant.
	 */
	@Override
	public void incrementTime() {
		if ( this instanceof ProxyAgent == false){
		statistics.putpoints(nomicService.getTurnNumber(), points);
		statistics.setFinalpoints(points);
		statistics.putmorality(nomicService.getTurnNumber(), Morality);
		statistics.putauthority(nomicService.getTurnNumber(), Authority);
		if ( Active == false){
			statistics.addexclusions();
			}
		}
		logger.info("I have " + getPoints() + " points.");
		if (nomicService.canProposeNow(this)) {
			logger.info("It's my turn to propose a rule!");
			doRuleChanges();
		}
		else if (nomicService.canVoteNow(this)) {
			try {
				doVoting();
			} catch (NoExistentRuleChangeException e) {
				logger.warn("Even though I can vote now, there is no rule to change.");
			}
		}
		else {
			logger.info("It isn't my turn, and we're not voting.");
		}
		MoralityDecay();
		
		Modifiers.decrementModifier();
		
		super.incrementTime();
	}
	
	private void doRuleChanges() {
		ProposeRuleChange myChange = chooseProposal();
		
		Unlock();
		
		

		if (myChange == null) {
			logger.info("No rule changes from me this turn.");
		}
		else {
//			coded by Sturat to read proposals, already replaced with my new ProposalReader class to perform
//			logger.info("I propose the following rule change: " + myChange);
			
			try {
				environment.act(myChange, getID(), authkey);
			} catch (ActionHandlingException e) {
				logger.warn("My rule change proposal has failed. Proposal: " + myChange, e);
			}
		}
	}
	
	/**
	 * New Agent AIs should override this function and use it to decide on a new rule change proposal.
	 * By default it will return a blank proposal that leads to this agent's turn to propose changes being skipped.
	 * @return
	 */
	protected ProposeRuleChange chooseProposal() {
		ProposeRuleChange MyProposal = new ProposeRuleChange(this);
		MyProposal = strategyadvisor.getMyProposal((float) 0.7);
		if (myReader == null){
			myReader = new ProposalReader(this.getName(),nomicService.getNumberOfAgents());
		}
		if(MyProposal != null){
		String postmyproposal = myReader.ReadProposal(MyProposal);
		logger.info(postmyproposal);
		}
		return MyProposal;
	}
	
	private void doVoting() throws NoExistentRuleChangeException {
		if(Active){
			ProposeRuleChange ruleChange = nomicService.getCurrentRuleChange();
		
			Vote vote = new Vote(this, chooseVote(ruleChange,(float)0.7));
			boolean success = false;
			try {
				environment.act(vote, getID(), authkey);
				success = true;
			} catch (ActionHandlingException e) {
				logger.warn("My attempt to vote " + vote.getVote() + " has failed.");
				e.printStackTrace();
			}
		
			if (success) {
				if ( this instanceof ProxyAgent == false){
					if (vote.getVote() == VoteType.YES){
						statistics.addNumofyesvotes();
					}
					if (vote.getVote() == VoteType.NO){
						statistics.addNumofnovotes();
					}
				}
				logger.info("I am voting " + vote.getVote() + " for this rule change.");
			}
		}
	}
	
	public int getSequentialID() {
		return SequentialID;
	}
	
	/**
	 * New agent AIs should override this function to decide on their vote for the parameter proposed rule change.
	 * 
	 * By default, voting will be random 50/50 for Yes/No.
	 * @param ruleChange
	 * @return
	 */
	public VoteType chooseVote(ProposeRuleChange ruleChange, float intprob) {
		
			return strategyadvisor.getMyVote(ruleChange,intprob, 50);
		
	}
	
	public VoteType chooseBinVote(ProposeRuleChange ruleChange) {
		if (rand.nextBoolean()) {
			return VoteType.YES;
		}
		else {
			return VoteType.NO;
		}
	}
	
	/**
	 * Given an integer, x, where 0 <= x <= 100, this function will return YES or NO
	 * as a function of that number treated as a probability. (Eg. 40 is 40%)
	 * @param chance
	 * @return
	 */
	public VoteType chooseVoteFromProbability(Integer chance) {
		if (rand.nextInt(100) < chance) {
			return VoteType.YES;
		}
		else {
			return VoteType.NO;
		}
	}
	
	/**
	 * Given an integer, x, where 0 <= x <= 100, this function will return true or
	 * false defined by that probability. (Eg. a parameter of 60 means 60% chance of true)
	 * @param chance
	 * @return
	 */
	public boolean isPreferred(Integer chance) {
		if (rand.nextInt(100) < chance) {
			return true;
		}
		else {
			return false;
		}
	}

	public void setSequentialID(int sequentialID) {
		SequentialID = sequentialID;
	}
	
	public void Lock(){
		Locked = true;
	}
	
	private void Unlock(){
		Locked = false;
	}
	
	public boolean Locked(){
		return Locked;
	}

	public float getPoints() {
		return points;
	}

	public synchronized void setPoints(float f) {
		this.points = f;
	}
	
	public float getHarvestrate() {
		return harvestrate;
	}

	public void setHarvestrate(float harvestrate) {
		this.harvestrate = harvestrate;
	}
	
	public void increasePoints(int points) {
		this.points += points;
	}
	
	public void decreasePoints(int points) {
		this.points -= points;
	}
	
	/**
	 * New agent AIs should override this function if they wish to have new behavior defined by any proposals
	 * succeeding.
	 * 
	 * Does nothing by default.
	 * @param ruleChange
	 */
	public void voteSucceeded(ProposeRuleChange ruleChange) {
		if (this instanceof ProxyAgent == false && ruleChange.getProposer() == this){
			statistics.putproposals(ruleChange);
			statistics.addsucproposals();
		}
	}
	
	/**
	 * New agent AIs should override this function if they wish to have new behavior defined by any proposals
	 * failing.
	 * 
	 * Does nothing by default.
	 * @param ruleChange
	 */
	public void voteFailed(ProposeRuleChange ruleChange) {
		if (this instanceof ProxyAgent == false && ruleChange.getProposer() == this){
			statistics.putproposals(ruleChange);
		}
	}
	

	

	/**
	 * New agent AIs should override this function if they wish to have an easily defined function for 
	 * deciding on subsimulation length based on rule changes.
	 * 
	 * By default, uses the rule's COMPLEXITY flavor to decide length.
	 * @param ruleChange
	 * @return
	 */
	public int getSubsimulationLength(ProposeRuleChange ruleChange) {
		Integer complexity = 0;

		
		Integer NumAgents = nomicService.getNumberOfAgents();
		
		return NumAgents * 6 + 2;
	}
	
	public void Lose() {
		nomicService.Lose(this);
	}
	
	
	public ProxyAgent getRepresentativeProxy() {
		ProxyAgent proxy = new ProxyAgent(uk.ac.imperial.presage2.core.util.random.Random.randomUUID(), 
				"proxy " + getName());
		proxy.setOwner(this);
		proxy.setPoints(getPoints());
		proxy.setSequentialID(getSequentialID());
		
		return proxy;
	}
	
	/**
	 * New agent AIs should override this function to tell the <code>ScenarioService</code> where to find
	 * the rules file that defines this AI's preferences.
	 * @return
	 */
	public String getProxyRulesFile() {
		return "src/main/resources/TestProxy.drl";
	}
	
	public int getNumSubSimsRun() {
		return scenarioService.getNumSubSimsRun();
	}
	
	public int getAverageSubSimLength() {
		return scenarioService.getAverageSubSimLength();
	}
	
	public ArrayList<RuleFlavor> getPositiveFlavors() {
		ArrayList<RuleFlavor> positives = new ArrayList<RuleFlavor>();
		
		for (RuleFlavor flavor : Flavors.keySet()) {
			if (Flavors.get(flavor) > 50)
				positives.add(flavor);
		}
		
		return positives;
	}
	public float getAuthority() {
		return Authority;
	}
	public float getMorality() {
		return Morality;
	}
	public void changeMorarlity(float c){
		Morality = Morality + c;
		logger.info("I got " + c + " Morality for this proposal/vote. Now I have " + Morality);
	}
	public AgentType getType(){
		return Type;
	}
	
	public void setType(AgentType type) {
		Type = type;
	}


	public void updateAffinity(ProposeRuleChange currentRuleChange) {
		strategyadvisor.updateAffinity(currentRuleChange);
	}

	public void updateAffinity(actions.ProposeRuleChange currentRuleChange,
			actions.Vote vote) {
		strategyadvisor.updateAffinity(currentRuleChange,vote);
		
	}
}
