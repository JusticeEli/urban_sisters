package com.justice.a2urbansisters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailEdtTxt, passwordEdtTxt, confirmPasswordEdtTxt;
    private TextView loginTxtView;
    private Button registerBtn;
    private Spinner spinner;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
public static final String ADMIN_CUSTOMER="admin or customer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initwidgets();
        setUpAnimation();
        setOnClickListeners();
        setValuesForSpinner();

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
            startActivity(new Intent(RegisterActivity.this, OrdersMainActivity.class));
            finish();

        }
    }


    private void setOnClickListeners() {

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fieldsAreEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog.show();
                String email = emailEdtTxt.getText().toString().trim();
                String password = passwordEdtTxt.getText().toString().trim();
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            setIsAdminOrCustomer();
                            startActivity(new Intent(RegisterActivity.this, OrdersMainActivity.class));
                            finish();

                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });


            }


        });


        loginTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, OrdersMainActivity.class));
            }
        });

    }

    private void setIsAdminOrCustomer() {
        Map<String,Object>map=new HashMap<>();

        if (spinner.getSelectedItemPosition()==0){
            map.put("isAdmin",false);

        }else {
            map.put("isAdmin",true);

        }


       firebaseFirestore.collection(ADMIN_CUSTOMER).document(firebaseAuth.getUid()).set(map).addOnCompleteListener(null);
    }

    private boolean fieldsAreEmpty() {
        if (emailEdtTxt.getText().toString().trim().isEmpty() || passwordEdtTxt.getText().toString().trim().isEmpty() || confirmPasswordEdtTxt.getText().toString().trim().isEmpty()) {
            return true;
        }
        return false;
    }


    private void initwidgets() {
        emailEdtTxt = findViewById(R.id.emailEdtTxt);
        passwordEdtTxt = findViewById(R.id.passwordEdtTxt);
        confirmPasswordEdtTxt = findViewById(R.id.confirmPasswordEdtTxt);
        loginTxtView = findViewById(R.id.loginTxtView);
        registerBtn = findViewById(R.id.registerBtn);
        spinner = findViewById(R.id.spinner);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Signing user in");
        progressDialog.setMessage("please wait...");
        progressDialog.setCancelable(false);

    }

    private void setValuesForSpinner() {
        String[] classGrade = {"Register Customer", "Register Admin"};
        ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, classGrade);
        spinner.setAdapter(arrayAdapter1);
    }
}
