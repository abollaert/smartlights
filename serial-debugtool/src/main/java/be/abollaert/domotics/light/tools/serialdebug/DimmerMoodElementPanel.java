package be.abollaert.domotics.light.tools.serialdebug;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DimMoodElement;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.api.Driver;
import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.api.SwitchMoodElement;

/**
 * Panel that is one row.
 * 
 * @author alex
 */
abstract class DimmerMoodElementPanel extends JPanel {

	/** The module. */
	private final JComboBox cbModule = new JComboBox();
	
	/** The channel. */
	private final JComboBox cbChannel = new JComboBox();
	
	/** The name. */
	private final JTextField txtName = new JTextField(30);
	
	/** The slider for the percentage. */
	private final JSlider sldPercentage = new JSlider(0, 100, 100);
	
	/** The label showing the percentage. */
	private final JLabel lblPercentage = new JLabel("");
	
	/** The driver. */
	private final Driver driver;
	
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	driver		The driver.
	 */
	DimmerMoodElementPanel(final Driver driver, final Mood mood) {
		this.driver = driver;
		
		this.cbModule.removeAllItems();
		
		this.cbModule.addItem(null);
		
		for (final int moduleId : this.getAvailableDimmerModuleIds()) {
			this.cbModule.addItem(moduleId);
		}
		
		this.cbModule.addItemListener(new ItemListener() {
			@Override
			public final void itemStateChanged(final ItemEvent e) {
				final Integer selectedModuleId = (Integer)e.getItem();
				
				cbChannel.removeAllItems();
				cbChannel.addItem(null);
				
				if (selectedModuleId != null) {
					final Integer[] availableChannelNumbers = getAvailableChannelNumbers(selectedModuleId);
					
					for (final Integer channelNumber : availableChannelNumbers) {
						cbChannel.addItem(channelNumber);
					}
				}
			}
		});
	
		this.cbChannel.addItemListener(new ItemListener() {
			@Override
			public final void itemStateChanged(final ItemEvent e) {
				final Integer selectedChannelNumber = (Integer)e.getItem();
				
				if (selectedChannelNumber != null) {
					final DimmerModule selectedModule = driver.getDimmerModuleWithID((Integer)cbModule.getSelectedItem());
				
					final String channelName = selectedModule.getDimmerConfiguration().getDimmerChannelConfiguration(selectedChannelNumber).getName();
					txtName.setText(channelName);
					sldPercentage.setEnabled(true);
				} else {
					txtName.setText("");
					sldPercentage.setEnabled(false);
				}
			}
		});
		
		this.add(new JLabel("Dimmer rule: "));
		this.add(new JLabel("Module"));
		this.add(this.cbModule);
		this.add(new JLabel("Channel"));
		this.add(this.cbChannel);
		this.add(new JLabel("Channel name"));
		this.add(this.txtName);
		this.txtName.setEditable(false);
		
		this.sldPercentage.setPaintLabels(true);
		this.sldPercentage.setPaintTicks(true);
		this.sldPercentage.addChangeListener(new ChangeListener() {
			@Override
			public final void stateChanged(final ChangeEvent event) {
				final DimmerModule module = driver.getDimmerModuleWithID((Integer)cbModule.getSelectedItem());
				
				try {
					if (module.getOutputChannelState(getChannelNumber()) == ChannelState.OFF) {
						module.switchOutputChannel(getChannelNumber(), ChannelState.ON);
					}
					
					module.dim(getChannelNumber(), (short)sldPercentage.getValue());
				} catch (IOException e) {
					JOptionPane.showMessageDialog(DimmerMoodElementPanel.this, "IO error while dimming.", "Error while adjusting", JOptionPane.ERROR_MESSAGE);
				}
				
				lblPercentage.setText(String.valueOf(sldPercentage.getValue()));
			}
		});
		
		this.sldPercentage.setEnabled(false);
		
		this.add(new JLabel("Desired percentage"));
		this.add(this.sldPercentage);
		this.add(this.lblPercentage);
		
		this.add(new JButton(new AbstractAction("Remove") {
			@Override
			public final void actionPerformed(ActionEvent e) {
				remove();
			}
		}));
	}
	
	private final Integer[] getAvailableDimmerModuleIds() {
		final List<Integer> moduleIds = new ArrayList<Integer>();
		
		for (final DimmerModule module : this.driver.getAllDimmerModules()) {
			boolean channelsLeft = false;
			
			try {
				for (int i = 0; i < module.getDimmerConfiguration().getNumberOfChannels(); i++) {
					if (!this.channelTaken(module.getId(), i)) {
						channelsLeft = true;
					}
				}
				
				if (channelsLeft) {
					moduleIds.add(module.getId());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return moduleIds.toArray(new Integer[ moduleIds.size() ]);
	}
	
	private final Integer[] getAvailableChannelNumbers(final Integer selectedModuleId) {
		final DimmerModule selectedModule = this.driver.getDimmerModuleWithID(selectedModuleId);
		
		final List<Integer> channelNumbers = new ArrayList<Integer>();
		
		try {
			for (int i = 0; i < selectedModule.getDimmerConfiguration().getNumberOfChannels(); i++) {
				if (!this.channelTaken(selectedModuleId.intValue(), i)) {
					channelNumbers.add(i);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return channelNumbers.toArray(new Integer[ channelNumbers.size() ]);
	}
	
	abstract boolean channelTaken(final int moduleId, final int channelNumber);
	
	abstract void remove();
	
	final Integer getModuleId() {
		return (Integer)this.cbModule.getSelectedItem();
	}
	
	final Integer getChannelNumber() {
		return (Integer)this.cbChannel.getSelectedItem();
	}
	
	final Integer getPercentage() {
		return this.sldPercentage.getValue();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Dimension getMaximumSize() {
		return super.getPreferredSize();
	}
}
