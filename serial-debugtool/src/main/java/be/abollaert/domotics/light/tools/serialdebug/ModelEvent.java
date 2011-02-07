package be.abollaert.domotics.light.tools.serialdebug;

/**
 * Defines a model event.
 * 
 * @author alex
 */
final class ModelEvent<T> {

	/** Enumerates the type of events. */
	enum Type {
		ADD, EDIT, REMOVE;
	}
	
	/** Enumerates the model types. */
	enum Model {
		MOOD;
	}
	
	/** The type. */
	private final Type type;
	
	/** The model. */
	private final Model model;
	
	private final T modelObject;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	type		The type.
	 * @param 	model		The model.
	 */
	ModelEvent(final Type type, final Model model, final T modelObject) {
		this.type = type;
		this.model = model;
		this.modelObject = modelObject;
		
	}
	
	final Type getType() {
		return this.type;
	}
	
	final Model getModel() {
		return this.model;
	}
	
	final T getModelObject() {
		return this.modelObject;
	}
}
