package be.abollaert.domotics.light.servers.tcp.api;

import java.io.IOException;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.protocolbuffers.Api;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * Handler that fetches the digital output channels. For now the channels are hardcoded, but they should be configurable and fetched from
 * a database.
 * 
 * @author alex
 */
final class GetDigitalOutputChannelsHandler extends AbstractHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Builder createRequestBuilder() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final String getURI() {
		return "/api/GetOutputChannels";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final Message processRequest(final Message request) throws IOException {
		final Api.GetOutputChannelsResponse.Builder builder = Api.GetOutputChannelsResponse.newBuilder();
		
		for (final DigitalModule digitalModule : this.getDriver().getAllDigitalModules()) {
			for (int i = 0; i < digitalModule.getDigitalConfiguration().getNumberOfChannels(); i++) {
				final Api.DigitalOutputChannel.Builder channelBuilder = Api.DigitalOutputChannel.newBuilder();
				channelBuilder.setChannelNumber(i);
				
				// Ouch hardcoded.
				System.out.println("MO: " + digitalModule.getId());
				System.out.println("CH: " + i);
				switch (digitalModule.getId()) {
					case 1: {
						switch (i) {
							case 0:
								channelBuilder.setName("Keuken");
								break;
							case 2:
								channelBuilder.setName("Voordeur");
								break;
							case 4:
								channelBuilder.setName("WC");
								break;
							default:
								channelBuilder.setName("Digital Module [" + digitalModule.getId() + "], Channel " + (i + 1));
								break;
						}
						
						break;
					}
					
					case 2: {
						switch (i) {
						case 4:
							channelBuilder.setName("Gang");
							break;
						case 5:
							channelBuilder.setName("Garage");
							break;
						default:
							channelBuilder.setName("Digital Module [" + digitalModule.getId() + "], Channel " + (i + 1));
							break;
						}
						
						break;
					}
					
					default: {
						channelBuilder.setName("Digital Module [" + digitalModule.getId() + "], Channel " + (i + 1));
						break;
					}
				}
				
				channelBuilder.setModuleId(digitalModule.getId());
				channelBuilder.setCurrentState(digitalModule.getOutputChannelState(i) == ChannelState.ON ? true : false);
				
				builder.addDigitalChannels(channelBuilder.build());
			}
		}
		
		for (final DimmerModule digitalModule : this.getDriver().getAllDimmerModules()) {
			for (int i = 0; i < digitalModule.getDimmerConfiguration().getNumberOfChannels(); i++) {
				final Api.DimmerOutputChannel.Builder channelBuilder = Api.DimmerOutputChannel.newBuilder();
				channelBuilder.setChannelNumber(i);
				channelBuilder.setName("Module [" + digitalModule.getId() + "], Channel " + (i + 1));
				channelBuilder.setModuleId(digitalModule.getId());
				channelBuilder.setCurrentPercentage(digitalModule.getDimmerPercentage(i));
				channelBuilder.setCurrentState(digitalModule.getOutputChannelState(i) == ChannelState.ON ? true : false);
				
				builder.addDimmerChannels(channelBuilder.build());
			}
		}
		
		return builder.build();
	}

}
