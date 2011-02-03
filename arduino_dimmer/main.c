/*
 * digital.c
 *
 *  Created on: 15 Mar 2010
 *      Author: alex
 */

#include "WProgram.h"
#include "protocol.h"
#include "configuration.h"
#include "switching.h"

// This needs to be here as otherwise you get an error compiling.
// See http://www.arduino.cc/playground/Code/Eclipse
extern "C" void __cxa_pure_virtual(void) {
    while(1);
}

/**
 * Main function. Sets everything up and loops forever.
 */
int main(void) {
	init();

	Serial.begin(19200);

	setup_pins();
	configuration_load();
	setup_default_output_states();

	for (;;) {
		check_timers();
		handle_switches();
		run_protocol();
	}

	return 0;
}
