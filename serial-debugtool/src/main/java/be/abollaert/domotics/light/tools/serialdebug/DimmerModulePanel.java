package be.abollaert.domotics.light.tools.serialdebug;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import be.abollaert.domotics.light.api.DimmerModule;


/**
 * The digital module panel.
 * 
 * @author alex
 */
final class DimmerModulePanel extends JPanel {
	
	/** The module that is currently displayed. */
	private DimmerModule module;
	
	/** The label containing the module ID. */
	private final JLabel lblModuleId = new JLabel("NONE");
	
	/** The label containing the firmware version. */
	private final JLabel lblFirmwareVersion = new JLabel("NONE");
	
	/** The label containing the number of channels. */
	private final JLabel lblNumberOfChannels = new JLabel("NONE");
	
	/** The switching delay. */
	private final JTextField txtSwitchingThreshold = new JTextField(5);
	
	/** The switching delay. */
	private final JTextField txtDimmerThreshold = new JTextField(5);
	
	/** The switching delay. */
	private final JTextField txtDimmerDelay = new JTextField(5);
	
	/** The channel panels. */
	private DimmerChannelPanel[] channelPanels;
	
	/** The button panel. */
	private JPanel buttonPanel = new JPanel();
	
	private final JFrame hostFrame;
	
	/**
	 * Create a new panel. Only done once.
	 */
	DimmerModulePanel(final JFrame hostFrame) {
		this.hostFrame = hostFrame;
		this.buildUI();
	}
	
	/**
	 * Builds the UI.
	 */
	private final void buildUI() {
		final PropertiesPanel generalInformationPanel = new PropertiesPanel("General information");
		generalInformationPanel.addRow(new JLabel("Module ID : "), this.lblModuleId);
		generalInformationPanel.addRow(new JLabel("Firmware version : "), this.lblFirmwareVersion);
		generalInformationPanel.addRow(new JLabel("Number of channels : "), this.lblNumberOfChannels);
		
		final PropertiesPanel generalConfigurationPanel = new PropertiesPanel("General configuration");
		generalConfigurationPanel.addRow(new JLabel("Switch threshold in milliseconds : "), this.txtSwitchingThreshold);
		generalConfigurationPanel.addRow(new JLabel("Dimmer threshold in milliseconds : "), this.txtDimmerThreshold);
		generalConfigurationPanel.addRow(new JLabel("Dimmer delay in milliseconds : "), this.txtDimmerDelay);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(generalInformationPanel);
		this.add(generalConfigurationPanel);
		
		this.buttonPanel.add(new JButton(new AbstractAction("Save") {
			@Override
			public final void actionPerformed(final ActionEvent evt) {
				try {
					for (final DimmerChannelPanel channelPanel : channelPanels) {
						channelPanel.save();
					}
					
					module.getDimmerConfiguration().setSwitchThreshold(Integer.parseInt(txtSwitchingThreshold.getText()));
					module.getDimmerConfiguration().setDimmerThreshold(Integer.parseInt(txtDimmerThreshold.getText()));
					module.getDimmerConfiguration().setDimmerDelay(Integer.parseInt(txtDimmerDelay.getText()));
					module.getDimmerConfiguration().save();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}));
		this.buttonPanel.add(new JButton("Revert to saved values"));
	}
	
	/**
	 * Sets the digital module.
	 * 
	 * @param 	module		The module to set.
	 */
	final void setDimmerModule(final DimmerModule module) {
		try {
			this.lblModuleId.setText(String.valueOf(module.getId()));	
			this.lblFirmwareVersion.setText(String.valueOf(module.getDimmerConfiguration().getFirmwareVersion()));
			this.lblNumberOfChannels.setText(String.valueOf(module.getDimmerConfiguration().getNumberOfChannels()));
			this.txtSwitchingThreshold.setText(String.valueOf(module.getDimmerConfiguration().getSwitchThreshold()));
			this.txtDimmerThreshold.setText(String.valueOf(module.getDimmerConfiguration().getDimmerThreshold()));
			this.txtDimmerDelay.setText(String.valueOf(module.getDimmerConfiguration().getDimmerDelay()));

			if (this.channelPanels != null) {
				for (final DimmerChannelPanel channelPanel : this.channelPanels) {
					this.remove(channelPanel);
				}
				
				this.remove(this.buttonPanel);
			}
			
			this.channelPanels = new DimmerChannelPanel[module.getDimmerConfiguration().getNumberOfChannels()];
			
			for (int i = 0; i < module.getDimmerConfiguration().getNumberOfChannels(); i++) {
				final DimmerChannelPanel channelPanel = new DimmerChannelPanel(this.hostFrame, i, module.getDimmerConfiguration().getNumberOfChannels());
				channelPanel.setChannel(module.getDimmerConfiguration().getDimmerChannelConfiguration(i), module);
				this.add(channelPanel);
				this.channelPanels[i] = channelPanel;
			}
			
			this.add(this.buttonPanel);
			
			this.module = module;
			
			this.updateUI();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
