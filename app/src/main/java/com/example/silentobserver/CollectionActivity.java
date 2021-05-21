package com.example.silentobserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CollectionActivity extends AppCompatActivity implements SensorEventListener {

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String userId;
    private int rowCount;

    //Shared Preference
    Context context;
    SharedPreferences sharedPref ;
    private static final String ltUrl = "LocalTunnelURL";
    private static final String sentDataCount= "SentDataCount";

    //Sensor Manager
    private SensorManager mSensorManager;

    // Accelerometer and gyroscope sensors, as retrieved from the sensor manager.
    private Sensor mSensorAcc;
    private Sensor mSensorGyro;
    private Sensor mSensorMagneticField;

    //local variables
    private TextView responseText;

    private float[] accelerometerReading = new float[3];
    private float[] gyroscopeReading = new float[3];
    private float[] magnetometerReading = new float[3];

    private float[] rotationMatrix = new float[9];
    private float[] orientationAngles = new float[3];

    //time variables
    long startTime= new Date().getTime();
    long currentTime;

    //Matrix to send
    //ArrayList<Float> sensorMatrix = new ArrayList<Float>();

    //Json Object to send to server
    JSONArray jsonArray= new JSONArray();

    //URL
    String url;

    int counter=0;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();

        // Shared preference
        context = this;
        sharedPref = context.getSharedPreferences(getString(R.string.input_url_key), Context.MODE_PRIVATE);
        url= sharedPref.getString(ltUrl,"");

        // Initialization
        responseText = (TextView) findViewById(R.id.requestValue);

        // Volley
        requestQueue = Volley.newRequestQueue(this);

        // Get an instance of the sensor manager.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Get the error message from string resources.
        String sensor_error = "No sensor";


   }

    @Override
    protected void onStart() {
        super.onStart();
        if (mSensorAcc != null) {
            mSensorManager.registerListener(this, mSensorAcc,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorGyro != null) {
            mSensorManager.registerListener(this, mSensorGyro,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(mSensorMagneticField != null){
            mSensorManager.registerListener(this, mSensorMagneticField,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int sensorType = sensorEvent.sensor.getType();
        switch(sensorType){
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerReading[0] = sensorEvent.values[0];
                accelerometerReading[1] = sensorEvent.values[1];
                accelerometerReading[2] = sensorEvent.values[2];
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroscopeReading[0] = sensorEvent.values[0];
                gyroscopeReading[1] = sensorEvent.values[1];
                gyroscopeReading[2] = sensorEvent.values[2];
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magnetometerReading[0] = sensorEvent.values[0];
                magnetometerReading[1] = sensorEvent.values[1];
                magnetometerReading[2] = sensorEvent.values[2];
                break;
            default:
                System.out.println("Default Encountered");
        }

        currentTime= new Date().getTime();
        //System.out.println(currentTime-startTime);
        if(currentTime-startTime>100){

            SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
            SensorManager.getOrientation(rotationMatrix, orientationAngles);

            try {
                jsonArray.put(accelerometerReading[0]);
                jsonArray.put(accelerometerReading[1]);
                jsonArray.put(accelerometerReading[2]);
                jsonArray.put(gyroscopeReading[0]);
                jsonArray.put(gyroscopeReading[1]);
                jsonArray.put(gyroscopeReading[2]);
                jsonArray.put(magnetometerReading[0]);
                jsonArray.put(magnetometerReading[1]);
                jsonArray.put(magnetometerReading[2]);
                jsonArray.put(orientationAngles[0]);
                jsonArray.put(orientationAngles[1]);
                jsonArray.put(orientationAngles[2]);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            counter+=1;
            //System.out.println(startTime-currentTime + " ---- " + counter);
            startTime=currentTime;
            //rowCount++;

        }
        if(counter==128){
            counter=0;
            JSONObject jsonObject= new JSONObject();
            try {
                jsonObject.put("userId", userId);
                jsonObject.put("values",jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("URL", url);
            jsonArray = new JSONArray();
            JsonObjectRequest jsonObjectRequest = null;

            int currentSentDataCount = sharedPref.getInt(sentDataCount,0);
            System.out.println("CURRENTSENTDATACOUNT: "+ currentSentDataCount);

            if(currentSentDataCount<2000){
                String tempURL = url + "/save";
                jsonObjectRequest= new JsonObjectRequest(
                        Request.Method.POST,
                        tempURL,
                        jsonObject,
                        new Response.Listener<JSONObject>() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Log.d("response", response.get("response").toString());
                                    responseText.setText("Application is collecting the sensors data.\n\n"+
                                            "Current data sent count after application installation: "+ currentSentDataCount);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                responseText.setText("Error, while fetching details");
                            }
                        }
                ){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> customHeaders = new HashMap<>();
                        customHeaders.put("Bypass-Tunnel-Reminder", "True");
                        return customHeaders;
                    }
                };
            }else if(currentSentDataCount==2000){
                String tempURL = url + "/model";
                jsonObjectRequest= new JsonObjectRequest(
                        Request.Method.POST,
                        tempURL,
                        jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Log.d("response", response.get("response").toString());
                                    responseText.setText("Model is being created.\n\nKindly Wait!");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                responseText.setText("Error, while fetching details");
                            }
                        }
                ){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> customHeaders = new HashMap<>();
                        customHeaders.put("Bypass-Tunnel-Reminder", "True");
                        return customHeaders;
                    }
                };

            }else if(currentSentDataCount>2100){
                String tempURL = url + "/result";
                jsonObjectRequest= new JsonObjectRequest(
                        Request.Method.POST,
                        tempURL,
                        jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Log.d("response", response.get("response").toString());
                                    String result = response.getString("response");
                                    if(result=="Malicious"){
                                        responseText.setText("Result is: "+response.get("response").toString());
                                        //TODO
                                        //Lock the screen
                                    }else if(result == "Non-Malicious"){
                                        responseText.setText("Result is: "+response.get("response").toString());
                                        //Do Nothing
                                    }else{
                                        responseText.setText("Model hasn't been created yet.\n\nKindly Wait!");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                responseText.setText("Error, while fetching details");
                            }
                        }
                ){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> customHeaders = new HashMap<>();
                        customHeaders.put("Bypass-Tunnel-Reminder", "True");
                        return customHeaders;
                    }
                };

            }

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(sentDataCount, currentSentDataCount+1);
            editor.apply();                                  //can use editor.commit() also
            requestQueue.add(jsonObjectRequest);

        }
        //System.out.println(counter);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}