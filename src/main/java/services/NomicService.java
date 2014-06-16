package services;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
//import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.compiler.DroolsParserException;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.core.simulator.Events;
import uk.ac.imperial.presage2.core.simulator.FinalizeEvent;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.util.random.Random;
import actions.ProposeRuleAddition;
import actions.ProposeRuleChange;
import actions.ProposeRuleModification;
import actions.ProposeRuleRemoval;
import actions.Vote;
import agents.NomicAgent;
import agents.ProxyAgent;


import com.google.inject.Inject;

import enums.RuleChangeType;
import enums.TurnType;
import exceptions.InvalidRuleProposalException;
import exceptions.NoExistentRuleChangeException;
import facts.ModifierManager;
import facts.RuleChangeApplier;
import facts.Turn;

public class NomicService extends EnvironmentService {

	private final Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * Controls access to the Drools KnowledgeBuilder class, because there were concurrency
	 * issues with creating many new knowledge packages simultaneously.
	 */
	public static Semaphore kBuilderSemaphore = new Semaphore(1);
	
	/**
	 * Controls 'refreshing' of the simulation. See refreshSession() for more details.
	 */
	public static Semaphore refreshSemaphore = new Semaphore(1);
	
	
	private EnvironmentServiceProvider serviceProvider;
	
	
	StatefulKnowledgeSession session;
	int TurnNumber = 0;
	
	public ModifierManager repmod;
	
	private RuleChangeApplier changeImplementor;
	
	/**
	 * List of all agents in this simulation.
	 */
	private ArrayList<NomicAgent> agents;
	
	/**
	 * List of all agent IDs in this simulation.
	 */
	private ArrayList<UUID> agentIDs;
	
	/**
	 * Map of votes made this turn.
	 */
	private Map<UUID,Vote> votesThisTurn;
	
	private float votevalueThisTurn = 0;
	
	/**
	 * List of all votes made during this simulation.
	 */
	private ArrayList<Vote> SimVotes;
	
	public class simpointschange{
		int t;
		NomicAgent harvesting; 
		float harvestamount;
		float pool;
		float replenished = -1;
		
		public simpointschange(int t) {
			super();
			this.t = t;
		}
		public int getT() {
			return t;
		}
		public void setT(int t) {
			this.t = t;
		}
		public NomicAgent getHarvesting() {
			return harvesting;
		}
		public void setHarvesting(NomicAgent harvesting) {
			this.harvesting = harvesting;
		}
		public float getHarvestamount() {
			return harvestamount;
		}
		public void setHarvestamount(float harvestamount) {
			this.harvestamount = harvestamount;
		}
		public float getPool() {
			return pool;
		}
		public void setPool(float pool) {
			this.pool = pool;
		}
		public float getReplenished() {
			return replenished;
		}
		public void setReplenished(float replenished) {
			this.replenished = replenished;
		}
		
	}
	
	private ArrayList<simpointschange> simpointchanges = new ArrayList<simpointschange>();
	
	private simpointschange pointchangethisturn = new simpointschange(0);
	/**
	 * List of all proposals from this simulation.
	 */
	private ArrayList<ProposeRuleChange> SimRuleChanges;
	/**
	 * List of all modifiers in effect but to expire.
	 */
	private ArrayList<ProposeRuleChange> timedChanges;
	
	/**
	 * Object that defines the state of the current turn.
	 */
	Turn currentTurn;
	
	@Parameter(name = "ResourcePool")
	float ResourcePool;
	
	@Parameter(name = "Replenishment")
	float Replenishment;
	
	/**
	 * Drools facthandler for above object.
	 */
	FactHandle turnHandle;
	
	/**
	 * Place holder agent used to instantiate the turn object at initialization without artificially choosing
	 * which agent will move first.
	 */
	NomicAgent placeHolderAgent = new NomicAgent(Random.randomUUID(), "placeholder");
	
	/**
	 * The rule change currently up for voting.
	 */
	ProposeRuleChange currentRuleChange;
	
	/**
	 * Previous rule change, from last turn.
	 */
	ProposeRuleChange previousRuleChange;
	
	/**
	 * The winner of this game of Nomic.
	 */
	NomicAgent Loser;
	
	/**
	 * The time this game was won.
	 */
	Integer LoseTime = -1;
	
	EventBus eb;
	
	/**
	 * Nomic SimTime (not Presage2 time), maps to the defined phases of Nomic turns.
	 */
	Integer SimTime;
	
	/**
	 * Returns the number of current active players.
	 */
	
	public int GetNoActiveAgents() {
		int n = 0;
		for (NomicAgent agent : agents) {
			if(agent.Active){
				n++;
			}
		} 
		return n;
	}
	
	public NomicAgent getRandAgent() throws NullPointerException{
		NomicAgent R = null;
		int chosen = Random.randomInt(agents.size());
		int i = 0;
		for (NomicAgent RA : agents){
			if(i == chosen){
				R = RA;
			}
			++i;
		}
		return R;
	}
	
	public float harvest(NomicAgent harvestor, float amount){
		float actualgain = amount;
		if (ResourcePool > amount){
			ResourcePool = ResourcePool -amount;
		}
		else {
			actualgain = ResourcePool;
			ResourcePool = 0;
			for( NomicAgent A : agents){
				A.Lock();
			}
			Lose(harvestor);
		}
		pointchangethisturn.setHarvestamount(actualgain);
		pointchangethisturn.setHarvesting(harvestor);
		return actualgain;
	}
	
	private void applyReplenishment(){
		float repthisturn = Replenishment * repmod.getOverallModifier();
		pointchangethisturn.setPool(ResourcePool);
		if(currentTurn.getType()!=TurnType.GAMEOVER){
		pointchangethisturn.setReplenished(repthisturn);
		ResourcePool = ResourcePool + repthisturn;
		logger.info("Turn " + currentTurn.getNumber() + " resourcepool has been replenished by " + repthisturn + "; Now there is " + ResourcePool + " left.");
		repmod.decrementModifier();
		}
	}
	
	@Inject
	public NomicService(EnvironmentSharedStateAccess sharedState, EnvironmentServiceProvider serviceProvider,
			StatefulKnowledgeSession session) {
		super(sharedState);
		this.session = session;
		this.serviceProvider = serviceProvider;
		
		currentTurn = new Turn(0, TurnType.INIT, placeHolderAgent);
		
		agents = new ArrayList<NomicAgent>();
		agentIDs = new ArrayList<UUID>();
		votesThisTurn = new HashMap<UUID, Vote>();
		SimVotes = new ArrayList<Vote>();
		SimRuleChanges = new ArrayList<ProposeRuleChange>();
		repmod = new ModifierManager();
		changeImplementor = new RuleChangeApplier(this);
		SimTime = 0;
		ResourcePool = 100;
		Replenishment = 2;

	}
	
	@Inject
	public void SetEventBus(EventBus e) {
		e.subscribe(this);
		this.eb = e;
	}
	

	
	@EventListener
	public void onInitialize(Events.Initialised e) {
		turnHandle = session.insert(currentTurn);
		
//		for (RuleDefinition definition : ruleClassificationService.getAllRules()) {
//			session.insert(definition);
//		}
	}
	
	public void archivepointchanges(){
		simpointchanges.add(pointchangethisturn);
	}
	
	@EventListener
	public void onIncrementTime(EndOfTimeCycle e) {
		// If someone has got this done, this game is over, let's stop everything.
		if (Loser != null) {
			currentTurn.setType(TurnType.GAMEOVER);
			currentRuleChange = null;
			previousRuleChange = null;
		}
		// If we're in initialization mode, then set up the first turn.
		else if (currentTurn.getType() == TurnType.INIT) {
			currentTurn.setType(TurnType.PROPOSE);
			currentTurn.setNumber(TurnNumber);
			SimTime++;
			
		}
		// Deals with proposal phase of a turn when we've received a proposal
		else if (currentTurn.getType() == TurnType.PROPOSE && currentRuleChange != null) {
			simpointchanges.add(pointchangethisturn);
			pointchangethisturn = new simpointschange(TurnNumber);
			for ( NomicAgent NA : agents){
//				logger.info(currentRuleChange.getPurposes());
				NA.updateAffinity(currentRuleChange);
			}
			// If this is a non-blank proposal, move on to voting phase
			if (currentRuleChange.getRuleChangeType() != RuleChangeType.NONE) {
				currentTurn.setType(TurnType.VOTE);
				previousRuleChange = currentRuleChange;
				SimTime++;
			}
			// If this is a blank proposal, the agent couldn't decide what to do, so we skip them.
			else {
				currentTurn.setNumber(++TurnNumber);
				applyReplenishment();
/*				for(ProposeRuleChange activemodifier : timedChanges){
					activemodifier.decrementTimer();
					if (activemodifier.getSucceeded() == false){
						activemodifier.setRuleChangeType(RuleChangeType.REMOVAL);
						ApplyRuleChange(activemodifier);
					}
				}*/
				SimTime++;
			}
		}
		// Deals with voting phase of a turn
		else if (currentTurn.getType() == TurnType.VOTE) {
			
			//TODO:just to mark this important section
			// If the required number of agents have voted, let's evaluate the current proposal
			if (currentTurn.isAllVoted()) {
				if ( votevalueThisTurn > 0){
					currentRuleChange.setSucceeded(true);
				}
				votevalueThisTurn = 0;
				votesThisTurn.clear();
				if (currentRuleChange.getSucceeded()) {
					logger.info("This proposal has succeeded.");
					
					ApplyRuleChange(currentRuleChange);
					for (NomicAgent agent : agents) {
						agent.voteSucceeded(currentRuleChange);
					}
				}
				else {
					logger.info("This proposal has failed to pass.");
					for (NomicAgent agent : agents) {
						agent.voteFailed(currentRuleChange);
					}
				}
				// Move on to propose phase of the next turn.
				previousRuleChange = currentRuleChange;
				currentRuleChange = null;
				currentTurn.setType(TurnType.PROPOSE);
				currentTurn.setNumber(++TurnNumber);
				applyReplenishment();
				/*for(ProposeRuleChange activemodifier : timedChanges){
					activemodifier.decrementTimer();
					if (activemodifier.getSucceeded() == false){
						activemodifier.setRuleChangeType(RuleChangeType.REMOVAL);
						ApplyRuleChange(activemodifier);
					}
				}*/
				currentTurn.setAllVoted(false);
				SimTime++;
			}
		}
		
		refreshSession();
		
		// Update turn object for new turn (there may be no change if some agents are taking long)
		session.update(session.getFactHandle(currentTurn), currentTurn);
		
		session.fireAllRules();
		logger.info("Next move, turn: " + currentTurn.getNumber() + ", " + currentTurn.getType());
	}
	
	@EventListener
	public void onFinalizeEvent(FinalizeEvent e) {
		applyReplenishment();
		if (Loser != null) {
			logger.info("THIS SIMULATION'S DEPLETER IS: " + Loser.getName() + "!");
		}
		else {
			logger.info("THIS SIMULATION HAS SUSTAINED SO FAR!");
		}
	}
	
	private String TestRule = "import agents.Test; "
			+ " rule \"Refresher\" "
			+ " when "
			+ " Test( ) "
			+ " then "
			+ " end ";
	
	/**
	 * Workaround to deal with a bug in Drools. Moving between multiple instances of the Drools rule engine
	 * which share some rule definitions can lead to some cached data in the engine reporting incorrect
	 * loaded class definitions for those shared rules when the state differs between the two.
	 * 
	 * Loading and unloading a rule from the current knowledge base forces Drools to update its records of
	 * which rules are currently active. This reduces the prevalence of class definition errors, but does not
	 * eliminate them.
	 */
	public void refreshSession() {
		logger.info("Refreshing session.");
		
		while (!refreshSemaphore.tryAcquire()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				logger.warn("Waiting for refresh semaphore interrupted.", e);
			}
		}
		
		try {
			RemoveRule("defaultpkg", "Refresher");
			
			try {
				addRule(TestRule);
			} catch (DroolsParserException e) {
				logger.warn("Refreshing failed.", e);
			}
		} finally {
			refreshSemaphore.release();
		}
		
//		logger.info(session.getKnowledgeBase().getStatefulKnowledgeSessions().size());
//		
//		logger.info(session.getKnowledgeBase().getKnowledgePackage("Rules").getName());
	}
	
	@Override
	public void registerParticipant(EnvironmentRegistrationRequest req) {
		agents.add((NomicAgent)req.getParticipant());
		agentIDs.add(req.getParticipantID());
		//session.insert(req.getParticipant());
		super.registerParticipant(req);
	}
	
	public Integer getSequentialID(UUID agentID) {
		for (NomicAgent agent : agents) {
			if (agent.getID() == agentID)
				return agent.getSequentialID();
		}
		
		return -1;
	}
	
	public Integer getNumSubSimsRun(UUID agentID) {
		for (NomicAgent agent : agents) {
			if (agent.getID() == agentID)
				return agent.getNumSubSimsRun();
		}
		
		return 0;
	}
	
	public Integer getAverageSubSimLength(UUID agentID) {
		for (NomicAgent agent : agents) {
			if (agent.getID() == agentID)
				return agent.getAverageSubSimLength();
		}
		
		return 0;
	}
	
	/**
	 * True if the parameter agent is the active agent and we are in the proposal phase of any turn.
	 * @param agent
	 * @return
	 */
	public boolean canProposeNow(NomicAgent agent) {
		return currentTurn.getType() == TurnType.PROPOSE &&
				currentTurn.getActivePlayer().getID() == agent.getID();
	}
	
	/**
	 * True if we are in the voting phase of any turn.
	 * @param agent
	 * @return
	 */
	public boolean canVoteNow(NomicAgent agent) {
		return currentTurn.type == TurnType.VOTE;
	}
	
	/**
	 * True if all required agents have voted for the success of the current proposal to be evaluated.
	 * @return
	 */
	public boolean isAllVoted() {
		return currentTurn.isAllVoted();
	}
	
	public void RemoveRule(String packageName, String ruleName) {		
		session.getKnowledgeBase().removeRule(packageName, ruleName);
	}
	
	public void Vote(Vote vote) throws NoExistentRuleChangeException {
		votevalueThisTurn = votevalueThisTurn + vote.getVoteValue();
		votesThisTurn.put(vote.getVoter().getID(), vote);
		for ( NomicAgent NA : agents){
//			logger.info(currentRuleChange.getPurposes());
			NA.updateAffinity(currentRuleChange,vote);
		}
		changeImplementor.MoralityUpdate(getCurrentRuleChange(), getCurrentRuleChange().getTAgent(), vote.getVoter(), vote);
		SimVotes.add(vote);
	}
	
	public void Lose(NomicAgent agent) {
		logger.info(agent.getName() + " has depleted the pool!");
		Loser = agent;
		LoseTime = getSimTime();
	}
	
	/**
	 * Called by the <code>ProposeRuleChangeActionHandler</code> to register a proposal with the Nomic service.
	 * @param ruleChange
	 * @throws InvalidRuleProposalException If we are not in the proposal phase.
	 */
	public void ProposeRuleChange(ProposeRuleChange ruleChange) 
			throws InvalidRuleProposalException {
		if (currentTurn.type != TurnType.PROPOSE) {
			throw new InvalidRuleProposalException("This turn has passed its proposal stage.");
		}
		
		currentRuleChange = ruleChange;
		changeImplementor.MoralityUpdate(currentRuleChange, currentRuleChange.getTAgent(), currentRuleChange.getProposer(), null);
		SimRuleChanges.add(ruleChange);
	}
	
	/**
	 * Returns the current rule change proposal.
	 * @return
	 * @throws NoExistentRuleChangeException If there is no valid rule change.
	 */
	public ProposeRuleChange getCurrentRuleChange() throws NoExistentRuleChangeException {
		if (currentRuleChange == null)
			throw new NoExistentRuleChangeException("There is no valid rule change proposition.");
		else 
			return currentRuleChange;
	}
	
	/**
	 * Applies the parameter rule change to the currently active Drools knowledge base.
	 * @param ruleChange
	 */
	public void ApplyRuleChange(ProposeRuleChange ruleChange) {
		logger.info("I am a Nomic Service applying a rule change.");
		logger.info("My agents are: ");
		for (NomicAgent agent : agents) {
			logger.info(agent.getName());
		}
		RuleChangeType change = ruleChange.getRuleChangeType();
		if (change != RuleChangeType.NONE){
			changeImplementor.ApplyChange(ruleChange);
		}
		/*
		if (change == RuleChangeType.MODIFICATION) {
			ProposeRuleModification ruleMod = (ProposeRuleModification)ruleChange;
			try {
				logger.info("Modifying rule \'" + ruleMod.getOldRuleName()
						+ "\'");
				getRuleClassificationService().setActive(ruleMod.getOldRuleName(), false);
				getRuleClassificationService().setActive(ruleMod.getNewRuleName(), true);
				
				RemoveRule(ruleMod.getOldRulePackage(), ruleMod.getOldRuleName());
				addRule(ruleMod.getNewRule());
			} catch (DroolsParserException e) {
				logger.warn("Unable to parse new version of existing rule.", e);
				
			}
		}
		else if (change == RuleChangeType.ADDITION) {
			ProposeRuleAddition ruleMod = (ProposeRuleAddition)ruleChange;
			try {
				logger.info("Adding new rule " + ruleMod.getNewRuleName());
				
				getRuleClassificationService().setActive(ruleMod.getNewRuleName(), true);
				
				timedChanges.add(ruleChange);
				
				addRule(ruleMod.getNewRule());
			} catch (DroolsParserException e) {
				logger.warn("Unable to parse new rule.", e);
			}
		}
		else if (change == RuleChangeType.REMOVAL) {
			ProposeRuleRemoval ruleMod = (ProposeRuleRemoval)ruleChange;
			
			logger.info("Removing old rule " + ruleMod.getOldRuleName());
			
			getRuleClassificationService().setActive(ruleMod.getOldRuleName(), false);
			
			timedChanges.remove(ruleChange);
			
			RemoveRule(ruleMod.getOldRulePackage(), ruleMod.getOldRuleName());
		}*/
		else if (change == RuleChangeType.NONE) {
			logger.info("Blank change for current forecasting has been 'applied'.");
		}
	}
	
	public void addRule(Collection<String> imports, String ruleName,
			Collection<String> conditions, Collection<String> actions)
					throws DroolsParserException {
		String rule = "";
		
		for(String importe : imports) {
			rule += "import " + importe + " ";
		}
		
		rule += "rule \"" + ruleName + "\" ";
		
		rule += "when ";
		
		for (String condition : conditions) {
			rule += condition + " ";
		}
		
		rule += "then ";
		
		for (String action : actions) {
			rule += action + " ";
		}
		
		rule += "end";
		
		addRule(rule);
	}
	
	public void addRule(String rule) throws DroolsParserException {
		
		Collection<KnowledgePackage> packages = parseRule(rule);
		
		session.getKnowledgeBase().addKnowledgePackages(packages);
	}
	
	public void AddRuleFile(String filePath) throws DroolsParserException {
		Collection<KnowledgePackage> packages = parseRuleFile(filePath);
		
		session.getKnowledgeBase().addKnowledgePackages(packages);
	}
	
	public Collection<ProxyAgent> getProxyAgents() {
		Collection<ProxyAgent> proxies = new ArrayList<ProxyAgent>();
		for (NomicAgent agent : agents) {
			proxies.add(agent.getRepresentativeProxy());
		}
		
		return proxies;
	}
	
	/**
	 * Constructs Drools <code>KnowledgePackage</code>s for the parameter rule.
	 * @param rule
	 * @return
	 * @throws DroolsParserException If the rule cannot be parsed.
	 */
	public Collection<KnowledgePackage> parseRule(String rule) throws DroolsParserException {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		
		while (!kBuilderSemaphore.tryAcquire()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				logger.warn("Wait interrupted.", e);
			}
		}
		
		Resource myResource = ResourceFactory.newReaderResource(new StringReader(rule));
		kbuilder.add(myResource, ResourceType.DRL);
		kBuilderSemaphore.release();
		
		
		if (kbuilder.hasErrors()) {
			throw new DroolsParserException("Unable to parse new rule.\n"
					+ kbuilder.getErrors().toString());
		}
		
		return kbuilder.getKnowledgePackages();
	}
	
	/**
	 * Constructs Drools <code>KnowledgePackage</code>s from the rules in the file from the parameter file path.
	 * @param filePath
	 * @return
	 * @throws DroolsParserException
	 */
	public Collection<KnowledgePackage> parseRuleFile(String filePath)
			throws DroolsParserException {
		
		logger.info("Parsing rule file at " + filePath);
		KnowledgeBuilder kBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		
		while (!kBuilderSemaphore.tryAcquire()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				logger.warn("Wait interrupted.", e);
			}
		}
		
		Resource myResource = ResourceFactory.newFileResource(filePath);
		kBuilder.add(myResource, ResourceType.DRL);
		kBuilderSemaphore.release();
		
		if (kBuilder.hasErrors()) {
			throw new DroolsParserException("Unable to parse new rule from file.\n"
					+ filePath + "\n"
					+ kBuilder.getErrors().toString());
		}
		
		return kBuilder.getKnowledgePackages();
	}
	
	public Collection<Rule> getRules() {
		Collection<KnowledgePackage> packages = session.getKnowledgeBase().getKnowledgePackages();
		
		Collection<Rule> rules = null;
		
		for (KnowledgePackage pack : packages) {
			if (rules == null) {
				rules = pack.getRules();
			}
			else {
				rules.addAll(pack.getRules());
			}
		}
		
		return rules;
	}
	
	/**
	 * Creates a new <code>StatefulKnowledgeSession</code> based on the current state of the game,
	 * with all valid rules, facts, and globals for subsimulation execution.
	 * @return
	 */
	public StatefulKnowledgeSession getNewStatefulKnowledgeSession() {
		refreshSession();
		
		StatefulKnowledgeSession newSession = session.getKnowledgeBase().newStatefulKnowledgeSession();
		
		newSession.setGlobal("logger", session.getGlobal("logger"));
		newSession.setGlobal("rand", session.getGlobal("rand"));
		newSession.setGlobal("storage", session.getGlobal("storage"));
		
		for (Object object : session.getObjects())
		{
			if (WantToCopyToNewSession(object))
				newSession.insert(object);
		}
		
		newSession.getAgenda().clear();
		
		return newSession;
	}
	
	private boolean WantToCopyToNewSession(Object object) {
		if (object instanceof NomicAgent)
			return false;
		
		if (object instanceof Turn)
			return false;
		
		if (object instanceof Vote && ((Vote)object).getT() == getTurnNumber())
			return false;
		
		if (object instanceof ProposeRuleChange && ((ProposeRuleChange)object).getT() == getTurnNumber())
			return false;
		
		return true;
	}
	
	public StatefulKnowledgeSession getActiveStatefulKnowledgeSession() {
		return session;
	}
	
	/**
	 * This is not actual Presage2 simulation time, it corresponds instead to phases in the turns of a game of Nomic.
	 * @return
	 */
	public Integer getSimTime() {
		return SimTime;
	}
	
	
	public float getReplenishment() {
		return Replenishment;
	}

	public void setReplenishment(float replenishment) {
		Replenishment = replenishment;
	}

	public Integer getTurnNumber() {
		return currentTurn.getNumber();
	}
	
	public Integer getRoundNumber() {
		return (int) Math.floor(currentTurn.getNumber() / agents.size());
	}
	
	public TurnType getTurnType() {
		return currentTurn.getType();
	}
	
	public Integer getNumberOfAgents() {
		return agents.size();
	}
	
	public ArrayList<NomicAgent> getAgents(){
		return agents;
	}
	
	public Collection<UUID> getAgentIDs() {
		return agentIDs;
	}
	
	public ArrayList<Vote> getSimVotes() {
		return SimVotes;
	}
	
	public ArrayList<ProposeRuleChange> getSimRuleChanges() {
		return SimRuleChanges;
	}
	
	public String getAgentName(UUID pid) {
		for (NomicAgent agent : agents) {
			if (agent.getID() == pid)
				return agent.getName();
		}
		
		return "";
	}
	
/*	public Collection<ProxyAgent> getProxyAgents() {
		Collection<ProxyAgent> proxies = new ArrayList<ProxyAgent>();
		for (NomicAgent agent : agents) {
			proxies.add(agent.getRepresentativeProxy());
		}
		
		return proxies;
	}*/
	
	public Vote getVote(UUID pid) {
		return votesThisTurn.get(pid);
	}
	
	public ArrayList<Vote> getVotesThisTurn() {
		ArrayList<Vote> thisTurn = new ArrayList<Vote>();
		
		for (Vote vote : SimVotes) {
			if (vote.getT() == currentTurn.getNumber())
				thisTurn.add(vote);
		}
		
		return thisTurn;
	}
	
	public ProposeRuleChange getPreviousRuleChange() {
		return previousRuleChange;
	}
	
	public boolean isGameOver() {
		return Loser != null;
	}
	
	public NomicAgent getLoser() {
		return Loser;
	}
	
	public Integer getGameOverTime() {
		return LoseTime;
	}
	
	
	
	public float getResourcePool() {
		return ResourcePool;
	}

	public boolean isActive(String ruleName) {
		for (Rule rule : getRules()) {
			if (rule.getName().equals(ruleName))
				return true;
		}
		
		return false;
	}
	
	public Map<String, Integer> getPointsMap() {
		HashMap<String, Integer> agentsToPoints = new HashMap<String, Integer>();
		for (NomicAgent agent : agents) {
			agentsToPoints.put(agent.getName(), agent.getSequentialID());
		}
		
		return agentsToPoints;
	}

	public ArrayList<simpointschange> getSimpointchanges() {
		return simpointchanges;
	}

	
	
	
}
