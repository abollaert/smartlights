package be.techniquez.hometinkering.lightcontrol.ui.configuration.swing.components.panel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.font.TextAttribute;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.sun.rowset.internal.InsertRow;

import be.techniquez.hometinkering.lightcontrol.dpws.client.model.DigitalBoard;

/**
 * Digital board panel. This one holds one digital board.
 * 
 * @author alex
 */
public final class DigitalBoardPanel extends JPanel {
	
	/** The board this panel is holding. */
	private final DigitalBoard board;
	
	/**
	 * Creates a new instance.
	 */
	public DigitalBoardPanel(final DigitalBoard board) {
		this.setLayout(new BorderLayout());
		this.board = board;
		
		
		this.add(this.createBoardInformationPanel(), BorderLayout.NORTH);
		this.add(this.createChannelPanel(), BorderLayout.CENTER);
	}
	
	private final JPanel createBoardInformationPanel() {
		final StringBuilder boardInformation = new StringBuilder("<html>Digital Board, ID [<b>").append(board.getId()).append("</b>]<br>");
		boardInformation.append("Driver name [<b>").append(board.getDriverName()).append("</b>], version [<b>").append(this.board.getSoftwareVersion()).append("</b>]<br>");
		
		final JPanel informationPanel = new JPanel();
		
		final JLabel boardInformationLabel = new JLabel(boardInformation.toString());
		boardInformationLabel.setBorder(BorderFactory.createEtchedBorder());

		informationPanel.add(boardInformationLabel);
		
		return informationPanel;
	}
	
	private final JPanel createChannelPanel() {
		final JPanel channelPanel = new JPanel();
		
		channelPanel.setLayout(new GridBagLayout());
		
		final Insets leftPaddingInsets = new Insets(0, 200, 0, 0);
		final Insets rightPaddingInsets = new Insets(0, 0, 0, 200);
		final Insets noPaddingInsets = new Insets(0, 0, 0, 0);
		
		final GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.fill = GridBagConstraints.HORIZONTAL;
		
		final JLabel channelNumberHeaderLabel = new JLabel("Channel number");
		channelNumberHeaderLabel.setBorder(LineBorder.createGrayLineBorder());
		constraints.insets = leftPaddingInsets;
		constraints.weightx = 0.5;
		constraints.gridx = 0;
		constraints.gridy = 0;
		channelPanel.add(channelNumberHeaderLabel, constraints);
		
		constraints.insets = noPaddingInsets;
		
		final JLabel lightNameHeaderLabel = new JLabel("Light name");
		lightNameHeaderLabel.setBorder(LineBorder.createGrayLineBorder());
		constraints.weightx = 0.5;
		constraints.gridx = 1;
		constraints.gridy = 0;
		channelPanel.add(lightNameHeaderLabel, constraints);
		
		final JLabel lightDescriptionHeaderLabel = new JLabel("Light description");
		lightDescriptionHeaderLabel.setBorder(LineBorder.createGrayLineBorder());
		constraints.weightx = 0.5;
		constraints.gridx = 2;
		constraints.gridy = 0;
		channelPanel.add(lightDescriptionHeaderLabel, constraints);
		
		constraints.weightx = 0.5;
		constraints.gridx = 3;
		constraints.gridy = 0;
		channelPanel.add(new JLabel(), constraints);
		
		constraints.weightx = 0.5;
		constraints.gridx = 4;
		constraints.gridy = 0;
		channelPanel.add(new JLabel(), constraints);
		
		constraints.insets = rightPaddingInsets;
		constraints.weightx = 0.5;
		constraints.gridx = 5;
		constraints.gridy = 0;
		channelPanel.add(new JLabel(), constraints);
		constraints.insets = noPaddingInsets;
		
		for (final int channelNumber : this.board.getChannels().keySet()) {
			final JLabel channelNumberLabel = new JLabel(String.valueOf(channelNumber));
			final JTextField lightNameTextField = new JTextField(15);
			final JTextField lightDescriptionTextField = new JTextField(50);
			
			final JButton allowChangeButton = new JButton();
			final JButton lightSaveButton = new JButton();
			final JButton cancelChangesButton = new JButton();
			
			lightSaveButton.setAction(new SaveLightAction(channelNumber, allowChangeButton, lightSaveButton, cancelChangesButton, lightNameTextField, lightDescriptionTextField));
			cancelChangesButton.setAction(new CancelLightChangeAction(channelNumber, allowChangeButton, lightSaveButton, cancelChangesButton, lightNameTextField, lightDescriptionTextField));
			allowChangeButton.setAction(new AllowEditAction(channelNumber, allowChangeButton, lightSaveButton, cancelChangesButton, lightNameTextField, lightDescriptionTextField));
			
			constraints.insets = leftPaddingInsets;
			constraints.weightx = 0.5;
			constraints.gridx = 0;
			constraints.gridy = channelNumber + 1;
			channelPanel.add(channelNumberLabel, constraints);
			constraints.insets =  noPaddingInsets;
			
			constraints.weightx = 0.5;
			constraints.gridx = 1;
			channelPanel.add(lightNameTextField, constraints);
			
			constraints.weightx = 0.5;
			constraints.gridx = 2;
			channelPanel.add(lightDescriptionTextField, constraints);
			
			constraints.weightx = 0.5;
			constraints.gridx = 3;
			channelPanel.add(allowChangeButton, constraints);
			
			constraints.weightx = 0.5;
			constraints.gridx = 4;
			channelPanel.add(lightSaveButton, constraints);
			
			constraints.insets = rightPaddingInsets;
			constraints.weightx = 0.5;
			constraints.gridx = 5;
			channelPanel.add(cancelChangesButton, constraints);
			constraints.insets = noPaddingInsets;
			
			cancelChangesButton.setEnabled(false);
			lightSaveButton.setEnabled(false);
			lightNameTextField.setEditable(false);
			lightDescriptionTextField.setEditable(false);
		}
		
		return channelPanel;
	}
	
	private abstract class ChannelChangeAction extends AbstractAction {
		
		private final int channelNumber;
		
		private final JButton saveButton;
		
		private final JButton cancelButton;
		
		private final JButton allowChangeButton;
		
		protected final int getChannelNumber() {
			return channelNumber;
		}

		protected final JButton getSaveButton() {
			return saveButton;
		}

		protected final JButton getCancelButton() {
			return cancelButton;
		}

		protected final JButton getAllowChangeButton() {
			return allowChangeButton;
		}

		protected final JTextField getChannelNameText() {
			return channelNameText;
		}

		protected final JTextField getChannelDescriptionText() {
			return channelDescriptionText;
		}

		private final JTextField channelNameText;
		
		private final JTextField channelDescriptionText;
		
		private ChannelChangeAction(final int channelNumber, final JButton allowChangeButton, final JButton saveButton, final JButton cancelButton, final JTextField channelNameText, final JTextField channelDescriptionText) {
			super();
			
			this.channelNumber = channelNumber;
			this.cancelButton = cancelButton;
			this.saveButton = saveButton;
			this.allowChangeButton = allowChangeButton;
			this.channelDescriptionText = channelDescriptionText;
			this.channelNameText = channelNameText;
		}
		
		protected final void setEditEnabled(final boolean enabled) {
			this.allowChangeButton.setEnabled(!enabled);
			this.saveButton.setEnabled(enabled);
			this.cancelButton.setEnabled(enabled);
			this.channelNameText.setEditable(enabled);
			this.channelDescriptionText.setEditable(enabled);
			
			this.channelNameText.requestFocus();
		}
	}
	
	private final class SaveLightAction extends ChannelChangeAction {
		
		private SaveLightAction(final int channelNumber, final JButton allowChangeButton, final JButton saveButton, final JButton cancelButton, final JTextField channelNameText, final JTextField channelDescriptionText) {
			super(channelNumber, allowChangeButton, saveButton, cancelButton, channelNameText, channelDescriptionText);
			
			this.putValue(Action.NAME, "Save channel");
			this.putValue(Action.LONG_DESCRIPTION, "Saves the current values of the channel");
		}

		public final void actionPerformed(final ActionEvent e) {
			this.setEditEnabled(false);
		}
	}
	
	private final class CancelLightChangeAction extends ChannelChangeAction {
		
		private CancelLightChangeAction(final int channelNumber, final JButton allowChangeButton, final JButton saveButton, final JButton cancelButton, final JTextField channelNameText, final JTextField channelDescriptionText) {
			super(channelNumber, allowChangeButton, saveButton, cancelButton, channelNameText, channelDescriptionText);
			
			this.putValue(Action.NAME, "Cancel channel changes");
			this.putValue(Action.LONG_DESCRIPTION, "Cancels the changes on the channel and reverts to the old situation");
		}

		public final void actionPerformed(final ActionEvent e) {
			this.getChannelNameText().setText(board.getChannels().get(this.getChannelNumber()));
			
			setEditEnabled(false);
		}
	}
	
	
	private final class AllowEditAction extends ChannelChangeAction {
		
		private AllowEditAction(final int channelNumber, final JButton allowChangeButton, final JButton saveButton, final JButton cancelButton, final JTextField channelNameText, final JTextField channelDescriptionText) {
			super(channelNumber, allowChangeButton, saveButton, cancelButton, channelNameText, channelDescriptionText);
			
			this.putValue(Action.NAME, "Change configuration");
			this.putValue(Action.LONG_DESCRIPTION, "Allows the editing of the configuration");
		}

		public final void actionPerformed(final ActionEvent e) {
			setEditEnabled(true);
		}
	}
	
	private final class ChannelValueChangedListener implements DocumentListener {
		
		private final JButton saveButton;
		
		private final JButton cancelButton;
		
		private ChannelValueChangedListener(final JButton saveButton, final JButton cancelButton) {
			this.cancelButton = cancelButton;
			this.saveButton = saveButton;
		}

		/**
		 */
		public final void changedUpdate(final DocumentEvent e) {
		}

		public final void insertUpdate(final DocumentEvent e) {
			this.saveButton.setEnabled(true);
			this.cancelButton.setEnabled(true);
			
		}

		public final void removeUpdate(final DocumentEvent e) {
			this.saveButton.setEnabled(true);
			this.cancelButton.setEnabled(true);
		}
		
	}
}