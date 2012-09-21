package be.abollaert.domotics.light.driver.simulated;

import java.io.IOException;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalInputChannelConfiguration;

public final class SimulatedDigitalChannelConfiguration implements DigitalInputChannelConfiguration {

	private int mappedOutputChannel;
	private int timerInSeconds;
	private ChannelState defaultState;
	private final int moduleId;
	private String name;
	private boolean loggingEnabled;
	private final int channelNumber;
	
	SimulatedDigitalChannelConfiguration(final int moduleId, final int channel) {
		this.moduleId = moduleId;
		this.channelNumber = channel;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getMappedOutputChannel() throws IOException {
		return this.mappedOutputChannel;
	}

	@Override
	public void setMappedOutputChannel(int outputChannel) throws IOException {
		this.mappedOutputChannel = outputChannel;
	}

	@Override
	public int getTimerInSeconds() throws IOException {
		return this.timerInSeconds;
	}

	@Override
	public void setTimerInSeconds(int seconds) throws IOException {
		this.timerInSeconds = seconds;
	}

	@Override
	public ChannelState getDefaultState() throws IOException {
		return this.defaultState;
	}

	@Override
	public void setDefaultState(ChannelState state) throws IOException {
		this.defaultState = state;
	}

	@Override
	public final int getModuleId() {
		return this.moduleId;
	}

	@Override
	public final int getChannelNumber() {
		return this.channelNumber;
	}

	@Override
	public final String getName() {
		return this.name;
	}

	@Override
	public boolean isLoggingEnabled() {
		return this.loggingEnabled;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setLoggingEnabled(boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}

}
