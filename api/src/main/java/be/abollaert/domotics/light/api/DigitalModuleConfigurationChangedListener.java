package be.abollaert.domotics.light.api;

/**
 * Listener for module configuration changes.
 * 
 * @author alex
 */
public interface DigitalModuleConfigurationChangedListener {

	/**
	 * Called when the configuration of a digital module changes.
	 * 
	 * @param 	moduleId		The ID of the module.
	 */
	void digitalModuleConfigurationChanged(final int moduleId);
}
