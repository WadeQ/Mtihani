package com.wadektech.mtihanirevise;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.wadektech.mtihanirevise.Auth.SignUpActivity;
import com.wadektech.mtihanirevise.UI.PastPapersActivity;

public class LoginActivity extends SignUpActivity {

    Button btnlogin , mLogin ;
    EditText loginEmail, loginPassword ;
    FirebaseAuth mAuth ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnlogin = findViewById(R.id.btn_login);
        loginEmail = findViewById(R.id.et_email);
        loginPassword = findViewById(R.id.et_password);
        mAuth = FirebaseAuth.getInstance() ;

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString() ;
                String pasword = loginPassword.getText().toString() ;
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pasword)){
                    Toast.makeText(getApplicationContext() , "All fields must be filled!", Toast.LENGTH_SHORT).show();
                }else {
                    mAuth.signInWithEmailAndPassword(email , pasword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Intent intent = new Intent(getApplicationContext(), PastPapersActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }else {
                                Toast.makeText(getApplicationContext() , "Authentication Failed! Please check your network and try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
