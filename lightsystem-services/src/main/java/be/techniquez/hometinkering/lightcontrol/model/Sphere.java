package be.techniquez.hometinkering.lightcontrol.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Implements a sphere. Please note that spheres should be explicit, meaning that
 * lights not mentioned in spheres will not be touched when the sphere is applied.
 * 
 * @author alex
 */
public final class Sphere {
	
	/**
	 * Interface that defines a sphere element.
	 * 
	 * @author alex
	 */
	private interface Element {
		
		/**
		 * Applies the sphere to the given element.
		 */
		void apply() throws IOException;
	}
	
	/**
	 * Configuration for a digital light.
	 * 
	 * @author alex
	 */
	private final class DigitalLightElement implements Element {
		
		/** The light. */
		private final DigitalLight light;
		
		/** Indicates whether it should be on or not. */
		private final boolean on;
		
		/**
		 * Creates a new instance of this element for the given light.
		 * 
		 * @param 	light		The light
		 * @param 	on			Indicates whether the light should be on or not.
		 */
		private DigitalLightElement(final DigitalLight light, final boolean on) {
			this.light = light;
			this.on = on;
		}

		/**
		 * {@inheritDoc}
		 */
		public final void apply() throws IOException {
			if (this.on) {
				this.light.switchOn();
			} else {
				this.light.switchOff();
			}
		}
	}
	
	/**
	 * Configuration for a dimmer light.
	 * 
	 * @author alex
	 */
	private final class DimmerLightElement implements Element {
		
		/** The dimmer light. */
		private final DimmerLight light;
		
		/** Indicates whether the light should be on or not when applied. */
		private final boolean on;
		
		/** The percentage the light should be put to. */
		private final int percentage;
		
		/**
		 * Creates a new dimmer light element for this sphere.
		 * 
		 * @param 	light			The dimmer light.
		 * @param 	on				Indicates whether it is on or not.
		 * @param 	percentage		The percentage.
		 */
		private DimmerLightElement(final DimmerLight light, final boolean on, final int percentage) {
			this.light = light;
			this.on = on;
			this.percentage = percentage;
		}

		/**
		 * {@inheritDoc}
		 */
		public final void apply() throws IOException {
			if (on) {
				this.light.switchOn();
				this.light.dim(this.percentage);
			} else {
				// Do not touch the percentage in this case.
				this.light.switchOff();
			}
		}
	}
	
	/** The elements that constitute the sphere. */
	private final Set<Element> elements = new HashSet<Element>();
	
	/** The name of the sphere. */
	private final String name;
	
	/**
	 * Creates a new sphere with the given name.
	 * 
	 * @param 	name	The name of the sphere.
	 */
	public Sphere(final String name) {
		this.name = name;
	}
	
	/**
	 * Adds a digital light element to the sphere.
	 * 
	 * @param 	light		The light
	 * @param 	on			Indicates whether the light should be on or not.
	 */
	public final void addDigitalLightElement(final DigitalLight light, final boolean on) {
		this.elements.add(new DigitalLightElement(light, on));
	}
	
	/**
	 * Adds a dimmer element to the sphere.
	 * 
	 * @param 	light			The light.
	 * @param 	on				Indicates whether it should be on.
	 * @param 	percentage		If it should be on, indicates the percentage it should be set to.
	 */
	public final void addDimmerLightElement(final DimmerLight light, final boolean on, final int percentage) {
		this.elements.add(new DimmerLightElement(light, on, percentage));
	}
	
	/**
	 * Applies the sphere.
	 */
	public final void apply() throws IOException {
		for (final Element element : this.elements) {
			element.apply();
		}
	}
	
	/**
	 * Returns the name of this sphere.
	 * 
	 * @return	The name of this sphere.
	 */
	public final String getName() {
		return this.name;
	}
}
