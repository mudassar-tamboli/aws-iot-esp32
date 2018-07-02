// Author : Mudassar Tamboli

// Description:
// Demonstrates how to use AWS IoT Javascript(Nodejs) SDK and securely communicate with 
// ESP32 Thing using MQTT Pub/Sub protocol
// It Subscribes for Green, Blue Led status 

//Go to "<work-dir>/aws-iot/aws-iot-device-sdk-js/examples"
//Run "nodejs aws-iot-sub.js"

//import modules
const deviceModule = require('..').device;

// wait for ever
function blockForever() {
    console.log("\n")
    // not implemented
}
  
// subscribe MQTT message callback
function subscribeCallback(topic, payload) {

      console.log("\n")
      console.log("Topic: " + topic);
      console.log("Message: ")
      console.log( payload.toString())
      console.log("--------------")
}
  
//main
function main() {
   
// AWS IoT Credentials    
   hostEndpoint    = "xxxxxxxxxxxxx.iot.us-west-2.amazonaws.com"
   rootCAPath      = "../../cert/VeriSign-Class 3-Public-Primary-Certification-Authority-G5.pem"
   certificatePath = "../../cert/1f5b006af1-certificate.pem.crt"
   privateKeyPath  = "../../cert/1f5b006af1-private.pem.key"
   clientId        = "nodejs-client-admin"
   region          = "us-west-2"
   mqttPort        = 8883
   mqttProtocol    = "mqtts"	

// Subscribed Topics
   sub_topic_green_led = "secure/led/green/status"
   sub_topic_blue_led  = "secure/led/blue/status"

// AWS IoT MQTT Broker prepare login	
   const device = deviceModule({
      region:    region,
      host:      hostEndpoint,
      protocol:  mqttProtocol,
      port:      mqttPort,
      keyPath:   privateKeyPath,
      certPath:  certificatePath,
      caPath:    rootCAPath,
      clientId:  clientId,
      baseReconnectTimeMs: 3000, // ms
      keepalive:  30 //seconds
   });

// Connect to AWS IoT MQTT Broker
   device
      .on('connect', function() {
         console.log("\n " + clientId + " successfully connected to " + hostEndpoint)

         // Subscribe to topic green led commands
         console.log("\n " + clientId + " subscribing to topic " + sub_topic_green_led)
         device.subscribe(sub_topic_green_led);
         
         // Subscribe to topic blue led commands
         console.log("\n " + clientId + " subscribing to topic " + sub_topic_blue_led)
         device.subscribe(sub_topic_blue_led);
      });

   device
      .on('close', function() {
         console.log('close');
      });
   device
      .on('reconnect', function() {
         console.log('reconnect');
      });
   device
      .on('offline', function() {
         console.log('offline');
      });
   device
      .on('error', function(error) {
         console.log('error', error);
      });

   device
      .on('message', subscribeCallback);

}

if (require.main === module) {
   main();
}
