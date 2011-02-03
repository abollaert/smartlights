package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.protocolbuffers.Api;
import be.abollaert.domotics.light.protocolbuffers.Api.SwitchOutput;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * Handler to switch an output.
 * 
 * @author alex
 */
final class SwitchOutputHandler extends AbstractHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Builder createRequestBuilder() {
		return Api.SwitchOutput.newBuilder();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final String getURI() {
		return "/api/SwitchOutput";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Message processRequest(final Message request) throws IOException {
		final SwitchOutput message = (SwitchOutput)request;
		
		final DigitalModule digitalModule = this.getDriver().getDigitalModuleWithID(message.getModuleId());
		
		if (digitalModule != null) {
			digitalModule.switchOutputChannel(message.getChannelNumber(), message.getRequiredState() ? ChannelState.ON : ChannelState.OFF);
		} else {
			final DimmerModule dimmerModule = this.getDriver().getDimmerModuleWithID(message.getModuleId());
			
			if (dimmerModule != null) {
				dimmerModule.switchOutputChannel(message.getChannelNumber(), message.getRequiredState() ? ChannelState.ON : ChannelState.OFF);
			}
		}
		
		final Api.MessageResponse.Builder responseBuilder = Api.MessageResponse.newBuilder();
		responseBuilder.setType(Api.MessageResponse.Type.OK);
		
		return responseBuilder.build();
	}

}
