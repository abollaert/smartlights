/*
 * eventing.h
 *
 *  Created on: 7 May 2010
 *      Author: alex
 */

#ifndef EVENTING_H_
#define EVENTING_H_

#define RESPONSE_TYPE_EVENT_OUTPUT_STATE_CHANGE '5'
#define RESPONSE_TYPE_EVENT_INPUT_STATE_CHANGE '6'
#define RESPONSE_TYPE_EVENT_CONFIG_CHANGE '7'

void dispatch_output_changed_event(uint8_t channel_number, uint8_t state);
void dispatch_input_changed_event(uint8_t channel_number, uint8_t state);
void dispatch_configuration_change_event(void);

#endif /* EVENTING_H_ */
