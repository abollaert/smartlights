package be.techniquez.hometinkering.lightcontrol.services;

import java.util.List;

import be.techniquez.hometinkering.lightcontrol.model.Board;
import be.techniquez.hometinkering.lightcontrol.model.Light;


/**
 * Defines the repository. This is the central service holding all information necessary to make things work.
 * 
 * @author alex
 */
public interface RepositoryService {
	
	/**
	 * Returns all the boards known in the repository.
	 * 
	 * @return	All the boards known in the repository.
	 */
	@SuppressWarnings("unchecked")
	List<Board> allBoards();
	
	/**
	 * Adds the given listener.
	 * 
	 * @param 	listener	The listener to add.
	 */
	void addListener(final LightSystemEventListener listener);
	
	/**
	 * Returns the light on the given board and channel.
	 * 
	 * @param 	boardId		The board ID.
	 * @param 	channel		The channel.
	 * 
	 * @return	The light on the given channel, null if not known.
	 */
	@SuppressWarnings("unchecked")
	Light getLight(final int boardId, final int channel);
	
	/**
	 * Saves the light to the database.
	 * 
	 * @param 	light		The light to save.
	 */
	@SuppressWarnings("unchecked")
	void saveLight(final Light light);
}