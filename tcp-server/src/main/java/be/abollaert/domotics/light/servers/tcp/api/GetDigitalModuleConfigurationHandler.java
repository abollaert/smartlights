package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;

import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.protocolbuffers.Api;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * Handler that fetches the module configuration.
 * 
 * @author alex
 *
 */
final class GetDigitalModuleConfigurationHandler extends AbstractHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The URI. */
	private static final String URI = "/api/GetDigitalModuleConfiguration";

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Builder createRequestBuilder() {
		return Api.GetDigitalModuleConfig.newBuilder();
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
		final Api.GetDigitalModuleConfig message = (Api.GetDigitalModuleConfig)request;
		final DigitalModule module = this.getDriver().getDigitalModuleWithID(message.getModuleId());
		
		System.out.println(module);
		
		if (module == null) {
			System.out.println("Error");
			return createErrorResponse("No digital module with ID [" + message.getModuleId() + "] is registered on the system.");
		} else {
			final Api.GetDigitalModuleConfigResponse.Builder responseBuilder = Api.GetDigitalModuleConfigResponse.newBuilder();
			
			final Api.DigitalModuleConfig.Builder configBuilder = Api.DigitalModuleConfig.newBuilder();
			configBuilder.setSwitchThresholdInMs(module.getDigitalConfiguration().getSwitchThreshold());
			
			responseBuilder.setConfig(configBuilder);
			
			return responseBuilder.build();
		}
	}
}
