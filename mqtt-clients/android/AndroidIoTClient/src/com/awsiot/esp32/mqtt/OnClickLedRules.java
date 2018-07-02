package com.awsiot.esp32.mqtt;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class OnClickLedRules {

    static final String LOG_TAG = "OnClickLedRules";

    static final String PUB_TOPIC_LED_GREEN_COMMAND = "secure/led/green/command";
    static final String PUB_TOPIC_LED_BLUE_COMMAND  = "secure/led/blue/command";
    static final String PUB_TOPIC_LED_RED_COMMAND   = "secure/led/red/command";
    static final String PUB_TOPIC_LED_ESP32_COMMAND = "secure/led/esp32/command";
    
    static final int LED_OFF    = 0;
    static final int LED_ON     = 1;
    static final int LED_BLINK  = 10;

    static int stateGreenLed1, stateGreenLed2, stateGreenLed3;
    static int stateBlueLed1, stateBlueLed2, stateBlueLed3;
    static int stateRedLed1, stateRedLed2, stateRedLed3;

    static AWSIotMqttManager mqttManager;

    static final JSONObject json1On = new JSONObject();
    static final JSONObject json2On = new JSONObject();
    static final JSONObject json3On = new JSONObject();

    static final JSONObject json1Off = new JSONObject();
    static final JSONObject json2Off = new JSONObject();
    static final JSONObject json3Off = new JSONObject();

    static final JSONObject json1Blink = new JSONObject();
    static final JSONObject json2Blink = new JSONObject();
    static final JSONObject json3Blink = new JSONObject();
    
    public static void InitOnClickLedRules()
    {
        stateGreenLed1 = stateGreenLed2 = stateGreenLed3 = LED_OFF;
        stateBlueLed1  = stateBlueLed2  = stateBlueLed3  = LED_OFF;
        stateRedLed1   = stateRedLed2   = stateRedLed3   = LED_OFF;

        try { json1Off.put("LED1", "OFF");} catch (Exception e) {}
        try { json2Off.put("LED2", "OFF");} catch (Exception e) {}
        try { json3Off.put("LED3", "OFF");} catch (Exception e) {}
        try { json1On.put("LED1", "ON");} catch (Exception e) {}
        try { json2On.put("LED2", "ON");} catch (Exception e) {}
        try { json3On.put("LED3", "ON");} catch (Exception e) {}
        try { json1Blink.put("LED1", "BLINK");} catch (Exception e) {}
        try { json2Blink.put("LED2", "BLINK");} catch (Exception e) {}
        try { json3Blink.put("LED3", "BLINK");} catch (Exception e) {}
        
    }

    static public void setGreenLed1OnClicks(ImageView imgGreenLed1)
    {
        
        imgGreenLed1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (stateGreenLed1 == LED_ON || stateGreenLed1 == LED_BLINK ) {
                    stateGreenLed1 = LED_OFF;
                    publishMesgToTopic(PUB_TOPIC_LED_GREEN_COMMAND, json1Off.toString());
                } else {
                    stateGreenLed1 = LED_ON;
                    publishMesgToTopic(PUB_TOPIC_LED_GREEN_COMMAND, json1On.toString());
                }
            }
        });

        imgGreenLed1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (stateGreenLed1 == LED_BLINK) {
                    stateGreenLed1 = LED_OFF;
                    publishMesgToTopic(PUB_TOPIC_LED_GREEN_COMMAND, json1Off.toString());
                } else {
                    stateGreenLed1 = LED_BLINK;
                    publishMesgToTopic(PUB_TOPIC_LED_GREEN_COMMAND, json1Blink.toString());
                }
                return true;
            }
        });

    }


    static public void setGreenLed2OnClicks(ImageView imgGreenLed2)
    {
        imgGreenLed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (stateGreenLed2 == LED_ON || stateGreenLed2 == LED_BLINK ) {
                    stateGreenLed2 = LED_OFF;
                    publishMesgToTopic(PUB_TOPIC_LED_GREEN_COMMAND, json2Off.toString());
                } else {
                    stateGreenLed2 = LED_ON;
                    publishMesgToTopic(PUB_TOPIC_LED_GREEN_COMMAND, json2On.toString());
                }
            }
        });

        imgGreenLed2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (stateGreenLed2 == LED_BLINK) {
                    stateGreenLed2 = LED_OFF;
                    publishMesgToTopic(PUB_TOPIC_LED_GREEN_COMMAND, json2Off.toString());
                } else {
                    stateGreenLed2 = LED_BLINK;
                    publishMesgToTopic(PUB_TOPIC_LED_GREEN_COMMAND, json2Blink.toString());
                }
                return true;
            }
        });

    }


    static public void setGreenLed3OnClicks(ImageView imgGreenLed3)
    {
        imgGreenLed3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (stateGreenLed3 == LED_ON || stateGreenLed3 == LED_BLINK ) {
                    stateGreenLed3 = LED_OFF;
                    publishMesgToTopic(PUB_TOPIC_LED_GREEN_COMMAND, json3Off.toString());
                } else {
                    stateGreenLed3 = LED_ON;
                    publishMesgToTopic(PUB_TOPIC_LED_GREEN_COMMAND, json3On.toString());
                }
            }
        });

        imgGreenLed3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (stateGreenLed3 == LED_BLINK) {
                    stateGreenLed3 = LED_OFF;
                    publishMesgToTopic(PUB_TOPIC_LED_GREEN_COMMAND, json3Off.toString());
                } else {
                    stateGreenLed3 = LED_BLINK;
                    publishMesgToTopic(PUB_TOPIC_LED_GREEN_COMMAND, json3Blink.toString());
                }
                return true;
            }
        });

    }

////////////////////////////////////////// Blue //////////////////////////////////////////

    static public void setBlueLed1OnClicks(ImageView imgBlueLed1)
    {
        imgBlueLed1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (stateBlueLed1 == LED_ON || stateBlueLed1 == LED_BLINK ) {
                    stateBlueLed1 = LED_OFF;
                    publishMesgToTopic(PUB_TOPIC_LED_BLUE_COMMAND, json1Off.toString());
                } else {
                    stateBlueLed1 = LED_ON;
                    publishMesgToTopic(PUB_TOPIC_LED_BLUE_COMMAND, json1On.toString());
                }
            }
        });

        imgBlueLed1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (stateBlueLed1 == LED_BLINK) {
                    stateBlueLed1 = LED_OFF;
                    publishMesgToTopic(PUB_TOPIC_LED_BLUE_COMMAND, json1Off.toString());
                } else {
                    stateBlueLed1 = LED_BLINK;
                    publishMesgToTopic(PUB_TOPIC_LED_BLUE_COMMAND, json1Blink.toString());
                }
                return true;
            }
        });

    }


    static public void setBlueLed2OnClicks(ImageView imgBlueLed2) {
        imgBlueLed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (stateBlueLed2 == LED_ON || stateBlueLed2 == LED_BLINK ) {
                    stateBlueLed2 = LED_OFF;
                    publishMesgToTopic(PUB_TOPIC_LED_BLUE_COMMAND, json2Off.toString());
                } else {
                    stateBlueLed2 = LED_ON;
                    publishMesgToTopic(PUB_TOPIC_LED_BLUE_COMMAND, json2On.toString());
                }
            }
        });

        imgBlueLed2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (stateBlueLed2 == LED_BLINK) {
                    stateBlueLed2 = LED_OFF;
                    publishMesgToTopic(PUB_TOPIC_LED_BLUE_COMMAND, json2Off.toString());
                } else {
                    stateBlueLed2 = LED_BLINK;
                    publishMesgToTopic(PUB_TOPIC_LED_BLUE_COMMAND, json2Blink.toString());
                }
                return true;
            }
        });
    }

    static public void setBlueLed3OnClicks(ImageView imgBlueLed3)
    {
        imgBlueLed3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (stateBlueLed3 == LED_ON || stateBlueLed3 == LED_BLINK ) {
                    stateBlueLed3 = LED_OFF;
                    publishMesgToTopic(PUB_TOPIC_LED_BLUE_COMMAND, json3Off.toString());
                } else {
                    stateBlueLed3 = LED_ON;
                    publishMesgToTopic(PUB_TOPIC_LED_BLUE_COMMAND, json3On.toString());
                }
            }
        });

        imgBlueLed3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (stateBlueLed3 == LED_BLINK) {
                    stateBlueLed3 = LED_OFF;
                    publishMesgToTopic(PUB_TOPIC_LED_BLUE_COMMAND, json3Off.toString());
                } else {
                    stateBlueLed3 = LED_BLINK;
                    publishMesgToTopic(PUB_TOPIC_LED_BLUE_COMMAND, json3Blink.toString());
                }
                return true;
            }
        });

    }

////////////////////////////////////////// Red //////////////////////////////////////////

    static public void setRedLed1OnClicks(ImageView imgRedLed1)
    {
        imgRedLed1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (stateRedLed1 == LED_ON || stateRedLed1 == LED_BLINK ) {
                    stateRedLed1 = LED_OFF;
                    publishMesgToTopic(PUB_TOPIC_LED_RED_COMMAND, json1Off.toString());
                } else {
                    stateRedLed1 = LED_ON;
                    publishMesgToTopic(PUB_TOPIC_LED_RED_COMMAND, json1On.toString());
                }
            }
        });

        imgRedLed1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (stateRedLed1 == LED_BLINK) {
                    stateRedLed1 = LED_OFF;
                    publishMesgToTopic(PUB_TOPIC_LED_RED_COMMAND, json1Off.toString());
                } else {
                    stateRedLed1 = LED_BLINK;
                    publishMesgToTopic(PUB_TOPIC_LED_RED_COMMAND, json1Blink.toString());
                }
                return true;
            }
        });

    }


    static public void setRedLed2OnClicks(ImageView imgRedLed2) {
        imgRedLed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (stateRedLed2 == LED_ON || stateRedLed2 == LED_BLINK ) {
                    stateRedLed2 = LED_OFF;
                    publishMesgToTopic(PUB_TOPIC_LED_RED_COMMAND, json2Off.toString());
                } else {
                    stateRedLed2 = LED_ON;
                    publishMesgToTopic(PUB_TOPIC_LED_RED_COMMAND, json2On.toString());
                }
            }
        });

        imgRedLed2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (stateRedLed2 == LED_BLINK) {
                    stateRedLed2 = LED_OFF;
                    publishMesgToTopic(PUB_TOPIC_LED_RED_COMMAND, json2Off.toString());
                } else {
                    stateRedLed2 = LED_BLINK;
                    publishMesgToTopic(PUB_TOPIC_LED_RED_COMMAND, json2Blink.toString());
                }
                return true;
            }
        });
    }

    static public void setRedLed3OnClicks(ImageView imgRedLed3)
    {
        imgRedLed3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (stateRedLed3 == LED_ON || stateRedLed3 == LED_BLINK ) {
                    stateRedLed3 = LED_OFF;
                    publishMesgToTopic(PUB_TOPIC_LED_RED_COMMAND, json3Off.toString());
                } else {
                    stateRedLed3 = LED_ON;
                    publishMesgToTopic(PUB_TOPIC_LED_RED_COMMAND, json3On.toString());
                }
            }
        });

        imgRedLed3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (stateRedLed3 == LED_BLINK) {
                    stateRedLed3 = LED_OFF;
                    publishMesgToTopic(PUB_TOPIC_LED_RED_COMMAND, json3Off.toString());
                } else {
                    stateRedLed3 = LED_BLINK;
                    publishMesgToTopic(PUB_TOPIC_LED_RED_COMMAND, json3Blink.toString());
                }
                return true;
            }
        });

    }

    static void publishMesgToTopic(String topic, String msg)
    {
        try {
            mqttManager.publishString(msg, topic, AWSIotMqttQos.QOS0);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Publish error.", e);
        }

        return;
    }


}
