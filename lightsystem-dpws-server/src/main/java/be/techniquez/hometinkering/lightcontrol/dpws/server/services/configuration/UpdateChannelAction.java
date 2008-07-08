package be.techniquez.hometinkering.lightcontrol.dpws.server.services.configuration;

import java.util.logging.Logger;

import org.ws4d.java.communication.DPWSException;
import org.ws4d.java.service.ParameterType;

import be.techniquez.hometinkering.lightcontrol.dpws.server.services.AbstractDPWSAction;
import be.techniquez.hometinkering.lightcontrol.dpws.server.services.AbstractDPWSService;
import be.techniquez.hometinkering.lightcontrol.services.RepositoryService;

/**
 * Updates a channel with the new light information.
 * 
 * @author alex
 *
 */
public final class UpdateChannelAction extends AbstractDPWSAction {
	
	/** Logger instance used in this class. */
	private static final Logger logger = Logger.getLogger(UpdateChannelAction.class.getName());
	
	/** Name of the action. */
	static final String NAME = "UpdateChannel";

	/**
	 * Creates a new instance of this action.
	 * 			
	 * @param 	service
	 * @param 	repositoryService
	 */
	public UpdateChannelAction(final AbstractDPWSService service, final RepositoryService repositoryService) {
		super(NAME, true, service, repositoryService);
		
		this.addInputParameter("boardId", ParameterType.PARAMETER_TYPE_INTEGER);
		this.addInputParameter("channelNumber", ParameterType.PARAMETER_TYPE_INTEGER);
		this.addInputParameter("lightName", ParameterType.PARAMETER_TYPE_STRING);
		this.addInputParameter("lightDescription", ParameterType.PARAMETER_TYPE_STRING);
		
		service.addAction(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void invoke() throws DPWSException {
		final int boardId = Integer.valueOf(this.getInputParameter("boardId").getValue());
		final int channelNumber = Integer.valueOf(this.getInputParameter("channelNumber").getValue());
		final String lightName = this.getInputParameter("lightName").getValue();
		final String lightDescription = this.getInputParameter("lightDescription").getValue();
		
		logger.info("Updating channel [" + channelNumber + "] of board [" + boardId + "] to light name [" + lightName + "], description [" + lightDescription + "]");
		
		this.getRepository().updateChannel(boardId, channelNumber, lightName, lightDescription);
		
		logger.info("Done, update finished...");
	}

}
