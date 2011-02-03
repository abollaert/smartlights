package be.abollaert.domotics.light.tools.serialdebug;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalChannelStateChangeListener;
import be.abollaert.domotics.light.api.DigitalInputChannelConfiguration;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DimmerChannelStateChangeListener;


final class DigitalChannelPanel extends PropertiesPanel implements DigitalChannelStateChangeListener, DimmerChannelStateChangeListener {
	
	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(DigitalChannelPanel.class
			.getName());

	/** The combo box containing the mapped output channel. */
	private final JComboBox cbxMappedOutputChannel = new JComboBox();
	
	/** The text field containing the timer value. */
	private final JTextField txtTimer = new JTextField(5);
	
	private final JComboBox cbxDefaultState = new JComboBox();
	
	private final JFrame parent;
	
	/** Tests the output channel. */
	private final JButton btnTestOutputChannel = new JButton(new AbstractAction("Switch output channel") {
		@Override
		public final void actionPerformed(final ActionEvent event) {
			try {
				if (lblOutputChannelState.getText().equals("ON")) {
					module.switchOutputChannel((Integer)cbxMappedOutputChannel.getSelectedItem(), ChannelState.OFF);
				} else if (lblOutputChannelState.getText().equals("OFF")) {
					module.switchOutputChannel((Integer)cbxMappedOutputChannel.getSelectedItem(), ChannelState.ON);
				}
			} catch (IOException e) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "Could not switch channel due to an IO error !", e);
				}
				
				JOptionPane.showMessageDialog(DigitalChannelPanel.this,e.getMessage(), "IO error when switching output channel.", JOptionPane.ERROR_MESSAGE);
				
			}
		}
	});
	
	/** Contains the output channel state. */
	private final JLabel lblOutputChannelState = new JLabel("NONE");
	
	/** Contains the output channel state. */
	private final JLabel lblSwitchState = new JLabel("NONE");
	
	/** The text field containing the name of the channel. */
	private final JTextField txtName = new JTextField(25);
	
	/** The checkbox that indicates if logging is enabled. */
	private final JCheckBox chkLoggingEnabled = new JCheckBox();
	
	/** Button to show the switch events. */
	private final JButton btnShowSwitchEvents = new JButton(new AbstractAction("Show switch events") {
		@Override
		public final void actionPerformed(final ActionEvent e) {
			final ChannelEventDialog dialog = new DigitalChannelEventDialog(parent, module, channelNumber);
			dialog.setModal(false);
			dialog.setVisible(true);
		}
	});
	
	/** The channel configuration. */
	private DigitalInputChannelConfiguration configuration;
	
	/** The module. */
	private DigitalModule module;
	
	/** The channel number. */
	private final int channelNumber;
	
	/**
	 * Create a new instance.
	 * 
	 * @param channelNumber
	 */
	DigitalChannelPanel(final JFrame parent, final int channelNumber, final int numberOfChannels) {
		super("Channel " + channelNumber);
		
		this.parent = parent;
		
		for (int i = 0; i < numberOfChannels; i++) {
			this.cbxMappedOutputChannel.addItem(i);
		}
		
		for (final ChannelState state : ChannelState.values()) {
			this.cbxDefaultState.addItem(state);
		}
		
		this.channelNumber = channelNumber;
		
		this.addRow(new JLabel("Name"), this.txtName);
		this.addRow(new JLabel("Mapped output channel : "), this.cbxMappedOutputChannel);
		this.cbxMappedOutputChannel.addItemListener(new ItemListener() {
			@Override
			public final void itemStateChanged(ItemEvent e) {
				try {
					lblOutputChannelState.setText(String.valueOf(module.getOutputChannelState((Integer)cbxMappedOutputChannel.getSelectedItem())));
				} catch (IOException exc) {
					exc.printStackTrace();
				}
			}
		});
		
		this.addRow(new JLabel("Default state : "), this.cbxDefaultState);
		this.addRow(new JLabel("Current switch state : "), this.lblSwitchState);
		this.addRow(new JLabel("Current output channel state : "), this.lblOutputChannelState, this.btnTestOutputChannel);
		this.addRow(new JLabel("Timer (in seconds) : "), this.txtTimer);
		this.addRow(new JLabel("Enable logging"), this.chkLoggingEnabled);
		this.addRow(this.btnShowSwitchEvents);
	}
	
	final void setChannel(final DigitalInputChannelConfiguration configuration, final DigitalModule module) {
		try {
			this.configuration = configuration;
			this.module = module;
			this.module.addChannelStateListener(this);
			
			this.cbxMappedOutputChannel.setSelectedItem(configuration.getMappedOutputChannel());
			this.cbxDefaultState.setSelectedItem(configuration.getDefaultState());
			this.txtTimer.setText(String.valueOf(configuration.getTimerInSeconds()));
			
			try {
				this.lblSwitchState.setText(this.module.getInputChannelState(this.channelNumber).toString());
			} catch (IOException e) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "IO error when getting channel state for channel number [" + this.channelNumber + "] !", e);
				}
				
				this.lblSwitchState.setText("ERROR");
			}
			
			try {
				this.lblOutputChannelState.setText(this.module.getOutputChannelState(this.configuration.getMappedOutputChannel()).toString());
			} catch (IOException e) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING, "IO error when getting channel state for channel number [" + this.channelNumber + "] !", e);
				}
				
				this.lblOutputChannelState.setText("ERROR");
			}
			
			if (configuration.getName() != null) {
				this.txtName.setText(configuration.getName());
			}
			
			this.chkLoggingEnabled.setSelected(configuration.isLoggingEnabled());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void outputChannelStateChanged(final int channelNumber, final ChannelState newState) {
		if (this.cbxMappedOutputChannel.getSelectedItem() != null && ((Integer)this.cbxMappedOutputChannel.getSelectedItem()).intValue() == channelNumber) {
			this.lblOutputChannelState.setText(newState.toString());
		}
	}

	@Override
	public void inputChannelStateChanged(int channelNumber, ChannelState newState) {
		if (channelNumber == this.channelNumber) {
			this.lblSwitchState.setText(newState.toString());
		}
		
	}
	
	final void save() throws IOException {
		this.configuration.setTimerInSeconds(Integer.parseInt(this.txtTimer.getText()));
		this.configuration.setMappedOutputChannel(((Integer)this.cbxMappedOutputChannel.getSelectedItem()));
		this.configuration.setDefaultState((ChannelState)this.cbxDefaultState.getSelectedItem());
		this.configuration.setName(this.txtName.getText());
		this.configuration.setLoggingEnabled(this.chkLoggingEnabled.isSelected());
	}

	@Override
	public final void outputChannelStateChanged(int channelNumber, ChannelState newState, int percentage) {
		if (channelNumber == this.channelNumber) {
			this.lblSwitchState.setText(newState.toString());
		}
	}

}
