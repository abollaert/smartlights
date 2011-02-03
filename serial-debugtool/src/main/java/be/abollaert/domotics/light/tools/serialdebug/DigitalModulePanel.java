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

import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DigitalModuleConfigurationChangedListener;


/**
 * The digital module panel.
 * 
 * @author alex
 */
final class DigitalModulePanel extends JPanel implements DigitalModuleConfigurationChangedListener {
	
	/** The module that is currently displayed. */
	private DigitalModule module;
	
	/** The text field containing the module ID. */
	//private final JLabel lblModuleId = new JLabel("NONE");
	private final JTextField txtModuleId = new JTextField(2);
	
	/** The label containing the firmware version. */
	private final JLabel lblFirmwareVersion = new JLabel("NONE");
	
	/** The label containing the number of channels. */
	private final JLabel lblNumberOfChannels = new JLabel("NONE");
	
	/** The switching delay. */
	private final JTextField txtSwitchingDelay = new JTextField(5);
	
	/** The channel panels. */
	private DigitalChannelPanel[] channelPanels;
	
	/** The button panel. */
	private JPanel buttonPanel = new JPanel();
	
	private final JFrame hostFrame;
	
	/**
	 * Create a new panel. Only done once.
	 */
	DigitalModulePanel(final JFrame hostFrame) {
		this.hostFrame = hostFrame;
		this.buildUI();
	}
	
	/**
	 * Builds the UI.
	 */
	private final void buildUI() {
		final PropertiesPanel generalInformationPanel = new PropertiesPanel("General information");
		generalInformationPanel.addRow(new JLabel("Module ID : "), this.txtModuleId);
		generalInformationPanel.addRow(new JLabel("Firmware version : "), this.lblFirmwareVersion);
		generalInformationPanel.addRow(new JLabel("Number of channels : "), this.lblNumberOfChannels);
		
		final PropertiesPanel generalConfigurationPanel = new PropertiesPanel("General configuration");
		generalConfigurationPanel.addRow(new JLabel("Switch threshold in milliseconds : "), this.txtSwitchingDelay);
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(generalInformationPanel);
		this.add(generalConfigurationPanel);
		
		this.buttonPanel.add(new JButton(new AbstractAction("Save") {
			@Override
			public final void actionPerformed(final ActionEvent evt) {
				try {
					for (final DigitalChannelPanel channelPanel : channelPanels) {
						channelPanel.save();
					}
					
					module.getDigitalConfiguration().setSwitchThreshold(Integer.parseInt(txtSwitchingDelay.getText()));
					module.getDigitalConfiguration().setModuleId(Integer.parseInt(txtModuleId.getText()));
					module.getDigitalConfiguration().save();
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
	final void setDigitalModule(final DigitalModule module) {
		if (this.module != null) {
			this.module.removeModuleConfigurationListener(this);
		}
		
		try {
			this.txtModuleId.setText(String.valueOf(module.getId()));	
			this.lblFirmwareVersion.setText(String.valueOf(module.getDigitalConfiguration().getFirmwareVersion()));
			this.lblNumberOfChannels.setText(String.valueOf(module.getDigitalConfiguration().getNumberOfChannels()));
			this.txtSwitchingDelay.setText(String.valueOf(module.getDigitalConfiguration().getSwitchThreshold()));

			if (this.channelPanels != null) {
				for (final DigitalChannelPanel channelPanel : this.channelPanels) {
					this.remove(channelPanel);
				}
				
				this.remove(this.buttonPanel);
			}
			
			this.channelPanels = new DigitalChannelPanel[module.getDigitalConfiguration().getNumberOfChannels()];
			
			for (int i = 0; i < module.getDigitalConfiguration().getNumberOfChannels(); i++) {
				final DigitalChannelPanel channelPanel = new DigitalChannelPanel(this.hostFrame, i, module.getDigitalConfiguration().getNumberOfChannels());
				channelPanel.setChannel(module.getDigitalConfiguration().getDigitalChannelConfiguration(i), module);
				this.add(channelPanel);
				this.channelPanels[i] = channelPanel;
			}
			
			this.add(this.buttonPanel);
			
			this.module = module;
			this.module.addModuleConfigurationListener(this);
			
			this.updateUI();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void digitalModuleConfigurationChanged(final int moduleId) {
		if (this.module != null && this.module.getId() == moduleId) {
			try {
				this.txtSwitchingDelay.setText(String.valueOf(this.module.getDigitalConfiguration().getSwitchThreshold()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
