package actionHandlers;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.compiler.DroolsParserException;
import org.drools.runtime.StatefulKnowledgeSession;

import services.NomicService;
import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandler;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import actions.ProposeRuleAddition;
import actions.ProposeRuleChange;
import actions.ProposeRuleModification;
import actions.ProposeRuleRemoval;
import actions.TimeStampedAction;

import com.google.inject.Inject;

import enums.RuleChangeType;
import exceptions.InvalidRuleProposalException;

/**
 * Action handler for all rule change proposals. Informs the nomic service and the current Drools
 * knowledge session about the proposal that has been received.
 * 
 * Also time stamps the action for storage later.
 * @author Stuart Holland
 *
 */
public class ProposeRuleChangeActionHandler implements ActionHandler {
	
	StatefulKnowledgeSession session;
	private final Logger logger = Logger.getLogger(ProposeRuleChangeActionHandler.class);
	final EnvironmentServiceProvider serviceProvider;
	
	NomicService nomicService;
	
	@Inject
	public ProposeRuleChangeActionHandler(EnvironmentServiceProvider serviceProvider) {
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
		return action instanceof ProposeRuleChange;
	}

	@Override
	public Input handle(Action action, UUID actor)
			throws ActionHandlingException {
		
		logger.info("Handling action: " + action);
		
		NomicService service = getNomicService();
		
		if (action instanceof TimeStampedAction) {
			((TimeStampedAction) action).setT(service.getTurnNumber());
			((TimeStampedAction) action).setSimTime(service.getSimTime());
		}
		
		try {
			service.ProposeRuleChange((ProposeRuleChange)action);
			
			if (((ProposeRuleChange)action).getRuleChangeType() != RuleChangeType.NONE)
				session.insert(action);
		} catch(ClassCastException e) {
			throw new ActionHandlingException("Supplied action is the wrong class." + e.getMessage());
		} catch(InvalidRuleProposalException e) {
			throw new ActionHandlingException("It is not time to propose rule changes now.\n" 
					+ e.getMessage());
		}
		return null;
	}

}
