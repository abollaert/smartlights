package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;

import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.protocolbuffers.Api;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * Handler for dimming.
 * 
 * @author alex
 *
 */
final class DimHandler extends AbstractHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The URI. */
	private static final String URI = "/api/Dim";

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Builder createRequestBuilder() {
		return Api.Dim.newBuilder();
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
		final Api.Dim message = (Api.Dim)request;
		
		if (message != null) {
			final DimmerModule dimmerModule = this.getDriver().getDimmerModuleWithID(message.getModuleId());
			
			if (dimmerModule != null) {
				dimmerModule.dim(message.getChannelNumber(), (short)message.getPercentage());
				
				return createOKResponse();
			} else {
				return createErrorResponse("Could not find dimmer module with ID [" + message.getModuleId() + "]");
			}
		} else {
			return createErrorResponse("No message received !");
		}
	}

}
