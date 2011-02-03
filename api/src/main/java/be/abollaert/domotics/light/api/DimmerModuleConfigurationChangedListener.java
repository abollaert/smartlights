package be.abollaert.domotics.light.api;

/**
 * Listener for module configuration changes.
 * 
 * @author alex
 */
public interface DimmerModuleConfigurationChangedListener {

	/**
	 * Called when the configuration of a digital module changes.
	 * 
	 * @param 	moduleId		The ID of the module.
	 */
	void dimmerModuleConfigurationChanged(final int moduleId);
}
