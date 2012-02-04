package be.abollaert.domotics.light.server.kernel;


import java.io.IOException;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalInputChannelConfiguration;
import be.abollaert.domotics.light.api.DimmerDirection;
import be.abollaert.domotics.light.api.DimmerInputChannelConfiguration;
import be.abollaert.domotics.light.driver.base.Channel;
import be.abollaert.domotics.light.driver.base.RequestPDU;
import be.abollaert.domotics.light.driver.base.RequestPDU.Type;
import be.abollaert.domotics.light.driver.base.ResponsePDU;
import be.abollaert.domotics.light.server.kernel.persistence.Storage;
import be.abollaert.domotics.light.server.kernel.persistence.StoredChannelConfiguration;


/**
 * Input channel config.
 * 
 * @author alex
 *
 */
final class InputChannelConfigurationImpl implements DigitalInputChannelConfiguration, DimmerInputChannelConfiguration {

	private final int channelNumber;
	
	private final Channel channel;
	
	/** The module ID. */
	private final int moduleId;
	
	/** The storage to be used. */
	private final Storage storage;
	
	InputChannelConfigurationImpl(final int channelNumber, final int moduleId, final Channel channel, final Storage storage) {
		this.channelNumber = channelNumber;
		this.channel = channel;
		this.moduleId = moduleId;
		this.storage = storage;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getMappedOutputChannel() throws IOException {
		final RequestPDU requestPDU = new RequestPDU(Type.GET_CHANNEL_MAPPING, new int[] { channelNumber });
		final ResponsePDU response = this.channel.sendCommand(requestPDU);
		
		if (response.getType() == ResponsePDU.Type.OK) {
			if (response.getArguments().length == 1) {
				return response.getArguments()[0];
			}
		}
		
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getTimerInSeconds() throws IOException {
		final RequestPDU requestPDU = new RequestPDU(Type.GET_SW_TIMER, new int[] { channelNumber });
		final ResponsePDU response = this.channel.sendCommand(requestPDU);
		
		if (response.getType() == ResponsePDU.Type.OK) {
			if (response.getArguments().length == 1) {
				return response.getArguments()[0];
			}
		}
		
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setMappedOutputChannel(final int outputChannel) throws IOException {
		final RequestPDU requestPDU = new RequestPDU(RequestPDU.Type.SET_CHANNEL_MAPPING, new int[] { this.channelNumber, outputChannel });
		final ResponsePDU responsePDU = this.channel.sendCommand(requestPDU);
		
		if (responsePDU != null) {
			if (responsePDU.getType() == ResponsePDU.Type.ERROR) {
				throw new IOException("Response indicated an error, error code [" + responsePDU.getArguments()[0] + "]");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setTimerInSeconds(final int seconds) throws IOException {
		final RequestPDU requestPDU = new RequestPDU(RequestPDU.Type.SET_SW_TIMER, new int[] { this.channelNumber, seconds });
		final ResponsePDU responsePDU = this.channel.sendCommand(requestPDU);
		
		if (responsePDU != null) {
			if (responsePDU.getType() == ResponsePDU.Type.ERROR) {
				throw new IOException("Response indicated an error, error code [" + responsePDU.getArguments()[0] + "]");
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final String toString() {
		final StringBuilder builder = new StringBuilder();
		//builder.append("Mapped output channel : ").append(this.mappedOutputChannel).append(", ").append("Timer : ").append(this.timerInSeconds);
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ChannelState getDefaultState() throws IOException {
		final RequestPDU request = new RequestPDU(RequestPDU.Type.GET_DEFAULT_STATE, new int [] { this.channelNumber });
		final ResponsePDU response = this.channel.sendCommand(request);
		
		if (response != null) {
			if (response.getType() == ResponsePDU.Type.OK) {
				if (response.getArguments().length == 1) {
					return (response.getArguments()[0] == 1 ? ChannelState.ON : ChannelState.OFF);
				}
			}
		}
		
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setDefaultState(ChannelState state) throws IOException {
		final RequestPDU request = new RequestPDU(RequestPDU.Type.SET_DEFAULT_STATE, new int [] { this.channelNumber, state.getState() });
		final ResponsePDU response = this.channel.sendCommand(request);
		
		if (response != null) {
			if (response.getType() == ResponsePDU.Type.ERROR) {
				throw new IOException("Error while setting default state, error code [" + response.getArguments()[0] + "]");
			}
		} else {
			throw new IOException("No response received when setting default state.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final DimmerDirection getDefaultDirection() throws IOException {
		final RequestPDU request = new RequestPDU(RequestPDU.Type.GET_DEFAULT_DIMMER_DIRECTION, new int [] { this.channelNumber });
		final ResponsePDU response = this.channel.sendCommand(request);
		
		if (response != null) {
			if (response.getType() == ResponsePDU.Type.OK) {
				if (response.getArguments().length == 1) {
					return (response.getArguments()[0] == 1 ? DimmerDirection.UP : DimmerDirection.DOWN);
				}
			}
		}
		
		return null;
	}

	@Override
	public int getDefaultPercentage() throws IOException {
		final RequestPDU request = new RequestPDU(RequestPDU.Type.GET_DEFAULT_PERCENTAGE, new int [] { this.channelNumber });
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

	@Override
	public final void setDefaultDirection(DimmerDirection direction) throws IOException {
		final RequestPDU request = new RequestPDU(RequestPDU.Type.SET_DEFAULT_DIMMER_DIRECTION, new int [] { this.channelNumber, direction == DimmerDirection.DOWN ? 0 : 1 });
		final ResponsePDU response = this.channel.sendCommand(request);
		
		if (response != null) {
			if (response.getType() == ResponsePDU.Type.ERROR) {
				throw new IOException("Error while setting default state, error code [" + response.getArguments()[0] + "]");
			}
		} else {
			throw new IOException("No response received when setting default state.");
		}
	}

	@Override
	public void setDefaultPercentage(int percentage) throws IOException {
		final RequestPDU request = new RequestPDU(RequestPDU.Type.SET_DEFAULT_PERCENTAGE, new int [] { this.channelNumber, percentage });
		final ResponsePDU response = this.channel.sendCommand(request);
		
		if (response != null) {
			if (response.getType() == ResponsePDU.Type.ERROR) {
				throw new IOException("Error while setting default state, error code [" + response.getArguments()[0] + "]");
			}
		} else {
			throw new IOException("No response received when setting default state.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getChannelNumber() {
		return this.channelNumber;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getModuleId() {
		return this.moduleId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getName() {
		final StoredChannelConfiguration configuration = this.storage.loadChannelConfiguration(this.moduleId, this.channelNumber);
		
		if (configuration != null) {
			return configuration.getName();
		}
		
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isLoggingEnabled() {
		final StoredChannelConfiguration configuration = this.storage.loadChannelConfiguration(this.moduleId, this.channelNumber);
		
		if (configuration != null) {
			return configuration.isLoggingEnabled();
		}
		
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setLoggingEnabled(final boolean loggingEnabled) {
		final StoredChannelConfiguration configuration = new StoredChannelConfiguration(this.getName(), loggingEnabled);
		this.storage.saveChannelConfiguration(this.moduleId, this.channelNumber, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setName(final String name) {
		final StoredChannelConfiguration configuration = new StoredChannelConfiguration(name, this.isLoggingEnabled());
		this.storage.saveChannelConfiguration(this.moduleId, this.channelNumber, configuration);
	}
}
