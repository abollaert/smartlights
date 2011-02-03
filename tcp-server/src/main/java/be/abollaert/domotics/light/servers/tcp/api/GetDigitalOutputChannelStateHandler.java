package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.protocolbuffers.Api;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * Handler that gets the output state.
 * 
 * @author alex
 */
final class GetDigitalOutputChannelStateHandler extends AbstractHandler {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The URL. */
	private static final String URI = "/api/GetDigitalOutputState";

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Builder createRequestBuilder() {
		return Api.GetOutputChannelState.newBuilder();
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
		final Api.GetOutputChannelState message = (Api.GetOutputChannelState)request;
		
		final DigitalModule module = this.getDriver().getDigitalModuleWithID(message.getModuleId());
		
		if (module != null) {
			final ChannelState state = module.getOutputChannelState(message.getChannelNumber());
			
			final Api.DigitalChannelOutputState.Builder responseBuilder = Api.DigitalChannelOutputState.newBuilder();
			responseBuilder.setModuleId(message.getModuleId());
			responseBuilder.setChannelNumber(message.getChannelNumber());
			responseBuilder.setState(state == ChannelState.ON);
			
			return responseBuilder.build();
		} else {
			throw new IllegalStateException("No module found with ID [" + message.getModuleId() + "]");
		}
	}

}
