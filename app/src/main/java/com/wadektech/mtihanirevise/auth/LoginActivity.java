package com.wadektech.mtihanirevise.auth;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.wadektech.mtihanirevise.R;
import com.wadektech.mtihanirevise.ui.PastPapersActivity;

public class LoginActivity extends SignUpActivity {

    Button btnlogin , btnSignUp;
    EditText loginEmail, loginPassword ;
    FirebaseAuth mAuth ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnSignUp = findViewById(R.id.btn_sign_up);
        btnlogin = findViewById(R.id.btn_login);
        loginEmail = findViewById(R.id.et_email);
        loginPassword = findViewById(R.id.et_password);
        mAuth = FirebaseAuth.getInstance() ;

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString() ;
                String password = loginPassword.getText().toString() ;
                if (TextUtils.isEmpty(email)){
                    loginEmail.setError("Enter a valid email!");
                }else if (TextUtils.isEmpty(password)){
                    loginPassword.setError("Please enter a strong email of more that 6 characters!");
                }else {
                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Intent intent = new Intent(getApplicationContext(), PastPapersActivity.class);
                                startActivity(intent);
                                finish();
                            }else {
                                Toast.makeText(getApplicationContext() , "Authentication Failed! Please check your network and try again"+ task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
