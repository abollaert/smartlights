package be.abollaert.domotics.light.tools.serialdebug;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

final class EventDispatcher {
	
	/** Logger definition. */
	private static final Logger logger = Logger.getLogger(EventDispatcher.class
			.getName());
	
	/** Allow max of 20 events. */
	private static final int EVENT_QUEUE_CAPACITY = 20;
	
	private static final EventDispatcher INSTANCE = new EventDispatcher();
	
	static final EventDispatcher getInstance() {
		return INSTANCE;
	}
	
	/** The event queue. */
	private final BlockingQueue<ModelEvent<?>> eventQueue = new ArrayBlockingQueue<ModelEvent<?>>(EVENT_QUEUE_CAPACITY);
	
	private final ExecutorService dispatcherService = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public final Thread newThread(final Runnable r) {
			return new Thread(r, "Event Dispatcher : dispatch thread");
		}
		
	});
	
	private final Dispatcher dispatcher = new Dispatcher();
	
	/** 
	 * Weak collection of model listeners. Weak means the listener can be garbage collected if we have the only reference to it.
	 */
	private final Set<ModelListener> modelListeners = Collections.newSetFromMap(new WeakHashMap<ModelListener, Boolean>());
	
	private EventDispatcher() {
		this.dispatcherService.execute(this.dispatcher);
	}
	
	final void dispatchModelEvent(final ModelEvent<?> event) {
		try {
			this.eventQueue.put(event);
		} catch (InterruptedException e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, "Interrupted while dispatching model event [" + event + "]");
			}
		}
	}
	
	final void addModelListener(final ModelListener listener) {
		this.modelListeners.add(listener);
	}
	
	final void removeModelListener(final ModelListener listener) {
		this.modelListeners.remove(listener);
	}
	
	private final class Dispatcher implements Runnable {
		
		private volatile boolean keepRunning = true;
		
		@Override
		public void run() {
			while (this.keepRunning) {
				try {
					final ModelEvent<?> event = EventDispatcher.this.eventQueue.take();
					
					if (logger.isLoggable(Level.INFO)) {
						logger.log(Level.INFO, "Dispatching model event : [" + event + "]");
					}
					
					for (final ModelListener listener : modelListeners) {
						listener.modelChanged(event);
					}
				} catch (InterruptedException e) {
				} catch (RuntimeException e) {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "Runtime error when dispatching model event : [" + e.getMessage() + "]", e);
					}
				}
			}
		}
		
	}
	
}
