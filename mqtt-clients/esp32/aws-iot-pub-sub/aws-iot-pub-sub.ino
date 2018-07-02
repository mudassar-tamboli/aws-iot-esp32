
// Author : Mudassar Tamboli

// Description:

// ESP32 is programmed to connect to AWS IoT and securely communicate with 
// with other MQTT clients using MQTT Pub/Sub protocol. It uses "hornbill" library.
// It controls 3x3x3 LED matrix where row 1 is Green, row 2 is Blue and row 3 is Red
// It subcribes for commands to set LEDs On/Off and publishes status of updated LEDs.
// It also subscribes for esp32 restricted commands like RESET that sets all Leds OFF.

// The certificates and private key are saved as array of bytes 
// in <InstallDir>\Arduino\libraries\AWS_IOT\src\aws_iot_certifcates.c

#include <AWS_IOT.h>
#include <WiFi.h>
#include <ArduinoJson.h>

AWS_IOT hornbill;

char WIFI_SSID[]     ="SSID";
char WIFI_PASSWORD[] ="Password";

char HOST_ADDRESS[] = "xxxxxxxxxxxxx.iot.us-west-2.amazonaws.com";
char CLIENT_ID[] = "arduino-esp32-owner";

char SUB_TOPIC_GREEN_LED_COMMAND[] = "secure/led/green/command";
char SUB_TOPIC_BLUE_LED_COMMAND[]  = "secure/led/blue/command";
char SUB_TOPIC_RED_LED_COMMAND[]   = "secure/led/red/command";

char PUB_TOPIC_GREEN_LED_STATUS[] = "secure/led/green/status";
char PUB_TOPIC_BLUE_LED_STATUS[]  = "secure/led/blue/status";
char PUB_TOPIC_RED_LED_STATUS[]   = "secure/led/red/status";

char PUB_TOPIC_ESP32_STATUS[]      = "secure/esp32/status";
char SUB_TOPIC_ESP32_RESTRICTED[]  = "secure/esp32/restricted";

int status = WL_IDLE_STATUS;


int LED_GREEN_1 = 33;
int LED_GREEN_2 = 32;
int LED_GREEN_3 = 23;

int LED_BLUE_1 = 25;
int LED_BLUE_2 = 26;
int LED_BLUE_3 = 27;

int LED_RED_1 = 14;
int LED_RED_2 = 12;
int LED_RED_3 = 13;

void AWSSubCallBackHandler (char *topicName, int payloadLen, char *payLoad)
{
    char rcvdPayload[512];
    char pubPayload[512];
    String strTopic;
    StaticJsonBuffer<200> jsonBuffer;
    strncpy(rcvdPayload,payLoad,payloadLen);
    rcvdPayload[payloadLen] = 0;
    strTopic = topicName;

    Serial.println(rcvdPayload);
    memcpy(pubPayload, rcvdPayload, sizeof(pubPayload));
    
    JsonObject& root = jsonBuffer.parseObject(rcvdPayload);
    
    String LED1State = root["LED1"];
    String LED2State = root["LED2"];
    String LED3State = root["LED3"];

    if (strTopic.indexOf(SUB_TOPIC_GREEN_LED_COMMAND) != -1) {

        if (LED1State == "ON") { 
            digitalWrite(LED_GREEN_1, HIGH);
        }
        if (LED2State == "ON") {
            digitalWrite(LED_GREEN_2, HIGH);
        }
        if (LED3State == "ON") {
            digitalWrite(LED_GREEN_3, HIGH);
        }
        
        if (LED1State == "OFF")   {
            digitalWrite(LED_GREEN_1, LOW);
        }
        if (LED2State == "OFF") {
            digitalWrite(LED_GREEN_2, LOW);
        }
        if (LED3State == "OFF") {
            digitalWrite(LED_GREEN_3, LOW);
        }

        if (hornbill.publish(PUB_TOPIC_GREEN_LED_STATUS, pubPayload) == 0) {
            Serial.println("Successfully published to topic secure/led/green/status");
        } else {
            Serial.println("Failed to publish to topic secure/led/green/status");
        }
    }

    if (strTopic.indexOf(SUB_TOPIC_BLUE_LED_COMMAND) != -1) {
      
        if (LED1State == "ON") {
            digitalWrite(LED_BLUE_1, HIGH);
        }
        if (LED2State == "ON") {
            digitalWrite(LED_BLUE_2, HIGH);
        }
        if (LED3State == "ON") {
            digitalWrite(LED_BLUE_3, HIGH);
        }
        
        if (LED1State == "OFF") {
            digitalWrite(LED_BLUE_1, LOW);
        }
        if (LED2State == "OFF") {
            digitalWrite(LED_BLUE_2, LOW);
        }
        if (LED3State == "OFF") {
            digitalWrite(LED_BLUE_3, LOW);
        }

        if (hornbill.publish(PUB_TOPIC_BLUE_LED_STATUS, pubPayload) == 0) {
            Serial.println("Successfully published to topic secure/led/blue/status");
        } else {
            Serial.println("Failed to publish to topic secure/led/blue/status");
        }

    }

    if (strTopic.indexOf(SUB_TOPIC_RED_LED_COMMAND) != -1) {
      
        if (LED1State == "ON") {
            digitalWrite(LED_RED_1, HIGH);
        }
        if (LED2State == "ON") {
            digitalWrite(LED_RED_2, HIGH);
        }
        if (LED3State == "ON") {
            digitalWrite(LED_RED_3, HIGH);
        }
        
        if (LED1State == "OFF") {
            digitalWrite(LED_RED_1, LOW);
        }
        if (LED2State == "OFF") {
            digitalWrite(LED_RED_2, LOW);
        }
        if (LED3State == "OFF") {
            digitalWrite(LED_RED_3, LOW);
        }

        if (hornbill.publish(PUB_TOPIC_RED_LED_STATUS, pubPayload) == 0) {
            Serial.println("Successfully published to topic secure/led/red/status");
        } else {
            Serial.println("Failed to publish to topic secure/led/red/status");
        }

    }

    if (strTopic.indexOf(SUB_TOPIC_ESP32_RESTRICTED) != -1) {
        
        String cmd = root["CMD"];

        if (cmd == "RESET") {
          
            switchAllLedsOff();

            hornbill.publish(PUB_TOPIC_GREEN_LED_STATUS, "{\"LED1\" : \"OFF\"}");
            hornbill.publish(PUB_TOPIC_GREEN_LED_STATUS, "{\"LED2\" : \"OFF\"}");
            hornbill.publish(PUB_TOPIC_GREEN_LED_STATUS, "{\"LED3\" : \"OFF\"}");

            hornbill.publish(PUB_TOPIC_BLUE_LED_STATUS, "{\"LED1\" : \"OFF\"}");
            hornbill.publish(PUB_TOPIC_BLUE_LED_STATUS, "{\"LED2\" : \"OFF\"}");
            hornbill.publish(PUB_TOPIC_BLUE_LED_STATUS, "{\"LED3\" : \"OFF\"}");

            hornbill.publish(PUB_TOPIC_RED_LED_STATUS, "{\"LED1\" : \"OFF\"}");
            hornbill.publish(PUB_TOPIC_RED_LED_STATUS, "{\"LED2\" : \"OFF\"}");
            hornbill.publish(PUB_TOPIC_RED_LED_STATUS, "{\"LED3\" : \"OFF\"}");

        }

    }
}

void switchAllLedsOn()
{
    digitalWrite(LED_GREEN_1, HIGH); delay(100);digitalWrite(LED_GREEN_2, HIGH); delay(100);digitalWrite(LED_GREEN_3, HIGH); delay(100);
    digitalWrite(LED_BLUE_1, HIGH); delay(100);digitalWrite(LED_BLUE_2, HIGH); delay(100);digitalWrite(LED_BLUE_3, HIGH); delay(100);
    digitalWrite(LED_RED_1, HIGH); delay(100);digitalWrite(LED_RED_2, HIGH); delay(100);digitalWrite(LED_RED_3, HIGH); delay(100);
}

void switchAllLedsOff()
{
    digitalWrite(LED_GREEN_1, LOW); delay(100);digitalWrite(LED_GREEN_2, LOW); delay(100);digitalWrite(LED_GREEN_3, LOW); delay(100);
    digitalWrite(LED_BLUE_1, LOW); delay(100);digitalWrite(LED_BLUE_2, LOW); delay(100);digitalWrite(LED_BLUE_3, LOW); delay(100);
    digitalWrite(LED_RED_1, LOW); delay(100);digitalWrite(LED_RED_2, LOW); delay(100);digitalWrite(LED_RED_3, LOW); delay(100);
}

void setup() {
  
    Serial.begin(115200);

    pinMode(LED_GREEN_1, OUTPUT); pinMode(LED_GREEN_2, OUTPUT); pinMode(LED_GREEN_3, OUTPUT); 
    pinMode(LED_BLUE_1, OUTPUT);  pinMode(LED_BLUE_2, OUTPUT);  pinMode(LED_BLUE_3, OUTPUT); 
    pinMode(LED_RED_1, OUTPUT);   pinMode(LED_RED_2, OUTPUT);   pinMode(LED_RED_3, OUTPUT); 

    switchAllLedsOn();
    
    while (status != WL_CONNECTED)
    {
        Serial.print("Attempting to connect to SSID: ");
        Serial.println(WIFI_SSID);
        // Connect to WPA/WPA2 network. Change this line if using open or WEP network:
        status = WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

        // wait 5 seconds for connection:
        delay(5000);
    }

    Serial.println("Connected to wifi");

    if(hornbill.connect(HOST_ADDRESS,CLIENT_ID)== 0)
    {
        Serial.println("Connected to AWS");
        delay(1000);
       if(0==hornbill.subscribe(SUB_TOPIC_GREEN_LED_COMMAND, AWSSubCallBackHandler))
        {
            Serial.println("Successfully subscribed to topic secure/led/green/command");
        }
        else
        {
            Serial.println("Green Subscribe Failed, Check the Thing Name and Certificates");
            while(1);
        }
 
        if(0==hornbill.subscribe(SUB_TOPIC_BLUE_LED_COMMAND, AWSSubCallBackHandler))
        {
            Serial.println("Successfully subscribed to topic secure/led/blue/command");
        }
        else
        {
            Serial.println("Subscribe Failed, Check the Thing Name and Certificates");
            while(1);
        }
 
        if(0==hornbill.subscribe(SUB_TOPIC_RED_LED_COMMAND, AWSSubCallBackHandler))
        {
            Serial.println("Successfully subscribed to topic secure/led/red/command");
        }
        else
        {
            Serial.println("Subscribe Failed, Check the Thing Name and Certificates");
            while(1);
        }
    
        if(0==hornbill.subscribe(SUB_TOPIC_ESP32_RESTRICTED, AWSSubCallBackHandler))
        {
            Serial.println("Successfully subscribed to topic secure/esp32/restricted");
        }
        else
        {
            Serial.println("Subscribe Failed, Check the Thing Name and Certificates");
            while(1);
        }

    }
    else
    {
        Serial.println("AWS connection failed, Check the HOST Address");
        while(1);
    }

    switchAllLedsOff();

}

void loop() {
    delay(10);
}


