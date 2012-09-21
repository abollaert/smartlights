package be.abollaert.domotics.light.driver.simulated;

import java.io.IOException;

import be.abollaert.domotics.light.api.DigitalInputChannelConfiguration;
import be.abollaert.domotics.light.api.DigitalModuleConfiguration;

/**
 * In memory configuration.
 * 
 * @author alex
 */
final class SimulatedDigitalModuleConfiguration implements DigitalModuleConfiguration {

	/** The ID of the parent module. */
	private int moduleId;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	moduleId		The ID of the parent module.
	 */	
	SimulatedDigitalModuleConfiguration(final int moduleId) {
		this.moduleId = moduleId;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getModuleId() throws IOException {
		return this.moduleId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setModuleId(final int moduleId) throws IOException {
		this.moduleId = moduleId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getFirmwareVersion() throws IOException {
		return "0.0.99";
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getNumberOfChannels() throws IOException {
		return 6;
	}

	@Override
	public final int getSwitchThreshold() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public final void setSwitchThreshold(final int switchThreshold) throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void save() throws IOException {
	}

	@Override
	public DigitalInputChannelConfiguration getDigitalChannelConfiguration(
			int channelNumber) {
		// TODO Auto-generated method stub
		return null;
	}

}
