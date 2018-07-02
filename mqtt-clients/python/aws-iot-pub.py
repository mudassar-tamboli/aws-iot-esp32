# Author : Mudassar Tamboli

# Description:
# Demonstrates how to use AWS IoT Python SDK and securely communicate with 
# ESP32 Thing using MQTT Pub/Sub protocol
# It Publishes "Toggle" pattern where all Red LEDs are alternately set On/Off 

# import modules
from AWSIoTPythonSDK.MQTTLib import AWSIoTMQTTClient
import time
import json

# wait for ever
def blockForever():

    while 1: 
        time.sleep(0.1)

def publishToggle( esp32AWSIoTMQTTClient ):
   
    pub_topic_red_led_status = "secure/led/red/command"
    print("\n Publishing \"Toggle\" to topic " + pub_topic_red_led_status)
    loopCount = 0
    while True:
        message = {}
       
        if loopCount % 2 == 0 :
            message['LED1'] = "ON"
            message['LED2'] = "ON"
            message['LED3'] = "ON"
        else :
            message['LED1'] = "OFF"
            message['LED2'] = "OFF"
            message['LED3'] = "OFF"

        messageJson = json.dumps(message)

        if esp32AWSIoTMQTTClient.publish(pub_topic_red_led_status, messageJson, 1) == False: 
            print("Failed to publish to topic " + pub_topic_red_led_status)
            exit(2)

        loopCount += 1
        time.sleep(1)


# main    
def main():    

    # AWS IoT Credentials
    hostEndpoint    = "xxxxxxxxxxxxx.iot.us-west-2.amazonaws.com"
    rootCAPath      = "../../cert/VeriSign-Class 3-Public-Primary-Certification-Authority-G5.pem"
    certificatePath = "../../cert/73d79ad78b-certificate.pem.crt"
    privateKeyPath  = "../../cert/73d79ad78b-private.pem.key"
    clientId        = "python-client-master"
    mqttPort        = 8883

    # AWS IoT MQTT Broker prepare login
    esp32AWSIoTMQTTClient = AWSIoTMQTTClient(clientId)
    esp32AWSIoTMQTTClient.configureEndpoint(hostEndpoint, mqttPort)
    esp32AWSIoTMQTTClient.configureCredentials(rootCAPath, privateKeyPath, certificatePath)

    # Configure logging
    #logger = logging.getLogger("AWSIoTPythonSDK.core")
    #logger.setLevel(logging.DEBUG)
    #streamHandler = logging.StreamHandler()
    #formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    #streamHandler.setFormatter(formatter)
    #logger.addHandler(streamHandler)

    # AWSIoTMQTTClient connection configuration
    esp32AWSIoTMQTTClient.configureAutoReconnectBackoffTime(1, 32, 20)
    esp32AWSIoTMQTTClient.configureOfflinePublishQueueing(-1)  # Infinite offline Publish queueing
    esp32AWSIoTMQTTClient.configureDrainingFrequency(2)  # Draining: 2 Hz
    esp32AWSIoTMQTTClient.configureConnectDisconnectTimeout(10)  # 10 sec
    esp32AWSIoTMQTTClient.configureMQTTOperationTimeout(5)  # 5 sec


    # Connect to AWS IoT MQTT Broker
    if esp32AWSIoTMQTTClient.connect() == True:
        print("\n " + clientId + " successfully connected to " + hostEndpoint)
    else:
        print(" " + clientId + " failed to connect to " + hostEndpoint)
        exit(2)

    # Publish to a topic in a loop forever
    publishToggle(esp32AWSIoTMQTTClient)

    # sleep forever
    blockForever()

if __name__ == "__main__":
    main()