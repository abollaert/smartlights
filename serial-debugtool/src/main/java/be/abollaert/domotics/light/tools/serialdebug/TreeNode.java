package be.abollaert.domotics.light.tools.serialdebug;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import be.abollaert.domotics.light.drivers.tcp.SupportsPropertyChanges;

/**
 * Re-defines a tree node.
 * 
 * @author alex
 */
abstract class TreeNode extends DefaultMutableTreeNode implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Enumerate the node types. */
	enum NodeType {
		ROOT,
		DETECTED_DEVICES,
		DIGITAL_DEVICES,
		DIGITAL_DEVICE,
		DIMMER_DEVICES,
		DIMMER_DEVICE,
		MOODS,
		MOOD;
		
	};
	
	/** The type of node. */
	private final NodeType type;
	
	private final DefaultTreeModel treeModel;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	type			The type.
	 * @param 	userObject		The user object.
	 */
	TreeNode(final NodeType type, final Object userObject, final DefaultTreeModel model) {
		super(userObject);
		
		this.type = type;
		this.treeModel = model;
		
		if (userObject instanceof SupportsPropertyChanges) {
			System.out.println("Add property listener");
			((SupportsPropertyChanges) userObject).addPropertyChangeListener(this);
		}
	}
	
	final NodeType getType() {
		return this.type;
	}
	
	/**
	 * Default implementation does nothing, meant to be overridden.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		System.out.println(evt);
	}
	
	final DefaultTreeModel getTreeModel() {
		return this.treeModel;
	}
}
