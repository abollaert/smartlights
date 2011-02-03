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
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DimmerChannelStateChangeListener;
import be.abollaert.domotics.light.api.DimmerDirection;
import be.abollaert.domotics.light.api.DimmerInputChannelConfiguration;
import be.abollaert.domotics.light.api.DimmerModule;

final class DimmerChannelPanel extends PropertiesPanel implements
		DimmerChannelStateChangeListener {

	/** Logger instance. */
	private static final Logger logger = Logger
			.getLogger(DimmerChannelPanel.class.getName());

	/** The combo box containing the mapped output channel. */
	private final JComboBox cbxMappedOutputChannel = new JComboBox();

	/** The text field containing the timer value. */
	private final JTextField txtTimer = new JTextField(5);

	private final JComboBox cbxDefaultState = new JComboBox();

	private final JComboBox cbxDefaultDimDirection = new JComboBox();

	/** The slider used for dimming. */
	private final JSlider sldDimmerPercentage = new JSlider(0, 100);
	
	/** The host frame. */
	private final JFrame hostFrame;

	/** Tests the output channel. */
	private final JButton btnTestOutputChannel = new JButton(
			new AbstractAction("Switch output channel") {
				@Override
				public final void actionPerformed(final ActionEvent event) {
					try {
						if (lblOutputChannelState.getText().equals("ON")) {
							module.switchOutputChannel(
									(Integer) cbxMappedOutputChannel
											.getSelectedItem(),
									ChannelState.OFF);
						} else if (lblOutputChannelState.getText()
								.equals("OFF")) {
							module
									.switchOutputChannel(
											(Integer) cbxMappedOutputChannel
													.getSelectedItem(),
											ChannelState.ON);
						}
					} catch (IOException e) {
						if (logger.isLoggable(Level.WARNING)) {
							logger
									.log(
											Level.WARNING,
											"Could not switch channel due to an IO error !",
											e);
						}

						JOptionPane.showMessageDialog(DimmerChannelPanel.this,
								e.getMessage(),
								"IO error when switching output channel.",
								JOptionPane.ERROR_MESSAGE);

					}
				}
			});

	/** Contains the output channel state. */
	private final JLabel lblOutputChannelState = new JLabel("NONE");

	/** Contains the output channel state. */
	private final JLabel lblSwitchState = new JLabel("NONE");

	/** The channel configuration. */
	private DimmerInputChannelConfiguration configuration;

	/** The switching delay. */
	private final JTextField txtDefaultPercentage = new JTextField(5);

	/** The module. */
	private DimmerModule module;

	/** The channel number. */
	private final int channelNumber;
	
	/** Indicates whether slider listeners are disabled. */
	private volatile boolean disableSliderListeners;
	
	/** Text field containing the name. */
	private final JTextField txtName = new JTextField(25);
	
	/** Checkbox that enables logging. */
	private final JCheckBox chkEnableLogging = new JCheckBox();
	
	/** Button to show the switch events. */
	private final JButton btnShowSwitchEvents = new JButton(new AbstractAction("Show switch events") {
		@Override
		public final void actionPerformed(final ActionEvent e) {
			final ChannelEventDialog dialog = new DimmerChannelEventDialog(hostFrame, module, channelNumber);
			dialog.setModal(false);
			dialog.setVisible(true);
		}
	});

	/**
	 * Create a new instance.
	 * 
	 * @param channelNumber
	 */
	DimmerChannelPanel(final JFrame hostFrame, final int channelNumber, final int numberOfChannels) {
		super("Channel " + channelNumber);

		this.hostFrame = hostFrame;
		
		for (int i = 0; i < numberOfChannels; i++) {
			this.cbxMappedOutputChannel.addItem(i);
		}

		for (final ChannelState state : ChannelState.values()) {
			this.cbxDefaultState.addItem(state);
		}

		for (final DimmerDirection state : DimmerDirection.values()) {
			this.cbxDefaultDimDirection.addItem(state);
		}

		this.channelNumber = channelNumber;
		
		this.addRow(new JLabel("Channel name : "), this.txtName);
		this.addRow(new JLabel("Mapped output channel : "),
				this.cbxMappedOutputChannel);
		this.cbxMappedOutputChannel.addItemListener(new ItemListener() {
			@Override
			public final void itemStateChanged(ItemEvent e) {
				try {
					lblOutputChannelState
							.setText(String
									.valueOf(module
											.getOutputChannelState((Integer) cbxMappedOutputChannel
													.getSelectedItem())));
				} catch (IOException exc) {
					exc.printStackTrace();
				}
			}
		});

		this.addRow(new JLabel("Default state : "), this.cbxDefaultState);
		this.addRow(new JLabel("Default direction : "),
				this.cbxDefaultDimDirection);
		this.addRow(new JLabel("Default percentage : "),
				this.txtDefaultPercentage);
		this.addRow(new JLabel("Timer (in seconds) : "), this.txtTimer);
		this.addRow(new JLabel("Logging enabled : "), this.chkEnableLogging);
		this.addRow(new JLabel("Current switch state : "), this.lblSwitchState);
		this.addRow(new JLabel("Current output channel state : "),
				this.lblOutputChannelState, this.btnTestOutputChannel);
		this.addRow(new JLabel("Current dimmer state : "),
				this.sldDimmerPercentage);
		this.addRow(this.btnShowSwitchEvents);
	}

	final void setChannel(final DimmerInputChannelConfiguration configuration,
			final DimmerModule module) {
		try {
			this.configuration = configuration;
			this.module = module;
			this.module.addChannelStateListener(this);

			this.cbxMappedOutputChannel.setSelectedItem(configuration
					.getMappedOutputChannel());
			this.cbxDefaultState.setSelectedItem(configuration
					.getDefaultState());
			this.cbxDefaultDimDirection.setSelectedItem(configuration
					.getDefaultDirection());
			this.txtTimer.setText(String.valueOf(configuration
					.getTimerInSeconds()));
			this.txtDefaultPercentage.setText(String.valueOf(configuration
					.getDefaultPercentage()));
			this.sldDimmerPercentage.setValue(this.module
					.getDimmerPercentage(this.channelNumber));
			this.sldDimmerPercentage.addChangeListener(new ChangeListener() {
				@Override
				public final void stateChanged(final ChangeEvent event) {
					if (!sldDimmerPercentage.getValueIsAdjusting() && !disableSliderListeners) {
						try {
							module.dim(channelNumber,
									(short) sldDimmerPercentage.getValue());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});

			try {
				this.lblSwitchState.setText(this.module.getInputChannelState(
						this.channelNumber).toString());
			} catch (IOException e) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING,
							"IO error when getting channel state for channel number ["
									+ this.channelNumber + "] !", e);
				}

				this.lblSwitchState.setText("ERROR");
			}

			try {
				this.lblOutputChannelState.setText(this.module
						.getOutputChannelState(
								this.configuration.getMappedOutputChannel())
						.toString());
			} catch (IOException e) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.log(Level.WARNING,
							"IO error when getting channel state for channel number ["
									+ this.channelNumber + "] !", e);
				}

				this.lblOutputChannelState.setText("ERROR");
			}
			
			if (this.configuration.getName() != null) {
				this.txtName.setText(this.configuration.getName());
			} else {
				this.txtName.setText("");
			}
			
			this.chkEnableLogging.setSelected(this.configuration.isLoggingEnabled());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void outputChannelStateChanged(final int channelNumber,
			final ChannelState newState, final int percentage) {
		if (channelNumber == (Integer) this.cbxMappedOutputChannel
				.getSelectedItem()) {
			this.lblOutputChannelState.setText(newState.toString());
			this.disableSliderListeners = true;
			this.sldDimmerPercentage.setValue(percentage);
			this.disableSliderListeners = false;
		}
	}

	@Override
	public void inputChannelStateChanged(int channelNumber,
			ChannelState newState) {
		if (channelNumber == this.channelNumber) {
			this.lblSwitchState.setText(newState.toString());
		}

	}

	final void save() throws IOException {
		this.configuration.setTimerInSeconds(Integer.parseInt(this.txtTimer
				.getText()));
		this.configuration
				.setMappedOutputChannel(((Integer) this.cbxMappedOutputChannel
						.getSelectedItem()));
		this.configuration.setDefaultState((ChannelState) this.cbxDefaultState
				.getSelectedItem());
		this.configuration
				.setDefaultDirection((DimmerDirection) this.cbxDefaultDimDirection
						.getSelectedItem());
		this.configuration.setDefaultPercentage(Integer
				.parseInt(this.txtDefaultPercentage.getText()));
		
		this.configuration.setName(this.txtName.getText());
		this.configuration.setLoggingEnabled(this.chkEnableLogging.isSelected());
	}

}
