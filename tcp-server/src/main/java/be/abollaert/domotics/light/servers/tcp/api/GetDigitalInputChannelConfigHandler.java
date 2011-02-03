package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.protocolbuffers.Api;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

final class GetDigitalInputChannelConfigHandler extends AbstractHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Builder createRequestBuilder() {
		return Api.GetDigitalInputChannelConfig.newBuilder();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final String getURI() {
		return "/api/GetDigitalInputChannelConfig";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Message processRequest(final Message message) throws IOException {
		final Api.GetDigitalInputChannelConfig request = (Api.GetDigitalInputChannelConfig)message;
		
		final DigitalModule module = this.getDriver().getDigitalModuleWithID(request.getModuleId());
		
		if (module != null) {
			final Api.DigitalInputChannelConfig.Builder configBuilder = Api.DigitalInputChannelConfig.newBuilder();
			configBuilder.setCurrentSwitchState(module.getInputChannelState(request.getChannelNumber()) == ChannelState.ON);
			
			final int outputChannel = module.getDigitalConfiguration().getDigitalChannelConfiguration(request.getChannelNumber()).getMappedOutputChannel();
			
			configBuilder.setCurrentOutputState(module.getOutputChannelState(outputChannel) == ChannelState.ON);
			configBuilder.setDefaultState(module.getDigitalConfiguration().getDigitalChannelConfiguration(request.getChannelNumber()).getDefaultState() == ChannelState.ON);
			configBuilder.setMappedOutputChannel(outputChannel);
			configBuilder.setTimerInSec(module.getDigitalConfiguration().getDigitalChannelConfiguration(request.getChannelNumber()).getTimerInSeconds());
			
			if (module.getDigitalConfiguration().getDigitalChannelConfiguration(request.getChannelNumber()).getName() != null) {
				configBuilder.setName(module.getDigitalConfiguration().getDigitalChannelConfiguration(request.getChannelNumber()).getName());
			}
 			
			configBuilder.setEnableLogging(module.getDigitalConfiguration().getDigitalChannelConfiguration(request.getChannelNumber()).isLoggingEnabled());
			
			final Api.GetDigitalInputChannelConfigResponse.Builder responseBuilder = Api.GetDigitalInputChannelConfigResponse.newBuilder();
			responseBuilder.setConfig(configBuilder.build());
			
			return responseBuilder.build();
		}
		
		return null;
	}
}
