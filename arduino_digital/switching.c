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

static boolean exceeded_threshold(uint8_t channel_number);
static boolean timer_exceeded(uint8_t channel_number);

const uint8_t input_channel_map[NUM_CHANNELS] = { 2, 3, 4, 5, 6, 7 };
const uint8_t output_channel_map[NUM_CHANNELS] = { 8, 9, 10, 11, 12, 13 };

uint8_t switch_states[NUM_CHANNELS] = { 0, 0, 0, 0, 0, 0 };
uint8_t output_states[NUM_CHANNELS];
unsigned long switch_timestamps[NUM_CHANNELS] = { 0, 0, 0, 0, 0, 0 };
boolean switch_pressed_event_sent[NUM_CHANNELS] = { false, false, false, false, false, false };
unsigned long timer_start[NUM_CHANNELS] = { 0, 0, 0, 0, 0, 0};
boolean timer_active[NUM_CHANNELS] = { false, false, false, false, false, false };

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

		if (default_state == STATE_ON) {
			digitalWrite(output_channel_map[i], HIGH);
			output_states[i] = STATE_ON;
		} else {
			digitalWrite(output_channel_map[i], LOW);
			output_states[i] = STATE_OFF;
		}
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
				if (exceeded_threshold(i)) {
					dispatch_input_changed_event(i, STATE_OFF);

					if (output_states[get_switch_mapping(i)] == STATE_OFF) {
						switch_output(get_switch_mapping(i), STATE_ON);
					} else {
						switch_output(get_switch_mapping(i), STATE_OFF);
					}
				}

				switch_states[i] = STATE_OFF;
				switch_pressed_event_sent[i] = false;
			}
		} else {
			if (current_switch_state == STATE_ON && !switch_pressed_event_sent[i] && exceeded_threshold(i)) {
				dispatch_input_changed_event(i, STATE_ON);
				switch_pressed_event_sent[i] = true;
			}
		}
	}
}

static boolean exceeded_threshold(uint8_t channel_number) {
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

void switch_output(uint8_t channel_number, uint8_t new_state) {
	if (output_states[channel_number] != new_state) {
		digitalWrite(output_channel_map[channel_number], (new_state == STATE_OFF ? LOW : HIGH));
		output_states[channel_number] = new_state;
		dispatch_output_changed_event(channel_number, new_state);

		if (new_state == STATE_ON) {
			if (get_switch_timer(channel_number) > 0) {
				timer_start[channel_number] = millis();
				timer_active[channel_number] = true;
			}
		}
	}
}

uint8_t get_output_state(uint8_t channel_number) {
	return output_states[channel_number];
}

uint8_t get_switch_state(uint8_t channel_number) {
	return switch_states[channel_number];
}

void check_timers(void) {
	uint8_t i = 0;

	for (i = 0; i < NUM_CHANNELS; i++) {
		if (timer_active[i]) {
			if (get_output_state(get_switch_mapping(i)) == STATE_ON) {
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
		if ((ULONG_MAX - timer_start[channel_number]) + now > (1000 * (unsigned long)get_switch_timer(channel_number))) {
			return true;
		}
	}

	return false;
}

