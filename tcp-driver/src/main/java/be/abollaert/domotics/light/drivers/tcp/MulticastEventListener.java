package be.abollaert.domotics.light.drivers.tcp;

import be.abollaert.domotics.light.protocolbuffers.Eventing;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DigitalInputChannelStateChanged;
import be.abollaert.domotics.light.protocolbuffers.Eventing.DigitalOutputChannelStateChanged;

/**
 * Implemented by instances who want to receive events from the multicast channel.
 * 
 * @author alex
 *
 */
public interface MulticastEventListener {

	/**
	 * Called when a digital input channel state has changed.
	 * 
	 * @param 	event		The event.
	 */
	void digitalInputChannelStateChanged(final DigitalInputChannelStateChanged event);
	
	/**
	 * Called when a digital module configuration changed.
	 * 
	 * @param 		event		The event.
	 */
	void digitalModuleConfigurationChanged(final Eventing.DigitalModuleConfigurationChanged event);
	
	/**
	 * Called when a dimmer module configuration changed.
	 * 
	 * @param 		event		The event.
	 */
	void dimmerModuleConfigurationChanged(final Eventing.DimmerModuleConfigurationChanged event);
	
	/**
	 * Called when the state of a digital output channel has changed.
	 * 
	 * @param 	event		The event.
	 */
	void digitalOutputChannelStateChanged(final DigitalOutputChannelStateChanged event);
	
	/**
	 * Called when a dimmer output has changed.
	 * 
	 * @param 	event		The event.
	 */
	void dimmerOutputStateChanged(final Eventing.DimmerOutputChannelStateChanged event);
	
	/**
	 * Called when a dimmer input has changed.
	 * 
	 * @param 	event		The event.
	 */
	void dimmerInputStateChanged(final Eventing.DimmerInputChannelStateChanged event);
}
