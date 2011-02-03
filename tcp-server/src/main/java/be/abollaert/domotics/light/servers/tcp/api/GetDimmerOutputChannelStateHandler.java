package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.protocolbuffers.Api;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * Handler that gets the output state.
 * 
 * @author alex
 */
final class GetDimmerOutputChannelStateHandler extends AbstractHandler {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The URL. */
	private static final String URI = "/api/GetDimmerOutputState";

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
		
		final DimmerModule module = this.getDriver().getDimmerModuleWithID(message.getModuleId());
		
		if (module != null) {
			final ChannelState state = module.getOutputChannelState(message.getChannelNumber());
			
			final Api.DimmerChannelOutputState.Builder responseBuilder = Api.DimmerChannelOutputState.newBuilder();
			responseBuilder.setModuleId(message.getModuleId());
			responseBuilder.setChannelNumber(message.getChannelNumber());
			responseBuilder.setState(state == ChannelState.ON);
			responseBuilder.setPercentage(module.getDimmerPercentage(message.getChannelNumber()));
			
			return responseBuilder.build();
		} else {
			throw new IllegalStateException("No module found with ID [" + message.getModuleId() + "]");
		}
	}

}
