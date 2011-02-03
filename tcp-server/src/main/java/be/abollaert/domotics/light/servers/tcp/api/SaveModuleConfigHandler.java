package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;

import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.protocolbuffers.Api;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * Saves the digital module configuration.
 * 
 * @author alex
 */
final class SaveModuleConfigHandler extends AbstractHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The URI. */
	private static final String URI = "/api/SaveModuleConfig";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	final Builder createRequestBuilder() {
		return Api.SaveDigitalModuleConfig.newBuilder();
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
		final Api.SaveDigitalModuleConfig message = (Api.SaveDigitalModuleConfig)request;
		final DigitalModule module = this.getDriver().getDigitalModuleWithID(message.getModuleId());
		
		if (module == null) {
			final DimmerModule dimmerModule = this.getDriver().getDimmerModuleWithID(message.getModuleId());
			
			if (dimmerModule == null) {
				return createErrorResponse("No module with ID [" + message.getModuleId() + "] is registered on the system.");
			} else {
				dimmerModule.getDimmerConfiguration().save();
				
				return createOKResponse();
			}
		} else {
			module.getDigitalConfiguration().save();
			
			return createOKResponse();
		}
	}
}
