package be.abollaert.domotics.light.tools.serialdebug;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.api.Mood;


/**
 * The tree listener for the module tree.
 * 
 * @author alex
 *
 */
abstract class ModuleTreeListener implements TreeSelectionListener {

	/** Logger instance. */
	private static final Logger logger = Logger
			.getLogger(ModuleTreeListener.class.getName());
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void valueChanged(final TreeSelectionEvent event) {
		if (event.getPath() != null) {
			final TreeNode selectedNode = (TreeNode)event.getPath().getLastPathComponent();
			
			switch (selectedNode.getType()) {
				case DIGITAL_DEVICE: {
					final DigitalModule selectedModule = (DigitalModule)selectedNode.getUserObject();
					
					if (logger.isLoggable(Level.FINE)) {
						logger.log(Level.FINE, "Tree : Digital module selected, ID [" + selectedModule.getId() + "]");
					}
					
					if (logger.isLoggable(Level.INFO)) {
						logger.log(Level.INFO, "Show digital module.");
					}
					
					this.showDigitalModule(selectedModule);
					
					break;
				}
				
				case DIMMER_DEVICE: {
					final DimmerModule selectedModule = (DimmerModule)selectedNode.getUserObject();
					
					if (logger.isLoggable(Level.FINE)) {
						logger.log(Level.FINE, "Tree : Digital module selected, ID [" + selectedModule.getId() + "]");
					}
					
					if (logger.isLoggable(Level.INFO)) {
						logger.log(Level.INFO, "Show dimmer module.");
					}
			
					this.showDimmerModule(selectedModule);
					
					break;
				}
				
				case MOOD: {
					final Mood modelObject = (Mood)selectedNode.getUserObject();
					
					this.showMood(modelObject);
					
					break;
				}
				
				default: {
					showNone();
					
					break;
				}
			}
		} else {
			this.showNone();
		}
	}
	
	/**
	 * Shows the digital module.
	 * 
	 * @param 	module		The module.
	 */
	abstract void showDigitalModule(final DigitalModule module);
	
	/**
	 * Shows the digital module.
	 * 
	 * @param 	module		The module.
	 */
	abstract void showDimmerModule(final DimmerModule module);
	
	/**
	 * Show nothing.
	 */
	abstract void showNone();
	
	/**
	 * Show a mood.
	 * 
	 * @param 	mood		The mood to show.
	 */
	abstract void showMood(final Mood mood);
}
