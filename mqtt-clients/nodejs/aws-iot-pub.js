// Author : Mudassar Tamboli

// Description:
// Demonstrates how to use AWS IoT Javascript(Nodejs) SDK and securely communicate with 
// ESP32 Thing using MQTT Pub/Sub protocol
// It Publishes "Progress Bar" pattern where the Blue LEDs are sequentially set On and restarts again

//Go to "<work-dir>/aws-iot/aws-iot-device-sdk-js/examples"
//Run "nodejs aws-iot-pub.js"


//import modules
const deviceModule = require('..').device;

// wait for ever
function blockForever() {
    console.log("\n")
    // not implemented
}
  
function publishProgressBar(device, count, topic)
{
   var message = { "LED1":"OFF", "LED2":"OFF", "LED3":"OFF"};
   message.LED1 = "OFF";
   message.LED2 = "OFF";
   message.LED3 = "OFF";

   if (count % 4 == 1 ) {
       message.LED1 = "ON";
   }

   if (count % 4 == 2 ) {
       message.LED1 = "ON";
       message.LED2 = "ON";
   }

   if (count % 4 == 3 ) {
       message.LED1 = "ON";
       message.LED2 = "ON";
       message.LED3 = "ON";
   }
             
   device.publish(topic, JSON.stringify(message));
	
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
            var count = 0; 
            pub_topic_blue_led = "secure/led/blue/command"

            console.log("\n Publishing \"Progress Bar\" to topic %s \n", pub_topic_blue_led)
            // Publish to topic blue led commands
            timeout = setInterval(function() {
                            count++;
                            publishProgressBar(device, count, pub_topic_blue_led);
                      }, 1000); 
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

}

if (require.main === module) {
   main();
}
