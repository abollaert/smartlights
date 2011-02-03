package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;

import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.protocolbuffers.Api;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * Handler for the setting of a digital module configuration.
 * 
 * @author alex
 */
final class SetDigitalModuleConfigHandler extends AbstractHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The URI for this action. */
	private static final String URI = "/api/SetDigitalModuleConfig";
	
	/**
	 * Create a new builder.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	final Builder createRequestBuilder() {
		return Api.SetDigitalModuleConfig.newBuilder();
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
		final Api.SetDigitalModuleConfig message = (Api.SetDigitalModuleConfig)request;
		
		final DigitalModule module = this.getDriver().getDigitalModuleWithID(message.getModuleId());
		
		if (module == null) {
			return createErrorResponse("No digital module with ID [" + message.getModuleId() + "] is registered on the system.");
		} else {
			final Api.DigitalModuleConfig newConfiguration = message.getConfiguration();
			
			module.getDigitalConfiguration().setSwitchThreshold(newConfiguration.getSwitchThresholdInMs());
			
			return createOKResponse();
		}
	}

}
