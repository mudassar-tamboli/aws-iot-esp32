// Author : Mudassar Tamboli

// Description:
// Demonstrates how to make Go script to connect to AWS IoT and securely communicate with 
// ESP32 Thing using MQTT Pub/Sub protocol. It uses paho mqtt library and latest versio of Go
// It Subscribes for Green LED Status connected to ESP32.

//Go to "<work-dir>/src/github.com/<user>/aws-iot-esp32"
//Run "go run aws-iot-sub.go"

package main

//import modules
import (
	"crypto/tls"
	"crypto/x509"
	"io/ioutil"
	"fmt"
	"os"
    "time"
	MQTT "github.com/eclipse/paho.mqtt.golang"
)

// wait for ever
func blockForever() {
    select{ }
}

// subscribe MQTT message callback
func subscribeCallback(client MQTT.Client, message MQTT.Message) {

    fmt.Printf("\n")
    fmt.Printf("\nTopic: " + message.Topic())
    fmt.Printf("\nMessage: \n")
    fmt.Printf(string(message.Payload()))
    fmt.Printf("\n--------------")
}

// main
func main() {

// AWS IoT Credentials    
    hostEndpoint    := "xxxxxxxxxxxxx.iot.us-west-2.amazonaws.com"
    rootCAPath      := "../../cert/VeriSign-Class 3-Public-Primary-Certification-Authority-G5.pem"
    certificatePath := "../../cert/d9bc616ce6-certificate.pem.crt"
    privateKeyPath  := "../../cert/d9bc616ce6-private.pem.key"
    clientId        := "go-client-guest"
    mqttPort        := 8883
    mqttPath        := "/mqtt"
    qos             := 1

// Subscribed Topics
    sub_topic_green_led := "secure/led/green/status"

// AWS IoT MQTT Broker prepare login	
    cfg := new(tls.Config)
    cfg.RootCAs = x509.NewCertPool()
    if ca, err := ioutil.ReadFile(rootCAPath); err == nil {
        cfg.RootCAs.AppendCertsFromPEM(ca)
    } else {
        fmt.Println(err)
        os.Exit(1)
    }

    cert, err := tls.LoadX509KeyPair(certificatePath, privateKeyPath)
    if err != nil {
        fmt.Println(err)
        os.Exit(1)
    } else {
        cfg.Certificates = append(cfg.Certificates, cert)
    }

    connOpts := &MQTT.ClientOptions{
	    ClientID:             clientId,
	    CleanSession:         true,
	    AutoReconnect:        true,
	    MaxReconnectInterval: 1 * time.Second,
	    KeepAlive:            30 * int64(time.Second),
	    TLSConfig:            tls.Config{Certificates: []tls.Certificate{cert}},
    }

    awsBrokerURL := fmt.Sprintf("tcps://%s:%d%s", hostEndpoint, mqttPort, mqttPath)

    connOpts.AddBroker(awsBrokerURL)

    received := 0;
    connOpts.SetDefaultPublishHandler(func(client MQTT.Client, msg MQTT.Message) {
	    s := string(msg.Payload())
	    fmt.Printf("%d heard %s %s", received, msg.Topic(), s)
	        t, err := time.Parse(time.RFC3339Nano, s)
	    if err != nil {
		    fmt.Printf("message '%s' not a time", s)
	    } else {
		    fmt.Printf("elasped %dms", int(time.Now().Sub(t).Seconds()*1000))
	    }
	    received++
        })
    mqttClient := MQTT.NewClient(connOpts)

// Connect to AWS IoT MQTT Broker
    if token := mqttClient.Connect(); token.Wait() && token.Error() != nil {
       panic(token.Error())
       os.Exit(1)
    }

    fmt.Printf("\n %s successfully connected to %s ", clientId, hostEndpoint)

    // Subscribe to topic green led commands
    go func() {
	    if token := mqttClient.Subscribe(sub_topic_green_led, byte(qos), subscribeCallback); token.Wait() && token.Error() != nil {
		    fmt.Println(token.Error())
		    os.Exit(1)
	    }
	    fmt.Printf("\n %s successfully subscribed to topic %s \n", clientId, sub_topic_green_led)
    } ()

   blockForever()
}



