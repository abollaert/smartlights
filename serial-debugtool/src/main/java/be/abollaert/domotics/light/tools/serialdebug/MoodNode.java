package be.abollaert.domotics.light.tools.serialdebug;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.tree.DefaultTreeModel;

import be.abollaert.domotics.light.api.Mood;

/**
 * Node in the tree that represents a {@link Mood}.
 * 
 * @author alex
 *
 */
final class MoodNode extends TreeNode {

	/**
	 * Create a new instance.
	 * 
	 * @param 	userObject		The user object.
	 */
	MoodNode(Object userObject, final DefaultTreeModel model) {
		super(NodeType.MOOD, userObject, model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void propertyChange(final PropertyChangeEvent evt) {
		if (evt.getPropertyName() != null && evt.getPropertyName().equals("name")) {
			this.getTreeModel().nodesChanged(this.getParent(), new int[] { this.getParent().getIndex(this) });
		}
	}
}
