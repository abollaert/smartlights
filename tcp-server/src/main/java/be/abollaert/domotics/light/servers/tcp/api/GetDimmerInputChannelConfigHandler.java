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
 * Handler for dimmer input channel configuration requests.
 * 
 * @author alex
 *
 */
final class GetDimmerInputChannelConfigHandler extends AbstractHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1467857553087727171L;
	/**
	 * The URI.
	 */
	private static final String URI = "/api/GetDimmerInputChannelConfig";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	final Builder createRequestBuilder() {
		return Api.GetDimmerInputChannelConfig.newBuilder();
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
		final Api.GetDimmerInputChannelConfig message = (Api.GetDimmerInputChannelConfig)request;
		
		final DimmerModule module = this.getDriver().getDimmerModuleWithID(message.getModuleId());
		
		if (module == null) {
			final Api.MessageResponse.Builder responseBuilder = Api.MessageResponse.newBuilder();
			
			responseBuilder.setType(Api.MessageResponse.Type.ERROR);
			responseBuilder.setMessage("No dimmer module with ID [" + message.getModuleId() + "] registered on this system.");
			
			return responseBuilder.build();
		}
		
		final DimmerInputChannelConfiguration channelConfiguration = module.getDimmerConfiguration().getDimmerChannelConfiguration(message.getChannelNumber());
		
		if (channelConfiguration == null) {
			final Api.MessageResponse.Builder responseBuilder = Api.MessageResponse.newBuilder();
			
			responseBuilder.setType(Api.MessageResponse.Type.ERROR);
			responseBuilder.setMessage("No channel [" + message.getChannelNumber() + "] found on dimmer module with ID [" + message.getModuleId() + "]  on this system.");
			
			return responseBuilder.build();
		}
		
		final Api.DimmerInputChannelConfig.Builder configBuilder = Api.DimmerInputChannelConfig.newBuilder();
		configBuilder.setDefaultPercentage(channelConfiguration.getDefaultPercentage());
		configBuilder.setDefaultState(channelConfiguration.getDefaultState() == ChannelState.ON ? true : false);
		configBuilder.setMappedOutputChannel(channelConfiguration.getMappedOutputChannel());
		configBuilder.setTimerInSec(channelConfiguration.getTimerInSeconds());
		configBuilder.setCurrentSwitchState(module.getInputChannelState(message.getChannelNumber()) == ChannelState.ON ? true : false);
		configBuilder.setCurrentOutputState(module.getOutputChannelState(channelConfiguration.getMappedOutputChannel()) == ChannelState.ON ? true : false);
		configBuilder.setCurrentDimmerPercentage(module.getDimmerPercentage(message.getChannelNumber()));
		configBuilder.setDefaultDirection(channelConfiguration.getDefaultDirection() == DimmerDirection.UP? true : false);
		
		if (channelConfiguration.getName() != null) {
			configBuilder.setName(channelConfiguration.getName());
		}
		
		configBuilder.setEnableLogging(channelConfiguration.isLoggingEnabled());
		
		final Api.GetDimmerInputChannelConfigResponse.Builder responseBuilder = Api.GetDimmerInputChannelConfigResponse.newBuilder();
		responseBuilder.setConfig(configBuilder);
		
		return responseBuilder.build();
	}

}
