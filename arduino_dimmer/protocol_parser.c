/*
 * protocol_parser.c
 *
 *  Created on: 7 May 2010
 *      Author: alex
 */
#include <stdint.h>
#include <string.h>
#include <stdlib.h>

#include "WProgram.h"
#include "protocol_opcode.h"
#include "protocol_parser.h"

#define DELIMITER ","

// Parse the buffer into the given pdu structure.
// Returns 0 or error code on failure.
uint8_t parse(char *buffer, pdu_type *pdu) {
	const char *delimiter = DELIMITER;
	char **current_token_location = &buffer;
	char *current_token;

	current_token = strsep(current_token_location, delimiter);
	uint8_t i = 0;

	while (current_token != NULL) {
		const int current_digit = atoi(current_token);

		// Opcode.
		if (i == 0) {
			pdu->opcode = current_digit;
		} else if (i == 1) {
			pdu->number_of_arguments = current_digit;
		} else {
			pdu->arguments[i - 2] = current_digit;
		}

		i++;
		current_token = strsep(current_token_location, delimiter);
	}

	if (pdu->number_of_arguments == i - 2) {
		return 0;
	} else {
		return ERROR_WRONG_NUMBER_OF_ARGUMENTS;
	}
}
