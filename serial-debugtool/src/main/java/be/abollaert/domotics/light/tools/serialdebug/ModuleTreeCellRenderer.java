package be.abollaert.domotics.light.tools.serialdebug;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Tree cell renderer for the module tree.
 * 
 * @author alex
 */
final class ModuleTreeCellRenderer extends DefaultTreeCellRenderer {

	/** The arduino icon. */
	private static final ImageIcon ARDUINO_ICON = new ImageIcon(ModuleTreeCellRenderer.class.getResource("/img/icon/arduino_small.png"));

	/** The server icon. */
	private static final ImageIcon SERVER_ICON = new ImageIcon(ModuleTreeCellRenderer.class.getResource("/img/icon/server.png"));

	private static final ImageIcon SWITCH_ICON = new ImageIcon(ModuleTreeCellRenderer.class.getResource("/img/icon/switch.png"));

	/** The world. */
	private static final ImageIcon WORLD_ICON = new ImageIcon(ModuleTreeCellRenderer.class.getResource("/img/icon/world.png"));
	
	/** The world. */
	private static final ImageIcon MOOD_ICON = new ImageIcon(ModuleTreeCellRenderer.class.getResource("/img/icon/mood.png"));
	
	/** The world. */
	private static final ImageIcon MOODS_ICON = new ImageIcon(ModuleTreeCellRenderer.class.getResource("/img/icon/moods.png"));
	
	/**
	 * {@inheritDoc}
	 */
	public final Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		if (value != null && value instanceof TreeNode) {
			final TreeNode selectedNode = (TreeNode)value;
			
			switch (selectedNode.getType()) {
				case DETECTED_DEVICES: {
					this.setIcon(SERVER_ICON);
					break;
				}
				
				case ROOT: {
					this.setIcon(WORLD_ICON);
					break;
				}
				
				case DIGITAL_DEVICE: {
					this.setIcon(ARDUINO_ICON);
					break;
				}
				
				case DIMMER_DEVICE: {
					this.setIcon(ARDUINO_ICON);
					break;
				}
				
				case DIMMER_DEVICES: {
					this.setIcon(SWITCH_ICON);
					break;
				}
				
				case DIGITAL_DEVICES: {
					this.setIcon(SWITCH_ICON);
					break;
				}
				
				case MOOD: {
					this.setIcon(MOOD_ICON);
					break;
				}
				
				case MOODS: {
					this.setIcon(MOODS_ICON);
					break;
				}
			}
		}

		return this;
	}
}
