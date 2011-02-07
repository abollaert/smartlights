package be.abollaert.domotics.light.tools.serialdebug;

import java.awt.Component;
import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import be.abollaert.domotics.light.api.Driver;

abstract class BaseAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** The driver. */
	private final Driver driver;
	
	/**
	 * Create a new save mood action.
	 */
	BaseAction(final Driver driver, final String name) {
		super(name);
		
		this.driver = driver;
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
	
	/**
	 * Returns the driver.
	 * 
	 * @return	The driver.
	 */
	final Driver getDriver() {
		return this.driver;
	}
}
