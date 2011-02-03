/*
 * configuration.c
 *
 *  Created on: 7 May 2010
 *      Author: alex
 */
#include <stdlib.h>
#include <stdint.h>
#include <avr/eeprom.h>
#include <util/crc16.h>

#include "module_info.h"
#include "eventing.h"
#include "WProgram.h"

typedef struct configuration {
	uint8_t module_id;
	uint16_t switch_threshold;
	uint16_t long_press_threshold;
	uint8_t dimmer_delay;
	uint8_t switch_mappings[NUM_CHANNELS];
	uint16_t switch_timers[NUM_CHANNELS];
	uint8_t default_state[NUM_CHANNELS];
	uint8_t default_dimmer_percentage[NUM_CHANNELS];
	uint8_t default_dimmer_direction[NUM_CHANNELS];
	uint16_t crc;
} configuration_type;

const configuration_type default_configuration = {
		5,
		30,
		200,
		13,
		{ 0, 1, 2, 3, 4, 5 },
		{ 0, 0, 0, 0, 0, 0 },
		{ 0, 0, 0, 0, 0, 0 },
		{ 100, 100, 100, 100, 100, 100 },
		{ 1, 1, 1, 1, 1, 1 },
		0
};

configuration_type current_configuration;

static uint16_t calculate_crc(const configuration_type* configuration) {
	uint16_t calculated_crc = 0;
	uint8_t i = 0;
	uint8_t length = sizeof(configuration_type) - 2;

	while (i < length) {
		_crc16_update(calculated_crc, *((uint8_t*)configuration + i));
		i++;
	}

	return calculated_crc;
}

void configuration_load(void) {
	eeprom_read_block(&current_configuration, 0, sizeof(configuration_type));

	if (calculate_crc(&current_configuration) != current_configuration.crc) {
		memcpy(&current_configuration, &default_configuration, sizeof(configuration_type));
	}

	dispatch_configuration_change_event();
}

uint16_t get_switch_threshold(void) {
	return current_configuration.switch_threshold;
}

uint16_t get_long_press_threshold(void) {
	return current_configuration.long_press_threshold;
}

uint8_t get_dimmer_delay(void) {
	return current_configuration.dimmer_delay;
}

uint8_t get_switch_mapping(uint8_t channel_number) {
	return current_configuration.switch_mappings[channel_number];
}

uint8_t get_default_channel_state(uint8_t channel_number) {
	return current_configuration.default_state[channel_number];
}

uint16_t get_switch_timer(uint8_t channel_number) {
	return current_configuration.switch_timers[channel_number];
}

void set_switch_threshold(uint16_t threshold) {
	current_configuration.switch_threshold = threshold;
}

void set_switch_mapping(uint8_t channel_number, uint8_t mapped_output) {
	current_configuration.switch_mappings[channel_number] = mapped_output;
}

void set_long_press_threshold(uint16_t threshold) {
	current_configuration.long_press_threshold = threshold;
}

void set_dimmer_delay(uint16_t delay) {
	current_configuration.dimmer_delay = delay;
}

void set_default_channel_state(uint8_t channel_number, uint8_t default_state) {
	current_configuration.default_state[channel_number] = default_state;
}

void set_switch_timer(uint8_t channel_number, uint16_t timer) {
	current_configuration.switch_timers[channel_number] = timer;
}

void set_default_dimmer_percentage(uint8_t channel_number, uint8_t percentage) {
	current_configuration.default_dimmer_percentage[channel_number] = percentage;
}

void set_default_dimmer_direction(uint8_t channel_number, uint8_t direction) {
	current_configuration.default_dimmer_direction[channel_number] = direction;
}

uint8_t get_default_dimmer_percentage(uint8_t channel_number) {
	return current_configuration.default_dimmer_percentage[channel_number];
}

uint8_t get_default_dimmer_direction(uint8_t channel_number) {
	return current_configuration.default_dimmer_direction[channel_number];
}

void configuration_save(void) {
	current_configuration.crc = calculate_crc(&current_configuration);
	eeprom_write_block(&current_configuration, 0, sizeof(configuration_type));
	dispatch_configuration_change_event();
}

uint8_t get_module_id(void) {
	return current_configuration.module_id;
}
