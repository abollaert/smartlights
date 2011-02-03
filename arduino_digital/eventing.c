/*
 * eventing.c
 *
 *  Created on: 7 May 2010
 *      Author: alex
 */
#include "WProgram.h"
#include "protocol_types.h"
#include "eventing.h"

#define LIGHT_EVENT '0'
#define SWITCH_EVENT '1'
#define DEBUG_EVENT '9'

#define BUFFER_SIZE 20

char event_buffer[BUFFER_SIZE];

void dispatch_output_changed_event(uint8_t channel_number, uint8_t state) {
	char* buffer_location = event_buffer;

	*(buffer_location++) = PDU_START;
	*(buffer_location++) = RESPONSE_TYPE_EVENT_OUTPUT_STATE_CHANGE;
	*(buffer_location++) = ',';
	*(buffer_location++) = '2';
	*(buffer_location++) = ',';
	buffer_location += sprintf(buffer_location, "%i", channel_number);
	*(buffer_location++) = ',';
	buffer_location += sprintf(buffer_location, "%i", state);
	*(buffer_location++) = PDU_END;
	(*buffer_location) = '\0';

	Serial.println(event_buffer);
}

void dispatch_input_changed_event(uint8_t channel_number, uint8_t state) {
	char* buffer_location = event_buffer;

	*(buffer_location++) = PDU_START;
	*(buffer_location++) = RESPONSE_TYPE_EVENT_INPUT_STATE_CHANGE;
	*(buffer_location++) = ',';
	*(buffer_location++) = '2';
	*(buffer_location++) = ',';
	buffer_location += sprintf(buffer_location, "%i", channel_number);
	*(buffer_location++) = ',';
	buffer_location += sprintf(buffer_location, "%i", state);
	*(buffer_location++) = PDU_END;
	(*buffer_location) = '\0';

	Serial.println(event_buffer);
}

void dispatch_configuration_change_event(void) {
	char* buffer_location = event_buffer;

	*(buffer_location++) = PDU_START;
	*(buffer_location++) = RESPONSE_TYPE_EVENT_CONFIG_CHANGE;
	*(buffer_location++) = ',';
	*(buffer_location++) = '0';
	*(buffer_location++) = PDU_END;
	(*buffer_location) = '\0';

	Serial.println(event_buffer);
}
