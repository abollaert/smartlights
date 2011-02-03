package be.abollaert.domotics.light.tools.serialdebug;

import java.io.IOException;

import javax.swing.tree.DefaultTreeModel;

import be.abollaert.domotics.light.api.DimmerModule;


/**
 * A digital module node.
 * 
 * @author alex
 */
final class DimmerModuleNode extends TreeNode {
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	module		The module.
	 */
	DimmerModuleNode(final DimmerModule module, final DefaultTreeModel model) {
		super(NodeType.DIMMER_DEVICE, module, model);
	}
	
	/**
	 * Returns the module.
	 * 
	 * @return	The module.
	 */
	private final DimmerModule getModule() {
		return (DimmerModule)this.getUserObject();
	}
	
	public final String toString() {
		try {
			final StringBuilder builder = new StringBuilder();
			
			builder.append("Dimmer device [").append(this.getModule().getId()).append("], v. [").append(this.getModule().getDimmerConfiguration().getFirmwareVersion()).append("]");
			
			return builder.toString();
		} catch (IOException e) {
			return "Error while getting information from module.";
		}
	}
}
