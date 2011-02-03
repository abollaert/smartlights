package be.abollaert.domotics.light.drivers.tcp;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DefaultDimMoodElement;
import be.abollaert.domotics.light.api.DefaultSwitchMoodElement;
import be.abollaert.domotics.light.api.DimMoodElement;
import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.api.SwitchMoodElement;

/**
 * Client side implementation of a {@link Mood}.
 * 
 * @author alex
 */
final class MoodImpl implements Mood, SupportsPropertyChanges {
	
	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(MoodImpl.class
			.getName());
	
	/**
	 * The name of the mood.
	 */
	private String name;
	
	/** The ID. */
	private int id;
	
	private final List<SwitchMoodElement> switchElements = new ArrayList<SwitchMoodElement>();
	
	private final List<DimMoodElement> dimElements = new ArrayList<DimMoodElement>();
	
	/** The client used to connect to the server. */
	private final TCPClient client;
	
	/** Property change support. */
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	client	The TCP client.
	 */	
	MoodImpl(final TCPClient client) {
		super();
		
		this.client = client;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void activate() throws IOException {
		this.client.activateMood(this.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String getName() {
		return this.name;
	}

	@Override
	public final int getId() {
		return this.id;
	}

	@Override
	public final void addSwitchElement(int moduleId, int channelNumber, ChannelState state) {
		this.switchElements.add(new DefaultSwitchMoodElement(moduleId, channelNumber, state));
	}

	@Override
	public final void addDimElement(int moduleId, int channelNumber, int percentage) {
		this.dimElements.add(new DefaultDimMoodElement(moduleId, channelNumber, percentage));
	}

	@Override
	public List<SwitchMoodElement> getSwitchMoodElements() {
		return this.switchElements;
	}

	@Override
	public final List<DimMoodElement> getDimMoodElements() {
		return this.dimElements;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setName(final String name) {
		this.propertyChangeSupport.firePropertyChange("name", this.name, name);
		this.name = name;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final String toString() {
		return this.name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void remove() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void save() throws IOException {
		try {
			this.id = this.client.saveMood(this);
		} catch (IOException e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "IO error while saving mood : [" + e.getMessage() + "]", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final DimMoodElement getDimmerElementFor(final int moduleId, final int channelNumber) {
		for (final DimMoodElement element : this.dimElements) {
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
		for (final SwitchMoodElement element : this.switchElements) {
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
		for (final Iterator<DimMoodElement> elementIterator = this.dimElements.iterator(); elementIterator.hasNext(); ) {
			final DimMoodElement current = elementIterator.next();
			
			if (current.getChannelNumber() == channelNumber && current.getModuleId() == moduleId) {
				elementIterator.remove();
				break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removeSwitchElement(final int moduleId, final int channelNumber) {
		for (final Iterator<SwitchMoodElement> elementIterator = this.switchElements.iterator(); elementIterator.hasNext(); ) {
			final SwitchMoodElement current = elementIterator.next();
			
			if (current.getChannelNumber() == channelNumber && current.getModuleId() == moduleId) {
				elementIterator.remove();
				break;
			}
		}
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public final void setId(int id) {
		this.id = id;
	}

	@Override
	public final void addPropertyChangeListener(final PropertyChangeListener listener) {
		this.propertyChangeSupport.addPropertyChangeListener(listener);
	}

	@Override
	public final void removePropertyChangeListener(final PropertyChangeListener listener) {
		this.propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
