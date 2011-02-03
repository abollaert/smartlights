/*
 * protocol_opcode.h
 *
 *  Created on: 7 May 2010
 *      Author: alex
 */

#ifndef PROTOCOL_OPCODE_H_
#define PROTOCOL_OPCODE_H_

// Static information.
#define OPCODE_GET_TYPE 1
#define OPCODE_GET_ID 2
#define OPCODE_GET_FW_VERSION 3
#define OPCODE_GET_NR_CHANNELS 4
#define OPCODE_GET_FEATURES 5

// Configuration getters.
#define OPCODE_GET_SW_THRESHOLD 20
#define OPCODE_GET_SW_TIMER 21
#define OPCODE_GET_CHANNEL_MAPPING 22
#define OPCODE_GET_DEFAULT_STATE 23

// Configuration setters.
#define OPCODE_SET_SW_THRESHOLD 50
#define OPCODE_SET_SW_TIMER 51
#define OPCODE_SET_CHANNEL_MAPPING 52
#define OPCODE_SET_DEFAULT_STATE 53
#define OPCODE_SET_MODULE_ID 58

// Persistence
#define OPCODE_SAVE_CONFIGURATION 70
#define OPCODE_RELOAD_CONFIGURATION 71

// Current state.
#define OPCODE_GET_OUTPUT_STATE 80
#define OPCODE_GET_INPUT_STATE 81

// Actual switching.
#define OPCODE_SWITCH_OUTPUT 90

#endif /* PROTOCOL_OPCODE_H_ */
