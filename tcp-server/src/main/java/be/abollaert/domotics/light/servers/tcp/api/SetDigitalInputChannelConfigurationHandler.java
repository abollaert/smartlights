package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalInputChannelConfiguration;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.protocolbuffers.Api;
import be.abollaert.domotics.light.protocolbuffers.Api.SetDigitalInputConfig;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * Hanlder that is responsible for configuring a digital input channel.
 * 
 * @author alex
 */
final class SetDigitalInputChannelConfigurationHandler extends AbstractHandler {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The URI. */
	private static final String URI = "/api/SetDigitalInputChannelConfig";

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Builder createRequestBuilder() {
		return Api.SetDigitalInputConfig.newBuilder();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final String getURI() {
		return URI;
	}

	@Override
	final Message processRequest(final Message request) throws IOException {
		final SetDigitalInputConfig message = (SetDigitalInputConfig)request;
		
		final DigitalModule module = this.getDriver().getDigitalModuleWithID(message.getModuleId());
		
		if (module == null) {
			return createErrorResponse("No digital module with ID [" + message.getModuleId() + "] is registered on the system.");
		} else {
			final Api.DigitalInputChannelConfig newConfiguration = message.getConfig();
			
			if (newConfiguration == null) {
				return createErrorResponse("No configuration was specified in the message, please specify a configuration.");
			}
			
			final DigitalInputChannelConfiguration configuration = module.getDigitalConfiguration().getDigitalChannelConfiguration(message.getChannelNumber());
			configuration.setDefaultState(newConfiguration.getDefaultState() ? ChannelState.ON : ChannelState.OFF);
			configuration.setMappedOutputChannel(newConfiguration.getMappedOutputChannel());
			configuration.setTimerInSeconds(newConfiguration.getTimerInSec());

			if (message.getConfig().hasName()) {
				configuration.setName(message.getConfig().getName());
			}
			
			if (message.getConfig().hasEnableLogging()) {
				configuration.setLoggingEnabled(message.getConfig().getEnableLogging());
			}
			
			return createOKResponse();
		}
	}

}
