/*
 * switching.c
 *
 *  Created on: 8 May 2010
 *      Author: alex
 */
#include <stdint.h>
#include <limits.h>
#include "configuration.h"
#include "module_info.h"
#include "switching_types.h"
#include "WProgram.h"
#include "switching.h"
#include "eventing.h"

#define DIM_UP 0
#define DIM_DOWN 1

static boolean exceeded_short_threshold(uint8_t channel_number);
static boolean exceeded_long_threshold(uint8_t channel_number);
static boolean dimmer_exceeded(uint8_t channel_number);
static boolean timer_exceeded(uint8_t channel_number);
static uint8_t calculate_analog_value(uint8_t percentage);
static uint8_t calculate_step(uint8_t current_percentage);

const uint8_t input_channel_map[NUM_CHANNELS] = { 2, 4, 7, 8, 12, 13 };
const uint8_t output_channel_map[NUM_CHANNELS] = { 3, 5, 6, 9, 10, 11 };

uint8_t switch_states[NUM_CHANNELS] = { 0, 0, 0, 0, 0, 0 };
uint8_t output_states[NUM_CHANNELS];
uint8_t dimmer_percentages[NUM_CHANNELS];
uint8_t dimmer_directions[NUM_CHANNELS];

unsigned long switch_timestamps[NUM_CHANNELS] = { 0, 0, 0, 0, 0, 0 };
boolean switch_pressed_event_sent[NUM_CHANNELS] = { false, false, false, false, false, false };
unsigned long timer_start[NUM_CHANNELS] = { 0, 0, 0, 0, 0, 0 };
unsigned long dimmer_start[NUM_CHANNELS] = { 0, 0, 0, 0, 0, 0 };
boolean timer_active[NUM_CHANNELS] = { false, false, false, false, false, false };
boolean dimmer_active[NUM_CHANNELS] = { false, false, false, false, false, false };

// Setup the pins.
void setup_pins(void) {
	uint8_t i = 0;

	for (i = 0; i < NUM_CHANNELS; i++) {
		pinMode(input_channel_map[i], INPUT);
		pinMode(output_channel_map[i], OUTPUT);
	}
}

void setup_default_output_states(void) {
	uint8_t i = 0;

	for (i = 0; i < NUM_CHANNELS; i++) {
		uint8_t default_state = get_default_channel_state(i);
		uint8_t default_percentage = get_default_dimmer_percentage(i);
		uint8_t analog_value = calculate_analog_value(default_percentage);
		uint8_t output_channel = get_switch_mapping(i);

		if (default_state == STATE_ON) {
			analogWrite(output_channel_map[output_channel], analog_value);
			output_states[output_channel] = STATE_ON;
		} else {
			analogWrite(output_channel_map[output_channel], 0);
			output_states[output_channel] = STATE_OFF;
		}

		dimmer_directions[output_channel] = get_default_dimmer_direction(i);
		dimmer_percentages[output_channel] = default_percentage;
	}
}

void handle_switches(void) {
	uint8_t i = 0;

	for (i = 0; i < NUM_CHANNELS; i++) {
		uint8_t current_switch_state = (digitalRead(input_channel_map[i]) == HIGH ? STATE_ON : STATE_OFF);

		// Switch has changed.
		if (current_switch_state != switch_states[i]) {
			// From not pressed to pressed.
			// Set timestamp.
			if (current_switch_state == STATE_ON) {
				switch_timestamps[i] = millis();
				switch_states[i] = STATE_ON;
			} else {
				// From pressed to not pressed.
				uint8_t output_channel = get_switch_mapping(i);

				if (exceeded_short_threshold(i)) {
					if (!exceeded_long_threshold(i)) {
						if (output_states[output_channel] == STATE_OFF) {
							//Serial.println("Switch output to on.");
							switch_output(output_channel, STATE_ON);
						} else {
							//Serial.println("Switch output to off.");
							switch_output(output_channel, STATE_OFF);
						}
					} else {
						// Just reverse the dimmer.
						if (dimmer_directions[i] == DIM_UP) {
							dimmer_directions[i] = DIM_DOWN;
						} else {
							dimmer_directions[i] = DIM_UP;
						}
					}

					dispatch_input_changed_event(i, STATE_OFF);
				}

				switch_states[i] = STATE_OFF;
				switch_pressed_event_sent[i] = false;
				dimmer_active[i] = false;
			}
		} else {
			if (current_switch_state == STATE_ON && exceeded_short_threshold(i)) {
				if (exceeded_long_threshold(i)) {
					uint8_t target_percentage = 0;

					if (dimmer_directions[i] == DIM_UP && dimmer_percentages[i] == 100) {
						target_percentage = 100;
					} else if (dimmer_directions[i] == DIM_DOWN && dimmer_percentages[i] == 0) {
						target_percentage = 0;
					} else {
						uint8_t step = calculate_step(dimmer_percentages[i]);

						target_percentage = (dimmer_directions[i] == DIM_UP ? dimmer_percentages[i] + step : dimmer_percentages[i] - step);
					}

					if (!dimmer_active[i]) {
						if (get_output_state(i) == STATE_OFF) {
							switch_output(get_switch_mapping(i), STATE_ON);
						}

						dimmer_start[i] = millis();
						dimmer_active[i] = true;
					}

					if (dimmer_exceeded(i)) {
						dim(get_switch_mapping(i), target_percentage);
						dimmer_start[i] = millis();
					}
				} else {
					if (!switch_pressed_event_sent[i]) {
						dispatch_input_changed_event(i, STATE_ON);
						switch_pressed_event_sent[i] = true;
					}
				}
			}
		}
	}
}

static boolean exceeded_short_threshold(uint8_t channel_number) {
	unsigned long time = millis();

	if (time > switch_timestamps[channel_number]) {
		if (time - switch_timestamps[channel_number] > get_switch_threshold()) {
			return true;
		}
	} else if (time < switch_timestamps[channel_number]) {
		// Compensate for overflow.
		if ((ULONG_MAX - switch_timestamps[channel_number]) + time > get_switch_threshold()) {
			return true;
		}
	}

	return false;
}

static boolean exceeded_long_threshold(uint8_t channel_number) {
	unsigned long time = millis();

	if (time > switch_timestamps[channel_number]) {
		if (time - switch_timestamps[channel_number] > get_long_press_threshold()) {
			return true;
		}
	} else if (time < switch_timestamps[channel_number]) {
		// Compensate for overflow.
		if ((ULONG_MAX - switch_timestamps[channel_number]) + time > get_long_press_threshold()) {
			return true;
		}
	}

	return false;
}

void switch_output(uint8_t channel_number, uint8_t new_state) {
	if (output_states[channel_number] != new_state) {
		analogWrite(output_channel_map[channel_number], (new_state == STATE_OFF ? 0 : calculate_analog_value(dimmer_percentages[channel_number])));
		output_states[channel_number] = new_state;
		dispatch_output_changed_event(channel_number, new_state, dimmer_percentages[channel_number]);

		if (new_state == STATE_ON) {
			if (get_switch_timer(channel_number) > 0) {
				timer_start[channel_number] = millis();
				timer_active[channel_number] = true;
			}
		}
	}
}

void dim(uint8_t channel_number, uint8_t percentage) {
	if (percentage != dimmer_percentages[channel_number]) {
		if (percentage >= 100) {
			percentage = 100;
		} else if (percentage <= 0) {
			percentage = 0;
		}

		uint8_t analog_value = calculate_analog_value(percentage);

		if (get_output_state(channel_number) == STATE_ON) {
			analogWrite(output_channel_map[channel_number], analog_value);
		}

		dimmer_percentages[channel_number] = percentage;
		dispatch_output_changed_event(channel_number, output_states[channel_number], percentage);
	}
}

uint8_t get_output_state(uint8_t channel_number) {
	return output_states[channel_number];
}

uint8_t get_percentage(uint8_t channel_number) {
	return dimmer_percentages[channel_number];
}

uint8_t get_switch_state(uint8_t channel_number) {
	return switch_states[channel_number];
}

void check_timers(void) {
	uint8_t i = 0;

	for (i = 0; i < NUM_CHANNELS; i++) {
		if (timer_active[i]) {
			if (get_output_state(i) == STATE_ON) {
				if (timer_exceeded(i)) {
					switch_output(i, STATE_OFF);
					timer_active[i] = false;
				}
			}
		}
	}
}

static boolean timer_exceeded(uint8_t channel_number) {
	unsigned long now = millis();

	if (now >= timer_start[channel_number]) {
		if (now > timer_start[channel_number] + (1000 * (unsigned long)get_switch_timer(channel_number))) {
			return true;
		}
	} else {
		// Compensate for overflow.
		if ((ULONG_MAX - timer_start[channel_number]) + now > (1000 * get_switch_timer(channel_number))) {
			return true;
		}
	}

	return false;
}

static boolean dimmer_exceeded(uint8_t channel_number) {
	unsigned long now = millis();

	if (now >= dimmer_start[channel_number]) {
		if (now > dimmer_start[channel_number] + get_dimmer_delay()) {
			return true;
		}
	} else {
		// Compensate for overflow.
		if ((ULONG_MAX - dimmer_start[channel_number]) + now > get_dimmer_delay()) {
			return true;
		}
	}

	return false;
}

static uint8_t calculate_analog_value(uint8_t percentage) {
	int value = (255 * percentage) / 100;

	if (value >= 255) {
		return 255;
	} else if (value <= 0) {
		return 0;
	} else {
		return value;
	}
}

static uint8_t calculate_step(uint8_t percentage) {
	if (percentage >= 75) {
		return 3;
	} else if (percentage >= 50) {
		return 2;
	} else {
		return 1;
	}
}

