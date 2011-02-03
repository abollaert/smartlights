package be.abollaert.domotics.light.services.upgrade;

import java.io.IOException;

/**
 * The upgrade service does just one thing: upgrading a node at a particular address given a particular hex file.
 * 
 * @author alex
 */
public interface UpgradeService {

	/**
	 * Upgrade the node at the given port using the given hex file.
	 * 
	 * @param 	port				The port.
	 * @param 	hexFileLocation		The location of the hex file to use.
	 * 
	 * @throws 	IOException 		If an error occurs during the upgrade.
	 */
	void upgradeNode(final String port, final String hexFileLocation) throws IOException;
}
