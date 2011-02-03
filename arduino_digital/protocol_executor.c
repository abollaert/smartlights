/*
 * protocol_executor.c
 *
 *  Created on: 7 May 2010
 *      Author: alex
 */
#include <stdint.h>

#include "WProgram.h"
#include "protocol_opcode.h"
#include "protocol_types.h"
#include "protocol_errors.h"
#include "module_info.h"
#include "eventing.h"
#include "configuration.h"
#include "switching.h"
#include "switching_types.h"

static boolean is_valid_channel(uint8_t channel_number);

uint8_t execute_pdu(pdu_type *pdu) {
	switch (pdu->opcode) {
		case OPCODE_GET_TYPE: {
			pdu->number_of_arguments = 1;
			pdu->arguments[0] = MODULE_TYPE;
			break;
		}

		case OPCODE_GET_ID: {
			pdu->number_of_arguments = 1;
			pdu->arguments[0] = get_module_id();
			break;
		}

		case OPCODE_GET_FW_VERSION: {
			pdu->number_of_arguments = 3;
			pdu->arguments[0] = FIRMWARE_MAJOR;
			pdu->arguments[1] = FIRMWARE_MINOR;
			pdu->arguments[2] = FIRMWARE_PATCHLEVEL;

			break;
		}

		case OPCODE_GET_NR_CHANNELS: {
			pdu->number_of_arguments = 1;
			pdu->arguments[0] = NUM_CHANNELS;

			break;
		}

		case OPCODE_GET_FEATURES: {
			pdu->number_of_arguments = 1;
			pdu->arguments[0] = 0;

			break;
		}

		case OPCODE_GET_SW_THRESHOLD: {
			pdu->number_of_arguments = 1;
			pdu->arguments[0] = get_switch_threshold();

			break;
		}

		case OPCODE_GET_SW_TIMER: {
			if (pdu->number_of_arguments == 1) {
				uint8_t channel_number = pdu->arguments[0];

				if (is_valid_channel(channel_number)) {
					uint16_t channel_timer = get_switch_timer(channel_number);

					pdu->number_of_arguments = 1;
					pdu->arguments[0] = channel_timer;
				} else {
					return ERROR_INVALID_CHANNEL_NUMBER;
				}
			} else {
				return ERROR_WRONG_NUMBER_OF_ARGUMENTS;
			}

			break;
		}

		case OPCODE_GET_CHANNEL_MAPPING: {
			if (pdu->number_of_arguments == 1) {
				uint8_t channel_number = pdu->arguments[0];

				if (is_valid_channel(channel_number)) {
					uint8_t channel_mapping = get_switch_mapping(channel_number);

					pdu->number_of_arguments = 1;
					pdu->arguments[0] = channel_mapping;
				} else {
					return ERROR_INVALID_CHANNEL_NUMBER;
				}
			} else {
				return ERROR_WRONG_NUMBER_OF_ARGUMENTS;
			}

			break;
		}

		case OPCODE_GET_DEFAULT_STATE: {
			if (pdu->number_of_arguments == 1) {
				uint8_t channel_number = pdu->arguments[0];

				if (is_valid_channel(channel_number)) {
					uint8_t channel_state = get_default_channel_state(channel_number);

					pdu->number_of_arguments = 1;
					pdu->arguments[0] = channel_state;
				} else {
					return ERROR_INVALID_CHANNEL_NUMBER;
				}
			} else {
				return ERROR_WRONG_NUMBER_OF_ARGUMENTS;
			}

			break;
		}

		case OPCODE_SET_SW_THRESHOLD: {
			if (pdu->number_of_arguments == 1) {
				uint16_t threshold = pdu->arguments[0];
				set_switch_threshold(threshold);
				pdu->number_of_arguments = 0;
			} else {
				return ERROR_WRONG_NUMBER_OF_ARGUMENTS;
			}

			break;
		}

		case OPCODE_SET_SW_TIMER: {
			if (pdu->number_of_arguments == 2) {
				uint8_t channel_number = pdu->arguments[0];
				uint16_t timer = pdu->arguments[1];

				if (is_valid_channel(channel_number)) {
					set_switch_timer(channel_number, timer);
					pdu->number_of_arguments = 0;
				} else {
					return ERROR_WRONG_NUMBER_OF_ARGUMENTS;
				}
			} else {
				return ERROR_WRONG_NUMBER_OF_ARGUMENTS;
			}

			break;
		}

		case OPCODE_SET_CHANNEL_MAPPING: {
			if (pdu->number_of_arguments == 2) {
				uint8_t channel_number = pdu->arguments[0];
				uint8_t mapping = pdu->arguments[1];

				if (is_valid_channel(channel_number) && is_valid_channel(mapping)) {
					set_switch_mapping(channel_number, mapping);
					pdu->number_of_arguments = 0;
				} else {
					return ERROR_WRONG_NUMBER_OF_ARGUMENTS;
				}
			} else {
				return ERROR_WRONG_NUMBER_OF_ARGUMENTS;
			}

			break;
		}

		case OPCODE_SET_DEFAULT_STATE: {
			if (pdu->number_of_arguments == 2) {
				uint8_t channel_number = pdu->arguments[0];
				uint8_t default_state = pdu->arguments[1];

				if (is_valid_channel(channel_number)) {
					if (default_state == 0 || default_state == 1) {
						set_default_channel_state(channel_number, default_state);
						pdu->number_of_arguments = 0;
					} else {
						return ERROR_INVALID_STATE;
					}
				} else {
					return ERROR_WRONG_NUMBER_OF_ARGUMENTS;
				}
			} else {
				return ERROR_WRONG_NUMBER_OF_ARGUMENTS;
			}
			break;
		}

		case OPCODE_RELOAD_CONFIGURATION: {
			configuration_load();
			pdu->number_of_arguments = 0;

			break;
		}

		case OPCODE_SAVE_CONFIGURATION: {
			configuration_save();
			pdu->number_of_arguments = 0;

			break;
		}

		case OPCODE_GET_OUTPUT_STATE: {
			if (pdu->number_of_arguments == 1) {
				uint8_t channel_number = pdu->arguments[0];

				if (is_valid_channel(channel_number)) {
					uint8_t output_state = get_output_state(channel_number);

					pdu->number_of_arguments = 1;
					pdu->arguments[0] = output_state;
				} else {
					return ERROR_INVALID_CHANNEL_NUMBER;
				}
			} else {
				return ERROR_WRONG_NUMBER_OF_ARGUMENTS;
			}

			break;
		}

		case OPCODE_GET_INPUT_STATE: {
			if (pdu->number_of_arguments == 1) {
				uint8_t channel_number = pdu->arguments[0];

				if (is_valid_channel(channel_number)) {
					uint8_t switch_state = get_switch_state(channel_number);

					pdu->number_of_arguments = 1;
					pdu->arguments[0] = switch_state;
				} else {
					return ERROR_INVALID_CHANNEL_NUMBER;
				}
			} else {
				return ERROR_WRONG_NUMBER_OF_ARGUMENTS;
			}

			break;
		}

		case OPCODE_SWITCH_OUTPUT: {
			if (pdu->number_of_arguments == 2) {
				uint8_t channel_number = pdu->arguments[0];

				if (is_valid_channel(channel_number)) {
					uint8_t new_state = pdu->arguments[1];

					if (new_state == STATE_OFF || new_state == STATE_ON) {
						switch_output(channel_number, new_state);

						pdu->number_of_arguments = 0;
					} else {
						return ERROR_INVALID_STATE;
					}
				} else {
					return ERROR_INVALID_CHANNEL_NUMBER;
				}
			} else {
				return ERROR_WRONG_NUMBER_OF_ARGUMENTS;
			}

			break;
		}

		case OPCODE_SET_MODULE_ID: {
			if (pdu->number_of_arguments == 1) {
				uint8_t module_id = pdu->arguments[0];
				set_module_id(module_id);
				pdu->number_of_arguments = 0;
			} else {
				return ERROR_WRONG_NUMBER_OF_ARGUMENTS;
			}
			break;
		}

		default: {
			return ERROR_INVALID_OPCODE;
		}
	}

	return 0;
}

static boolean is_valid_channel(uint8_t channel_number) {
	return channel_number >= 0 && channel_number < NUM_CHANNELS;
}
