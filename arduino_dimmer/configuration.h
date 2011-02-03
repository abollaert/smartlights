/*
 * configuration.h
 *
 *  Created on: 7 May 2010
 *      Author: alex
 */

#ifndef CONFIGURATION_H_
#define CONFIGURATION_H_

#include <stdint.h>

void configuration_load(void);
void configuration_save(void);

uint16_t get_switch_threshold(void);
uint16_t get_long_press_threshold(void);
uint8_t get_dimmer_delay(void);
uint8_t get_switch_mapping(uint8_t channel_number);
uint16_t get_switch_timer(uint8_t channel_number);
uint8_t get_default_channel_state(uint8_t channel_number);
uint8_t get_default_dimmer_percentage(uint8_t channel_number);
uint8_t get_default_dimmer_direction(uint8_t channel_number);
uint8_t get_module_id(void);

void set_switch_threshold(uint16_t threshold);
void set_long_press_threshold(uint16_t threshold);
void set_dimmer_delay(uint16_t delay);
void set_switch_mapping(uint8_t channel_number, uint8_t mapped_output);
void set_default_channel_state(uint8_t channel_number, uint8_t default_state);
void set_switch_timer(uint8_t channel_number, uint16_t timer);
void set_default_dimmer_percentage(uint8_t channel_number, uint8_t percentage);
void set_default_dimmer_direction(uint8_t channel_number, uint8_t direction);

#endif /* CONFIGURATION_H_ */
