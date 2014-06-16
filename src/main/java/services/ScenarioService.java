package services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.drools.definition.KnowledgePackage;
import org.drools.runtime.StatefulKnowledgeSession;

import simulations.SubScenarioSimulation;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBusModule;
import uk.ac.imperial.presage2.core.participant.Participant;
import actions.ProposeRuleChange;
import agents.NomicAgent;
import agents.ProxyAgent;

import com.google.inject.AbstractModule;

import facts.RuleDefinition;

/**
 * Participant environment service, each agent has a separate instance of this service.
 * Most frequently used function is RunQuerySimulation.
 * @author Stuart
 *
 */
public class ScenarioService extends EnvironmentService {
	
	class SimResults {
		Integer SubID;
		Integer SubLength;
		Integer AvatarPreference;
		
		public SimResults(Integer subID, Integer subLength,
				Integer avatarPreference) {
			this.SubID = subID;
			this.SubLength = subLength;
			this.AvatarPreference = avatarPreference;
		}

		public Integer getSubID() {
			return SubID;
		}

		public void setSubID(Integer subID) {
			SubID = subID;
		}

		public Integer getSubLength() {
			return SubLength;
		}

		public void setSubLength(Integer subLength) {
			SubLength = subLength;
		}

		public Integer getAvatarPreference() {
			return AvatarPreference;
		}

		public void setAvatarPreference(Integer avatarPreference) {
			AvatarPreference = avatarPreference;
		}
	}
	
	private final Logger logger = Logger.getLogger(this.getClass());
	
	final private EnvironmentServiceProvider serviceProvider;
	
	private NomicService superNomicService;
	
	
	private StatefulKnowledgeSession testSession;
	
	private SubScenarioSimulation subScenarioSimulation;
	
	private NomicAgent controller;
	
	private ProxyAgent avatar;
	
	private ArrayList<SimResults> SubSimulationResults;
	
	private ArrayList<ProxyAgent> currentProxies = new ArrayList<ProxyAgent>();

	private RuleClassificationService superClassificationService;

	public ScenarioService(EnvironmentSharedStateAccess ss, EnvironmentServiceProvider provider,
			Participant p) {
		super(ss);
		this.serviceProvider = provider;
		
		if (p instanceof NomicAgent) {
			controller = (NomicAgent)p;
		}
		
		SubSimulationResults = new ArrayList<ScenarioService.SimResults>();
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
	
	public Collection<ProxyAgent> getProxyAgents() {
		Collection<ProxyAgent> proxies = superNomicService.getProxyAgents();
		
		currentProxies.clear();
		
		for (ProxyAgent proxy : proxies) {
			if (proxy.GetOwnerID() == controller.getID()) {
				avatar = proxy;
				avatar.SetAvatar(true);
			}
			
			currentProxies.add(proxy);
		}
		
		return proxies;
	}
	
	/**
	 * Runs a subsimulation of length timeIntoFuture, applying the rule change ruleChange at the beginning
	 * of the simulation. The subsimulation's initial state will be based on the state of the supersimulation.
	 * @param ruleChange
	 * @param timeIntoFuture
	 * @throws IOException 
	 */
	public void RunQuerySimulation(ProposeRuleChange ruleChange, int timeIntoFuture) throws IOException {
		testSession = getSuperNomicService().getNewStatefulKnowledgeSession();
		
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
		
		modules.add(new EventBusModule());
		
		subScenarioSimulation = new SubScenarioSimulation(modules, this, ruleChange);
		subScenarioSimulation.finishTime = timeIntoFuture;
		
		subScenarioSimulation.load();
		
		subScenarioSimulation.run();
		
//		logger.info("Super service rules.");
//		
//		for (Rule rule : getSuperNomicService().getRules()) {
//			logger.info(rule.getName());
//		}
//		
//		logger.info("Sub service rules.");
//		
//		for (Rule rule : getSubNomicService().getRules()) {
//			logger.info(rule.getName());
//		}
		
		//logger.info("Super session kbase sessions: ");
		
		testSession.dispose();
		
		getSuperNomicService().refreshSession();
		
		avatar.setPreferenceLocked(false);
		
		if (avatar.getPreference() > 100)
			avatar.setPreference(100);
		else if (avatar.getPreference() < 0)
			avatar.setPreference(0);
		
		avatar.setPreferenceLocked(true);
		
		SimResults results = new SimResults(SubSimulationResults.size(), 
				subScenarioSimulation.getSimulationFinishTime().intValue(),
				avatar.getPreference());
		
		SubSimulationResults.add(results);
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
	
	/**
	 * Returns true if the most recently run subsimulation had a winner.
	 * @return
	 */
	public boolean isSimWon() {
		for (ProxyAgent proxy : currentProxies) {
			if (proxy.isWinner()) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Returns the proxy agent who won the most recently run subsimulation.
	 * 
	 * Returns null if the most recent subsimulation had no winner.
	 * @return
	 */
	public ProxyAgent getWinner() {
		for (ProxyAgent proxy : currentProxies) {
			if (proxy.isWinner()) {
				return proxy;
			}
		}
		
		return null;
	}
	
	public ProxyAgent getAvatar() {
		return avatar;
	}
	
	public Integer getPreference() {
		return avatar.getPreference();
	}
	
	public Map<String, Integer> getPointsAtEnd() {
		Map<String, Integer> pointsMap = new HashMap<String, Integer>();
		
		for (ProxyAgent proxy : currentProxies) {
			pointsMap.put(proxy.getName(), (int) proxy.getPoints());
		}
		
		return pointsMap;
	}

	public int getNumSubSimsRun() {
		return SubSimulationResults.size();
	}
	
	public int getAverageSubSimLength() {
		int length = 0;
		
		if (SubSimulationResults.size() == 0)
			return 0;
		
		for (SimResults results : SubSimulationResults) {
			length += results.getSubLength();
		}
		length /= SubSimulationResults.size();
		return length;
	}



	public RuleClassificationService getSuperClassificationService() {
		if (superClassificationService == null) {
			try {
				superClassificationService = serviceProvider.getEnvironmentService(RuleClassificationService.class);
			} catch (UnavailableServiceException e) {
				logger.warn("Unable to get superClassificationService for " + controller.getName(), e);
			}
		}
		
		return superClassificationService;
	}
}
