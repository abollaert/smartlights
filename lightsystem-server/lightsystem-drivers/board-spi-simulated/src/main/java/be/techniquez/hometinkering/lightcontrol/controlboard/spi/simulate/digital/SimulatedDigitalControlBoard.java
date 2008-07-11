package be.techniquez.hometinkering.lightcontrol.controlboard.spi.simulate.digital;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import be.techniquez.hometinkering.lightcontrol.controlboard.spi.digital.AbstractDigitalControlBoard;

/**
 * Simulated digital board, can be used to test the upped layers without having a 
 * real board connected.
 * 
 * @author alex
 *
 */
public final class SimulatedDigitalControlBoard extends AbstractDigitalControlBoard {
	
	/** We keep it to 7 channels. */
	public static final int NUM_CHANNELS = 7;
	
	/** Keep hold of the channel states. */
	private final boolean[] channelStates = new boolean[NUM_CHANNELS];
	
	/** Timer for the random updates. */
	private final Timer randomUpdateTimer = new Timer();
	
	/** The board ID. */
	private final int boardId;
	
	/**
	 * Creates a new simulated board.
	 * 
	 * @param 	boardId		The board ID.
	 */
	public SimulatedDigitalControlBoard(final int boardId) {
		this.boardId = boardId;
		
		this.randomUpdateTimer.schedule(new RandomUpdateTimerTask(), 1000, 2000);
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getBoardID() {
		return boardId;
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getBoardVersion() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getNumberOfChannels() {
		return NUM_CHANNELS;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isLightOn(final int lightIndex) {
		return channelStates[lightIndex];
	}

	/**
	 * {@inheritDoc}
	 */
	public final void switchLight(final int lightIndex, final boolean on) throws IOException {
		this.channelStates[lightIndex] = on;
	}
	
	/**
	 * Timer task that does the random updates.
	 * 
	 * @author alex
	 */
	private final class RandomUpdateTimerTask extends TimerTask {
		
		/** Random number generator. */
		private final Random random = new Random();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void run() {
			// Select a random channel...
			final int channel = random.nextInt(7);
			
			try {
				if (isLightOn(channel)) {
					switchLight(channel, false);
					notifyLightSwitchedOff(channel);
				} else {
					switchLight(channel, true);
					notifyLightSwitchedOn(channel);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getDriverName() {
		return "Simulation";
	}

}
