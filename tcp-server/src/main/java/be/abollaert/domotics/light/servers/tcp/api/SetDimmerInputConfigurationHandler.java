package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DimmerDirection;
import be.abollaert.domotics.light.api.DimmerInputChannelConfiguration;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.protocolbuffers.Api;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * Handler for setting the dimmer input configuration.
 * 
 * @author alex
 */
final class SetDimmerInputConfigurationHandler extends AbstractHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The URI. */
	private static final String URI = "/api/SetDimmerInputConfiguration";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	final Builder createRequestBuilder() {
		return Api.SetDimmerInputConfig.newBuilder();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final String getURI() {
		return URI;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Message processRequest(final Message request) throws IOException {
		final Api.SetDimmerInputConfig message = (Api.SetDimmerInputConfig)request;
		
		final DimmerModule module = this.getDriver().getDimmerModuleWithID(message.getModuleId());
		
		if (module != null) {
			final DimmerInputChannelConfiguration configuration = module.getDimmerConfiguration().getDimmerChannelConfiguration(message.getChannelNumber());
			
			configuration.setDefaultDirection(message.getConfig().getDefaultDirection()? DimmerDirection.UP : DimmerDirection.DOWN);
			configuration.setDefaultPercentage(message.getConfig().getDefaultPercentage());
			configuration.setDefaultState(message.getConfig().getDefaultState() ? ChannelState.ON : ChannelState.OFF);
			configuration.setMappedOutputChannel(message.getConfig().getMappedOutputChannel());
			configuration.setTimerInSeconds(message.getConfig().getTimerInSec());
			
			if (message.getConfig().hasName()) {
				configuration.setName(message.getConfig().getName());
			}
			
			if (message.getConfig().hasEnableLogging()) {
				configuration.setLoggingEnabled(message.getConfig().getEnableLogging());
			}
			
			return createOKResponse();
		} else {
			return createErrorResponse("Cannot find a dimmer module with ID [" + message.getModuleId() + "]");
		}
	}

}
