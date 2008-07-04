#include "WProgram.h"
void loadFromEEPROM();
void handleSwitch(struct Switch* sw);
void handleGeneral();
void switchOn(struct Light* light);
void dimUp(struct Light* light);
void dimDown(struct Light* light);
void switchOff(struct Light* light);
void reportCurrentStatus();
void checkCommandsOnSerial();
void executeCommand(char command[]);
#include "EEPROM.h"
#define NUM_CHANNELS 5
#define DIM_STEP 1
#define SHORT_PRESS_DURATION 200
#define DIMMER_DELAY 10
#define VERSION 1
#define BOARD_ID 2
#define BOARD_TYPE 2

// Enumerate the statuses a channel can take (these are the outputs).
enum STATUS {
  OFF, ON
};

enum DIM_DIRECTION {
  DOWN, UP
};

// Enumerate the input pins.
enum INPUTPIN {
  INPUT_1 = 2,
  INPUT_2 = 4,
  INPUT_3 = 7,
  INPUT_4 = 8,
  INPUT_5 = 11,
  GENERAL = 12
};

// Enumerate the output pins.
enum OUTPUTPIN {
  OUTPUT_1 = 3,
  OUTPUT_2 = 5,
  OUTPUT_3 = 6,
  OUTPUT_4 = 9,
  OUTPUT_5 = 10
};

// Defines a light.
typedef struct Light {
  enum OUTPUTPIN outputPin;
  enum STATUS status;
  enum DIM_DIRECTION dimDirection;
  int value;
  int eepromAddress;
  int eepromValueAddress;
  int eepromDirectionAddress;
};

// Defines a switch.
typedef struct Switch {
  enum INPUTPIN inputPin;
  enum STATUS status;
  long pressStartTime;
  long lastDimTime;
  boolean ignoreShortPress;
  Light *light;
};

Switch switches[NUM_CHANNELS];
Light lights[NUM_CHANNELS];

void setup() {
  // Setup the lights first.
  lights[0].outputPin = OUTPUT_1;
  analogWrite(lights[0].outputPin, 0);
  lights[0].status = OFF;
  lights[0].value = 0;
  lights[0].eepromAddress = 0;
  lights[0].eepromValueAddress = 5;
  lights[0].eepromDirectionAddress = 10;
  
  lights[1].outputPin = OUTPUT_2;
  analogWrite(lights[1].outputPin, 0);
  lights[1].status = OFF;
  lights[1].value = 0;
  lights[1].eepromAddress = 1;
  lights[1].eepromValueAddress = 6;
  lights[1].eepromDirectionAddress = 11;
  
  lights[2].outputPin = OUTPUT_3;
  analogWrite(lights[2].outputPin, 0);
  lights[2].status = OFF;
  lights[2].value = 0;
  lights[2].eepromAddress = 2;
  lights[2].eepromValueAddress = 7;
  lights[2].eepromDirectionAddress = 12;
  
  lights[3].outputPin = OUTPUT_4;
  analogWrite(lights[3].outputPin, 0);
  lights[3].status = OFF;
  lights[3].value = 0;
  lights[3].eepromAddress = 3;
  lights[3].eepromValueAddress = 8;
  lights[3].eepromDirectionAddress = 13;
  
  lights[4].outputPin = OUTPUT_5;
  analogWrite(lights[4].outputPin, 0);
  lights[4].status = OFF;
  lights[4].value = 0;
  lights[4].eepromAddress = 4;
  lights[4].eepromValueAddress = 9;
  lights[4].eepromDirectionAddress = 14;
  
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
  
  pinMode(GENERAL, INPUT);
  
  loadFromEEPROM();
  
  Serial.begin(9600);
}

/** Loads previous values from the EEPROM. */
void loadFromEEPROM() {
  int i;
  
  for (i = 0; i < NUM_CHANNELS; i++) {
    int storedValue = EEPROM.read(lights[i].eepromValueAddress);
    
    if (storedValue > 255) {
      storedValue = 255;
    } else if (storedValue < 0) {
      storedValue = 0;
    }
    
    lights[i].value = storedValue;
    
    if (EEPROM.read(lights[i].eepromDirectionAddress) == UP) {
      lights[i].dimDirection = UP;
    } else {
      lights[i].dimDirection = DOWN;
    }
    
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
      sw->pressStartTime = millis();
      sw->lastDimTime = sw->pressStartTime;
      sw->status = ON;
      sw->ignoreShortPress = false;
    }
      
    if (sw->light->status == OFF) {
      switchOn(sw->light);
      sw->ignoreShortPress = true;
    }
    
    long currentTime = millis();
    
    if (currentTime - DIMMER_DELAY >= sw->lastDimTime) {
      if (sw->light->dimDirection == UP) {
        dimUp(sw->light);
      } else {
        dimDown(sw->light);
      }
      
      sw->lastDimTime = currentTime;
    }
  } else {
    if (sw->status == ON) {
      sw->status = OFF;
      
      long currentTime = millis();
      
      long pressDuration = currentTime - sw->pressStartTime;
      
      // Take account of possible overflow...
      if (currentTime < sw->pressStartTime) {
        pressDuration = 2,147,483,647 - sw->pressStartTime + currentTime;
      }
      
      if (pressDuration <= SHORT_PRESS_DURATION && !sw->ignoreShortPress) {
        switchOff(sw->light);
      }
      
      // Reverse direction and save to the EEPROM...
      if (sw->light->dimDirection == UP) {
        sw->light->dimDirection = DOWN;
        EEPROM.write(sw->light->eepromDirectionAddress, DOWN);
      } else {
        sw->light->dimDirection = UP;
        EEPROM.write(sw->light->eepromDirectionAddress, UP);
      }
      
      EEPROM.write(sw->light->eepromValueAddress, sw->light->value);
      
      reportCurrentStatus();
    }
  }
}

/** Handles the general switch (on input pin 7) */
void handleGeneral() {
  if (digitalRead(GENERAL) == HIGH) {
    int i;
    
    for (i = 0; i < NUM_CHANNELS; i++) {
      switchOff(&lights[i]);
    }
  }
}

/** Switches the light on. */
void switchOn(struct Light* light) {
  analogWrite(light->outputPin, light->value);
  light->status = ON;
  light->dimDirection = UP;
  EEPROM.write(light->eepromAddress, ON);
}

/** Dims the light up with a fixed step. */
void dimUp(struct Light* light) {
  int newValue = light->value + DIM_STEP;
  
  if (newValue > 255) {
    newValue = 255;
  }
  
  analogWrite(light->outputPin, newValue);
  light->value = newValue;
}

/** Dims the light down with a fixed step. */
void dimDown(struct Light* light) {
  int newValue = light->value - DIM_STEP;
  
  if (newValue < 0) {
    newValue = 0;
  }
  
  analogWrite(light->outputPin, newValue);
  light->value = newValue;
}

/** Switches the light off. */
void switchOff(struct Light* light) {
  analogWrite(light->outputPin, 0);
  light->status = OFF;
  EEPROM.write(light->eepromAddress, OFF);
}

/** Reports the current status over the serial line. Used for status updates or explicit requests. */
void reportCurrentStatus() {
  int i = 0;
  
  for (i = 0; i < NUM_CHANNELS; i++) {
    Serial.print(lights[i].status);
    
    if (lights[i].value < 100) {
      Serial.print(0);
    }
    
    if (lights[i].value < 10) {
      Serial.print(0);
    }
    
    Serial.print(lights[i].value);
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
  int dimValue = (((int)command[2] - 48) * 100) + (((int)command[3] - 48) * 10) + ((int)command[4] - 48);
  
  if (lightId >= 0 && lightId <= 5) {
    if (opCode == 48) {
      // Switch light off...
      switchOff(&lights[lightId]);
      reportCurrentStatus();
    } else if (opCode == 49) {
      // Switch light on...
      switchOn(&lights[lightId]);
      reportCurrentStatus();
    } else if (opCode == 50) {
      // Report status...
      reportCurrentStatus();
    } else if (opCode == 51) {
      // Report version...
      Serial.print(VERSION);
      Serial.print('#');
    } else if (opCode == 52) {
      // Report type of board...
      Serial.print(BOARD_TYPE);
      Serial.print('#');
    } else if (opCode == 53) {
      // Report the ID of the board...
      Serial.print(BOARD_ID);
      Serial.print("#");
    } else if (opCode == 54) {
      // Dim function...
      if (dimValue >= 255) {
        dimValue = 255;
      }
      
      if (dimValue <= 0) {
        dimValue = 0;
      }
      
      if (!lights[lightId].status == OFF) {
        analogWrite(lights[lightId].outputPin, dimValue);
      }
      
      lights[lightId].value = dimValue;
      reportCurrentStatus();
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

