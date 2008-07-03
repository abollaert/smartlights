package be.techniquez.hometinkering.lightcontrol.controlboard.spi.digital;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base implementation of a digital board, it takes care of some common issues such as taking
 * care of the callback update listeners.
 * 
 * @author alex
 */
public abstract class AbstractDigitalControlBoard implements DigitalLightControlBoard {

	/** Contains the set of listeners that are notified when the state of something on the board changes. */
	private final Set<DigitalLightControllerStateListener> listeners = new HashSet<DigitalLightControllerStateListener>();
	
	/**
	 * {@inheritDoc}
	 */
	public final void addStateEventListener(final DigitalLightControllerStateListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void removeStateEventListener(final DigitalLightControllerStateListener listener) {
		this.listeners.remove(listener);
	}
	
	/**
	 * Notifies listeners that light with the given index has been switched on.
	 * 
	 * @param 	lightIndex	The index of the light that has been switched on.
	 */
	protected final void notifyLightSwitchedOn(final int lightIndex) {
		for (final DigitalLightControllerStateListener listener : this.listeners) {
			listener.lightSwitchedOn(this.getBoardID(), lightIndex);
		}
	}
	
	/**
	 * Notifies all listeners that light has been switched off.
	 * 
	 * @param 	lightIndex	The index of the light that has been switched off.
	 */
	protected final void notifyLightSwitchedOff(final int lightIndex) {
		for (final DigitalLightControllerStateListener listener : this.listeners) {
			listener.lightSwitchedOff(this.getBoardID(), lightIndex);
		}
	}
}
