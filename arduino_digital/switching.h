/*
 * switching.h
 *
 *  Created on: 8 May 2010
 *      Author: alex
 */

#ifndef SWITCHING_H_
#define SWITCHING_H_

void setup_pins(void);
void handle_switches(void);
void switch_output(uint8_t channel_number, uint8_t new_state);
void setup_default_output_states(void);
uint8_t get_output_state(uint8_t channel_number);
uint8_t get_switch_state(uint8_t channel_number);
void check_timers(void);

#endif /* SWITCHING_H_ */
