package be.abollaert.domotics.light.tools.serialdebug;

import javax.swing.tree.DefaultTreeModel;

/**
 * A string tree node.
 * 
 * @author alex
 *
 */
final class StringTreeNode extends TreeNode {

	StringTreeNode(final NodeType type, final String userObject, final DefaultTreeModel model) {
		super(type, userObject, model);
	}

}
