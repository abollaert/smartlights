package be.abollaert.domotics.light.tools.serialdebug;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import be.abollaert.domotics.light.api.Driver;
import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.tools.serialdebug.TreeNode.NodeType;

/**
 * Mouse listener for the tree.
 * 
 * @author alex
 */
abstract class ModuleTreeMouseListener extends MouseAdapter {
	
	/** The popup menu that is shown when the Moods node is clicked. */
	private final JPopupMenu rightClickMoodsPopupMenu = new JPopupMenu();
	
	/** The tree. */
	private final JTree tree;
	
	/** The driver in use. */
	private final Driver driver;
	
	/**
	 * Create a new instance.
	 */
	ModuleTreeMouseListener(final JTree tree, final Driver driver) {
		super();
		
		this.driver = driver;
		this.tree = tree;
		this.rightClickMoodsPopupMenu.add(new JMenuItem(new NewMoodAction()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void mouseClicked(final MouseEvent event) {
		if (event.getButton() == MouseEvent.BUTTON3) {
			this.tree.setSelectionPath(tree.getClosestPathForLocation(event.getX(), event.getY()));
			
			final Object selectedItem = this.tree.getLastSelectedPathComponent();
			
			if (selectedItem != null) {
				if (selectedItem instanceof DefaultMutableTreeNode) {
					final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)selectedItem;
					
					if (selectedNode.getUserObject() != null && selectedNode.getUserObject().equals("Moods")) {
						this.rightClickMoodsPopupMenu.show(event.getComponent(), event.getX(), event.getY());
					}
				}
			}
		}
	}
	
	abstract void showMood(final Mood mood);
	
	private final class NewMoodAction extends AbstractAction {
		
		/**
		 * Create a new instance.
		 */
		private NewMoodAction() {
			super("Create new Mood");
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void actionPerformed(final ActionEvent e) {
			final TreeNode moodsNode = (TreeNode)tree.getLastSelectedPathComponent();
			
			final Mood newMood = driver.getNewMood("New mood");
			
			final TreeNode newMoodNode = new MoodNode(newMood, getTreeModel());
			moodsNode.add(newMoodNode);
			
			getTreeModel().nodesWereInserted(moodsNode, new int[] { getTreeModel().getIndexOfChild(moodsNode, newMoodNode) });
			tree.setSelectionPath(new TreePath(newMoodNode.getPath()));
			
			showMood(newMood);
		}
		
	}
	
	private final DefaultTreeModel getTreeModel() {
		return (DefaultTreeModel)this.tree.getModel();
	}
	
	private final class RemoveMoodAction extends AbstractAction {

		private RemoveMoodAction() {
			super("Remove mood");
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void actionPerformed(ActionEvent e) {
		}
		
	}
}
