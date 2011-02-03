package be.abollaert.domotics.light.tools.serialdebug;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.api.Driver;
import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.drivers.tcp.TCPDriver;
import be.abollaert.domotics.light.tools.serialdebug.TreeNode.NodeType;

/**
 * Main class.
 * 
 * @author alex
 */
public final class Main {

	/**
	 * The main frame.
	 * 
	 * @author alex
	 *
	 */
	private static final class MainFrame extends JFrame {
		
		/** Logger instance. */
		private static final Logger logger = Logger.getLogger(Main.MainFrame.class.getName());
		
		/** The serial driver. */
		private final Driver driver;
		
		/** The JSplitPane. */
		private final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		private JTree tree;
		
		/** The empty panel. */
		private final JPanel holderPanel = new JPanel();
		
		/** The module panel. */
		private final DigitalModulePanel modulePanel = new DigitalModulePanel(this);
		
		/** A dimmer module panel. */
		private final DimmerModulePanel dimmerModulePanel = new DimmerModulePanel(this);
		
		private final MoodPanel moodPanel;
		
		/**
		 * Create a new instance.
		 */
		private MainFrame() {
			this.setTitle("Smartlights control center");
			this.setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/img/icon/smartlights.png")));
			
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			//this.driver = new SerialDriver();
			this.driver = new TCPDriver();
			this.moodPanel = new MoodPanel(driver);
			
			this.setLayout(new BorderLayout());
			
			try {
				this.driver.probe();
				this.createTree();
			
				final JPanel treePanel = new JPanel();
				treePanel.setLayout(new GridLayout(1, 1));
				treePanel.add(new JScrollPane(this.tree));
				
				final Dimension treeMinimumSize = new Dimension(400, treePanel.getPreferredSize().height);
				treePanel.setMinimumSize(treeMinimumSize);
				
				this.splitPane.add(treePanel);
				
				this.holderPanel.setLayout(new GridLayout(1, 1));
				this.splitPane.add(new JScrollPane(this.holderPanel));
				
				this.splitPane.setOneTouchExpandable(true);
				this.add(this.splitPane, BorderLayout.CENTER);
				
				this.pack();
				this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
			} catch (IOException e) {
				if (logger.isLoggable(Level.SEVERE)) {
					logger.log(Level.SEVERE, "Cannot initialize due to an IO error [" + e.getMessage() + "]", e);
				}
				
				System.exit(1);
			}
		}
		
		/**
		 * Creates the tree.
		 * 
		 * @throws 		IOException		If an IO error is thrown.
		 */
		private final void createTree() throws IOException {
			final TreeNode rootNode = new StringTreeNode(NodeType.ROOT, "This installation", null);
			
			this.tree = new JTree();
			
			final TreeNode devicesNode = new StringTreeNode(NodeType.DETECTED_DEVICES, "Detected devices", this.getTreeModel());
			final TreeNode digitalModulesNode = new StringTreeNode(NodeType.DIGITAL_DEVICES, "Digital Modules", this.getTreeModel());
			
			for (final DigitalModule module : this.driver.getAllDigitalModules()) {
				digitalModulesNode.add(new DigitalModuleNode(module, this.getTreeModel()));
			}
			
			devicesNode.add(digitalModulesNode);
			
			final TreeNode dimmerModulesNode = new StringTreeNode(NodeType.DIMMER_DEVICES, "Dimmer devices", this.getTreeModel());
			
			for (final DimmerModule module : this.driver.getAllDimmerModules()) {
				dimmerModulesNode.add(new DimmerModuleNode(module, this.getTreeModel()));
			}
			
			devicesNode.add(dimmerModulesNode);
			rootNode.add(devicesNode);
			
			final TreeNode moodsNode = new StringTreeNode(NodeType.MOODS, "Moods", this.getTreeModel());
			rootNode.add(moodsNode);
			
			for (final Mood mood : this.driver.getAllMoods()) {
				moodsNode.add(new MoodNode(mood, this.getTreeModel()));
			}
			
			final DefaultTreeModel model = new DefaultTreeModel(rootNode);
			this.tree.setModel(model);
			
			this.tree.addTreeSelectionListener(new ModuleTreeListener() {
				@Override
				final void showDigitalModule(final DigitalModule module) {
					modulePanel.setDigitalModule(module);
					showPanel(modulePanel);
				}

				@Override
				final void showNone() {
					showPanel(null);
				}

				@Override
				final void showDimmerModule(final DimmerModule module) {
					dimmerModulePanel.setDimmerModule(module);
					showPanel(dimmerModulePanel);
				}
				
				@Override
				final void showMood(final Mood mood) {
					moodPanel.setMood(mood);
					showPanel(moodPanel);
				}
				
			});
			
			this.tree.setCellRenderer(new ModuleTreeCellRenderer());
			this.tree.addMouseListener(new ModuleTreeMouseListener(this.tree, this.driver) {
				@Override
				final void showMood(Mood mood) {
					moodPanel.setMood(mood);
					showPanel(moodPanel);
				}
			});
		}
		
		/**
		 * Shows another panel than the one that is shown now.
		 * 
		 * @param 	panel		The panel to show.
		 */
		private final void showPanel(final JPanel panel) {
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "Panel : Showing panel : [" + panel + "]");
			}
			
			this.holderPanel.removeAll();
			
			if (panel != null) {
				this.holderPanel.add(panel);
			}
			
			this.holderPanel.updateUI();
		}
		
		private final DefaultTreeModel getTreeModel() {
			return (DefaultTreeModel)this.tree.getModel();
		}
	}
	
	/**
	 * Main.
	 * 
	 * @param 	args		command line args.
	 */
	public static final void main(final String[] args) {
		final MainFrame frame = new MainFrame();
		frame.setVisible(true);
	}
}
