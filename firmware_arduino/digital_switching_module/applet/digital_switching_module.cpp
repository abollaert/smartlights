#include "WProgram.h"
void loadFromEEPROM();
void handleSwitch(struct Switch* sw);
void switchOn(struct Light* light);
void switchOff(struct Light* light);
void reportCurrentStatus();
void checkCommandsOnSerial();
void executeCommand(char command[]);
#include "EEPROM.h"
#define NUM_CHANNELS 6
#define VERSION 1
#define BOARD_ID 1
#define BOARD_TYPE 1

// Enumerate the statuses a channel can take (these are the outputs).
enum STATUS {
  OFF, ON
};

// Enumerate the input pins.
enum INPUTPIN {
  INPUT_1 = 2,
  INPUT_2 = 3,
  INPUT_3 = 4,
  INPUT_4 = 5,
  INPUT_5 = 6,
  INPUT_6 = 7
};

// Enumerate the output pins.
enum OUTPUTPIN {
  OUTPUT_1 = 8,
  OUTPUT_2 = 9,
  OUTPUT_3 = 10,
  OUTPUT_4 = 11,
  OUTPUT_5 = 12,
  OUTPUT_6 = 13
};

// Defines a light.
typedef struct Light {
  enum OUTPUTPIN outputPin;
  enum STATUS status;
  int eepromAddress;
};

// Defines a switch.
typedef struct Switch {
  enum INPUTPIN inputPin;
  enum STATUS status;
  Light *light;
};

/** Keep track of switches and lights. */
Switch switches[NUM_CHANNELS];
Light lights[NUM_CHANNELS];

/** Set us up. */
void setup() {
  // Setup the lights first.
  lights[0].outputPin = OUTPUT_1;
  pinMode(lights[0].outputPin, OUTPUT);
  digitalWrite(lights[0].outputPin, LOW);
  lights[0].status = OFF;
  lights[0].eepromAddress = 0;
  
  lights[1].outputPin = OUTPUT_2;
  pinMode(lights[1].outputPin, OUTPUT);
  digitalWrite(lights[1].outputPin, LOW);
  lights[1].status = OFF;
  lights[1].eepromAddress = 1;
  
  lights[2].outputPin = OUTPUT_3;
  pinMode(lights[2].outputPin, OUTPUT);
  digitalWrite(lights[2].outputPin, LOW);
  lights[2].status = OFF;
  lights[2].eepromAddress = 2;
  
  lights[3].outputPin = OUTPUT_4;
  pinMode(lights[3].outputPin, OUTPUT);
  digitalWrite(lights[3].outputPin, LOW);
  lights[3].status = OFF;
  lights[3].eepromAddress = 3;
  
  lights[4].outputPin = OUTPUT_5;
  pinMode(lights[4].outputPin, OUTPUT);
  digitalWrite(lights[4].outputPin, LOW);
  lights[4].status = OFF;
  lights[4].eepromAddress = 4;
  
  lights[5].outputPin = OUTPUT_6;
  pinMode(lights[5].outputPin, OUTPUT);
  digitalWrite(lights[5].outputPin, LOW);
  lights[5].status = OFF;
  lights[5].eepromAddress = 5;
  
  // Setup the switches.
  switches[0].inputPin = INPUT_1;
  pinMode(switches[0].inputPin, INPUT);
  switches[0].status = OFF;
  switches[0].light = &lights[0];
 
  switches[1].inputPin = INPUT_2;
  pinMode(switches[1].inputPin, INPUT);
  switches[1].status = OFF; 
  switches[1].light = &lights[1];
  
  switches[2].inputPin = INPUT_3;
  pinMode(switches[2].inputPin, INPUT);
  switches[2].status = OFF;
  switches[2].light = &lights[2]; 
  
  switches[3].inputPin = INPUT_4;
  pinMode(switches[3].inputPin, INPUT);
  switches[3].status = OFF;
  switches[3].light = &lights[3];
  
  switches[4].inputPin = INPUT_5;
  pinMode(switches[4].inputPin, INPUT);
  switches[4].status = OFF;
  switches[4].light = &lights[4];
  
  switches[5].inputPin = INPUT_6;
  pinMode(switches[5].inputPin, INPUT);
  switches[5].status = OFF;
  switches[5].light = &lights[5];
  
  loadFromEEPROM();
  
  Serial.begin(9600);
}

/** Loads previous values from the EEPROM. */
void loadFromEEPROM() {
  int i;
  
  for (i = 0; i < NUM_CHANNELS; i++) {
    if (EEPROM.read(lights[i].eepromAddress) == ON) {
      switchOn(&lights[i]);
    } else {
      switchOff(&lights[i]);
    }
  }
}

/** Main loop. */
void loop() {
  int i;
  
  for (i = 0; i < NUM_CHANNELS; i++) {
    handleSwitch(&switches[i]);
  }
  
  checkCommandsOnSerial();
}

/** Handle the particular switch. */
void handleSwitch(struct Switch* sw) {
  if (digitalRead(sw->inputPin) == HIGH) {
    // Switch is pressed.
    if (sw->status == OFF) {
      if (sw->light->status == OFF) {
        switchOn(sw->light);
      } else {
        switchOff(sw->light);
      }
      
      sw->status = ON;
    }
  } else {
    if (sw->status == ON) {
      sw->status = OFF;
    }
  }
}

/** Switches the light on. */
void switchOn(struct Light* light) {
  digitalWrite(light->outputPin, HIGH);
  light->status = ON;
  EEPROM.write(light->eepromAddress, ON);
}

/** Switches the light off. */
void switchOff(struct Light* light) {
  digitalWrite(light->outputPin, LOW);
  light->status = OFF;
  EEPROM.write(light->eepromAddress, OFF);
}

/** Reports the current status over the serial line. Used for status updates or explicit requests. */
void reportCurrentStatus() {
  int i = 0;
  
  for (i = 0; i < NUM_CHANNELS; i++) {
    Serial.print(lights[i].status);
    Serial.print("000");
  }
  
  Serial.print('#');
}

/** Checks if a serial command is available, and executes it if necessary. */
void checkCommandsOnSerial() {
  if (Serial.available() >= 6) {
    char command[6];
    int currentIndex = 0;
    
    byte currentByte = Serial.read();
    
    while (currentByte != 35) {
      if (currentIndex < 5) {
        command[currentIndex] = (char)currentByte;
        currentIndex++;
      }
      
      currentByte = Serial.read();
    }
    
    command[5] = (char)0;
    
    executeCommand(command);
    
    Serial.flush();
    
    free(command);
  }
}

/** Executes the command as it was received from the controlling instance. */
void executeCommand(char command[]) {
  char opCode = command[0];
  int lightId = (int)command[1] - 48;
  
  if (lightId >= 0 && lightId <= 5) {
    if (opCode == 48) {
      // Switch light off...
      switchOff(&lights[lightId]);
      reportCurrentStatus();
    } else if (opCode == 49) {
      switchOn(&lights[lightId]);
      reportCurrentStatus();
    } else if (opCode == 50) {
      reportCurrentStatus();
    } else if (opCode == 51) {
      Serial.print(VERSION);
      Serial.print('#');
    } else if (opCode == 52) {
      Serial.print(BOARD_TYPE);
      Serial.print('#');
    } else if (opCode == 53) {
      Serial.print(BOARD_ID);
      Serial.print("#");
    } else if (opCode == 55) {
      Serial.print(NUM_CHANNELS);
      Serial.print("#");
    }
  }
}
      
    

int main(void)
{
	init();

	setup();
    
	for (;;)
		loop();
        
	return 0;
}

