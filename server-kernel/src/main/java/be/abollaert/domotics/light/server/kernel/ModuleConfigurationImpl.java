package be.abollaert.domotics.light.server.kernel;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.DigitalInputChannelConfiguration;
import be.abollaert.domotics.light.api.DigitalModuleConfiguration;
import be.abollaert.domotics.light.api.DimmerInputChannelConfiguration;
import be.abollaert.domotics.light.api.DimmerModuleConfiguration;
import be.abollaert.domotics.light.server.kernel.ProtocolParser.RequestPDU;
import be.abollaert.domotics.light.server.kernel.ProtocolParser.ResponsePDU;
import be.abollaert.domotics.light.server.kernel.persistence.Storage;

/**
 * Digital module configuration implementation.
 * 
 * @author alex
 */
final class ModuleConfigurationImpl implements DigitalModuleConfiguration, DimmerModuleConfiguration {
	
	/** Logger instance. */
	private static final Logger logger = Logger
			.getLogger(ModuleConfigurationImpl.class.getName());
	
	/** The serial channel. */
	private final Channel channel;
	
	/** The input channel configuration. */
	private InputChannelConfigurationImpl[] inputChannelConfigurations;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	channel	The serial channel.
	 */
	ModuleConfigurationImpl(final Channel channel, final Storage storage) throws IOException {
		this.channel = channel;
		
		final int numberOfChannels = this.getNumberOfChannels();
		this.inputChannelConfigurations = new InputChannelConfigurationImpl[6];
		
		for (int i = 0; i < numberOfChannels; i++) {
			this.inputChannelConfigurations[i] = new InputChannelConfigurationImpl(i, this.getModuleId(), this.channel, storage);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getFirmwareVersion() throws IOException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("PDU: GetFirmwareVersion");
		}
		
		ResponsePDU response = this.channel.sendCommand(new RequestPDU(RequestPDU.Type.GET_FW_VERSION));
		
		if (response.getType() == ResponsePDU.Type.OK) {
			if (response.getArguments().length == 3) {
				final StringBuilder builder = new StringBuilder("v. ");
				builder.append(response.getArguments()[0]).append(".");
				builder.append(response.getArguments()[1]).append(".");
				builder.append(response.getArguments()[2]);
				
				return builder.toString();
			}
		} else {
			throw new IOException("Response length was [" + response.getArguments().length + "] while I was expecting 1 argument !");
		}
		
		return "Unknown";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getModuleId() throws IOException {
		ResponsePDU response = this.channel.sendCommand(new RequestPDU(RequestPDU.Type.GET_ID));
			
		if (response.getArguments().length == 1) {
			return response.getArguments()[0];
		} else {
			throw new IOException("Response length was [" + response.getArguments().length + "] while I was expecting 1 argument !");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getNumberOfChannels() throws IOException {
		final RequestPDU request = new RequestPDU(RequestPDU.Type.GET_NR_CHANNELS, new int[0]);
		final ResponsePDU response = this.channel.sendCommand(request);
		
		if (response != null) {
			if (response.getType() == ResponsePDU.Type.OK) {
				if (response.getArguments().length == 1) {
					return response.getArguments()[0];
				}
			}
		}
		
		return -1;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void save() throws IOException {
		final RequestPDU request = new RequestPDU(RequestPDU.Type.SAVE_CONFIGURATION, new int[0]);
		final ResponsePDU response = this.channel.sendCommand(request);
		
		if (response != null) {
			if (response.getType() == ResponsePDU.Type.ERROR) {
			}
		}
	}

	/**
	 * @return the switchThreshold
	 */
	public final int getSwitchThreshold() throws IOException {
		final RequestPDU requestPDU = new RequestPDU(RequestPDU.Type.GET_SW_THRESHOLD, new int[0]);
		final ResponsePDU response = this.channel.sendCommand(requestPDU);
		
		if (response != null) {
			if (response.getType() == ResponsePDU.Type.OK) {
				if (response.getArguments().length == 1) {
					return response.getArguments()[0];
				}
			}
		}
		
		return -1;
	}

	/**
	 * @param switchThreshold the switchThreshold to set
	 */
	public final void setSwitchThreshold(int switchThreshold) throws IOException {
		final RequestPDU requestPDU = new RequestPDU(RequestPDU.Type.SET_SW_THRESHOLD, new int[] { switchThreshold });
		final ResponsePDU responsePDU = this.channel.sendCommand(requestPDU);
		
		if (responsePDU != null) {
			if (responsePDU.getType() == ResponsePDU.Type.ERROR) {
				throw new IOException("Response indicated an error, error code [" + responsePDU.getArguments()[0] + "]");
			}
		}
	}
	
	/***
	 * {@inheritDoc}
	 */
	public final String toString() {
		return "Module configuration.";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final DigitalInputChannelConfiguration getDigitalChannelConfiguration(final int channelNumber) {
		return this.inputChannelConfigurations[channelNumber];
	}

	@Override
	public int getDimmerDelay() throws IOException {
		final RequestPDU requestPDU = new RequestPDU(RequestPDU.Type.GET_DIMMER_DELAY, new int[0]);
		final ResponsePDU response = this.channel.sendCommand(requestPDU);
		
		if (response != null) {
			if (response.getType() == ResponsePDU.Type.OK) {
				if (response.getArguments().length == 1) {
					return response.getArguments()[0];
				}
			}
		}
		
		return -1;
	}

	@Override
	public int getDimmerThreshold() throws IOException {
		final RequestPDU requestPDU = new RequestPDU(RequestPDU.Type.GET_DIMMER_THRESHOLD, new int[0]);
		final ResponsePDU response = this.channel.sendCommand(requestPDU);
		
		if (response != null) {
			if (response.getType() == ResponsePDU.Type.OK) {
				if (response.getArguments().length == 1) {
					return response.getArguments()[0];
				}
			}
		}
		
		return -1;
	}

	@Override
	public final void setDimmerDelay(int dimmerDelay) throws IOException {
		final RequestPDU requestPDU = new RequestPDU(RequestPDU.Type.SET_DIMMER_DELAY, new int[] { dimmerDelay });
		final ResponsePDU responsePDU = this.channel.sendCommand(requestPDU);
		
		if (responsePDU != null) {
			if (responsePDU.getType() == ResponsePDU.Type.ERROR) {
				throw new IOException("Response indicated an error, error code [" + responsePDU.getArguments()[0] + "]");
			}
		}
	}

	@Override
	public void setDimmerThreshold(int dimmerThreshold) throws IOException {
		final RequestPDU requestPDU = new RequestPDU(RequestPDU.Type.SET_DIMMER_THRESHOLD, new int[] { dimmerThreshold });
		final ResponsePDU responsePDU = this.channel.sendCommand(requestPDU);
		
		if (responsePDU != null) {
			if (responsePDU.getType() == ResponsePDU.Type.ERROR) {
				throw new IOException("Response indicated an error, error code [" + responsePDU.getArguments()[0] + "]");
			}
		}
	}

	@Override
	public DimmerInputChannelConfiguration getDimmerChannelConfiguration(int channelNumber) {
		return this.inputChannelConfigurations[channelNumber];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setModuleId(final int moduleId) throws IOException {
		final RequestPDU requestPDU = new RequestPDU(RequestPDU.Type.SET_MODULE_ID, new int[] { moduleId });
		final ResponsePDU responsePDU = this.channel.sendCommand(requestPDU);
		
		if (responsePDU != null) {
			if (responsePDU.getType() == ResponsePDU.Type.ERROR) {
				throw new IOException("Response indicated an error, error code [" + responsePDU.getArguments()[0] + "]");
			}
		}
	}

}
