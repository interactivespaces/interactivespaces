/*
  Echo
 
 Write back on the serial connection what is written to it.
 
 This is for demonstrating Interactve Spaces serial communication.
 
 created 1/2013
 by Keith Hughes 
 */

void setup()
{
  // initialize the serial communication:
  Serial.begin(9600);
}

void loop() {
  // Check if data has been recieved from the computer:
  if (Serial.available()) {
    // read the most recent byte:
    byte value = Serial.read();
    
    // Write it back.
    Serial.write(value);
  }
}

