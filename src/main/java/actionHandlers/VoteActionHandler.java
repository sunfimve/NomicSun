package actionHandlers;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;

import services.NomicService;
import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandler;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import actions.TimeStampedAction;
import actions.Vote;

import com.google.inject.Inject;

import exceptions.NoExistentRuleChangeException;

/**
 * Handler for voting actions, informs the nomic service and Drools knowledge session of votes that have occurred.
 * 
 * Also timestamps votes for later storage.
 * @author Stuart Holland
 *
 */
public class VoteActionHandler implements ActionHandler {

	StatefulKnowledgeSession session;
	private final Logger logger = Logger.getLogger(ProposeRuleChangeActionHandler.class);
	final EnvironmentServiceProvider serviceProvider;
	
	NomicService nomicService;
	
	@Inject
	public VoteActionHandler(EnvironmentServiceProvider serviceProvider) {
		super();
		this.serviceProvider = serviceProvider;
	}
	
	public NomicService getNomicService() {
		if (nomicService == null) {
			try {
				nomicService = serviceProvider.getEnvironmentService(NomicService.class);
				session = nomicService.getActiveStatefulKnowledgeSession();
			} catch (UnavailableServiceException e) {
				logger.warn("Unable to get NomicService.");
			}
		}
		return nomicService;
	}
	
	@Override
	public boolean canHandle(Action action) {
		return action instanceof Vote;
	}

	@Override
	public Input handle(Action action, UUID actor)
			throws ActionHandlingException {
		
		NomicService service = getNomicService();
		
		if (action instanceof TimeStampedAction) {
			((TimeStampedAction) action).setT(service.getTurnNumber());
			((TimeStampedAction) action).setSimTime(service.getSimTime());
		}
		
		try {
			Vote vote = (Vote)action;
			logger.info("Handling vote " + vote.getVote() + " from " + vote.getVoter().getName() + " at time " + nomicService.getSimTime());
			service.Vote(vote);
			session.insert(vote);
		} catch(ClassCastException e) {
			throw new ActionHandlingException("Supplied action is the wrong class." + e.getMessage());
		} catch (NoExistentRuleChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

}
