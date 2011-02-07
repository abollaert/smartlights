package be.abollaert.domotics.light.tools.serialdebug;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DimMoodElement;
import be.abollaert.domotics.light.api.Driver;
import be.abollaert.domotics.light.api.Mood;
import be.abollaert.domotics.light.api.SwitchMoodElement;

final class MoodPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** The panel containing the basic mood information. */
	private final MoodInfoPanel moodInfoPanel;
	
	/** The button panel. */
	private final JPanel buttonPanel = new JPanel();
	
	/** The elements panel */
	private final JPanel elementsPanel = new JPanel();
	
	/** The save button. */
	private final JButton btnSave;
	
	private final List<SwitchElementPanel> switchElementPanels = new ArrayList<SwitchElementPanel>();
	
	private final List<DimmerMoodElementPanel> dimmerElementPanels = new ArrayList<DimmerMoodElementPanel>();
	
	@SuppressWarnings("serial")
	private final JButton btnAddDigitalElement = new JButton(new AbstractAction("Add new switch element") {
		@Override
		public final void actionPerformed(final ActionEvent e) {
			addSwitchElementPanel(null);
			updateUI();
		}
	});
	
	private final void addSwitchElementPanel(final SwitchMoodElement element) {
		final SwitchElementPanel elementPanel = new SwitchElementPanel(driver, mood) {
			@Override
			final boolean channelTaken(final int moduleId, final int channelNumber) {
				for (final SwitchElementPanel switchPanel : switchElementPanels) {
					if (switchPanel.getModuleId() != null && switchPanel.getChannelNumber() != null) {
						if (switchPanel.getModuleId().intValue() == moduleId && switchPanel.getChannelNumber().intValue() == channelNumber) {
							return true;
						}
					}
				}
				
				return false;
			}
			
			@Override
			final void remove() {
				elementsPanel.remove(this);
				switchElementPanels.remove(this);
				
				MoodPanel.this.updateUI();
			}
		};
		
		if (element != null) {
			elementPanel.setSwitchElement(element);
		}
		
		this.switchElementPanels.add(elementPanel);
		this.elementsPanel.add(elementPanel);
	}
	
	private final JButton btnAddDimmerElement = new JButton(new AbstractAction("Add dimmer element") {
		@Override
		public final void actionPerformed(ActionEvent e) {
			addDimElementPanel(null);
			updateUI();
		}
	});
	
	/** Button that activates a mood. */
	private final JButton btnActivateMood = new JButton(new ActivateMoodAction() {
		@Override
		final Component getParentComponent() {
			return MoodPanel.this;
		}
		
		@Override
		final Mood getMood() {
			return MoodPanel.this.mood;
		}
	});
	
	private final void addDimElementPanel(final DimMoodElement element) {
		final DimmerMoodElementPanel elementPanel = new DimmerMoodElementPanel(driver, mood) {
			@Override
			final void remove() {
				elementsPanel.remove(this);
				dimmerElementPanels.remove(this);
				MoodPanel.this.updateUI();
			}
			
			@Override
			final boolean channelTaken(final int moduleId, final int channelNumber) {
				for (final DimmerMoodElementPanel dimmerPanel : dimmerElementPanels) {
					if (dimmerPanel.getModuleId() != null && dimmerPanel.getChannelNumber() != null) {
						if (dimmerPanel.getModuleId().intValue() == moduleId && dimmerPanel.getChannelNumber().intValue() == channelNumber) {
							return true;
						}
					}
				}
				return false;
			}
		};
		
		if (element != null) {
			elementPanel.setDimElement(element);
		}
		
		this.dimmerElementPanels.add(elementPanel);
		this.elementsPanel.add(elementPanel);
	}
	
	/** The mood to set. */
	private Mood mood;
	
	/** The driver. */
	private Driver driver;
	
	MoodPanel(final Driver driver) {
		super();
		
		this.driver = driver;
		this.moodInfoPanel = new MoodInfoPanel();
		
		this.btnSave = new JButton(new SaveMoodAction() {
			@Override
			final Component getParentComponent() {
				return MoodPanel.this;
			}
			
			@Override
			final Mood getMood() {
				moodInfoPanel.copyFromUI();
				
				mood.getSwitchMoodElements().clear();
				
				for (final SwitchElementPanel panel : switchElementPanels) {
					mood.addSwitchElement(panel.getModuleId(), panel.getChannelNumber(), panel.getRequestedState());
				}
				
				mood.getDimMoodElements().clear();
				
				for (final DimmerMoodElementPanel panel : dimmerElementPanels) {
					mood.addDimElement(panel.getModuleId(), panel.getChannelNumber(), panel.getPercentage());
				}
				
				return mood;
			}

			@Override
			final void afterSuccess() {
				moodInfoPanel.setMood(mood);
			}
		});
		
		this.buttonPanel.add(this.btnSave);
		this.buttonPanel.add(this.btnAddDigitalElement);
		this.buttonPanel.add(this.btnAddDimmerElement);
		this.buttonPanel.add(this.btnActivateMood);
		
		this.elementsPanel.setLayout(new BoxLayout(this.elementsPanel, BoxLayout.Y_AXIS));
		this.elementsPanel.setBorder(BorderFactory.createTitledBorder("Elements"));
		
		this.setLayout(new BorderLayout());
		
		this.add(this.moodInfoPanel, BorderLayout.NORTH);
		this.add(this.elementsPanel, BorderLayout.CENTER);
		this.add(this.buttonPanel, BorderLayout.SOUTH);
	}
	
	final void setMood(final Mood mood) {
		this.mood = mood;
		
		this.moodInfoPanel.setMood(mood);
		
		for (final SwitchElementPanel panel : this.switchElementPanels) {
			this.elementsPanel.remove(panel);
		}
		
		this.switchElementPanels.clear();
		
		for (final DimmerMoodElementPanel panel : this.dimmerElementPanels) {
			this.elementsPanel.remove(panel);
		}
		
		this.dimmerElementPanels.clear();
		
		for (final SwitchMoodElement switchElement : mood.getSwitchMoodElements()) {
			this.addSwitchElementPanel(switchElement);
		}
		
		for (final DimMoodElement dimElement : mood.getDimMoodElements()) {
			this.addDimElementPanel(dimElement);
		}
		
		this.updateUI();
	}
}
