/*
 * protocol.c
 *
 *  Created on: 7 May 2010
 *      Author: alex
 */

#include <stdio.h>
#include "WProgram.h"
#include "protocol_parser.h"
#include "protocol_executor.h"
#include "eventing.h"

#define STATE_WAITING_FOR_COMMAND 0
#define STATE_READING_COMMAND 1

#define RESPONSE_TYPE_OK '0'
#define RESPONSE_TYPE_ERROR '1'

// The buffer may go up to 100 bytes.
#define BUFFER_SIZE 50

// The current state.
uint8_t current_state = STATE_WAITING_FOR_COMMAND;
char buffer[BUFFER_SIZE];
char *buffer_location = buffer;
pdu_type current_pdu;

static void report_processing_error(const uint8_t result);
static void report_processing_ok(void);

void run_protocol() {
	while (Serial.available() > 0) {
		const char current_character = (char)Serial.read();

		switch (current_state) {
			case STATE_WAITING_FOR_COMMAND: {
				if (current_character == PDU_START) {

					buffer_location = buffer;
					current_state = STATE_READING_COMMAND;
				}

				break;
			}

			case STATE_READING_COMMAND: {
				if (current_character == PDU_END) {

					*(buffer_location) = '\0';

					buffer_location = buffer;

					uint8_t result = parse(buffer_location, &current_pdu);

					if (result == 0) {
						result = execute_pdu(&current_pdu);
					}

					if (result != 0) {
						report_processing_error(result);
					} else {
						report_processing_ok();
					}

					current_state = STATE_WAITING_FOR_COMMAND;
					buffer_location = buffer;
				} else if (current_character == PDU_START) {
					current_state = STATE_WAITING_FOR_COMMAND;
					buffer_location = buffer;
				} else {
					*(buffer_location++) = current_character;
				}

				break;
			}
		}
	}
}

static void report_processing_error(const uint8_t result) {
	// We can use the buffer for this as we are single threaded :-)
	buffer_location = buffer;
	*(buffer_location++) = PDU_START;
	*(buffer_location++) = RESPONSE_TYPE_ERROR;
	*(buffer_location++) = ',';
	*(buffer_location++) = '1';
	*(buffer_location++) = ',';
	buffer_location += sprintf(buffer_location, "%i", result);
	*(buffer_location++) = PDU_END;
	(*buffer_location) = '\0';

	Serial.println(buffer);
}

static void report_processing_ok(void) {
	buffer_location = buffer;
	*(buffer_location++) = PDU_START;
	*(buffer_location++) = RESPONSE_TYPE_OK;
	*(buffer_location++) = ',';
	buffer_location += sprintf(buffer_location, "%i", current_pdu.number_of_arguments);

	uint8_t i = 0;

	while (i < current_pdu.number_of_arguments) {
		*(buffer_location++) = ',';
		buffer_location += sprintf(buffer_location, "%i", current_pdu.arguments[i]);
		i++;
	}

	*(buffer_location++) = PDU_END;
	*(buffer_location) = '\0';

	Serial.println(buffer);
	buffer_location = buffer;
}
