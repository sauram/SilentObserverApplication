package com.example.silentobserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerEmail;
    private EditText registerPassword;
    private EditText registerPasswordConfirm;
    private Button registerButton;
    private TextView registerLoginButton;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        registerEmail = (EditText) findViewById(R.id.reg_email);
        registerPassword = (EditText) findViewById(R.id.reg_password);
        registerPasswordConfirm = (EditText) findViewById(R.id.reg_confirm_password);
        registerButton = (Button) findViewById(R.id.reg_button);
        registerLoginButton = (TextView) findViewById(R.id.reg_login_txt);

        registerLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = registerEmail.getText().toString();
                String password = registerPassword.getText().toString();
                String confirmPassword = registerPasswordConfirm.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)){
                    if(password.equals(confirmPassword)){
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    sendToMain();
                                }else{
                                    Toast.makeText(RegisterActivity.this, "Error : "+ task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }else{
                        Toast.makeText(RegisterActivity.this, "Confirm Password doesn't match Password",Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(RegisterActivity.this, "Enter all details",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser= mAuth.getCurrentUser();
        if(currentUser != null){
            sendToMain();
        }

    }

    private void sendToMain() {
        Intent mainIntent= new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}