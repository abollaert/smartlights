package be.techniquez.hometinkering.lightcontrol.dpws.server.services.configuration;

import java.util.List;
import java.util.logging.Logger;

import org.ws4d.java.communication.DPWSException;
import org.ws4d.java.service.PVI;
import org.ws4d.java.service.Parameter;
import org.ws4d.java.service.ParameterType;

import be.techniquez.hometinkering.lightcontrol.dpws.server.services.AbstractDPWSAction;
import be.techniquez.hometinkering.lightcontrol.dpws.server.services.AbstractDPWSService;
import be.techniquez.hometinkering.lightcontrol.model.Board;
import be.techniquez.hometinkering.lightcontrol.model.Light;
import be.techniquez.hometinkering.lightcontrol.services.RepositoryService;

/**
 * Gets the current configuration of the system.
 * 
 * @author alex
 */
public final class GetCurrentConfigurationAction extends AbstractDPWSAction {

	/** Logger instance as it is used in this class. */
	private static final Logger logger = Logger
			.getLogger(GetCurrentConfigurationAction.class.getName());

	/** The name of this action. */
	static final String NAME = "GetCurrentConfiguration";

	/**
	 * Constructs this action.
	 * 
	 * @param actionName
	 *            The name of this action.
	 * @param oneway
	 *            True if the action has no output.
	 * @param service
	 *            The service this action belongs to.
	 * @param repositoryService
	 *            The repo service.
	 */
	protected GetCurrentConfigurationAction(final AbstractDPWSService service,
			final RepositoryService repositoryService) {
		super(NAME, false, service, repositoryService);

		this.addOutputParameterDefinition(this.createReturnTypeParameter());

		service.addAction(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public final void invoke() throws DPWSException {
		logger.info("First getting the configuration of the digital connected boards...");

		final List<Board> allBoards = this.getRepository().allBoards();

		logger.info("Setting up the response.");

		final Parameter rootParameter = this
				.getOutputParameter("CurrentConfiguration");

		final PVI[] pathToNumberOfBoards = PVI.createPath("NumberOfBoards");
		final PVI[] pathToBoardID = PVI.createPath("Board/BoardID");
		final PVI[] pathToBoardType = PVI.createPath("Board/BoardType");
		final PVI[] pathToBoardNumberOfChannels = PVI.createPath("Board/NumberOfChannels");
		final PVI[] pathToBoardDriverName = PVI.createPath("Board/DriverName");
		final PVI[] pathToChannelNumber = PVI.createPath("Board/Channel/ChannelNumber");
		final PVI[] pathToLightName = PVI.createPath("Board/Channel/LightName");
		final PVI[] pathToLightDescription = PVI.createPath("Board/Channel/LightDescription");

		int boardIndex = 0;
		
		pathToNumberOfBoards[0].setIndex(0);
		rootParameter.setValue(pathToNumberOfBoards, String.valueOf(allBoards.size()));
		
		for (final Board board : allBoards) {
			pathToBoardID[0].setIndex(boardIndex);
			pathToBoardID[1].setIndex(0);
			rootParameter.setValue(pathToBoardID, String.valueOf(board.getID()));
			
			pathToBoardType[0].setIndex(boardIndex);
			pathToBoardType[1].setIndex(0);
			rootParameter.setValue(pathToBoardType, board.getType().name());
			
			pathToBoardDriverName[0].setIndex(boardIndex);
			pathToBoardDriverName[1].setIndex(0);
			rootParameter.setValue(pathToBoardDriverName, board.getDriverName());
			
			pathToBoardNumberOfChannels[0].setIndex(boardIndex);
			pathToBoardNumberOfChannels[1].setIndex(0);
			rootParameter.setValue(pathToBoardNumberOfChannels, String.valueOf(board.getNumberOfChannels()));
			
			pathToChannelNumber[0].setIndex(boardIndex);
			pathToLightName[0].setIndex(boardIndex);
			pathToLightDescription[0].setIndex(boardIndex);

			int channelIndex = 0;

			final List<Light> allLightsOnBoard = board.getAllLights();

			for (final Light light : allLightsOnBoard) {
				pathToChannelNumber[1].setIndex(channelIndex);
				pathToChannelNumber[2].setIndex(channelIndex);
				pathToLightName[1].setIndex(channelIndex);
				pathToLightName[2].setIndex(channelIndex);
				pathToLightDescription[1].setIndex(channelIndex);
				pathToLightDescription[2].setIndex(channelIndex);

				rootParameter.setValue(pathToChannelNumber, String
						.valueOf(light.getChannelNumber()));

				rootParameter.setValue(pathToLightName, light.getName() == null ? "" : light.getName());
				rootParameter.setValue(pathToLightDescription, light.getDescription() == null ? "" : light.getDescription());
				
				channelIndex++;
			}

			boardIndex++;
		}
		
		logger.info("Done, invocation was successful.");
	}

	/**
	 * Creates the complex element type that will be returned as a result of
	 * this query.
	 * 
	 * @return The complex element type that will be returned as a result of
	 *         this query.
	 */
	private final Parameter createReturnTypeParameter() {
		// We're going to do this bottom-up...
		
		// Define the type that holds information about one channel...
		final ParameterType channelInfoType = new ParameterType("ChannelInfoType", this.getNamespace());
		channelInfoType.addElement("ChannelNumber", ParameterType.PARAMETER_TYPE_INTEGER, 1, 1);
		channelInfoType.addElement("LightName", ParameterType.PARAMETER_TYPE_STRING, 1, 1);
		channelInfoType.addElement("LightDescription", ParameterType.PARAMETER_TYPE_STRING, 1, 1);

		// We need to have this one included in a type that holds a board...
		final ParameterType boardType = new ParameterType("BoardType", this.getNamespace());
		boardType.addElement("BoardID", ParameterType.PARAMETER_TYPE_INTEGER, 1, 1);
		boardType.addElement("BoardType", ParameterType.PARAMETER_TYPE_STRING, 1, 1);
		boardType.addElement("DriverName", ParameterType.PARAMETER_TYPE_STRING, 1, 1);
		boardType.addElement("NumberOfChannels", ParameterType.PARAMETER_TYPE_INTEGER, 1, 1);
		boardType.addElement("Channel", channelInfoType, 0, -1);
		
		// Assemble the configuration element (root)...
		final ParameterType currentConfigurationType = new ParameterType("BoardConfigurationType", this.getNamespace());
		currentConfigurationType.addElement("NumberOfBoards", ParameterType.PARAMETER_TYPE_INTEGER, 1, 1);
		currentConfigurationType.addElement("Board", boardType, 0, -1);

		final Parameter returnParameter = new Parameter("CurrentConfiguration", this.getNamespace(), currentConfigurationType);
		return returnParameter;
	}
}

