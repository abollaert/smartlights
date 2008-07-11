package be.techniquez.hometinkering.lightcontrol.controlboard.spi.simulate.dimmer;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import be.techniquez.hometinkering.lightcontrol.controlboard.spi.dimmer.AbstractDimmerLightControlBoard;

/**
 * Simulated dimmer board.
 * 
 * @author alex
 *
 */
public final class SimulatedDimmerBoard extends AbstractDimmerLightControlBoard {
	
	/** The simulated boards have 7 channels. */
	private static final int NUM_CHANNELS = 7;
	
	/** The version of the board. */
	private final int boardId;
	
	/** The light states. */
	private final boolean[] lightStates = new boolean[NUM_CHANNELS];
	
	/** Keep track of the dimmer values. */
	private final int[] lightDimValues = new int[NUM_CHANNELS];
	
	/** Timer that does the random state changes. */
	private final Timer randomStateChangeTimer = new Timer();
	
	/**
	 * Creates a new instance, using the given board version.
	 * @param boardVersion
	 */
	public SimulatedDimmerBoard(final int boardId) {
		this.boardId = boardId;
		
		this.randomStateChangeTimer.schedule(new RandomUpdateTimerTask(), 1000, 2000);
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getLightPercentage(final int lightIndex) {
		return this.lightDimValues[lightIndex];
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean isLightOn(final int lightIndex) {
		return this.lightStates[lightIndex];
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setLightPercentage(final int lightIndex, final int percentage) {
		this.lightDimValues[lightIndex] = percentage;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void switchLight(final int lightIndex, final boolean on) throws IOException {
		this.lightStates[lightIndex] = on;
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getBoardID() {
		return this.boardId;
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getBoardVersion() {
		return 999;
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getNumberOfChannels() {
		return NUM_CHANNELS;
	}
	
	/**
	 * Timer task that is responsible for the random updates on the simulated board.
	 * 
	 * @author alex
	 */
	private final class RandomUpdateTimerTask extends TimerTask {
		
		/** The random we use to generate random values. */
		private final Random random = new Random();

		@Override
		public final void run() {
			// Select an action.
			final int action = random.nextInt(2);
			final int channel = random.nextInt(NUM_CHANNELS);
			
			try {
				if (action == 0) {
					// Switch...
					if (isLightOn(channel)) {
						switchLight(channel, false);
						notifyLightSwitchedOff(channel);
					} else {
						switchLight(channel, true);
						notifyLightSwitchedOn(channel);
					}
				} else {
					final int newPercentage = random.nextInt(101);
					setLightPercentage(channel, newPercentage);
					notifyLightDimPercentageChanged(channel, newPercentage);
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
