package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;

import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.api.DimmerModuleConfiguration;
import be.abollaert.domotics.light.protocolbuffers.Api;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * Handler that can save a dimmer module configuration.
 * 
 * @author alex
 */
final class SetDimmerModuleConfigHandler extends AbstractHandler {

	/** Serial version UID. */
	private static final long serialVersionUID = 1L;
	
	/** The URI. */
	private static final String URI = "/api/SetDimmerModuleConfig";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	final Builder createRequestBuilder() {
		return Api.SetDimmerModuleConfig.newBuilder();
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
		final Api.SetDimmerModuleConfig message = (Api.SetDimmerModuleConfig)request;
		
		final DimmerModule module = this.getDriver().getDimmerModuleWithID(message.getModuleId());
		
		if (module == null) {
			final Api.MessageResponse.Builder responseBuilder = Api.MessageResponse.newBuilder();
			
			responseBuilder.setType(Api.MessageResponse.Type.ERROR);
			responseBuilder.setMessage("Could not find a dimmer module with ID [" + message.getModuleId() + "]");
		
			return responseBuilder.build();
		}
		
		final DimmerModuleConfiguration configuration = module.getDimmerConfiguration();
		configuration.setDimmerDelay(message.getConfiguration().getDimmerDelay());
		configuration.setDimmerThreshold(message.getConfiguration().getDimmerThresholdInMs());
		configuration.setSwitchThreshold(message.getConfiguration().getSwitchThresholdInMs());
	
		final Api.MessageResponse.Builder responseBuilder = Api.MessageResponse.newBuilder();
		responseBuilder.setType(Api.MessageResponse.Type.OK);
		
		return responseBuilder.build();
	}
}
