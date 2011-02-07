package be.abollaert.domotics.light.tools.serialdebug;

import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.tools.serialdebug.TreeNode.NodeType;

public final class InstallationTree extends JTree implements ModelListener {
	
	/** Logger definition. */
	private static final Logger logger = Logger
			.getLogger(InstallationTree.class.getName());

	/**
	 * Returns the tree model.
	 * 
	 * @return	The tree model.
	 */
	private final DefaultTreeModel getTreeModel() {
		return (DefaultTreeModel)this.getModel();
	}

	/**
	 *
	 */
	InstallationTree() {
		EventDispatcher.getInstance().addModelListener(this);
	}
	
	/**
	 * Finds the mood node with the given ID.
	 * 
	 * @param 		moodId		The ID of the mood.
	 * 
	 * @return		The corresponding tree node, if any.
	 */
	public final MoodNode findMoodNode(final int moodId) {
		final TreeNode node = (TreeNode)this.getTreeModel().getRoot();
		final Enumeration<?> allNodes = node.breadthFirstEnumeration();
		
		while (allNodes.hasMoreElements()) {
			final TreeNode nextElement = (TreeNode)allNodes.nextElement();
			
			if (nextElement.getType() == NodeType.MOOD) {
				final MoodNode moodNode = (MoodNode)nextElement;
				final Mood mood = (Mood)moodNode.getUserObject();
				
				if (mood.getId() == moodId) {
					return moodNode;
				}
			}
		}
		
		if (logger.isLoggable(Level.WARNING)) {
			logger.log(Level.WARNING, "Could not find a corresponding tree node for mood with ID [" + moodId + "]");
		}
		
		return null;
	}

	@Override
	public final void modelChanged(ModelEvent<?> modelEvent) {
		switch (modelEvent.getModel()) {
			case MOOD: {
				final Mood mood = (Mood)modelEvent.getModelObject();
				
				switch (modelEvent.getType()) {
					case REMOVE: {
						final MoodNode moodNode = this.findMoodNode(mood.getId());
						
						if (moodNode != null) {
							this.getTreeModel().removeNodeFromParent(moodNode);
						}
					
						break;
					}
				}
				
				break;
			}
		}
	}
}
