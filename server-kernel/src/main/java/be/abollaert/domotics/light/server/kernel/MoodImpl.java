package be.abollaert.domotics.light.server.kernel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DefaultDimMoodElement;
import be.abollaert.domotics.light.api.DefaultSwitchMoodElement;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DimMoodElement;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.api.Driver;
import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.api.SwitchMoodElement;
import be.abollaert.domotics.light.server.kernel.persistence.Storage;
import be.abollaert.domotics.light.server.kernel.persistence.StoredMoodInfo;

/**
 * Server side {@link Mood} implementation.
 * 
 * @author alex
 */
final class MoodImpl implements Mood {
	
	/** Logger instance. */
	public static final Logger logger = Logger.getLogger(MoodImpl.class.getName());
	
	/** The driver. */
	private final Driver driver;
	
	/** The storage. */
	private final Storage storage;
	
	/** The name. */
	private String name;
	
	/** The ID. */
	private int id;
	
	/** The switch mood elements. */
	private final List<SwitchMoodElement> switchMoodElements = new ArrayList<SwitchMoodElement>();
	
	/** The dimmer mood elements. */
	private final List<DimMoodElement> dimMoodElements = new ArrayList<DimMoodElement>();
	
	/**
	 * Create a new mood.
	 * 
	 * @param 	driver		The driver.
	 */
	MoodImpl(final int id, final String name, final Driver driver, final Storage storage) {
		this.driver = driver;
		this.storage = storage;
		this.id = id;
		this.name = name;
		
		this.loadFromStorage();
	}
	
	/**
	 * Loads the mood from storage.
	 */
	private final void loadFromStorage() {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Loading mood from storage.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void activate() throws IOException {
		for (final SwitchMoodElement switchMoodElement : this.switchMoodElements) {
			((SwitchMoodElementImpl)switchMoodElement).activate();
		}
		
		for (final DimMoodElement dimMoodElement : this.dimMoodElements) {
			((DimMoodElementImpl)dimMoodElement).activate();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void addDimElement(int moduleId, int channelNumber, int percentage) {
		this.dimMoodElements.add(new DimMoodElementImpl(moduleId, channelNumber, percentage));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addSwitchElement(int moduleId, int channelNumber, ChannelState state) {
		this.switchMoodElements.add(new SwitchMoodElementImpl(moduleId, channelNumber, state));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final List<DimMoodElement> getDimMoodElements() {
		return this.dimMoodElements;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getId() {
		return this.id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getName() {
		return this.name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final List<SwitchMoodElement> getSwitchMoodElements() {
		return this.switchMoodElements;
	}
	
	private final class SwitchMoodElementImpl extends DefaultSwitchMoodElement {

		/**
		 * Create a new instance.
		 * 
		 * @param moduleId
		 * @param channelNumber
		 * @param state
		 */
		private SwitchMoodElementImpl(final int moduleId, final int channelNumber, final ChannelState state) {
			super(moduleId, channelNumber, state);
		}

		/**
		 * {@inheritDoc}
		 */
		private final void activate() throws IOException {
			final DigitalModule digitalModule = driver.getDigitalModuleWithID(this.getModuleId());
			
			if (digitalModule == null) {
				final DimmerModule dimmerModule = driver.getDimmerModuleWithID(this.getModuleId());
				
				if (dimmerModule == null) {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "Could not find a module with ID [" + this.getModuleId() + "], not activating anything.");
					}
				} else {
					dimmerModule.switchOutputChannel(this.getChannelNumber(), this.getRequestedState());
				}
			} else {
				digitalModule.switchOutputChannel(this.getChannelNumber(), this.getRequestedState());
			}
		}
	}
	
	private final class DimMoodElementImpl extends DefaultDimMoodElement {

		private DimMoodElementImpl(int moduleId, int channelNumber, int targetPercentage) {
			super(moduleId, channelNumber, targetPercentage);
		}

		private final void activate() throws IOException {
			final DimmerModule module = driver.getDimmerModuleWithID(this.getModuleId());
			
			if (module == null) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "Dimmer mood element : Cannot find dimmer module with ID [" + this.getModuleId() + "]");
				}
			} else {
				module.switchOutputChannel(this.getChannelNumber(), ChannelState.ON);
				module.dim(this.getChannelNumber(), (short)this.getTargetPercentage());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setName(final String name) {
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void remove() {
		this.storage.removeMood(this.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void save() {
		final StoredMoodInfo info = this.storage.saveMoodInformation(this.getId(), this.getName());
		
		this.id = info.getId();
		
		for (final SwitchMoodElement switchElement : this.getSwitchMoodElements()) {
			this.storage.saveMoodSwitchElement(this.getId(), switchElement.getModuleId(), switchElement.getChannelNumber(), switchElement.getRequestedState());
		}
		
		for (final DimMoodElement moodElement : this.getDimMoodElements()) {
			this.storage.saveMoodDimElement(this.getId(), moodElement.getModuleId(), moodElement.getChannelNumber(), moodElement.getTargetPercentage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final DimMoodElement getDimmerElementFor(final int moduleId, final int channelNumber) {
		for (final DimMoodElement element : this.dimMoodElements) {
			if (element.getModuleId() == moduleId && element.getChannelNumber() == channelNumber) {
				return element;
			}
		}
		
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final SwitchMoodElement getSwitchElementFor(final int moduleId, final int channelNumber) {
		for (final SwitchMoodElement element : this.switchMoodElements) {
			if (element.getModuleId() == moduleId && element.getChannelNumber() == channelNumber) {
				return element;
			}
		}
		
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removeDimmerElement(final int moduleId, final int channelNumber) {
		for (final Iterator<DimMoodElement> elementIterator = this.dimMoodElements.iterator(); elementIterator.hasNext(); ) {
			final DimMoodElement element = elementIterator.next();
			
			if (element.getModuleId() == moduleId && element.getChannelNumber() == channelNumber) {
				elementIterator.remove();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removeSwitchElement(final int moduleId, final int channelNumber) {
		for (final Iterator<SwitchMoodElement> elementIterator = this.switchMoodElements.iterator(); elementIterator.hasNext(); ) {
			final SwitchMoodElement element = elementIterator.next();
			
			if (element.getModuleId() == moduleId && element.getChannelNumber() == channelNumber) {
				elementIterator.remove();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setId(final int id) {
		this.id = id;
	}
}
