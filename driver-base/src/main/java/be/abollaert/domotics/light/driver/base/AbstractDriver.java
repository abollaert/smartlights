package be.abollaert.domotics.light.driver.base;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.api.Driver;

public abstract class AbstractDriver implements Driver {

	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(AbstractDriver.class
			.getName());
	
	/** The digital modules. */
	private final Set<DigitalModule> digitalModules = new HashSet<DigitalModule>();
	
	/** The dimmer modules. */
	private final Set<DimmerModule> dimmerModules = new HashSet<DimmerModule>();
	
	protected final void addDigitalModule(final DigitalModule module) {
		this.digitalModules.add(module);
	}
	
	protected final void addDimmerModule(final DimmerModule module) {
		this.dimmerModules.add(module);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Set<DigitalModule> getAllDigitalModules() {
		return this.digitalModules;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Set<DimmerModule> getAllDimmerModules() {
		return this.dimmerModules;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract void probe() throws IOException;
	
	protected abstract List<Channel> searchChannels() throws IOException;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final DigitalModule getDigitalModuleWithID(final int id) {
		for (final DigitalModule module : this.digitalModules) {
			if (module.getId() == id) {
				return module;
			}
		}
		
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final DimmerModule getDimmerModuleWithID(int id) {
		for (final DimmerModule module : this.dimmerModules) {
			if (module.getId() == id) {
				return module;
			}
		}
		
		return null;
	}
}
