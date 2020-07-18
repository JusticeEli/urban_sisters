package com.justice.a2urbansisters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity {
    private EditText emailEdtTxt, passwordEdtTxt;
    private TextView resetPasswordTxtView, registerTxtView;
    private Button loginBtn;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initwidgets();
        setUpAnimation();

        firebaseAuth = FirebaseAuth.getInstance();
        setOnClickListeners();
    }

    private void setUpAnimation() {
        RelativeLayout relativeLayout = findViewById(R.id.relativeLayout);
        ScrollView scrollView = findViewById(R.id.scrollView);
        AnimationDrawable animationDrawable1 = (AnimationDrawable) relativeLayout.getBackground();
        AnimationDrawable animationDrawable2 = (AnimationDrawable) scrollView.getBackground();

        animationDrawable1.setEnterFadeDuration(2000);
        animationDrawable2.setEnterFadeDuration(1500);
        animationDrawable1.setExitFadeDuration(1500);
        animationDrawable2.setExitFadeDuration(2000);
        animationDrawable1.start();
        animationDrawable2.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, OrdersMainActivity.class));
            finish();

        }
    }


    private void setOnClickListeners() {

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fieldsAreEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog.show();
                String email = emailEdtTxt.getText().toString().trim();
                String password = passwordEdtTxt.getText().toString().trim();
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, OrdersMainActivity.class));
                            finish();

                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });


            }


        });


        resetPasswordTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailEdtTxt.getText().toString().trim().isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please Fill Email !!", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog.show();
                firebaseAuth.sendPasswordResetEmail(emailEdtTxt.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Password Reset Send", Toast.LENGTH_SHORT).show();
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();

                        }
                        progressDialog.dismiss();
                    }
                });
            }
        });
        registerTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private boolean fieldsAreEmpty() {
        if (emailEdtTxt.getText().toString().trim().isEmpty() || passwordEdtTxt.getText().toString().trim().isEmpty()) {
            return true;
        }
        return false;
    }


    private void initwidgets() {
        emailEdtTxt = findViewById(R.id.emailEdtTxt);
        passwordEdtTxt = findViewById(R.id.passwordEdtTxt);
        resetPasswordTxtView = findViewById(R.id.resetPasswordEdtTxt);
        loginBtn = findViewById(R.id.loginBtn);
        registerTxtView = findViewById(R.id.registerTxtView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Login user in");
        progressDialog.setMessage("please wait...");
        progressDialog.setCancelable(false);
    }
}
