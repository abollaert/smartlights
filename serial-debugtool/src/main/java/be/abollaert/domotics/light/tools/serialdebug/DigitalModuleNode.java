package be.abollaert.domotics.light.tools.serialdebug;

import java.io.IOException;

import javax.swing.tree.DefaultTreeModel;

import be.abollaert.domotics.light.api.DigitalModule;


/**
 * A digital module node.
 * 
 * @author alex
 */
final class DigitalModuleNode extends TreeNode {
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	module		The module.
	 */
	DigitalModuleNode(final DigitalModule module, final DefaultTreeModel model) {
		super(NodeType.DIGITAL_DEVICE, module, model);
		
		this.setAllowsChildren(false);
	}
	
	/**
	 * Returns the module.
	 * 
	 * @return	The module.
	 */
	private final DigitalModule getModule() {
		return (DigitalModule)this.getUserObject();
	}
	
	public final String toString() {
		try {
			final StringBuilder builder = new StringBuilder();
			
			builder.append("Digital device [").append(this.getModule().getId()).append("], v. [").append(this.getModule().getDigitalConfiguration().getFirmwareVersion()).append("]");
			
			return builder.toString();
		} catch (IOException e) {
			return "Error while getting information from module.";
		}
	}
}
