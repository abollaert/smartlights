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
import javax.swing.JPanel;
import javax.swing.JTextField;

import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.Driver;
import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.api.SwitchMoodElement;

/**
 * Panel that is one row.
 * 
 * @author alex
 */
abstract class SwitchElementPanel extends JPanel {

	/** The module. */
	private final JComboBox cbModule = new JComboBox();
	
	/** The channel. */
	private final JComboBox cbChannel = new JComboBox();
	
	/** The name. */
	private final JTextField txtName = new JTextField(30);
	
	/** The driver. */
	private final Driver driver;
	
	/** The mood. */
	private final Mood mood;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	driver		The driver.
	 */
	SwitchElementPanel(final Driver driver, final Mood mood) {
		this.driver = driver;
		this.mood = mood;
		
		this.cbModule.removeAllItems();
		
		this.cbModule.addItem(null);
		
		for (final int moduleId : this.getAvailableDigitalModuleIds()) {
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
					final DigitalModule selectedModule = driver.getDigitalModuleWithID((Integer)cbModule.getSelectedItem());
				
					final String channelName = selectedModule.getDigitalConfiguration().getDigitalChannelConfiguration(selectedChannelNumber).getName();
					txtName.setText(channelName);
				} else {
					txtName.setText("");
				}
			}
		});
		
		this.add(new JLabel("Switch rule: "));
		this.add(new JLabel("Module"));
		this.add(this.cbModule);
		this.add(new JLabel("Channel"));
		this.add(this.cbChannel);
		this.add(new JLabel("Channel name"));
		this.add(this.txtName);
		this.txtName.setEditable(false);
		
		this.add(new JButton(new AbstractAction("Remove") {
			@Override
			public final void actionPerformed(ActionEvent e) {
				remove();
			}
		}));
	}
	
	private final Integer[] getAvailableDigitalModuleIds() {
		final List<Integer> moduleIds = new ArrayList<Integer>();
		
		for (final DigitalModule module : this.driver.getAllDigitalModules()) {
			boolean channelsLeft = false;
			
			try {
				for (int i = 0; i < module.getDigitalConfiguration().getNumberOfChannels(); i++) {
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
		final DigitalModule selectedModule = this.driver.getDigitalModuleWithID(selectedModuleId);
		
		final List<Integer> channelNumbers = new ArrayList<Integer>();
		
		try {
			for (int i = 0; i < selectedModule.getDigitalConfiguration().getNumberOfChannels(); i++) {
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
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Dimension getMaximumSize() {
		return super.getPreferredSize();
	}
	
	void setSwitchElement(final SwitchMoodElement element) {
		this.cbModule.setSelectedItem(element.getModuleId());
		this.cbChannel.setSelectedItem(element.getChannelNumber());
	}
}
