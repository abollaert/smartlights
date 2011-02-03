/*
 * protocol_types.h
 *
 *  Created on: 7 May 2010
 *      Author: alex
 */

#ifndef PROTOCOL_TYPES_H_
#define PROTOCOL_TYPES_H_

#define PDU_START '{'
#define PDU_END '}'
#define MAX_ARGUMENTS 20

// Structure definition for a PDU.
typedef struct pdu {
	uint8_t opcode;
	uint8_t number_of_arguments;
	uint16_t arguments[MAX_ARGUMENTS];
} pdu_type;

#endif /* PROTOCOL_TYPES_H_ */
