package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DimmerModule;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * Switches all the lights off.
 * 
 * @author alex
 */
final class AllLightsOffHandler extends AbstractHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3091904843337184481L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Message processRequest(final Message request) throws IOException {
		for (final DigitalModule digitalModule : this.getDriver().getAllDigitalModules()) {
			for (int channel = 0; channel < digitalModule.getDigitalConfiguration().getNumberOfChannels(); channel++) {
				digitalModule.switchOutputChannel(channel, ChannelState.OFF);
			}
		}
		
		for (final DimmerModule dimmerModule : this.getDriver().getAllDimmerModules()) {
			for (int channel = 0; channel < dimmerModule.getDimmerConfiguration().getNumberOfChannels(); channel++) {
				dimmerModule.switchOutputChannel(channel, ChannelState.OFF);
			}
		}
		
		return createOKResponse();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final String getURI() {
		return "/api/AllLightsOff";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Builder createRequestBuilder() {
		return null;
	}

}
