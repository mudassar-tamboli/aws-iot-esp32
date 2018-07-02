# Author : Mudassar Tamboli

# Description:
# Demonstrates how to use AWS IoT Python SDK and securely communicate with 
# ESP32 Thing using MQTT Pub/Sub protocol
# It Subscribes for Green, Blue and Red Led status 

# import modules
from AWSIoTPythonSDK.MQTTLib import AWSIoTMQTTClient
import time
import json

# wait for ever
def blockForever():

    while 1: 
        time.sleep(0.1)

# subscribe MQTT message callback
def subscribeCallback(client, userdata, message):

    print("\n")
    print("Topic: " + message.topic)
    print("Message: ")
    print(message.payload)
    print("--------------")

# main    
def main():    

    # AWS IoT Credentials
    hostEndpoint    = "xxxxxxxxxxxxx.iot.us-west-2.amazonaws.com"
    rootCAPath      = "../../cert/VeriSign-Class 3-Public-Primary-Certification-Authority-G5.pem"
    certificatePath = "../../cert/73d79ad78b-certificate.pem.crt"
    privateKeyPath  = "../../cert/73d79ad78b-private.pem.key"
    clientId        = "python-client-master"
    mqttPort        = 8883

    # Subscribed Topics
    sub_topic_green_led = "secure/led/green/status"
    sub_topic_blue_led  = "secure/led/blue/status"
    sub_topic_red_led   = "secure/led/red/status"

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

    # Subscribe to topic green led commands
    if esp32AWSIoTMQTTClient.subscribe(sub_topic_green_led, 1, subscribeCallback) == True:
        print("\n " + clientId + " successfully subscribed to topic " + sub_topic_green_led)
    else:
        print("\n " + clientId + " failed to subscribed to topic " + sub_topic_green_led)
        exit(2)

    # Subscribe to topic blue led commands
    if esp32AWSIoTMQTTClient.subscribe(sub_topic_blue_led, 1, subscribeCallback) == True:
        print("\n " + clientId + " successfully subscribed to topic " + sub_topic_blue_led)
    else:
        print("\n " + clientId + " failed to subscribed to topic " + sub_topic_blue_led)
        exit(2)
    
    # Subscribe to topic red led commands
    if esp32AWSIoTMQTTClient.subscribe(sub_topic_red_led, 1, subscribeCallback) == True:
        print("\n " + clientId + " successfully subscribed to topic " + sub_topic_red_led)
    else:
        print("\n " + clientId + " failed to subscribed to topic " + sub_topic_red_led)
        exit(2)

    # sleep forever
    blockForever()

if __name__ == "__main__":
    main()