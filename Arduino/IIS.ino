/*    
 * Gardening.c
 * Gardening Demo for Arduino 
 *   
 * Copyright (c) 2015 seeed technology inc.  
 * Author      : Jiankai.li  
 * Create Time:  Aug 2015
 * Change Log : 
 *
 * The MIT License (MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
#include <Wire.h>
#include <SeeedOLED.h>
#include <EEPROM.h>
#include "DHT.h"
#include <TimerOne.h>
#include "Arduino.h"
#include "SI114X.h"



enum Status 
{
    Standby  =  0,
    Warning  =  1,
    Setting   = 2,
    Watering =  3,
};
typedef enum Status Systemstatus;
Systemstatus WorkingStatus;


enum EncoderDir
{
    Anticlockwise = 0,
    Clockwise     = 1,
};
typedef enum EncoderDir EncodedirStatus;
EncodedirStatus EncoderRoateDir;


enum WarningStatus
{
    NoWarning          = 0,
    AirHumidityWarning = 1,
    AirTemperWarning   = 2,
    UVIndexWarning     = 3,
    NoWaterWarning     = 4,
};
typedef enum WarningStatus WarningStatusType;
WarningStatusType SystemWarning;


struct Limens 
{
    unsigned char UVIndex_Limen       = 9;
    unsigned char DHTHumidity_Hi      = 60;
    unsigned char DHTHumidity_Low     = 0;
    unsigned char DHTTemperature_Hi   = 30;
    unsigned char DHTTemperature_Low  = 0;
    unsigned char MoisHumidity_Limen  = 0;
    float         WaterVolume         = 0.2;
};
typedef struct Limens WorkingLimens;
WorkingLimens SystemLimens;

#define DHTPIN          A0     // what pin we're connected to
#define MoisturePin     A1
#define ButtonPin       2
// Uncomment whatever type you're using!
#define DHTTYPE DHT11   // DHT 11 
//#define DHTTYPE DHT22   // DHT 22  (AM2302)
//#define DHTTYPE DHT21   // DHT 21 (AM2301)
#define EncoderPin1     3
#define EncoderPin2     4
#define WaterflowPin    5
#define RelayPin        6

#define OneSecond       1000
#define DataUpdateInterval 10000  // 20S
#define RelayOn         HIGH
#define RelayOff        LOW

#define NoWaterTimeOut  3        // 10s

unsigned int  uiWaterVolume = 0;
unsigned char WaterflowFlag = 0;
unsigned int  WaterflowRate = 0;  // L/Hour
unsigned int  NbTopsFan     = 0;  // count the edges

unsigned char EncoderFlag = 0;
unsigned long StartTime   = 0;
unsigned char ButtonFlag  = 0;
signed   char LCDPage     = 4;
unsigned char SwitchtoWateringFlag = 0;
unsigned char SwitchtoWarningFlag  = 0;
unsigned char SwitchtoStandbyFlag  = 0;
unsigned char UpdateDataFlag  = 0;
unsigned char ButtonIndex = 0;
unsigned char EEPROMAddress = 0;
float Volume     = 0;
unsigned long counter = 0;

SI114X SI1145 = SI114X();
DHT dht(DHTPIN, DHTTYPE);
float DHTHumidity    = 0;
float DHTTemperature = 0;
float MoisHumidity   = 100;
float UVIndex        = 0;
char buffer[30];

void setup() 
{
    /* Init OLED */
    Wire.begin();
    SeeedOled.init();  //initialze SEEED OLED display
    DDRB|=0x21;        
    PORTB |= 0x21;
    SeeedOled.clearDisplay();          //clear the screen and set start position to top left corner
    SeeedOled.setNormalDisplay();      //Set display to normal mode (i.e non-inverse mode)
    SeeedOled.setPageMode();           //Set addressing mode to Page Mode

//    encoder.Timer_init();
    /* Init DHT11 */
    Serial.begin(9600); 
    dht.begin();
    
    /* Init Button */
    pinMode(ButtonPin,INPUT);
    attachInterrupt(0,ButtonClick,FALLING);
    /* Init Encoder */
    pinMode(EncoderPin1,INPUT);
    pinMode(EncoderPin2,INPUT);

    /* Init UV */
    while (!SI1145.Begin()) {
        delay(1000);
    }

    
    /* Init Water flow */
    pinMode(WaterflowPin,INPUT);
    
    /* Init Relay      */
    pinMode(RelayPin,OUTPUT);
    /* The First time power on to write the default data to EEPROM */
    if (EEPROM.read(EEPROMAddress) == 0xff) {
        EEPROM.write(EEPROMAddress,0x00);
        EEPROM.write(++EEPROMAddress,SystemLimens.UVIndex_Limen);
        EEPROM.write(++EEPROMAddress,SystemLimens.DHTHumidity_Hi);
        EEPROM.write(++EEPROMAddress,SystemLimens.DHTHumidity_Low);
        EEPROM.write(++EEPROMAddress,SystemLimens.DHTTemperature_Hi);
        EEPROM.write(++EEPROMAddress,SystemLimens.DHTTemperature_Low);
        EEPROM.write(++EEPROMAddress,SystemLimens.MoisHumidity_Limen);
        EEPROM.write(++EEPROMAddress,((int)(SystemLimens.WaterVolume*100))/255);    /*  */
        EEPROM.write(++EEPROMAddress,((int)(SystemLimens.WaterVolume*100))%255);
    } else { /* If It's the first time power on , read the last time data */
        EEPROMAddress++;
        SystemLimens.UVIndex_Limen      = EEPROM.read(EEPROMAddress++);
        SystemLimens.DHTHumidity_Hi     = EEPROM.read(EEPROMAddress++);
        SystemLimens.DHTHumidity_Low    = EEPROM.read(EEPROMAddress++);
        SystemLimens.DHTTemperature_Hi  = EEPROM.read(EEPROMAddress++);
        SystemLimens.DHTTemperature_Low = EEPROM.read(EEPROMAddress++);
        SystemLimens.MoisHumidity_Limen = EEPROM.read(EEPROMAddress++);
        SystemLimens.WaterVolume =   (EEPROM.read(EEPROMAddress++)*255 + EEPROM.read(EEPROMAddress))/100.0;
    }
    
    StartTime = millis();
    WorkingStatus = Standby;
    SystemWarning = NoWarning;
}


void loop() 
{
    // Reading temperature or humidity takes about 250 milliseconds!
    // Sensor readings may also be up to 2 seconds 'old' (its a very slow sensor)

    StartTime      = millis();
    DHTHumidity    = dht.readHumidity();
    DHTTemperature = dht.readTemperature();
    MoisHumidity   = analogRead(MoisturePin);
    UVIndex        = (float)SI1145.ReadUV()/100 + 0.5;

    Serial.println(UVIndex);
    Serial.println(DHTTemperature);
    Serial.println(MoisHumidity);

    delay(10000);
    
      char state = Serial.read();

        if(state == '1'){
          WaterPumpOn();
        } else if(state == '0'){
          WaterPumpOff();
        }
      
//      if(state.length() > 0){
//        int waterPumpState = atoi(state.c_str());
//        if(waterPumpState == 1){
//          WaterPumpOn();
//          Serial.print("Start raining ");
//          Serial.println(waterPumpState);
//        } else if(waterPumpState == 0) {
//           WaterPumpOff();
//           Serial.print("Stop raining ");
//           Serial.println(waterPumpState);
//        }
//      }

}

void WaterPumpOn()
{
    digitalWrite(RelayPin,RelayOn);
}

void WaterPumpOff()
{
    digitalWrite(RelayPin,RelayOff);
}


void ButtonClick()
{
    
    if(digitalRead(ButtonPin) == 0){
        delay(10);
        if(digitalRead(ButtonPin) == 0){
            ButtonFlag = 1;
        }
    }  
}
