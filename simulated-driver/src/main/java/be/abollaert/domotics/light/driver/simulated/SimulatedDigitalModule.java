package be.abollaert.domotics.light.driver.simulated;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalChannelStateChangeListener;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DigitalModuleConfiguration;
import be.abollaert.domotics.light.api.DigitalModuleConfigurationChangedListener;
import be.abollaert.domotics.light.api.SwitchEvent;

final class SimulatedDigitalModule implements DigitalModule {

	@Override
	public DigitalModuleConfiguration getDigitalConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ChannelState getOutputChannelState(int channelNumber)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChannelState getInputChannelState(int channelNumber)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void switchOutputChannel(int channelNumber, ChannelState desiredState)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addChannelStateListener(
			DigitalChannelStateChangeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeChannelStateListener(
			DigitalChannelStateChangeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addModuleConfigurationListener(
			DigitalModuleConfigurationChangedListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeModuleConfigurationListener(
			DigitalModuleConfigurationChangedListener listener) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getPortName() {
		return "SIM";
	}

	@Override
	public List<SwitchEvent> getSwitchEvents(int channelNumber, Date startDate,
			Date endDate) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
