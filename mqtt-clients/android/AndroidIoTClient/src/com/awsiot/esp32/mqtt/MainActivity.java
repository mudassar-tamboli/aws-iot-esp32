// Author : Mudassar Tamboli

package com.awsiot.esp32.mqtt;

import android.app.Activity;
import android.os.Bundle;
import android.util.JsonToken;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.UUID;

public class MainActivity extends Activity {

    final String Tag = "awsmqttclient";
    JSONObject redLedJson = new JSONObject();

    static final String LOG_TAG = "MainActivity";

    // IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com
    private static final String AWS_IOT_ENDPOINT = "xxxxxxxxxxxxx.iot.us-west-2.amazonaws.com";//"CHANGE_ME";
    // Cognito pool ID. For this app, pool needs to be unauthenticated pool with
    // AWS IoT permissions.
    private static final String COGNITO_POOL_ID = "us-west-2:xxxxxxxxxxxxxxxxxxxxxxxxxx";//"CHANGE_ME";
    // Name of the AWS IoT policy to attach to a newly created certificate
    private static final String AWS_IOT_OWNER_POLICY_NAME  = "ESP32_Owner_Policy";//"CHANGE_ME";
    private static final String AWS_IOT_MASTER_POLICY_NAME = "ESP32_Led_Master_Policy";//"CHANGE_ME";
    private static final String AWS_IOT_ADMIN_POLICY_NAME  = "ESP32_Led_Admin_Policy";//"CHANGE_ME";
    private static final String AWS_IOT_GUEST_POLICY_NAME  = "ESP32_Led_Guest_Policy";//"CHANGE_ME";


    final String SUB_TOPIC_LED_GREEN_STATUS = "secure/led/green/status";
    final String SUB_TOPIC_LED_BLUE_STATUS  = "secure/led/blue/status";
    final String SUB_TOPIC_LED_RED_STATUS   = "secure/led/red/status";
    final String SUB_TOPIC_LED_ESP32_STATUS = "secure/led/esp32/status";

    final String clientId_Owner        = "android-esp32-owner";
    final String clientId_MobileMaster = "mobile-client-master";
    final String clientId_MobileAdmin  = "mobile-client-admin";
    final String clientId_MobileGuest  = "mobile-client-guest";
    final String clientId_Intruder     = "intruder";

    // Region of AWS IoT
    private static final Regions MY_REGION = Regions.US_WEST_2;
    // Filename of KeyStore file on the filesystem
    private static final String KEYSTORE_NAME = "iot_keystore";
    // Password for the private key in the KeyStore
    private static final String KEYSTORE_PASSWORD = "password";
    // Certificate and key aliases in the KeyStore
    private static final String CERTIFICATE_ID = "default";

    TextView tvClientId;
    TextView tvStatus;

    Button btnOwner;
    Button btnMaster;
    Button btnAdmin;
    Button btnGuest;
    Button btnUnknown;

    ImageView imgGreenLed1, imgGreenLed2, imgGreenLed3;
    ImageView imgBlueLed1, imgBlueLed2, imgBlueLed3;
    ImageView imgRedLed1, imgRedLed2, imgRedLed3;
    ImageView imgEsp32;

    Button btnDisconnect;

    AWSIotClient mIotAndroidClient;
    AWSIotMqttManager mqttManager;
    //String clientId;
    String keystorePath;
    String keystoreName;
    String keystorePassword;
    String topic;
    KeyStore clientKeyStore = null;
    String certificateId;
    String mUserType;
    CognitoCachingCredentialsProvider credentialsProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgGreenLed1 = (ImageView) findViewById(R.id.greenled1);
        imgGreenLed2 = (ImageView) findViewById(R.id.greenled2);
        imgGreenLed3 = (ImageView) findViewById(R.id.greenled3);

        imgBlueLed1 = (ImageView) findViewById(R.id.blueled1);
        imgBlueLed2 = (ImageView) findViewById(R.id.blueled2);
        imgBlueLed3 = (ImageView) findViewById(R.id.blueled3);

        imgRedLed1 = (ImageView) findViewById(R.id.redled1);
        imgRedLed2 = (ImageView) findViewById(R.id.redled2);
        imgRedLed3 = (ImageView) findViewById(R.id.redled3);

        setLedOnClicks();

        tvStatus = (TextView) findViewById(R.id.tvStatus);

        btnOwner = (Button) findViewById(R.id.btnOwner);
        btnOwner.setOnClickListener(connectOwner);

        btnMaster = (Button) findViewById(R.id.btnMaster);
        btnMaster.setOnClickListener(connectMaster);

        btnAdmin = (Button) findViewById(R.id.btnAdmin);
        btnAdmin.setOnClickListener(connectAdmin);

        btnGuest = (Button) findViewById(R.id.btnGuest);
        btnGuest.setOnClickListener(connectGuest);

        btnUnknown = (Button) findViewById(R.id.btnUnknown);
        btnUnknown.setOnClickListener(connectUnknown);

        //        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
//        btnDisconnect.setOnClickListener(disconnectClick);

        // MQTT client IDs are required to be unique per AWS IoT account.
        // This UUID is "practically unique" but does not _guarantee_
        // uniqueness.

        // Initialize the AWS Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // context
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );

        ((ImageView) findViewById(R.id.client)).setVisibility(View.INVISIBLE);
        loadOffLedImage();

        //subscribeToTopic("/secure/led/blue/command");
        //subscribeToTopic("/secure/led/red/command");
    }

    boolean secureConnectAWS(final String clientId)
    {

        // MQTT Client

        Region region = Region.getRegion(MY_REGION);

        if (mqttManager != null) {
            mqttManager.disconnect();
        }

        mqttManager = new AWSIotMqttManager(clientId, AWS_IOT_ENDPOINT);

        OnClickLedRules.mqttManager = mqttManager;
        // Set keepalive to 10 seconds.  Will recognize disconnects more quickly but will also send
        // MQTT pings every 10 seconds.
        mqttManager.setKeepAlive(10);
        mqttManager.setAutoReconnect(false);
        // Set Last Will and Testament for MQTT.  On an unclean disconnect (loss of connection)
        // AWS IoT will publish this message to alert other clients.
        AWSIotMqttLastWillAndTestament lwt = new AWSIotMqttLastWillAndTestament("android/my/lwt/topic",
                "Android client lost connection", AWSIotMqttQos.QOS0);
        mqttManager.setMqttLastWillAndTestament(lwt);

        // IoT Client (for creation of certificate if needed)
        mIotAndroidClient = new AWSIotClient(credentialsProvider);
        mIotAndroidClient.setRegion(region);

        keystorePath = getFilesDir().getPath();
        keystoreName = clientId + "-" + KEYSTORE_NAME;
        keystorePassword = clientId + "-" + KEYSTORE_PASSWORD;
        certificateId = clientId + "-" + CERTIFICATE_ID;
        clientKeyStore = null;

        // To load cert/key from keystore on filesystem
        try {
            if (AWSIotKeystoreHelper.isKeystorePresent(keystorePath, keystoreName)) {
                if (AWSIotKeystoreHelper.keystoreContainsAlias(certificateId, keystorePath,
                        keystoreName, keystorePassword)) {
                    Log.i(LOG_TAG, "Certificate " + certificateId
                            + " found in keystore - using for MQTT.");
                    // load keystore from file into memory to pass on connection
                    clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                            keystorePath, keystoreName, keystorePassword);
                    //btnConnect.setEnabled(true);
                    secureConnectClient(clientId);
                } else {
                    Log.i(LOG_TAG, "Key/cert " + certificateId + " not found in keystore.");
                }
            } else {
                Log.i(LOG_TAG, "Keystore " + keystorePath + "/" + keystoreName + " not found.");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "An error occurred retrieving cert/key from keystore.", e);
        }


        if (clientKeyStore == null) {
            Log.i(LOG_TAG, "Cert/key was not found in keystore - creating new key and certificate.");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Create a new private key and certificate. This call
                        // creates both on the server and returns them to the
                        // device.
                        CreateKeysAndCertificateRequest createKeysAndCertificateRequest =
                                new CreateKeysAndCertificateRequest();
                        createKeysAndCertificateRequest.setSetAsActive(true);
                        final CreateKeysAndCertificateResult createKeysAndCertificateResult;
                        createKeysAndCertificateResult =
                                mIotAndroidClient.createKeysAndCertificate(createKeysAndCertificateRequest);
                        Log.i(LOG_TAG,
                                "Cert ID: " +
                                        createKeysAndCertificateResult.getCertificateId() +
                                        " created.");

                        // store in keystore for use in MQTT client
                        // saved as alias "default" so a new certificate isn't
                        // generated each run of this application
                        AWSIotKeystoreHelper.saveCertificateAndPrivateKey(certificateId,
                                createKeysAndCertificateResult.getCertificatePem(),
                                createKeysAndCertificateResult.getKeyPair().getPrivateKey(),
                                keystorePath, keystoreName, keystorePassword);

                        // load keystore from file into memory to pass on
                        // connection
                        clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                                keystorePath, keystoreName, keystorePassword);

                        // Attach a policy to the newly created certificate.
                        // This flow assumes the policy was already created in
                        // AWS IoT and we are now just attaching it to the
                        // certificate.
                        AttachPrincipalPolicyRequest policyAttachRequest =
                                new AttachPrincipalPolicyRequest();

                        if (clientId.compareTo(clientId_MobileMaster) == 0) {
                            Log.v(Tag, clientId_MobileMaster + " connecting with policy " + AWS_IOT_MASTER_POLICY_NAME);
                            policyAttachRequest.setPolicyName(AWS_IOT_MASTER_POLICY_NAME);

                        } else if (clientId.compareTo(clientId_MobileAdmin) == 0) {
                            policyAttachRequest.setPolicyName(AWS_IOT_ADMIN_POLICY_NAME);

                        } else if (clientId.compareTo(clientId_MobileGuest) == 0) {
                            policyAttachRequest.setPolicyName(AWS_IOT_GUEST_POLICY_NAME);

                        } else {
                            // intruder using owner policy ************
                            policyAttachRequest.setPolicyName(AWS_IOT_OWNER_POLICY_NAME);
                        }

                        policyAttachRequest.setPrincipal(createKeysAndCertificateResult
                                .getCertificateArn());
                        mIotAndroidClient.attachPrincipalPolicy(policyAttachRequest);

                        secureConnectClient(clientId);

                    } catch (Exception e) {
                        Log.e(LOG_TAG,
                                "Exception occurred when generating new private key and certificate.",
                                e);
                    }
                }
            }).start();
        }

        return true;
    }

    View.OnClickListener connectOwner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ImageView) findViewById(R.id.client)).setImageResource(R.drawable.owner);
                }
            });
            OnClickLedRules.InitOnClickLedRules();
            mUserType = "Owner";
            secureConnectAWS(clientId_Owner);
        }
    };


    View.OnClickListener connectMaster = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                  ((ImageView) findViewById(R.id.client)).setImageResource(R.drawable.master);
                              }
                          });
            OnClickLedRules.InitOnClickLedRules();
            mUserType = "Master";
            secureConnectAWS(clientId_MobileMaster);
        }
    };

    View.OnClickListener connectAdmin = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadOffLedImage();
                    ((ImageView) findViewById(R.id.client)).setImageResource(R.drawable.admin);
                }
            });
            OnClickLedRules.InitOnClickLedRules();
            mUserType = "Admin";
            secureConnectAWS(clientId_MobileAdmin);

        }
    };

    View.OnClickListener connectGuest = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadOffLedImage();
                    ((ImageView) findViewById(R.id.client)).setImageResource(R.drawable.guest);
                }
            });
            OnClickLedRules.InitOnClickLedRules();
            mUserType = "Guest";
            secureConnectAWS(clientId_MobileGuest);
        }
    };

    View.OnClickListener connectUnknown = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadOffLedImage();
                    ((ImageView) findViewById(R.id.client)).setImageResource(R.drawable.unknown);
                }
            });
            OnClickLedRules.InitOnClickLedRules();
            mUserType = "Unknown";
            secureConnectAWS(clientId_Intruder);
        }
    };

    boolean secureConnectClient(final String clientId)
    {
            Log.d(LOG_TAG, "clientId = " + clientId);

            try {
                mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                    @Override
                    public void onStatusChanged(final AWSIotMqttClientStatus status,
                            final Throwable throwable) {
                        Log.d(LOG_TAG, "Status = " + String.valueOf(status));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //if ()
                                //((ImageView) findViewById(R.id.client)).setVisibility(View.INVISIBLE);
                                ((ImageView) findViewById(R.id.client)).setVisibility(View.VISIBLE);
                                if (status == AWSIotMqttClientStatus.Connecting) {
                                    tvStatus.setText(mUserType + " Connecting...");

                                } else if (status == AWSIotMqttClientStatus.Connected) {
                                    tvStatus.setText(mUserType + " Connected");

                                    subscribeToTopics(clientId);

                                } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                                    if (throwable != null) {
                                        Log.e(LOG_TAG, mUserType + " Connection error.", throwable);
                                    }
                                    loadDisableLedImage();
                                    tvStatus.setText(mUserType + " Reconnecting");
                                } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
                                    if (throwable != null) {
                                        Log.e(LOG_TAG, mUserType + " Connection error.", throwable);
                                    }
                                    tvStatus.setText(mUserType + " Disconnected");
                                    loadDisableLedImage();
                                } else {
                                    tvStatus.setText(mUserType + " Disconnected");
                                    loadDisableLedImage();

                                }
                            }
                        });
                    }
                });
            } catch (final Exception e) {
                Log.e(LOG_TAG, "Connection error.", e);
                tvStatus.setText("Error! " + e.getMessage());
            }

            return true;
    }

    View.OnClickListener subscribeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            Log.d(LOG_TAG, "topic = " + topic);

            try {
                mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                        new AWSIotMqttNewMessageCallback() {
                            @Override
                            public void onMessageArrived(final String topic, final byte[] data) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            String message = new String(data, "UTF-8");
                                            Log.d(LOG_TAG, "Message arrived:");
                                            Log.d(LOG_TAG, "   Topic: " + topic);
                                            Log.d(LOG_TAG, " Message: " + message);

                                        } catch (UnsupportedEncodingException e) {
                                            Log.e(LOG_TAG, "Message encoding error.", e);
                                        }
                                    }
                                });
                            }
                        });
            } catch (Exception e) {
                Log.e(LOG_TAG, "Subscription error.", e);
            }
        }
    };

    View.OnClickListener publishClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final String topic = "";
            final String msg = "";

            try {
                mqttManager.publishString(msg, topic, AWSIotMqttQos.QOS0);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Publish error.", e);
            }

        }
    };

    View.OnClickListener disconnectClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            try {
                mqttManager.disconnect();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Disconnect error.", e);
            }

        }
    };



    public void loadGreenLedImage(JSONObject jsonObect)
    {
        String greenleduri = "@drawable/greenled";
        String offleduri = "@drawable/offled";

        int ledGreenResource = getResources().getIdentifier(greenleduri, null, getPackageName());
        int ledOffResource = getResources().getIdentifier(offleduri, null, getPackageName());

        try {
            if (jsonObect.getString("LED1").equals("ON")) {
                ((ImageView) findViewById(R.id.greenled1)).setImageDrawable(getResources().getDrawable(ledGreenResource));
                OnClickLedRules.stateGreenLed1 = OnClickLedRules.LED_ON;
            } else if (jsonObect.getString("LED1").equals("OFF")) {
                ((ImageView) findViewById(R.id.greenled1)).setImageDrawable(getResources().getDrawable(ledOffResource));
                OnClickLedRules.stateGreenLed1 = OnClickLedRules.LED_OFF;
            }
        } catch (Exception ex) {
            Log.e(Tag, ex.getMessage());
        }

        try {
            if (jsonObect.getString("LED2").equals("ON")) {
                ((ImageView) findViewById(R.id.greenled2)).setImageDrawable(getResources().getDrawable(ledGreenResource));
                OnClickLedRules.stateGreenLed2 = OnClickLedRules.LED_ON;
            } else if (jsonObect.getString("LED2").equals("OFF")) {
                ((ImageView) findViewById(R.id.greenled2)).setImageDrawable(getResources().getDrawable(ledOffResource));
                OnClickLedRules.stateGreenLed2 = OnClickLedRules.LED_OFF;
            }
        } catch (Exception ex) {
            Log.e(Tag, ex.getMessage());
        }

        try {
            if (jsonObect.getString("LED3").equals("ON")) {
                ((ImageView) findViewById(R.id.greenled3)).setImageDrawable(getResources().getDrawable(ledGreenResource));
                OnClickLedRules.stateGreenLed3 = OnClickLedRules.LED_ON;
            } else if (jsonObect.getString("LED3").equals("OFF")) {
                ((ImageView) findViewById(R.id.greenled3)).setImageDrawable(getResources().getDrawable(ledOffResource));
                OnClickLedRules.stateGreenLed3 = OnClickLedRules.LED_OFF;
            }
        } catch (Exception ex) {
            Log.e(Tag, ex.getMessage());
        }



    }

    public void loadBlueLedImage(JSONObject jsonObect)
    {
        String blueleduri = "@drawable/blueled";
        String offleduri = "@drawable/offled";

        int ledBlueResource = getResources().getIdentifier(blueleduri, null, getPackageName());
        int ledOffResource = getResources().getIdentifier(offleduri, null, getPackageName());

        try {
            if (jsonObect.getString("LED1").equals("ON")) {
                ((ImageView) findViewById(R.id.blueled1)).setImageDrawable(getResources().getDrawable(ledBlueResource));
                OnClickLedRules.stateBlueLed1 = OnClickLedRules.LED_ON;
            } else if (jsonObect.getString("LED1").equals("OFF")) {
                ((ImageView) findViewById(R.id.blueled1)).setImageDrawable(getResources().getDrawable(ledOffResource));
                OnClickLedRules.stateBlueLed1 = OnClickLedRules.LED_OFF;
            }
        } catch (Exception ex) {
            Log.e(Tag, ex.getMessage());
        }

        try {
            if (jsonObect.getString("LED2").equals("ON")) {
                ((ImageView) findViewById(R.id.blueled2)).setImageDrawable(getResources().getDrawable(ledBlueResource));
                OnClickLedRules.stateBlueLed2 = OnClickLedRules.LED_ON;
            } else if (jsonObect.getString("LED2").equals("OFF")) {
                ((ImageView) findViewById(R.id.blueled2)).setImageDrawable(getResources().getDrawable(ledOffResource));
                OnClickLedRules.stateBlueLed2 = OnClickLedRules.LED_OFF;
            }
        } catch (Exception ex) {
            Log.e(Tag, ex.getMessage());
        }

        try {
            if (jsonObect.getString("LED3").equals("ON")) {
                ((ImageView) findViewById(R.id.blueled3)).setImageDrawable(getResources().getDrawable(ledBlueResource));
                OnClickLedRules.stateBlueLed3 = OnClickLedRules.LED_ON;
            } else if (jsonObect.getString("LED3").equals("OFF")) {
                ((ImageView) findViewById(R.id.blueled3)).setImageDrawable(getResources().getDrawable(ledOffResource));
                OnClickLedRules.stateBlueLed3 = OnClickLedRules.LED_OFF;
            }
        } catch (Exception ex) {
            Log.e(Tag, ex.getMessage());
        }


    }

    public void loadRedLedImage(JSONObject jsonObect)
    {
        String redleduri = "@drawable/redled";
        String offleduri = "@drawable/offled";

        int ledRedResource = getResources().getIdentifier(redleduri, null, getPackageName());
        int ledOffResource = getResources().getIdentifier(offleduri, null, getPackageName());

        try {
            if (jsonObect.getString("LED1").equals("ON")) {
                ((ImageView) findViewById(R.id.redled1)).setImageDrawable(getResources().getDrawable(ledRedResource));
                OnClickLedRules.stateRedLed1 = OnClickLedRules.LED_ON;
            } else if (jsonObect.getString("LED1").equals("OFF")) {
                ((ImageView) findViewById(R.id.redled1)).setImageDrawable(getResources().getDrawable(ledOffResource));
                OnClickLedRules.stateRedLed1 = OnClickLedRules.LED_OFF;
            }

        } catch (Exception ex) {
            Log.e(Tag, ex.getMessage());
        }

        try {
            if (jsonObect.getString("LED2").equals("ON")) {
                ((ImageView) findViewById(R.id.redled2)).setImageDrawable(getResources().getDrawable(ledRedResource));
                OnClickLedRules.stateRedLed2 = OnClickLedRules.LED_ON;
            } else if (jsonObect.getString("LED2").equals("OFF")) {
                ((ImageView) findViewById(R.id.redled2)).setImageDrawable(getResources().getDrawable(ledOffResource));
                OnClickLedRules.stateRedLed2 = OnClickLedRules.LED_OFF;
            }
        } catch (Exception ex) {
            Log.e(Tag, ex.getMessage());
        }

        try {
            if (jsonObect.getString("LED3").equals("ON")) {
                ((ImageView) findViewById(R.id.redled3)).setImageDrawable(getResources().getDrawable(ledRedResource));
                OnClickLedRules.stateRedLed3 = OnClickLedRules.LED_ON;
            } else if (jsonObect.getString("LED3").equals("OFF")) {
                ((ImageView) findViewById(R.id.redled3)).setImageDrawable(getResources().getDrawable(ledOffResource));
                OnClickLedRules.stateRedLed3 = OnClickLedRules.LED_OFF;
            }
        } catch (Exception ex) {
            Log.e(Tag, ex.getMessage());
        }

    }
    public void loadOffLedImage() {

        ((ImageView) findViewById(R.id.greenled1)).setImageResource(R.drawable.offled);
        ((ImageView) findViewById(R.id.greenled2)).setImageResource(R.drawable.offled);
        ((ImageView) findViewById(R.id.greenled3)).setImageResource(R.drawable.offled);

        ((ImageView) findViewById(R.id.blueled1)).setImageResource(R.drawable.offled);
        ((ImageView) findViewById(R.id.blueled2)).setImageResource(R.drawable.offled);
        ((ImageView) findViewById(R.id.blueled3)).setImageResource(R.drawable.offled);

        ((ImageView) findViewById(R.id.redled1)).setImageResource(R.drawable.offled);
        ((ImageView) findViewById(R.id.redled2)).setImageResource(R.drawable.offled);
        ((ImageView) findViewById(R.id.redled3)).setImageResource(R.drawable.offled);
    }
    public void loadDisabledLedGreenImages() {

        ((ImageView) findViewById(R.id.greenled1)).setImageResource(R.drawable.disableled);
        ((ImageView) findViewById(R.id.greenled2)).setImageResource(R.drawable.disableled);
        ((ImageView) findViewById(R.id.greenled3)).setImageResource(R.drawable.disableled);
    }

    public void loadDisabledLedBlueImages() {

        ((ImageView) findViewById(R.id.blueled1)).setImageResource(R.drawable.disableled);
        ((ImageView) findViewById(R.id.blueled2)).setImageResource(R.drawable.disableled);
        ((ImageView) findViewById(R.id.blueled3)).setImageResource(R.drawable.disableled);
    }

    public void loadDisabledLedRedImages() {

        ((ImageView) findViewById(R.id.redled1)).setImageResource(R.drawable.disableled);
        ((ImageView) findViewById(R.id.redled2)).setImageResource(R.drawable.disableled);
        ((ImageView) findViewById(R.id.redled3)).setImageResource(R.drawable.disableled);
    }

    public void loadDisableLedImage() {
        loadDisabledLedGreenImages();
        loadDisabledLedBlueImages();
        loadDisabledLedRedImages();
    }

    void setLedOnClicks()
    {
        OnClickLedRules.InitOnClickLedRules();
        OnClickLedRules.setGreenLed1OnClicks(imgGreenLed1);
        OnClickLedRules.setGreenLed2OnClicks(imgGreenLed2);
        OnClickLedRules.setGreenLed3OnClicks(imgGreenLed3);

        OnClickLedRules.setBlueLed1OnClicks(imgBlueLed1);
        OnClickLedRules.setBlueLed2OnClicks(imgBlueLed2);
        OnClickLedRules.setBlueLed3OnClicks(imgBlueLed3);

        OnClickLedRules.setRedLed1OnClicks(imgRedLed1);
        OnClickLedRules.setRedLed2OnClicks(imgRedLed2);
        OnClickLedRules.setRedLed3OnClicks(imgRedLed3);

    }

    void subscribeToTopics(String clientId)
    {
        loadOffLedImage();

        if (clientId_MobileGuest.compareTo(clientId) == 0) {
            loadDisabledLedBlueImages();
            loadDisabledLedRedImages();
            subscribeToTopic(SUB_TOPIC_LED_GREEN_STATUS);
        } else if (clientId_MobileAdmin.compareTo(clientId) == 0) {
            loadDisabledLedRedImages();
            subscribeToTopic(SUB_TOPIC_LED_GREEN_STATUS);
            subscribeToTopic(SUB_TOPIC_LED_BLUE_STATUS);
        } else if (clientId_MobileMaster.compareTo(clientId) == 0) {
            subscribeToTopic(SUB_TOPIC_LED_GREEN_STATUS);
            subscribeToTopic(SUB_TOPIC_LED_BLUE_STATUS);
            subscribeToTopic(SUB_TOPIC_LED_RED_STATUS);
        } else if (clientId_Owner.compareTo(clientId) == 0) {
            subscribeToTopic(SUB_TOPIC_LED_GREEN_STATUS);
            subscribeToTopic(SUB_TOPIC_LED_BLUE_STATUS);
            subscribeToTopic(SUB_TOPIC_LED_RED_STATUS);
        } else {
            loadDisabledLedGreenImages();
            loadDisabledLedBlueImages();
            loadDisabledLedRedImages();

        }
    }

    boolean subscribeToTopic(String topic)
    {
        boolean ret = true;
        try {
            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(final String topic, final byte[] data) {
                            try {
                                final String message = new String(data, "UTF-8");
                                try {
                                    Log.d(LOG_TAG, "Message arrived:");
                                    Log.d(LOG_TAG, "   Topic: " + topic);
                                    Log.d(LOG_TAG, " Message: " + message);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            handleTopic(topic, message);
                                        }
                                    });


                                } catch (Exception e) {

                                }
                            } catch (UnsupportedEncodingException e) {
                                Log.e(LOG_TAG, "Message encoding error.", e);
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(LOG_TAG, "Subscription error.", e);
            handleSubscriptionError(e);
            ret = false;
        }

        return ret;
    }

    public void handleTopic(String topic, String message) {

        JSONObject jsonObject = null;

        try {

            Log.d(LOG_TAG, "Message arrived:");
            Log.d(LOG_TAG, "   Topic: " + topic);
            Log.d(LOG_TAG, " Message: " + message);

            jsonObject = new JSONObject(message);

            if (topic.compareTo(SUB_TOPIC_LED_GREEN_STATUS) == 0) {
                loadGreenLedImage(jsonObject);
            } else if (topic.compareTo(SUB_TOPIC_LED_BLUE_STATUS) == 0) {
                loadBlueLedImage(jsonObject);
            } else if (topic.compareTo(SUB_TOPIC_LED_RED_STATUS) == 0) {
                loadRedLedImage(jsonObject);
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Message encoding error.", e);
            return;
        }

        return;
    }

    void handleSubscriptionError(Exception e)
    {
        String mesg = e.getMessage();
        mesg = mesg + "123";
    }
}
