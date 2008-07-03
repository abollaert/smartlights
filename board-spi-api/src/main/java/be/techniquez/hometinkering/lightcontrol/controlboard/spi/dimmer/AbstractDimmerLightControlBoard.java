package be.techniquez.hometinkering.lightcontrol.controlboard.spi.dimmer;

import java.util.HashSet;
import java.util.Set;

/**
 * Base implementation of a dimmer control board. Provides some common and easy
 * functionality for the board, such as notifications.
 * 
 * @author alex
 *
 */
public abstract class AbstractDimmerLightControlBoard implements DimmerLightControlBoard {

	/** Set of listeners contained in this control board. */
	private final Set<DimmerLightControllerStateListener> listeners = new HashSet<DimmerLightControllerStateListener>();

	/**
	 * {@inheritDoc}
	 */
	public final void addStateChangeListener(final DimmerLightControllerStateListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void removeStateChangeListener(final DimmerLightControllerStateListener listener) {
		this.listeners.remove(listener);
	}
	
	/**
	 * Notifies the listeners that the light with the given index has been switched
	 * on.
	 * 
	 * @param 	lightIndex	The index of the light that has been switched on.
	 */
	protected final void notifyLightSwitchedOn(final int lightIndex) {
		for (final DimmerLightControllerStateListener listener : this.listeners) {
			listener.lightSwitchedOn(this.getBoardID(), lightIndex);
		}
	}
	
	/**
	 * Notifies the listeners that the light with the given index has been switched
	 * off.
	 * 
	 * @param 	lightIndex	The index of the light that has been switched off.
	 */
	protected final void notifyLightSwitchedOff(final int lightIndex) {
		for (final DimmerLightControllerStateListener listener : this.listeners) {
			listener.lightSwitchedOff(this.getBoardID(), lightIndex);
		}
	}
	
	/**
	 * Notifies all the listeners that the dim percentage of the light with given index
	 * has changed to the given percentage.
	 * 
	 * @param 	lightIndex		The index of the light whose percentage has changed.
	 * @param 	percentage		The percentage the light has been dimmed to.
	 */
	protected final void notifyLightDimPercentageChanged(final int lightIndex, final int percentage) {
		for (final DimmerLightControllerStateListener listener : this.listeners) {
			listener.percentageChanged(this.getBoardID(), lightIndex, percentage);
		}
	}
}
