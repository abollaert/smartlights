/*
 * protocol_parser.h
 *
 *  Created on: 7 May 2010
 *      Author: alex
 */

#ifndef PROTOCOL_PARSER_H_
#define PROTOCOL_PARSER_H_

#include <stdint.h>
#include "protocol_types.h"
#include "protocol_errors.h"

// Parse the buffer into the given pdu structure.
// Returns 0 or error code on failure.
uint8_t parse(char *buffer, pdu_type *pdu);

#endif /* PROTOCOL_PARSER_H_ */
