package com.example.silentobserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

    //Sensor Manager
    private SensorManager mSensorManager;

    // Accelerometer and gyroscope sensors, as retrieved from the sensor manager.
    private Sensor mSensorAcc;
    private Sensor mSensorGyro;
    private Sensor mSensorMagneticField;

    // TextViews to display current Accelerometer sensor values.
    private TextView xValue, yValue, zValue;

    //TextViews to display current Gyroscope sensor values.
    private TextView pValue, qValue, rValue;

    //local variables
    //float x=0,y=0,z=0,p=0,q=0,r=0;

    private float[] accelerometerReading = new float[3];
    private float[] gyroscopeReading = new float[3];
    private float[] magnetometerReading = new float[3];

    private float[] rotationMatrix = new float[9];
    private float[] orientationAngles = new float[3];

    //time variables
    long startTime= new Date().getTime();
    long currentTime;

    //Matrix to send
    ArrayList<Float> sensorMatrix = new ArrayList<Float>();
    int counter=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();


        // Initialize all view variables.
        xValue = (TextView) findViewById(R.id.xValue);
        yValue = (TextView) findViewById(R.id.yValue);
        zValue = (TextView) findViewById(R.id.zValue);

        pValue = (TextView) findViewById(R.id.pValue);
        qValue = (TextView) findViewById(R.id.qValue);
        rValue = (TextView) findViewById(R.id.rValue);


        // Get an instance of the sensor manager.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Get the error message from string resources.
        String sensor_error = "No sensor";

        // If either mSensorAcc or mSensorGyro are null, those sensors
        // are not available in the device.  Set the text to the error message
        if (mSensorAcc == null) {
            xValue.setText(sensor_error);
            yValue.setText(sensor_error);
            zValue.setText(sensor_error);
        }
        if (mSensorGyro == null) {
            pValue.setText(sensor_error);
            qValue.setText(sensor_error);
            rValue.setText(sensor_error);
        }
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
        if(currentTime-startTime>250){
            xValue.setText("x value is : " + accelerometerReading[0]);
            yValue.setText("y value is : " + accelerometerReading[1]);
            zValue.setText("z value is : " + accelerometerReading[2]);

            pValue.setText("x value is : " + gyroscopeReading[0]);
            qValue.setText("y value is : " + gyroscopeReading[1]);
            rValue.setText("z value is : " + gyroscopeReading[2]);

            SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
            SensorManager.getOrientation(rotationMatrix, orientationAngles);

            sensorMatrix.add(accelerometerReading[0]);
            sensorMatrix.add(accelerometerReading[1]);
            sensorMatrix.add(accelerometerReading[2]);
            sensorMatrix.add(gyroscopeReading[0]);
            sensorMatrix.add(gyroscopeReading[1]);
            sensorMatrix.add(gyroscopeReading[2]);
            sensorMatrix.add(magnetometerReading[0]);
            sensorMatrix.add(magnetometerReading[1]);
            sensorMatrix.add(magnetometerReading[2]);
            sensorMatrix.add(orientationAngles[0]);
            sensorMatrix.add(orientationAngles[1]);
            sensorMatrix.add(orientationAngles[2]);

            counter+=1;
            //System.out.println(startTime-currentTime + " ---- " + counter);
            startTime=currentTime;
            //rowCount++;



        }
        if(counter==128){
            counter=0;

            Map<String, ArrayList> userMap = new HashMap<>();
            userMap.put(userId,sensorMatrix);
            firebaseFirestore.collection("NewData/").add(userMap);
            sensorMatrix.clear();
            rowCount++;
            System.out.println(rowCount);
        }
        //System.out.println(counter);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}