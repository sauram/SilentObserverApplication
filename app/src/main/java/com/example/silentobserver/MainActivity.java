package com.example.silentobserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private Button logoutButton;
    private Button collectDataButton;
    private Button buttonUrl;
    private EditText inputUrl;
    private String URL;
    private TextView messageToDisplay;
    Context context;
    SharedPreferences sharedPref;
    private static final String ltUrl = "LocalTunnelURL";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        logoutButton = (Button) findViewById(R.id.logout_button);
        collectDataButton = (Button) findViewById(R.id.start_collecting_data);
        buttonUrl = (Button) findViewById(R.id.buttonurl);
        inputUrl = (EditText) findViewById(R.id.input_url);
        messageToDisplay = (TextView) findViewById(R.id.message_main_activity);

        context = this;
        sharedPref = context.getSharedPreferences(getString(R.string.input_url_key), Context.MODE_PRIVATE);

        buttonUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                URL = inputUrl.getText().toString();
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(ltUrl, URL);
                editor.commit();                                           // could use editor.apply() for asynchronously updating the disk data
                messageToDisplay.setText("URL is set. To change enter different url. Thanks!");
                inputUrl.setText("");
            }
        });

        collectDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToCollection();
            }
        });
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        System.out.println("Lmao, WORKING - Main Activity");
    }

    private void sendToCollection() {
        Intent collectionIntent = new Intent(MainActivity.this, SilentObserverService.class);
        ContextCompat.startForegroundService(this, collectionIntent);
        //finish();
    }

    private void logout() {
//        Intent collectionIntent = new Intent(MainActivity.this, SilentObserverService.class);
//        stopService(collectionIntent);
        firebaseAuth.signOut();
        sendToLogin();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            sendToLogin();
        }
    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

}