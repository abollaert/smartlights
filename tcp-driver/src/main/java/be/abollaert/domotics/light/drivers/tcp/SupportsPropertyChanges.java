package be.abollaert.domotics.light.drivers.tcp;

import java.beans.PropertyChangeListener;

/**
 * Implemented by models who support property changes.
 * 
 * @author alex
 *
 */
public interface SupportsPropertyChanges {
	void addPropertyChangeListener(final PropertyChangeListener listener);
	void removePropertyChangeListener(final PropertyChangeListener listener);
}
