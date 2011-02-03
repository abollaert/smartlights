package be.abollaert.domotics.light.tools.serialdebug;

import java.awt.Component;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

abstract class BaseAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new save mood action.
	 */
	BaseAction(final String name) {
		super(name);
	}
	
	final void showError(final String title, final String message) {
		JOptionPane.showMessageDialog(this.getParentComponent(), message, title, JOptionPane.ERROR_MESSAGE);
	}
	
	final void showError(final String title, final Exception e) {
		this.showError(title, e.getMessage());
	}
	
	final void showError(final Exception e) {
		this.showError("Error while executing action.", e.getMessage());
	}
	
	/**
	 * Return the parent component.
	 * 
	 * @return	The parent component.
	 */
	abstract Component getParentComponent();
	
	void afterSuccess() {
	}
	
	void afterError() {
	}
}
